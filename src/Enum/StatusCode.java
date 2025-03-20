package Enum;

public enum StatusCode {
    PENDING(0), PROCESSING(1), COMPLETED(2);

    private final int code;

    StatusCode(int code) { this.code = code; }

    public int getCode() { return code; }

    public static StatusCode fromCode(int code) {
        for (StatusCode status : StatusCode.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status code: " + code);
    }
}
