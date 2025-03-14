package entity;

import java.util.Date;

public class Order {
	private int orderID;
	private Date oderDate;
	private double total;
	private int customerID;
	private int staffID ;
	
	public Order() {
		super();
	}

	public Order(int orderID, Date oderDate, double total, int customerID, int staffID) {
		super();
		this.orderID = orderID;
		this.oderDate = oderDate;
		this.total = total;
		this.customerID = customerID;
		this.staffID = staffID;
	}

	public int getOrderID() {
		return orderID;
	}

	public void setOrderID(int orderID) {
		this.orderID = orderID;
	}

	public Date getOderDate() {
		return oderDate;
	}

	public void setOderDate(Date oderDate) {
		this.oderDate = oderDate;
	}

	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}

	public int getCustomerID() {
		return customerID;
	}

	public void setCustomerID(int customerID) {
		this.customerID = customerID;
	}

	public int getStaffID() {
		return staffID;
	}

	public void setStaffID(int staffID) {
		this.staffID = staffID;
	}

	@Override
	public String toString() {
		return "Order: ID: " + orderID + "\n\tDate: " + oderDate + "\n\tTotal: " + total + "\n\tCustomer ID: "
				+ customerID + "S\n\tStaff ID=" + staffID;
	}
	
	
	
}
