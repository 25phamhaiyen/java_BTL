package controllers.Staff;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import enums.StatusEnum;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Booking;
import model.Customer;
import model.Invoice;
import model.Pet;
import repository.BookingRepository;
import repository.CustomerRepository;
import repository.InvoiceRepository;
import utils.DatabaseConnection;
import java.util.List;
/**
 * Controller quản lý xem lịch sử của khách hàng
 */
public class CustomerHistoryController implements Initializable {

    @FXML
    private Label customerNameLabel;
    @FXML
    private Label customerPhoneLabel;
    @FXML
    private Label customerAddressLabel;
    @FXML
    private Label customerPointsLabel;
    @FXML
    private Label totalBookingsLabel;
    @FXML
    private Label totalInvoicesLabel;
    
    @FXML
    private TableView<Booking> bookingTableView;
    @FXML
    private TableColumn<Booking, Integer> bookingIdColumn;
    @FXML
    private TableColumn<Booking, LocalDateTime> bookingDateTimeColumn;
    @FXML
    private TableColumn<Booking, String> bookingServiceColumn;
    @FXML
    private TableColumn<Booking, String> bookingPetColumn;
    @FXML
    private TableColumn<Booking, String> bookingStatusColumn;
    
    @FXML
    private TableView<Invoice> invoiceTableView;
    @FXML
    private TableColumn<Invoice, Integer> invoiceIdColumn;
    @FXML
    private TableColumn<Invoice, String> invoiceDateColumn;
    @FXML
    private TableColumn<Invoice, String> invoiceServicesColumn;
    @FXML
    private TableColumn<Invoice, Double> invoiceTotalColumn;
    @FXML
    private TableColumn<Invoice, String> invoiceStatusColumn;
    
    @FXML
    private Button closeButton;
    
    private CustomerRepository customerRepository;
    private BookingRepository bookingRepository;
    private InvoiceRepository invoiceRepository;
    
    private Customer customer;
    private int customerId;
    
    private ObservableList<Booking> bookings = FXCollections.observableArrayList();
    private ObservableList<Invoice> invoices = FXCollections.observableArrayList();
    
