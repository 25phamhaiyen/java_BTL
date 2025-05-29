
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import controllers.SceneSwitcher;
import enums.StatusEnum;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Booking;
import model.BookingDetail;
import model.Customer;
import model.Pet;
import model.Staff;
import repository.BookingDetailRepository;
import repository.BookingRepository;
import repository.PetRepository;
import utils.DatabaseConnection;
import utils.RoleChecker;
import utils.Session;

public class BookingViewController implements Initializable {
    @FXML
    private Button newBookingButton;
    @FXML
    private Label currentDateLabel;
    @FXML
    private Label staffNameLabel;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Button todayButton;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private Button confirmArrivalButton;
    @FXML
    private Button startButton;
    @FXML
    private Button completeButton;
    @FXML
    private Button refreshButton; // Added for refresh functionality
    @FXML
    private TableView<Booking> bookingTable;
    @FXML
    private TableColumn<Booking, Integer> idColumn;
    @FXML
    private TableColumn<Booking, LocalDateTime> timeColumn;
    @FXML
    private TableColumn<Booking, String> customerColumn;
    @FXML
    private TableColumn<Booking, String> phoneColumn;
    @FXML
    private TableColumn<Booking, String> petColumn;
    @FXML
    private TableColumn<Booking, String> serviceColumn;
    @FXML
    private TableColumn<Booking, String> statusColumn;
    @FXML
    private TableColumn<Booking, String> assignedStaffColumn;
    @FXML
    private TextArea notesArea;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private ComboBox<String> upcomingStatusFilter;
    @FXML
    private TableView<Booking> upcomingBookingTable;
    @FXML
    private TableColumn<Booking, Integer> upcomingIdColumn;
    @FXML
    private TableColumn<Booking, LocalDate> upcomingDateColumn;
    @FXML
    private TableColumn<Booking, String> upcomingTimeColumn;
    @FXML
    private TableColumn<Booking, String> upcomingCustomerColumn;
    @FXML
    private TableColumn<Booking, String> upcomingPhoneColumn;
    @FXML
    private TableColumn<Booking, String> upcomingPetColumn;
    @FXML
    private TableColumn<Booking, String> upcomingServiceColumn;
    @FXML
    private TableColumn<Booking, String> upcomingStatusColumn;
    @FXML
    private TableColumn<Booking, String> upcomingStaffColumn;
    @FXML
    private Label statusMessageLabel;
    @FXML
    private Button homeButton;

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
            ObservableList<String> statusOptions = FXCollections.observableArrayList("Tất cả",
                    StatusEnum.PENDING.name() + " - Chờ xác nhận", StatusEnum.CONFIRMED.name() + " - Đã xác nhận",
                    StatusEnum.COMPLETED.name() + " - Hoàn thành", StatusEnum.CANCELLED.name() + " - Đã hủy");
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
            bookingTable.getSelectionModel().selectedItemProperty()
                    .addListener((obs, old, newValue) -> handleBookingSelection(newValue));
            upcomingBookingTable.getSelectionModel().selectedItemProperty()
                    .addListener((obs, old, newValue) -> handleBookingSelection(newValue));

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

    // Thiết lập định dạng màu sắc và văn bản cho cột trạng thái trong bảng lịch hẹn
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

