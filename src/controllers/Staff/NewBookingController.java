package controllers.Staff;

import enums.GenderEnum;
import enums.StatusEnum;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.*;
import repository.*;
import service.BookingService;
import service.PetService;
import utils.DatabaseConnection;
import utils.Session;
import utils.TransactionManager;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NewBookingController {

    @FXML
    private TextField phoneField;
    @FXML
    private TextField customerNameField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField emailField;
    @FXML
    private ComboBox<String> petComboBox;
    @FXML
    private ListView<Service> serviceListView;
    @FXML
    private DatePicker bookingDatePicker;
    @FXML
    private ComboBox<String> bookingTimeComboBox;
    @FXML
    private TextArea notesAreaNewBooking;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button viewHistoryButton;
    @FXML
    private ListView<String> bookedTimesListView;

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
    private PetService petService;
    
    // Thêm map để lưu trữ thông tin về Pet ID theo tên
    private Map<String, Integer> petIdMap = new HashMap<>();

    private final List<String> DEFAULT_TIME_SLOTS = IntStream.rangeClosed(8, 21)
            .boxed()
            .flatMap(hour -> List.of(
                String.format("%02d:00", hour), 
                String.format("%02d:30", hour)
            ).stream())
            .collect(Collectors.toList());

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
            this.petService = new PetService();
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
        bookingTimeComboBox.setItems(FXCollections.observableArrayList(DEFAULT_TIME_SLOTS));
        serviceListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        loadServices();

        if (bookingDatePicker != null) {
            bookingDatePicker.setValue(LocalDate.now());
            bookingDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
                if (newDate != null) {
                    updateBookedTimesList(newDate);
                    updateAvailableTimeSlots(newDate);
                }
            });
        }

        if (phoneField != null) {
            phoneField.textProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null && newValue.length() >= 10) {
                    searchCustomer(newValue);
                } else {
                    clearCustomerData();
                }
            });
        }

        bookedTimesListView.setItems(FXCollections.observableArrayList());
        viewHistoryButton.setDisable(true);

        if (bookingDatePicker.getValue() != null) {
            updateBookedTimesList(bookingDatePicker.getValue());
            updateAvailableTimeSlots(bookingDatePicker.getValue());
        }

        initializeCustomerFromSession();
        System.out.println("NewBookingController khởi tạo thành công");
    }

    private void initializeCustomerFromSession() {
        try {
            Integer selectedCustomerId = (Integer) Session.getInstance().getAttribute("selectedCustomerId");
            if (selectedCustomerId != null) {
                Customer customer = customerRepository.selectById(selectedCustomerId);
                if (customer != null) {
                    selectedCustomer = customer;
                    phoneField.setText(customer.getPhone());
                    customerNameField.setText(customer.getFullName());
                    addressField.setText(customer.getAddress());
                    emailField.setText(customer.getEmail());
                    loadPetsByCustomer(selectedCustomerId);
                    viewHistoryButton.setDisable(false);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thông tin khách hàng từ Session: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải thông tin khách hàng", 
                "Lỗi: " + e.getMessage());
        }
    }

    private void clearCustomerData() {
        selectedCustomer = null;
        customerNameField.clear();
        addressField.clear();
        emailField.clear();
        petComboBox.getItems().clear();
        petIdMap.clear(); // Xóa map
        viewHistoryButton.setDisable(true);
    }

 // Trong NewBookingController.java
 // Cải thiện phương thức hiển thị thời gian đã đặt
 private void updateBookedTimesList(LocalDate date) {
     try {
         List<Booking> bookings = bookingRepository.getBookingsByDate(date);
         List<String> bookedTimesInfo = new ArrayList<>();
         
         // Phân loại theo trạng thái
         Map<StatusEnum, String> statusColors = new HashMap<>();
         statusColors.put(StatusEnum.PENDING, "🟡");    // Màu vàng - chờ xác nhận
         statusColors.put(StatusEnum.CONFIRMED, "🟢");  // Màu xanh - đã xác nhận
         statusColors.put(StatusEnum.COMPLETED, "🔵");  // Màu xanh lam - hoàn thành
         statusColors.put(StatusEnum.CANCELLED, "🔴");  // Màu đỏ - đã hủy
         
         // Sắp xếp booking theo thời gian
         bookings.sort(Comparator.comparing(Booking::getBookingTime));
         
         for (Booking booking : bookings) {
             if (booking.getBookingTime() == null) continue;
             
             // Lấy thông tin dịch vụ
             String serviceName = getServiceNameFromBooking(booking);
             
             // Lấy thông tin khách hàng
             String customerInfo = booking.getCustomer() != null ? 
                 booking.getCustomer().getFullName() + 
                 " (" + booking.getCustomer().getPhone() + ")" : "Không rõ";
             
             // Lấy thông tin thú cưng
             String petInfo = booking.getPet() != null ? booking.getPet().getName() : "Không rõ";
             
             // Lấy trạng thái và icon tương ứng
             String statusIcon = statusColors.getOrDefault(booking.getStatus(), "⚪️");
             
             // Định dạng thời gian
             String timeInfo = booking.getBookingTime().format(DateTimeFormatter.ofPattern("HH:mm"));
             
             // Tạo chuỗi thông tin đầy đủ
             String bookingInfo = String.format("%s - %s - %s - %s %s - %s",
                 statusIcon,
                 timeInfo,
                 serviceName,
                 customerInfo,
                 booking.getStatus() != null ? "(" + booking.getStatus() + ")" : "",
                 petInfo
             );
             
             bookedTimesInfo.add(bookingInfo);
         }
         
         // Thêm chú thích màu
         if (!bookedTimesInfo.isEmpty()) {
             bookedTimesInfo.add(0, "--- Chú thích: 🟡 Chờ xác nhận | 🟢 Đã xác nhận | 🔵 Hoàn thành | 🔴 Đã hủy ---");
         } else {
             bookedTimesInfo.add("Không có lịch hẹn nào vào ngày này");
         }
                 
         bookedTimesListView.setItems(FXCollections.observableArrayList(bookedTimesInfo));
         
         // Tùy chỉnh hiển thị với CSS
         bookedTimesListView.setCellFactory(listView -> new ListCell<String>() {
             @Override
             protected void updateItem(String item, boolean empty) {
                 super.updateItem(item, empty);
                 if (empty || item == null) {
                     setText(null);
                     setStyle("");
                 } else {
                     setText(item);
                     if (item.startsWith("---")) {
                         // Style cho dòng chú thích
                         setStyle("-fx-font-style: italic; -fx-font-size: 11; -fx-text-fill: #666;");
                     } else if (item.contains("(PENDING)")) {
                         // Style cho trạng thái chờ xác nhận
                         setStyle("-fx-background-color: rgba(255, 255, 224, 0.3);");
                     } else if (item.contains("(CONFIRMED)")) {
                         // Style cho trạng thái đã xác nhận
                         setStyle("-fx-background-color: rgba(200, 255, 200, 0.3);");
                     } else if (item.contains("(COMPLETED)")) {
                         // Style cho trạng thái hoàn thành
                         setStyle("-fx-background-color: rgba(200, 200, 255, 0.3);");
                     } else if (item.contains("(CANCELLED)")) {
                         // Style cho trạng thái đã hủy
                         setStyle("-fx-background-color: rgba(255, 200, 200, 0.3);");
                     }
                 }
             }
         });
     } catch (Exception e) {
         System.err.println("Lỗi khi tải danh sách lịch hẹn trong ngày: " + e.getMessage());
         e.printStackTrace();
     }
 }
 private void updateAvailableTimeSlots(LocalDate date) {
	    try {
	        // Giờ làm việc mặc định từ 8:00 đến 21:30 (30 phút mỗi slot)
	        final List<String> DEFAULT_TIME_SLOTS = IntStream.rangeClosed(8, 21)
	            .boxed()
	            .flatMap(hour -> List.of(
	                String.format("%02d:00", hour), 
	                String.format("%02d:30", hour)
	            ).stream())
	            .collect(Collectors.toList());
	            
	        // Kiểm tra ngày đã qua hay chưa
	        LocalDate today = LocalDate.now();
	        LocalTime currentTime = LocalTime.now();
	        
	        // Map lưu các time slots và trạng thái (true = available, false = booked)
	        Map<String, Boolean> timeSlotAvailability = new LinkedHashMap<>();
	        
	        // Khởi tạo tất cả các slots là available
	        for (String timeSlot : DEFAULT_TIME_SLOTS) {
	            timeSlotAvailability.put(timeSlot, true);
	        }
	        
	        // Xử lý ngày và thời gian đã qua
	        if (date.isBefore(today)) {
	            // Ngày quá khứ: không có slot nào khả dụng
	            timeSlotAvailability.replaceAll((slot, available) -> false);
	        } else if (date.isEqual(today)) {
	            // Ngày hiện tại: vô hiệu hóa các slot đã qua
	            for (String timeSlot : DEFAULT_TIME_SLOTS) {
	                try {
	                    LocalTime slotTime = LocalTime.parse(timeSlot, DateTimeFormatter.ofPattern("HH:mm"));
	                    // Vô hiệu các slot đã qua hoặc sắp tới (trong vòng 30 phút)
	                    if (slotTime.isBefore(currentTime.plusMinutes(30))) {
	                        timeSlotAvailability.put(timeSlot, false);
	                    }
	                } catch (Exception e) {
	                    System.err.println("Lỗi khi phân tích thời gian: " + e.getMessage());
	                    // Đánh dấu không khả dụng nếu có lỗi
	                    timeSlotAvailability.put(timeSlot, false);
	                }
	            }
	        }
	        
	        // Lấy danh sách booking trong ngày
	        String sql = 
	            "SELECT b.booking_id, b.booking_time, b.staff_id, " +
	            "(SELECT COALESCE(SUM(s.duration_minutes), 60) FROM booking_detail bd " +
	            " JOIN service s ON bd.service_id = s.service_id WHERE bd.booking_id = b.booking_id) AS total_duration " +
	            "FROM booking b " +
	            "WHERE DATE(b.booking_time) = ? " +
	            "AND b.status NOT IN ('CANCELLED', 'COMPLETED')";
	        
	        try (Connection conn = DatabaseConnection.getConnection();
	             PreparedStatement stmt = conn.prepareStatement(sql)) {
	            
	            stmt.setDate(1, java.sql.Date.valueOf(date));
	            
	            try (ResultSet rs = stmt.executeQuery()) {
	                while (rs.next()) {
	                    int bookingId = rs.getInt("booking_id");
	                    LocalDateTime startDateTime = rs.getTimestamp("booking_time").toLocalDateTime();
	                    LocalTime startTime = startDateTime.toLocalTime();
	                    
	                    // Lấy thời lượng dịch vụ
	                    int duration = rs.getInt("total_duration");
	                    if (rs.wasNull() || duration <= 0) {
	                        duration = 60; // Mặc định 60 phút
	                    }
	                    
	                    // Thêm buffer 15 phút
	                    duration += 15;
	                    
	                    // Tính thời gian kết thúc
	                    LocalTime endTime = startTime.plusMinutes(duration);
	                    
	                    // Vô hiệu hóa các slots trong khoảng này
	                    for (String timeSlot : timeSlotAvailability.keySet()) {
	                        try {
	                            LocalTime slotTime = LocalTime.parse(timeSlot, DateTimeFormatter.ofPattern("HH:mm"));
	                            
	                            // Một slot bị chiếm nếu thời gian của nó nằm giữa thời gian bắt đầu và kết thúc của một booking
	                            if ((slotTime.equals(startTime) || slotTime.isAfter(startTime)) && 
	                                slotTime.isBefore(endTime)) {
	                                timeSlotAvailability.put(timeSlot, false);
	                            }
	                            
	                            // Hoặc nếu thời gian kết thúc của slot (30 phút sau) nằm trong khoảng thời gian của booking
	                            LocalTime slotEndTime = slotTime.plusMinutes(30);
	                            if (slotTime.isBefore(startTime) && slotEndTime.isAfter(startTime)) {
	                                timeSlotAvailability.put(timeSlot, false);
	                            }
	                        } catch (Exception e) {
	                            System.err.println("Lỗi khi xử lý slot " + timeSlot + ": " + e.getMessage());
	                            // Đánh dấu không khả dụng nếu có lỗi
	                            timeSlotAvailability.put(timeSlot, false);
	                        }
	                    }
	                }
	            }
	        } catch (Exception e) {
	            System.err.println("Lỗi khi truy vấn booking: " + e.getMessage());
	            e.printStackTrace();
	        }
	        
	        // Lọc các slot còn trống
	        List<String> availableSlots = timeSlotAvailability.entrySet().stream()
	            .filter(Map.Entry::getValue)
	            .map(Map.Entry::getKey)
	            .collect(Collectors.toList());
	        
	        // Cập nhật ComboBox
	        Platform.runLater(() -> {
	            ObservableList<String> items = FXCollections.observableArrayList(availableSlots);
	            bookingTimeComboBox.setItems(items);
	            
	            // Hiển thị thông báo nếu không có slot nào
	            if (items.isEmpty()) {
	                bookingTimeComboBox.setPromptText("Không có khung giờ nào khả dụng");
	                bookingTimeComboBox.setValue(null);
	            } else {
	                bookingTimeComboBox.setPromptText("Chọn thời gian");
	                bookingTimeComboBox.setValue(items.get(0)); // Chọn mặc định slot đầu tiên
	            }
	        });
	    } catch (Exception e) {
	        Platform.runLater(() -> {
	            System.err.println("Lỗi khi cập nhật khung giờ khả dụng: " + e.getMessage());
	            e.printStackTrace();
	            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật khung giờ", 
	                "Đã xảy ra lỗi: " + e.getMessage());
	            
	            // Đặt một giá trị mặc định để tránh lỗi
	            bookingTimeComboBox.setItems(FXCollections.observableArrayList());
	            bookingTimeComboBox.setPromptText("Không có khung giờ khả dụng");
	        });
	    }
	}
