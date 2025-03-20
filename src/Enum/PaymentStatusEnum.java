package Enum;

public enum PaymentStatusEnum {
    PENDING(0, "Pending"),
    PAID(1, "Paid"),
    FAILED(2, "Failed");

    private final int code;
    private final String name;

    PaymentStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static PaymentStatusEnum fromCode(int code) {
        for (PaymentStatusEnum status : PaymentStatusEnum.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid Payment Status Code: " + code);
    }
}
