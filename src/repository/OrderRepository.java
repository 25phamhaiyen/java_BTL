package repository;

import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import enums.TypeOrder;
import model.Customer;
import model.HappenStatus;
import model.Order;
import model.Staff;

public class OrderRepository implements IRepository<Order> {
    
    public static OrderRepository getInstance() {
        return new OrderRepository();
    }
    
    public double calculateTotal(int orderId) {
        String sql = "SELECT SUM(TotalPrice) FROM order_detail WHERE OrderID = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1); // Lấy tổng tiền từ SQL
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tính tổng tiền: " + e.getMessage());
        }
        return 0.0; // Nếu có lỗi, trả về 0
    }

    public void updateTotal(int orderId, double total) {
        String sql = "UPDATE `order` SET Total = ? WHERE orderID = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setDouble(1, total);
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật tổng tiền: " + e.getMessage());
        }
    }

    public void updateTotalPrice(int orderId) {
        String sql = "UPDATE `order` SET Total = (SELECT COALESCE(SUM(TotalPrice), 0) FROM order_detail WHERE OrderID = ?) WHERE orderID = ?";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật tổng tiền đơn hàng: " + e.getMessage());
        }
    }

    public int insert(Order order) {
        String sql = "INSERT INTO `order` (orderDate, appointmentDate, orderType, Total, Customer_ID, StaffID, HappenStatusID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setTimestamp(1, order.getOrderDate());
            pstmt.setTimestamp(2, order.getAppointmentDate());
            pstmt.setString(3, order.getOrderType().getDescription());
            pstmt.setDouble(4, 0.0); // Ban đầu đặt tổng tiền là 0
            pstmt.setInt(5, order.getCustomer().getCustomerID());
            pstmt.setObject(6, order.getStaff() != null ? order.getStaff().getStaffID() : null);
            pstmt.setInt(7, order.getHappentStatus().getHappenStatusID());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int orderId = rs.getInt(1);
                        order.setOrderId(orderId);
                        
                        System.out.println("✅ Thêm đơn hàng thành công! OrderID = " + orderId);
                        
                        // Quan trọng: Cập nhật tổng tiền sau khi thêm OrderDetail
                        updateTotalPrice(orderId);
                    }
                }
            }
            return affectedRows;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi thêm đơn hàng: " + e.getMessage());
            return 0;
        }
    }



    @Override
    public int update(Order order) {
        String sql = "UPDATE `order` SET orderDate=?, appointmentDate=?, orderType=?, Customer_ID=?, StaffID=?, HappenStatusID=? WHERE orderID=?";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, order.getOrderDate());
            pstmt.setTimestamp(2, order.getAppointmentDate());
            pstmt.setString(3, order.getOrderType().getDescription());
            pstmt.setInt(4, order.getCustomer().getCustomerID());
            pstmt.setObject(5, order.getStaff() != null ? order.getStaff().getStaffID() : null);
            pstmt.setInt(6, order.getHappentStatus().getHappenStatusID());
            pstmt.setInt(7, order.getOrderId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✅ Cập nhật đơn hàng thành công! OrderID = " + order.getOrderId());
                
                // Cập nhật lại tổng tiền của đơn hàng sau khi update
                updateTotalPrice(order.getOrderId());
            }
            return affectedRows;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi cập nhật đơn hàng: " + e.getMessage());
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
        double total = rs.getDouble("Total");

        // Kiểm tra TypeOrder NULL
        TypeOrder orderType = null;
        String orderTypeStr = rs.getString("orderType");
        if (orderTypeStr != null) {
            try {
                orderType = TypeOrder.valueOf(orderTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Lỗi: orderType không hợp lệ trong DB: " + orderTypeStr);
            }
        }

        Customer customer = new Customer();
        customer.setCustomerID(rs.getInt("Customer_ID"));

        // Kiểm tra Staff NULL
        Staff staff = null;
        if (rs.getObject("StaffID") != null) {
            staff = new Staff();
            staff.setStaffID(rs.getInt("StaffID"));
        }

        // Kiểm tra HappenStatus NULL
        HappenStatus happenStatus = null;
        if (rs.getObject("HappenStatusID") != null) {
            happenStatus = new HappenStatus();
            happenStatus.setHappenStatusID(rs.getInt("HappenStatusID"));
        }

        return new Order(orderId, orderDate, appointmentDate, orderType, total, customer, staff, happenStatus);
    }



}
