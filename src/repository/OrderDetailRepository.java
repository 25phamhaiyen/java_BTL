package repository;

import utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.Order;
import model.OrderDetail;
import model.Service;

public class OrderDetailRepository implements IRepository<OrderDetail> {
    
	private static OrderDetailRepository instance;

	public static OrderDetailRepository getInstance() {
	    if (instance == null) {
	        instance = new OrderDetailRepository();
	    }
	    return instance;
	}


	public int insert(OrderDetail orderDetail) {
	    String sql = "INSERT INTO order_detail (order_id, service_id, quantity, price) VALUES (?, ?, ?, ?)";

	    try (Connection con = DatabaseConnection.getConnection();
	         PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

	        // Lấy giá và trạng thái từ bảng service
	        ServiceRepository serviceRepository = ServiceRepository.getInstance();
	        Service service = serviceRepository.selectById(orderDetail.getService().getServiceId());
	        if (service == null) {
	            System.err.println("Lỗi: Không tìm thấy dịch vụ!");
	            return 0;
	        }
	        // Kiểm tra trạng thái active
	        if (!service.isActive()) {
	            System.err.println("Lỗi: Dịch vụ " + service.getName() + " đã ngừng hoạt động!");
	            return 0;
	        }
	        BigDecimal unitPrice = BigDecimal.valueOf(service.getPrice());

	        pstmt.setInt(1, orderDetail.getOrder().getOrderId());
	        pstmt.setInt(2, orderDetail.getService().getServiceId());
	        pstmt.setInt(3, orderDetail.getQuantity());
	        pstmt.setBigDecimal(4, unitPrice);

	        int affectedRows = pstmt.executeUpdate();
	        if (affectedRows > 0) {
	            try (ResultSet rs = pstmt.getGeneratedKeys()) {
	                if (rs.next()) {
	                    orderDetail.setOrderDetailId(rs.getInt(1));
	                }
	            }
	            OrderRepository.getInstance().updateTotalPrice(orderDetail.getOrder().getOrderId());
	        }
	        return affectedRows;
	    } catch (SQLException e) {
	        System.err.println("Lỗi khi thêm chi tiết đơn hàng: " + e.getMessage());
	        return 0;
	    }
	}


    @Override
    public int update(OrderDetail orderDetail) {
        String sql = "UPDATE order_detail SET service_id=?, quantity=?, price=? WHERE order_detail_id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            // Lấy giá và trạng thái từ bảng service
            ServiceRepository serviceRepository = ServiceRepository.getInstance();
            Service service = serviceRepository.selectById(orderDetail.getService().getServiceId());
            if (service == null) {
                System.err.println("Lỗi: Không tìm thấy dịch vụ!");
                return 0;
            }
            // Kiểm tra trạng thái active
            if (!service.isActive()) {
                System.err.println("Lỗi: Dịch vụ " + service.getName() + " đã ngừng hoạt động!");
                return 0;
            }
            BigDecimal unitPrice = BigDecimal.valueOf(service.getPrice());

            pstmt.setInt(1, orderDetail.getService().getServiceId());
            pstmt.setInt(2, orderDetail.getQuantity());
            pstmt.setBigDecimal(3, unitPrice);
            pstmt.setInt(4, orderDetail.getOrderDetailId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // Cập nhật tổng tiền đơn hàng sau khi sửa
                OrderRepository.getInstance().updateTotalPrice(orderDetail.getOrder().getOrderId());
            }
            return affectedRows;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật chi tiết đơn hàng: " + e.getMessage());
            return 0;
        }
    }
    @Override
    public int delete(OrderDetail orderDetail) {
        String sql = "DELETE FROM order_detail WHERE order_detail_id=?";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderDetail.getOrderDetailId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // Cập nhật tổng tiền đơn hàng sau khi xóa
            	 OrderRepository.getInstance().updateTotalPrice(orderDetail.getOrder().getOrderId());

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
        String sql = "SELECT * FROM order_detail WHERE order_detail_id = ?";
        
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
        int orderDetailId = rs.getInt("order_detail_id");
        int orderId = rs.getInt("order_id");
        int serviceId = rs.getInt("service_id");
        int quantity = rs.getInt("quantity");
        //  Lấy costPrice từ Service
        Service service = ServiceRepository.getInstance().selectById(serviceId);
        if (service == null) {
            throw new SQLException("Không tìm thấy service với ID: " + serviceId);
        }
        BigDecimal unitPrice = BigDecimal.valueOf(service.getPrice());
        
        Order order = new Order();
        order.setOrderId(orderId);
        
        
        return new OrderDetail(orderDetailId, order, service, quantity, unitPrice);
    }
    /**
     * Obtiene la lista de servicios asociados a un pedido específico
     * @param orderId El ID del pedido
     * @return Lista de servicios del pedido
     */
    public List<Service> getServicesByOrderId(int orderId) {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT s.* FROM service s " +
                     "JOIN order_detail od ON s.service_id = od.service_id " +
                     "WHERE od.order_id = ? AND s.active = true";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Service service = new Service();
                    service.setServiceId(rs.getInt("service_id"));
                    service.setName(rs.getString("name"));
                    service.setDescription(rs.getString("description"));
                    service.setPrice(rs.getDouble("price"));
                    service.setDurationMinutes(rs.getInt("duration_minutes"));
                    service.setActive(rs.getBoolean("active"));
                    services.add(service);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách dịch vụ theo mã đơn hàng: " + e.getMessage());
        }

        return services;
    }
    /**
     * Calcula el precio total de los servicios de un pedido
     * @param orderId El ID del pedido
     * @return El precio total
     */
    public double calculateOrderTotal(int orderId) {
        double total = 0;
        String sql = "SELECT SUM(od.price * od.quantity) as total " +
                     "FROM order_detail od WHERE od.order_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tính tổng tiền đơn hàng: " + e.getMessage());
        }
        
        return total;
    }

    /**
     * Obtiene la cantidad de un servicio específico en un pedido
     * @param orderId El ID del pedido
     * @param serviceId El ID del servicio
     * @return La cantidad del servicio
     */
    public int getServiceQuantity(int orderId, int serviceId) {
        int quantity = 0;
        String sql = "SELECT quantity FROM order_detail " +
                     "WHERE order_id = ? AND service_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, serviceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    quantity = rs.getInt("quantity");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy số lượng dịch vụ: " + e.getMessage());
        }
        
        return quantity;
    }

    /**
     * Obtiene todos los detalles de un pedido
     * @param orderId El ID del pedido
     * @return Lista de detalles del pedido
     */
    public List<OrderDetail> getOrderDetailsByOrderId(int orderId) {
        List<OrderDetail> orderDetails = new ArrayList<>();
        String sql = "SELECT * FROM order_detail WHERE order_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orderDetails.add(mapResultSetToOrderDetail(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy chi tiết đơn hàng: " + e.getMessage());
        }
        
        return orderDetails;
    }
}
