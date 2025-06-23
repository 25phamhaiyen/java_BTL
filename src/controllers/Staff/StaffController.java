package controllers.Staff;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import controllers.SceneSwitcher;
import enums.Shift;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.Booking;
import model.Customer;
import model.Pet;
import model.Staff;
import model.WorkSchedule;
import service.BookingService;
import service.ScheduleService;
import service.StaffService;
import utils.RoleChecker;
import javafx.scene.control.ListCell;
import utils.Session;
import utils.LanguageChangeListener;
import utils.LanguageManagerStaff;

public class StaffController implements Initializable, LanguageChangeListener {

	@FXML
	private ComboBox<String> languageCombo;
	
	@FXML
	private BorderPane mainContainer;

	@FXML
	private Label staffNameLabel;

	@FXML
	private Label staffRoleLabel;
	
	@FXML
	private Label staffAppTitle;

	@FXML
	private Label staffWelcomeSubtitle;

	@FXML
	private Button myScheduleButton;

	@FXML
	private Button bookingViewButton;

	@FXML
	private Button okila;

	@FXML
	private Button invoiceViewButton;

	@FXML
	private Button promotionButton;

	@FXML
	private Button editProfileButton;

	@FXML
	private Button logoutButton;

	@FXML
	private ListView<WorkSchedule> todayScheduleListView;

	@FXML
	private Label welcomeLabel;

	@FXML
	private ListView<String> todayAppointmentListView;

	// Labels for internationalization
	@FXML
	private Label staffLabelText;

	@FXML
	private Label roleLabelText;

	@FXML
	private Label todayScheduleTitle;

	@FXML
	private Label todayAppointmentTitle;

	private final ScheduleService scheduleService = new ScheduleService();
	private Staff currentStaff;
	
	@FXML
	private Label staffWorkShiftLabel, staffWorkShiftValue;
	@FXML
	private Label staffAppointmentsLabel, staffAppointmentsValue;
	@FXML
	private Label staffCompletedLabel, staffCompletedValue;
	
	@FXML
	private Label appointmentBadgeLabel;
	@FXML
	private Label scheduleBadgeLabel;
	

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Register this controller for language updates
		LanguageManagerStaff.addListener(this);

		new StaffService();

		// Lấy thông tin nhân viên hiện tại từ Session
		currentStaff = Session.getCurrentStaff();

		// Set up language combo box
		setupLanguageCombo();

		// Hiển thị thông tin nhân viên trên giao diện
		if (currentStaff != null) {
			staffNameLabel.setText(currentStaff.getFullName());
			staffRoleLabel.setText(currentStaff.getRole().getRoleName());
		}

		
		// Cập nhật tên giao diện với đa ngôn ngữ
		updateWelcomeLabel();

		// Kiểm tra quyền và hiển thị/ẩn các nút tương ứng
		setupButtonVisibility();

		// Cấu hình nút dựa trên vai trò
		setupRoleBasedFocus();

		// Cập nhật ngôn ngữ cho tất cả các thành phần
		loadTexts();

		todayScheduleListView.setCellFactory(lv -> new ListCell<WorkSchedule>() {
			@Override
			protected void updateItem(WorkSchedule item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
				} else {
					setText(item.getShift() + (item.getWorkDate() == null ? "" : " - " + item.getWorkDate()));
				}
			}
		});

		// Thêm dòng này ở cuối phương thức initialize
	    updateStats();
	    
		loadTodaySchedule();
		loadTodayAppointments();
	}

	private void setupLanguageCombo() {
		// Lấy danh sách ngôn ngữ có sẵn từ LanguageManagerStaff
		languageCombo.getItems().clear();
		languageCombo.getItems().addAll("Tiếng Việt", "English");
		
		// Set current language based on current locale
		Locale currentLocale = LanguageManagerStaff.getCurrentLocale();
		if (currentLocale != null && currentLocale.getLanguage().equals("en")) {
			languageCombo.setValue("English");
		} else {
			languageCombo.setValue("Tiếng Việt");
		}
		
		// Handle language change
		languageCombo.setOnAction(e -> {
			String selectedLang = languageCombo.getValue();
			if ("English".equals(selectedLang)) {
				LanguageManagerStaff.setLocale(new Locale("en", "US"));
			} else {
				LanguageManagerStaff.setLocale(new Locale("vi", "VN"));
			}
			System.out.println("Language changed to: " + selectedLang);
		});
	}

	@Override
	public void onLanguageChanged() {
		// Được gọi khi ngôn ngữ thay đổi từ LanguageManagerStaff
		System.out.println("Language changed event received in StaffController");
		loadTexts();
	}

	private void loadTexts() {
		try {
			// Update all UI text with current language
			staffLabelText.setText(LanguageManagerStaff.getString("staff.label"));
			staffAppTitle.setText(LanguageManagerStaff.getString("staff.apptitle"));
			staffWelcomeSubtitle.setText(LanguageManagerStaff.getString("staff.WelcomeSubtitle"));
			roleLabelText.setText(LanguageManagerStaff.getString("role.label"));
			logoutButton.setText(LanguageManagerStaff.getString("logout.button"));
			editProfileButton.setText(LanguageManagerStaff.getString("edit.profile.button"));
			todayScheduleTitle.setText(LanguageManagerStaff.getString("today.schedule.title"));
			todayAppointmentTitle.setText(LanguageManagerStaff.getString("today.appointment.title"));
			myScheduleButton.setText(LanguageManagerStaff.getString("my.schedule.button"));
			bookingViewButton.setText(LanguageManagerStaff.getString("booking.view.button"));
			invoiceViewButton.setText(LanguageManagerStaff.getString("invoice.view.button"));
			
			// Thêm các dòng này
	        staffWorkShiftLabel.setText(LanguageManagerStaff.getString("staff.workshift"));
	        staffAppointmentsLabel.setText(LanguageManagerStaff.getString("staff.appointments"));
	        staffCompletedLabel.setText(LanguageManagerStaff.getString("staff.completed"));
	        
	     // Cập nhật text cho badge
	        appointmentBadgeLabel.setText(LanguageManagerStaff.getString("badge.appointment"));
	        scheduleBadgeLabel.setText(LanguageManagerStaff.getString("badge.schedule"));
	        
	        // Cập nhật lại stats khi ngôn ngữ thay đổi
	        updateStats();
	        
			// Update welcome label
			updateWelcomeLabel();
			
			// Reload data with new language
			loadTodaySchedule();
			loadTodayAppointments();
			
			System.out.println("All texts updated successfully");
		} catch (Exception e) {
			System.err.println("Error updating texts: " + e.getMessage());
			e.printStackTrace();
		}
	}

