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

    public Staff() {
        super();
    }

    public Staff(int staffID, String lastName, String firstName, GenderEnum sex, String phoneNumber, String citizenNumber,
                 String address, Role role) {
        super();
        this.staffID = staffID;
        this.lastName = lastName;
        this.firstName = firstName;
        this.sex = sex;
        this.phoneNumber = phoneNumber;
        this.citizenNumber = citizenNumber;
        this.address = address;
        this.role = role;
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

    @Override
    public String toString() {
        return "Staff: ID: " + staffID +
               "\n\t  Name: " + lastName + " " + firstName +
               "\n\t  Sex: " + sex.getDescription() +
               "\n\t  Phone Number: " + phoneNumber +
               "\n\t  Citizen Number: " + citizenNumber +
               "\n\t  Address: " + address +
               "\n\t  Role: " + role.getRoleName();
    }
}
