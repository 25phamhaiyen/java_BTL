package controllers.Staff;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import controllers.SceneSwitcher;
import enums.StatusEnum;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Booking;
import model.BookingDetail;
import model.Staff;
import repository.BookingDetailRepository;
import repository.BookingRepository;
import utils.DatabaseConnection;
import utils.RoleChecker;
import utils.Session;
import model.Pet;
import repository.PetRepository;
import model.Customer;
public class BookingViewController implements Initializable {
    @FXML private Button newBookingButton;
    @FXML private Label currentDateLabel;
    @FXML private Label staffNameLabel;
    @FXML private DatePicker datePicker;
    @FXML private Button todayButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button confirmArrivalButton;
    @FXML private Button startButton;
    @FXML private Button completeButton;
    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, Integer> idColumn;
    @FXML private TableColumn<Booking, LocalDateTime> timeColumn;
    @FXML private TableColumn<Booking, String> customerColumn;
    @FXML private TableColumn<Booking, String> phoneColumn;
    @FXML private TableColumn<Booking, String> petColumn;
    @FXML private TableColumn<Booking, String> serviceColumn;
    @FXML private TableColumn<Booking, String> statusColumn;
    @FXML private TableColumn<Booking, String> assignedStaffColumn;
    @FXML private TextArea notesArea;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> upcomingStatusFilter;
    @FXML private TableView<Booking> upcomingBookingTable;
    @FXML private TableColumn<Booking, Integer> upcomingIdColumn;
    @FXML private TableColumn<Booking, LocalDate> upcomingDateColumn;
    @FXML private TableColumn<Booking, String> upcomingTimeColumn;
    @FXML private TableColumn<Booking, String> upcomingCustomerColumn;
    @FXML private TableColumn<Booking, String> upcomingPhoneColumn;
    @FXML private TableColumn<Booking, String> upcomingPetColumn;
    @FXML private TableColumn<Booking, String> upcomingServiceColumn;
    @FXML private TableColumn<Booking, String> upcomingStatusColumn;
    @FXML private TableColumn<Booking, String> upcomingStaffColumn;
    @FXML private Label statusMessageLabel;
    @FXML private Button homeButton;

    private BookingRepository bookingRepository;
    private BookingDetailRepository bookingDetailRepository;
    private ObservableList<Booking> bookingList;
    private ObservableList<Booking> upcomingBookingList;
    private Booking selectedBooking;

    /**
     * Khởi tạo controller với các repository cần thiết
     */
    public BookingViewController() {
        try {
            this.bookingRepository = BookingRepository.getInstance();
            this.bookingDetailRepository = new BookingDetailRepository();
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo BookingViewController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Kiểm tra quyền truy cập vào controller
            Staff currentStaff = Session.getCurrentStaff();
            if (currentStaff == null || !RoleChecker.hasPermission("VIEW_BOOKING_ASSIGNED")) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không có quyền truy cập",
                        "Bạn không có quyền truy cập vào màn hình quản lý đặt lịch.");
                Stage stage = (Stage) currentDateLabel.getScene().getWindow();
                stage.close();
                return;
            }

