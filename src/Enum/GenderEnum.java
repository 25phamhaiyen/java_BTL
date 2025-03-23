package Enum;

public enum GenderEnum {
    MALE(1, "Nam"),
    FEMALE(0, "Nữ"),
    UNKNOWN(2, "Không xác định");

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
}
