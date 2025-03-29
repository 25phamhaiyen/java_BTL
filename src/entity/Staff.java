package entity;

import Enum.GenderEnum;

public class Staff {
    private int staffID;
    private String lastName;
    private String firstName;
    private GenderEnum sex;
    private String phoneNumber;
    private String citizenNumber;
    private String address;
    private Role role;
    private int accountID; 

    public Staff() {
        super();
    }

    public Staff(int staffID, String lastName, String firstName, GenderEnum sex, String phoneNumber, 
                 String citizenNumber, String address, Role role, int accountID) {
        super();
        this.staffID = staffID;
        this.lastName = lastName;
        this.firstName = firstName;
        this.sex = sex;
        this.phoneNumber = phoneNumber;
        this.citizenNumber = citizenNumber;
        this.address = address;
        this.role = role;
        this.accountID = accountID; 
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

    public GenderEnum getSex() {
        return sex;
    }

    public void setSex(GenderEnum sex) {
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public int getAccountID() {  // Getter cho AccountID**
        return accountID;
    }

    public void setAccountID(int accountID) {  // Setter cho AccountID**
        this.accountID = accountID;
    }


    public void validate() throws IllegalArgumentException {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ không được để trống");
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên không được để trống");
        }
        if (phoneNumber == null || !phoneNumber.matches("^[0-9]{10}$")) {
            throw new IllegalArgumentException("Số điện thoại phải có đúng 10 chữ số");
        }
        if (citizenNumber == null || !citizenNumber.matches("^[0-9]{12}$")) {
            throw new IllegalArgumentException("Số CCCD phải có đúng 12 chữ số");
        }
    }
    
    @Override
    public String toString() {
        return "Staff: ID: " + staffID +
               "\n\t  Name: " + lastName + " " + firstName +
               "\n\t  Sex: " + sex.getDescription() +
               "\n\t  Phone Number: " + phoneNumber +
               "\n\t  Citizen Number: " + citizenNumber +
               "\n\t  Address: " + address +
               "\n\t  Role: " + role.getRoleName() +
               "\n\t  AccountID: " + accountID;  // Thêm AccountID vào toString**
    }
}