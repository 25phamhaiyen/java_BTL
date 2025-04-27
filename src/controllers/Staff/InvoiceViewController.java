package controllers.Staff;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import service.InvoiceService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Invoice;
import utils.RoleChecker;
import controllers.SceneSwitcher;  // Đảm bảo import đúng
import utils.Session;

public class InvoiceViewController implements Initializable {

    @FXML
    private TableView<Invoice> invoiceTable;
    
    @FXML
    private TableColumn<Invoice, Integer> idColumn;
    
    @FXML
    private TableColumn<Invoice, Integer> orderIdColumn;
    
    @FXML
    private TableColumn<Invoice, LocalDateTime> dateColumn;
    
    @FXML
    private TableColumn<Invoice, Double> totalColumn;
    
    @FXML
    private TableColumn<Invoice, String> paymentMethodColumn;
    
    @FXML
    private TableColumn<Invoice, String> statusColumn;
    
    @FXML
    private Button viewDetailsButton;
    
    @FXML
    private Button reprintButton;
    
    @FXML
    private Button sendEmailButton;
    
    @FXML
    private DatePicker fromDatePicker;
    
    @FXML
    private DatePicker toDatePicker;
    
    @FXML
    private Button searchButton;
    
    private InvoiceService invoiceService;
    private ObservableList<Invoice> invoiceList;
    private Invoice selectedInvoice;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Khởi tạo service
        invoiceService = new InvoiceService();
        
        // Khởi tạo các cột cho bảng
        idColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Format ngày giờ và số tiền
        dateColumn.setCellFactory(column -> {
            return new javafx.scene.control.TableCell<Invoice, LocalDateTime>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(item));
                    }
                }
            };
        });
        
        totalColumn.setCellFactory(column -> {
            return new javafx.scene.control.TableCell<Invoice, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%,.0f VND", item));
                    }
                }
            };
        });
        
        // Thiết lập giá trị mặc định cho DatePicker
        LocalDateTime now = LocalDateTime.now();
        fromDatePicker.setValue(now.toLocalDate().minusDays(30));
        toDatePicker.setValue(now.toLocalDate());
        
        // Tải dữ liệu hóa đơn
        loadInvoices();
        
        // Xử lý sự kiện khi chọn một hóa đơn
        invoiceTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> handleInvoiceSelection(newValue));
        
        // Kiểm tra quyền và hiển thị/ẩn các nút tương ứng
        setupButtonVisibility();
    }
    
    /**
     * Thiết lập hiển thị/ẩn các nút dựa trên quyền của người dùng
     */
    private void setupButtonVisibility() {
        boolean canViewInvoice = RoleChecker.hasPermission("VIEW_INVOICE");
        boolean canPrintReceipt = RoleChecker.hasPermission("PRINT_RECEIPT");
        boolean canManagePayment = RoleChecker.hasPermission("MANAGE_PAYMENT");
        
        viewDetailsButton.setVisible(canViewInvoice);
        reprintButton.setVisible(canPrintReceipt);
        sendEmailButton.setVisible(canManagePayment || canViewInvoice);
    }
    
    /**
     * Tải danh sách hóa đơn trong khoảng thời gian
     */
    private void loadInvoices() {
        try {
            List<Invoice> invoices;
            
            if (fromDatePicker.getValue() != null && toDatePicker.getValue() != null) {
                invoices = invoiceService.getInvoicesByDateRange(
                    fromDatePicker.getValue().atStartOfDay(), 
                    toDatePicker.getValue().plusDays(1).atStartOfDay()
                );
            } else {
                // Mặc định lấy hóa đơn trong 30 ngày gần nhất
                invoices = invoiceService.getRecentInvoices(30);
            }
            
            invoiceList = FXCollections.observableArrayList(invoices);
            invoiceTable.setItems(invoiceList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách hóa đơn", e.getMessage());
        }
    }
    
    /**
     * Xử lý khi chọn một hóa đơn trong bảng
     */
    private void handleInvoiceSelection(Invoice invoice) {
        selectedInvoice = invoice;
        
        boolean hasSelection = (invoice != null);
        boolean isCompleted = hasSelection && "COMPLETED".equals(invoice.getStatus().name());
        
        // Cập nhật trạng thái của các nút
        viewDetailsButton.setDisable(!hasSelection);
        reprintButton.setDisable(!(hasSelection && isCompleted));
        sendEmailButton.setDisable(!hasSelection);
    }
    
    /**
     * Xem chi tiết hóa đơn được chọn
     */
    @FXML
    private void viewDetails(ActionEvent event) {
        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn hóa đơn", 
                    "Vui lòng chọn một hóa đơn để xem chi tiết.");
            return;
        }
        
        try {
            // Thử sử dụng switchScene với đường dẫn trực tiếp thay vì phương thức không tồn tại
            Stage currentStage = (Stage) viewDetailsButton.getScene().getWindow();
            // Tạm thời sử dụng switchScene với đường dẫn
            SceneSwitcher.switchScene("staff/invoiceDetail.fxml");
            
            // Nếu cần truyền invoiceId, có thể thêm vào sau thông qua cách khác
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở chi tiết hóa đơn", e.getMessage());
        }
    }
    
    /**
     * In lại hóa đơn đã chọn
     */
    @FXML
    private void reprintInvoice(ActionEvent event) {
        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn hóa đơn", 
                    "Vui lòng chọn một hóa đơn để in lại.");
            return;
        }
        
        if (!"COMPLETED".equals(selectedInvoice.getStatus().name())) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không thể in", 
                    "Chỉ có thể in lại các hóa đơn đã hoàn thành.");
            return;
        }
        
        try {
            invoiceService.printInvoice(selectedInvoice.getInvoiceId());
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã gửi lệnh in", 
                    "Hóa đơn #" + selectedInvoice.getInvoiceId() + " đã được gửi đến máy in.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể in hóa đơn", e.getMessage());
        }
    }
    
    /**
     * Gửi email hóa đơn cho khách hàng
     */
    @FXML
    private void sendEmail(ActionEvent event) {
        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn hóa đơn", 
                    "Vui lòng chọn một hóa đơn để gửi email.");
            return;
        }
        
        try {
            invoiceService.sendInvoiceByEmail(selectedInvoice.getInvoiceId());
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã gửi email", 
                    "Hóa đơn #" + selectedInvoice.getInvoiceId() + " đã được gửi đến email khách hàng.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể gửi email", e.getMessage());
        }
    }
    
    /**
     * Tìm kiếm hóa đơn theo khoảng thời gian
     */
    @FXML
    private void searchInvoices(ActionEvent event) {
        if (fromDatePicker.getValue() == null || toDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Thiếu thông tin", 
                    "Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc.");
            return;
        }
        
        if (fromDatePicker.getValue().isAfter(toDatePicker.getValue())) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Thời gian không hợp lệ", 
                    "Ngày bắt đầu phải trước hoặc cùng ngày kết thúc.");
            return;
        }
        
        loadInvoices();
    }
    
    /**
     * Reset lại bộ lọc và hiển thị tất cả hóa đơn gần đây
     */
    @FXML
    private void resetFilter(ActionEvent event) {
        LocalDateTime now = LocalDateTime.now();
        fromDatePicker.setValue(now.toLocalDate().minusDays(30));
        toDatePicker.setValue(now.toLocalDate());
        loadInvoices();
    }
    
    /**
     * Hiển thị thông báo
     */
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}