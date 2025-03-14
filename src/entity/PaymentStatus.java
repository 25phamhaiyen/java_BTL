package entity;

public class PaymentStatus {
	private int paymentStatusID;
	private String statusName;
	private int statusCode;
	public PaymentStatus() {
		super();
	}
	public PaymentStatus(int paymentStatusID, String statusName, int statusCode) {
		super();
		this.paymentStatusID = paymentStatusID;
		this.statusName = statusName;
		this.statusCode = statusCode;
	}
	public int getPaymentStatusID() {
		return paymentStatusID;
	}
	public void setPaymentStatusID(int paymentStatusID) {
		this.paymentStatusID = paymentStatusID;
	}
	public String getStatusName() {
		return statusName;
	}
	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	@Override
	public String toString() {
		return "PaymentStatus: ID:" + paymentStatusID + "\n\t\t\t  Name: " + statusName + "\n\t\t\t  Code: "
				+ statusCode;
	}
	
	
	
	
}
