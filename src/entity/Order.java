package entity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Order {
    private int orderID;
    private Date orderDate;
    private double total;
    private Customer customer;
    private Staff staff;

    public Order() {}

    public Order(int orderID, Date orderDate, double total, Customer customer, Staff staff) {
        this.orderID = orderID;
        this.orderDate = orderDate;
        this.total = total;
        this.customer = customer;
        this.staff = staff;
    }

    public int getOrderID() { return orderID; }
    public void setOrderID(int orderID) { this.orderID = orderID; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Staff getStaff() { return staff; }
    public void setStaff(Staff staff) { this.staff = staff; }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return "Order: ID: " + orderID +
               "\n\tDate: " + (orderDate != null ? sdf.format(orderDate) : "N/A") +
               "\n\tTotal: $" + total +
               "\n\tCustomer: " + (customer != null ? customer.getCustomerID() : "N/A") +
               "\n\tStaff: " + (staff != null ? staff.getStaffID() : "N/A");
    }
}
