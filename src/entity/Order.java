package entity;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Order {
    private int orderId;
    private Timestamp orderDate;
    private Timestamp appointmentDate;
    private String orderType;
    private double total;
    
    private Customer customer;
    private Staff staff;
    private PaymentStatus paymentStatus;
    
    public Order() {}

    public Order(int orderId, Timestamp orderDate, Timestamp appointmentDate, String orderType, double total, 
                 Customer customer, Staff staff, PaymentStatus paymentStatus) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.appointmentDate = appointmentDate;
        this.orderType = orderType;
        this.total = total;
        this.customer = customer;
        this.staff = staff;
        this.paymentStatus = paymentStatus;
    }

    // Getter & Setter
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Timestamp orderDate) {
        this.orderDate = orderDate;
    }

    public Timestamp getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Timestamp appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    // Format ngày tháng
    public String getFormattedOrderDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(orderDate.getTime()));
    }

    public String getFormattedAppointmentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(appointmentDate.getTime()));
    }
    
    
}
