package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

import enums.PaymentMethodEnum;
import enums.StatusEnum;

public class Invoice {
    private int invoiceId;
    private Order order;
    private Timestamp paymentDate;
    private BigDecimal subtotal;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private Integer pointsUsed;
    private String promotionCode;
    private BigDecimal total;
    private BigDecimal amountPaid;
    private PaymentMethodEnum paymentMethod;
    private StatusEnum status;
    private Staff staff;
    private String note;

    // Default constructor
    public Invoice() {}

    // Constructor for invoiceId
    public Invoice(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    // Constructor used in InvoiceRepository
    public Invoice(int invoiceId, Order order, Timestamp paymentDate, BigDecimal total,
                   PaymentMethodEnum paymentMethod, StatusEnum status, Staff staff) {
        this.invoiceId = invoiceId;
        this.order = order;
        this.paymentDate = paymentDate;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.staff = staff;
    }

    // Full constructor
    public Invoice(int invoiceId, Order order, Timestamp paymentDate, BigDecimal subtotal,
                   BigDecimal discountPercent, BigDecimal discountAmount, Integer pointsUsed,
                   String promotionCode, BigDecimal total, BigDecimal amountPaid,
                   PaymentMethodEnum paymentMethod, StatusEnum status, Staff staff, String note) {
        this.invoiceId = invoiceId;
        this.order = order;
        this.paymentDate = paymentDate;
        this.subtotal = subtotal;
        this.discountPercent = discountPercent;
        this.discountAmount = discountAmount;
        this.pointsUsed = pointsUsed;
        this.promotionCode = promotionCode;
        this.total = total;
        this.amountPaid = amountPaid;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.staff = staff;
        this.note = note;
    }

    // Getters and Setters
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

    public Timestamp getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Timestamp paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Integer getPointsUsed() {
        return pointsUsed;
    }

    public void setPointsUsed(Integer pointsUsed) {
        this.pointsUsed = pointsUsed;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public void setPromotionCode(String promotionCode) {
        this.promotionCode = promotionCode;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public PaymentMethodEnum getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethodEnum paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "Invoice [invoiceId=" + invoiceId + ", order=" + order + ", paymentDate=" + paymentDate +
               ", subtotal=" + subtotal + ", discountPercent=" + discountPercent + ", discountAmount=" + discountAmount +
               ", pointsUsed=" + pointsUsed + ", promotionCode=" + promotionCode + ", total=" + total +
               ", amountPaid=" + amountPaid + ", paymentMethod=" + paymentMethod + ", status=" + status +
               ", staff=" + staff + ", note=" + note + "]";
    }
}