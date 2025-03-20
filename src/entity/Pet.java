package entity;

public class Pet {
    private int petID;
    private String petName;
    private int age;
    private Customer customer;
    private TypePet typePet; // Thay vì chỉ lưu ID

    public Pet() {}

    public Pet(int petID, String petName, int age, Customer customer, TypePet typePet) {
        this.petID = petID;
        this.petName = petName;
        this.age = age;
        this.customer = customer;
        this.typePet = typePet;
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

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public TypePet getTypePet() {
        return typePet;
    }

    public void setTypePet(TypePet typePet) {
        this.typePet = typePet;
    }

    @Override
    public String toString() {
        return "Pet: ID: " + petID + 
               "\n\tName: " + petName + 
               "\n\tAge: " + age + 
               "\n\tCustomer: " + (customer != null ? customer.getLastName() + " " + customer.getFirstName() : "None") +
               "\n\tType: " + (typePet != null ? typePet.getTypeName() : "Unknown");
    }
}