    // Khởi tạo bảng lịch hẹn hôm nay với các cột: ID, thời gian, khách hàng, số điện thoại, thú cưng, dịch vụ, trạng thái, nhân viên phụ trách
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
        petColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() == null) {
                return new SimpleStringProperty("");
            }

            Booking booking = cellData.getValue();
            
            if (booking.getPet() == null) {
                return new SimpleStringProperty("Không có thông tin");
            }

            Pet pet = booking.getPet();
            
            // Đảm bảo luôn có tên thú cưng
            if (pet.getName() == null || pet.getName().isEmpty()) {
                try {
                    // Tải lại thông tin thú cưng từ database nếu cần
                    Pet refreshedPet = PetRepository.getInstance().selectById(pet.getPetId());
                    if (refreshedPet != null && refreshedPet.getName() != null && !refreshedPet.getName().isEmpty()) {
                        return new SimpleStringProperty(refreshedPet.getName());
                    } else {
                        return new SimpleStringProperty("Thú cưng #" + pet.getPetId());
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi tải lại thông tin thú cưng: " + e.getMessage());
                    return new SimpleStringProperty("Thú cưng #" + pet.getPetId());
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

    // Khởi tạo bảng lịch hẹn sắp tới với các cột tương tự bảng lịch hẹn hôm nay
    private void initializeUpcomingBookingTable() {
        upcomingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        upcomingDateColumn.setCellValueFactory(cellData -> Bindings.createObjectBinding(
                () -> cellData.getValue() != null ? cellData.getValue().getBookingTime().toLocalDate() : null));
        upcomingTimeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getBookingTime() != null
                        ? cellData.getValue().getBookingTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                        : ""));
        upcomingCustomerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCustomer() != null ? cellData.getValue().getCustomer().getFullName() : ""));
        upcomingPhoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCustomer() != null ? cellData.getValue().getCustomer().getPhone() : ""));
        upcomingPetColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getPet() != null ? cellData.getValue().getPet().getName() : ""));
        upcomingServiceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(getServiceNameFromBooking(cellData.getValue())));
        upcomingStatusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().name() : ""));
        upcomingStaffColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStaff() != null ? cellData.getValue().getStaff().getFullName() : ""));
    }

    // Lấy tên dịch vụ của lịch hẹn từ cơ sở dữ liệu
    private String getServiceNameFromBooking(Booking booking) {
        if (booking == null) {
            System.out.println("Booking là null trong getServiceNameFromBooking");
            return "Không có thông tin dịch vụ";
        }

        System.out.println("Lấy dịch vụ cho booking ID: " + booking.getBookingId());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT s.name, s.service_id FROM service s " +
                     "JOIN booking_detail bd ON s.service_id = bd.service_id " +
                     "WHERE bd.booking_id = ?")) {
            
            stmt.setInt(1, booking.getBookingId());
            ResultSet rs = stmt.executeQuery();
            
            StringBuilder serviceNames = new StringBuilder();
            while (rs.next()) {
                String serviceName = rs.getString("name");
                int serviceId = rs.getInt("service_id");
                System.out.println("-> Dịch vụ: '" + serviceName + "', ID=" + serviceId);
                serviceNames.append(serviceName).append(", ");
            }
            
            if (serviceNames.length() > 0) {
                return serviceNames.substring(0, serviceNames.length() - 2);
            }
            
            // Kiểm tra số lượng bản ghi trong booking_detail
            try (PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM booking_detail WHERE booking_id = ?")) {
                checkStmt.setInt(1, booking.getBookingId());
                ResultSet checkRs = checkStmt.executeQuery();
                if (checkRs.next()) {
                    int count = checkRs.getInt(1);
                    System.out.println("Số lượng bản ghi trong booking_detail: " + count);
                    if (count == 0) {
                        System.out.println("Không có chi tiết dịch vụ nào cho booking ID: " + 
                            booking.getBookingId());
                    }
                }
            }
            
            return "Không có thông tin dịch vụ";
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tên dịch vụ: " + e.getMessage());
            e.printStackTrace();
            return "Không có thông tin dịch vụ";
        }
    }

    // Tải thông tin lịch hẹn trực tiếp từ cơ sở dữ liệu dựa trên ID
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
                        "LEFT JOIN pet p ON b.pet_id = p.pet_id " +
                        "LEFT JOIN staff s ON b.staff_id = s.staff_id " +
                        "LEFT JOIN person sp ON s.staff_id = sp.person_id " +
                        "WHERE b.booking_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, bookingId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Booking booking = new Booking();
                        booking.setBookingId(rs.getInt("booking_id"));
                        booking.setBookingTime(rs.getTimestamp("booking_time").toLocalDateTime());
                        
                        // Xử lý trạng thái đúng
                        String statusStr = rs.getString("status");
                        if (statusStr != null && !statusStr.isEmpty()) {
                            try {
                                booking.setStatus(StatusEnum.valueOf(statusStr));
                            } catch (IllegalArgumentException e) {
                                // Nếu có lỗi khi parse enum, sử dụng giá trị mặc định
                                System.err.println("Lỗi khi parse trạng thái: " + statusStr);
                                booking.setStatus(StatusEnum.PENDING);
                            }
                        } else {
                            booking.setStatus(StatusEnum.PENDING);
                        }
                        
                        booking.setNote(rs.getString("note"));

                        // Thiết lập thông tin khách hàng
                        Customer customer = new Customer();
                        customer.setId(rs.getInt("customer_id"));
                        customer.setFullName(rs.getString("customer_name"));
                        customer.setPhone(rs.getString("customer_phone"));
                        booking.setCustomer(customer);

                        // Thiết lập thông tin thú cưng (xử lý trường hợp null)
                        Pet pet = null;
                        if (rs.getObject("pet_id") != null) {
                            pet = new Pet();
                            pet.setPetId(rs.getInt("pet_id"));
                            pet.setName(rs.getString("pet_name"));
                        }
                        booking.setPet(pet);

                        // Thiết lập thông tin nhân viên (xử lý trường hợp null)
                        Staff staff = null;
                        if (rs.getObject("staff_id") != null) {
                            staff = new Staff();
                            staff.setId(rs.getInt("staff_id"));
                            staff.setFullName(rs.getString("staff_name"));
                            booking.setStaff(staff);
                        }

                        System.out.println("Đã tải booking " + bookingId + ", Thú cưng: '" + 
                            (pet != null ? pet.getName() : "null") + "', ID: " + 
                            (pet != null ? pet.getPetId() : "null"));

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
                // Cập nhật trạng thái nút dựa trên trạng thái booking
                confirmArrivalButton.setDisable(status != StatusEnum.PENDING); // Chỉ cho phép xác nhận khi PENDING
                startButton.setDisable(status != StatusEnum.CONFIRMED); // Chỉ cho phép bắt đầu khi CONFIRMED
                completeButton.setDisable(status != StatusEnum.CONFIRMED); // Chỉ cho phép hoàn thành khi CONFIRMED
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

            String statusFilterValue = upcomingStatusFilter.getValue();
            if (statusFilterValue != null && !statusFilterValue.equals("Tất cả")) {
                String statusCode = statusFilterValue.split(" - ")[0];
                bookings = bookings.stream().filter(booking -> booking.getStatus().name().equals(statusCode))
                        .collect(Collectors.toList());
            }

            upcomingBookingList = FXCollections.observableArrayList(bookings);
            upcomingBookingTable.setItems(upcomingBookingList);

            statusMessageLabel.setText("Đã tải " + bookings.size() + " lịch hẹn từ "
                    + startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " đến "
                    + endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
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
            String searchText = searchField.getText().trim();
            if (searchText.isEmpty()) {
                loadBookingsByDate(datePicker.getValue());
                return;
            }

            // Phân loại loại tìm kiếm (phone hoặc id)
            boolean isNumeric = searchText.matches("\\d+");
            
            List<Booking> searchResults;
            if (isNumeric && searchText.length() <= 5) {
                // Tìm theo ID booking
                int bookingId = Integer.parseInt(searchText);
                Booking booking = bookingRepository.selectById(bookingId);
                searchResults = booking != null ? List.of(booking) : new ArrayList<>();
            } else {
                // Tìm theo số điện thoại hoặc tên khách hàng
                String whereClause;
                if (isNumeric) {
                    // Tìm theo số điện thoại
                    whereClause = "cp.phone LIKE ?";
                } else {
                    // Tìm theo tên khách hàng
                    whereClause = "cp.full_name LIKE ?";
                }
                
                searchResults = bookingRepository.selectByCondition(whereClause, "%" + searchText + "%");
            }
            
            // Lọc theo ngày nếu đã chọn ngày
            if (datePicker.getValue() != null) {
                LocalDate date = datePicker.getValue();
                searchResults = searchResults.stream()
                        .filter(booking -> booking.getBookingTime().toLocalDate().isEqual(date))
                        .collect(Collectors.toList());
            }

            // Cập nhật lại chi tiết từ DB
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

            // Sắp xếp kết quả theo thời gian giảm dần (mới nhất lên đầu)
            searchResults.sort(Comparator.comparing(Booking::getBookingTime).reversed());
            
            bookingList = FXCollections.observableArrayList(searchResults);
            bookingTable.setItems(bookingList);
            
            // Hiển thị thông báo về kết quả tìm kiếm
            String resultText;
            if (searchResults.isEmpty()) {
                resultText = "Không tìm thấy lịch hẹn nào phù hợp với: " + searchText;
            } else {
                resultText = "Tìm thấy " + searchResults.size() + " lịch hẹn phù hợp với: " + searchText;
                
                // Nếu kết quả nhiều hơn 1, thêm thông tin về cách xem chi tiết
                if (searchResults.size() > 1) {
                    resultText += ". Nhấp vào mỗi lịch hẹn để xem chi tiết.";
                }
            }
            
            statusMessageLabel.setText(resultText);
            
            // Đặt trạng thái nút làm mới
            refreshButton.setDisable(false);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "ID không hợp lệ", 
                    "ID lịch hẹn phải là số nguyên.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tìm kiếm lịch hẹn",
                    "Lỗi: " + e.getMessage());
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
                bookings = bookings.stream().filter(booking -> booking.getStatus().name().equals(statusCode))
                        .collect(Collectors.toList());
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
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch hẹn",
                    "Vui lòng chọn một lịch hẹn.");
            return;
        }

        // Kiểm tra nếu không phải PENDING thì không cho xác nhận
        if (selectedBooking.getStatus() != StatusEnum.PENDING) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Trạng thái không hợp lệ",
                    "Chỉ có thể xác nhận đến cho các lịch hẹn đang chờ xác nhận.");
            return;
        }

        // Hiển thị hộp thoại xác nhận với thêm thông tin chi tiết
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận");
        confirmAlert.setHeaderText("Xác nhận khách hàng đến");
        
        Customer customer = selectedBooking.getCustomer();
        Pet pet = selectedBooking.getPet();
        String services = getServiceNameFromBooking(selectedBooking);
        
        String contentText = String.format(
            "Bạn có chắc chắn muốn xác nhận khách hàng đã đến cho lịch hẹn #%d?\n\n" +
            "Thông tin lịch hẹn:\n" +
            "- Khách hàng: %s\n" +
            "- Số điện thoại: %s\n" +
            "- Thú cưng: %s\n" +
            "- Dịch vụ: %s\n" +
            "- Thời gian: %s",
            selectedBooking.getBookingId(),
            customer != null ? customer.getFullName() : "Không có thông tin",
            customer != null ? customer.getPhone() : "Không có thông tin",
            pet != null ? pet.getName() : "Không có thông tin",
            services,
            selectedBooking.getBookingTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
        
        confirmAlert.setContentText(contentText);
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            // Thay đổi trạng thái và cập nhật booking
            selectedBooking.setStatus(StatusEnum.CONFIRMED);
            int resultUpdate = bookingRepository.update(selectedBooking);
            if (resultUpdate > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xác nhận thành công",
                        "Đã xác nhận khách hàng đến cho lịch hẹn #" + selectedBooking.getBookingId());
                
                // Cập nhật ghi chú nếu có
                String notes = notesArea.getText();
                if (notes != null && !notes.trim().isEmpty() && !notes.equals(selectedBooking.getNote())) {
                    selectedBooking.setNote(notes);
                    bookingRepository.update(selectedBooking);
                }
                
                refreshBookings();
                
                // Cập nhật trạng thái các nút
                confirmArrivalButton.setDisable(true);
                startButton.setDisable(false);
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xác nhận",
                        "Không thể cập nhật trạng thái.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xác nhận đến",
                    "Lỗi: " + e.getMessage());
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
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch hẹn",
                    "Vui lòng chọn một lịch hẹn.");
            return;
        }

        if (selectedBooking.getStatus() != StatusEnum.CONFIRMED) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Trạng thái không hợp lệ",
                    "Chỉ có thể hoàn thành dịch vụ cho các lịch hẹn đã xác nhận.");
            return;
        }

        // Hiển thị dialog cho phép nhập ghi chú kết quả
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Hoàn thành dịch vụ");
        dialog.setHeaderText("Nhập ghi chú kết quả dịch vụ cho thú cưng");

        // Set the button types
        ButtonType completeButtonType = new ButtonType("Hoàn thành", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(completeButtonType, ButtonType.CANCEL);

        // Create the labels and text fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextArea noteArea = new TextArea();
        noteArea.setPrefWidth(400);
        noteArea.setPrefHeight(200);
        
        // Set current note if exists
        if (selectedBooking.getNote() != null && !selectedBooking.getNote().isEmpty()) {
            noteArea.setText(selectedBooking.getNote());
        }
        
        // Add other fields as needed, e.g., checkbox for customer notification
        CheckBox notifyCustomerCheckBox = new CheckBox("Gửi thông báo cho khách hàng");

        grid.add(new Label("Ghi chú kết quả:"), 0, 0);
        grid.add(noteArea, 1, 0);
        grid.add(notifyCustomerCheckBox, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Enable/Disable complete button depending on whether a note was entered
        Node completeButton = dialog.getDialogPane().lookupButton(completeButtonType);
        completeButton.setDisable(false);

        // Request focus on the note field by default
        Platform.runLater(noteArea::requestFocus);

        // Convert the result to a note when the complete button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == completeButtonType) {
                return noteArea.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(note -> {
            try {
                // Set note if provided
                if (!note.trim().isEmpty()) {
                    selectedBooking.setNote(note);
                }

                selectedBooking.setStatus(StatusEnum.COMPLETED);
                int resultUpdate = bookingRepository.update(selectedBooking);
                
                if (resultUpdate > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Hoàn thành dịch vụ",
                            "Đã hoàn thành dịch vụ cho lịch hẹn #" + selectedBooking.getBookingId());
                    
                    // Hiển thị thông báo tạo hóa đơn nếu đã check vào checkbox
                    if (notifyCustomerCheckBox.isSelected()) {
                        // Giả lập gửi thông báo cho khách hàng
                        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Đã gửi thông báo", 
                            "Đã gửi thông báo hoàn thành dịch vụ cho khách hàng.");
                    }
                    
                    // Hiển thị hộp thoại hỏi người dùng có muốn tạo hóa đơn ngay không
                    Alert invoiceAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    invoiceAlert.setTitle("Tạo hóa đơn");
                    invoiceAlert.setHeaderText("Tạo hóa đơn cho dịch vụ đã hoàn thành?");
                    invoiceAlert.setContentText("Bạn có muốn tạo hóa đơn ngay cho dịch vụ này không?");
                    
                    Optional<ButtonType> invoiceResult = invoiceAlert.showAndWait();
                    if (invoiceResult.isPresent() && invoiceResult.get() == ButtonType.OK) {
                        printInvoice();
                    }
                    
                    refreshBookings();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hoàn thành dịch vụ",
                            "Không thể cập nhật trạng thái.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hoàn thành dịch vụ",
                        "Lỗi: " + e.getMessage());
            }
        });
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
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có dữ liệu", "Không có lịch hẹn nào để xuất");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Lưu báo cáo lịch hẹn");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName(
                    "booking_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv");

            File file = fileChooser.showSaveDialog(bookingTable.getScene().getWindow());
            if (file != null) {
                StringBuilder csvContent = new StringBuilder();

                csvContent.append(
                        "ID,Thời gian,Khách hàng,Số điện thoại,Thú cưng,Dịch vụ,Trạng thái,Nhân viên phụ trách\n");

                for (Booking booking : bookingList) {
                    csvContent.append(booking.getBookingId()).append(",");
                    csvContent.append(booking.getBookingTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                            .append(",");
                    csvContent.append(booking.getCustomer() != null ? booking.getCustomer().getFullName() : "")
                            .append(",");
                    csvContent.append(booking.getCustomer() != null ? booking.getCustomer().getPhone() : "")
                            .append(",");
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

        String helpContent = "1. Lịch hẹn hôm nay:\n" + "   - Dùng DatePicker để chọn ngày xem lịch\n"
                + "   - Nút 'Hôm nay' để quay về xem lịch ngày hiện tại\n"
                + "   - Tìm kiếm lịch hẹn theo số điện thoại khách hàng\n" + "   - Lọc theo trạng thái lịch hẹn\n\n"
                + "2. Xử lý lịch hẹn:\n" + "   - Chọn một lịch hẹn để xem chi tiết\n"
                + "   - 'Xác nhận đến' khi khách hàng đã đến\n"
                + "   - 'Bắt đầu dịch vụ' khi bắt đầu thực hiện dịch vụ\n"
                + "   - 'Hoàn thành' khi đã hoàn thành tất cả dịch vụ\n"
                + "   - 'Tạo hóa đơn' để tạo hóa đơn cho dịch vụ đã hoàn thành\n\n" + "3. Lịch hẹn sắp tới:\n"
                + "   - Xem lịch hẹn trong khoảng thời gian từ ngày đến ngày\n"
                + "   - Lọc theo trạng thái lịch hẹn\n\n" + "4. Thao tác khác:\n"
                + "   - 'Đặt lịch mới' để tạo lịch hẹn mới\n" + "   - 'Làm mới' để tải lại danh sách lịch hẹn";

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
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể trở về trang chủ", "Đã xảy ra lỗi: " + e.getMessage());
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
