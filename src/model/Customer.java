package model;

import enums.GenderEnum;

public class Customer {
    private int customerID;
    private String lastName;
    private String firstName;
    private String phoneNumber;
    private GenderEnum gender; // Dùng enum thay vì int
    private String citizenNumber;
    private String address;
    private Account account; // Khóa ngoại

    public Customer() {
        super();
    }

    public Customer(int customerID, String lastName, String firstName, String phoneNumber, GenderEnum gender,
                    String citizenNumber, String address, Account account) {
        super();
        this.customerID = customerID;
        this.lastName = lastName;
        this.firstName = firstName;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.citizenNumber = citizenNumber;
        this.address = address;
        this.account = account;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
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

    public GenderEnum getGender() {
        return gender;
    }

    public void setGender(GenderEnum gender) {
        this.gender = gender;
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

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "Customer: ID " + customerID + "\n\tName: " + lastName + " " + firstName
                + "\n\tPhone Number: " + phoneNumber + "\n\tGender: " + gender.getDescription()
                + "\n\tCitizen Number: " + citizenNumber + "\n\tAddress: " + address
                + "\n\tAccount ID: " + (account != null ? account.getAccountID() : "None");
    }
}
