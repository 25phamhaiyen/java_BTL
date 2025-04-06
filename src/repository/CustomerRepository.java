package repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import enums.GenderEnum;
import exception.BusinessException;
import model.Account;
import model.Customer;
import utils.DBUtil;
import utils.DatabaseConnection;

public class CustomerRepository implements IRepository<Customer> {

	public static CustomerRepository getInstance() {
		return new CustomerRepository();
	}

	@Override
	public int insert(Customer t) {
	    int ketQua = 0;
	    String personSql = "INSERT INTO person (lastName, firstName, phoneNumber, sex, citizenNumber, address, email) "
	                       + "VALUES (?, ?, ?, ?, ?, ?, ?)";

	    String customerSql = "INSERT INTO customer (PersonID, AccountID, registrationDate, loyaltyPoints) "
	                         + "VALUES (?, ?, ?, ?)";

	    Connection con = null;
	    PreparedStatement personPstmt = null;
	    PreparedStatement customerPstmt = null;
	    ResultSet personRs = null;

	    try {
	        con = DatabaseConnection.getConnection();
	        
	        // 1. Thêm thông tin vào bảng person
	        personPstmt = con.prepareStatement(personSql, Statement.RETURN_GENERATED_KEYS);
	        personPstmt.setString(1, t.getLastName());
	        personPstmt.setString(2, t.getFirstName());
	        personPstmt.setString(3, t.getPhoneNumber());
	        personPstmt.setInt(4, t.getGender().getCode());
	        personPstmt.setString(5, t.getCitizenNumber());
	        personPstmt.setString(6, t.getAddress());
	        personPstmt.setString(7, t.getEmail());

	        int personRowsAffected = personPstmt.executeUpdate();
	        if (personRowsAffected > 0) {
	            personRs = personPstmt.getGeneratedKeys();
	            if (personRs.next()) {
	                // Lấy PersonID vừa tạo
	                int personID = personRs.getInt(1);

	                // 2. Thêm thông tin vào bảng customer với PersonID
	                customerPstmt = con.prepareStatement(customerSql);
	                customerPstmt.setInt(1, personID); // Gắn PersonID
	                customerPstmt.setInt(2, t.getAccount().getAccountID());
	                customerPstmt.setDate(3, new java.sql.Date(t.getRegistrationDate().getTime()));
	                customerPstmt.setInt(4, t.getLoyaltyPoints());

	                ketQua = customerPstmt.executeUpdate();

	                // Nếu INSERT thành công vào customer, cập nhật lại customer ID cho đối tượng t
	                if (ketQua > 0) {
	                    t.setId(personID); // Cập nhật ID của customer vào đối tượng
	                }

	                System.out.println("INSERT thành công, " + ketQua + " dòng bị thay đổi.");
	            }
	        }

	    } catch (SQLException e) {
	        throw new BusinessException("Lỗi khi thêm khách hàng: " + e.getMessage());
	    } finally {
	        DBUtil.closeResources(con, personPstmt, personRs);
	        DBUtil.closeResources(null, customerPstmt, null);
	    }

	    return ketQua;
	}

