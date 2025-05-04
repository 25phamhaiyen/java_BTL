package controllers.Staff;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import controllers.SceneSwitcher;
import enums.StatusEnum;
import enums.PaymentMethodEnum;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Booking;
import model.BookingDetail;
import utils.DatabaseConnection;
import utils.Session;

/**
 * Controller cho màn hình tạo hóa đơn từ booking
 */
public class CreateInvoiceController implements Initializable {

    @FXML private Label invoiceNumberLabel;
    @FXML private Label dateLabel;
    @FXML private Label customerNameLabel;
    @FXML private Label phoneLabel;
    @FXML private Label petNameLabel;
    @FXML private TableView<ServiceRow> servicesTableView;
    @FXML private Label subtotalLabel;
    @FXML private TextField discountPercentField;
    @FXML private Label discountAmountLabel;
    @FXML private Label totalLabel;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private TextField amountPaidField;
    @FXML private Label changeLabel;
    @FXML private TextArea notesTextArea;
    @FXML private Button processButton;
    @FXML private Button cancelButton;
    
    private Booking booking;
    private ObservableList<ServiceRow> serviceRows = FXCollections.observableArrayList();
    private double subtotal = 0.0;
    private double discountAmount = 0.0;
    private double total = 0.0;

    /**
     * Khởi tạo dữ liệu từ booking
     * @param booking Booking cần tạo hóa đơn
     */
    public void initData(Booking booking) {
        this.booking = booking;
        
        if (booking != null) {
            // Hiển thị thông tin booking
            customerNameLabel.setText(booking.getCustomer() != null ? booking.getCustomer().getFullName() : "N/A");
            phoneLabel.setText(booking.getCustomer() != null ? booking.getCustomer().getPhone() : "N/A");
            petNameLabel.setText(booking.getPet() != null ? booking.getPet().getName() : "N/A");
            
            // Tải dịch vụ từ booking
            loadServicesFromBooking();
            
            // Cập nhật tổng tiền
            updateTotals();
        }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Khởi tạo ngày hiện tại
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        // Khởi tạo mã hóa đơn
        try {
            int nextId = getNextInvoiceId();
            invoiceNumberLabel.setText("HĐ" + String.format("%05d", nextId));
        } catch (Exception e) {
            invoiceNumberLabel.setText("HDXXXXX");
            e.printStackTrace();
        }
        
        // Khởi tạo bảng dịch vụ
        initializeServicesTable();
        
        // Khởi tạo combobox phương thức thanh toán
        ObservableList<String> paymentMethods = FXCollections.observableArrayList();
        for (PaymentMethodEnum method : PaymentMethodEnum.values()) {
            paymentMethods.add(method.name());
        }
        paymentMethodComboBox.setItems(paymentMethods);
        paymentMethodComboBox.setValue(PaymentMethodEnum.CASH.name());
        
        // Thiết lập listeners cho các trường nhập liệu
        setupListeners();
        
        // Thiết lập các nút
        processButton.setOnAction(event -> processPayment());
        cancelButton.setOnAction(event -> cancelInvoice());
    }
    
