package model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import enums.TypeOrder;

public class Order {
    private int orderId;
    private Timestamp orderDate;
    private Timestamp appointmentDate;
    private TypeOrder orderType;
    private double total;
    
    private Customer customer;
    private Staff staff;
    private HappenStatus happenStatus;
    
    public Order() {}

    public Order(int orderId, Timestamp orderDate, Timestamp appointmentDate, TypeOrder orderType, double total, 
                 Customer customer, Staff staff, HappenStatus happenStatus) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.appointmentDate = appointmentDate;
        this.orderType = orderType;
        this.total = total;
        this.customer = customer;
        this.staff = staff;
        this.happenStatus = happenStatus;
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

    public TypeOrder getOrderType() {
        return orderType;
    }

    public void setOrderType(TypeOrder orderType) {
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

    public HappenStatus getHappentStatus() {
        return happenStatus;
    }

    public void setHappenStatus(HappenStatus happenStatus) {
        this.happenStatus = happenStatus;
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
    @Override
    public String toString() {
        return "Order{" +
               "orderId=" + orderId +
               ", orderDate=" + orderDate +
               ", appointmentDate=" + appointmentDate +
               ", orderType=" + orderType +
               ", total=" + total +
               ", customer=" + (customer != null ? customer.getCustomerID() : "null") +
               ", staff=" + (staff != null ? staff.getStaffID() : "null") +
               ", status=" + (happenStatus != null ? happenStatus.getHappenStatusID() : "null") +
               '}';
    }

    
}