	@Override
	public int update(Customer t) {
	    int ketQua = 0;
	    String updatePersonSql = "UPDATE person SET lastName = ?, firstName = ?, phoneNumber = ?, sex = ?, citizenNumber = ?, address = ?, email = ? WHERE PersonID = ?";
	    String updateCustomerSql = "UPDATE customer SET registrationDate = ?, loyaltyPoints = ?, AccountID = ? WHERE PersonID = ?";

	    // Declare the connection outside the try block
	    Connection con = null;

	    try {
	        con = DatabaseConnection.getConnection();  // Initialize connection here
	        // Start a transaction (turn off auto-commit)
	        con.setAutoCommit(false);
	        
	        // 1. Update the 'person' table
	        try (PreparedStatement pstmt = con.prepareStatement(updatePersonSql)) {
	            pstmt.setString(1, t.getLastName());
	            pstmt.setString(2, t.getFirstName());
	            pstmt.setString(3, t.getPhoneNumber());
	            pstmt.setInt(4, t.getGender().getCode());
	            pstmt.setString(5, t.getCitizenNumber());
	            pstmt.setString(6, t.getAddress());
	            pstmt.setString(7, t.getEmail());
	            pstmt.setInt(8, t.getId()); // The ID from the 'customer' table (which is the 'personID')

	            int rowsAffected = pstmt.executeUpdate();
	            if (rowsAffected > 0) {
	                System.out.println("Person update thành công.");
	            }
	        }

	        // 2. Update the 'customer' table
	        try (PreparedStatement pstmt = con.prepareStatement(updateCustomerSql)) {
	            pstmt.setDate(1, new java.sql.Date(t.getRegistrationDate().getTime())); // Updated registration date
	            pstmt.setInt(2, t.getLoyaltyPoints()); // Updated loyalty points
	            pstmt.setInt(3, t.getAccount().getAccountID()); // Updated accountID
	            pstmt.setInt(4, t.getId()); // The ID from the 'customer' table (which is the 'personID')

	            int rowsAffected = pstmt.executeUpdate();
	            if (rowsAffected > 0) {
	                System.out.println("Customer update thành công.");
	            }
	        }

	        // Commit the transaction if both updates are successful
	        con.commit();
	        ketQua = 1; // Successful update
	    } catch (SQLException e) {
	        // Rollback the transaction if any error occurs
	        try {
	            if (con != null) {
	                con.rollback();
	            }
	        } catch (SQLException rollbackEx) {
	            throw new BusinessException("Lỗi khi rollback giao dịch: " + rollbackEx.getMessage());
	        }
	        if (e.getMessage().contains("Duplicate entry")) {
	            throw new BusinessException("Email hoặc số điện thoại đã tồn tại.");
	        }
	        throw new BusinessException("Lỗi SQL khi cập nhật khách hàng: " + e.getMessage());
	    } finally {
	        try {
	            if (con != null) {
	                con.setAutoCommit(true); // Reset to default auto-commit behavior
	            }
	        } catch (SQLException e) {
	            throw new BusinessException("Lỗi khi reset auto-commit: " + e.getMessage());
	        }
	    }

	    return ketQua;
	}


	@Override
	public int delete(Customer t) {
	    int ketQua = 0;
	    String deleteCustomerSql = "DELETE FROM customer WHERE PersonID = ?";
	    String deletePersonSql = "DELETE FROM person WHERE PersonID = ?"; // Deleting from person based on PersonID

	    Connection con = null;

	    try {
	        con = DatabaseConnection.getConnection();
	        con.setAutoCommit(false);  // Start a transaction

	        // 1. Delete from customer table
	        try (PreparedStatement pstmt = con.prepareStatement(deleteCustomerSql)) {
	            pstmt.setInt(1, t.getId()); // Delete customer by PersonID
	            ketQua = pstmt.executeUpdate();
	            System.out.println("DELETE from customer thành công, " + ketQua + " dòng bị thay đổi.");
	        }

	        // 2. Delete from person table
	        try (PreparedStatement pstmt = con.prepareStatement(deletePersonSql)) {
	            pstmt.setInt(1, t.getId()); // Assuming PersonID in customer is the same as PersonID in person table
	            int personDeleteCount = pstmt.executeUpdate();
	            System.out.println("DELETE from person thành công, " + personDeleteCount + " dòng bị thay đổi.");
	        }

	        // Commit transaction if both deletes were successful
	        con.commit();
	    } catch (SQLException e) {
	        // Rollback transaction if there's any error
	        try {
	            if (con != null) {
	                con.rollback();
	            }
	        } catch (SQLException rollbackEx) {
	            System.err.println("Lỗi khi rollback giao dịch: " + rollbackEx.getMessage());
	        }
	        System.err.println("Lỗi khi xóa khách hàng: " + e.getMessage());
	    } finally {
	        try {
	            if (con != null) {
	                con.setAutoCommit(true);  // Reset to default auto-commit behavior
	            }
	        } catch (SQLException e) {
	            System.err.println("Lỗi khi reset auto-commit: " + e.getMessage());
	        }
	        DBUtil.closeResources(con, null);  // Closing resources
	    }

	    return ketQua;
	}

