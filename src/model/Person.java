package model;

import enums.GenderEnum;

public class Person {
    protected int id;
    protected String lastName;
    protected String firstName;
    protected GenderEnum gender;
    protected String phoneNumber;
    protected String citizenNumber;
    protected String address;
    protected String email; 
    

	public Person() {}

    

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Person(int id, String lastName, String firstName, GenderEnum gender, String phoneNumber,
			String citizenNumber, String address, String email) {
		super();
		this.id = id;
		this.lastName = lastName;
		this.firstName = firstName;
		this.gender = gender;
		this.phoneNumber = phoneNumber;
		this.citizenNumber = citizenNumber;
		this.address = address;
		this.email = email;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public GenderEnum getGender() {
		return gender;
	}

	public void setGender(GenderEnum gender) {
		this.gender = gender;
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

	@Override
	public String toString() {
		return "Person [id=" + id + ", lastName=" + lastName + ", firstName=" + firstName + ", gender=" + gender
				+ ", phoneNumber=" + phoneNumber + ", citizenNumber=" + citizenNumber + ", address=" + address
				+  "]";
	}

    
}