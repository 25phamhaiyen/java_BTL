package controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import model.Booking;
import service.BookingService;

public class CreateInvoiceController implements Initializable {
    
    @FXML
    private Label bookingIdLabel;
    
    @FXML
    private Label customerNameLabel;
    
    @FXML
    private Label serviceNameLabel;
    
    @FXML
    private Label totalAmountLabel;
    
    @FXML
    private ComboBox<String> paymentMethodComboBox;
    
    @FXML
    private Button createInvoiceButton;
    
    private BookingService bookingService;
    private int bookingId;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bookingService = new BookingService();
        
        // Khởi tạo danh sách phương thức thanh toán
        paymentMethodComboBox.getItems().addAll("CASH", "CARD", "MOMO", "BANKING");
        paymentMethodComboBox.getSelectionModel().select(0);
    }
    
    /**
     * Khởi tạo dữ liệu cho controller
     * @param bookingId ID của booking cần tạo hóa đơn
     */
    public void initData(int bookingId) {
        this.bookingId = bookingId;
        loadBookingDetails();
    }
    
    /**
     * Tải thông tin chi tiết của booking
     */
    private void loadBookingDetails() {
        try {
            Booking booking = bookingService.getBookingById(bookingId);
            
            if (booking != null) {
                bookingIdLabel.setText(String.valueOf(booking.getBookingId()));
                customerNameLabel.setText(booking.getCustomer().getFullName());
                // serviceNameLabel.setText(booking.getService().getName());
                // totalAmountLabel.setText(String.format("%,.0f VND", booking.getService().getPrice()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleCreateInvoice() {
        try {
            String paymentMethod = paymentMethodComboBox.getValue();
            // Tạo hóa đơn
            // invoiceService.createInvoice(bookingId, paymentMethod);
            
            // Hiển thị thông báo thành công
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText("Đã tạo hóa đơn");
            alert.setContentText("Hóa đơn cho đặt lịch #" + bookingId + " đã được tạo thành công.");
            alert.showAndWait();
            
            // Quay lại màn hình danh sách booking
            javafx.stage.Stage stage = (javafx.stage.Stage) createInvoiceButton.getScene().getWindow();
            
            // Sửa: Sử dụng phương thức chung switchScene thay vì phương thức chuyên biệt
            SceneSwitcher.switchScene("staff/staffView.fxml");
            
            // Hoặc nếu cần dùng đúng tên phương thức, hãy đảm bảo tên đúng:
            // SceneSwitcher.switchToStaffScene(stage); 
        } catch (Exception e) {
            e.printStackTrace();
            
            // Hiển thị thông báo lỗi
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Không thể tạo hóa đơn");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}