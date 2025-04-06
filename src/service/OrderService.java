package service;

import model.Order;
import repository.OrderRepository;
import java.util.List;

public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService() {
        this.orderRepository = OrderRepository.getInstance();
    }

    // Lấy tất cả các đơn hàng
    public List<Order> getAllOrders() {
        return orderRepository.selectAll();
    }

    // Lấy đơn hàng theo ID
    public Order getOrderById(int orderId) {
        return orderRepository.selectById(orderId);
    }

    // Lấy đơn hàng theo điều kiện
    public List<Order> getOrdersByCondition(String condition, Object... params) {
        return orderRepository.selectByCondition(condition, params);
    }

    // Thêm một đơn hàng mới
    public void addOrder(Order order) {
        int rowsAffected = orderRepository.insert(order);
        if (rowsAffected > 0) {
            System.out.println("Đơn hàng đã được thêm thành công với ID: " + order.getOrderId());
        }
    }

    // Cập nhật thông tin đơn hàng
    public void updateOrder(Order order) {
        int rowsAffected = orderRepository.update(order);
        if (rowsAffected > 0) {
            System.out.println("Đơn hàng đã được cập nhật thành công với ID: " + order.getOrderId());
        }
    }

    // Xóa đơn hàng
    public void deleteOrder(Order order) {
        int rowsAffected = orderRepository.delete(order);
        if (rowsAffected > 0) {
            System.out.println("Đơn hàng đã được xóa thành công với ID: " + order.getOrderId());
        }
    }

    // Cập nhật tổng tiền của đơn hàng sau khi thay đổi chi tiết đơn hàng
    public void updateOrderTotal(int orderId) {
        orderRepository.updateTotalPrice(orderId);
    }
}
