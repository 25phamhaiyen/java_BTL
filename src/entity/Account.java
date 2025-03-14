package entity;

public class Account {
	private int accountID;
	private String userName;
	private String password;
	private String email;
	private int staffID;
	public Account() {
		super();
	}
	public Account(int accountID, String userName, String password, String email, int staffID) {
		super();
		this.accountID = accountID;
		this.userName = userName;
		this.password = password;
		this.email = email;
		this.staffID = staffID;
	}
	public int getAccountID() {
		return accountID;
	}
	public void setAccountID(int accountID) {
		this.accountID = accountID;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public int getStaffID() {
		return staffID;
	}
	public void setStaffID(int staffID) {
		this.staffID = staffID;
	}
	@Override
	public String toString() {
		return "Account: ID: " + accountID + "\n\t\tUsername: " + userName + "\\n\\t\\tPassword: " + password + "\\n\\t\\tEmail: "
				+ email + "\\n\\t\\tStaff ID: " + staffID;
	}
	
}
