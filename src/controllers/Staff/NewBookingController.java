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
import service.BookingService;
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
import java.util.Optional;
import java.util.stream.Collectors;

public class NewBookingController {

    @FXML private TextField phoneField;
    @FXML private TextField customerNameField;
    @FXML private TextField addressField;
    @FXML private TextField emailField;
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
    private BookingService bookingService;

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
            this.bookingService = new BookingService();
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
                    if (addressField != null) addressField.clear();
                    if (emailField != null) emailField.clear();
                    if (petNameField != null) petNameField.clear();
                }
            });
        }

        if (addressField != null) addressField.setPromptText("Nhập địa chỉ khách hàng (tùy chọn)");
        if (emailField != null) emailField.setPromptText("Nhập email khách hàng (tùy chọn)");

        Integer selectedCustomerId = (Integer) Session.getInstance().getAttribute("selectedCustomerId");
        if (selectedCustomerId != null) {
            try {
                Customer customer = customerRepository.selectById(selectedCustomerId);
                if (customer != null) {
                    selectedCustomer = customer;
                    if (phoneField != null) phoneField.setText(customer.getPhone());
                    if (customerNameField != null) customerNameField.setText(customer.getFullName());
                    if (addressField != null) addressField.setText(customer.getAddress());
                    if (emailField != null) emailField.setText(customer.getEmail());
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi lấy thông tin khách hàng từ Session: " + e.getMessage());
            }
        }

        System.out.println("NewBookingController khởi tạo thành công");
    }

    private void loadServices() {
        try {
            List<Service> services = serviceRepository.selectAll()
                    .stream()
                    .filter(Service::isActive)
                    .collect(Collectors.toList());
            serviceComboBox.getItems().clear();
            serviceComboBox.getItems().addAll(services);
            if (!services.isEmpty()) {
                serviceComboBox.setValue(services.get(0));
            } else {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có dịch vụ khả dụng",
                        "Hiện tại không có dịch vụ nào đang hoạt động.");
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
                addressField.setText(customer.getAddress());
                emailField.setText(customer.getEmail());
                petNameField.clear();
                petNameField.setPromptText("Nhập tên thú cưng");
            } else {
                selectedCustomer = null;
                customerNameField.setText("");
                addressField.setText("");
                emailField.setText("");
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
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Phiên đăng nhập không hợp lệ",
                        "Không tìm thấy thông tin nhân viên. Vui lòng đăng nhập lại.");
                closeNewBookingWindow();
                return;
            }

            // Validation số điện thoại
            String phoneNumber = phoneField.getText().trim();
            if (phoneNumber.isEmpty() || !phoneNumber.matches("^0\\d{9}$")) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số điện thoại không hợp lệ",
                        "Số điện thoại phải bắt đầu bằng 0 và có đúng 10 chữ số.");
                return;
            }

            // Validation tên khách hàng
            String customerName = customerNameField.getText().trim();
            if (customerName.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Thiếu thông tin khách hàng",
                        "Vui lòng nhập tên khách hàng.");
                return;
            }
            if (customerName.length() > 100) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Tên khách hàng quá dài",
                        "Tên khách hàng không được quá 100 ký tự.");
                return;
            }
            if (!customerName.matches("^[a-zA-ZÀ-ỹ\\s]+$")) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Tên khách hàng không hợp lệ",
                        "Tên khách hàng chỉ được chứa chữ cái và khoảng trắng.");
                return;
            }

            // Validation địa chỉ
            String address = addressField.getText().trim();
            if (!address.isEmpty()) {
                if (address.length() > 200) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Địa chỉ quá dài",
                            "Địa chỉ không được quá 200 ký tự.");
                    return;
                }
                if (!address.matches("^[a-zA-ZÀ-ỹ0-9\\s,.-]+$")) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Địa chỉ không hợp lệ",
                            "Địa chỉ chỉ được chứa chữ cái, số, khoảng trắng, dấu phẩy, dấu chấm, dấu gạch ngang.");
                    return;
                }
            }

            // Validation email
            String email = emailField.getText().trim();
            if (!email.isEmpty()) {
                if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Email không hợp lệ",
                            "Vui lòng nhập email đúng định dạng (ví dụ: example@domain.com).");
                    return;
                }
            }

            // Validation tên thú cưng
            String petName = petNameField.getText().trim();
            if (petName.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Thiếu thông tin thú cưng",
                        "Vui lòng nhập tên thú cưng.");
                return;
            }
            if (petName.length() > 50) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Tên thú cưng quá dài",
                        "Tên thú cưng không được quá 50 ký tự.");
                return;
            }
            if (!petName.matches("^[a-zA-ZÀ-ỹ0-9\\s]+$")) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Tên thú cưng không hợp lệ",
                        "Tên thú cưng chỉ được chứa chữ cái, số và khoảng trắng.");
                return;
            }

            // Validation dịch vụ
            Service selectedService = serviceComboBox.getValue();
            if (selectedService == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Thiếu thông tin dịch vụ",
                        "Vui lòng chọn một dịch vụ.");
                return;
            }
            if (!selectedService.isActive()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Dịch vụ không khả dụng",
                        "Dịch vụ " + selectedService.getName() + " đã ngừng hoạt động. Vui lòng chọn dịch vụ khác.");
                return;
            }

            // Validation ngày hẹn
            LocalDate bookingDate = bookingDatePicker.getValue();
            if (bookingDate == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Thiếu thông tin ngày hẹn",
                        "Vui lòng chọn ngày đặt lịch.");
                return;
            }
            if (bookingDate.isBefore(LocalDate.now())) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Ngày hẹn không hợp lệ",
                        "Ngày hẹn không thể là ngày quá khứ.");
                return;
            }
            if (bookingDate.isAfter(LocalDate.now().plusYears(1))) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Ngày hẹn quá xa",
                        "Ngày hẹn không thể quá xa trong tương lai (tối đa 1 năm).");
                return;
            }

            // Validation giờ hẹn
            String timeStr = bookingTimeField.getText().trim();
            if (timeStr.isEmpty() || !timeStr.matches("\\d{2}:\\d{2}")) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Định dạng giờ không hợp lệ",
                        "Vui lòng nhập giờ theo định dạng HH:mm (ví dụ: 14:30).");
                return;
            }

            LocalDateTime bookingTime;
            LocalTime time;
            try {
                time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
                bookingTime = LocalDateTime.of(bookingDate, time);
            } catch (DateTimeParseException e) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Định dạng giờ không hợp lệ",
                        "Vui lòng nhập giờ theo định dạng HH:mm (ví dụ: 14:30).");
                return;
            }

            if (time.isBefore(LocalTime.of(8, 0)) || time.isAfter(LocalTime.of(22, 0))) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Giờ hẹn không hợp lệ",
                        "Giờ hẹn phải từ 8:00 đến 22:00.");
                return;
            }

            if (bookingTime.isBefore(LocalDateTime.now())) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Thời gian không hợp lệ",
                        "Thời gian đặt lịch phải là thời gian trong tương lai.");
                return;
            }

            if (checkBookingConflict(bookingTime)) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Xung đột lịch hẹn",
                        "Thời gian này đã có lịch hẹn khác. Vui lòng chọn thời gian khác.");
                return;
            }

            // Validation ghi chú
            String notes = notesAreaNewBooking.getText().trim();
            if (notes.length() > 500) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Ghi chú quá dài",
                        "Ghi chú không được quá 500 ký tự.");
                return;
            }

            // Tạo hoặc cập nhật khách hàng
            if (selectedCustomer == null) {
                selectedCustomer = new Customer();
                selectedCustomer.setFullName(customerName);
                selectedCustomer.setPhone(phoneNumber);
                selectedCustomer.setAddress(address);
                selectedCustomer.setEmail(email);
                selectedCustomer.setPoint(0);
                selectedCustomer.setGender(GenderEnum.OTHER);

                try {
                    int result = customerRepository.insert(selectedCustomer);
                    if (result <= 0) {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo khách hàng",
                                "Lỗi khi lưu thông tin khách hàng.");
                        return;
                    }
                    System.out.println("Đã tạo khách hàng mới với ID: " + selectedCustomer.getId());
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo khách hàng", "Lỗi: " + e.getMessage());
                    return;
                }
            } else {
                selectedCustomer.setFullName(customerName);
                selectedCustomer.setAddress(address);
                selectedCustomer.setEmail(email);
                customerRepository.update(selectedCustomer);
            }

            // Tạo thú cưng
            Pet selectedPet;
            try {
                PetType defaultType = petTypeRepository.selectAll().stream().findFirst().orElse(null);
                if (defaultType == null) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy loại thú cưng",
                            "Vui lòng cấu hình loại thú cưng trước.");
                    return;
                }

                // Kiểm tra xem thú cưng đã tồn tại chưa
                List<Pet> existingPets = petRepository.selectByCondition("customer_id = ? AND name = ?", 
                        selectedCustomer.getId(), petName);
                if (!existingPets.isEmpty()) {
                    selectedPet = existingPets.get(0);
                } else {
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
                            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo thú cưng",
                                    "Lỗi khi lưu thông tin thú cưng.");
                            return;
                        }
                    } catch (SQLException e) {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo thú cưng", "Lỗi SQL: " + e.getMessage());
                        return;
                    }

                    selectedPet = petRepository.selectById(selectedPet.getPetId());
                    if (selectedPet == null) {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lấy thông tin thú cưng",
                                "Lỗi khi lấy thông tin thú cưng.");
                        return;
                    }
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo thú cưng", "Lỗi: " + e.getMessage());
                return;
            }

            // Tạo booking và các liên quan
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
                        stmt.setString(6, notes);

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
                        if (result <= 0) {
                            throw new SQLException("Không thể lưu chi tiết lịch hẹn.");
                        }
                    }

                    // Tạo Order
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
                        if (!selectedService.isActive()) {
                            throw new SQLException("Dịch vụ " + selectedService.getName() + " đã ngừng hoạt động!");
                        }
                        stmt.setInt(1, orderId);
                        stmt.setInt(2, selectedService.getServiceId());
                        stmt.setInt(3, 1);
                        stmt.setDouble(4, selectedService.getPrice());

                        int result = stmt.executeUpdate();
                        if (result <= 0) {
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
                        if (result <= 0) {
                            throw new SQLException("Không thể tạo Invoice.");
                        }
                    }

                    connection.commit();
                    System.out.println("Giao dịch lưu lịch hẹn, order và invoice thành công.");

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

    @FXML
    private void handleSubmit() {
        try {
            String phone = phoneField.getText().trim();
            String customerName = customerNameField.getText().trim();
            String address = addressField.getText().trim();
            String email = emailField.getText().trim();
            String petName = petNameField.getText().trim();
            LocalDate bookingDate = bookingDatePicker.getValue();

            // Validation số điện thoại
            if (phone.isEmpty() || !phone.matches("^0\\d{9}$")) {
                showAlert(Alert.AlertType.WARNING, "Thông báo", "Số điện thoại không hợp lệ",
                        "Số điện thoại phải bắt đầu bằng 0 và có đúng 10 chữ số.");
                return;
            }

            // Validation tên khách hàng
            if (customerName.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Thông báo", "Thiếu thông tin",
                        "Vui lòng nhập tên khách hàng.");
                return;
            }
            if (customerName.length() > 100) {
                showAlert(Alert.AlertType.WARNING, "Thông báo", "Tên khách hàng quá dài",
                        "Tên khách hàng không được quá 100 ký tự.");
                return;
            }
            if (!customerName.matches("^[a-zA-ZÀ-ỹ\\s]+$")) {
                showAlert(Alert.AlertType.WARNING, "Thông báo", "Tên khách hàng không hợp lệ",
                        "Tên khách hàng chỉ được chứa chữ cái và khoảng trắng.");
                return;
            }

            // Validation địa chỉ
            if (!address.isEmpty()) {
                if (address.length() > 200) {
                    showAlert(Alert.AlertType.WARNING, "Thông báo", "Địa chỉ quá dài",
                            "Địa chỉ không được quá 200 ký tự.");
                    return;
                }
                if (!address.matches("^[a-zA-ZÀ-ỹ0-9\\s,.-]+$")) {
                    showAlert(Alert.AlertType.WARNING, "Thông báo", "Địa chỉ không hợp lệ",
                            "Địa chỉ chỉ được chứa chữ cái, số, khoảng trắng, dấu phẩy, dấu chấm, dấu gạch ngang.");
                    return;
                }
            }

            // Validation email
            if (!email.isEmpty()) {
                if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                    showAlert(Alert.AlertType.WARNING, "Thông báo", "Email không hợp lệ",
                            "Vui lòng nhập email đúng định dạng (ví dụ: example@domain.com).");
                    return;
                }
            }

            // Validation tên thú cưng
            if (petName.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Thông báo", "Thiếu thông tin",
                        "Vui lòng nhập tên thú cưng.");
                return;
            }
            if (petName.length() > 50) {
                showAlert(Alert.AlertType.WARNING, "Thông báo", "Tên thú cưng quá dài",
                        "Tên thú cưng không được quá 50 ký tự.");
                return;
            }
            if (!petName.matches("^[a-zA-ZÀ-ỹ0-9\\s]+$")) {
                showAlert(Alert.AlertType.WARNING, "Thông báo", "Tên thú cưng không hợp lệ",
                        "Tên thú cưng chỉ được chứa chữ cái, số và khoảng trắng.");
                return;
            }

            // Validation dịch vụ
            Service selectedService = serviceComboBox.getValue();
            if (selectedService == null) {
                showAlert(Alert.AlertType.WARNING, "Thông báo", "Thiếu thông tin",
                        "Vui lòng chọn dịch vụ.");
                return;
            }
            if (!selectedService.isActive()) {
                showAlert(Alert.AlertType.WARNING, "Thông báo", "Dịch vụ không khả dụng",
                        "Dịch vụ " + selectedService.getName() + " đã ngừng hoạt động.");
                return;
            }

            // Validation ngày giờ
            if (bookingDate == null) {
                showAlert(Alert.AlertType.WARNING, "Thông báo", "Thiếu thông tin",
                        "Vui lòng chọn ngày hẹn.");
                return;
            }
            String timeStr = bookingTimeField.getText().trim();
            LocalTime bookingTime;
            try {
                bookingTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException e) {
                showAlert(Alert.AlertType.WARNING, "Thông báo", "Giờ không hợp lệ",
                        "Vui lòng nhập giờ theo định dạng HH:mm.");
                return;
            }
            LocalDateTime bookingDateTime = LocalDateTime.of(bookingDate, bookingTime);
            LocalDate today = LocalDate.now();
            if (bookingDate.isBefore(today)) {
                showAlert(Alert.AlertType.WARNING, "Thông báo", "Ngày không hợp lệ",
                        "Không thể đặt lịch cho ngày trước ngày hiện tại.");
                return;
            }
            if (bookingDate.isEqual(today)) {
                LocalTime now = LocalTime.now();
                if (bookingTime.isBefore(now)) {
                    showAlert(Alert.AlertType.WARNING, "Thông báo", "Giờ không hợp lệ",
                            "Không thể đặt lịch cho giờ đã qua trong ngày hôm nay.");
                    return;
                }
            }
            if (bookingTime.isBefore(LocalTime.of(8, 0)) || bookingTime.isAfter(LocalTime.of(22, 0))) {
                showAlert(Alert.AlertType.WARNING, "Thông báo", "Giờ không hợp lệ",
                        "Giờ hẹn phải từ 8:00 đến 22:00.");
                return;
            }
            if (checkBookingConflict(bookingDateTime)) {
                showAlert(Alert.AlertType.WARNING, "Thông báo", "Xung đột lịch hẹn",
                        "Thời gian này đã có lịch hẹn khác. Vui lòng chọn thời gian khác.");
                return;
            }

            // Validation ghi chú
            String notes = notesAreaNewBooking.getText().trim();
            if (notes.length() > 500) {
                showAlert(Alert.AlertType.WARNING, "Thông báo", "Ghi chú quá dài",
                        "Ghi chú không được quá 500 ký tự.");
                return;
            }

            // Kiểm tra khách hàng
            if (selectedCustomer == null) {
                showAlert(Alert.AlertType.WARNING, "Thông báo", "Thiếu thông tin",
                        "Vui lòng chọn khách hàng.");
                return;
            }

            // Kiểm tra thú cưng
            List<Pet> existingPets = petRepository.selectByCondition("customer_id = ? AND name = ?",
                    selectedCustomer.getId(), petName);
            Pet selectedPet = existingPets.isEmpty() ? null : existingPets.get(0);
            if (selectedPet == null) {
                showAlert(Alert.AlertType.WARNING, "Thông báo", "Thú cưng không tồn tại",
                        "Thú cưng với tên " + petName + " không tồn tại. Vui lòng tạo mới thú cưng.");
                return;
            }

            // Hiển thị thông tin xác nhận
            String message = String.format(
                    "Thông tin đặt lịch:\n" +
                    "Khách hàng: %s\n" +
                    "Số điện thoại: %s\n" +
                    "Địa chỉ: %s\n" +
                    "Email: %s\n" +
                    "Thú cưng: %s\n" +
                    "Ngày hẹn: %s\n" +
                    "Giờ hẹn: %s\n" +
                    "Dịch vụ: %s\n" +
                    "Ghi chú: %s\n\n" +
                    "Xác nhận đặt lịch?",
                    selectedCustomer.getFullName(),
                    phone,
                    address.isEmpty() ? "Không có" : address,
                    email.isEmpty() ? "Không có" : email,
                    selectedPet.getName(),
                    bookingDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    bookingTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    selectedService.getName(),
                    notes.isEmpty() ? "Không có" : notes
            );

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
            confirmAlert.setTitle("Xác nhận đặt lịch");
            confirmAlert.setHeaderText("Xác nhận thông tin đặt lịch");

            Optional<ButtonType> result = confirmAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.YES) {
                Staff currentStaff = Session.getCurrentStaff();

                // Tạo booking
                Booking createdBooking = bookingService.createBooking(
                        selectedCustomer.getId(),
                        selectedPet.getPetId(),
                        currentStaff != null ? currentStaff.getId() : 0,
                        bookingDateTime,
                        notes
                );

                // Thêm booking details
                BookingDetail bookingDetail = new BookingDetail();
                bookingDetail.setBooking(createdBooking);
                bookingDetail.setService(selectedService);
                bookingDetail.setQuantity(1);
                bookingDetail.setPrice(selectedService.getPrice());

                if (!selectedService.isActive()) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Dịch vụ không khả dụng",
                            "Dịch vụ " + selectedService.getName() + " đã ngừng hoạt động!");
                    return;
                }

                bookingDetailRepository.insert(bookingDetail);

                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đặt lịch thành công",
                        "Lịch hẹn đã được tạo thành công với trạng thái: " + createdBooking.getStatus().name());

                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể đặt lịch",
                    "Đã xảy ra lỗi: " + e.getMessage());
            e.printStackTrace();
        }
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