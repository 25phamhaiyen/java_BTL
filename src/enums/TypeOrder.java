package enums;

public enum TypeOrder {
	AT_STORE("AtStore"),
    APPOINTMENT("Appointment");
	private final String description;

    TypeOrder(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
