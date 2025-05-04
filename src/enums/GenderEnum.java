package enums;

public enum GenderEnum {
    MALE(0, "MALE"),
    FEMALE(1, "FEMALE"),
    OTHER(2, "OTHER"),
    UNKNOWN(3, "Không xác định");

    private final int code;
    private final String description;

    GenderEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static GenderEnum fromCode(int code) {
        for (GenderEnum gender : GenderEnum.values()) {
            if (gender.code == code) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Invalid gender code: " + code);
    }
    
    @Override
    public String toString() {
        return switch (this) {
            case MALE -> "Nam";
            case FEMALE -> "Nữ";
            case OTHER -> "Khác";
            default -> "Không xác định";
        };
    }
}
