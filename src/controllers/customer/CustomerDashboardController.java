package controllers.customer;

import controllers.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import model.Account;
import service.AccountService;
import utils.Session;

public class CustomerDashboardController {

	@FXML
	private Label lblWelcome;
	@FXML
	private Button btnProfile;
	@FXML
	private Button btnBookAppointment;
	@FXML
	private Button btnServiceHistory;
	@FXML
	private Button btnAppointmentHistory;
	@FXML
	private Button btnChangePassword;
	@FXML
	private Button btnLogout;

	private final AccountService accountService = new AccountService();

	@FXML
	public void initialize() {
		// Hiển thị thông tin chào mừng khách hàng khi đăng nhập
		Account currentUser = Session.getCurrentUser();
		if (currentUser != null) {
			lblWelcome.setText("Chào mừng, " + currentUser.getUserName());
		} else {
			lblWelcome.setText("Vui lòng đăng nhập!");
		}

		// Sự kiện xem thông tin cá nhân
		btnProfile.setOnAction(event -> SceneSwitcher.switchScene("customer/viewProfile.fxml"));

		// Sự kiện đặt lịch hẹn
		btnBookAppointment.setOnAction(event -> SceneSwitcher.switchScene("customer/bookAppointment.fxml"));

		// Sự kiện xem lịch sử dịch vụ
		btnServiceHistory.setOnAction(event -> SceneSwitcher.switchScene("customer/serviceHistory.fxml"));

		// Sự kiện xem lịch sử cuộc hẹn
		btnAppointmentHistory.setOnAction(event -> SceneSwitcher.switchScene("customer/appointmentHistory.fxml"));

		// Sự kiện thay đổi mật khẩu
		btnChangePassword.setOnAction(event -> SceneSwitcher.switchScene("customer/changePassword.fxml"));

	}

	// Hiển thị thông tin người dùng
	@FXML
	private void handleViewProfile() {
		Account currentUser = Session.getCurrentUser();
		if (currentUser != null) {
			// Ví dụ: SceneSwitcher.switchScene("customer/viewProfile.fxml");
			System.out.println("Thông tin người dùng: " + currentUser.toString());
		} else {
			showAlert("Lỗi", "Bạn cần đăng nhập để xem thông tin cá nhân.");
		}
	}

	// Đặt lịch hẹn
	@FXML
	private void handleBookAppointment() {
		Account currentUser = Session.getCurrentUser();
		if (currentUser != null) {
			// Ví dụ: SceneSwitcher.switchScene("customer/bookAppointment.fxml");
			System.out.println("Đặt lịch hẹn cho: " + currentUser.getUserName());
		} else {
			showAlert("Lỗi", "Bạn cần đăng nhập để đặt lịch hẹn.");
		}
	}

	// Xem lịch sử đăng ký dịch vụ
	@FXML
	private void handleViewServiceHistory() {
		Account currentUser = Session.getCurrentUser();
		if (currentUser != null) {
			// Ví dụ: SceneSwitcher.switchScene("customer/serviceHistory.fxml");
			System.out.println("Lịch sử dịch vụ của: " + currentUser.getUserName());
		} else {
			showAlert("Lỗi", "Bạn cần đăng nhập để xem lịch sử dịch vụ.");
		}
	}

	// Xem lịch sử cuộc hẹn
	@FXML
	private void handleViewAppointmentHistory() {
		Account currentUser = Session.getCurrentUser();
		if (currentUser != null) {
			// Ví dụ: SceneSwitcher.switchScene("customer/appointmentHistory.fxml");
			System.out.println("Lịch sử cuộc hẹn của: " + currentUser.getUserName());
		} else {
			showAlert("Lỗi", "Bạn cần đăng nhập để xem lịch sử cuộc hẹn.");
		}
	}

	// Đổi mật khẩu
	@FXML
	private void handleChangePassword() {
		Account currentUser = Session.getCurrentUser();
		if (currentUser != null) {
			// Ví dụ: SceneSwitcher.switchScene("customer/changePassword.fxml");
			System.out.println("Đổi mật khẩu cho: " + currentUser.getUserName());
		} else {
			showAlert("Lỗi", "Bạn cần đăng nhập để đổi mật khẩu.");
		}
	}

	// Đăng xuất
	@FXML
	private void handleLogout() {
		Session.logout();
		SceneSwitcher.switchScene("login.fxml");
	}

	// Hiển thị thông báo lỗi
	private void showAlert(String title, String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
