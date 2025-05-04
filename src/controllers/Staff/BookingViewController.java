package controllers.Staff;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
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
import model.Customer;
import model.Pet;
import model.Service;
import model.Staff;
import repository.BookingDetailRepository;
import repository.BookingRepository;
import repository.CustomerRepository;
import repository.PetRepository;
import repository.ServiceRepository;
import service.BookingService;
import utils.RoleChecker;
import utils.Session;

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
    @FXML private Button printInvoiceButton;
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

    // Các biến cho giao diện tạo lịch hẹn mới
    @FXML private TextField phoneField;
    @FXML private TextField customerNameField;
    @FXML private ComboBox<Pet> petComboBox;
    @FXML private ComboBox<Service> serviceComboBox;
    @FXML private DatePicker bookingDatePicker;
    @FXML private TextField bookingTimeField;
    @FXML private TextArea notesAreaNewBooking;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    @FXML private Button homeButton; // Thêm tham chiếu Button


  
    private BookingService bookingService;
    private BookingRepository bookingRepository;
    private BookingDetailRepository bookingDetailRepository;
    private ServiceRepository serviceRepository;
    private CustomerRepository customerRepository;
    private PetRepository petRepository;
    private ObservableList<Booking> bookingList;
    private ObservableList<Booking> upcomingBookingList;
    private Booking selectedBooking;
    private Customer selectedCustomer;

    /**
     * Khởi tạo controller với các repository và service cần thiết
     */
    public BookingViewController() {
        try {
            this.bookingService = new BookingService();
            this.bookingRepository = BookingRepository.getInstance();
            this.bookingDetailRepository = new BookingDetailRepository();
            this.serviceRepository = ServiceRepository.getInstance();
            this.customerRepository = CustomerRepository.getInstance();
            this.petRepository = PetRepository.getInstance();
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
            if (currentStaff == null || !RoleChecker.hasPermission("VIEW_BOOKING_ASSIGNED") ) 
            {
                showAlert(AlertType.ERROR, "Lỗi", "Không có quyền truy cập",
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
            
            // Khởi tạo giao diện tạo lịch hẹn mới (nếu được load)
            initializeNewBookingForm();
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể khởi tạo giao diện",
                    "Đã xảy ra lỗi khi khởi tạo giao diện: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Khởi tạo giao diện tạo lịch hẹn mới
     */
    private void initializeNewBookingForm() {
        if (phoneField != null) { // Chỉ khởi tạo nếu giao diện NewBookingView.fxml được load
            // Tải danh sách dịch vụ
            loadServices();

            // Thiết lập mặc định ngày
            bookingDatePicker.setValue(LocalDate.now());

            // Xử lý tìm khách hàng khi nhập số điện thoại
            phoneField.textProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null && newValue.length() >= 10) {
                    searchCustomer(newValue);
                } else {
                    customerNameField.setText("");
                    petComboBox.getItems().clear();
                    selectedCustomer = null;
                }
            });
        }
    }

    /**
     * Tải danh sách dịch vụ vào ComboBox
     */
    private void loadServices() {
        try {
            List<Service> services = serviceRepository.selectAll();
            serviceComboBox.getItems().addAll(services);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải danh sách dịch vụ",
                    "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    /**
     * Tìm khách hàng theo số điện thoại
     */
    private void searchCustomer(String phone) {
        try {
            Customer customer = customerRepository.findByPhone(phone);
            if (customer != null) {
                selectedCustomer = customer;
                customerNameField.setText(customer.getFullName());
                loadPets(customer.getId());
            } else {
                customerNameField.setText("Không tìm thấy khách hàng");
                petComboBox.getItems().clear();
                selectedCustomer = null;
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tìm khách hàng",
                    "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    /**
     * Tải danh sách thú cưng của khách hàng
     */
    private void loadPets(int customerId) {
        try {
            List<Pet> pets = petRepository.selectByCondition("customer_id = ?", customerId);
            petComboBox.getItems().clear();
            petComboBox.getItems().addAll(pets);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải danh sách thú cưng",
                    "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    /**
     * Lưu lịch hẹn mới
     */
    @FXML
    private void saveBooking() {
        try {
            // Kiểm tra thông tin nhân viên hiện tại
            Staff currentStaff = Session.getCurrentStaff();
            if (currentStaff == null) {
                showAlert(AlertType.ERROR, "Lỗi", "Phiên làm việc không hợp lệ",
                        "Không tìm thấy thông tin nhân viên. Vui lòng đăng nhập lại.");
                closeNewBookingWindow();
                return;
            }

            // Kiểm tra dữ liệu
            if (selectedCustomer == null) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Thiếu thông tin khách hàng",
                        "Vui lòng nhập số điện thoại hợp lệ.");
                return;
            }

            Pet selectedPet = petComboBox.getValue();
            if (selectedPet == null) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Thiếu thông tin thú cưng",
                        "Vui lòng chọn một thú cưng.");
                return;
            }

            Service selectedService = serviceComboBox.getValue();
            if (selectedService == null) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Thiếu thông tin dịch vụ",
                        "Vui lòng chọn một dịch vụ.");
                return;
            }

            LocalDate bookingDate = bookingDatePicker.getValue();
            if (bookingDate == null) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Thiếu thông tin ngày",
                        "Vui lòng chọn ngày đặt lịch.");
                return;
            }

            String timeStr = bookingTimeField.getText().trim();
            if (timeStr.isEmpty() || !timeStr.matches("\\d{2}:\\d{2}")) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Giờ không hợp lệ",
                        "Vui lòng nhập giờ theo định dạng HH:mm (VD: 14:30).");
                return;
            }

            // Tạo thời gian đặt lịch
            LocalDateTime bookingTime = LocalDateTime.parse(
                    bookingDate + " " + timeStr,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            );

            if (bookingTime.isBefore(LocalDateTime.now())) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Thời gian không hợp lệ",
                        "Thời gian đặt lịch phải ở tương lai.");
                return;
            }

            // Tạo booking mới
            Booking booking = new Booking();
            booking.setCustomer(selectedCustomer);
            booking.setPet(selectedPet);
            booking.setStaff(currentStaff);
            booking.setBookingTime(bookingTime);
            booking.setStatus(StatusEnum.PENDING);
            booking.setNote(notesAreaNewBooking.getText().trim());

            // Lưu booking
            int bookingId = bookingRepository.insert(booking);
            if (bookingId > 0) {
                // Lưu chi tiết booking (dịch vụ)
                BookingDetail detail = new BookingDetail();
                detail.setBooking(booking);
                detail.setService(selectedService);
                detail.setQuantity(1); // Mặc định số lượng là 1
                detail.setPrice(selectedService.getPrice());
                bookingDetailRepository.insert(detail);

                showAlert(AlertType.INFORMATION, "Thành công", "Tạo lịch hẹn thành công",
                        "Lịch hẹn đã được tạo với ID: " + bookingId);
                closeNewBookingWindow();
            } else {
                showAlert(AlertType.ERROR, "Lỗi", "Không thể tạo lịch hẹn",
                        "Có lỗi xảy ra khi lưu lịch hẹn.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tạo lịch hẹn",
                    "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    /**
     * Đóng cửa sổ tạo lịch hẹn mới
     */
    @FXML
    private void cancelNewBooking() {
        closeNewBookingWindow();
    }

    private void closeNewBookingWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Thiết lập định dạng cho cột trạng thái để hiển thị màu sắc
     */
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
                
                setText(status);
                
                // Thiết lập màu sắc dựa trên trạng thái
                switch (status) {
                    case "PENDING":
                        setText("Chờ xác nhận");
                        setStyle("-fx-background-color: #FFF9C4; -fx-text-fill: #F57F17; -fx-padding: 3 8;");
                        break;
                    case "CONFIRMED":
                        setText("Đã xác nhận");
                        setStyle("-fx-background-color: #C8E6C9; -fx-text-fill: #2E7D32; -fx-padding: 3 8;");
                        break;
                    case "COMPLETED":
                        setText("Hoàn thành");
                        setStyle("-fx-background-color: #BBDEFB; -fx-text-fill: #1565C0; -fx-padding: 3 8;");
                        break;
                    case "CANCELLED":
                        setText("Đã hủy");
                        setStyle("-fx-background-color: #FFCDD2; -fx-text-fill: #B71C1C; -fx-padding: 3 8;");
                        break;
                    default:
                        setStyle("");
                        break;
                }
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
                
                setText(status);
                
                // Thiết lập màu sắc dựa trên trạng thái
                switch (status) {
                    case "PENDING":
                        setText("Chờ xác nhận");
                        setStyle("-fx-background-color: #FFF9C4; -fx-text-fill: #F57F17; -fx-padding: 3 8;");
                        break;
                    case "CONFIRMED":
                        setText("Đã xác nhận");
                        setStyle("-fx-background-color: #C8E6C9; -fx-text-fill: #2E7D32; -fx-padding: 3 8;");
                        break;
                    case "COMPLETED":
                        setText("Hoàn thành");
                        setStyle("-fx-background-color: #BBDEFB; -fx-text-fill: #1565C0; -fx-padding: 3 8;");
                        break;
                    case "CANCELLED":
                        setText("Đã hủy");
                        setStyle("-fx-background-color: #FFCDD2; -fx-text-fill: #B71C1C; -fx-padding: 3 8;");
                        break;
                    default:
                        setStyle("");
                        break;
                }
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
        petColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getPet() != null ? cellData.getValue().getPet().getName() : ""));
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
        if (booking == null) return "";
        try {
            List<BookingDetail> details = getBookingDetails(booking.getBookingId());
            if (details != null && !details.isEmpty()) {
                if (details.size() == 1) {
                    return details.get(0).getService().getName();
                } else {
                    // Nếu có nhiều dịch vụ, hiển thị dịch vụ đầu tiên và số lượng dịch vụ còn lại
                    return details.get(0).getService().getName() + " + " + (details.size() - 1) + " dịch vụ khác";
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tên dịch vụ: " + e.getMessage());
        }
        return "Không có thông tin";
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
                // Nếu không có thông tin nhân viên, vô hiệu hóa tất cả các nút
                confirmArrivalButton.setDisable(true);
                startButton.setDisable(true);
                completeButton.setDisable(true);
                printInvoiceButton.setDisable(true);
                return;
            }
            
            boolean canCreateBooking = RoleChecker.hasPermission("CREATE_BOOKING");
            boolean canMarkServiceDone = RoleChecker.hasPermission("MARK_SERVICE_DONE");
            boolean canPrintReceipt = RoleChecker.hasPermission("PRINT_RECEIPT");
            
            // Vô hiệu hóa các nút dựa trên quyền
            confirmArrivalButton.setDisable(!canMarkServiceDone);
            startButton.setDisable(!canMarkServiceDone);
            completeButton.setDisable(!canMarkServiceDone);
            printInvoiceButton.setDisable(!canPrintReceipt);
            
            // Thay vì tìm kiếm nút bằng lookup, sử dụng tham chiếu FXML trực tiếp nếu có
            // HOẶC đợi đến khi Scene được thiết lập
            
            // Kiểm tra nếu newBookingButton đã được inject thông qua FXML
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
                // Hiển thị ghi chú
                notesArea.setText(selectedBooking.getNote() != null ? selectedBooking.getNote() : "");
                
                // Thiết lập trạng thái các nút dựa trên trạng thái booking
                StatusEnum status = selectedBooking.getStatus();
                
                // Nếu booking đang ở trạng thái PENDING, có thể xác nhận đến
                confirmArrivalButton.setDisable(status != StatusEnum.PENDING && status != StatusEnum.CONFIRMED);
                
                // Nếu đã xác nhận, có thể bắt đầu dịch vụ
                startButton.setDisable(status != StatusEnum.CONFIRMED);
                
                // Nếu đã bắt đầu, có thể hoàn thành
                completeButton.setDisable(status != StatusEnum.CONFIRMED);
                
                // Nếu đã hoàn thành, có thể in hóa đơn
                printInvoiceButton.setDisable(status != StatusEnum.COMPLETED);
            } else {
                // Reset trạng thái nếu không có booking nào được chọn
                notesArea.setText("");
                confirmArrivalButton.setDisable(true);
                startButton.setDisable(true);
                completeButton.setDisable(true);
                printInvoiceButton.setDisable(true);
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
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải lịch hẹn",
                    "Đã xảy ra lỗi khi tải lịch hẹn hôm nay: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Tải lịch hẹn theo ngày được chọn
     */
    private void loadBookingsByDate(LocalDate date) {
        try {
            // Danh sách các booking theo ngày
            List<Booking> bookings = bookingRepository.getBookingsByDate(date);
            bookingList = FXCollections.observableArrayList(bookings);
            bookingTable.setItems(bookingList);
            
            // Clear selection để tránh lỗi
            bookingTable.getSelectionModel().clearSelection();
            selectedBooking = null;
            
            // Reset các controls
            notesArea.setText("");
            confirmArrivalButton.setDisable(true);
            startButton.setDisable(true);
            completeButton.setDisable(true);
            printInvoiceButton.setDisable(true);
            
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải lịch hẹn",
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
                showAlert(AlertType.WARNING, "Cảnh báo", "Thiếu thông tin",
                        "Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc");
                return;
            }
            
            if (startDate.isAfter(endDate)) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Lỗi khoảng thời gian",
                        "Ngày bắt đầu không thể sau ngày kết thúc");
                return;
            }
            
            List<Booking> bookings = bookingRepository.getBookingsByDateRange(startDate, endDate);
            
            // Áp dụng bộ lọc trạng thái nếu có
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
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải lịch hẹn sắp tới",
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
            
            // Nếu đang xem theo ngày cụ thể, lọc thêm theo ngày
            if (datePicker.getValue() != null) {
                LocalDate date = datePicker.getValue();
                searchResults = searchResults.stream()
                        .filter(booking -> booking.getBookingTime().toLocalDate().isEqual(date))
                        .collect(java.util.stream.Collectors.toList());
            }
            
            bookingList = FXCollections.observableArrayList(searchResults);
            bookingTable.setItems(bookingList);
            
            statusMessageLabel.setText("Tìm thấy " + searchResults.size() + " lịch hẹn với SĐT: " + phone);
            
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tìm kiếm lịch hẹn",
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
            
            // Tải tất cả booking của ngày
            List<Booking> bookings = bookingRepository.getBookingsByDate(date);
            
            // Áp dụng bộ lọc trạng thái nếu có
            if (statusValue != null && !statusValue.equals("Tất cả")) {
                String statusCode = statusValue.split(" - ")[0];
                bookings = bookings.stream()
                        .filter(booking -> booking.getStatus().name().equals(statusCode))
                        .collect(java.util.stream.Collectors.toList());
            }
            
            bookingList = FXCollections.observableArrayList(bookings);
            bookingTable.setItems(bookingList);
            
            statusMessageLabel.setText("Đã lọc " + bookings.size() + " lịch hẹn");
            
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể lọc lịch hẹn",
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
            showAlert(AlertType.ERROR, "Lỗi", "Không thể lọc lịch hẹn sắp tới",
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
            showAlert(AlertType.ERROR, "Lỗi", "Không thể làm mới lịch hẹn",
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
            showAlert(AlertType.ERROR, "Lỗi", "Không thể xem lịch hẹn",
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
                showAlert(AlertType.ERROR, "Lỗi", "Phiên làm việc không hợp lệ",
                        "Không tìm thấy thông tin nhân viên. Vui lòng đăng nhập lại.");
                return;
            }

            if (!RoleChecker.hasPermission("CREATE_BOOKING")) {
                System.err.println("Người dùng không có quyền CREATE_BOOKING: " + currentStaff.getFullName());
                showAlert(AlertType.WARNING, "Cảnh báo", "Không có quyền",
                        "Bạn không có quyền tạo lịch hẹn mới. Vui lòng liên hệ quản trị viên để được cấp quyền.");
                return;
            }
            // Tạo cửa sổ tạo booking mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/NewBookingView.fxml"));
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
            
        } catch (Exception e) {
            System.err.println("Lỗi khi tạo lịch hẹn mới: " + e.getMessage());
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tạo booking mới",
                    "Đã xảy ra lỗi khi tạo booking mới: " + e.getMessage());
        }
    }

    /**
     * Xác nhận khách hàng đã đến
     */
    @FXML
    private void confirmArrival() {
        try {
            if (selectedBooking == null) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch hẹn",
                        "Vui lòng chọn một lịch hẹn để xác nhận");
                return;
            }
            
            if (selectedBooking.getStatus() != StatusEnum.PENDING) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Trạng thái không hợp lệ",
                        "Chỉ có thể xác nhận đến cho các lịch hẹn đang chờ xác nhận");
                return;
            }
            
            // Cập nhật trạng thái booking
            selectedBooking.setStatus(StatusEnum.CONFIRMED);
            int result = bookingRepository.update(selectedBooking);
            boolean success = result > 0; // Chuyển đổi kết quả int thành boolean
            
            if (success) {
                showAlert(AlertType.INFORMATION, "Thành công", "Xác nhận thành công",
                        "Đã xác nhận khách hàng đến cho lịch hẹn #" + selectedBooking.getBookingId());
                refreshBookings();
            } else {
                showAlert(AlertType.ERROR, "Lỗi", "Không thể xác nhận",
                        "Không thể cập nhật trạng thái cho lịch hẹn này");
            }
            
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể xác nhận đến",
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
                showAlert(AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch hẹn",
                        "Vui lòng chọn một lịch hẹn để bắt đầu dịch vụ");
                return;
            }
            
            if (selectedBooking.getStatus() != StatusEnum.CONFIRMED) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Trạng thái không hợp lệ",
                        "Chỉ có thể bắt đầu dịch vụ cho các lịch hẹn đã xác nhận");
                return;
            }
            
            // Lưu ghi chú nếu có
            String notes = notesArea.getText().trim();
            if (!notes.isEmpty()) {
                selectedBooking.setNote(notes);
                bookingRepository.update(selectedBooking);
            }
            
            showAlert(AlertType.INFORMATION, "Thành công", "Đã bắt đầu dịch vụ",
                    "Đã bắt đầu dịch vụ cho lịch hẹn #" + selectedBooking.getBookingId());
            
            // Kích hoạt nút hoàn thành
            completeButton.setDisable(false);
            
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể bắt đầu dịch vụ",
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
                showAlert(AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch hẹn",
                        "Vui lòng chọn một lịch hẹn để hoàn thành dịch vụ");
                return;
            }
            
            if (selectedBooking.getStatus() != StatusEnum.CONFIRMED) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Trạng thái không hợp lệ",
                        "Chỉ có thể hoàn thành dịch vụ cho các lịch hẹn đã xác nhận");
                return;
            }
            
            // Lưu ghi chú nếu có
            String notes = notesArea.getText().trim();
            if (!notes.isEmpty()) {
                selectedBooking.setNote(notes);
            }
            
            // Cập nhật trạng thái booking
            selectedBooking.setStatus(StatusEnum.COMPLETED);
            int result = bookingRepository.update(selectedBooking);
            boolean success = result > 0; // Chuyển đổi kết quả int thành boolean
            
            if (success) {
                showAlert(AlertType.INFORMATION, "Thành công", "Hoàn thành dịch vụ",
                        "Đã hoàn thành dịch vụ cho lịch hẹn #" + selectedBooking.getBookingId());
                refreshBookings();
                printInvoiceButton.setDisable(false);
            } else {
                showAlert(AlertType.ERROR, "Lỗi", "Không thể hoàn thành dịch vụ",
                        "Không thể cập nhật trạng thái cho lịch hẹn này");
            }
            
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể hoàn thành dịch vụ",
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
                showAlert(AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch hẹn",
                        "Vui lòng chọn một lịch hẹn để tạo hóa đơn");
                return;
            }
            
            if (selectedBooking.getStatus() != StatusEnum.COMPLETED) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Trạng thái không hợp lệ",
                        "Chỉ có thể tạo hóa đơn cho các lịch hẹn đã hoàn thành");
                return;
            }
            
            // Tạo cửa sổ tạo hóa đơn
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Staff/CreateInvoiceView.fxml"));
            Parent root = loader.load();
            
            // Truyền thông tin booking cho controller tạo hóa đơn
            controllers.Staff.CreateInvoiceController controller = loader.getController();
            controller.initData(selectedBooking);
            
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle("Tạo hóa đơn cho lịch hẹn #" + selectedBooking.getBookingId());
            modalStage.setScene(new Scene(root));
            
            // Hiển thị cửa sổ và chờ cho đến khi nó đóng
            modalStage.showAndWait();
            
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tạo hóa đơn",
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
                showAlert(AlertType.WARNING, "Cảnh báo", "Không có dữ liệu",
                        "Không có lịch hẹn nào để xuất");
                return;
            }
            
            // Mở hộp thoại chọn nơi lưu file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Lưu báo cáo lịch hẹn");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName("booking_report_" + 
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv");
            
            File file = fileChooser.showSaveDialog(bookingTable.getScene().getWindow());
            if (file != null) {
                // Tạo nội dung CSV
                StringBuilder csvContent = new StringBuilder();
                
                // Header
                csvContent.append("ID,Thời gian,Khách hàng,Số điện thoại,Thú cưng,Dịch vụ,Trạng thái,Nhân viên phụ trách\n");
                
                // Dữ liệu
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
                
                // Ghi file
                try (FileWriter fileWriter = new FileWriter(file)) {
                    fileWriter.write(csvContent.toString());
                }
                
                showAlert(AlertType.INFORMATION, "Thành công", "Xuất báo cáo thành công",
                        "Đã xuất báo cáo lịch hẹn vào file " + file.getAbsolutePath());
            }
            
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể xuất báo cáo",
                    "Đã xảy ra lỗi khi xuất báo cáo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hiển thị cửa sổ trợ giúp
     */
    @FXML
    private void showHelp() {
        Alert helpAlert = new Alert(AlertType.INFORMATION);
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
            // Sử dụng todayButton làm nguồn để chuyển scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Staff/MainStaffView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) todayButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi chuyển về màn hình chính: " + e.getMessage());
            
            // Nếu lỗi thì đóng luôn cửa sổ hiện tại
            Stage stage = (Stage) todayButton.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Chuyển về trang chủ
     */
    @FXML
    private void goToHome() {
    	SceneSwitcher.switchScene("staff/staff_home.fxml");
    }    /**
     * Hiển thị thông báo
     */
    private void showAlert(AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}