package entity;

public class Pet {
	private int petID;
	private String petName;
	private int age;
	private int customerID;
	private int typePetID;
	public Pet() {
		super();
	}
	public Pet(int petID, String petName, int age, int customerID, int typePetID) {
		super();
		this.petID = petID;
		this.petName = petName;
		this.age = age;
		this.customerID = customerID;
		this.typePetID = typePetID;
	}
	public int getPetID() {
		return petID;
	}
	public void setPetID(int petID) {
		this.petID = petID;
	}
	public String getPetName() {
		return petName;
	}
	public void setPetName(String petName) {
		this.petName = petName;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public int getCustomerID() {
		return customerID;
	}
	public void setCustomerID(int customerID) {
		this.customerID = customerID;
	}
	public int getTypePetID() {
		return typePetID;
	}
	public void setTypePetID(int typePetID) {
		this.typePetID = typePetID;
	}
	@Override
	public String toString() {
		return "Pet: ID: " + petID + "\n\tName: " + petName + "\n\tAge: " + age + "\n\tCustomer ID: " + customerID
				+ "\n\tType Pet ID: " + typePetID;
	}
	
	
}
