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
import utils.LanguageManagerStaff;
import utils.LanguageChangeListener;
import java.util.Locale;
import java.text.MessageFormat;

public class BookingViewController implements Initializable, LanguageChangeListener {
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
    private Button refreshButton;
    @FXML
    private TableView<Booking> bookingTable;
    
    
    
    @FXML
    private TableColumn<Booking, Integer> idColumnText;
    @FXML
    private TableColumn<Booking, LocalDateTime> timeColumnText;
    @FXML
    private TableColumn<Booking, String> customerColumnText;
    @FXML
    private TableColumn<Booking, String> phoneColumnText;
    @FXML
    private TableColumn<Booking, String> petColumnText;
    @FXML
    private TableColumn<Booking, String> serviceColumnText;
    @FXML
    private TableColumn<Booking, String> statusColumnText;
    @FXML
    private TableColumn<Booking, String> assignedStaffColumnText;
    
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
    
    // Các thành phần đa ngôn ngữ
    @FXML private Label bookingManagementTitle;
    @FXML private Label currentDateText;
    @FXML private Label staffText;
    @FXML private Label dateText;
    @FXML private Button todayButtonText;
    @FXML private Label searchText;
    @FXML private Button searchButtonText;
    @FXML private Label filterStatusText;
    @FXML private Button applyFilterButtonText;
    @FXML private Button newBookingButtonText;
    @FXML private Button refreshButtonText;
    @FXML private Label notesText;
    @FXML private Button confirmArrivalButtonText;
    @FXML private Button startButtonText;
    @FXML private Button completeButtonText;
    @FXML private Tab todayAppointmentsTab;
    @FXML private Tab upcomingAppointmentsTab;
    @FXML private Label viewFromText;
    @FXML private Label toText;
    @FXML private Button viewButtonText;
    @FXML private Button homeButtonText;
    @FXML private Button helpButtonText;
    @FXML private Button exitButtonText;
    @FXML private Button applyUpcomingFilterButtonText;
    
    @FXML private ComboBox<String> languageCombo;

