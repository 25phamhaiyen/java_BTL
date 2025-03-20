package Enum;

public enum RoleType {
    ADMIN(1, "Admin"),
    STAFF(2, "Staff"),
    CUSTOMER(3, "Customer");

    private final int id;
    private final String name;

    RoleType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static RoleType fromId(int id) {
        for (RoleType role : values()) {
            if (role.getId() == id) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid RoleType ID: " + id);
    }

    @Override
    public String toString() {
        return name;
    }
}

