package exception;

/**
 * Ngoại lệ đặc biệt cho các vấn đề liên quan đến thanh toán
 */
public class PaymentException extends RuntimeException {
    private final PaymentErrorCode errorCode;
    
    public PaymentException(String message) {
        super(message);
        this.errorCode = PaymentErrorCode.GENERAL_ERROR;
    }
    
    public PaymentException(String message, PaymentErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public PaymentException(String message, Throwable cause, PaymentErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public PaymentErrorCode getErrorCode() {
        return errorCode;
    }
    
    /**
     * Mã lỗi thanh toán để xác định chính xác vấn đề
     */
    public enum PaymentErrorCode {
        GENERAL_ERROR,
        CONNECTION_ERROR,
        INVALID_INVOICE,
        INVALID_AMOUNT,
        TIMEOUT_ERROR,
        API_ERROR,
        TRANSACTION_NOT_FOUND,
        TRANSACTION_ALREADY_COMPLETED,
        TRANSACTION_ALREADY_FAILED
    }
}