    private BookingRepository bookingRepository;
    private BookingDetailRepository bookingDetailRepository;
    private ObservableList<Booking> bookingList;
    private ObservableList<Booking> upcomingBookingList;
    private Booking selectedBooking;

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
            Staff currentStaff = Session.getCurrentStaff();
            if (currentStaff == null || !RoleChecker.hasPermission("VIEW_BOOKING_ASSIGNED")) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không có quyền truy cập",
                        "Bạn không có quyền truy cập vào màn hình quản lý đặt lịch.");
                Stage stage = (Stage) currentDateLabel.getScene().getWindow();
                stage.close();
                return;
            }
            
            LanguageManagerStaff.addListener(this);
            
            // Hiển thị thông tin ngày hiện tại và nhân viên
            updateCurrentDateAndStaff();

            initializeBookingTable();
            initializeUpcomingBookingTable();

            setupStatusFilters();

            datePicker.setValue(LocalDate.now());
            startDatePicker.setValue(LocalDate.now());
            endDatePicker.setValue(LocalDate.now().plusDays(7));

            loadTodaySchedule();
            loadUpcomingBookings();

            bookingTable.getSelectionModel().selectedItemProperty()
                    .addListener((obs, old, newValue) -> handleBookingSelection(newValue));
            upcomingBookingTable.getSelectionModel().selectedItemProperty()
                    .addListener((obs, old, newValue) -> handleBookingSelection(newValue));

            setupButtonVisibility();
            setupStatusColumnFormatter();
            
            // Load texts after initialization
            loadTexts();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể khởi tạo giao diện",
                    "Đã xảy ra lỗi khi khởi tạo giao diện: " + e.getMessage());
            e.printStackTrace();
        }
    }
    


    @Override
    public void onLanguageChanged() {
        loadTexts();
        updateCurrentDateAndStaff();
        setupStatusFilters();
    }
    
    private void updateCurrentDateAndStaff() {
        Staff currentStaff = Session.getCurrentStaff();
        String currentDateFormat = LanguageManagerStaff.getString("current.date.format");
        String staffFormat = LanguageManagerStaff.getString("staff.format");
        
        currentDateLabel.setText(MessageFormat.format(currentDateFormat, 
            LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        staffNameLabel.setText(MessageFormat.format(staffFormat, 
            currentStaff != null ? currentStaff.getFullName() : "N/A"));
    }
    
    private void setupStatusFilters() {
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
            LanguageManagerStaff.getString("all.status"),
            StatusEnum.PENDING.name() + " - " + LanguageManagerStaff.getString("status.pending"),
            StatusEnum.CONFIRMED.name() + " - " + LanguageManagerStaff.getString("status.confirmed"),
            StatusEnum.COMPLETED.name() + " - " + LanguageManagerStaff.getString("status.completed"),
            StatusEnum.CANCELLED.name() + " - " + LanguageManagerStaff.getString("status.cancelled")
        );
        
        statusFilter.setItems(statusOptions);
        statusFilter.setValue(LanguageManagerStaff.getString("all.status"));
        upcomingStatusFilter.setItems(statusOptions);
        upcomingStatusFilter.setValue(LanguageManagerStaff.getString("all.status"));
    }
    
    private void loadTexts() {
        try {
            // Cập nhật các label và button
            bookingManagementTitle.setText(LanguageManagerStaff.getString("booking.management.title"));
            currentDateText.setText(LanguageManagerStaff.getString("current.date"));
            staffText.setText(LanguageManagerStaff.getString("staff.label"));
            dateText.setText(LanguageManagerStaff.getString("date.label"));
            todayButtonText.setText(LanguageManagerStaff.getString("today.button"));
            searchText.setText(LanguageManagerStaff.getString("search.label"));
            searchButtonText.setText(LanguageManagerStaff.getString("search.button"));
            filterStatusText.setText(LanguageManagerStaff.getString("filter.status"));
            applyFilterButtonText.setText(LanguageManagerStaff.getString("apply.filter.button"));
            newBookingButtonText.setText(LanguageManagerStaff.getString("new.booking.button"));
            refreshButtonText.setText(LanguageManagerStaff.getString("refresh.button"));
            notesText.setText(LanguageManagerStaff.getString("notes.label"));
            confirmArrivalButtonText.setText(LanguageManagerStaff.getString("confirm.arrival.button"));
            startButtonText.setText(LanguageManagerStaff.getString("start.service.button"));
            completeButtonText.setText(LanguageManagerStaff.getString("complete.service.button"));
            todayAppointmentsTab.setText(LanguageManagerStaff.getString("today.appointments.tab"));
            upcomingAppointmentsTab.setText(LanguageManagerStaff.getString("upcoming.appointments.tab"));
            viewFromText.setText(LanguageManagerStaff.getString("view.from"));
            toText.setText(LanguageManagerStaff.getString("to.label"));
            viewButtonText.setText(LanguageManagerStaff.getString("view.button"));
            homeButtonText.setText(LanguageManagerStaff.getString("home.button"));
            helpButtonText.setText(LanguageManagerStaff.getString("help.button"));
            exitButtonText.setText(LanguageManagerStaff.getString("exit.button"));
            applyUpcomingFilterButtonText.setText(LanguageManagerStaff.getString("apply.filter.button"));
            
            // Cập nhật các cột trong bảng
            idColumnText.setText(LanguageManagerStaff.getString("id.column"));
            timeColumnText.setText(LanguageManagerStaff.getString("time.column"));
            customerColumnText.setText(LanguageManagerStaff.getString("customer.column"));
            phoneColumnText.setText(LanguageManagerStaff.getString("phone.column"));
            petColumnText.setText(LanguageManagerStaff.getString("pet.column"));
            serviceColumnText.setText(LanguageManagerStaff.getString("service.column"));
            statusColumnText.setText(LanguageManagerStaff.getString("status.column"));
            assignedStaffColumnText.setText(LanguageManagerStaff.getString("assigned.staff.column"));
            
            // Cập nhật các cột trong bảng upcoming
            upcomingIdColumn.setText(LanguageManagerStaff.getString("id.column"));
            upcomingDateColumn.setText(LanguageManagerStaff.getString("date.column"));
            upcomingTimeColumn.setText(LanguageManagerStaff.getString("time.column"));
            upcomingCustomerColumn.setText(LanguageManagerStaff.getString("customer.column"));
            upcomingPhoneColumn.setText(LanguageManagerStaff.getString("phone.column"));
            upcomingPetColumn.setText(LanguageManagerStaff.getString("pet.column"));
            upcomingServiceColumn.setText(LanguageManagerStaff.getString("service.column"));
            upcomingStatusColumn.setText(LanguageManagerStaff.getString("status.column"));
            upcomingStaffColumn.setText(LanguageManagerStaff.getString("assigned.staff.column"));
            
            // Cập nhật placeholder cho bảng
            bookingTable.setPlaceholder(new Label(LanguageManagerStaff.getString("no.appointments.today")));
            upcomingBookingTable.setPlaceholder(new Label(LanguageManagerStaff.getString("no.appointments.range")));
            
            // Cập nhật placeholder cho search field
            searchField.setPromptText(LanguageManagerStaff.getString("search.phone.prompt"));
            
        } catch (Exception e) {
            System.err.println("Error loading texts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void cleanup() {
        LanguageManagerStaff.removeListener(this);
    }
    
    private void setupStatusColumnFormatter() {
        statusColumnText.setCellFactory(column -> new TableCell<Booking, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                String statusText;
                String styleClass;

                switch (status) {
                    case "PENDING":
                        statusText = LanguageManagerStaff.getString("status.pending");
                        styleClass = "-fx-background-color: #FFF9C4; -fx-text-fill: #F57F17; -fx-padding: 3 8;";
                        break;
                    case "CONFIRMED":
                        statusText = LanguageManagerStaff.getString("status.confirmed");
                        styleClass = "-fx-background-color: #C8E6C9; -fx-text-fill: #2E7D32; -fx-padding: 3 8;";
                        break;
                    case "COMPLETED":
                        statusText = LanguageManagerStaff.getString("status.completed");
                        styleClass = "-fx-background-color: #BBDEFB; -fx-text-fill: #1565C0; -fx-padding: 3 8;";
                        break;
                    case "CANCELLED":
                        statusText = LanguageManagerStaff.getString("status.cancelled");
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

        upcomingStatusColumn.setCellFactory(column -> new TableCell<Booking, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                String statusText;
                String styleClass;

                switch (status) {
                    case "PENDING":
                        statusText = LanguageManagerStaff.getString("status.pending");
                        styleClass = "-fx-background-color: #FFF9C4; -fx-text-fill: #F57F17; -fx-padding: 3 8;";
                        break;
                    case "CONFIRMED":
                        statusText = LanguageManagerStaff.getString("status.confirmed");
                        styleClass = "-fx-background-color: #C8E6C9; -fx-text-fill: #2E7D32; -fx-padding: 3 8;";
                        break;
                    case "COMPLETED":
                        statusText = LanguageManagerStaff.getString("status.completed");
                        styleClass = "-fx-background-color: #BBDEFB; -fx-text-fill: #1565C0; -fx-padding: 3 8;";
                        break;
                    case "CANCELLED":
                        statusText = LanguageManagerStaff.getString("status.cancelled");
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

    private void initializeBookingTable() {
        idColumnText.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        timeColumnText.setCellValueFactory(new PropertyValueFactory<>("bookingTime"));
        timeColumnText.setCellFactory(column -> new TableCell<Booking, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatter.format(item));
            }
        });
        customerColumnText.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCustomer() != null ? cellData.getValue().getCustomer().getFullName() : ""));
        phoneColumnText.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCustomer() != null ? cellData.getValue().getCustomer().getPhone() : ""));
        petColumnText.setCellValueFactory(cellData -> {
            if (cellData.getValue() == null) {
                return new SimpleStringProperty("");
            }

            Booking booking = cellData.getValue();
            
            if (booking.getPet() == null) {
                return new SimpleStringProperty(LanguageManagerStaff.getString("no.pet.info"));
            }

            Pet pet = booking.getPet();
            
            if (pet.getName() == null || pet.getName().isEmpty()) {
                try {
                    Pet refreshedPet = PetRepository.getInstance().selectById(pet.getPetId());
                    if (refreshedPet != null && refreshedPet.getName() != null && !refreshedPet.getName().isEmpty()) {
                        return new SimpleStringProperty(refreshedPet.getName());
                    } else {
                        return new SimpleStringProperty(LanguageManagerStaff.getString("pet.id.format") + pet.getPetId());
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi tải lại thông tin thú cưng: " + e.getMessage());
                    return new SimpleStringProperty(LanguageManagerStaff.getString("pet.id.format") + pet.getPetId());
                }
            }

            return new SimpleStringProperty(pet.getName());
        });
        serviceColumnText.setCellValueFactory(cellData -> new SimpleStringProperty(getServiceNameFromBooking(cellData.getValue())));
        statusColumnText.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().name() : ""));
        assignedStaffColumnText.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStaff() != null ? cellData.getValue().getStaff().getFullName() : ""));
    }

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

    private String getServiceNameFromBooking(Booking booking) {
        if (booking == null) {
            System.out.println("Booking là null trong getServiceNameFromBooking");
            return LanguageManagerStaff.getString("no.service.info");
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
            
            return LanguageManagerStaff.getString("no.service.info");
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tên dịch vụ: " + e.getMessage());
            e.printStackTrace();
            return LanguageManagerStaff.getString("no.service.info");
        }
    }

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
                        
                        String statusStr = rs.getString("status");
                        if (statusStr != null && !statusStr.isEmpty()) {
                            try {
                                booking.setStatus(StatusEnum.valueOf(statusStr));
                            } catch (IllegalArgumentException e) {
                                System.err.println("Lỗi khi parse trạng thái: " + statusStr);
                                booking.setStatus(StatusEnum.PENDING);
                            }
                        } else {
                            booking.setStatus(StatusEnum.PENDING);
                        }
                        
                        booking.setNote(rs.getString("note"));

                        Customer customer = new Customer();
                        customer.setId(rs.getInt("customer_id"));
                        customer.setFullName(rs.getString("customer_name"));
                        customer.setPhone(rs.getString("customer_phone"));
                        booking.setCustomer(customer);

                        Pet pet = null;
                        if (rs.getObject("pet_id") != null) {
                            pet = new Pet();
                            pet.setPetId(rs.getInt("pet_id"));
                            pet.setName(rs.getString("pet_name"));
                        }
                        booking.setPet(pet);

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

    private List<BookingDetail> getBookingDetails(int bookingId) {
        try {
            String condition = "booking_id = ?";
            return bookingDetailRepository.selectByCondition(condition, bookingId);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy chi tiết booking: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void setupButtonVisibility() {
        try {
            Staff currentStaff = Session.getCurrentStaff();
            if (currentStaff == null) {
                confirmArrivalButtonText.setDisable(true);
                startButtonText.setDisable(true);
                completeButtonText.setDisable(true);
                return;
            }

            boolean canCreateBooking = RoleChecker.hasPermission("CREATE_BOOKING");
            boolean canMarkServiceDone = RoleChecker.hasPermission("MARK_SERVICE_DONE");

            confirmArrivalButtonText.setDisable(!canMarkServiceDone);
            startButtonText.setDisable(!canMarkServiceDone);
            completeButtonText.setDisable(!canMarkServiceDone);

            if (newBookingButtonText != null) {
                newBookingButtonText.setVisible(canCreateBooking);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi thiết lập hiển thị nút: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleBookingSelection(Booking booking) {
        try {
            selectedBooking = booking;

            if (selectedBooking != null) {
                notesArea.setText(selectedBooking.getNote() != null ? selectedBooking.getNote() : "");

                StatusEnum status = selectedBooking.getStatus();
                confirmArrivalButtonText.setDisable(status != StatusEnum.PENDING);
                startButtonText.setDisable(status != StatusEnum.CONFIRMED);
                completeButtonText.setDisable(status != StatusEnum.CONFIRMED);
            } else {
                notesArea.setText("");
                confirmArrivalButtonText.setDisable(true);
                startButtonText.setDisable(true);
                completeButtonText.setDisable(true);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi xử lý chọn booking: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void loadTodaySchedule() {
        try {
            datePicker.setValue(LocalDate.now());
            loadBookingsByDate(LocalDate.now());
            statusMessageLabel.setText(LanguageManagerStaff.getString("today.schedule.loaded"));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("cannot.load.bookings"),
                LanguageManagerStaff.getString("error.loading.today") + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadBookingsByDate(LocalDate date) {
        try {
            List<Booking> bookings = bookingRepository.getBookingsByDate(date);
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
            confirmArrivalButtonText.setDisable(true);
            startButtonText.setDisable(true);
            completeButtonText.setDisable(true);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("cannot.load.bookings"),
                LanguageManagerStaff.getString("error.loading.by.date") + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadUpcomingBookings() {
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (startDate == null || endDate == null) {
                showAlert(Alert.AlertType.WARNING, 
                    LanguageManagerStaff.getString("warning.title"), 
                    LanguageManagerStaff.getString("missing.info.title"),
                    LanguageManagerStaff.getString("select.start.end.date"));
                return;
            }

            if (startDate.isAfter(endDate)) {
                showAlert(Alert.AlertType.WARNING, 
                    LanguageManagerStaff.getString("warning.title"), 
                    LanguageManagerStaff.getString("date.range.error.title"),
                    LanguageManagerStaff.getString("start.date.after.end"));
                return;
            }

            List<Booking> bookings = bookingRepository.getBookingsByDateRange(startDate, endDate);
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
            if (statusFilterValue != null && !statusFilterValue.equals(LanguageManagerStaff.getString("all.status"))) {
                String statusCode = statusFilterValue.split(" - ")[0];
                bookings = bookings.stream().filter(booking -> booking.getStatus().name().equals(statusCode))
                        .collect(Collectors.toList());
            }

            upcomingBookingList = FXCollections.observableArrayList(bookings);
            upcomingBookingTable.setItems(upcomingBookingList);

            statusMessageLabel.setText(MessageFormat.format(LanguageManagerStaff.getString("loaded.bookings.range"), 
                bookings.size(),
                startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("cannot.load.upcoming"),
                LanguageManagerStaff.getString("error.loading.upcoming") + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void searchBookings() {
        try {
            String searchText = searchField.getText().trim();
            if (searchText.isEmpty()) {
                loadBookingsByDate(datePicker.getValue());
                return;
            }

            boolean isNumeric = searchText.matches("\\d+");
            
            List<Booking> searchResults;
            if (isNumeric && searchText.length() <= 5) {
                int bookingId = Integer.parseInt(searchText);
                Booking booking = bookingRepository.selectById(bookingId);
                searchResults = booking != null ? List.of(booking) : new ArrayList<>();
            } else {
                String whereClause;
                if (isNumeric) {
                    whereClause = "cp.phone LIKE ?";
                } else {
                    whereClause = "cp.full_name LIKE ?";
                }
                
                searchResults = bookingRepository.selectByCondition(whereClause, "%" + searchText + "%");
            }
            
            if (datePicker.getValue() != null) {
                LocalDate date = datePicker.getValue();
                searchResults = searchResults.stream()
                        .filter(booking -> booking.getBookingTime().toLocalDate().isEqual(date))
                        .collect(Collectors.toList());
            }

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

            searchResults.sort(Comparator.comparing(Booking::getBookingTime).reversed());
            
            bookingList = FXCollections.observableArrayList(searchResults);
            bookingTable.setItems(bookingList);
            
            String resultText;
            if (searchResults.isEmpty()) {
                resultText = MessageFormat.format(LanguageManagerStaff.getString("no.booking.found"), searchText);
            } else {
                resultText = MessageFormat.format(LanguageManagerStaff.getString("found.bookings"), 
                    searchResults.size(), searchText);
                
                if (searchResults.size() > 1) {
                    resultText += ". " + LanguageManagerStaff.getString("click.for.details");
                }
            }
            
            statusMessageLabel.setText(resultText);
            refreshButton.setDisable(false);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, 
                LanguageManagerStaff.getString("warning.title"), 
                LanguageManagerStaff.getString("invalid.id.title"), 
                LanguageManagerStaff.getString("id.must.be.number"));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("cannot.search.bookings"),
                LanguageManagerStaff.getString("error.title") + ": " + e.getMessage());
        }
    }

    @FXML
    private void applyFilters() {
        try {
            LocalDate date = datePicker.getValue();
            String statusValue = statusFilter.getValue();

            List<Booking> bookings = bookingRepository.getBookingsByDate(date);

            if (statusValue != null && !statusValue.equals(LanguageManagerStaff.getString("all.status"))) {
                String statusCode = statusValue.split(" - ")[0];
                bookings = bookings.stream().filter(booking -> booking.getStatus().name().equals(statusCode))
                        .collect(Collectors.toList());
            }

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

            statusMessageLabel.setText(MessageFormat.format(LanguageManagerStaff.getString("filtered.bookings"), bookings.size()));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("cannot.filter.bookings"),
                LanguageManagerStaff.getString("error.filtering") + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void applyUpcomingFilters() {
        try {
            loadUpcomingBookings();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("cannot.filter.upcoming"),
                LanguageManagerStaff.getString("error.filtering.upcoming") + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshBookings() {
        try {
            searchField.clear();
            statusFilter.setValue(LanguageManagerStaff.getString("all.status"));
            loadBookingsByDate(datePicker.getValue());
            statusMessageLabel.setText(LanguageManagerStaff.getString("refreshed.bookings"));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("cannot.refresh.bookings"),
                LanguageManagerStaff.getString("error.refreshing") + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void viewDateRange() {
        try {
            loadUpcomingBookings();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("cannot.view.bookings"),
                LanguageManagerStaff.getString("error.viewing.range") + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNewBooking() {
        try {
            Staff currentStaff = Session.getCurrentStaff();
            if (currentStaff == null) {
                showAlert(Alert.AlertType.ERROR, 
                    LanguageManagerStaff.getString("error.title"), 
                    LanguageManagerStaff.getString("invalid.session.title"),
                    LanguageManagerStaff.getString("staff.not.found.relogin"));
                return;
            }

            if (!RoleChecker.hasPermission("CREATE_BOOKING")) {
                System.err.println("Người dùng không có quyền CREATE_BOOKING: " + currentStaff.getFullName());
                showAlert(Alert.AlertType.WARNING, 
                    LanguageManagerStaff.getString("warning.title"), 
                    LanguageManagerStaff.getString("no.permission.title"),
                    LanguageManagerStaff.getString("no.create.booking.permission"));
                return;
            }

            URL fxmlUrl = getClass().getResource("/view/staff/NewBookingView.fxml");
            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, 
                    LanguageManagerStaff.getString("error.title"), 
                    LanguageManagerStaff.getString("interface.not.found.title"),
                    LanguageManagerStaff.getString("newbooking.fxml.not.found"));
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle(LanguageManagerStaff.getString("new.booking.title"));
            modalStage.setScene(new Scene(root));

            modalStage.showAndWait();

            refreshBookings();
            loadUpcomingBookings();

        } catch (IOException e) {
            System.err.println("Lỗi khi tải NewBookingView.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("cannot.create.new.booking"),
                LanguageManagerStaff.getString("error.loading.interface") + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Lỗi không xác định khi tạo lịch hẹn mới: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("cannot.create.new.booking"),
                LanguageManagerStaff.getString("unknown.error") + ": " + e.getMessage());
        }
    }

    @FXML
    private void confirmArrival() {
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, 
                LanguageManagerStaff.getString("warning.title"), 
                LanguageManagerStaff.getString("no.booking.selected.title"),
                LanguageManagerStaff.getString("please.select.booking"));
            return;
        }

        if (selectedBooking.getStatus() != StatusEnum.PENDING) {
            showAlert(Alert.AlertType.WARNING, 
                LanguageManagerStaff.getString("warning.title"), 
                LanguageManagerStaff.getString("invalid.status.title"),
                LanguageManagerStaff.getString("only.confirm.pending"));
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(LanguageManagerStaff.getString("confirm.title"));
        confirmAlert.setHeaderText(LanguageManagerStaff.getString("confirm.customer.arrival"));
        
        Customer customer = selectedBooking.getCustomer();
        Pet pet = selectedBooking.getPet();
        String services = getServiceNameFromBooking(selectedBooking);
        
        String contentText = MessageFormat.format(
            LanguageManagerStaff.getString("confirm.arrival.details"),
            selectedBooking.getBookingId(),
            customer != null ? customer.getFullName() : LanguageManagerStaff.getString("no.info"),
            customer != null ? customer.getPhone() : LanguageManagerStaff.getString("no.info"),
            pet != null ? pet.getName() : LanguageManagerStaff.getString("no.info"),
            services,
            selectedBooking.getBookingTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
        
        confirmAlert.setContentText(contentText);
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            selectedBooking.setStatus(StatusEnum.CONFIRMED);
            int resultUpdate = bookingRepository.update(selectedBooking);
            if (resultUpdate > 0) {
                showAlert(Alert.AlertType.INFORMATION, 
                    LanguageManagerStaff.getString("success.title"), 
                    LanguageManagerStaff.getString("confirm.success.title"),
                    MessageFormat.format(LanguageManagerStaff.getString("confirmed.arrival"), selectedBooking.getBookingId()));
                
                String notes = notesArea.getText();
                if (notes != null && !notes.trim().isEmpty() && !notes.equals(selectedBooking.getNote())) {
                    selectedBooking.setNote(notes);
                    bookingRepository.update(selectedBooking);
                }
                
                refreshBookings();
                
                confirmArrivalButtonText.setDisable(true);
                startButtonText.setDisable(false);
            } else {
                showAlert(Alert.AlertType.ERROR, 
                    LanguageManagerStaff.getString("error.title"), 
                    LanguageManagerStaff.getString("cannot.confirm.title"),
                    LanguageManagerStaff.getString("cannot.update.status"));
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("cannot.confirm.arrival"),
                LanguageManagerStaff.getString("error.title") + ": " + e.getMessage());
        }
    }

    @FXML
    private void startService() {
        try {
            if (selectedBooking == null) {
                showAlert(Alert.AlertType.WARNING, 
                    LanguageManagerStaff.getString("warning.title"), 
                    LanguageManagerStaff.getString("no.booking.selected.title"),
                    LanguageManagerStaff.getString("select.booking.to.start"));
                return;
            }

            if (selectedBooking.getStatus() != StatusEnum.CONFIRMED) {
                showAlert(Alert.AlertType.WARNING, 
                    LanguageManagerStaff.getString("warning.title"), 
                    LanguageManagerStaff.getString("invalid.status.title"),
                    LanguageManagerStaff.getString("only.start.confirmed"));
                return;
            }

            String notes = notesArea.getText().trim();
            if (!notes.isEmpty()) {
                selectedBooking.setNote(notes);
                bookingRepository.update(selectedBooking);
            }

            showAlert(Alert.AlertType.INFORMATION, 
                LanguageManagerStaff.getString("success.title"), 
                LanguageManagerStaff.getString("service.started.title"),
                MessageFormat.format(LanguageManagerStaff.getString("started.service"), selectedBooking.getBookingId()));

            completeButtonText.setDisable(false);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("cannot.start.service"),
                LanguageManagerStaff.getString("error.starting.service") + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void completeService() {
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, 
                LanguageManagerStaff.getString("warning.title"), 
                LanguageManagerStaff.getString("no.booking.selected.title"),
                LanguageManagerStaff.getString("please.select.booking"));
            return;
        }

        if (selectedBooking.getStatus() != StatusEnum.CONFIRMED) {
            showAlert(Alert.AlertType.WARNING, 
                LanguageManagerStaff.getString("warning.title"), 
                LanguageManagerStaff.getString("invalid.status.title"),
                LanguageManagerStaff.getString("only.complete.confirmed"));
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(LanguageManagerStaff.getString("complete.service.title"));
        dialog.setHeaderText(LanguageManagerStaff.getString("enter.service.result"));

        ButtonType completeButtonType = new ButtonType(LanguageManagerStaff.getString("complete.button"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(completeButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextArea noteArea = new TextArea();
        noteArea.setPrefWidth(400);
        noteArea.setPrefHeight(200);
        
        if (selectedBooking.getNote() != null && !selectedBooking.getNote().isEmpty()) {
            noteArea.setText(selectedBooking.getNote());
        }
        
        CheckBox notifyCustomerCheckBox = new CheckBox(LanguageManagerStaff.getString("notify.customer"));

        grid.add(new Label(LanguageManagerStaff.getString("result.notes")), 0, 0);
        grid.add(noteArea, 1, 0);
        grid.add(notifyCustomerCheckBox, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Node completeButton = dialog.getDialogPane().lookupButton(completeButtonType);
        completeButton.setDisable(false);

        Platform.runLater(noteArea::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == completeButtonType) {
                return noteArea.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(note -> {
            try {
                if (!note.trim().isEmpty()) {
                    selectedBooking.setNote(note);
                }

                selectedBooking.setStatus(StatusEnum.COMPLETED);
                int resultUpdate = bookingRepository.update(selectedBooking);
                
                if (resultUpdate > 0) {
                    showAlert(Alert.AlertType.INFORMATION, 
                        LanguageManagerStaff.getString("success.title"), 
                        LanguageManagerStaff.getString("service.completed.title"),
                        MessageFormat.format(LanguageManagerStaff.getString("completed.service"), selectedBooking.getBookingId()));
                    
                    if (notifyCustomerCheckBox.isSelected()) {
                        showAlert(Alert.AlertType.INFORMATION, 
                            LanguageManagerStaff.getString("notification.title"), 
                            LanguageManagerStaff.getString("notification.sent.title"), 
                            LanguageManagerStaff.getString("sent.completion.notification"));
                    }
                    
                    Alert invoiceAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    invoiceAlert.setTitle(LanguageManagerStaff.getString("create.invoice.title"));
                    invoiceAlert.setHeaderText(LanguageManagerStaff.getString("create.invoice.for.completed"));
                    invoiceAlert.setContentText(LanguageManagerStaff.getString("want.create.invoice.now"));
                    
                    Optional<ButtonType> invoiceResult = invoiceAlert.showAndWait();
                    if (invoiceResult.isPresent() && invoiceResult.get() == ButtonType.OK) {
                        printInvoice();
                    }
                    
                    refreshBookings();
                } else {
                    showAlert(Alert.AlertType.ERROR, 
                        LanguageManagerStaff.getString("error.title"), 
                        LanguageManagerStaff.getString("cannot.complete.service"),
                        LanguageManagerStaff.getString("cannot.update.status"));
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, 
                    LanguageManagerStaff.getString("error.title"), 
                    LanguageManagerStaff.getString("cannot.complete.service"),
                    LanguageManagerStaff.getString("error.title") + ": " + e.getMessage());
            }
        });
    }

    @FXML
    private void printInvoice() {
        try {
            if (selectedBooking == null) {
                showAlert(Alert.AlertType.WARNING, 
                    LanguageManagerStaff.getString("warning.title"), 
                    LanguageManagerStaff.getString("no.booking.selected.title"),
                    LanguageManagerStaff.getString("select.booking.for.invoice"));
                return;
            }

            if (selectedBooking.getStatus() != StatusEnum.COMPLETED) {
                showAlert(Alert.AlertType.WARNING, 
                    LanguageManagerStaff.getString("warning.title"), 
                    LanguageManagerStaff.getString("invalid.status.title"),
                    LanguageManagerStaff.getString("only.invoice.completed"));
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
            modalStage.setTitle(MessageFormat.format(LanguageManagerStaff.getString("create.invoice.for.booking"), selectedBooking.getBookingId()));
            modalStage.setScene(new Scene(root));

            modalStage.showAndWait();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("cannot.create.invoice"),
                LanguageManagerStaff.getString("error.creating.invoice") + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exportBookingsToCSV() {
        try {
            if (bookingList == null || bookingList.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, 
                    LanguageManagerStaff.getString("warning.title"), 
                    LanguageManagerStaff.getString("no.data.title"), 
                    LanguageManagerStaff.getString("no.bookings.to.export"));
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(LanguageManagerStaff.getString("save.booking.report"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName(
                    "booking_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv");

            File file = fileChooser.showSaveDialog(bookingTable.getScene().getWindow());
            if (file != null) {
                StringBuilder csvContent = new StringBuilder();

                csvContent.append(LanguageManagerStaff.getString("csv.header")).append("\n");

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

                showAlert(Alert.AlertType.INFORMATION, 
                    LanguageManagerStaff.getString("success.title"), 
                    LanguageManagerStaff.getString("export.success.title"),
                    MessageFormat.format(LanguageManagerStaff.getString("exported.report.to"), file.getAbsolutePath()));
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("cannot.export.report"),
                LanguageManagerStaff.getString("error.exporting") + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showHelp() {
        Alert helpAlert = new Alert(Alert.AlertType.INFORMATION);
        helpAlert.setTitle(LanguageManagerStaff.getString("help.title"));
        helpAlert.setHeaderText(LanguageManagerStaff.getString("help.header"));

        String helpContent = LanguageManagerStaff.getString("help.content");

        helpAlert.setContentText(helpContent);
        helpAlert.showAndWait();
    }

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

    @FXML
    private void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/staff_home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) homeButtonText.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi chuyển về trang chủ: " + e.getMessage());

            showAlert(Alert.AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("cannot.go.home"), 
                LanguageManagerStaff.getString("error.occurred") + ": " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}