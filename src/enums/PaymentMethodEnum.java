package enums;

public enum PaymentMethodEnum {
    CASH(0), CARD(1), MOMO(2), BANKING(3), QR(4);

    private final int code;

    PaymentMethodEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static PaymentMethodEnum fromCode(int code) {
        for (PaymentMethodEnum method : PaymentMethodEnum.values()) {
            if (method.getCode() == code) {
                return method;
            }
        }
        throw new IllegalArgumentException("Invalid payment method code: " + code);
    }
    
    // Thêm phương thức để hiển thị tên thân thiện
    @Override
    public String toString() {
        switch (this) {
            case CASH: return "Tiền mặt";
            case CARD: return "Thẻ tín dụng";
            case MOMO: return "Ví MoMo";
            case BANKING: return "Chuyển khoản";
            case QR: return "QR Code";
            default: return name();
        }
    }
}