//	private void updateWelcomeLabel() {
//		try {
//			String welcomeText = LanguageManagerStaff.getString("welcome.message");
//			if (currentStaff != null) {
//				welcomeText = String.format(welcomeText, 
//					currentStaff.getFullName(),
//					currentStaff.getRole() != null ? currentStaff.getRole().getRoleName() : LanguageManagerStaff.getString("staff.default"));
//			}
//			welcomeLabel.setText(welcomeText);
//		} catch (Exception e) {
//			System.err.println("Error updating welcome label: " + e.getMessage());
//			welcomeLabel.setText("Welcome"); // Fallback
//		}
//	}
	
	private void updateWelcomeLabel() {
	    try {
	        String welcomePattern = LanguageManagerStaff.getString("welcome.message");
	        if (currentStaff != null) {
	            String roleName = currentStaff.getRole() != null 
	                ? currentStaff.getRole().getRoleName() 
	                : LanguageManagerStaff.getString("staff.default");
	            
	            welcomeLabel.setText(MessageFormat.format(welcomePattern, 
	                currentStaff.getFullName(),
	                roleName));
	        } else {
	            welcomeLabel.setText(MessageFormat.format(welcomePattern, 
	                LanguageManagerStaff.getString("unknown.user"),
	                LanguageManagerStaff.getString("unknown.role")));
	        }
	    } catch (Exception e) {
	        System.err.println("Error updating welcome label: " + e.getMessage());
	        welcomeLabel.setText("Welcome"); // Fallback
	    }
	}

	/**
	 * Configure role-based button focus and styling
	 */
	private void setupRoleBasedFocus() {
		if (currentStaff == null || currentStaff.getRole() == null)
			return;

		String roleName = currentStaff.getRole().getRoleName().toUpperCase();
		// Ở đây bạn có thể thêm code để đặt focus hoặc style cho các nút dựa trên vai trò
	}

	private void loadTodaySchedule() {
		try {
			currentStaff = Session.getCurrentStaff();
			if (currentStaff == null) {
				System.out.println("Không thể xác định nhân viên hiện tại");
				todayScheduleListView.getItems().clear();
				return;
			}

			LocalDate today = LocalDate.now();
			List<WorkSchedule> schedules = scheduleService.getSchedulesByStaffAndDate(currentStaff.getId(), today);

			if (schedules != null && !schedules.isEmpty()) {
				System.out.println("Dữ liệu lịch làm việc: ");
				for (WorkSchedule schedule : schedules) {
					System.out.println(schedule);
				}
				todayScheduleListView.getItems().setAll(schedules);
			} else {
				System.out.println(LanguageManagerStaff.getString("no.schedule.today"));
				todayScheduleListView.getItems().clear();
			}
		} catch (Exception e) {
			System.out.println("Lỗi trong loadTodaySchedule: " + e.getMessage());
			e.printStackTrace();
			todayScheduleListView.getItems().clear();
		}
	}

	private void loadTodayAppointments() {
		ObservableList<String> appointments = FXCollections.observableArrayList();
		BookingService bookingService = new BookingService();

		try {
			currentStaff = Session.getCurrentStaff();
			if (currentStaff == null) {
				appointments.add(LanguageManagerStaff.getString("cannot.identify.staff"));
				todayAppointmentListView.setItems(appointments);
				return;
			}

			List<Booking> bookings = bookingService.getBookingsByStaffId(currentStaff.getId());
			LocalDate today = LocalDate.now();

			for (Booking booking : bookings) {
				if (booking.getBookingTime().toLocalDate().equals(today)) {
					Pet pet = booking.getPet();
					Customer customer = booking.getCustomer();

					String time = booking.getBookingTime().toLocalTime().toString();
					String petName = (pet != null) ? pet.getName() : LanguageManagerStaff.getString("unknown");
					String customerName = (customer != null) ? customer.getFullName() : LanguageManagerStaff.getString("unknown");

					String displayText = MessageFormat.format(
						    LanguageManagerStaff.getString("appointment.format"), 
						    time, 
						    petName, 
						    customerName
						);

					appointments.add(displayText);
				}
			}

			if (appointments.isEmpty()) {
				appointments.add(LanguageManagerStaff.getString("no.appointments.today"));
			}

			todayAppointmentListView.setItems(appointments);

		} catch (Exception e) {
			appointments.clear();
			appointments.add(String.format(LanguageManagerStaff.getString("error.loading.appointments"), e.getMessage()));
			todayAppointmentListView.setItems(appointments);
			e.printStackTrace();
		}
	}

	private void setupButtonVisibility() {
		// Các nút mặc định hiển thị cho tất cả nhân viên
		editProfileButton.setVisible(true);
		okila.setVisible(false);

		// Kiểm tra quyền và hiển thị/ẩn các nút tương ứng
		myScheduleButton.setVisible(RoleChecker.hasPermission("VIEW_SCHEDULE"));
		bookingViewButton.setVisible(RoleChecker.hasPermission("VIEW_BOOKING_ASSIGNED"));
		invoiceViewButton.setVisible(RoleChecker.hasPermission("VIEW_INVOICE"));
	}

	private void updateStats() {
	    try {
	        currentStaff = Session.getCurrentStaff();
	        if (currentStaff == null) return;

	        LocalDate today = LocalDate.now();
	        
	        // 1. Cập nhật số ca làm việc
	        List<WorkSchedule> todaySchedules = scheduleService.getSchedulesByStaffAndDate(currentStaff.getId(), today);
	        int totalShiftsToday = todaySchedules.size();
	        int totalWeeklyShifts = getTotalShiftsPerWeek();
	        staffWorkShiftValue.setText(totalShiftsToday + "/" + totalWeeklyShifts);
	        
	        // 2. Cập nhật số khách hẹn
	        BookingService bookingService = new BookingService();
	        List<Booking> todayBookings = bookingService.getBookingsByStaffId(currentStaff.getId()).stream()
	                .filter(b -> b.getBookingTime().toLocalDate().equals(today))
	                .collect(Collectors.toList());
	        int totalAppointments = todayBookings.size();
	        staffAppointmentsValue.setText(String.valueOf(totalAppointments));
	        
	        // 3. Cập nhật số hoàn thành (sử dụng enum đúng cách)
	        long completedCount = todayBookings.stream()
	                .filter(b -> b.getStatus() != null && b.getStatus().isCompleted())
	                .count();
	        staffCompletedValue.setText(completedCount + "/" + totalAppointments);
	        
	    } catch (Exception e) {
	        System.err.println("Error updating stats: " + e.getMessage());
	        e.printStackTrace();
	        staffWorkShiftValue.setText("0/0");
	        staffAppointmentsValue.setText("0");
	        staffCompletedValue.setText("0/0");
	    }
	}

	private int getTotalShiftsPerWeek() {
	    // Giả sử mỗi nhân viên làm 7 ca/tuần (có thể điều chỉnh theo logic nghiệp vụ)
	    return 7;
	    
	    // Hoặc tính toán thực tế hơn:
	    // LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
	    // LocalDate endOfWeek = startOfWeek.plusDays(6);
	    // return scheduleService.getSchedulesByStaffAndDateRange(currentStaff.getId(), startOfWeek, endOfWeek).size();
	}
	
	@FXML
	private void showMySchedule(ActionEvent event) {
		SceneSwitcher.switchScene("staff/my_schedule.fxml");
	}

	@FXML
	private void showBookingView(ActionEvent event) {
		SceneSwitcher.switchScene("staff/booking_view.fxml");
	}

	@FXML
	private void showInvoiceView(ActionEvent event) {
		SceneSwitcher.switchScene("staff/invoice_view.fxml");
	}

	@FXML
	private void showEditProfile(ActionEvent event) {
		SceneSwitcher.switchScene("staff/edit_profile.fxml");
	}

	@FXML
	private void handleLogout() {
		// Show confirmation dialog
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle(LanguageManagerStaff.getString("app.confirm"));
		alert.setContentText(LanguageManagerStaff.getString("msg.confirm.logout"));
		
		alert.showAndWait().ifPresent(response -> {
			if (response == javafx.scene.control.ButtonType.OK) {
				Session.logout();
				SceneSwitcher.switchScene("login.fxml");
			}
		});
	}

	private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
	
	/**
	 * Cleanup method - gọi khi controller bị hủy
	 */
	public void cleanup() {
		LanguageManagerStaff.removeListener(this);
		System.out.println("StaffController cleanup completed");
	}
}