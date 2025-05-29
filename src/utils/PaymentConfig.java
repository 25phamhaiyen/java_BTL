package utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class PaymentConfig {
    // Cấu hình PayOS - Cần thay bằng thông tin thực từ PayOS
    private static String payosClientId = "cd779b5a-a3c9-41c2-946f-728303006b95";
    private static String payosApiKey = "6bc1d560-02cb-41d3-8a0d-f437fda5906d";
    private static String payosChecksumKey = "fb28ae4d1a0baed33621b30006bd668bf1b88a51e61a22a1afe0f26799c54edb";
    private static String payosApiUrl = "https://api-merchant.payos.vn";
    private static final String STATUS_API_URL = "https://api.payos.vn/v2/payment-requests/";
    private static String returnUrlSuccess = "";
    private static String returnUrlCancel = "";
    private static String orderPrefix = "PCC";
    private static long orderMinAmount = 1000;
    private static long orderMaxAmount = 50000000;
    private static String accountNumber = "6910889701";
    private static boolean debugMode = true;

    // Thêm các hằng số bổ sung
    private static final int API_TIMEOUT_MS = 30000; // 30 giây
    private static final int RETRY_COUNT = 3; // Số lần thử lại nếu API thất bại
    private static final long STATUS_CHECK_INTERVAL_MS = 5000; // 5 giây
    
    // Trạng thái thanh toán PayOS
    public static final String PAYMENT_STATUS_PAID = "PAID";
    public static final String PAYMENT_STATUS_PENDING = "PENDING";
    public static final String PAYMENT_STATUS_CANCELLED = "CANCELLED";
    public static final String PAYMENT_STATUS_EXPIRED = "EXPIRED";

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        try {
            String envClientId = System.getenv("PAYOS_CLIENT_ID");
            if (envClientId != null && !envClientId.isEmpty()) {
                payosClientId = envClientId;
                PaymentLogger.info("Đã tải clientId từ biến môi trường: " + payosClientId);
            } else {
                PaymentLogger.warning("Không tìm thấy biến môi trường PAYOS_CLIENT_ID, sử dụng giá trị mặc định");
            }

            String envApiKey = System.getenv("PAYOS_API_KEY");
            if (envApiKey != null && !envApiKey.isEmpty()) {
                payosApiKey = envApiKey;
                PaymentLogger.info("Đã tải apiKey từ biến môi trường: [HIDDEN]");
            } else {
                PaymentLogger.warning("Không tìm thấy biến môi trường PAYOS_API_KEY, sử dụng giá trị mặc định");
            }

            String envChecksumKey = System.getenv("PAYOS_CHECKSUM_KEY");
            if (envChecksumKey != null && !envChecksumKey.isEmpty()) {
                payosChecksumKey = envChecksumKey;
                PaymentLogger.info("Đã tải checksumKey từ biến môi trường: [HIDDEN]");
            } else {
                PaymentLogger.warning("Không tìm thấy biến môi trường PAYOS_CHECKSUM_KEY, sử dụng giá trị mặc định");
            }

            String envDebugMode = System.getenv("PAYOS_DEBUG_MODE");
            if (envDebugMode != null && !envDebugMode.isEmpty()) {
                debugMode = Boolean.parseBoolean(envDebugMode);
                PaymentLogger.info("Đã tải debugMode từ biến môi trường: " + debugMode);
            }

            validateConfiguration();

            PaymentLogger.info("Cấu hình PayOS đã được tải: " +
                "clientId=" + payosClientId + ", apiKey=[HIDDEN], checksumKey=[HIDDEN], " +
                "apiUrl=" + payosApiUrl + ", debugMode=" + debugMode);
        } catch (Exception e) {
            PaymentLogger.error("Lỗi tải cấu hình PayOS: " + e.getMessage(), e);
        }
    }

    public static String getPayOSClientId() { return payosClientId; }
    public static String getPayOSApiKey() { return payosApiKey; }
    public static String getPayOSChecksumKey() { return payosChecksumKey; }
    public static String getPayOSApiUrl() { return payosApiUrl; }
    public static String getStatusApiUrl() { return STATUS_API_URL; }
    public static String getReturnUrlSuccess() { return returnUrlSuccess; }
    public static String getReturnUrlCancel() { return returnUrlCancel; }
    public static String getOrderPrefix() { return orderPrefix; }
    public static long getOrderMinAmount() { return orderMinAmount; }
    public static long getOrderMaxAmount() { return orderMaxAmount; }
    public static String getAccountNumber() { return accountNumber; }
    public static boolean isDebugMode() { return debugMode; }
    public static int getApiTimeoutMs() { return API_TIMEOUT_MS; }
    public static int getRetryCount() { return RETRY_COUNT; }
    public static long getStatusCheckIntervalMs() { return STATUS_CHECK_INTERVAL_MS; }

    public static boolean isValidAmount(long amount) {
        boolean valid = amount >= orderMinAmount && amount <= orderMaxAmount;
        if (!valid) {
            PaymentLogger.warning("Số tiền không hợp lệ: " + amount + 
                                 ", Giới hạn: " + orderMinAmount + " - " + orderMaxAmount + " VND");
        }
        return valid;
    }

    public static String generateUniqueOrderCode(int orderId) {
        try {
            long timestamp = System.currentTimeMillis();
            String orderCode = orderPrefix + String.format("%06d", orderId) + "-" + (timestamp % 10000);
            PaymentLogger.info("Tạo orderCode: " + orderCode);
            return orderCode;
        } catch (Exception e) {
            PaymentLogger.error("Lỗi tạo orderCode: " + e.getMessage(), e);
            throw new RuntimeException("Lỗi tạo orderCode", e);
        }
    }

    public static String generateReturnUrl(int orderId, long amount, String description) {
        try {
            return returnUrlSuccess + "?orderId=" + orderId +
                   "&amount=" + amount +
                   "&accountNumber=" + URLEncoder.encode(accountNumber, StandardCharsets.UTF_8) +
                   "&description=" + URLEncoder.encode(description, StandardCharsets.UTF_8);
        } catch (Exception e) {
            PaymentLogger.error("Lỗi tạo returnUrl: " + e.getMessage(), e);
            throw new RuntimeException("Lỗi tạo returnUrl", e);
        }
    }

    public static String generateCancelUrl(int orderId, long amount, String description) {
        try {
            return returnUrlCancel + "?orderId=" + orderId +
                   "&amount=" + amount +
                   "&accountNumber=" + URLEncoder.encode(accountNumber, StandardCharsets.UTF_8) +
                   "&description=" + URLEncoder.encode(description, StandardCharsets.UTF_8);
        } catch (Exception e) {
            PaymentLogger.error("Lỗi tạo cancelUrl: " + e.getMessage(), e);
            throw new RuntimeException("Lỗi tạo cancelUrl", e);
        }
    }

    private static void validateConfiguration() {
        if (payosClientId == null || payosClientId.trim().isEmpty()) {
            PaymentLogger.error("Cấu hình PayOS không hợp lệ: clientId trống");
            throw new IllegalStateException("Cấu hình PayOS không hợp lệ: clientId trống");
        }
        if (payosApiKey == null || payosApiKey.trim().isEmpty()) {
            PaymentLogger.error("Cấu hình PayOS không hợp lệ: apiKey trống");
            throw new IllegalStateException("Cấu hình PayOS không hợp lệ: apiKey trống");
        }
        if (payosChecksumKey == null || payosChecksumKey.trim().isEmpty()) {
            PaymentLogger.error("Cấu hình PayOS không hợp lệ: checksumKey trống");
            throw new IllegalStateException("Cấu hình PayOS không hợp lệ: checksumKey trống");
        }
        if (payosApiUrl == null || payosApiUrl.trim().isEmpty()) {
            PaymentLogger.error("Cấu hình PayOS không hợp lệ: apiUrl trống");
            throw new IllegalStateException("Cấu hình PayOS không hợp lệ: apiUrl trống");
        }
        if (returnUrlSuccess == null || returnUrlSuccess.trim().isEmpty()) {
            PaymentLogger.error("Cấu hình PayOS không hợp lệ: returnUrlSuccess trống");
            throw new IllegalStateException("Cấu hình PayOS không hợp lệ: returnUrlSuccess trống");
        }
        if (returnUrlCancel == null || returnUrlCancel.trim().isEmpty()) {
            PaymentLogger.error("Cấu hình PayOS không hợp lệ: returnUrlCancel trống");
            throw new IllegalStateException("Cấu hình PayOS không hợp lệ: returnUrlCancel trống");
        }
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            PaymentLogger.error("Cấu hình PayOS không hợp lệ: accountNumber trống");
            throw new IllegalStateException("Cấu hình PayOS không hợp lệ: accountNumber trống");
        }
        PaymentLogger.info("Cấu hình PayOS hợp lệ");
    }
}