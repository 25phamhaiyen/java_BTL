package controllers.Staff;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Invoice;
import service.QRPaymentService;
import service.QRPaymentService.QRPaymentResult;
import service.QRPaymentService.PaymentStatusResult;
import utils.PaymentLogger;
import utils.PaymentConfig;
import javafx.scene.image.Image;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import enums.StatusEnum;
import java.util.concurrent.CompletableFuture;
import repository.InvoiceRepository;
import java.math.BigDecimal;
/**
 * Controller cho thanh toán QR PayOS - Tích hợp với API PayOS thật
 */
public class QRPaymentViewController implements Initializable {
    
    // UI Components
    @FXML private ImageView qrImageView;
    @FXML private Label statusLabel;
    @FXML private Label invoiceInfoLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button checkStatusButton;
    @FXML private Button refreshQRButton;
    @FXML private Button closeButton;
    @FXML private VBox transferInfoBox;
    @FXML private TextArea transferInstructionsArea;
    @FXML private Label countdownLabel;
    @FXML private Button cancelPaymentButton;
    @FXML private Label warningLabel;
    @FXML private Button openPaymentUrlButton;
    @FXML private Button debugButton;
    
    // Services and data
    private QRPaymentService qrPaymentService;
    private Invoice invoice;
    private InvoiceViewController parentController;
    private String currentTransactionId;
    private String currentOrderCode;
    private String paymentUrl;
    private InvoiceRepository invoiceRepository;
    
    // Executors for background tasks
    private ScheduledExecutorService countdownExecutor;
    private ScheduledExecutorService statusCheckExecutor;
    
    // State management
    private AtomicInteger remainingSeconds = new AtomicInteger(900); // 15 phút cho PayOS
    private volatile boolean paymentSuccess = false;
    private volatile boolean paymentCancelled = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            qrPaymentService = QRPaymentService.getInstance();
            invoiceRepository = InvoiceRepository.getInstance();
            