	@Override
	public List<Customer> selectAll() {
	    List<Customer> ketQua = new ArrayList<>();
	    String sql = "SELECT p.*, a.AccountID, a.UN_Username, a.Email, c.registrationDate, c.loyaltyPoints FROM customer c "
	            + "JOIN person p ON c.PersonID = p.PersonID "
	            + "JOIN account a ON c.AccountID = a.AccountID";

	    Connection con = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    try {
	        con = DatabaseConnection.getConnection();
	        pstmt = con.prepareStatement(sql);
	        rs = pstmt.executeQuery();

	        while (rs.next()) {
	            ketQua.add(getCustomerFromResultSet(rs));
	        }

	    } catch (SQLException e) {
	        System.err.println("Lỗi khi lấy danh sách khách hàng: " + e.getMessage());
	    } finally {
	        DBUtil.closeResources(con, pstmt, rs);
	    }
	    return ketQua;
	}


	public Customer selectById(int customerID) {
	    Customer ketQua = null;
	    String sql = "SELECT p.*, c.AccountID, a.UN_Username, a.Email, c.registrationDate, c.loyaltyPoints "
	            + "FROM customer c "
	            + "JOIN person p ON c.PersonID = p.PersonID "
	            + "JOIN account a ON c.AccountID = a.AccountID "
	            + "WHERE c.PersonID = ?"; 

	    Connection con = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    try {
	        con = DatabaseConnection.getConnection();
	        pstmt = con.prepareStatement(sql);
	        pstmt.setInt(1, customerID);  // customerID là PersonID

	        rs = pstmt.executeQuery();
	        if (rs.next()) {
	            ketQua = getCustomerFromResultSet(rs);
	        }

	    } catch (SQLException e) {
	        System.err.println("Lỗi khi tìm khách hàng theo ID: " + e.getMessage());
	    } finally {
	        DBUtil.closeResources(con, pstmt, rs);
	    }
	    return ketQua;
	}


	@Override
	public Customer selectById(Customer t) {
		return selectById(t.getId()); // Gọi lại phương thức mới
	}

	@Override
	public List<Customer> selectByCondition(String condition, Object... params) {
	    List<Customer> customers = new ArrayList<>();

	    if (condition == null || condition.trim().isEmpty()) {
	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
	    }

	    String sql = "SELECT p.*, c.AccountID, a.UN_Username, a.Email, c.registrationDate, c.loyaltyPoints FROM customer c "
	            + "JOIN person p ON c.PersonID = p.PersonID "
	            + "JOIN account a ON c.AccountID = a.AccountID WHERE " + condition;

	    try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

	        for (int i = 0; i < params.length; i++) {
	            pstmt.setObject(i + 1, params[i]);
	        }

	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                customers.add(getCustomerFromResultSet(rs));
	            }
	        }
	    } catch (SQLException e) {
	        System.err.println("Lỗi khi truy vấn Customer theo điều kiện: " + e.getMessage());
	    }
	    return customers;
	}

	public void deleteAll(Connection conn) throws SQLException {
        String sql = "DELETE FROM customer"; // Câu lệnh SQL để xóa tất cả khách hàng

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }


	public void resetAutoIncrement(Connection conn) throws SQLException {
		String sql = "ALTER TABLE Customer AUTO_INCREMENT = 1";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.executeUpdate();
		}
	}

	private Customer getCustomerFromResultSet(ResultSet rs) throws SQLException {
	    int personID = rs.getInt("PersonID");  // Đọc PersonID từ bảng person
	    String lName = rs.getString("lastName");
	    String fName = rs.getString("firstName");
	    String phone = rs.getString("phoneNumber");
	    GenderEnum gender = GenderEnum.fromCode(rs.getInt("sex"));
	    String citizenNumber = rs.getString("citizenNumber");
	    String address = rs.getString("address");
	    String email = rs.getString("email");
	    Date registrationDate = rs.getDate("registrationDate");
	    int loyaltyPoints = rs.getInt("loyaltyPoints");

	    int accountID = rs.getInt("AccountID");
	    Account account = null;
	    if (rs.wasNull()) {
	        account = null;
	    } else {
	        String userName = rs.getString("UN_Username");
	        String accountEmail = rs.getString("Email");
	        account = new Account(accountID, userName, null, accountEmail, null);
	    }

	    return new Customer(personID, lName, fName, gender, phone, citizenNumber, address, email, account, registrationDate,
	            loyaltyPoints);
	}


}
