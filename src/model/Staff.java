package model;

import java.time.LocalDate;


import enums.GenderEnum;

public class Staff extends Person {
	
	private Account account;
    private Role role;
    private LocalDate startDate;
    private LocalDate endDate;
    private double salary;
    private String workShift;
    private String position;

    public Staff() {
        super();
    }


	public Account getAccount() {
		return account;
	}


	public void setAccount(Account account) {
		this.account = account;
	}



	public Staff(int id, String lastName, String firstName, GenderEnum gender, String phoneNumber,
			String citizenNumber, String address, String email,Account account, Role role, LocalDate startDate, LocalDate endDate, double salary, String workShift,
			String position) {
		super(id, lastName, firstName, gender, phoneNumber, citizenNumber, address, email);
		this.account = account;
		this.role = role;
		this.startDate = startDate;
		this.endDate = endDate;
		this.salary = salary;
		this.workShift = workShift;
		this.position = position;
	}


	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public double getSalary() {
		return salary;
	}

	public void setSalary(double salary) {
		this.salary = salary;
	}

	public String getWorkShift() {
		return workShift;
	}

	public void setWorkShift(String workShift) {
		this.workShift = workShift;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}



	@Override
	public String toString() {
		return "Staff [account=" + account + ", role=" + role + ", startDate=" + startDate + ", endDate=" + endDate
				+ ", salary=" + salary + ", workShift=" + workShift + ", position=" + position + "]";
	}


}