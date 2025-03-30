package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import Enum.GenderEnum;
import entity.Customer;
import entity.Account;
import utils.DBUtil;
import utils.DatabaseConnection;

public class CustomerDAO implements DAOInterface<Customer> {

    public static CustomerDAO getInstance() {
        return new CustomerDAO();
    }

    @Override
    public int insert(Customer t) {
        int ketQua = 0;
        String sql = "INSERT INTO customer (lastName, firstName, phoneNumber, sex, citizenNumber, address, accountID) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, t.getLastName());
            pstmt.setString(2, t.getFirstName());
            pstmt.setString(3, t.getPhoneNumber());
            pstmt.setInt(4, t.getGender().getCode()); 
            pstmt.setString(5, t.getCitizenNumber());
            pstmt.setString(6, t.getAddress());
            pstmt.setInt(7, t.getAccount().getAccountID());

            ketQua = pstmt.executeUpdate();
            
            // Lấy ID mới tự động tăng
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                t.setCustomerID(rs.getInt(1));
            }

            System.out.println("INSERT thành công, " + ketQua + " dòng bị thay đổi.");
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm khách hàng: " + e.getMessage());
        } finally {
            DBUtil.closeResources(con, pstmt);
        }
        return ketQua;
    }

    @Override
    public int update(Customer t) {
        int ketQua = 0;
        String sql = "UPDATE customer SET lastName = ?, firstName = ?, phoneNumber = ?, sex = ?, citizenNumber = ?, address = ?, accountID = ? " +
                     "WHERE customer_ID = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, t.getLastName());
            pstmt.setString(2, t.getFirstName());
            pstmt.setString(3, t.getPhoneNumber());
            pstmt.setInt(4, t.getGender().getCode()); 
            pstmt.setString(5, t.getCitizenNumber());
            pstmt.setString(6, t.getAddress());
            pstmt.setInt(7, t.getAccount().getAccountID());
            pstmt.setInt(8, t.getCustomerID());

            ketQua = pstmt.executeUpdate();
            System.out.println("UPDATE thành công, " + ketQua + " dòng bị thay đổi.");

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật khách hàng: " + e.getMessage());
        } finally {
            DBUtil.closeResources(con, pstmt);
        }
        return ketQua;
    }

    @Override
    public int delete(Customer t) {
        int ketQua = 0;
        String sql = "DELETE FROM Customer WHERE customer_ID = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, t.getCustomerID());

            ketQua = pstmt.executeUpdate();
            System.out.println("DELETE thành công, " + ketQua + " dòng bị thay đổi.");

        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa khách hàng: " + e.getMessage());
        } finally {
            DBUtil.closeResources(con, pstmt);
        }
        return ketQua;
    }

    @Override
    public List<Customer> selectAll() {
        List<Customer> ketQua = new ArrayList<>();
        String sql = "SELECT c.*, a.accountID, a.UN_Username, a.Email FROM customer c " +
                "JOIN account a ON c.accountID = a.AccountID";

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
        String sql = "SELECT c.*, a.accountID, a.UN_Username, a.Email " +
                "FROM customer c " +
                "JOIN account a ON c.accountID = a.accountID " +
                "WHERE c.customer_ID = ?";
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, customerID); 

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
        return selectById(t.getCustomerID()); // Gọi lại phương thức mới
    }

    
    @Override
    public List<Customer> selectByCondition(String condition, Object... params) {
        List<Customer> customers = new ArrayList<>();

        if (condition == null || condition.trim().isEmpty()) {
            throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
        }

        String sql = "SELECT c.*, a.accountID, a.UN_Username, a.Email FROM customer c " +
                "JOIN account a ON c.accountID = a.accountID WHERE " + condition;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

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
    private Customer getCustomerFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("customer_ID");
        String lName = rs.getString("lastName");
        String fName = rs.getString("firstName");
        String phone = rs.getString("phoneNumber");

        // Chuyển đổi từ int sang GenderEnum (có kiểm tra null)
        int genderValue = rs.getInt("sex");
        GenderEnum gender = GenderEnum.fromCode(genderValue);

        String citizenNumber = rs.getString("citizenNumber");
        String address = rs.getString("address");

        // Lấy thông tin tài khoản (có kiểm tra NULL)
        int accountID = rs.getInt("accountID");
        String userName = rs.getString("UN_Username");
        String email = rs.getString("Email"); // 🛠 Lấy email từ DB

        Account account = new Account(accountID, userName, null, email, null);

        return new Customer(id, lName, fName, phone, gender, citizenNumber, address, account);
    }

}