    /**
     * Khởi tạo bảng dịch vụ
     */
    private void initializeServicesTable() {
        if (servicesTableView.getColumns().size() >= 4) {
            TableColumn<ServiceRow, String> serviceNameCol = (TableColumn<ServiceRow, String>) servicesTableView.getColumns().get(0);
            TableColumn<ServiceRow, Integer> quantityCol = (TableColumn<ServiceRow, Integer>) servicesTableView.getColumns().get(1);
            TableColumn<ServiceRow, Double> priceCol = (TableColumn<ServiceRow, Double>) servicesTableView.getColumns().get(2);
            TableColumn<ServiceRow, Double> totalCol = (TableColumn<ServiceRow, Double>) servicesTableView.getColumns().get(3);
            
            serviceNameCol.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
            quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
            totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
            
            // Format giá tiền
            priceCol.setCellFactory(column -> new TableCell<ServiceRow, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%,.0f VND", item));
                    }
                }
            });
            
            totalCol.setCellFactory(column -> new TableCell<ServiceRow, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%,.0f VND", item));
                    }
                }
            });
            
            servicesTableView.setItems(serviceRows);
        }
    }
    
    /**
     * Thiết lập listeners cho các trường nhập liệu
     */
    private void setupListeners() {
        // Cập nhật giảm giá khi thay đổi % giảm giá
        discountPercentField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                discountPercentField.setText(oldValue);
            } else {
                updateDiscountAmount();
                updateTotals();
            }
        });
        
        // Cập nhật tiền thối lại khi thay đổi số tiền trả
        amountPaidField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                amountPaidField.setText(oldValue);
            } else {
                updateChange();
            }
        });
    }
    
    /**
     * Lấy ID hóa đơn tiếp theo
     * @return ID hóa đơn tiếp theo
     * @throws SQLException Nếu có lỗi khi truy vấn database
     */
    private int getNextInvoiceId() throws SQLException {
        String sql = "SELECT MAX(invoice_id) AS max_id FROM invoice";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("max_id") + 1;
            }
            return 1;
        }
    }
    
    /**
     * Tải dịch vụ từ booking
     */
    private void loadServicesFromBooking() {
        try {
            // Lấy danh sách dịch vụ từ booking_detail
            String sql = "SELECT bd.service_id, s.name, bd.quantity, bd.price " +
                         "FROM booking_detail bd " +
                         "JOIN service s ON bd.service_id = s.service_id " +
                         "WHERE bd.booking_id = ?";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, booking.getBookingId());
                try (ResultSet rs = stmt.executeQuery()) {
                    serviceRows.clear();
                    while (rs.next()) {
                        String serviceName = rs.getString("name");
                        int quantity = rs.getInt("quantity");
                        double price = rs.getDouble("price");
                        double total = quantity * price;
                        
                        serviceRows.add(new ServiceRow(serviceName, quantity, price, total));
                        subtotal += total;
                    }
                }
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải dịch vụ", 
                    "Đã xảy ra lỗi khi tải dịch vụ từ booking: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Cập nhật số tiền giảm giá
     */
    private void updateDiscountAmount() {
        try {
            double discountPercent = discountPercentField.getText().isEmpty() ? 
                    0.0 : Double.parseDouble(discountPercentField.getText());
            
            if (discountPercent < 0) discountPercent = 0;
            if (discountPercent > 100) discountPercent = 100;
            
            discountAmount = subtotal * discountPercent / 100.0;
            discountAmountLabel.setText(String.format("%,.0f VND", discountAmount));
        } catch (NumberFormatException e) {
            discountAmount = 0.0;
            discountAmountLabel.setText("0 VND");
        }
    }
    
    /**
     * Cập nhật tổng tiền
     */
    private void updateTotals() {
        subtotalLabel.setText(String.format("%,.0f VND", subtotal));
        total = subtotal - discountAmount;
        if (total < 0) total = 0;
        totalLabel.setText(String.format("%,.0f VND", total));
        
        // Cập nhật số tiền thối lại
        updateChange();
    }
    
    /**
     * Cập nhật số tiền thối lại
     */
    private void updateChange() {
        try {
            double amountPaid = amountPaidField.getText().isEmpty() ? 
                    0.0 : Double.parseDouble(amountPaidField.getText());
            double change = amountPaid - total;
            changeLabel.setText(String.format("%,.0f VND", Math.max(0, change)));
        } catch (NumberFormatException e) {
            changeLabel.setText("0 VND");
        }
    }
    
    /**
     * Xử lý thanh toán
     */
    private void processPayment() {
        try {
            if (serviceRows.isEmpty()) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Không có dịch vụ", 
                        "Không có dịch vụ nào để thanh toán");
                return;
            }
            
            double amountPaid;
            try {
                amountPaid = amountPaidField.getText().isEmpty() ? 
                        0.0 : Double.parseDouble(amountPaidField.getText());
            } catch (NumberFormatException e) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Số tiền không hợp lệ", 
                        "Vui lòng nhập số tiền hợp lệ");
                return;
            }
            
            if (amountPaid < total) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Thiếu tiền", 
                        "Số tiền trả không đủ để thanh toán");
                return;
            }
            
            // Tạo order mới
            int orderId = createOrder();
            if (orderId <= 0) {
                showAlert(AlertType.ERROR, "Lỗi", "Không thể tạo đơn hàng", 
                        "Đã xảy ra lỗi khi tạo đơn hàng");
                return;
            }
            
            // Tạo invoice
            boolean success = createInvoice(orderId, amountPaid);
            if (!success) {
                showAlert(AlertType.ERROR, "Lỗi", "Không thể tạo hóa đơn", 
                        "Đã xảy ra lỗi khi tạo hóa đơn");
                return;
            }
            
            // Cập nhật trạng thái booking
            booking.setStatus(StatusEnum.COMPLETED);
            updateBookingStatus();
            
            showAlert(AlertType.INFORMATION, "Thành công", "Thanh toán thành công", 
                    "Đã tạo hóa đơn thành công");
            
            // Đóng cửa sổ
            Stage stage = (Stage) processButton.getScene().getWindow();
            stage.close();
            
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể xử lý thanh toán", 
                    "Đã xảy ra lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Tạo đơn hàng mới
     * @return ID của đơn hàng mới
     * @throws SQLException Nếu có lỗi khi tạo đơn hàng
     */
    private int createOrder() throws SQLException {
        String sql = "INSERT INTO `order` (customer_id, staff_id, order_date, total_amount, status) " +
                     "VALUES (?, ?, NOW(), ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, booking.getCustomer().getId());
            stmt.setInt(2, Session.getCurrentStaff().getId());
            stmt.setDouble(3, total);
            stmt.setString(4, StatusEnum.COMPLETED.name());
            
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int orderId = rs.getInt(1);
                        
                        // Thêm chi tiết đơn hàng
                        addOrderDetails(orderId);
                        
                        return orderId;
                    }
                }
            }
        }
        
        return -1;
    }
    
    /**
     * Thêm chi tiết đơn hàng
     * @param orderId ID của đơn hàng
     * @throws SQLException Nếu có lỗi khi thêm chi tiết đơn hàng
     */
    private void addOrderDetails(int orderId) throws SQLException {
        String sql = "INSERT INTO order_detail (order_id, service_id, quantity, price) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (ServiceRow row : serviceRows) {
                // Lấy service_id từ tên dịch vụ
                int serviceId = getServiceIdByName(row.getServiceName());
                
                stmt.setInt(1, orderId);
                stmt.setInt(2, serviceId);
                stmt.setInt(3, row.getQuantity());
                stmt.setDouble(4, row.getPrice());
                stmt.addBatch();
            }
            
            stmt.executeBatch();
        }
    }
    
    /**
     * Lấy service_id từ tên dịch vụ
     * @param serviceName Tên dịch vụ
     * @return service_id
     * @throws SQLException Nếu có lỗi khi truy vấn
     */
    private int getServiceIdByName(String serviceName) throws SQLException {
        String sql = "SELECT service_id FROM service WHERE name = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, serviceName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("service_id");
                }
            }
        }
        
        throw new SQLException("Không tìm thấy dịch vụ: " + serviceName);
    }
    
    /**
     * Tạo hóa đơn
     * @param orderId ID của đơn hàng
     * @param amountPaid Số tiền khách trả
     * @return true nếu tạo thành công, false nếu thất bại
     */
    private boolean createInvoice(int orderId, double amountPaid) {
        String sql = "INSERT INTO invoice (order_id, payment_date, subtotal, discount_percent, " +
                     "discount_amount, total, amount_paid, payment_method, status, staff_id, note) " +
                     "VALUES (?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            double discountPercent = discountPercentField.getText().isEmpty() ? 
                    0.0 : Double.parseDouble(discountPercentField.getText());
            
            stmt.setInt(1, orderId);
            stmt.setDouble(2, subtotal);
            stmt.setDouble(3, discountPercent);
            stmt.setDouble(4, discountAmount);
            stmt.setDouble(5, total);
            stmt.setDouble(6, amountPaid);
            stmt.setString(7, paymentMethodComboBox.getValue());
            stmt.setString(8, StatusEnum.COMPLETED.name());
            stmt.setInt(9, Session.getCurrentStaff().getId());
            stmt.setString(10, notesTextArea.getText());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Cập nhật trạng thái booking
     */
    private void updateBookingStatus() {
        String sql = "UPDATE booking SET status = ? WHERE booking_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, StatusEnum.COMPLETED.name());
            stmt.setInt(2, booking.getBookingId());
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Hủy hóa đơn
     */
    private void cancelInvoice() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Hiển thị thông báo
     * @param alertType Loại thông báo
     * @param title Tiêu đề
     * @param header Tiêu đề phụ
     * @param content Nội dung
     */
    private void showAlert(AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    private void goBack(ActionEvent event) {
    	SceneSwitcher.switchScene("staff/staff_home.fxml");
    }
    /**
     * Lớp đại diện cho một dòng trong bảng dịch vụ
     */
    public static class ServiceRow {
        private final String serviceName;
        private final int quantity;
        private final double price;
        private final double total;
        
        public ServiceRow(String serviceName, int quantity, double price, double total) {
            this.serviceName = serviceName;
            this.quantity = quantity;
            this.price = price;
            this.total = total;
        }
        
        public String getServiceName() { return serviceName; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public double getTotal() { return total; }
    }
}