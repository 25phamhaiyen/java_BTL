package entity;

public class Staff {
	private int staffID;
	private String lastName;
	private String firstName;
	private int sex;
	private String phoneNumber;
	private String citizenNumber;
	private String address;
	private int roleID;
	public Staff() {
		super();
	}
	public Staff(int staffID, String lastName, String firstName, int sex, String phoneNumber, String citizenNumber,
			String address, int roleID) {
		super();
		this.staffID = staffID;
		this.lastName = lastName;
		this.firstName = firstName;
		this.sex = sex;
		this.phoneNumber = phoneNumber;
		this.citizenNumber = citizenNumber;
		this.address = address;
		this.roleID = roleID;
	}
	public int getStaffID() {
		return staffID;
	}
	public void setStaffID(int staffID) {
		this.staffID = staffID;
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
	public int getSex() {
		return sex;
	}
	public void setSex(int sex) {
		this.sex = sex;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
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
	public int getRoleID() {
		return roleID;
	}
	public void setRoleID(int roleID) {
		this.roleID = roleID;
	}
	@Override
	public String toString() {
		return "Staff: ID: " + staffID + "\n\t  Name: " + lastName + " " + firstName + "\n\t  Sex: " + sex
				+ "\n\t  Phone Number: " + phoneNumber + "\n\t  Citizen Number: " + citizenNumber + "\n\t  Address: " + address
				+ "\n\t  Role ID=" + roleID;
	}
	
	
}