/**
 * Hiển thị cửa sổ trợ giúp
 */
			@FXML
			private void showHelp() {
			    Alert helpAlert = new Alert(Alert.AlertType.INFORMATION);
			    helpAlert.setTitle("Trợ giúp");
			    helpAlert.setHeaderText("Hướng dẫn sử dụng màn hình Đặt lịch hẹn mới");
			    helpAlert.setContentText(
			        "1. Nhập thông tin khách hàng:\n" +
			        "   - Nhập số điện thoại để tìm khách hàng hiện có hoặc tạo mới.\n" +
			        "   - Điền tên, địa chỉ, email (địa chỉ và email là tùy chọn).\n" +
			        "   - Nhấn 'Lịch sử' để xem lịch sử đặt lịch của khách hàng.\n\n" +
			        "2. Chọn thú cưng:\n" +
			        "   - Chọn thú cưng từ danh sách hoặc nhập tên mới.\n\n" +
			        "3. Chọn dịch vụ:\n" +
			        "   - Chọn một hoặc nhiều dịch vụ từ danh sách (giữ Ctrl để chọn nhiều).\n\n" +
			        "4. Chọn thời gian:\n" +
			        "   - Chọn ngày hẹn từ DatePicker.\n" +
			        "   - Chọn khung giờ còn trống từ ComboBox.\n" +
			        "   - Kiểm tra danh sách thời gian đã đặt để tránh xung đột.\n\n" +
			        "5. Ghi chú:\n" +
			        "   - Nhập ghi chú nếu cần (tùy chọn, tối đa 500 ký tự).\n\n" +
			        "6. Lưu hoặc hủy:\n" +
			        "   - Nhấn 'Lưu lịch hẹn' để tạo lịch hẹn mới.\n" +
			        "   - Nhấn 'Hủy bỏ' để đóng cửa sổ mà không lưu."
			    );
			    helpAlert.getDialogPane().setPrefWidth(500);
			    helpAlert.showAndWait();
			}


    private int getServiceDurationFromBooking(Booking booking) {
        if (booking == null) return 60; // Mặc định 60 phút nếu không có booking
        
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT SUM(s.duration_minutes) as total_duration " +
                       "FROM booking_detail bd " +
                       "JOIN service s ON bd.service_id = s.service_id " +
                       "WHERE bd.booking_id = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, booking.getBookingId());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int duration = rs.getInt("total_duration");
                        if (rs.wasNull() || duration <= 0) return 60; // Mặc định 60 phút nếu không có dữ liệu
                        return duration;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy thời lượng dịch vụ: " + e.getMessage());
        }
        return 60; // Mặc định 60 phút nếu có lỗi
    }

    private void loadServices() {
        try {
            List<Service> services = serviceRepository.selectAll().stream()
                .filter(Service::isActive) // Chỉ lấy các dịch vụ đang hoạt động
                .distinct()
                .sorted(Comparator.comparing(Service::getName))
                .collect(Collectors.toList());
                
            serviceListView.getItems().clear();
            serviceListView.getItems().addAll(services);
            
            serviceListView.setCellFactory(listView -> new ListCell<Service>() {
                @Override
                protected void updateItem(Service service, boolean empty) {
                    super.updateItem(service, empty);
                    if (empty || service == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(String.format("%s (%d phút, %,.0f VND)", 
                            service.getName(), 
                            service.getDurationMinutes(), 
                            service.getPrice()));
                        setStyle("-fx-text-fill: black;");
                    }
                }
            });
            
            if (services.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có dịch vụ khả dụng",
                        "Hiện tại không có dịch vụ nào đang hoạt động.");
            } else {
                serviceListView.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách dịch vụ", 
                "Lỗi: " + e.getMessage());
        }
    }

    private void loadPetsByCustomer(int customerId) {
        try {
            List<Pet> pets = petService.findPetsByCustomerId(customerId);
            
            // Xóa map hiện tại
            petIdMap.clear();
            
            // Tạo danh sách tên và cập nhật map petId
            List<String> petNames = new ArrayList<>();
            for (Pet pet : pets) {
                String petName = pet.getName();
                petNames.add(petName);
                petIdMap.put(petName, pet.getPetId());
            }
                
            petComboBox.setItems(FXCollections.observableArrayList(petNames));
            if (!petNames.isEmpty()) {
                petComboBox.setValue(petNames.get(0));
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải danh sách thú cưng của khách hàng: " + e.getMessage());
            e.printStackTrace();
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
                loadPetsByCustomer(customer.getId());
                viewHistoryButton.setDisable(false);
            } else {
                clearCustomerData();
                petComboBox.setPromptText("Vui lòng tạo khách hàng mới trước");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tìm kiếm khách hàng", "Lỗi: " + e.getMessage());
        }
    }

    private boolean checkBookingConflict(LocalDateTime bookingTime, int totalDuration) {
        try {
            // Tính thời gian kết thúc dự kiến của booking mới (bao gồm buffer 15 phút)
            LocalDateTime endTime = bookingTime.plusMinutes(totalDuration + 15);
            
            // Truy vấn đơn giản, chỉ lấy tất cả booking trong ngày
            String sql = 
                "SELECT b.booking_id, b.booking_time, cp.full_name AS customer_name, p.name AS pet_name, " +
                "(SELECT GROUP_CONCAT(s.name SEPARATOR ', ') FROM booking_detail bd " +
                " JOIN service s ON bd.service_id = s.service_id WHERE bd.booking_id = b.booking_id) AS service_names, " +
                "(SELECT COALESCE(SUM(s.duration_minutes), 60) FROM booking_detail bd " +
                " JOIN service s ON bd.service_id = s.service_id WHERE bd.booking_id = b.booking_id) AS total_duration " +
                "FROM booking b " +
                "JOIN customer c ON b.customer_id = c.customer_id " +
                "JOIN person cp ON c.customer_id = cp.person_id " +
                "JOIN pet p ON b.pet_id = p.pet_id " +
                "WHERE DATE(b.booking_time) = ? " +
                "AND b.status NOT IN ('CANCELLED', 'COMPLETED')";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setDate(1, java.sql.Date.valueOf(bookingTime.toLocalDate()));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        LocalDateTime existingStart = rs.getTimestamp("booking_time").toLocalDateTime();
                        
                        // Lấy thời lượng dịch vụ, mặc định 60 phút nếu null hoặc 0
                        int duration = rs.getInt("total_duration");
                        if (rs.wasNull() || duration <= 0) {
                            duration = 60;
                        }
                        
                        // Tính thời gian kết thúc với buffer 15 phút
                        LocalDateTime existingEnd = existingStart.plusMinutes(duration + 15);
                        
                        // Kiểm tra xung đột
                        if (bookingTime.isBefore(existingEnd) && endTime.isAfter(existingStart)) {
                            String conflictInfo = String.format(
                                "Đã có lịch hẹn vào khung giờ này:\n" +
                                "Khách hàng: %s\n" +
                                "Thú cưng: %s\n" +
                                "Dịch vụ: %s\n" +
                                "Thời gian: %s",
                                rs.getString("customer_name"),
                                rs.getString("pet_name"),
                                rs.getString("service_names"),
                                existingStart.format(DateTimeFormatter.ofPattern("HH:mm"))
                            );
                            
                            showAlert(Alert.AlertType.WARNING, "Xung đột lịch hẹn", 
                                "Không thể đặt lịch vào thời gian này", conflictInfo);
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể kiểm tra xung đột lịch hẹn",
                    "Lỗi SQL: " + e.getMessage());
            e.printStackTrace();
            return true; // Giả định có xung đột nếu không kiểm tra được
        }
    }

    @FXML
    public void viewCustomerHistory() {
        if (selectedCustomer == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có khách hàng",
                    "Vui lòng chọn một khách hàng để xem lịch sử.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/CustomerHistoryView.fxml"));
            Parent root = loader.load();
            
            CustomerHistoryController controller = loader.getController();
            controller.initData(selectedCustomer.getId());
            
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle("Lịch sử khách hàng: " + selectedCustomer.getFullName());
            modalStage.setScene(new Scene(root));
            modalStage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở cửa sổ lịch sử",
                    "Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private class ValidationResult {
        boolean success;
        String message;
        
        ValidationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    private ValidationResult validateInputData() {
        // Kiểm tra số điện thoại
        String phoneNumber = phoneField.getText().trim();
        if (!phoneNumber.matches("^0\\d{9}$")) {
            return new ValidationResult(false, "Số điện thoại phải bắt đầu bằng 0 và có đúng 10 chữ số.");
        }

        // Kiểm tra tên khách hàng
        String customerName = customerNameField.getText().trim();
        if (customerName.isEmpty()) {
            return new ValidationResult(false, "Tên khách hàng không được để trống.");
        }
        if (customerName.length() > 100) {
            return new ValidationResult(false, "Tên khách hàng không được quá 100 ký tự.");
        }
        if (!customerName.matches("^[a-zA-ZÀ-ỹ\\s]+$")) {
            return new ValidationResult(false, "Tên khách hàng phải chứa chữ cái và khoảng trắng.");
        }

        // Kiểm tra địa chỉ
        String address = addressField.getText().trim();
        if (address.isEmpty()) {
            return new ValidationResult(false, "Địa chỉ không được để trống.");
        }
        if (address.length() > 200) {
            return new ValidationResult(false, "Địa chỉ không được quá 200 ký tự.");
        }

        // Kiểm tra email
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return new ValidationResult(false, "Email không đúng định dạng.");
        }

        // Kiểm tra thú cưng
        String petName = petComboBox.getValue();
        if (petName == null || petName.trim().isEmpty()) {
            return new ValidationResult(false, "Vui lòng chọn hoặc nhập tên thú cưng.");
        }
        if (petName.length() > 50) {
            return new ValidationResult(false, "Tên thú cưng không được quá 50 ký tự.");
        }

        // Kiểm tra dịch vụ
        ObservableList<Service> selectedServices = serviceListView.getSelectionModel().getSelectedItems();
        if (selectedServices.isEmpty()) {
            return new ValidationResult(false, "Vui lòng chọn ít nhất một dịch vụ.");
        }
        
        // Kiểm tra các dịch vụ đã chọn có đang hoạt động không
        for (Service service : selectedServices) {
            if (!service.isActive()) {
                return new ValidationResult(false, 
                    "Dịch vụ '" + service.getName() + "' đã ngừng hoạt động. Vui lòng chọn dịch vụ khác.");
            }
        }

        // Kiểm tra ngày đặt lịch
        LocalDate bookingDate = bookingDatePicker.getValue();
        if (bookingDate == null) {
            return new ValidationResult(false, "Vui lòng chọn ngày hẹn.");
        }
        
        LocalDate today = LocalDate.now();
        if (bookingDate.isBefore(today)) {
            return new ValidationResult(false, "Không thể đặt lịch cho ngày trong quá khứ.");
        }
        
        if (bookingDate.isAfter(today.plusMonths(3))) {
            return new ValidationResult(false, "Không thể đặt lịch xa quá 3 tháng từ hiện tại.");
        }

        // Kiểm tra giờ đặt lịch
        String timeStr = bookingTimeComboBox.getValue();
        if (timeStr == null || timeStr.isEmpty()) {
            return new ValidationResult(false, "Vui lòng chọn giờ hẹn.");
        }

        // Kiểm tra giờ có hợp lệ không
        LocalDateTime bookingTime;
        try {
            LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
            bookingTime = LocalDateTime.of(bookingDate, time);
            
            if (bookingTime.isBefore(LocalDateTime.now())) {
                return new ValidationResult(false, "Thời gian đặt lịch phải là thời gian trong tương lai.");
            }
            
            if (time.isBefore(LocalTime.of(8, 0)) || time.isAfter(LocalTime.of(21, 0))) {
                return new ValidationResult(false, "Giờ hẹn phải từ 8:00 đến 21:00.");
            }
        } catch (DateTimeParseException e) {
            return new ValidationResult(false, "Giờ hẹn không hợp lệ.");
        }

        // Kiểm tra xung đột lịch hẹn
        int totalDuration = selectedServices.stream()
            .mapToInt(Service::getDurationMinutes)
            .sum();
            
        if (totalDuration <= 0) totalDuration = 60; // Đặt mặc định là 60 phút nếu không có thông tin
            
        try {
            LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime bookingDateTime = LocalDateTime.of(bookingDate, time);
            
            if (checkBookingConflict(bookingDateTime, totalDuration)) {
                return new ValidationResult(false, "Thời gian này đã có lịch hẹn khác. Vui lòng chọn thời gian khác.");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi kiểm tra xung đột lịch hẹn: " + e.getMessage());
            return new ValidationResult(false, "Không thể kiểm tra xung đột lịch hẹn: " + e.getMessage());
        }

        // Kiểm tra ghi chú
        String notes = notesAreaNewBooking.getText().trim();
        if (notes.length() > 500) {
            return new ValidationResult(false, "Ghi chú không được quá 500 ký tự.");
        }

        return new ValidationResult(true, "");
    }

    private boolean isTimeSlotBooked(int staffId, LocalDateTime bookingTime, int totalDuration) throws Exception {
        try {
            // Tính thời gian kết thúc dự kiến của booking mới (bao gồm buffer 15 phút)
            LocalDateTime endTime = bookingTime.plusMinutes(totalDuration + 15);
            
            // Truy vấn đơn giản, chỉ lấy tất cả booking trong ngày
            String sql = 
                "SELECT b.booking_id, b.booking_time, " +
                "(SELECT COALESCE(SUM(s.duration_minutes), 60) FROM booking_detail bd " +
                " JOIN service s ON bd.service_id = s.service_id WHERE bd.booking_id = b.booking_id) AS total_duration " +
                "FROM booking b " +
                "WHERE DATE(b.booking_time) = ? " +
                "AND b.staff_id = ? " +
                "AND b.status NOT IN ('CANCELLED', 'COMPLETED')";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setDate(1, java.sql.Date.valueOf(bookingTime.toLocalDate()));
                stmt.setInt(2, staffId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        LocalDateTime existingStart = rs.getTimestamp("booking_time").toLocalDateTime();
                        
                        // Lấy thời lượng dịch vụ, mặc định 60 phút nếu null hoặc 0
                        int duration = rs.getInt("total_duration");
                        if (rs.wasNull() || duration <= 0) {
                            duration = 60;
                        }
                        
                        // Tính thời gian kết thúc với buffer 15 phút
                        LocalDateTime existingEnd = existingStart.plusMinutes(duration + 15);
                        
                        // Kiểm tra xung đột
                        // Xung đột xảy ra khi:
                        // 1. Lịch mới bắt đầu trong khoảng thời gian của lịch hiện có
                        // 2. Lịch mới kết thúc trong khoảng thời gian của lịch hiện có
                        // 3. Lịch mới bao trùm lịch hiện có
                        if (
                            (bookingTime.isEqual(existingStart) || 
                             (bookingTime.isAfter(existingStart) && bookingTime.isBefore(existingEnd))) ||
                            (endTime.isAfter(existingStart) && endTime.isBefore(existingEnd)) ||
                            (bookingTime.isBefore(existingStart) && endTime.isAfter(existingEnd))
                        ) {
                            int bookingId = rs.getInt("booking_id");
                            System.out.println("Phát hiện xung đột với booking ID: " + bookingId + 
                                               ", thời gian: " + existingStart + " đến " + existingEnd);
                            return true; // Có xung đột
                        }
                    }
                }
            }
            return false; // Không có xung đột
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra xung đột lịch hẹn: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Không thể kiểm tra xung đột lịch hẹn: " + e.getMessage());
        }
    }
    
    
    @FXML
    private void saveBooking() {
        try {
            // Kiểm tra nhân viên hiện tại
            Staff currentStaff = Session.getCurrentStaff();
            if (currentStaff == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Phiên đăng nhập không hợp lệ",
                        "Vui lòng đăng nhập lại.");
                closeNewBookingWindow();
                return;
            }

            // Kiểm tra dữ liệu đầu vào
            ValidationResult validationResult = validateInputData();
            if (!validationResult.success) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Dữ liệu không hợp lệ", 
                    validationResult.message);
                return;
            }

            // Lấy thông tin từ form
            String phoneNumber = phoneField.getText().trim();
            String customerName = customerNameField.getText().trim();
            String address = addressField.getText().trim();
            String email = emailField.getText().trim();
            String petName = petComboBox.getValue().trim();
            ObservableList<Service> selectedServices = serviceListView.getSelectionModel().getSelectedItems();
            LocalDate bookingDate = bookingDatePicker.getValue();
            LocalTime time = LocalTime.parse(bookingTimeComboBox.getValue(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime bookingTime = LocalDateTime.of(bookingDate, time);
            String notes = notesAreaNewBooking.getText().trim();
            // Tính tổng tiền
            double totalAmount = selectedServices.stream()
                .mapToDouble(Service::getPrice)
                .sum();

            // Tính tổng thời lượng dịch vụ
            int totalDuration = selectedServices.stream()
                .mapToInt(Service::getDurationMinutes)
                .sum();
                    
            // Kiểm tra xung đột lịch trình
            if (isTimeSlotBooked(currentStaff.getId(), bookingTime, totalDuration)) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Xung đột lịch trình",
                        "Thời gian này đã có lịch hẹn khác. Vui lòng chọn thời gian khác.");
                return;
            }

            // Tạo khách hàng mới hoặc cập nhật thông tin khách hàng hiện có
            if (selectedCustomer == null) {
                createNewCustomer(customerName, phoneNumber, address, email);
            } else {
                updateExistingCustomer(customerName, address, email);
            }

            // Tìm hoặc tạo thú cưng
            Pet selectedPet = findOrCreatePet(petName);
            
            // Tạo booking và các thông tin liên quan trong một transaction
            Connection connection = null;
            boolean success = false;
            
            try {
                connection = DatabaseConnection.getConnection();
                connection.setAutoCommit(false);
                
                // 1. Tạo booking
                Booking newBooking = new Booking();
                newBooking.setCustomer(selectedCustomer);
                newBooking.setPet(selectedPet);
                newBooking.setStaff(currentStaff);
                newBooking.setBookingTime(bookingTime);
                newBooking.setStatus(StatusEnum.PENDING);
                newBooking.setNote(notes);
                
                int bookingId = createBookingInTransaction(connection, newBooking);
                if (bookingId <= 0) {
                    throw new SQLException("Không thể tạo booking");
                }
                
                // Lấy booking đã tạo
                newBooking.setBookingId(bookingId);
                
                // 2. Thêm chi tiết booking (dịch vụ)
                for (Service service : selectedServices) {
                    createBookingDetailInTransaction(connection, bookingId, service);
                }
                
                // 3. Tạo order
                int orderId = createOrderInTransaction(connection, selectedCustomer, currentStaff, totalAmount);
                if (orderId <= 0) {
                    throw new SQLException("Không thể tạo order");
                }
                
                // 4. Thêm chi tiết order
                for (Service service : selectedServices) {
                    createOrderDetailInTransaction(connection, orderId, service);
                }
                
                // 5. Tạo hóa đơn (invoice)
                createInvoiceInTransaction(connection, orderId, currentStaff, totalAmount, bookingTime); // Truyền bookingTime
                
                // Hoàn thành transaction
                connection.commit();
                success = true;
                
                // Thông báo thành công
                String serviceNames = selectedServices.stream()
                    .map(Service::getName)
                    .collect(Collectors.joining(", "));
                    
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Tạo lịch hẹn thành công",
                    String.format("Đã tạo lịch hẹn cho: %s\nThú cưng: %s\nDịch vụ: %s\nThời gian: %s",
                        selectedCustomer.getFullName(),
                        petName,
                        serviceNames,
                        bookingTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
                
                // Cập nhật lại danh sách hóa đơn nếu có controller
                if (invoiceViewController != null) {
                    Platform.runLater(() -> {
                        invoiceViewController.setFromDatePickerToSpecificDate(bookingDate); // Sử dụng phương thức mới
                        invoiceViewController.loadInvoices();
                    });
                }
                
                // Đóng cửa sổ tạo booking
                closeNewBookingWindow();
                
            } catch (Exception e) {
                if (connection != null) {
                    try {
                        connection.rollback();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi tạo lịch hẹn",
                    "Đã xảy ra lỗi: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    try {
                        connection.setAutoCommit(true);
                        connection.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi không xác định",
                    "Đã xảy ra lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hoàn tất quá trình tạo lịch hẹn, lưu lịch hẹn và cập nhật các thành phần liên quan.
     * @param newBooking Đối tượng lịch hẹn cần được hoàn tất.
     */
    public void finishBookingCreation(Booking newBooking) {
        try {
            // Kiểm tra dữ liệu đầu vào
            ValidationResult validationResult = validateInputData();
            if (!validationResult.success) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Dữ liệu không hợp lệ", 
                    validationResult.message);
                return;
            }

            // Đảm bảo nhân viên hiện tại hợp lệ
            Staff currentStaff = Session.getCurrentStaff();
            if (currentStaff == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Phiên đăng nhập không hợp lệ",
                        "Vui lòng đăng nhập lại.");
                closeNewBookingWindow();
                return;
            }

            // Chuẩn bị chi tiết lịch hẹn
            ObservableList<Service> selectedServices = serviceListView.getSelectionModel().getSelectedItems();
            String petName = petComboBox.getValue().trim();
            LocalDate bookingDate = bookingDatePicker.getValue();
            LocalTime time = LocalTime.parse(bookingTimeComboBox.getValue(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime bookingTime = LocalDateTime.of(bookingDate, time);
            String notes = notesAreaNewBooking.getText().trim();

            // Thiết lập thuộc tính lịch hẹn
            newBooking.setStaff(currentStaff);
            newBooking.setBookingTime(bookingTime);
            newBooking.setStatus(StatusEnum.PENDING);
            newBooking.setNote(notes);

            // Tính tổng thời lượng và số tiền
            int totalDuration = selectedServices.stream()
                .mapToInt(Service::getDurationMinutes)
                .sum();
            double totalAmount = selectedServices.stream()
                .mapToDouble(Service::getPrice)
                .sum();

            // Kiểm tra xung đột lịch trình
            if (isTimeSlotBooked(currentStaff.getId(), bookingTime, totalDuration)) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Xung đột lịch trình",
                        "Thời gian này đã có lịch hẹn khác. Vui lòng chọn thời gian khác.");
                return;
            }

            // Tạo hoặc cập nhật khách hàng
            String phoneNumber = phoneField.getText().trim();
            String customerName = customerNameField.getText().trim();
            String address = addressField.getText().trim();
            String email = emailField.getText().trim();

            if (selectedCustomer == null) {
                createNewCustomer(customerName, phoneNumber, address, email);
            } else {
                updateExistingCustomer(customerName, address, email);
            }

            // Thiết lập khách hàng cho lịch hẹn
            newBooking.setCustomer(selectedCustomer);

            // Tìm hoặc tạo thú cưng
            Pet selectedPet = findOrCreatePet(petName);
            newBooking.setPet(selectedPet);

            // Lưu lịch hẹn và các thực thể liên quan trong một giao dịch
            Connection connection = null;
            boolean success = false;

            try {
                connection = DatabaseConnection.getConnection();
                connection.setAutoCommit(false);

                // Tạo lịch hẹn
                int bookingId = createBookingInTransaction(connection, newBooking);
                if (bookingId <= 0) {
                    throw new SQLException("Không thể tạo lịch hẹn");
                }
                newBooking.setBookingId(bookingId);

                // Thêm chi tiết lịch hẹn (dịch vụ)
                for (Service service : selectedServices) {
                    createBookingDetailInTransaction(connection, bookingId, service);
                }

                // Tạo đơn hàng
                int orderId = createOrderInTransaction(connection, selectedCustomer, currentStaff, totalAmount);
                if (orderId <= 0) {
                    throw new SQLException("Không thể tạo đơn hàng");
                }

                // Thêm chi tiết đơn hàng
                for (Service service : selectedServices) {
                    createOrderDetailInTransaction(connection, orderId, service);
                }

                // Tạo hóa đơn
                createInvoiceInTransaction(connection, orderId, currentStaff, totalAmount, bookingTime); // Truyền bookingTime

                // Hoàn tất giao dịch
                connection.commit();
                success = true;

                // Thông báo thành công
                String serviceNames = selectedServices.stream()
                    .map(Service::getName)
                    .collect(Collectors.joining(", "));
                    
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Tạo lịch hẹn thành công",
                    String.format("Đã tạo lịch hẹn cho: %s\nThú cưng: %s\nDịch vụ: %s\nThời gian: %s",
                        selectedCustomer.getFullName(),
                        petName,
                        serviceNames,
                        bookingTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

                // Cập nhật InvoiceViewController nếu có
                if (invoiceViewController != null) {
                    Platform.runLater(() -> {
                        invoiceViewController.setFromDatePickerToSpecificDate(bookingDate); // Sử dụng phương thức mới
                        invoiceViewController.loadInvoices();
                    });
                }

                // Đóng cửa sổ tạo lịch hẹn
                closeNewBookingWindow();

            } catch (Exception e) {
                if (connection != null) {
                    try {
                        connection.rollback();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi tạo lịch hẹn",
                    "Đã xảy ra lỗi: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    try {
                        connection.setAutoCommit(true);
                        connection.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi không xác định",
                    "Đã xảy ra lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createNewCustomer(String name, String phone, String address, String email) throws SQLException {
        selectedCustomer = new Customer();
        selectedCustomer.setFullName(name);
        selectedCustomer.setPhone(phone);
        selectedCustomer.setAddress(address);
        selectedCustomer.setEmail(email);
        selectedCustomer.setPoint(0);
        selectedCustomer.setGender(GenderEnum.OTHER);

        if (customerRepository.insert(selectedCustomer) <= 0) {
            throw new SQLException("Không thể tạo khách hàng mới.");
        }
    }

    private void updateExistingCustomer(String name, String address, String email) throws SQLException {
        // Chỉ cập nhật những thông tin thay đổi
        boolean needUpdate = false;
        
        if (!selectedCustomer.getFullName().equals(name)) {
            selectedCustomer.setFullName(name);
            needUpdate = true;
        }
        
        if (!Objects.equals(selectedCustomer.getAddress(), address)) {
            selectedCustomer.setAddress(address);
            needUpdate = true;
        }
        
        if (!Objects.equals(selectedCustomer.getEmail(), email)) {
            selectedCustomer.setEmail(email);
            needUpdate = true;
        }
        
        if (needUpdate && customerRepository.update(selectedCustomer) <= 0) {
            throw new SQLException("Không thể cập nhật thông tin khách hàng.");
        }
    }

    private Pet findOrCreatePet(String petName) throws SQLException {
        // Kiểm tra xem thú cưng đã tồn tại trong map chưa
        Integer petId = petIdMap.get(petName);
        if (petId != null) {
            // Nếu đã có trong map, lấy từ repository
            try {
                Pet pet = petRepository.selectById(petId);
                if (pet != null) {
                    return pet;
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi tìm thú cưng theo ID: " + e.getMessage());
            }
        }
        
        // Tìm thú cưng theo tên và ID khách hàng
        try {
            String condition = "p.customer_id = ? AND p.name = ?";
            List<Pet> existingPets = petRepository.selectByCondition(condition, selectedCustomer.getId(), petName);
            
            if (!existingPets.isEmpty()) {
                Pet pet = existingPets.get(0);
                // Lưu vào map để sử dụng sau này
                petIdMap.put(petName, pet.getPetId());
                return pet;
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm thú cưng theo tên: " + e.getMessage());
        }
        
        // Nếu không tìm thấy, tạo thú cưng mới
        try {
            // Lấy loại thú cưng mặc định (hoặc cho người dùng chọn)
            List<PetType> petTypes = petTypeRepository.selectAll();
            if (petTypes.isEmpty()) {
                throw new SQLException("Không có loại thú cưng nào trong hệ thống.");
            }
            
            PetType defaultType = petTypes.get(0);
            
            Pet newPet = new Pet();
            newPet.setName(petName);
            newPet.setGender(GenderEnum.MALE); // Mặc định là giới tính đực
            newPet.setDob(LocalDate.now().minusYears(1)); // Mặc định là 1 tuổi
            newPet.setWeight(1.0); // Mặc định 1kg
            newPet.setTypePet(defaultType);
            newPet.setOwner(selectedCustomer);

            // Sử dụng try-with-resources để đảm bảo đóng resources
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement stmt = connection.prepareStatement(
                     "INSERT INTO pet (name, pet_gender, dob, customer_id, type_id, weight) VALUES (?, ?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
                
                stmt.setString(1, petName);
                stmt.setString(2, newPet.getGender().name());
                stmt.setDate(3, java.sql.Date.valueOf(newPet.getDob()));
                stmt.setInt(4, selectedCustomer.getId());
                stmt.setInt(5, newPet.getTypePet().getTypePetID());
                stmt.setDouble(6, newPet.getWeight());

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected <= 0) {
                    throw new SQLException("Không thể tạo thú cưng mới.");
                }

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        newPet.setPetId(rs.getInt(1));
                        // Lưu vào map để sử dụng sau này
                        petIdMap.put(petName, newPet.getPetId());
                    } else {
                        throw new SQLException("Không thể lấy ID thú cưng sau khi tạo.");
                    }
                }
            }

            // Lấy đầy đủ thông tin thú cưng từ database
            return petRepository.selectById(newPet.getPetId());
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi tạo thú cưng mới: " + e.getMessage(), e);
        }
    }
    

    // Các phương thức làm việc với Transaction
    private int createBookingInTransaction(Connection connection, Booking booking) throws SQLException {
        String sql = "INSERT INTO booking (customer_id, pet_id, staff_id, booking_time, status, note) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, booking.getCustomer().getId());
            stmt.setInt(2, booking.getPet().getPetId());
            stmt.setInt(3, booking.getStaff().getId());
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(booking.getBookingTime()));
            stmt.setString(5, booking.getStatus().name());
            stmt.setString(6, booking.getNote());

            if (stmt.executeUpdate() <= 0) {
                throw new SQLException("Không thể tạo booking.");
            }
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new SQLException("Không thể lấy bookingId.");
            }
        }
    }

    private void createBookingDetailInTransaction(Connection connection, int bookingId, Service service) throws SQLException {
        String sql = "INSERT INTO booking_detail (booking_id, service_id, quantity, price) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            stmt.setInt(2, service.getServiceId());
            stmt.setInt(3, 1);
            stmt.setDouble(4, service.getPrice());

            if (stmt.executeUpdate() <= 0) {
                throw new SQLException("Không thể tạo booking detail.");
            }
        }
    }

    private int createOrderInTransaction(Connection connection, Customer customer, Staff staff, double totalAmount) throws SQLException {
        String sql = "INSERT INTO `order` (customer_id, staff_id, order_date, total_amount, status) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, customer.getId());
            stmt.setInt(2, staff.getId());
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            stmt.setDouble(4, totalAmount);
            stmt.setString(5, StatusEnum.PENDING.name());

            if (stmt.executeUpdate() <= 0) {
                throw new SQLException("Không thể tạo order.");
            }
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new SQLException("Không thể lấy orderId.");
            }
        }
    }

    private void createOrderDetailInTransaction(Connection connection, int orderId, Service service) throws SQLException {
        String sql = "INSERT INTO order_detail (order_id, service_id, quantity, price) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (!service.isActive()) {
                throw new SQLException("Dịch vụ " + service.getName() + " đã ngừng hoạt động!");
            }
            
            stmt.setInt(1, orderId);
            stmt.setInt(2, service.getServiceId());
            stmt.setInt(3, 1);
            stmt.setDouble(4, service.getPrice());

            if (stmt.executeUpdate() <= 0) {
                throw new SQLException("Không thể tạo order detail.");
            }
        }
    }

    private void createInvoiceInTransaction(Connection connection, int orderId, Staff staff, double amount, LocalDateTime bookingTime) throws SQLException {
        String sql = "INSERT INTO invoice (order_id, staff_id, subtotal, total, payment_date, status) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, staff.getId());
            stmt.setBigDecimal(3, BigDecimal.valueOf(amount));
            stmt.setBigDecimal(4, BigDecimal.valueOf(amount));
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(bookingTime)); // Sử dụng bookingTime thay vì LocalDateTime.now()
            stmt.setString(6, StatusEnum.PENDING.name());

            if (stmt.executeUpdate() <= 0) {
                throw new SQLException("Không thể tạo hóa đơn.");
            }
        }
    }

    private String getServiceNameFromBooking(Booking booking) {
        if (booking == null) return "";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                 "SELECT s.name FROM service s " + 
                 "JOIN booking_detail bd ON s.service_id = bd.service_id " + 
                 "WHERE bd.booking_id = ?")) {
            
            stmt.setInt(1, booking.getBookingId());
            ResultSet rs = stmt.executeQuery();
            
            StringBuilder serviceNames = new StringBuilder();
            while (rs.next()) {
                serviceNames.append(rs.getString("name")).append(", ");
            }
            
            return serviceNames.length() > 0 ? 
                   serviceNames.substring(0, serviceNames.length() - 2) : 
                   "Không có thông tin dịch vụ";
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tên dịch vụ: " + e.getMessage());
            return "Không có thông tin dịch vụ";
        }
    }

    @FXML
    private void handleSubmit() {
        saveBooking();
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