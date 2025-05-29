package service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javafx.scene.image.Image;
import model.Invoice;
import utils.PaymentLogger;
import utils.PaymentConfig;
import enums.PaymentMethodEnum;
import enums.StatusEnum;
import repository.InvoiceRepository;
import repository.OrderRepository;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import vn.payos.PayOS;
import vn.payos.exception.PayOSException;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class QRPaymentService {
    private static QRPaymentService instance;
    private final PayOS payOS;
    private final PayOSService payOSService;
    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final Map<String, QRPaymentResult> transactionCache;

    private QRPaymentService() {
        this.payOS = new PayOS(PaymentConfig.getPayOSClientId(), PaymentConfig.getPayOSApiKey(), PaymentConfig.getPayOSChecksumKey());
        this.payOSService = PayOSService.getInstance();
        this.invoiceRepository = InvoiceRepository.getInstance();
        this.orderRepository = OrderRepository.getInstance();
        this.transactionCache = new HashMap<>();
        PaymentLogger.info("QRPaymentService initialized");
    }

    public static synchronized QRPaymentService getInstance() {
        if (instance == null) {
            instance = new QRPaymentService();
        }
        return instance;
    }

    public QRPaymentResult createQRPayment(Invoice invoice) {
        try {
            PaymentLogger.info("Bắt đầu tạo QR payment cho hóa đơn #" + invoice.getInvoiceId());
            
            if (!isValidInvoice(invoice)) {
                PaymentLogger.error("Hóa đơn không hợp lệ để tạo QR payment");
                return QRPaymentResult.error("Hóa đơn không hợp lệ");
            }

            if (invoice.getTransactionId() == null) {
                String transactionId = generateTransactionId(invoice.getInvoiceId());
                invoice.setTransactionId(transactionId);
                int updateResult = invoiceRepository.update(invoice);
                PaymentLogger.info("Cập nhật transactionId cho hóa đơn #" + invoice.getInvoiceId() + ": " + transactionId + 
                                  ", Kết quả cập nhật: " + updateResult);
            }

            String transactionId = invoice.getTransactionId();

            if (transactionCache.containsKey(transactionId)) {
                QRPaymentResult cachedResult = transactionCache.get(transactionId);
                PaymentLogger.info("Giao dịch " + transactionId + " đã tồn tại trong cache, trả lại kết quả cũ");
                return cachedResult;
            }

            String description = "Thanh toán hóa đơn #" + invoice.getInvoiceId();
            long amount = invoice.getTotal().longValue();
            int orderId = invoice.getInvoiceId();

            List<ItemData> items = new ArrayList<>();
            items.add(ItemData.builder()
                    .name("Dịch vụ hóa đơn #" + orderId)
                    .quantity(1)
                    .price((int) amount)
                    .build());

            String returnUrl = PaymentConfig.generateReturnUrl(orderId, amount, description);
            String cancelUrl = PaymentConfig.generateCancelUrl(orderId, amount, description);

            PaymentData paymentData = PaymentData.builder()
                    .orderCode((long) orderId)
                    .amount((int) amount)
                    .description(description)
                    .items(items)
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .build();

            PaymentLogger.info("Gọi API PayOS để tạo liên kết thanh toán cho hóa đơn #" + invoice.getInvoiceId());
            CheckoutResponseData paymentResult;
            try {
                paymentResult = payOS.createPaymentLink(paymentData);
            } catch (PayOSException ex) {
                if (ex.getMessage().contains("Đơn thanh toán đã tồn tại")) {
                    PaymentLogger.info("Đơn hàng " + orderId + " đã tồn tại trên PayOS, kiểm tra trạng thái");
                    PaymentStatusResult statusResult = checkPaymentStatus(transactionId);
                    if (statusResult.isPending()) {
                        if (transactionCache.containsKey(transactionId)) {
                            return transactionCache.get(transactionId);
                        } else {
                            return QRPaymentResult.error("Không tìm thấy thông tin giao dịch trong bộ nhớ, vui lòng làm mới");
                        }
                    } else if (statusResult.isCompleted()) {
                        return QRPaymentResult.error("Đơn hàng này đã được thanh toán");
                    } else if (statusResult.isFailed() || statusResult.isError()) {
                        return QRPaymentResult.error("Đơn hàng này đã bị hủy hoặc hết hạn");
                    }
                }
                PaymentLogger.error("Lỗi tạo link thanh toán từ PayOS: " + ex.getMessage(), ex);
                return QRPaymentResult.error("Lỗi tạo link thanh toán: " + ex.getMessage());
            }

            String checkoutUrl = paymentResult.getCheckoutUrl();
            PaymentLogger.info("Checkout URL: " + checkoutUrl);

            String qrCodeUrl = fetchQRCodeUrl(checkoutUrl);
            if (qrCodeUrl == null) {
                PaymentLogger.error("Không thể lấy URL mã QR từ checkoutUrl: " + checkoutUrl);
                return QRPaymentResult.error("Không thể lấy mã QR từ PayOS");
            }

            PaymentLogger.info("Tải hình ảnh QR từ URL: " + qrCodeUrl);
            Image qrImage = new Image(qrCodeUrl, true);
            if (qrImage.isError()) {
                PaymentLogger.error("Lỗi tải hình ảnh QR từ URL: " + qrCodeUrl);
                return QRPaymentResult.error("Lỗi tải hình ảnh QR từ PayOS: " + qrImage.getException().getMessage());
            }

            invoice.setProviderTransactionId(String.valueOf(paymentResult.getOrderCode()));
            invoice.setPaymentProvider("PAYOS");
            invoice.setPaymentMethod(PaymentMethodEnum.QR);
            invoice.setStatus(StatusEnum.PENDING);
            int updateResult = invoiceRepository.update(invoice);
            PaymentLogger.info("Cập nhật hóa đơn #" + invoice.getInvoiceId() + " với providerTransactionId: " + 
                              paymentResult.getOrderCode() + ", Kết quả cập nhật: " + updateResult);

            String customerInfo = invoice.getOrder() != null && invoice.getOrder().getCustomer() != null
                ? invoice.getOrder().getCustomer().getFullName() : "Khách hàng";
            String instructions = createPaymentInstructions(
                invoice.getInvoiceId(), invoice.getTotal().doubleValue(), customerInfo, checkoutUrl);

            QRPaymentResult result = QRPaymentResult.success(
                transactionId, qrImage, instructions, checkoutUrl, String.valueOf(paymentResult.getOrderCode()));

            transactionCache.put(transactionId, result);
            PaymentLogger.info("Lưu giao dịch " + transactionId + " vào cache");

            return result;
        } catch (Exception e) {
            PaymentLogger.error("Lỗi tạo QR payment: " + e.getMessage(), e);
            return QRPaymentResult.error("Lỗi hệ thống: " + e.getMessage());
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

    private String createPaymentInstructions(int invoiceId, double amount, String customerInfo, String paymentUrl) {
        return String.format(
            "💳 PET CARE CENTER - THANH TOÁN\n" +
            "═══════════════════════════════════════\n" +
            "📋 Hóa đơn: #%d\n" +
            "💰 Số tiền: %,.0f VND\n" +
            "👤 Khách hàng: %s\n" +
            "📅 Thời gian: %s\n" +
            "🔗 Link thanh toán: %s\n\n" +
            "⚠️ Lưu ý:\n" +
            "• Quét QR hoặc truy cập link để thanh toán\n" +
            "• Liên hệ nhân viên nếu có vấn đề\n" +
            "📱 Quét QR bằng app ngân hàng",
            invoiceId, amount, customerInfo != null ? customerInfo : "Khách hàng",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), paymentUrl);
    }

    public PaymentStatusResult checkPaymentStatus(String transactionId) {
        try {
            PaymentLogger.info("Kiểm tra trạng thái thanh toán cho giao dịch: " + transactionId);
            Invoice invoice = invoiceRepository.selectByCondition("transaction_id = ?", transactionId).stream().findFirst().orElse(null);
            if (invoice == null) {
                PaymentLogger.error("Không tìm thấy hóa đơn cho giao dịch: " + transactionId);
                return PaymentStatusResult.error("Không tìm thấy hóa đơn");
            }

            String orderCode = invoice.getProviderTransactionId();
            if (orderCode == null) {
                PaymentLogger.error("Không tìm thấy mã đơn hàng PayOS cho giao dịch: " + transactionId);
                return PaymentStatusResult.error("Không tìm thấy mã đơn hàng PayOS");
            }

            PayOSService.PayOSPaymentStatus payOSStatus = payOSService.checkPaymentStatus(orderCode);
            if (payOSStatus.isPaid()) {
                updatePaymentStatusInSystem(invoice, true, payOSStatus.getTransactionId());
                PaymentLogger.info("Thanh toán thành công cho giao dịch: " + transactionId);
                return PaymentStatusResult.completed("Thanh toán thành công!");
            } else if (payOSStatus.isCancelled()) {
                updatePaymentStatusInSystem(invoice, false, null);
                PaymentLogger.info("Thanh toán đã bị hủy cho giao dịch: " + transactionId);
                return PaymentStatusResult.failed("Thanh toán đã bị hủy");
            } else {
                PaymentLogger.info("Thanh toán đang chờ xử lý cho giao dịch: " + transactionId);
                return PaymentStatusResult.pending("Đang chờ thanh toán...");
            }
        } catch (Exception e) {
            PaymentLogger.error("Lỗi kiểm tra trạng thái thanh toán: " + e.getMessage(), e);
            return PaymentStatusResult.error("Lỗi kiểm tra trạng thái: " + e.getMessage());
        }
    }

    public CompletableFuture<PaymentStatusResult> checkPaymentStatusAsync(String transactionId) {
        return CompletableFuture.supplyAsync(() -> checkPaymentStatus(transactionId));
    }

    public QRPaymentResult refreshQRPayment(Invoice invoice) {
        try {
            PaymentLogger.info("Làm mới QR payment cho hóa đơn #" + invoice.getInvoiceId());
            cancelExistingPayment(invoice);

            if (invoice.getTransactionId() != null) {
                transactionCache.remove(invoice.getTransactionId());
                PaymentLogger.info("Xóa giao dịch " + invoice.getTransactionId() + " khỏi cache");
            }

            invoice.setTransactionId(generateTransactionId(invoice.getInvoiceId()));
            invoice.setProviderTransactionId(null);
            invoice.setStatus(StatusEnum.PENDING);
            int updateResult = invoiceRepository.update(invoice);
            PaymentLogger.info("Cập nhật hóa đơn #" + invoice.getInvoiceId() + " để làm mới QR, Kết quả cập nhật: " + updateResult);

            return createQRPayment(invoice);
        } catch (Exception e) {
            PaymentLogger.error("Lỗi làm mới QR payment: " + e.getMessage(), e);
            return QRPaymentResult.error("Lỗi làm mới thanh toán: " + e.getMessage());
        }
    }

    public boolean cancelQRPayment(String transactionId, String reason) {
        try {
            PaymentLogger.info("Hủy QR payment: " + transactionId + ", Lý do: " + reason);
            Invoice invoice = invoiceRepository.selectByCondition("transaction_id = ?", transactionId).stream().findFirst().orElse(null);
            if (invoice == null) {
                PaymentLogger.warning("Không tìm thấy hóa đơn với giao dịch: " + transactionId);
                return false;
            }

            if (invoice.getProviderTransactionId() != null) {
                boolean cancelResult = payOSService.cancelPayment(invoice.getProviderTransactionId(), reason);
                PaymentLogger.info("Kết quả hủy từ PayOS API: " + cancelResult);
            } else {
                PaymentLogger.warning("Không có providerTransactionId để hủy trên PayOS cho giao dịch: " + transactionId);
            }

            invoice.setStatus(StatusEnum.CANCELLED);
            invoice.setNote(invoice.getNote() != null ? invoice.getNote() + " - Đã hủy: " + reason : "Đã hủy: " + reason);
            int updateResult = invoiceRepository.update(invoice);
            PaymentLogger.info("Cập nhật trạng thái hủy cho hóa đơn #" + invoice.getInvoiceId() + ", Kết quả cập nhật: " + updateResult);

            transactionCache.remove(transactionId);
            PaymentLogger.info("Xóa giao dịch " + transactionId + " khỏi cache");

            return true;
        } catch (Exception e) {
            PaymentLogger.error("Lỗi hủy QR payment: " + e.getMessage(), e);
            return false;
        }
    }

    private boolean isValidInvoice(Invoice invoice) {
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

    private String generateTransactionId(int invoiceId) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            int random = (int) (Math.random() * 999);
            String transactionId = String.format("PAYOS%d%s%03d", invoiceId, timestamp, random);
            PaymentLogger.info("Tạo transactionId: " + transactionId);
            return transactionId;
        } catch (Exception e) {
            PaymentLogger.error("Lỗi tạo transactionId: " + e.getMessage(), e);
            throw new RuntimeException("Lỗi tạo transactionId", e);
        }
    }

    private void updatePaymentStatusInSystem(Invoice invoice, boolean isSuccess, String payosTransactionId) {
        try {
            if (isSuccess) {
                invoice.setStatus(StatusEnum.COMPLETED);
                invoice.setPaymentDate(new java.sql.Timestamp(System.currentTimeMillis()));
                invoice.setAmountPaid(invoice.getTotal());
                if (payosTransactionId != null) {
                    invoice.setNote(invoice.getNote() != null ? 
                        invoice.getNote() + " - PayOS TxnID: " + payosTransactionId : 
                        "PayOS TxnID: " + payosTransactionId);
                }
                
                if (invoice.getOrder() != null) {
                    invoice.getOrder().setStatus(StatusEnum.COMPLETED);
                    int orderUpdateResult = orderRepository.update(invoice.getOrder());
                    PaymentLogger.info("Cập nhật trạng thái đơn hàng #" + invoice.getOrder().getOrderId() + 
                                      " thành COMPLETED, Kết quả cập nhật: " + orderUpdateResult);
                } else {
                    PaymentLogger.warning("Hóa đơn #" + invoice.getInvoiceId() + " không có đơn hàng liên kết");
                }
            } else {
                invoice.setStatus(StatusEnum.FAILED);
            }
            
            int invoiceUpdateResult = invoiceRepository.update(invoice);
            PaymentLogger.info("Cập nhật trạng thái hóa đơn #" + invoice.getInvoiceId() + 
                              " thành " + invoice.getStatus() + ", Kết quả cập nhật: " + invoiceUpdateResult);

            transactionCache.remove(invoice.getTransactionId());
            PaymentLogger.info("Xóa giao dịch " + invoice.getTransactionId() + " khỏi cache do giao dịch hoàn tất hoặc thất bại");
        } catch (Exception e) {
            PaymentLogger.error("Lỗi cập nhật trạng thái thanh toán trong hệ thống: " + e.getMessage(), e);
        }
    }

    private void cancelExistingPayment(Invoice invoice) {
        try {
            if (invoice.getProviderTransactionId() != null && invoice.getStatus() == StatusEnum.PENDING) {
                boolean cancelResult = payOSService.cancelPayment(invoice.getProviderTransactionId(), "Làm mới thanh toán");
                PaymentLogger.info("Hủy thanh toán hiện tại " + invoice.getProviderTransactionId() + ": " + 
                                  (cancelResult ? "Thành công" : "Thất bại"));
                
                invoice.setStatus(StatusEnum.CANCELLED);
                invoice.setNote(invoice.getNote() != null ? 
                    invoice.getNote() + " - Đã hủy để làm mới" : 
                    "Đã hủy để làm mới");
                int updateResult = invoiceRepository.update(invoice);
                PaymentLogger.info("Cập nhật trạng thái hủy để làm mới cho hóa đơn #" + invoice.getInvoiceId() + 
                                  ", Kết quả cập nhật: " + updateResult);

                transactionCache.remove(invoice.getTransactionId());
                PaymentLogger.info("Xóa giao dịch " + invoice.getTransactionId() + " khỏi cache do hủy để làm mới");
            } else {
                PaymentLogger.info("Không cần hủy thanh toán hiện tại cho hóa đơn #" + invoice.getInvoiceId());
            }
        } catch (Exception e) {
            PaymentLogger.warning("Không thể hủy thanh toán hiện tại: " + e.getMessage());
        }
    }

    public static class QRPaymentResult {
        private final boolean success;
        private final String transactionId;
        private final Image qrImage;
        private final String instructions;
        private final String paymentUrl;
        private final String orderCode;
        private final String errorMessage;

        private QRPaymentResult(boolean success, String transactionId, Image qrImage, String instructions, 
                               String paymentUrl, String orderCode, String errorMessage) {
            this.success = success;
            this.transactionId = transactionId;
            this.qrImage = qrImage;
            this.instructions = instructions;
            this.paymentUrl = paymentUrl;
            this.orderCode = orderCode;
            this.errorMessage = errorMessage;
        }

        public static QRPaymentResult success(String transactionId, Image qrImage, String instructions, 
                                             String paymentUrl, String orderCode) {
            return new QRPaymentResult(true, transactionId, qrImage, instructions, paymentUrl, orderCode, null);
        }

        public static QRPaymentResult error(String errorMessage) {
            return new QRPaymentResult(false, null, null, null, null, null, errorMessage);
        }

        public boolean isSuccess() { return success; }
        public String getTransactionId() { return transactionId; }
        public Image getQrImage() { return qrImage; }
        public String getInstructions() { return instructions; }
        public String getPaymentUrl() { return paymentUrl; }
        public String getOrderCode() { return orderCode; }
        public String getErrorMessage() { return errorMessage; }
    }

    public static class PaymentStatusResult {
        private final String status;
        private final String message;

        private PaymentStatusResult(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public static PaymentStatusResult completed(String message) {
            return new PaymentStatusResult("COMPLETED", message);
        }

        public static PaymentStatusResult pending(String message) {
            return new PaymentStatusResult("PENDING", message);
        }

        public static PaymentStatusResult failed(String message) {
            return new PaymentStatusResult("FAILED", message);
        }

        public static PaymentStatusResult error(String message) {
            return new PaymentStatusResult("ERROR", message);
        }

        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public boolean isCompleted() { return "COMPLETED".equals(status); }
        public boolean isPending() { return "PENDING".equals(status); }
        public boolean isFailed() { return "FAILED".equals(status); }
        public boolean isError() { return "ERROR".equals(status); }
    }
}