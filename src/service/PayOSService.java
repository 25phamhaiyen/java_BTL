package service;

import model.Invoice;
import model.Customer;
import model.Order;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import utils.PaymentConfig;
import utils.PaymentLogger;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PayOSService {
    private static PayOSService instance;
    private final JSONParser jsonParser;

    private PayOSService() {
        this.jsonParser = new JSONParser();
        PaymentLogger.info("PayOSService initialized");
        validateConfiguration();
    }

    public static synchronized PayOSService getInstance() {
        if (instance == null) {
            instance = new PayOSService();
        }
        return instance;
    }

    public PayOSPaymentResult createPaymentLink(Invoice invoice) {
        try {
            PaymentLogger.info("Tạo liên kết thanh toán PayOS cho hóa đơn #" + invoice.getInvoiceId());
            
            if (!validateInvoice(invoice)) {
                PaymentLogger.error("Hóa đơn không hợp lệ để tạo liên kết thanh toán");
                return PayOSPaymentResult.error("Hóa đơn không hợp lệ");
            }

            Map<String, Object> paymentData = buildPaymentRequest(invoice);
            PaymentLogger.info("Dữ liệu yêu cầu thanh toán: " + new JSONObject(paymentData).toJSONString());

            Map<String, Object> response = createPaymentLinkAPI(paymentData);
            PaymentLogger.info("Phản hồi từ API PayOS: " + response);

            if (response == null || response.isEmpty()) {
                PaymentLogger.error("Phản hồi từ API PayOS là null hoặc rỗng");
                return PayOSPaymentResult.error("Phản hồi từ API PayOS không hợp lệ");
            }

            if ("00".equals(response.get("code"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                if (data == null) {
                    PaymentLogger.error("Dữ liệu phản hồi từ API PayOS không chứa trường 'data'");
                    return PayOSPaymentResult.error("Dữ liệu phản hồi không hợp lệ: thiếu trường 'data'");
                }

                String paymentUrl = (String) data.get("checkoutUrl");
                String orderCode = String.valueOf(data.get("orderCode"));

                if (paymentUrl == null || paymentUrl.trim().isEmpty()) {
                    PaymentLogger.error("paymentUrl từ API PayOS là null hoặc rỗng: " + paymentUrl);
                    return PayOSPaymentResult.error("Không thể tạo liên kết thanh toán: paymentUrl không hợp lệ");
                }

                if (orderCode == null || orderCode.trim().isEmpty()) {
                    PaymentLogger.error("orderCode từ API PayOS là null hoặc rỗng: " + orderCode);
                    return PayOSPaymentResult.error("Không thể tạo liên kết thanh toán: orderCode không hợp lệ");
                }

                String qrCodeUrl = fetchQRCodeUrl(paymentUrl);
                if (qrCodeUrl == null) {
                    PaymentLogger.error("Không thể lấy URL mã QR từ checkoutUrl: " + paymentUrl);
                    return PayOSPaymentResult.error("Không thể lấy mã QR từ PayOS");
                }

                invoice.setProviderTransactionId(orderCode);
                PaymentLogger.info("Liên kết thanh toán PayOS tạo thành công: orderCode=" + orderCode + 
                                  ", paymentUrl=" + paymentUrl + ", qrCodeUrl=" + qrCodeUrl);
                return PayOSPaymentResult.success(orderCode, paymentUrl, qrCodeUrl, 
                    "Thanh toán hóa đơn #" + invoice.getInvoiceId());
            } else {
                String errorMsg = (String) response.get("desc");
                PaymentLogger.error("PayOS API Error: " + errorMsg);
                return PayOSPaymentResult.error("Lỗi tạo liên kết thanh toán: " + (errorMsg != null ? errorMsg : "Không có thông tin lỗi"));
            }
        } catch (Exception e) {
            PaymentLogger.error("Lỗi tạo liên kết thanh toán PayOS: " + e.getMessage(), e);
            return PayOSPaymentResult.error("Lỗi hệ thống: " + e.getMessage());
        }
    }

    private String fetchQRCodeUrl(String checkoutUrl) {
        int retries = PaymentConfig.getRetryCount();
        for (int attempt = 1; attempt <= retries; attempt++) {
            try {
                PaymentLogger.info("Tải HTML từ checkoutUrl để lấy QR code (lần " + attempt + "/" + retries + "): " + checkoutUrl);
                try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                    HttpGet httpGet = new HttpGet(checkoutUrl);
                    httpGet.setConfig(org.apache.hc.client5.http.config.RequestConfig.custom()
                            .setConnectTimeout(PaymentConfig.getApiTimeoutMs(), TimeUnit.MILLISECONDS)
                            .setResponseTimeout(PaymentConfig.getApiTimeoutMs(), TimeUnit.MILLISECONDS)
                            .build());
                    try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                        HttpEntity entity = response.getEntity();
                        if (entity == null) {
                            PaymentLogger.error("Không nhận được nội dung từ checkoutUrl");
                            continue;
                        }
                        String htmlContent = EntityUtils.toString(entity, StandardCharsets.UTF_8);

                        Document doc = Jsoup.parse(htmlContent);
                        Element imgElement = doc.selectFirst("img[src*=vietqr.io]");
                        if (imgElement == null) {
                            imgElement = doc.selectFirst("img.w-\\[(?:80%|[0-9]+%\\)]");
                        }
                        if (imgElement == null) {
                            imgElement = doc.selectFirst("img.max-w-\\[(?:80%|[0-9]+%\\)]");
                        }
                        if (imgElement != null) {
                            String qrCodeUrl = imgElement.attr("src");
                            if (qrCodeUrl != null && qrCodeUrl.startsWith("http")) {
                                PaymentLogger.info("Đã tìm thấy URL mã QR: " + qrCodeUrl);
                                return qrCodeUrl;
                            } else {
                                PaymentLogger.error("URL mã QR không hợp lệ: " + qrCodeUrl);
                                return null;
                            }
                        } else {
                            PaymentLogger.error("Không tìm thấy thẻ img chứa mã QR trong HTML của checkoutUrl");
                            return null;
                        }
                    }
                }
            } catch (Exception e) {
                PaymentLogger.error("Lỗi tải QR code từ checkoutUrl (lần " + attempt + "/" + retries + "): " + e.getMessage(), e);
                if (attempt == retries) {
                    return null;
                }
                try {
                    Thread.sleep(1000 * attempt);
                } catch (InterruptedException ie) {
                    PaymentLogger.error("Lỗi khi đợi để thử lại: " + ie.getMessage(), ie);
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null;
    }

    public PayOSPaymentStatus checkPaymentStatus(String orderCode) {
        int retries = PaymentConfig.getRetryCount();
        for (int attempt = 1; attempt <= retries; attempt++) {
            try {
                PaymentLogger.info("Kiểm tra trạng thái thanh toán PayOS cho orderCode (lần " + attempt + "/" + retries + "): " + orderCode);
                try (CloseableHttpClient statusClient = HttpClients.createDefault()) {
                    HttpGet statusGet = new HttpGet(PaymentConfig.getStatusApiUrl() + orderCode);
                    statusGet.setConfig(org.apache.hc.client5.http.config.RequestConfig.custom()
                            .setConnectTimeout(PaymentConfig.getApiTimeoutMs(), TimeUnit.MILLISECONDS)
                            .setResponseTimeout(PaymentConfig.getApiTimeoutMs(), TimeUnit.MILLISECONDS)
                            .build());
                    statusGet.setHeader("x-client-id", PaymentConfig.getPayOSClientId());
                    statusGet.setHeader("x-api-key", PaymentConfig.getPayOSApiKey());
                    try (CloseableHttpResponse statusResponse = statusClient.execute(statusGet)) {
                        HttpEntity statusEntity = statusResponse.getEntity();
                        String statusJson = EntityUtils.toString(statusEntity, StandardCharsets.UTF_8);

                        JSONObject jsonResponse = (JSONObject) jsonParser.parse(statusJson);
                        if ("00".equals(jsonResponse.get("code"))) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> data = (Map<String, Object>) jsonResponse.get("data");
                            if (data == null) {
                                PaymentLogger.error("Dữ liệu phản hồi từ API PayOS không chứa trường 'data'");
                                return PayOSPaymentStatus.error(orderCode, "Dữ liệu phản hồi không hợp lệ: thiếu trường 'data'");
                            }

                            String status = (String) data.get("status");
                            BigDecimal amount = new BigDecimal(data.get("amount").toString());
                            String transactionId = (String) data.get("id");

                            if (status == null || status.trim().isEmpty()) {
                                PaymentLogger.error("Trạng thái từ API PayOS là null hoặc rỗng");
                                return PayOSPaymentStatus.error(orderCode, "Trạng thái không hợp lệ");
                            }

                            PaymentLogger.info("PayOS payment status: " + status + " for orderCode: " + orderCode);
                            return new PayOSPaymentStatus(status, amount, transactionId, orderCode);
                        } else {
                            String errorMsg = (String) jsonResponse.get("desc");
                            PaymentLogger.warning("PayOS payment status check failed: " + errorMsg);
                            return PayOSPaymentStatus.pending(orderCode);
                        }
                    }
                }
            } catch (Exception e) {
                PaymentLogger.error("Lỗi kiểm tra trạng thái thanh toán PayOS (lần " + attempt + "/" + retries + "): " + e.getMessage(), e);
                if (attempt == retries) {
                    return PayOSPaymentStatus.error(orderCode, e.getMessage());
                }
                try {
                    Thread.sleep(1000 * attempt);
                } catch (InterruptedException ie) {
                    PaymentLogger.error("Lỗi khi đợi để thử lại: " + ie.getMessage(), ie);
                    Thread.currentThread().interrupt();
                    return PayOSPaymentStatus.error(orderCode, ie.getMessage());
                }
            }
        }
        return PayOSPaymentStatus.error(orderCode, "Không thể kiểm tra trạng thái sau " + retries + " lần thử");
    }

    public boolean cancelPayment(String orderCode, String reason) {
        try {
            PaymentLogger.info("Hủy thanh toán PayOS: orderCode=" + orderCode + ", Lý do: " + reason);
            Map<String, Object> cancelData = new HashMap<>();
            cancelData.put("cancellationReason", reason);

            Map<String, Object> response = cancelPaymentAPI(orderCode, cancelData);
            if (response == null || response.isEmpty()) {
                PaymentLogger.error("Phản hồi từ API hủy thanh toán PayOS là null hoặc rỗng");
                return false;
            }

            boolean success = "00".equals(response.get("code"));
            if (success) {
                PaymentLogger.info("PayOS payment cancelled successfully: " + orderCode);
            } else {
                String errorMsg = (String) response.get("desc");
                PaymentLogger.error("PayOS payment cancellation failed: " + (errorMsg != null ? errorMsg : "Không có thông tin lỗi"));
            }
            return success;
        } catch (Exception e) {
            PaymentLogger.error("Lỗi hủy thanh toán PayOS: " + e.getMessage(), e);
            return false;
        }
    }

    private boolean validateInvoice(Invoice invoice) {
        try {
            if (invoice == null || invoice.getInvoiceId() <= 0 || invoice.getTotal() == null ||
                invoice.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
                PaymentLogger.error("Hóa đơn không hợp lệ: " + (invoice == null ? "null" : invoice.getInvoiceId()));
                return false;
            }
            long amount = invoice.getTotal().longValue();
            boolean validAmount = PaymentConfig.isValidAmount(amount);
            if (!validAmount) {
                PaymentLogger.error("Số tiền ngoài giới hạn (" + PaymentConfig.getOrderMinAmount() + " - " + 
                                   PaymentConfig.getOrderMaxAmount() + " VND): " + amount);
            }
            return validAmount;
        } catch (Exception e) {
            PaymentLogger.error("Lỗi kiểm tra tính hợp lệ của hóa đơn: " + e.getMessage(), e);
            return false;
        }
    }

    private Map<String, Object> buildPaymentRequest(Invoice invoice) {
        try {
            String orderCode = PaymentConfig.generateUniqueOrderCode(invoice.getInvoiceId());
            long amount = invoice.getTotal().longValue();
            String description = "Thanh toán hóa đơn #" + invoice.getInvoiceId() + " - Pet Care Center";

            String buyerName = "Khách hàng";
            String buyerEmail = "";
            String buyerPhone = "";
            if (invoice.getOrder() != null && invoice.getOrder().getCustomer() != null) {
                Customer customer = invoice.getOrder().getCustomer();
                buyerName = customer.getFullName() != null ? customer.getFullName() : "Khách hàng";
                buyerEmail = customer.getEmail() != null ? customer.getEmail() : "";
                buyerPhone = customer.getPhone() != null ? customer.getPhone() : "";
            }

            String returnUrl = PaymentConfig.getReturnUrlSuccess() + "?invoiceId=" + invoice.getInvoiceId();
            String cancelUrl = PaymentConfig.getReturnUrlCancel() + "?invoiceId=" + invoice.getInvoiceId();

            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("orderCode", orderCode);
            paymentData.put("amount", amount);
            paymentData.put("description", description);
            paymentData.put("buyerName", buyerName);
            if (!buyerEmail.isEmpty()) paymentData.put("buyerEmail", buyerEmail);
            if (!buyerPhone.isEmpty()) paymentData.put("buyerPhone", buyerPhone);
            paymentData.put("returnUrl", returnUrl);
            paymentData.put("cancelUrl", cancelUrl);

            JSONArray itemsArray = new JSONArray();
            JSONObject item = new JSONObject();
            item.put("name", "Hóa đơn #" + invoice.getInvoiceId());
            item.put("quantity", 1);
            item.put("price", amount);
            itemsArray.add(item);
            paymentData.put("items", itemsArray);

            PaymentLogger.info("Dữ liệu yêu cầu thanh toán đã được xây dựng: " + paymentData);
            return paymentData;
        } catch (Exception e) {
            PaymentLogger.error("Lỗi xây dựng dữ liệu thanh toán: " + e.getMessage(), e);
            throw new RuntimeException("Lỗi xây dựng dữ liệu thanh toán", e);
        }
    }

    private Map<String, Object> createPaymentLinkAPI(Map<String, Object> paymentData) throws Exception {
        String endpoint = PaymentConfig.getPayOSApiUrl() + "/v2/payment-requests";
        return makePayOSApiCall("POST", endpoint, paymentData);
    }

    private Map<String, Object> getPaymentInfoAPI(String orderCode) throws Exception {
        String endpoint = PaymentConfig.getPayOSApiUrl() + "/v2/payment-requests/" + orderCode;
        return makePayOSApiCall("GET", endpoint, null);
    }

    private Map<String, Object> cancelPaymentAPI(String orderCode, Map<String, Object> cancelData) throws Exception {
        String endpoint = PaymentConfig.getPayOSApiUrl() + "/v2/payment-requests/" + orderCode + "/cancel";
        return makePayOSApiCall("POST", endpoint, cancelData);
    }

    private Map<String, Object> makePayOSApiCall(String method, String endpoint, Object requestBody) throws Exception {
        int retries = PaymentConfig.getRetryCount();
        for (int attempt = 1; attempt <= retries; attempt++) {
            try {
                PaymentLogger.info("Gửi yêu cầu API PayOS (lần " + attempt + "/" + retries + "): " + method + " " + endpoint);
                try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                    if ("POST".equals(method)) {
                        HttpPost httpPost = new HttpPost(endpoint);
                        httpPost.setConfig(org.apache.hc.client5.http.config.RequestConfig.custom()
                                .setConnectTimeout(PaymentConfig.getApiTimeoutMs(), TimeUnit.MILLISECONDS)
                                .setResponseTimeout(PaymentConfig.getApiTimeoutMs(), TimeUnit.MILLISECONDS)
                                .build());
                        httpPost.setHeader("Content-Type", "application/json");
                        httpPost.setHeader("x-client-id", PaymentConfig.getPayOSClientId().trim());
                        httpPost.setHeader("x-api-key", PaymentConfig.getPayOSApiKey().trim());

                        if (requestBody != null) {
                            PaymentLogger.info("Dữ liệu yêu cầu: " + requestBody.toString());
                            JSONObject jsonObject = new JSONObject((Map<String, Object>) requestBody);
                            httpPost.setEntity(new StringEntity(jsonObject.toJSONString(), StandardCharsets.UTF_8));
                        }

                        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                            return processApiResponse(response);
                        }
                    } else if ("GET".equals(method)) {
                        HttpGet httpGet = new HttpGet(endpoint);
                        httpGet.setConfig(org.apache.hc.client5.http.config.RequestConfig.custom()
                                .setConnectTimeout(PaymentConfig.getApiTimeoutMs(), TimeUnit.MILLISECONDS)
                                .setResponseTimeout(PaymentConfig.getApiTimeoutMs(), TimeUnit.MILLISECONDS)
                                .build());
                        httpGet.setHeader("x-client-id", PaymentConfig.getPayOSClientId().trim());
                        httpGet.setHeader("x-api-key", PaymentConfig.getPayOSApiKey().trim());

                        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                            return processApiResponse(response);
                        }
                    } else {
                        throw new UnsupportedOperationException("Phương thức HTTP không được hỗ trợ: " + method);
                    }
                }
            } catch (Exception e) {
                PaymentLogger.error("Lỗi gọi API PayOS (lần " + attempt + "/" + retries + "): " + e.getMessage(), e);
                if (attempt == retries) {
                    throw e;
                }
                try {
                    Thread.sleep(1000 * attempt);
                } catch (InterruptedException ie) {
                    PaymentLogger.error("Lỗi khi đợi để thử lại: " + ie.getMessage(), ie);
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
        return new HashMap<>();
    }

    private Map<String, Object> processApiResponse(CloseableHttpResponse response) throws Exception {
        try {
            int responseCode = response.getCode();
            HttpEntity entity = response.getEntity();
            String responseBody = entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : "";
            PaymentLogger.info("Phản hồi API PayOS: Mã trạng thái=" + responseCode + ", Nội dung=" + responseBody);

            if (responseCode >= 400) {
                PaymentLogger.error("Yêu cầu API PayOS thất bại với mã trạng thái: " + responseCode);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("code", String.valueOf(responseCode));
                errorResponse.put("desc", "Lỗi từ API PayOS: " + responseBody);
                return errorResponse;
            }

            return responseBody.isEmpty() ? new HashMap<>() : (Map<String, Object>) jsonParser.parse(responseBody);
        } catch (Exception e) {
            PaymentLogger.error("Lỗi xử lý phản hồi từ API PayOS: " + e.getMessage(), e);
            throw e;
        }
    }

    private void validateConfiguration() {
        try {
            String clientId = PaymentConfig.getPayOSClientId();
            String apiKey = PaymentConfig.getPayOSApiKey();
            String checksumKey = PaymentConfig.getPayOSChecksumKey();

            if (clientId == null || clientId.trim().isEmpty() ||
                apiKey == null || apiKey.trim().isEmpty() ||
                checksumKey == null || checksumKey.trim().isEmpty()) {
                PaymentLogger.error("Cấu hình PayOS không hợp lệ: " +
                    "clientId=" + clientId + ", apiKey=" + (apiKey == null ? "null" : "[HIDDEN]") + 
                    ", checksumKey=" + (checksumKey == null ? "null" : "[HIDDEN]"));
                throw new IllegalStateException("Cấu hình PayOS không hợp lệ");
            } else {
                PaymentLogger.info("Cấu hình PayOS hợp lệ: " +
                    "clientId=" + clientId + ", apiKey=[HIDDEN], checksumKey=[HIDDEN]");
            }
        } catch (Exception e) {
            PaymentLogger.error("Lỗi kiểm tra cấu hình PayOS: " + e.getMessage(), e);
            throw new RuntimeException("Lỗi kiểm tra cấu hình PayOS", e);
        }
    }

    public static class PayOSPaymentResult {
        private final boolean success;
        private final String orderCode;
        private final String paymentUrl;
        private final String qrCodeUrl;
        private final String description;
        private final String errorMessage;

        private PayOSPaymentResult(boolean success, String orderCode, String paymentUrl, String qrCodeUrl, 
                                  String description, String errorMessage) {
            this.success = success;
            this.orderCode = orderCode;
            this.paymentUrl = paymentUrl;
            this.qrCodeUrl = qrCodeUrl;
            this.description = description;
            this.errorMessage = errorMessage;
        }

        public static PayOSPaymentResult success(String orderCode, String paymentUrl, String qrCodeUrl, String description) {
            return new PayOSPaymentResult(true, orderCode, paymentUrl, qrCodeUrl, description, null);
        }

        public static PayOSPaymentResult error(String errorMessage) {
            return new PayOSPaymentResult(false, null, null, null, null, errorMessage);
        }

        public boolean isSuccess() { return success; }
        public String getOrderCode() { return orderCode; }
        public String getPaymentUrl() { return paymentUrl; }
        public String getQrCodeUrl() { return qrCodeUrl; }
        public String getDescription() { return description; }
        public String getErrorMessage() { return errorMessage; }
    }

    public static class PayOSPaymentStatus {
        private final String status;
        private final BigDecimal amount;
        private final String transactionId;
        private final String orderCode;
        private final String message;

        public PayOSPaymentStatus(String status, BigDecimal amount, String transactionId, String orderCode) {
            this.status = status;
            this.amount = amount;
            this.transactionId = transactionId;
            this.orderCode = orderCode;
            this.message = null;
        }

        public PayOSPaymentStatus(String status, String orderCode, String message) {
            this.status = status;
            this.amount = null;
            this.transactionId = null;
            this.orderCode = orderCode;
            this.message = message;
        }

        public static PayOSPaymentStatus pending(String orderCode) {
            return new PayOSPaymentStatus("PENDING", orderCode, "Payment is pending");
        }

        public static PayOSPaymentStatus error(String orderCode, String message) {
            return new PayOSPaymentStatus("ERROR", orderCode, message);
        }

        public String getStatus() { return status; }
        public BigDecimal getAmount() { return amount; }
        public String getTransactionId() { return transactionId; }
        public String getOrderCode() { return orderCode; }
        public String getMessage() { return message; }
        public boolean isPaid() { return "PAID".equals(status) || "PROCESSING".equals(status); }
        public boolean isCancelled() { return "CANCELLED".equals(status); }
        public boolean isPending() { return "PENDING".equals(status); }
    }
}