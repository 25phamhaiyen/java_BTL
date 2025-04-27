package controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import model.Booking;
import service.BookingService;

public class BookingDetailController implements Initializable {
    
    @FXML
    private Label bookingIdLabel;
    
    @FXML
    private Label customerNameLabel;
    
    @FXML
    private Label petNameLabel;
    
    @FXML
    private Label serviceNameLabel;
    
    @FXML
    private Label bookingTimeLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private TextArea noteTextArea;
    
    private BookingService bookingService;
    private int bookingId;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bookingService = new BookingService();
    }
    
    /**
     * Khởi tạo dữ liệu cho controller
     * @param bookingId ID của booking cần hiển thị
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
                petNameLabel.setText(booking.getPet().getName());
                // serviceNameLabel.setText(booking.getService().getName());
                bookingTimeLabel.setText(booking.getBookingTime().toString());
                statusLabel.setText(booking.getStatus().name());
                noteTextArea.setText(booking.getNote());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}