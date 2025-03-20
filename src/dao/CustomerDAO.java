package dao;

import java.sql.*;
import java.util.ArrayList;

import datatbase.DatabaseConnection;
import entity.Customer;
import utils.DBUtil;

public class CustomerDAO implements DAOInterface<Customer> {

    public static CustomerDAO getInstance() {
        return new CustomerDAO();
    }

    @Override
    public int insert(Customer t) {
        int ketQua = 0;
        String sql = "INSERT INTO customer (customer_ID, lastName, firstName, phoneNumber, sex, citizenNumber, address) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setInt(1, t.getCustomer_ID());
            pstmt.setString(2, t.getLastName());
            pstmt.setString(3, t.getFirstName());
            pstmt.setString(4, t.getPhoneNumber());
            pstmt.setInt(5, t.getSex());
            pstmt.setString(6, t.getCitizenNumber());
            pstmt.setString(7, t.getAddress());

            ketQua = pstmt.executeUpdate();
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
        String sql = "UPDATE customer SET lastName = ?, firstName = ?, phoneNumber = ?, sex = ?, citizenNumber = ?, address = ? " +
                     "WHERE customer_ID = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, t.getLastName());
            pstmt.setString(2, t.getFirstName());
            pstmt.setString(3, t.getPhoneNumber());
            pstmt.setInt(4, t.getSex());
            pstmt.setString(5, t.getCitizenNumber());
            pstmt.setString(6, t.getAddress());
            pstmt.setInt(7, t.getCustomer_ID());

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
            pstmt.setInt(1, t.getCustomer_ID());

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
        String sql = "SELECT * FROM customer";

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

    @Override
    public Customer selectById(Customer t) {
        Customer ketQua = null;
        String sql = "SELECT * FROM customer WHERE customer_ID = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, t.getCustomer_ID());

            rs = pstmt.executeQuery();
            if (rs.next()) {
                ketQua = getCustomerFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm khách hàng: " + e.getMessage());
        } finally {
            DBUtil.closeResources(con, pstmt, rs);
        }
        return ketQua;
    }

    @Override
    public ArrayList<Customer> selectByCondition(String condition) {
        ArrayList<Customer> ketQua = new ArrayList<>();
        String sql = "SELECT * FROM customer WHERE " + condition;

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
            System.err.println("Lỗi khi tìm kiếm khách hàng: " + e.getMessage());
        } finally {
            DBUtil.closeResources(con, pstmt, rs);
        }
        return ketQua;
    }

    private Customer getCustomerFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("customer_ID");
        String lName = rs.getString("lastName");
        String fName = rs.getString("firstName");
        String phone = rs.getString("phoneNumber");
        int sex = rs.getInt("sex");
        String citizenNumber = rs.getString("citizenNumber");
        String address = rs.getString("address");

        return new Customer(id, lName, fName, phone, sex, citizenNumber, address);
    }
}