            setupInitialUI();
            PaymentLogger.info("QRPaymentViewController initialized at " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        } catch (Exception e) {
            PaymentLogger.error("Lỗi khởi tạo QRPaymentViewController: " + e.getMessage(), e);
            showError("Lỗi khởi tạo giao diện: " + e.getMessage());
        }
    }
    
    private void setupInitialUI() {
        try {
            progressIndicator.setProgress(-1);
            statusLabel.setText("Đang khởi tạo thanh toán...");
            
            if (warningLabel != null) {
                warningLabel.setText("⚠️ THANH TOÁN THẬT - SỬ DỤNG TIỀN THẬT");
                warningLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14px;");
            }
            
            checkStatusButton.setDisable(true);
            refreshQRButton.setDisable(true);
            cancelPaymentButton.setDisable(true);
            if (openPaymentUrlButton != null) {
                openPaymentUrlButton.setDisable(true);
            }
            
            checkStatusButton.setOnAction(e -> checkPaymentStatusManually());
            refreshQRButton.setOnAction(e -> refreshQRCode());
            cancelPaymentButton.setOnAction(e -> cancelPayment());
            closeButton.setOnAction(e -> closeWindow());
            if (openPaymentUrlButton != null) {
                openPaymentUrlButton.setOnAction(e -> openPaymentUrl());
            }
            
            if (debugButton != null) {
                debugButton.setOnAction(e -> debugPaymentStatus());
                debugButton.setVisible(PaymentConfig.isDebugMode());
            }
        } catch (Exception e) {
            PaymentLogger.error("Lỗi thiết lập giao diện: " + e.getMessage(), e);
            showError("Lỗi thiết lập giao diện: " + e.getMessage());
        }
    }
    
    public void setInvoice(Invoice invoice) {
        try {
            if (invoice == null || invoice.getStatus() != StatusEnum.PENDING) {
                showError("Hóa đơn không hợp lệ hoặc không ở trạng thái chờ thanh toán");
                PaymentLogger.error("Hóa đơn không hợp lệ: " + (invoice == null ? "null" : "Trạng thái " + invoice.getStatus()));
                return;
            }
            
            if (invoice.getTotal() == null || invoice.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
                showError("Số tiền hóa đơn không hợp lệ");
                PaymentLogger.error("Số tiền hóa đơn không hợp lệ: " + (invoice.getTotal() == null ? "null" : invoice.getTotal()));
                return;
            }
            
            this.invoice = invoice;
            displayInvoiceInfo();
            createQRPayment();
        } catch (Exception e) {
            PaymentLogger.error("Lỗi thiết lập hóa đơn: " + e.getMessage(), e);
            showError("Lỗi thiết lập hóa đơn: " + e.getMessage());
        }
    }
    
    private void displayInvoiceInfo() {
        try {
            NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            String invoiceInfo = String.format("HÓA ĐƠN #%d - Số tiền: %s VND", 
                    invoice.getInvoiceId(), formatter.format(invoice.getTotal().doubleValue()));
            
            if (invoice.getOrder() != null && invoice.getOrder().getCustomer() != null) {
                invoiceInfo += String.format("\nKhách hàng: %s", invoice.getOrder().getCustomer().getFullName());
            }
            
            invoiceInfoLabel.setText(invoiceInfo);
            PaymentLogger.info("Hiển thị thông tin hóa đơn #" + invoice.getInvoiceId());
        } catch (Exception e) {
            PaymentLogger.error("Lỗi hiển thị thông tin hóa đơn: " + e.getMessage(), e);
            showError("Lỗi hiển thị thông tin hóa đơn");
        }
    }
    
    private void createQRPayment() {
        try {
            statusLabel.setText("Đang tạo thanh toán...");
            progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            
            PaymentLogger.info("Tạo thanh toán QR cho hóa đơn #" + invoice.getInvoiceId());
            
            CompletableFuture.supplyAsync(() -> {
                try {
                    QRPaymentResult result = qrPaymentService.createQRPayment(invoice);
                    if (!result.isSuccess()) {
                        PaymentLogger.error("Tạo QR thất bại: " + result.getErrorMessage());
                    }
                    return result;
                } catch (Exception e) {
                    PaymentLogger.error("Lỗi tạo QR trong luồng nền: " + e.getMessage(), e);
                    throw new RuntimeException("Lỗi tạo QR: " + e.getMessage(), e);
                }
            }).thenAccept(this::handleQRPaymentResult)
              .exceptionally(throwable -> {
                  Platform.runLater(() -> showError("Lỗi tạo thanh toán: " + throwable.getMessage()));
                  return null;
              });
        } catch (Exception e) {
            PaymentLogger.error("Lỗi khởi tạo thanh toán QR: " + e.getMessage(), e);
            showError("Lỗi khởi tạo thanh toán QR: " + e.getMessage());
        }
    }
    
    private void handleQRPaymentResult(QRPaymentResult result) {
        Platform.runLater(() -> {
            try {
                if (result == null) {
                    showError("Kết quả tạo QR không hợp lệ");
                    PaymentLogger.error("Kết quả tạo QR là null");
                    return;
                }
                
                if (result.isSuccess()) {
                    currentTransactionId = result.getTransactionId();
                    currentOrderCode = result.getOrderCode();
                    paymentUrl = result.getPaymentUrl();
                    
                    if (result.getQrImage() == null) {
                        showError("Không thể tải hình ảnh QR");
                        PaymentLogger.error("Hình ảnh QR trả về là null cho giao dịch " + currentTransactionId);
                        return;
                    }
                    
                    qrImageView.setImage(result.getQrImage());
                    transferInstructionsArea.setText(result.getInstructions());
                    
                    statusLabel.setText("Thanh toán đã được tạo. Quét QR để thanh toán.");
                    statusLabel.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
                    progressIndicator.setProgress(0);
                    
                    checkStatusButton.setDisable(false);
                    refreshQRButton.setDisable(false);
                    cancelPaymentButton.setDisable(false);
                    if (openPaymentUrlButton != null) {
                        openPaymentUrlButton.setDisable(false);
                    }
                    
                    startCountdown();
                    startAutoCheck();
                    
                    PaymentLogger.info("Tạo QR thành công cho giao dịch: " + currentTransactionId + ", URL: " + paymentUrl);
                } else {
                    showError(result.getErrorMessage());
                    if (result.getErrorMessage().contains("Đơn hàng này đã được thanh toán") ||
                        result.getErrorMessage().contains("Đơn hàng này đã bị hủy hoặc hết hạn") ||
                        result.getErrorMessage().contains("Không tìm thấy thông tin giao dịch trong bộ nhớ")) {
                        checkStatusButton.setDisable(true);
                        refreshQRButton.setDisable(true);
                        cancelPaymentButton.setDisable(true);
                        if (openPaymentUrlButton != null) {
                            openPaymentUrlButton.setDisable(true);
                        }
                    }
                }
            } catch (Exception e) {
                PaymentLogger.error("Lỗi xử lý kết quả QR: " + e.getMessage(), e);
                showError("Lỗi xử lý kết quả QR: " + e.getMessage());
            }
        });
    }
    
    private void startCountdown() {
        try {
            if (countdownExecutor != null && !countdownExecutor.isShutdown()) {
                countdownExecutor.shutdown();
            }
            
            remainingSeconds.set(900);
            updateCountdownLabel();
            
            countdownExecutor = Executors.newSingleThreadScheduledExecutor();
            countdownExecutor.scheduleAtFixedRate(() -> {
                try {
                    if (remainingSeconds.get() > 0 && !paymentSuccess && !paymentCancelled) {
                        remainingSeconds.decrementAndGet();
                        Platform.runLater(this::updateCountdownLabel);
                    } else if (remainingSeconds.get() <= 0 && !paymentSuccess && !paymentCancelled) {
                        Platform.runLater(this::handlePaymentTimeout);
                        countdownExecutor.shutdown();
                    }
                } catch (Exception e) {
                    PaymentLogger.error("Lỗi trong countdown: " + e.getMessage(), e);
                }
            }, 0, 1, TimeUnit.SECONDS);
        } catch (Exception e) {
            PaymentLogger.error("Lỗi khởi động đếm ngược: " + e.getMessage(), e);
            showError("Lỗi khởi động đếm ngược: " + e.getMessage());
        }
    }
    
    private void updateCountdownLabel() {
        try {
            int minutes = remainingSeconds.get() / 60;
            int seconds = remainingSeconds.get() % 60;
            countdownLabel.setText(String.format("Thời gian còn lại: %02d:%02d", minutes, seconds));
            
            if (remainingSeconds.get() < 120) {
                countdownLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            } else if (remainingSeconds.get() < 300) {
                countdownLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
            } else {
                countdownLabel.setStyle("-fx-text-fill: black;");
            }
        } catch (Exception e) {
            PaymentLogger.error("Lỗi cập nhật đếm ngược: " + e.getMessage(), e);
        }
    }
    
    private void startAutoCheck() {
        try {
            if (statusCheckExecutor != null && !statusCheckExecutor.isShutdown()) {
                statusCheckExecutor.shutdown();
            }
            
            statusCheckExecutor = Executors.newSingleThreadScheduledExecutor();
            statusCheckExecutor.scheduleAtFixedRate(() -> {
                try {
                    if (!paymentSuccess && !paymentCancelled && remainingSeconds.get() > 0) {
                        checkStatusAsync();
                    } else {
                        statusCheckExecutor.shutdown();
                    }
                } catch (Exception e) {
                    PaymentLogger.error("Lỗi trong kiểm tra tự động: " + e.getMessage(), e);
                }
            }, 10, 30, TimeUnit.SECONDS);
        } catch (Exception e) {
            PaymentLogger.error("Lỗi khởi động kiểm tra tự động: " + e.getMessage(), e);
        }
    }
    
    private void checkStatusAsync() {
        if (currentTransactionId == null) {
            PaymentLogger.warning("Không có mã giao dịch để kiểm tra trạng thái");
            return;
        }
        
        PaymentLogger.info("Kiểm tra trạng thái tự động cho giao dịch: " + currentTransactionId);
        qrPaymentService.checkPaymentStatusAsync(currentTransactionId)
                .thenAccept(this::handlePaymentStatusResult)
                .exceptionally(throwable -> {
                    PaymentLogger.error("Lỗi kiểm tra trạng thái tự động: " + throwable.getMessage(), throwable);
                    return null;
                });
    }
    
    private void handlePaymentStatusResult(PaymentStatusResult result) {
        Platform.runLater(() -> {
            try {
                if (result.isCompleted()) {
                    handlePaymentSuccess();
                } else if (result.isFailed()) {
                    statusLabel.setText("Thanh toán thất bại: " + result.getMessage());
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    paymentCancelled = true;
                    stopAllTasks();
                } else if (result.isError()) {
                    PaymentLogger.warning("Lỗi kiểm tra trạng thái: " + result.getMessage());
                }
            } catch (Exception e) {
                PaymentLogger.error("Lỗi xử lý trạng thái thanh toán: " + e.getMessage(), e);
            }
        });
    }
    
    private void checkPaymentStatusManually() {
        if (currentTransactionId == null || paymentSuccess || paymentCancelled) {
            PaymentLogger.warning("Không thể kiểm tra trạng thái thủ công: " + 
                (currentTransactionId == null ? "Không có mã giao dịch" : "Giao dịch đã kết thúc"));
            return;
        }
        
        statusLabel.setText("Đang kiểm tra trạng thái...");
        statusLabel.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        checkStatusButton.setDisable(true);
        
        PaymentLogger.info("Kiểm tra trạng thái thủ công cho giao dịch: " + currentTransactionId);
        CompletableFuture.supplyAsync(() -> qrPaymentService.checkPaymentStatus(currentTransactionId))
                .thenAccept(result -> Platform.runLater(() -> {
                    try {
                        progressIndicator.setProgress(0);
                        checkStatusButton.setDisable(false);
                        
                        if (result.isCompleted()) {
                            handlePaymentSuccess();
                        } else if (result.isPending()) {
                            statusLabel.setText("Thanh toán chưa được xác nhận. Thử lại sau.");
                            statusLabel.setStyle("-fx-text-fill: orange;");
                        } else if (result.isError()) {
                            showError("Lỗi kiểm tra trạng thái: " + result.getMessage());
                        } else if (result.isFailed()) {
                            statusLabel.setText("Thanh toán thất bại: " + result.getMessage());
                            statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            paymentCancelled = true;
                            stopAllTasks();
                        }
                    } catch (Exception e) {
                        PaymentLogger.error("Lỗi xử lý trạng thái thủ công: " + e.getMessage(), e);
                        showError("Lỗi xử lý trạng thái thủ công: " + e.getMessage());
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        progressIndicator.setProgress(0);
                        checkStatusButton.setDisable(false);
                        showError("Lỗi kiểm tra trạng thái: " + throwable.getMessage());
                    });
                    return null;
                });
    }
    
    private void handlePaymentSuccess() {
        if (paymentSuccess) return;
        
        paymentSuccess = true;
        statusLabel.setText("THANH TOÁN THÀNH CÔNG!");
        statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 16px;");
        progressIndicator.setProgress(1.0);
        
        checkStatusButton.setDisable(true);
        refreshQRButton.setDisable(true);
        cancelPaymentButton.setDisable(true);
        if (openPaymentUrlButton != null) {
            openPaymentUrlButton.setDisable(true);
        }
        
        stopAllTasks();
        showPaymentSuccessDialog();
        
        if (parentController != null) {
            parentController.loadInvoices();
        }
        
        PaymentLogger.info("Thanh toán thành công cho giao dịch: " + currentTransactionId);
    }
    
    private void showPaymentSuccessDialog() {
        try {
            String paymentInfo = String.format(
                "HÓA ĐƠN ĐÃ ĐƯỢC THANH TOÁN!\n\n" +
                "Hóa đơn #%d đã được thanh toán thành công!\n" +
                "Số tiền: %s VND\n" +
                "Phương thức: QR\n" +
                "Thời gian: %s\n" +
                "Mã giao dịch: %s\n" +
                "Mã đơn hàng: %s",
                invoice.getInvoiceId(),
                NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(invoice.getTotal()),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                currentTransactionId,
                currentOrderCode != null ? currentOrderCode : "N/A"
            );
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thanh toán thành công");
            alert.setHeaderText("Giao dịch hoàn tất");
            alert.setContentText(paymentInfo);
            alert.showAndWait();
        } catch (Exception e) {
            PaymentLogger.error("Lỗi hiển thị thông báo thành công: " + e.getMessage(), e);
        }
    }
    
    private void refreshQRCode() {
        if (paymentSuccess || paymentCancelled) {
            PaymentLogger.warning("Không thể làm mới QR: Giao dịch đã kết thúc");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận làm mới");
        confirmAlert.setHeaderText("Tạo thanh toán mới?");
        confirmAlert.setContentText("Thanh toán cũ sẽ bị hủy và tạo giao dịch mới.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                performRefreshQRCode();
            }
        });
    }
    
    private void performRefreshQRCode() {
        try {
            statusLabel.setText("Đang tạo thanh toán mới...");
            statusLabel.setStyle("-fx-text-fill: blue;");
            progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            refreshQRButton.setDisable(true);
            checkStatusButton.setDisable(true);
            cancelPaymentButton.setDisable(true);
            if (openPaymentUrlButton != null) {
                openPaymentUrlButton.setDisable(true);
            }
            
            stopAllTasks();
            
            CompletableFuture.supplyAsync(() -> qrPaymentService.refreshQRPayment(invoice))
                    .thenAccept(result -> Platform.runLater(() -> {
                        try {
                            if (result.isSuccess()) {
                                currentTransactionId = result.getTransactionId();
                                currentOrderCode = result.getOrderCode();
                                paymentUrl = result.getPaymentUrl();
                                
                                if (result.getQrImage() == null) {
                                    showError("Không thể tải hình ảnh QR mới");
                                    PaymentLogger.error("Hình ảnh QR mới trả về là null cho giao dịch " + currentTransactionId);
                                    return;
                                }
                                
                                qrImageView.setImage(result.getQrImage());
                                transferInstructionsArea.setText(result.getInstructions());
                                
                                statusLabel.setText("Thanh toán mới đã được tạo. Quét QR để thanh toán.");
                                statusLabel.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
                                progressIndicator.setProgress(0);
                                
                                refreshQRButton.setDisable(false);
                                checkStatusButton.setDisable(false);
                                cancelPaymentButton.setDisable(false);
                                if (openPaymentUrlButton != null) {
                                    openPaymentUrlButton.setDisable(false);
                                }
                                
                                paymentSuccess = false;
                                paymentCancelled = false;
                                startCountdown();
                                startAutoCheck();
                                
                                PaymentLogger.info("Làm mới QR thành công cho giao dịch: " + currentTransactionId);
                            } else {
                                showError("Lỗi tạo thanh toán mới: " + result.getErrorMessage());
                                refreshQRButton.setDisable(false);
                                checkStatusButton.setDisable(false);
                                cancelPaymentButton.setDisable(false);
                                if (openPaymentUrlButton != null) {
                                    openPaymentUrlButton.setDisable(false);
                                }
                            }
                        } catch (Exception e) {
                            PaymentLogger.error("Lỗi xử lý làm mới QR: " + e.getMessage(), e);
                            showError("Lỗi xử lý làm mới QR: " + e.getMessage());
                        }
                    }))
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> {
                            showError("Lỗi làm mới thanh toán: " + throwable.getMessage());
                            refreshQRButton.setDisable(false);
                            checkStatusButton.setDisable(false);
                            cancelPaymentButton.setDisable(false);
                            if (openPaymentUrlButton != null) {
                                openPaymentUrlButton.setDisable(false);
                            }
                        });
                        return null;
                    });
        } catch (Exception e) {
            PaymentLogger.error("Lỗi khởi động làm mới QR: " + e.getMessage(), e);
            showError("Lỗi khởi động làm mới QR: " + e.getMessage());
        }
    }
    
    private void cancelPayment() {
        if (paymentSuccess || paymentCancelled) {
            PaymentLogger.warning("Không thể hủy thanh toán: Giao dịch đã kết thúc");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận hủy");
        confirmAlert.setHeaderText("Hủy thanh toán?");
        confirmAlert.setContentText("Giao dịch sẽ bị hủy. Nếu đã chuyển tiền, liên hệ hỗ trợ.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                performCancelPayment();
            }
        });
    }
    
    private void performCancelPayment() {
        if (currentTransactionId == null) {
            showError("Không có mã giao dịch để hủy");
            PaymentLogger.warning("Không có mã giao dịch để hủy thanh toán");
            return;
        }
        
        statusLabel.setText("Đang hủy thanh toán...");
        statusLabel.setStyle("-fx-text-fill: red;");
        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        
        PaymentLogger.info("Hủy thanh toán cho giao dịch: " + currentTransactionId);
        CompletableFuture.supplyAsync(() -> qrPaymentService.cancelQRPayment(currentTransactionId, "Hủy bởi người dùng"))
                .thenAccept(success -> Platform.runLater(() -> {
                    try {
                        if (success) {
                            paymentCancelled = true;
                            statusLabel.setText("Thanh toán đã được hủy.");
                            statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            progressIndicator.setProgress(0);
                            
                            checkStatusButton.setDisable(true);
                            cancelPaymentButton.setDisable(true);
                            refreshQRButton.setDisable(false);
                            if (openPaymentUrlButton != null) {
                                openPaymentUrlButton.setDisable(true);
                            }
                            
                            stopAllTasks();
                            PaymentLogger.info("Hủy thanh toán thành công cho giao dịch: " + currentTransactionId);
                        } else {
                            showError("Lỗi hủy thanh toán");
                            PaymentLogger.error("Hủy thanh toán thất bại cho giao dịch: " + currentTransactionId);
                            progressIndicator.setProgress(0);
                        }
                    } catch (Exception e) {
                        PaymentLogger.error("Lỗi xử lý hủy thanh toán: " + e.getMessage(), e);
                        showError("Lỗi xử lý hủy thanh toán: " + e.getMessage());
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        showError("Lỗi hủy thanh toán: " + throwable.getMessage());
                        progressIndicator.setProgress(0);
                    });
                    return null;
                });
    }
    
    private void openPaymentUrl() {
        if (paymentUrl == null || paymentUrl.isEmpty()) {
            showError("Không có link thanh toán");
            PaymentLogger.warning("Không có link thanh toán để mở");
            return;
        }
        
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(paymentUrl));
            statusLabel.setText("Đã mở link thanh toán trong trình duyệt.");
            statusLabel.setStyle("-fx-text-fill: blue;");
            PaymentLogger.info("Mở link thanh toán thành công: " + paymentUrl);
        } catch (Exception e) {
            PaymentLogger.error("Lỗi mở link thanh toán: " + e.getMessage(), e);
            showError("Không thể mở link thanh toán: " + e.getMessage());
        }
    }
    
    private void handlePaymentTimeout() {
        if (paymentSuccess || paymentCancelled) return;
        
        PaymentLogger.info("Thanh toán hết hạn cho giao dịch: " + currentTransactionId);
        if (currentTransactionId != null) {
            qrPaymentService.cancelQRPayment(currentTransactionId, "Hết thời gian thanh toán");
        }
        
        statusLabel.setText("Thanh toán đã hết hạn. Tạo thanh toán mới.");
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        
        refreshQRButton.setDisable(false);
        checkStatusButton.setDisable(true);
        cancelPaymentButton.setDisable(true);
        if (openPaymentUrlButton != null) {
            openPaymentUrlButton.setDisable(true);
        }
        
        paymentCancelled = true;
        stopAllTasks();
    }
    
    private void stopAllTasks() {
        try {
            if (countdownExecutor != null && !countdownExecutor.isShutdown()) {
                countdownExecutor.shutdownNow();
                PaymentLogger.info("Dừng đếm ngược thành công");
            }
            
            if (statusCheckExecutor != null && !statusCheckExecutor.isShutdown()) {
                statusCheckExecutor.shutdownNow();
                PaymentLogger.info("Dừng kiểm tra tự động thành công");
            }
        } catch (Exception e) {
            PaymentLogger.error("Lỗi dừng tác vụ: " + e.getMessage(), e);
        }
    }
    
    private void showError(String message) {
        statusLabel.setText("Lỗi: " + message);
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        progressIndicator.setProgress(0);
        PaymentLogger.error("Lỗi thanh toán QR: " + message);
        
        checkStatusButton.setDisable(true);
        refreshQRButton.setDisable(false);
        cancelPaymentButton.setDisable(true);
        if (openPaymentUrlButton != null) {
            openPaymentUrlButton.setDisable(true);
        }

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi thanh toán");
        alert.setHeaderText("Có lỗi xảy ra");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void setParentController(InvoiceViewController parentController) {
        this.parentController = parentController;
        PaymentLogger.info("Đặt parentController cho QRPaymentViewController");
    }
    
    private void closeWindow() {
        try {
            stopAllTasks();
            
            if (currentTransactionId != null && !paymentSuccess && !paymentCancelled) {
                Alert warningAlert = new Alert(Alert.AlertType.WARNING);
                warningAlert.setTitle("Cảnh báo");
                warningAlert.setHeaderText("Đóng cửa sổ thanh toán?");
                warningAlert.setContentText("Giao dịch đang chờ sẽ bị hủy. Nếu đã chuyển tiền, liên hệ hỗ trợ.");
                
                warningAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        qrPaymentService.cancelQRPayment(currentTransactionId, "Cửa sổ đóng bởi người dùng");
                        actuallyCloseWindow();
                    }
                });
            } else {
                actuallyCloseWindow();
            }
        } catch (Exception e) {
            PaymentLogger.error("Lỗi đóng cửa sổ: " + e.getMessage(), e);
            actuallyCloseWindow();
        }
    }
    
    private void actuallyCloseWindow() {
        try {
            if (parentController != null) {
                parentController.loadInvoices();
            }
            
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
            PaymentLogger.info("Cửa sổ thanh toán QR đóng thành công");
        } catch (Exception e) {
            PaymentLogger.error("Lỗi đóng cửa sổ thực tế: " + e.getMessage(), e);
        }
    }
    
    private void debugPaymentStatus() {
        if (currentTransactionId == null) {
            showError("Không có mã giao dịch để kiểm tra");
            PaymentLogger.warning("Không có mã giao dịch để debug");
            return;
        }
        
        PaymentLogger.info("DEBUG: Kiểm tra trạng thái thanh toán");
        try {
            List<Invoice> invoices = invoiceRepository.selectByCondition("transaction_id = ?", currentTransactionId);
            Invoice invoice = invoices.isEmpty() ? null : invoices.get(0);
            
            if (invoice != null) {
                PaymentLogger.info("DEBUG Invoice: ID=" + invoice.getInvoiceId() + 
                                  ", Total=" + invoice.getTotal() + 
                                  ", Status=" + invoice.getStatus() + 
                                  ", ProviderTxnID=" + invoice.getProviderTransactionId());
                statusLabel.setText("DEBUG: Kiểm tra thông tin...");
                progressIndicator.setProgress(0);
            } else {
                PaymentLogger.error("DEBUG: Không tìm thấy hóa đơn cho giao dịch " + currentTransactionId);
                showError("DEBUG: Không tìm thấy hóa đơn");
            }
        } catch (Exception e) {
            PaymentLogger.error("DEBUG Lỗi: " + e.getMessage(), e);
            showError("DEBUG Lỗi: " + e.getMessage());
        }
    }
    
    public void cleanup() {
        try {
            stopAllTasks();
            if (currentTransactionId != null && !paymentSuccess && !paymentCancelled) {
                qrPaymentService.cancelQRPayment(currentTransactionId, "Dọn dẹp controller");
            }
            PaymentLogger.info("QRPaymentViewController dọn dẹp hoàn tất");
        } catch (Exception e) {
            PaymentLogger.error("Lỗi dọn dẹp controller: " + e.getMessage(), e);
        }
    }
}