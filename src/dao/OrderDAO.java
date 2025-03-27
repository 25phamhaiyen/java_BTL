package dao;

import database.DatabaseConnection;
import entity.Order;
import entity.Customer;
import entity.HappenStatus;
import entity.Staff;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO implements DAOInterface<Order> {
    
    public static OrderDAO getInstance() {
        return new OrderDAO();
    }

    @Override
    public int insert(Order order) {
        String sql = "INSERT INTO `order` (orderDate, appointmentDate, orderType, Total, Customer_ID, StaffID, HappenStatusID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setTimestamp(1, order.getOrderDate());
            pstmt.setTimestamp(2, order.getAppointmentDate());
            pstmt.setString(3, order.getOrderType());
            pstmt.setDouble(4, order.getTotal());
            pstmt.setInt(5, order.getCustomer().getCustomerID());
            pstmt.setInt(6, order.getStaff() != null ? order.getStaff().getStaffID() : null);
            pstmt.setInt(7, order.getHappentStatus().getHappenStatusID());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        order.setOrderId(rs.getInt(1));
                    }
                }
            }
            return affectedRows;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm đơn hàng: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int update(Order order) {
        String sql = "UPDATE `order` SET orderDate=?, appointmentDate=?, orderType=?, Total=?, Customer_ID=?, StaffID=?, HappenStatusID=? WHERE orderID=?";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, order.getOrderDate());
            pstmt.setTimestamp(2, order.getAppointmentDate());
            pstmt.setString(3, order.getOrderType());
            pstmt.setDouble(4, order.getTotal());
            pstmt.setInt(5, order.getCustomer().getCustomerID());
            pstmt.setInt(6, order.getStaff() != null ? order.getStaff().getStaffID() : null);
            pstmt.setInt(7, order.getHappentStatus().getHappenStatusID());
            pstmt.setInt(8, order.getOrderId());
            
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật đơn hàng: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int delete(Order order) {
        String sql = "DELETE FROM `order` WHERE orderID=?";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, order.getOrderId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa đơn hàng: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Order> selectAll() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM `order`";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách đơn hàng: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Order selectById(Order order) {
        return selectById(order.getOrderId());
    }
    
    public Order selectById(int orderId) {
        String sql = "SELECT * FROM `order` WHERE orderID = ?";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm đơn hàng theo ID: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public List<Order> selectByCondition(String condition, Object... params) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM `order` WHERE " + condition;
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn theo điều kiện: " + e.getMessage());
        }
        return list;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        int orderId = rs.getInt("orderID");
        Timestamp orderDate = rs.getTimestamp("orderDate");
        Timestamp appointmentDate = rs.getTimestamp("appointmentDate");
        String orderType = rs.getString("orderType");
        double total = rs.getDouble("Total");

        Customer customer = new Customer();
        customer.setCustomerID(rs.getInt("Customer_ID"));

        Staff staff = new Staff();
        if (rs.getObject("StaffID") != null) {
            staff.setStaffID(rs.getInt("StaffID"));
        }

        HappenStatus happenStatus = new HappenStatus();
        happenStatus.setHappenStatusID(rs.getInt("HappenStatusID"));

        return new Order(orderId, orderDate, appointmentDate, orderType, total, customer, staff, happenStatus);
    }


}
