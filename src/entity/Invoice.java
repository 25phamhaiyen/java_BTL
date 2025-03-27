package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Invoice {
    private int invoiceId;
    private Order order; // Liên kết với Order
    private BigDecimal totalAmount;
    private Timestamp createdAt;

    // Constructor không tham số
    public Invoice() {}

    // Constructor đầy đủ
    public Invoice(int invoiceId, Order order, BigDecimal totalAmount, Timestamp createdAt) {
        this.invoiceId = invoiceId;
        this.order = order;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    // Getter & Setter
    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        if (totalAmount.compareTo(BigDecimal.ZERO) >= 0) {
            this.totalAmount = totalAmount;
        } else {
            throw new IllegalArgumentException("Total amount must be non-negative");
        }
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "invoiceId=" + invoiceId +
                ", orderId=" + order.getOrderId() +
                ", totalAmount=" + totalAmount +
                ", createdAt=" + createdAt +
                '}';
    }
}
