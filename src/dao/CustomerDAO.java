package dao;

import java.sql.*;
import java.util.ArrayList;

import Enum.GenderEnum;
import database.DatabaseConnection;
import entity.Customer;
import entity.Account;
import utils.DBUtil;

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
            pstmt.setString(4, t.getGender().name());
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
            pstmt.setString(4, t.getGender().name());
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
    public ArrayList<Customer> selectAll() {
        ArrayList<Customer> ketQua = new ArrayList<>();
        String sql = "SELECT c.*, a.accountID, a.userName FROM customer c JOIN account a ON c.accountID = a.accountID";

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
        String sql = "SELECT c.*, a.accountID, a.userName FROM customer c " +
                     "JOIN account a ON c.accountID = a.accountID WHERE c.customer_ID = ?";

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
    public ArrayList<Customer> selectByCondition(String condition) {
        throw new UnsupportedOperationException("Không hỗ trợ `selectByCondition` để tránh SQL Injection.");
    }

    private Customer getCustomerFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("customer_ID");
        String lName = rs.getString("lastName");
        String fName = rs.getString("firstName");
        String phone = rs.getString("phoneNumber");
        // Chuyển đổi từ int sang GenderEnum
        int genderValue = rs.getInt("sex"); // Giá trị 1, 2, 3 từ database
        GenderEnum gender = GenderEnum.fromCode(genderValue);
        String citizenNumber = rs.getString("citizenNumber");
        String address = rs.getString("address");

        // Lấy thông tin tài khoản
        int accountID = rs.getInt("accountID");
        String userName = rs.getString("userName");
        Account account = new Account(accountID, userName, null, null, null); // Không lấy mật khẩu/email để bảo mật

        return new Customer(id, lName, fName, phone, gender, citizenNumber, address, account);
    }
}
