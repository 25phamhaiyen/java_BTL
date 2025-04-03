package model;

import java.util.Date;

import enums.GenderEnum;

public class Customer extends Person {

	private Account account;
	private Date registrationDate;
	private int loyaltyPoints;

	public Customer() {
		super();
	}

	public Customer(int id, String lastName, String firstName, GenderEnum gender, String phoneNumber,
			String citizenNumber, String address, String email, Account account, Date registrationDate,
			int loyaltyPoints) {
		super(id, lastName, firstName, gender, phoneNumber, citizenNumber, address, email);
		this.account = account;
		this.registrationDate = registrationDate;
		this.loyaltyPoints = loyaltyPoints;
	}

	public Customer(Account account, Date registrationDate, int loyaltyPoints) {
		super();
		this.account = account;
		this.registrationDate = registrationDate;
		this.loyaltyPoints = loyaltyPoints;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public int getLoyaltyPoints() {
		return loyaltyPoints;
	}

	public void setLoyaltyPoints(int loyaltyPoints) {
		this.loyaltyPoints = loyaltyPoints;
	}

	@Override
	public String toString() {
		return "Customer [account=" + account + ", registrationDate=" + registrationDate + ", loyaltyPoints="
				+ loyaltyPoints + "]";
	}

}