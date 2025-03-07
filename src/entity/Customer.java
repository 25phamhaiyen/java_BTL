package entity;

public class Customer {
	private int customer_ID;
	private String lastName;
	private String firstName;
	private String phoneNumber;
	private int sex;
	private String citizenNumber;
	private String address;
	
	public Customer() {
		super();
	}
	
	public Customer(int customer_ID, String lastName, String firstName, String phoneNumber, int sex,
			String citizenNumber, String address) {
		super();
		this.customer_ID = customer_ID;
		this.lastName = lastName;
		this.firstName = firstName;
		this.phoneNumber = phoneNumber;
		this.sex = sex;
		this.citizenNumber = citizenNumber;
		this.address = address;
	}

	public int getCustomer_ID() {
		return customer_ID;
	}

	public void setCustomer_ID(int customer_ID) {
		this.customer_ID = customer_ID;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public String getCitizenNumber() {
		return citizenNumber;
	}

	public void setCitizenNumber(String citizenNumber) {
		this.citizenNumber = citizenNumber;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "Customer: ID " + customer_ID + "\nName: " + lastName + " " + firstName
				+ "\nphoneNumber: " + phoneNumber + "\nsex: " + sex + "\ncitizenNumber=: " + citizenNumber + "\naddress: "
				+ address;
	}
	
	
	
	
}