            // Hiển thị thông tin ngày hiện tại và nhân viên
            currentDateLabel.setText("Ngày hiện tại: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            staffNameLabel.setText("Nhân viên: " + (currentStaff != null ? currentStaff.getFullName() : "N/A"));

            // Thiết lập cấu hình cho bảng
            initializeBookingTable();
            initializeUpcomingBookingTable();

            // Thiết lập các tùy chọn cho ComboBox trạng thái
            ObservableList<String> statusOptions = FXCollections.observableArrayList(
                    "Tất cả",
                    StatusEnum.PENDING.name() + " - Chờ xác nhận",
                    StatusEnum.CONFIRMED.name() + " - Đã xác nhận",
                    StatusEnum.COMPLETED.name() + " - Hoàn thành",
                    StatusEnum.CANCELLED.name() + " - Đã hủy");
            statusFilter.setItems(statusOptions);
            statusFilter.setValue("Tất cả");
            upcomingStatusFilter.setItems(statusOptions);
            upcomingStatusFilter.setValue("Tất cả");

            // Thiết lập giá trị mặc định cho DatePicker
            datePicker.setValue(LocalDate.now());
            startDatePicker.setValue(LocalDate.now());
            endDatePicker.setValue(LocalDate.now().plusDays(7));

            // Tải dữ liệu ban đầu
            loadTodaySchedule();
            loadUpcomingBookings();

            // Thiết lập xử lý sự kiện cho việc chọn booking
            bookingTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, old, newValue) -> handleBookingSelection(newValue));
            upcomingBookingTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, old, newValue) -> handleBookingSelection(newValue));

            // Thiết lập hiển thị nút dựa trên quyền
            setupButtonVisibility();

            // Cập nhật placeholder cho searchField
            searchField.setPromptText("Nhập số điện thoại khách hàng");

            // Thiết lập định dạng cột trạng thái
            setupStatusColumnFormatter();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể khởi tạo giao diện",
                    "Đã xảy ra lỗi khi khởi tạo giao diện: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Thiết lập định dạng cho cột trạng thái để hiển thị màu sắc
     */
 // Cập nhật setupStatusColumnFormatter trong BookingViewController

    private void setupStatusColumnFormatter() {
        statusColumn.setCellFactory(column -> new TableCell<Booking, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                // Hiển thị trạng thái bằng tiếng Việt
                String statusText;
                String styleClass;
                
                switch (status) {
                    case "PENDING":
                        statusText = "Chờ xác nhận";
                        styleClass = "-fx-background-color: #FFF9C4; -fx-text-fill: #F57F17; -fx-padding: 3 8;";
                        break;
                    case "CONFIRMED":
                        statusText = "Đã xác nhận";
                        styleClass = "-fx-background-color: #C8E6C9; -fx-text-fill: #2E7D32; -fx-padding: 3 8;";
                        break;
                    case "COMPLETED":
                        statusText = "Hoàn thành";
                        styleClass = "-fx-background-color: #BBDEFB; -fx-text-fill: #1565C0; -fx-padding: 3 8;";
                        break;
                    case "CANCELLED":
                        statusText = "Đã hủy";
                        styleClass = "-fx-background-color: #FFCDD2; -fx-text-fill: #B71C1C; -fx-padding: 3 8;";
                        break;
                    default:
                        statusText = status;
                        styleClass = "";
                        break;
                }
                
                setText(statusText);
                setStyle(styleClass);
            }
        });

        // Tương tự cho upcomingStatusColumn
        upcomingStatusColumn.setCellFactory(column -> new TableCell<Booking, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                // Hiển thị trạng thái bằng tiếng Việt
                String statusText;
                String styleClass;
                
                switch (status) {
                    case "PENDING":
                        statusText = "Chờ xác nhận";
                        styleClass = "-fx-background-color: #FFF9C4; -fx-text-fill: #F57F17; -fx-padding: 3 8;";
                        break;
                    case "CONFIRMED":
                        statusText = "Đã xác nhận";
                        styleClass = "-fx-background-color: #C8E6C9; -fx-text-fill: #2E7D32; -fx-padding: 3 8;";
                        break;
                    case "COMPLETED":
                        statusText = "Hoàn thành";
                        styleClass = "-fx-background-color: #BBDEFB; -fx-text-fill: #1565C0; -fx-padding: 3 8;";
                        break;
                    case "CANCELLED":
                        statusText = "Đã hủy";
                        styleClass = "-fx-background-color: #FFCDD2; -fx-text-fill: #B71C1C; -fx-padding: 3 8;";
                        break;
                    default:
                        statusText = status;
                        styleClass = "";
                        break;
                }
                
                setText(statusText);
                setStyle(styleClass);
            }
        });
    }

    /**
     * Khởi tạo bảng lịch hẹn hôm nay
     */
    private void initializeBookingTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("bookingTime"));
        timeColumn.setCellFactory(column -> new TableCell<Booking, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatter.format(item));
            }
        });
        customerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCustomer() != null ? cellData.getValue().getCustomer().getFullName() : ""));
        phoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCustomer() != null ? cellData.getValue().getCustomer().getPhone() : ""));

        // Sửa phần hiển thị tên thú cưng với debug detail
        petColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() == null) {
                System.out.println("Booking là null");
                return new SimpleStringProperty("");
            }

            Booking booking = cellData.getValue();
            System.out.println("Xử lý booking ID: " + booking.getBookingId());

            if (booking.getPet() == null) {
                System.out.println("Thú cưng của booking " + booking.getBookingId() + " là null");
                return new SimpleStringProperty("Không có thông tin");
            }

            Pet pet = booking.getPet();
            System.out.println("Thú cưng của booking " + booking.getBookingId()
                            + ": ID=" + pet.getPetId()
                            + ", Tên='" + pet.getName() + "'");

            // Kiểm tra nếu tên là null hoặc rỗng
            if (pet.getName() == null || pet.getName().isEmpty()) {
                System.out.println("Tên thú cưng rỗng hoặc null, thử tải lại từ DB");

                // Tải lại thông tin thú cưng từ database
                try {
                    Pet refreshedPet = PetRepository.getInstance().selectById(pet.getPetId());
                    if (refreshedPet != null && refreshedPet.getName() != null && !refreshedPet.getName().isEmpty()) {
                        System.out.println("Đã tải lại tên thú cưng: '" + refreshedPet.getName() + "'");
                        return new SimpleStringProperty(refreshedPet.getName());
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi tải lại thông tin thú cưng: " + e.getMessage());
                }
            }

            return new SimpleStringProperty(pet.getName());
        });

        serviceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(getServiceNameFromBooking(cellData.getValue())));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().name() : ""));
        assignedStaffColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStaff() != null ? cellData.getValue().getStaff().getFullName() : ""));
    }

    /**
     * Khởi tạo bảng lịch hẹn sắp tới
     */
    private void initializeUpcomingBookingTable() {
        upcomingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        upcomingDateColumn.setCellValueFactory(cellData -> Bindings.createObjectBinding(
                () -> cellData.getValue() != null ? cellData.getValue().getBookingTime().toLocalDate() : null));
        upcomingTimeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getBookingTime() != null ?
                cellData.getValue().getBookingTime().format(DateTimeFormatter.ofPattern("HH:mm")) : ""));
        upcomingCustomerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCustomer() != null ? cellData.getValue().getCustomer().getFullName() : ""));
        upcomingPhoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCustomer() != null ? cellData.getValue().getCustomer().getPhone() : ""));
        upcomingPetColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getPet() != null ? cellData.getValue().getPet().getName() : ""));
        upcomingServiceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                getServiceNameFromBooking(cellData.getValue())));
        upcomingStatusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().name() : ""));
        upcomingStaffColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStaff() != null ? cellData.getValue().getStaff().getFullName() : ""));
    }

    /**
     * Lấy tên dịch vụ từ booking
     */
    private String getServiceNameFromBooking(Booking booking) {
        if (booking == null) {
            System.out.println("Booking là null trong getServiceNameFromBooking");
            return "";
        }

        System.out.println("Lấy dịch vụ cho booking ID: " + booking.getBookingId());

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            // Truy vấn để lấy tất cả dịch vụ của booking
            String sql = "SELECT s.name, s.service_id FROM service s " +
                         "JOIN booking_detail bd ON s.service_id = bd.service_id " +
                         "WHERE bd.booking_id = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, booking.getBookingId());
            rs = stmt.executeQuery();

            StringBuilder serviceNames = new StringBuilder();
            while (rs.next()) {
                String serviceName = rs.getString("name");
                int serviceId = rs.getInt("service_id");
                System.out.println("-> Dịch vụ của booking ID " + booking.getBookingId() +
                                 ": '" + serviceName + "', ID=" + serviceId);
                serviceNames.append(serviceName).append(", ");
            }

            if (serviceNames.length() > 0) {
                return serviceNames.substring(0, serviceNames.length() - 2); // Xóa ", " cuối cùng
            }

            System.out.println("-> Không tìm thấy dịch vụ nào cho booking ID: " + booking.getBookingId());
            // Thêm kiểm tra số lượng bản ghi trong booking_detail
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM booking_detail WHERE booking_id = ?")) {
                checkStmt.setInt(1, booking.getBookingId());
                try (ResultSet checkRs = checkStmt.executeQuery()) {
                    if (checkRs.next()) {
                        int count = checkRs.getInt(1);
                        System.out.println("Số lượng bản ghi trong booking_detail cho booking " + booking.getBookingId() + ": " + count);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tên dịch vụ: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* Bỏ qua */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* Bỏ qua */ }
            if (conn != null) try { conn.close(); } catch (SQLException e) { /* Bỏ qua */ }
        }

        return "Không có thông tin dịch vụ";
    }

    /**
     * Thêm phương thức tải trực tiếp booking từ cơ sở dữ liệu
     */
    private Booking loadBookingDirectly(int bookingId) {
        System.out.println("Đang tải trực tiếp booking ID: " + bookingId);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, " +
                         "c.customer_id, cp.full_name AS customer_name, cp.phone AS customer_phone, " +
                         "p.pet_id, p.name AS pet_name, " +
                         "s.staff_id, sp.full_name AS staff_name " +
                         "FROM booking b " +
                         "JOIN customer c ON b.customer_id = c.customer_id " +
                         "JOIN person cp ON c.customer_id = cp.person_id " +
                         "JOIN pet p ON b.pet_id = p.pet_id " +
                         "LEFT JOIN staff s ON b.staff_id = s.staff_id " +
                         "LEFT JOIN person sp ON s.staff_id = sp.person_id " +
                         "WHERE b.booking_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, bookingId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Tạo đối tượng Booking với dữ liệu đầy đủ
                        Booking booking = new Booking();
                        booking.setBookingId(rs.getInt("booking_id"));
                        booking.setBookingTime(rs.getTimestamp("booking_time").toLocalDateTime());
                        booking.setStatus(StatusEnum.valueOf(rs.getString("status")));
                        booking.setNote(rs.getString("note"));

                        // Thiết lập thông tin khách hàng
                        Customer customer = new Customer();
                        customer.setId(rs.getInt("customer_id"));
                        customer.setFullName(rs.getString("customer_name"));
                        customer.setPhone(rs.getString("customer_phone"));
                        booking.setCustomer(customer);

                        // Thiết lập thông tin thú cưng
                        Pet pet = new Pet();
                        pet.setPetId(rs.getInt("pet_id"));
                        pet.setName(rs.getString("pet_name"));
                        booking.setPet(pet);

                        // Thiết lập thông tin nhân viên
                        Staff staff = new Staff();
                        staff.setId(rs.getInt("staff_id"));
                        staff.setFullName(rs.getString("staff_name"));
                        booking.setStaff(staff);

                        System.out.println("Đã tải booking " + bookingId + ", Thú cưng: '" +
                                         pet.getName() + "', ID: " + pet.getPetId());

                        return booking;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải booking trực tiếp: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Lấy chi tiết dịch vụ của booking từ cơ sở dữ liệu
     */
    private List<BookingDetail> getBookingDetails(int bookingId) {
        try {
            String condition = "booking_id = ?";
            return bookingDetailRepository.selectByCondition(condition, bookingId);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy chi tiết booking: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Thiết lập hiển thị các nút dựa trên quyền của người dùng
     */
    private void setupButtonVisibility() {
        try {
            Staff currentStaff = Session.getCurrentStaff();
            if (currentStaff == null) {
                confirmArrivalButton.setDisable(true);
                startButton.setDisable(true);
                completeButton.setDisable(true);
                return;
            }

            boolean canCreateBooking = RoleChecker.hasPermission("CREATE_BOOKING");
            boolean canMarkServiceDone = RoleChecker.hasPermission("MARK_SERVICE_DONE");

            confirmArrivalButton.setDisable(!canMarkServiceDone);
            startButton.setDisable(!canMarkServiceDone);
            completeButton.setDisable(!canMarkServiceDone);

            if (newBookingButton != null) {
                newBookingButton.setVisible(canCreateBooking);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi thiết lập hiển thị nút: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Xử lý khi chọn một booking trong danh sách
     */
    private void handleBookingSelection(Booking booking) {
        try {
            selectedBooking = booking;

            if (selectedBooking != null) {
                notesArea.setText(selectedBooking.getNote() != null ? selectedBooking.getNote() : "");

                StatusEnum status = selectedBooking.getStatus();
                confirmArrivalButton.setDisable(status != StatusEnum.PENDING && status != StatusEnum.CONFIRMED);
                startButton.setDisable(status != StatusEnum.CONFIRMED);
                completeButton.setDisable(status != StatusEnum.CONFIRMED);
            } else {
                notesArea.setText("");
                confirmArrivalButton.setDisable(true);
                startButton.setDisable(true);
                completeButton.setDisable(true);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi xử lý chọn booking: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tải lịch hẹn của ngày hôm nay
     */
    @FXML
    private void loadTodaySchedule() {
        try {
            datePicker.setValue(LocalDate.now());
            loadBookingsByDate(LocalDate.now());
            statusMessageLabel.setText("Đã tải lịch hẹn cho ngày hôm nay");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải lịch hẹn",
                    "Đã xảy ra lỗi khi tải lịch hẹn hôm nay: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tải lịch hẹn theo ngày được chọn
     */
    private void loadBookingsByDate(LocalDate date) {
        try {
            List<Booking> bookings = bookingRepository.getBookingsByDate(date);
            // Tải lại chi tiết booking để đảm bảo dữ liệu mới nhất
            for (Booking booking : bookings) {
                Booking refreshedBooking = loadBookingDirectly(booking.getBookingId());
                if (refreshedBooking != null) {
                    booking.setCustomer(refreshedBooking.getCustomer());
                    booking.setPet(refreshedBooking.getPet());
                    booking.setStaff(refreshedBooking.getStaff());
                    booking.setStatus(refreshedBooking.getStatus());
                    booking.setNote(refreshedBooking.getNote());
                    booking.setBookingTime(refreshedBooking.getBookingTime());
                }
            }
            bookingList = FXCollections.observableArrayList(bookings);
            bookingTable.setItems(bookingList);

            bookingTable.getSelectionModel().clearSelection();
            selectedBooking = null;

            notesArea.setText("");
            confirmArrivalButton.setDisable(true);
            startButton.setDisable(true);
            completeButton.setDisable(true);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải lịch hẹn",
                    "Đã xảy ra lỗi khi tải lịch hẹn theo ngày: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tải lịch hẹn sắp tới trong khoảng thời gian
     */
    private void loadUpcomingBookings() {
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (startDate == null || endDate == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Thiếu thông tin",
                        "Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc");
                return;
            }

            if (startDate.isAfter(endDate)) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Lỗi khoảng thời gian",
                        "Ngày bắt đầu không thể sau ngày kết thúc");
                return;
            }

            List<Booking> bookings = bookingRepository.getBookingsByDateRange(startDate, endDate);
            // Tải lại chi tiết booking để đảm bảo dữ liệu mới nhất
            for (Booking booking : bookings) {
                Booking refreshedBooking = loadBookingDirectly(booking.getBookingId());
                if (refreshedBooking != null) {
                    booking.setCustomer(refreshedBooking.getCustomer());
                    booking.setPet(refreshedBooking.getPet());
                    booking.setStaff(refreshedBooking.getStaff());
                    booking.setStatus(refreshedBooking.getStatus());
                    booking.setNote(refreshedBooking.getNote());
                    booking.setBookingTime(refreshedBooking.getBookingTime());
                }
            }

            String statusFilter = upcomingStatusFilter.getValue();
            if (statusFilter != null && !statusFilter.equals("Tất cả")) {
                String statusCode = statusFilter.split(" - ")[0];
                bookings = bookings.stream()
                        .filter(booking -> booking.getStatus().name().equals(statusCode))
                        .collect(java.util.stream.Collectors.toList());
            }

            upcomingBookingList = FXCollections.observableArrayList(bookings);
            upcomingBookingTable.setItems(upcomingBookingList);

            statusMessageLabel.setText("Đã tải " + bookings.size() + " lịch hẹn từ "
                    + startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + " đến " + endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải lịch hẹn sắp tới",
                    "Đã xảy ra lỗi khi tải lịch hẹn sắp tới: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Xử lý tìm kiếm booking theo số điện thoại
     */
    @FXML
    private void searchBookings() {
        try {
            String phone = searchField.getText().trim();

            if (phone.isEmpty()) {
                loadBookingsByDate(datePicker.getValue());
                return;
            }

            List<Booking> searchResults = bookingRepository.searchBookingsByPhone(phone);

            if (datePicker.getValue() != null) {
                LocalDate date = datePicker.getValue();
                searchResults = searchResults.stream()
                        .filter(booking -> booking.getBookingTime().toLocalDate().isEqual(date))
                        .collect(java.util.stream.Collectors.toList());
            }

            // Tải lại chi tiết booking để đảm bảo dữ liệu mới nhất
            for (Booking booking : searchResults) {
                Booking refreshedBooking = loadBookingDirectly(booking.getBookingId());
                if (refreshedBooking != null) {
                    booking.setCustomer(refreshedBooking.getCustomer());
                    booking.setPet(refreshedBooking.getPet());
                    booking.setStaff(refreshedBooking.getStaff());
                    booking.setStatus(refreshedBooking.getStatus());
                    booking.setNote(refreshedBooking.getNote());
                    booking.setBookingTime(refreshedBooking.getBookingTime());
                }
            }

            bookingList = FXCollections.observableArrayList(searchResults);
            bookingTable.setItems(bookingList);

            statusMessageLabel.setText("Tìm thấy " + searchResults.size() + " lịch hẹn với SĐT: " + phone);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tìm kiếm lịch hẹn",
                    "Đã xảy ra lỗi khi tìm kiếm lịch hẹn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Áp dụng bộ lọc cho danh sách lịch hẹn
     */
    @FXML
    private void applyFilters() {
        try {
            LocalDate date = datePicker.getValue();
            String statusValue = statusFilter.getValue();

            List<Booking> bookings = bookingRepository.getBookingsByDate(date);

            if (statusValue != null && !statusValue.equals("Tất cả")) {
                String statusCode = statusValue.split(" - ")[0];
                bookings = bookings.stream()
                        .filter(booking -> booking.getStatus().name().equals(statusCode))
                        .collect(java.util.stream.Collectors.toList());
            }

            // Tải lại chi tiết booking để đảm bảo dữ liệu mới nhất
            for (Booking booking : bookings) {
                Booking refreshedBooking = loadBookingDirectly(booking.getBookingId());
                if (refreshedBooking != null) {
                    booking.setCustomer(refreshedBooking.getCustomer());
                    booking.setPet(refreshedBooking.getPet());
                    booking.setStaff(refreshedBooking.getStaff());
                    booking.setStatus(refreshedBooking.getStatus());
                    booking.setNote(refreshedBooking.getNote());
                    booking.setBookingTime(refreshedBooking.getBookingTime());
                }
            }

            bookingList = FXCollections.observableArrayList(bookings);
            bookingTable.setItems(bookingList);

            statusMessageLabel.setText("Đã lọc " + bookings.size() + " lịch hẹn");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lọc lịch hẹn",
                    "Đã xảy ra lỗi khi lọc lịch hẹn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Áp dụng bộ lọc cho danh sách lịch hẹn sắp tới
     */
    @FXML
    private void applyUpcomingFilters() {
        try {
            loadUpcomingBookings();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lọc lịch hẹn sắp tới",
                    "Đã xảy ra lỗi khi lọc lịch hẹn sắp tới: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Làm mới danh sách lịch hẹn
     */
    @FXML
    private void refreshBookings() {
        try {
            searchField.clear();
            statusFilter.setValue("Tất cả");
            loadBookingsByDate(datePicker.getValue());
            statusMessageLabel.setText("Đã làm mới danh sách lịch hẹn");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể làm mới lịch hẹn",
                    "Đã xảy ra lỗi khi làm mới lịch hẹn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Xem lịch hẹn trong khoảng thời gian
     */
    @FXML
    private void viewDateRange() {
        try {
            loadUpcomingBookings();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xem lịch hẹn",
                    "Đã xảy ra lỗi khi xem lịch hẹn trong khoảng thời gian: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Xử lý tạo booking mới
     */
    @FXML
    private void handleNewBooking() {
        try {
            Staff currentStaff = Session.getCurrentStaff();
            if (currentStaff == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Phiên làm việc không hợp lệ",
                        "Không tìm thấy thông tin nhân viên. Vui lòng đăng nhập lại.");
                return;
            }

            if (!RoleChecker.hasPermission("CREATE_BOOKING")) {
                System.err.println("Người dùng không có quyền CREATE_BOOKING: " + currentStaff.getFullName());
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có quyền",
                        "Bạn không có quyền tạo lịch hẹn mới. Vui lòng liên hệ quản trị viên để được cấp quyền.");
                return;
            }

            // Kiểm tra tài nguyên FXML
            URL fxmlUrl = getClass().getResource("/view/staff/NewBookingView.fxml");
            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy giao diện",
                        "Không tìm thấy tệp NewBookingView.fxml trong thư mục /views/Staff/. Vui lòng kiểm tra cấu hình dự án.");
                return;
            }

            // Tạo cửa sổ tạo booking mới
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle("Tạo lịch hẹn mới");
            modalStage.setScene(new Scene(root));

            // Hiển thị cửa sổ và chờ cho đến khi nó đóng
            modalStage.showAndWait();

            // Sau khi đóng, làm mới danh sách
            refreshBookings();
            loadUpcomingBookings();

        } catch (IOException e) {
            System.err.println("Lỗi khi tải NewBookingView.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo lịch hẹn mới",
                    "Đã xảy ra lỗi khi tải giao diện: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Lỗi không xác định khi tạo lịch hẹn mới: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo lịch hẹn mới",
                    "Đã xảy ra lỗi không xác định: " + e.getMessage());
        }
    }

    /**
     * Xác nhận khách hàng đã đến
     */
    @FXML
    private void confirmArrival() {
        try {
            if (selectedBooking == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch hẹn",
                        "Vui lòng chọn một lịch hẹn để xác nhận");
                return;
            }

            if (selectedBooking.getStatus() != StatusEnum.PENDING) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Trạng thái không hợp lệ",
                        "Chỉ có thể xác nhận đến cho các lịch hẹn đang chờ xác nhận");
                return;
            }

            selectedBooking.setStatus(StatusEnum.CONFIRMED);
            int result = bookingRepository.update(selectedBooking);
            boolean success = result > 0;

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xác nhận thành công",
                        "Đã xác nhận khách hàng đến cho lịch hẹn #" + selectedBooking.getBookingId());
                refreshBookings();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xác nhận",
                        "Không thể cập nhật trạng thái cho lịch hẹn này");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xác nhận đến",
                    "Đã xảy ra lỗi khi xác nhận khách hàng đến: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Bắt đầu dịch vụ cho booking
     */
    @FXML
    private void startService() {
        try {
            if (selectedBooking == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch hẹn",
                        "Vui lòng chọn một lịch hẹn để bắt đầu dịch vụ");
                return;
            }

            if (selectedBooking.getStatus() != StatusEnum.CONFIRMED) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Trạng thái không hợp lệ",
                        "Chỉ có thể bắt đầu dịch vụ cho các lịch hẹn đã xác nhận");
                return;
            }

            String notes = notesArea.getText().trim();
            if (!notes.isEmpty()) {
                selectedBooking.setNote(notes);
                bookingRepository.update(selectedBooking);
            }

            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã bắt đầu dịch vụ",
                    "Đã bắt đầu dịch vụ cho lịch hẹn #" + selectedBooking.getBookingId());

            completeButton.setDisable(false);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể bắt đầu dịch vụ",
                    "Đã xảy ra lỗi khi bắt đầu dịch vụ: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hoàn thành dịch vụ cho booking
     */
    @FXML
    private void completeService() {
        try {
            if (selectedBooking == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch hẹn",
                        "Vui lòng chọn một lịch hẹn để hoàn thành dịch vụ");
                return;
            }

            if (selectedBooking.getStatus() != StatusEnum.CONFIRMED) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Trạng thái không hợp lệ",
                        "Chỉ có thể hoàn thành dịch vụ cho các lịch hẹn đã xác nhận");
                return;
            }

            String notes = notesArea.getText().trim();
            if (!notes.isEmpty()) {
                selectedBooking.setNote(notes);
            }

            selectedBooking.setStatus(StatusEnum.COMPLETED);
            int result = bookingRepository.update(selectedBooking);
            boolean success = result > 0;

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Hoàn thành dịch vụ",
                        "Đã hoàn thành dịch vụ cho lịch hẹn #" + selectedBooking.getBookingId());
                refreshBookings();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hoàn thành dịch vụ",
                        "Không thể cập nhật trạng thái cho lịch hẹn này");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hoàn thành dịch vụ",
                    "Đã xảy ra lỗi khi hoàn thành dịch vụ: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tạo hóa đơn cho booking đã hoàn thành
     */
    @FXML
    private void printInvoice() {
        try {
            if (selectedBooking == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch hẹn",
                        "Vui lòng chọn một lịch hẹn để tạo hóa đơn");
                return;
            }

            if (selectedBooking.getStatus() != StatusEnum.COMPLETED) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Trạng thái không hợp lệ",
                        "Chỉ có thể tạo hóa đơn cho các lịch hẹn đã hoàn thành");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/CreateInvoiceView.fxml"));
            Parent root = loader.load();

            controllers.Staff.CreateInvoiceController controller = loader.getController();

            if (selectedBooking.getLocation() == null || selectedBooking.getLocation().isEmpty()) {
                selectedBooking.setLocation("Store 1");
            }

            controller.initData(selectedBooking);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle("Tạo hóa đơn cho lịch hẹn #" + selectedBooking.getBookingId());
            modalStage.setScene(new Scene(root));

            modalStage.showAndWait();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo hóa đơn",
                    "Đã xảy ra lỗi khi tạo hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Xuất báo cáo lịch hẹn ra file CSV
     */
    private void exportBookingsToCSV() {
        try {
            if (bookingList == null || bookingList.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có dữ liệu",
                        "Không có lịch hẹn nào để xuất");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Lưu báo cáo lịch hẹn");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName("booking_report_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv");

            File file = fileChooser.showSaveDialog(bookingTable.getScene().getWindow());
            if (file != null) {
                StringBuilder csvContent = new StringBuilder();

                csvContent.append("ID,Thời gian,Khách hàng,Số điện thoại,Thú cưng,Dịch vụ,Trạng thái,Nhân viên phụ trách\n");

                for (Booking booking : bookingList) {
                    csvContent.append(booking.getBookingId()).append(",");
                    csvContent.append(booking.getBookingTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append(",");
                    csvContent.append(booking.getCustomer() != null ? booking.getCustomer().getFullName() : "").append(",");
                    csvContent.append(booking.getCustomer() != null ? booking.getCustomer().getPhone() : "").append(",");
                    csvContent.append(booking.getPet() != null ? booking.getPet().getName() : "").append(",");
                    csvContent.append(getServiceNameFromBooking(booking)).append(",");
                    csvContent.append(booking.getStatus() != null ? booking.getStatus().name() : "").append(",");
                    csvContent.append(booking.getStaff() != null ? booking.getStaff().getFullName() : "").append("\n");
                }

                try (FileWriter fileWriter = new FileWriter(file)) {
                    fileWriter.write(csvContent.toString());
                }

                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xuất báo cáo thành công",
                        "Đã xuất báo cáo lịch hẹn vào file " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xuất báo cáo",
                    "Đã xảy ra lỗi khi xuất báo cáo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hiển thị cửa sổ trợ giúp
     */
    @FXML
    private void showHelp() {
        Alert helpAlert = new Alert(Alert.AlertType.INFORMATION);
        helpAlert.setTitle("Trợ giúp");
        helpAlert.setHeaderText("Hướng dẫn sử dụng màn hình Quản lý đặt lịch");

        String helpContent = "1. Lịch hẹn hôm nay:\n"
                + "   - Dùng DatePicker để chọn ngày xem lịch\n"
                + "   - Nút 'Hôm nay' để quay về xem lịch ngày hiện tại\n"
                + "   - Tìm kiếm lịch hẹn theo số điện thoại khách hàng\n"
                + "   - Lọc theo trạng thái lịch hẹn\n\n"
                + "2. Xử lý lịch hẹn:\n"
                + "   - Chọn một lịch hẹn để xem chi tiết\n"
                + "   - 'Xác nhận đến' khi khách hàng đã đến\n"
                + "   - 'Bắt đầu dịch vụ' khi bắt đầu thực hiện dịch vụ\n"
                + "   - 'Hoàn thành' khi đã hoàn thành tất cả dịch vụ\n"
                + "   - 'Tạo hóa đơn' để tạo hóa đơn cho dịch vụ đã hoàn thành\n\n"
                + "3. Lịch hẹn sắp tới:\n"
                + "   - Xem lịch hẹn trong khoảng thời gian từ ngày đến ngày\n"
                + "   - Lọc theo trạng thái lịch hẹn\n\n"
                + "4. Thao tác khác:\n"
                + "   - 'Đặt lịch mới' để tạo lịch hẹn mới\n"
                + "   - 'Làm mới' để tải lại danh sách lịch hẹn";

        helpAlert.setContentText(helpContent);
        helpAlert.showAndWait();
    }

    /**
     * Thoát ứng dụng
     */
    @FXML
    private void exitApplication() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Staff/MainStaffView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) todayButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi chuyển về màn hình chính: " + e.getMessage());

            Stage stage = (Stage) todayButton.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Chuyển về trang chủ
     */
    @FXML
    private void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/staff_home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) homeButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi chuyển về trang chủ: " + e.getMessage());
            
            // Hiển thị thông báo lỗi
            showAlert(AlertType.ERROR, "Lỗi", "Không thể trở về trang chủ",
                    "Đã xảy ra lỗi: " + e.getMessage());
        }
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