    public CustomerHistoryController() {
        this.customerRepository = CustomerRepository.getInstance();
        this.bookingRepository = BookingRepository.getInstance();
        this.invoiceRepository = InvoiceRepository.getInstance();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Cấu hình cột cho bảng booking
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        bookingDateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("bookingTime"));
        bookingDateTimeColumn.setCellFactory(column -> {
            return new javafx.scene.control.TableCell<Booking, LocalDateTime>() {
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
        
        bookingServiceColumn.setCellValueFactory(data -> {
            return new SimpleStringProperty(getServiceNameFromBooking(data.getValue()));
        });
        
        bookingPetColumn.setCellValueFactory(data -> {
            Pet pet = data.getValue().getPet();
            return new SimpleStringProperty(pet != null ? pet.getName() : "");
        });
        
        bookingStatusColumn.setCellValueFactory(data -> {
            StatusEnum status = data.getValue().getStatus();
            String statusText = "";
            
            switch (status) {
                case PENDING:
                    statusText = "Chờ xác nhận";
                    break;
                case CONFIRMED:
                    statusText = "Đã xác nhận";
                    break;
                case COMPLETED:
                    statusText = "Hoàn thành";
                    break;
                case CANCELLED:
                    statusText = "Đã hủy";
                    break;
                default:
                    statusText = status.name();
                    break;
            }
            
            return new SimpleStringProperty(statusText);
        });
        
        // Cấu hình cột cho bảng invoice
        invoiceIdColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        
        invoiceDateColumn.setCellValueFactory(data -> {
            if (data.getValue().getPaymentDate() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(
                data.getValue().getPaymentDate().toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );
        });
        
        invoiceServicesColumn.setCellValueFactory(data -> {
            return new SimpleStringProperty(getServicesFromInvoice(data.getValue()));
        });
        
        invoiceTotalColumn.setCellValueFactory(data -> {
            if (data.getValue().getTotal() == null) return null;
            return javafx.beans.binding.Bindings.createObjectBinding(
                () -> data.getValue().getTotal().doubleValue()
            );
        });
        
        invoiceStatusColumn.setCellValueFactory(data -> {
            StatusEnum status = data.getValue().getStatus();
            String statusText = "";
            
            switch (status) {
                case PENDING:
                    statusText = "Chờ thanh toán";
                    break;
                case COMPLETED:
                    statusText = "Đã thanh toán";
                    break;
                case CANCELLED:
                    statusText = "Đã hủy";
                    break;
                default:
                    statusText = status.name();
                    break;
            }
            
            return new SimpleStringProperty(statusText);
        });
        
        // Đăng ký sự kiện cho nút đóng
        closeButton.setOnAction(e -> closeWindow());
    }
    
    /**
     * Lấy tên dịch vụ từ booking
     */
    private String getServiceNameFromBooking(Booking booking) {
        if (booking == null) {
            return "";
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT s.name FROM service s " + 
                         "JOIN booking_detail bd ON s.service_id = bd.service_id " + 
                         "WHERE bd.booking_id = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, booking.getBookingId());
            rs = stmt.executeQuery();

            StringBuilder serviceNames = new StringBuilder();
            while (rs.next()) {
                serviceNames.append(rs.getString("name")).append(", ");
            }

            if (serviceNames.length() > 0) {
                return serviceNames.substring(0, serviceNames.length() - 2);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tên dịch vụ: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) { /* Bỏ qua */ }
        }

        return "Không có thông tin dịch vụ";
    }
    
    /**
     * Lấy danh sách dịch vụ từ hóa đơn
     */
    private String getServicesFromInvoice(Invoice invoice) {
        if (invoice == null || invoice.getOrder() == null) {
            return "";
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT s.name FROM service s " + 
                         "JOIN order_detail od ON s.service_id = od.service_id " + 
                         "WHERE od.order_id = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, invoice.getOrder().getOrderId());
            rs = stmt.executeQuery();

            StringBuilder serviceNames = new StringBuilder();
            while (rs.next()) {
                serviceNames.append(rs.getString("name")).append(", ");
            }

            if (serviceNames.length() > 0) {
                return serviceNames.substring(0, serviceNames.length() - 2);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tên dịch vụ từ hóa đơn: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) { /* Bỏ qua */ }
        }

        return "Không có thông tin dịch vụ";
    }
    
    /**
     * Khởi tạo dữ liệu từ customerId
     */
    public void initData(int customerId) {
        this.customerId = customerId;
        
        // Tải thông tin khách hàng
        try {
            this.customer = customerRepository.selectById(customerId);
            if (this.customer != null) {
                customerNameLabel.setText(customer.getFullName());
                customerPhoneLabel.setText(customer.getPhone());
                customerAddressLabel.setText(customer.getAddress());
                customerPointsLabel.setText(String.valueOf(customer.getPoint()));
                
                // Tải lịch sử đặt lịch
                loadBookingHistory();
                
                // Tải lịch sử hóa đơn
                loadInvoiceHistory();
                
                // Cập nhật tổng số
                totalBookingsLabel.setText(String.valueOf(bookings.size()));
                totalInvoicesLabel.setText(String.valueOf(invoices.size()));
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải thông tin khách hàng: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Tải lịch sử đặt lịch
     */
    private void loadBookingHistory() {
        try {
            List<Booking> customerBookings = bookingRepository.selectByCondition("b.customer_id = ?", this.customerId);
            bookings.setAll(customerBookings);
            bookingTableView.setItems(bookings);
        } catch (Exception e) {
            System.err.println("Lỗi khi tải lịch sử đặt lịch: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Tải lịch sử hóa đơn
     */
    private void loadInvoiceHistory() {
        try {
            String condition = "o.customer_id = ?";
            List<Invoice> customerInvoices = invoiceRepository.selectByCondition(condition, this.customerId);
            invoices.setAll(customerInvoices);
            invoiceTableView.setItems(invoices);
        } catch (Exception e) {
            System.err.println("Lỗi khi tải lịch sử hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Đóng cửa sổ
     */
    @FXML
    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}