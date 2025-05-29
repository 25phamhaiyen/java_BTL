package controllers.Staff;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

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
import utils.LanguageManager;
import utils.I18nUtil;

public class StaffController implements Initializable, I18nUtil.I18nUpdatable {

	 @FXML
	    private Button btnLanguage;
	@FXML
	private BorderPane mainContainer;

	@FXML
	private Label staffNameLabel;

	@FXML
	private Label staffRoleLabel;

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
	private LanguageManager languageManager;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		languageManager = LanguageManager.getInstance();
		
		// Register this controller for language updates
		I18nUtil.register(this);

		new StaffService();

		// Lấy thông tin nhân viên hiện tại từ Session
		currentStaff = Session.getCurrentStaff();

		// Hiển thị thông tin nhân viên trên giao diện
		if (currentStaff != null) {
			btnLanguage.setOnAction(this::toggleLanguage);
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
		updateLanguage();

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

		loadTodaySchedule();
		loadTodayAppointments();
	}

	@Override
	public void updateLanguage() {
		// Update all UI text with current language
		I18nUtil.setText(btnLanguage, "staff.btnLanguage");
		I18nUtil.setText(staffLabelText, "staff.label");
		I18nUtil.setText(roleLabelText, "role.label");
		I18nUtil.setText(logoutButton, "logout.button");
		I18nUtil.setText(editProfileButton, "edit.profile.button");
		I18nUtil.setText(todayScheduleTitle, "today.schedule.title");
		I18nUtil.setText(todayAppointmentTitle, "today.appointment.title");
		I18nUtil.setText(myScheduleButton, "my.schedule.button");
		I18nUtil.setText(bookingViewButton, "booking.view.button");
		I18nUtil.setText(invoiceViewButton, "invoice.view.button");
		
		// Update welcome label
		updateWelcomeLabel();
		
		// Reload data with new language
		loadTodaySchedule();
		loadTodayAppointments();
	}

	private void updateWelcomeLabel() {
		String welcomeText = languageManager.getString("welcome.message", 
			currentStaff != null ? currentStaff.getFullName() : languageManager.getString("staff.default"),
			currentStaff != null && currentStaff.getRole() != null ? 
				currentStaff.getRole().getRoleName() : languageManager.getString("staff.default"));
		welcomeLabel.setText(welcomeText);
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
				System.out.println(languageManager.getString("no.schedule.today"));
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
				appointments.add(languageManager.getString("cannot.identify.staff"));
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
					String petName = (pet != null) ? pet.getName() : languageManager.getString("unknown");
					String customerName = (customer != null) ? customer.getFullName() : languageManager.getString("unknown");

					String displayText = languageManager.getString("appointment.format", time, petName, customerName);
					appointments.add(displayText);
				}
			}

			if (appointments.isEmpty()) {
				appointments.add(languageManager.getString("no.appointments.today"));
			}

			todayAppointmentListView.setItems(appointments);

		} catch (Exception e) {
			appointments.clear();
			appointments.add(languageManager.getString("error.loading.appointments", e.getMessage()));
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
        if (I18nUtil.showConfirmation("app.confirm", "msg.confirm.logout")) {
            Session.logout();
            SceneSwitcher.switchScene("login.fxml");
        }
    }
	
    private void toggleLanguage(ActionEvent event) {
        LanguageManager langManager = LanguageManager.getInstance();
        Locale currentLocale = langManager.getCurrentLocale();
        
        // Toggle between Vietnamese and English
        if (currentLocale.equals(LanguageManager.VIETNAMESE)) {
            I18nUtil.switchLanguage(LanguageManager.ENGLISH);
        } else {
            I18nUtil.switchLanguage(LanguageManager.VIETNAMESE);
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