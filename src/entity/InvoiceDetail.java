package entity;

import java.math.BigDecimal;

public class InvoiceDetail {
    private int invoiceDetailId;
    private Invoice invoice; // Liên kết với hóa đơn
    private Service service; // Liên kết với dịch vụ
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice; // Không cần set thủ công, tính từ quantity * unitPrice

    // Constructor không tham số
    public InvoiceDetail() {}

    // Constructor đầy đủ
    public InvoiceDetail(int invoiceDetailId, Invoice invoice, Service service, int quantity, BigDecimal unitPrice) {
        this.invoiceDetailId = invoiceDetailId;
        this.invoice = invoice;
        this.service = service;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity)); // Tính tổng tiền
    }

    // Getter & Setter
    public int getInvoiceDetailId() {
        return invoiceDetailId;
    }

    public void setInvoiceDetailId(int invoiceDetailId) {
        this.invoiceDetailId = invoiceDetailId;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
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
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity)); // Cập nhật tổng tiền
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
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity)); // Cập nhật tổng tiền
        } else {
            throw new IllegalArgumentException("Unit price must be non-negative");
        }
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    @Override
    public String toString() {
        return "InvoiceDetail{" +
                "invoiceDetailId=" + invoiceDetailId +
                ", invoiceId=" + invoice.getInvoiceId() +
                ", serviceId=" + service.getServiceID() +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
