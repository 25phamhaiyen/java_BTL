package dao;

import entity.OrderDetail;
import entity.Order;
import entity.Service;
import utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailDAO implements DAOInterface<OrderDetail> {
    
	private static OrderDetailDAO instance;

	public static OrderDetailDAO getInstance() {
	    if (instance == null) {
	        instance = new OrderDetailDAO();
	    }
	    return instance;
	}


	public int insert(OrderDetail orderDetail) {
	    String sql = "INSERT INTO order_detail (OrderID, ServiceID, Quantity, UnitPrice) VALUES (?, ?, ?, ?)";

	    try (Connection con = DatabaseConnection.getConnection();
	         PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

	        //  Lấy giá từ bảng service
	        ServiceDAO serviceDAO = ServiceDAO.getInstance();
	        Service service = serviceDAO.selectById(orderDetail.getService().getServiceID());
	        if (service == null) {
	            System.err.println(" Lỗi: Không tìm thấy dịch vụ!");
	            return 0;
	        }
	        BigDecimal unitPrice = BigDecimal.valueOf(service.getCostPrice());


	        pstmt.setInt(1, orderDetail.getOrder().getOrderId());
	        pstmt.setInt(2, orderDetail.getService().getServiceID());
	        pstmt.setInt(3, orderDetail.getQuantity());
	        pstmt.setBigDecimal(4, unitPrice);

	        int affectedRows = pstmt.executeUpdate();
	        if (affectedRows > 0) {
	            try (ResultSet rs = pstmt.getGeneratedKeys()) {
	                if (rs.next()) {
	                    orderDetail.setOrderDetailId(rs.getInt(1));
	                }
	            }
	            OrderDAO.getInstance().updateTotalPrice(orderDetail.getOrder().getOrderId());
	        }
	        return affectedRows;
	    } catch (SQLException e) {
	        System.err.println("❌ Lỗi khi thêm chi tiết đơn hàng: " + e.getMessage());
	        return 0;
	    }
	}


    @Override
    public int update(OrderDetail orderDetail) {
        String sql = "UPDATE order_detail SET ServiceID=?, Quantity=?, UnitPrice=? WHERE OrderDetailID=?";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
        //  Lấy giá từ bảng service
	        ServiceDAO serviceDAO = ServiceDAO.getInstance();
	        Service service = serviceDAO.selectById(orderDetail.getService().getServiceID());
	        if (service == null) {
	            System.err.println(" Lỗi: Không tìm thấy dịch vụ!");
	            return 0;
	        }
	        BigDecimal unitPrice = BigDecimal.valueOf(service.getCostPrice());

	        pstmt.setInt(1, orderDetail.getService().getServiceID());
            pstmt.setInt(2, orderDetail.getQuantity());
            pstmt.setBigDecimal(3, unitPrice);
            pstmt.setInt(4, orderDetail.getOrderDetailId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // Cập nhật tổng tiền đơn hàng sau khi sửa
                OrderDAO.getInstance().updateTotalPrice(orderDetail.getOrder().getOrderId());
            }
            return affectedRows;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật chi tiết đơn hàng: " + e.getMessage());
            return 0;
        }
    }


    @Override
    public int delete(OrderDetail orderDetail) {
        String sql = "DELETE FROM order_detail WHERE OrderDetailID=?";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderDetail.getOrderDetailId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // Cập nhật tổng tiền đơn hàng sau khi xóa
            	 OrderDAO.getInstance().updateTotalPrice(orderDetail.getOrder().getOrderId());

            }
            return affectedRows;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa chi tiết đơn hàng: " + e.getMessage());
            return 0;
        }
    }


    @Override
    public List<OrderDetail> selectAll() {
        List<OrderDetail> list = new ArrayList<>();
        String sql = "SELECT * FROM order_detail";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapResultSetToOrderDetail(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách chi tiết đơn hàng: " + e.getMessage());
        }
        return list;
    }

    @Override
    public OrderDetail selectById(OrderDetail orderDetail) {
        return selectById(orderDetail.getOrderDetailId());
    }
    
    public OrderDetail selectById(int orderDetailId) {
        String sql = "SELECT * FROM order_detail WHERE OrderDetailID = ?";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderDetailId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrderDetail(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm chi tiết đơn hàng theo ID: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public List<OrderDetail> selectByCondition(String condition, Object... params) {
        List<OrderDetail> list = new ArrayList<>();
        String sql = "SELECT * FROM order_detail WHERE " + condition;
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrderDetail(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn có điều kiện: " + e.getMessage());
        }
        return list;
    }

    private OrderDetail mapResultSetToOrderDetail(ResultSet rs) throws SQLException {
        int orderDetailId = rs.getInt("OrderDetailID");
        int orderId = rs.getInt("OrderID");
        int serviceId = rs.getInt("ServiceID");
        int quantity = rs.getInt("Quantity");
        //  Lấy costPrice từ Service
        Service service = ServiceDAO.getInstance().selectById(serviceId);
        if (service == null) {
            throw new SQLException("Không tìm thấy service với ID: " + serviceId);
        }
        BigDecimal unitPrice = BigDecimal.valueOf(service.getCostPrice());
        
        Order order = new Order();
        order.setOrderId(orderId);
        
        
        return new OrderDetail(orderDetailId, order, service, quantity, unitPrice);
    }
}
