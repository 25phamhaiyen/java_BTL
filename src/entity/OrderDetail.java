package entity;

import java.math.BigDecimal;

public class OrderDetail {
    private int orderDetailId;
    private Order order;   // Liên kết với Order
    private Service service; // Liên kết với Service
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice; // Tính tổng tiền
    
    // Constructor không tham số
    public OrderDetail() {}

    // Constructor đầy đủ
    public OrderDetail(int orderDetailId, Order order, Service service, int quantity, BigDecimal unitPrice) {
        this.orderDetailId = orderDetailId;
        this.order = order;
        this.service = service;
        this.quantity = quantity;
        if (unitPrice == null) {
            throw new IllegalArgumentException("unitPrice không thể là null");
        }
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity)); // Tính tổng tiền
    }

    // Getter & Setter
    public int getOrderDetailId() {
        return orderDetailId;
    }

    public void setOrderDetailId(int orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity > 0) {
            this.quantity = quantity;
            this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(quantity)); // Cập nhật tổng tiền
        } else {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        if (unitPrice.compareTo(BigDecimal.ZERO) >= 0) {
            this.unitPrice = unitPrice;
            this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity)); // Cập nhật tổng tiền
        } else {
            throw new IllegalArgumentException("Unit price must be non-negative");
        }
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    @Override
    public String toString() {
        return "OrderDetail{" +
                "orderDetailId=" + orderDetailId +
                ", order=" + order.getOrderId() +
                ", service=" + service.getServiceID() +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
