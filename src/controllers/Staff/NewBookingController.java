package controllers.Staff;

import enums.GenderEnum;
import enums.StatusEnum;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.*;
import repository.*;
import utils.DatabaseConnection;
import utils.Session;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class NewBookingController {

    @FXML private TextField phoneField;
    @FXML private TextField customerNameField;
    @FXML private TextField petNameField;
    @FXML private ComboBox<Service> serviceComboBox;
    @FXML private DatePicker bookingDatePicker;
    @FXML private TextField bookingTimeField;
    @FXML private TextArea notesAreaNewBooking;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private CustomerRepository customerRepository;
    private ServiceRepository serviceRepository;
    private BookingRepository bookingRepository;
    private BookingDetailRepository bookingDetailRepository;
    private PetRepository petRepository;
    private PetTypeRepository petTypeRepository;
    private OrderRepository orderRepository;
    private OrderDetailRepository orderDetailRepository;
    private InvoiceRepository invoiceRepository;
    private Customer selectedCustomer;

    private InvoiceViewController invoiceViewController;

    public NewBookingController() {
        try {
            this.customerRepository = CustomerRepository.getInstance();
            this.serviceRepository = ServiceRepository.getInstance();
            this.bookingRepository = BookingRepository.getInstance();
            this.bookingDetailRepository = new BookingDetailRepository();
            this.petRepository = PetRepository.getInstance();
            this.petTypeRepository = PetTypeRepository.getInstance();
            this.orderRepository = OrderRepository.getInstance();
            this.orderDetailRepository = OrderDetailRepository.getInstance();
            this.invoiceRepository = InvoiceRepository.getInstance();
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo NewBookingController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setInvoiceViewController(InvoiceViewController controller) {
        this.invoiceViewController = controller;
    }

    @FXML
    public void initialize() {
        loadServices();

        if (serviceComboBox != null) {
            serviceComboBox.setConverter(new StringConverter<Service>() {
                @Override
                public String toString(Service service) {
                    return service != null ? service.getName() : "";
                }

                @Override
                public Service fromString(String string) {
                    return serviceComboBox.getItems().stream()
                            .filter(service -> service.getName().equals(string))
                            .findFirst()
                            .orElse(null);
                }
            });
        }

        if (bookingDatePicker != null) {
            bookingDatePicker.setValue(LocalDate.now());
        }

        if (bookingTimeField != null) {
            bookingTimeField.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        if (phoneField != null) {
            phoneField.textProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null && newValue.length() >= 10) {
                    searchCustomer(newValue);
                } else {
                    selectedCustomer = null;
                    if (customerNameField != null) customerNameField.clear();
                    if (petNameField != null) petNameField.clear();
                }
            });
        }

        Integer selectedCustomerId = (Integer) Session.getInstance().getAttribute("selectedCustomerId");
        if (selectedCustomerId != null) {
            try {
                Customer customer = customerRepository.selectById(selectedCustomerId);
                if (customer != null) {
                    selectedCustomer = customer;
                    if (phoneField != null) phoneField.setText(customer.getPhone());
                    if (customerNameField != null) customerNameField.setText(customer.getFullName());
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi lấy thông tin khách hàng từ Session: " + e.getMessage());
            }
        }

        System.out.println("NewBookingController khởi tạo thành công");
    }

    private void loadServices() {
        try {
            List<Service> services = serviceRepository.selectAll();
            serviceComboBox.getItems().clear();
            serviceComboBox.getItems().addAll(services);
            if (!services.isEmpty()) {
                serviceComboBox.setValue(services.get(0));
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách dịch vụ", "Lỗi: " + e.getMessage());
        }
    }

    private void searchCustomer(String phone) {
        try {
            Customer customer = customerRepository.findByPhone(phone);
            if (customer != null) {
                selectedCustomer = customer;
                customerNameField.setText(customer.getFullName());
                petNameField.clear();
                petNameField.setPromptText("Nhập tên thú cưng");
            } else {
                selectedCustomer = null;
                customerNameField.setText("");
                petNameField.clear();
                petNameField.setPromptText("Vui lòng tạo khách hàng mới trước");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tìm kiếm khách hàng", "Lỗi: " + e.getMessage());
        }
    }

    private boolean checkBookingConflict(LocalDateTime bookingTime) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT COUNT(*) FROM booking WHERE booking_time = ?")) {
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(bookingTime));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("Số lượng lịch hẹn trùng thời gian " + bookingTime + ": " + count);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể kiểm tra xung đột lịch hẹn", "Lỗi SQL: " + e.getMessage());
        }
        return false;
    }

    @FXML
    private void saveBooking() {
        try {
            Staff currentStaff = Session.getCurrentStaff();
            if (currentStaff == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Phiên đăng nhập không hợp lệ", "Không tìm thấy thông tin nhân viên. Vui lòng đăng nhập lại.");
                closeNewBookingWindow();
                return;
            }

            String customerName = customerNameField.getText().trim();
            if (customerName.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Thiếu thông tin khách hàng", "Vui lòng nhập tên khách hàng.");
                return;
            }

            String phoneNumber = phoneField.getText().trim();
            if (phoneNumber.isEmpty() || phoneNumber.replaceAll("\\D", "").length() < 10) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số điện thoại không hợp lệ", "Vui lòng nhập số điện thoại hợp lệ (tối thiểu 10 chữ số).");
                return;
            }

            String petName = petNameField.getText().trim();
            if (petName.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Thiếu thông tin thú cưng", "Vui lòng nhập tên thú cưng.");
                return;
            }

            Service selectedService = serviceComboBox.getValue();
            if (selectedService == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Thiếu thông tin dịch vụ", "Vui lòng chọn một dịch vụ.");
                return;
            }

            LocalDate bookingDate = bookingDatePicker.getValue();
            if (bookingDate == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Thiếu thông tin ngày hẹn", "Vui lòng chọn ngày đặt lịch.");
                return;
            }

            String timeStr = bookingTimeField.getText().trim();
            if (timeStr.isEmpty() || !timeStr.matches("\\d{2}:\\d{2}")) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Định dạng giờ không hợp lệ", "Vui lòng nhập giờ theo định dạng HH:mm (ví dụ: 14:30).");
                return;
            }

            LocalDateTime bookingTime;
            try {
                LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
                bookingTime = LocalDateTime.of(bookingDate, time);
            } catch (DateTimeParseException e) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Định dạng giờ không hợp lệ", "Vui lòng nhập giờ theo định dạng HH:mm (ví dụ: 14:30).");
                return;
            }

            if (bookingTime.isBefore(LocalDateTime.now())) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Thời gian không hợp lệ", "Thời gian đặt lịch phải ở tương lai.");
                return;
            }

            if (checkBookingConflict(bookingTime)) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Xung đột lịch hẹn", "Thời gian này đã có lịch hẹn khác. Vui lòng chọn thời gian khác.");
                return;
            }

            if (selectedCustomer == null) {
                selectedCustomer = new Customer();
                selectedCustomer.setFullName(customerName);
                selectedCustomer.setPhone(phoneNumber);
                selectedCustomer.setPoint(0);
                selectedCustomer.setGender(GenderEnum.OTHER);
                selectedCustomer.setAddress("");
                selectedCustomer.setEmail("");

                try {
                    int result = customerRepository.insert(selectedCustomer);
                    if (result <= 0) {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo khách hàng", "Lỗi khi lưu thông tin khách hàng.");
                        return;
                    }
                    System.out.println("Đã tạo khách hàng mới với ID: " + selectedCustomer.getId());
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo khách hàng", "Lỗi: " + e.getMessage());
                    return;
                }
            }

            Pet selectedPet;
            try {
                PetType defaultType = petTypeRepository.selectAll().stream().findFirst().orElse(null);
                if (defaultType == null) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy loại thú cưng", "Vui lòng cấu hình loại thú cưng trước.");
                    return;
                }

                selectedPet = new Pet();
                selectedPet.setPetId(0);
                selectedPet.setName(petName);
                selectedPet.setGender(GenderEnum.MALE);
                selectedPet.setDob(LocalDate.now().minusYears(1));
                selectedPet.setWeight(0.0);
                selectedPet.setTypePet(defaultType);
                selectedPet.setOwner(selectedCustomer);

                try (Connection connection = DatabaseConnection.getConnection();
                     PreparedStatement stmt = connection.prepareStatement(
                             "INSERT INTO pet (name, pet_gender, dob, customer_id, type_id, weight) VALUES (?, ?, ?, ?, ?, ?)",
                             Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, petName);
                    stmt.setString(2, selectedPet.getGender().name());
                    stmt.setDate(3, java.sql.Date.valueOf(selectedPet.getDob()));
                    stmt.setInt(4, selectedCustomer.getId());
                    stmt.setInt(5, selectedPet.getTypePet().getTypePetID());
                    stmt.setDouble(6, selectedPet.getWeight());

                    int result = stmt.executeUpdate();
                    if (result > 0) {
                        try (ResultSet rs = stmt.getGeneratedKeys()) {
                            if (rs.next()) {
                                selectedPet.setPetId(rs.getInt(1));
                                System.out.println("Tạo thú cưng mới với ID: " + selectedPet.getPetId() + ", Tên: " + petName);
                            }
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo thú cưng", "Lỗi khi lưu thông tin thú cưng.");
                        return;
                    }
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo thú cưng", "Lỗi SQL: " + e.getMessage());
                    return;
                }

                selectedPet = petRepository.selectById(selectedPet.getPetId());
                if (selectedPet == null) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lấy thông tin thú cưng", "Lỗi khi lấy thông tin thú cưng.");
                    return;
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo thú cưng", "Lỗi: " + e.getMessage());
                return;
            }

            Booking booking = new Booking();
            booking.setCustomer(selectedCustomer);
            booking.setStaff(currentStaff);
            booking.setBookingTime(bookingTime);
            booking.setStatus(StatusEnum.PENDING);
            booking.setNote(notesAreaNewBooking.getText().trim());
            booking.setPet(selectedPet);

            int bookingId;
            int orderId;
            try (Connection connection = DatabaseConnection.getConnection()) {
                connection.setAutoCommit(false);
                try {
                    // Lưu booking
                    String insertBookingSql = "INSERT INTO booking (customer_id, staff_id, pet_id, booking_time, status, note) " +
                                             "VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = connection.prepareStatement(insertBookingSql, Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setInt(1, selectedCustomer.getId());
                        stmt.setInt(2, currentStaff.getId());
                        stmt.setInt(3, selectedPet.getPetId());
                        stmt.setTimestamp(4, java.sql.Timestamp.valueOf(bookingTime));
                        stmt.setString(5, StatusEnum.PENDING.name());
                        stmt.setString(6, booking.getNote());

                        int result = stmt.executeUpdate();
                        if (result > 0) {
                            try (ResultSet rs = stmt.getGeneratedKeys()) {
                                if (rs.next()) {
                                    bookingId = rs.getInt(1);
                                    System.out.println("Đã tạo lịch hẹn với ID: " + bookingId);
                                } else {
                                    throw new SQLException("Không thể lấy bookingId sau khi thêm lịch hẹn.");
                                }
                            }
                        } else {
                            throw new SQLException("Không thể tạo lịch hẹn.");
                        }
                    }

                    // Lưu chi tiết lịch hẹn
                    String insertDetailSql = "INSERT INTO booking_detail (booking_id, service_id, quantity, price) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement stmt = connection.prepareStatement(insertDetailSql)) {
                        stmt.setInt(1, bookingId);
                        stmt.setInt(2, selectedService.getServiceId());
                        stmt.setInt(3, 1);
                        stmt.setDouble(4, selectedService.getPrice());

                        int result = stmt.executeUpdate();
                        if (result > 0) {
                            System.out.println("Đã lưu chi tiết lịch hẹn: booking_id=" + bookingId + ", service_id=" + selectedService.getServiceId());
                        } else {
                            throw new SQLException("Không thể lưu chi tiết lịch hẹn.");
                        }
                    }

                    // Tạo Order dựa trên Booking (bỏ cột booking_id)
                    String insertOrderSql = "INSERT INTO `order` (customer_id, staff_id, order_date, total_amount, status) " +
                                           "VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = connection.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setInt(1, selectedCustomer.getId());
                        stmt.setInt(2, currentStaff.getId());
                        stmt.setTimestamp(3, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                        stmt.setDouble(4, selectedService.getPrice());
                        stmt.setString(5, StatusEnum.PENDING.name());

                        int result = stmt.executeUpdate();
                        if (result > 0) {
                            try (ResultSet rs = stmt.getGeneratedKeys()) {
                                if (rs.next()) {
                                    orderId = rs.getInt(1);
                                    System.out.println("Đã tạo Order với ID: " + orderId);
                                } else {
                                    throw new SQLException("Không thể lấy orderId sau khi thêm Order.");
                                }
                            }
                        } else {
                            throw new SQLException("Không thể tạo Order.");
                        }
                    }

                    // Tạo OrderDetail
                    String insertOrderDetailSql = "INSERT INTO order_detail (order_id, service_id, quantity, price) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement stmt = connection.prepareStatement(insertOrderDetailSql)) {
                        stmt.setInt(1, orderId);
                        stmt.setInt(2, selectedService.getServiceId());
                        stmt.setInt(3, 1);
                        stmt.setDouble(4, selectedService.getPrice());

                        int result = stmt.executeUpdate();
                        if (result > 0) {
                            System.out.println("Đã lưu chi tiết Order: order_id=" + orderId + ", service_id=" + selectedService.getServiceId());
                        } else {
                            throw new SQLException("Không thể lưu chi tiết Order.");
                        }
                    }

                    // Tạo Invoice
                    String insertInvoiceSql = "INSERT INTO invoice (order_id, staff_id, subtotal, total, payment_date, status) " +
                                             "VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = connection.prepareStatement(insertInvoiceSql)) {
                        stmt.setInt(1, orderId);
                        stmt.setInt(2, currentStaff.getId());
                        stmt.setBigDecimal(3, BigDecimal.valueOf(selectedService.getPrice()));
                        stmt.setBigDecimal(4, BigDecimal.valueOf(selectedService.getPrice()));
                        stmt.setTimestamp(5, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                        stmt.setString(6, StatusEnum.PENDING.name());

                        int result = stmt.executeUpdate();
                        if (result > 0) {
                            System.out.println("Đã tạo Invoice cho Order ID: " + orderId);
                        } else {
                            throw new SQLException("Không thể tạo Invoice.");
                        }
                    }

                    connection.commit();
                    System.out.println("Giao dịch lưu lịch hẹn, order và invoice thành công.");

                    // Cập nhật ngày của fromDatePicker và làm mới danh sách hóa đơn
                    if (invoiceViewController != null) {
                        Platform.runLater(() -> {
                            invoiceViewController.setFromDatePickerToToday();
                            invoiceViewController.loadInvoices();
                        });
                    }

                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Tạo lịch hẹn thành công",
                            "Lịch hẹn đã được tạo với ID: " + bookingId + "\nThú cưng: " + petName + "\nDịch vụ: " + selectedService.getName());

                } catch (SQLException e) {
                    connection.rollback();
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo lịch hẹn",
                            "Lỗi SQL khi lưu lịch hẹn, order hoặc invoice: " + e.getMessage());
                    System.err.println("Lỗi SQL: " + e.getMessage());
                    return;
                } finally {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể kết nối cơ sở dữ liệu",
                        "Lỗi SQL: " + e.getMessage());
                return;
            }

            closeNewBookingWindow();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi không xác định", "Đã xảy ra lỗi không xác định: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int getLastInsertedBookingId(Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT LAST_INSERT_ID() as last_id");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int id = rs.getInt("last_id");
                System.out.println("ID lịch hẹn vừa chèn: " + id);
                return id;
            }
        }
        return 0;
    }

    @FXML
    private void cancelNewBooking() {
        closeNewBookingWindow();
    }

    private void closeNewBookingWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}