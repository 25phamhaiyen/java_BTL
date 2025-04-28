package controllers.Staff;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import controllers.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.Staff;
import service.StaffService;
import utils.RoleChecker;
import utils.Session;

public class StaffController implements Initializable {

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
	private Button invoiceViewButton;

	@FXML
	private Button promotionButton;

	@FXML
	private Button editProfileButton;

	@FXML
	private Button logoutButton;

	private Staff currentStaff;
	private StaffService staffService;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Khởi tạo service
		staffService = new StaffService();

		// Lấy thông tin nhân viên hiện tại từ Session
		currentStaff = Session.getCurrentStaff();

		// Hiển thị thông tin nhân viên trên giao diện
		if (currentStaff != null) {
			staffNameLabel.setText(currentStaff.getFullName());
			staffRoleLabel.setText(currentStaff.getRole().getRoleName());
		}

		// Kiểm tra quyền và hiển thị/ẩn các nút tương ứng
		setupButtonVisibility();

		// Mặc định hiển thị màn hình chính của nhân viên
//        loadStaffHomeView();
	}

	private void setupButtonVisibility() {
		// Các nút mặc định hiển thị cho tất cả nhân viên
		editProfileButton.setVisible(true);

		// Kiểm tra quyền và hiển thị/ẩn các nút tương ứng
		myScheduleButton.setVisible(RoleChecker.hasPermission("VIEW_SCHEDULE"));
		bookingViewButton.setVisible(
				RoleChecker.hasPermission("VIEW_BOOKING_ASSIGNED") || RoleChecker.hasPermission("CREATE_BOOKING"));
		invoiceViewButton
				.setVisible(RoleChecker.hasPermission("VIEW_INVOICE") || RoleChecker.hasPermission("MANAGE_PAYMENT"));
		promotionButton.setVisible(RoleChecker.hasPermission("APPLY_PROMOTION"));
	}
//
//	private void loadStaffHomeView() {
//		try {
//			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/staff_home.fxml"));
//			AnchorPane view = loader.load();
//			mainContainer.setCenter(view);
//		} catch (IOException e) {
//			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình chính", e.getMessage());
//		}
//	}

	@FXML
	private void showMySchedule(ActionEvent event) {
		try {
			if (!RoleChecker.hasPermission("VIEW_SCHEDULE")) {
				showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có quyền",
						"Bạn không có quyền xem lịch làm việc.");
				return;
			}

			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/my_schedule.fxml"));
			Parent view = loader.load();
			mainContainer.setCenter(view);
		} catch (IOException e) {
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình lịch làm việc", e.getMessage());
		}
	}

	@FXML
	private void showBookingView(ActionEvent event) {
		try {
			if (!RoleChecker.hasPermission("VIEW_BOOKING_ASSIGNED") && !RoleChecker.hasPermission("CREATE_BOOKING")) {
				showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có quyền",
						"Bạn không có quyền xem danh sách đặt lịch.");
				return;
			}

			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/booking_view.fxml"));
			Parent view = loader.load();
			mainContainer.setCenter(view);
		} catch (IOException e) {
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình đặt lịch", e.getMessage());
		}
	}

	@FXML
	private void showInvoiceView(ActionEvent event) {
		try {
			if (!RoleChecker.hasPermission("VIEW_INVOICE") && !RoleChecker.hasPermission("MANAGE_PAYMENT")) {
				showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có quyền", "Bạn không có quyền xem hóa đơn.");
				return;
			}

			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/invoice_view.fxml"));
			Parent view = loader.load();
			mainContainer.setCenter(view);
		} catch (IOException e) {
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình hóa đơn", e.getMessage());
		}
	}

	@FXML
	private void showPromotionView(ActionEvent event) {
		try {
			if (!RoleChecker.hasPermission("APPLY_PROMOTION")) {
				showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có quyền",
						"Bạn không có quyền áp dụng khuyến mãi.");
				return;
			}

			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/promotion_apply.fxml"));
			Parent view = loader.load();
			mainContainer.setCenter(view);
		} catch (IOException e) {
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình khuyến mãi", e.getMessage());
		}
	}

	@FXML
	private void showEditProfile(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/edit_profile.fxml"));
			Parent view = loader.load();
			mainContainer.setCenter(view);
		} catch (IOException e) {
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình chỉnh sửa hồ sơ", e.getMessage());
		}
	}

	@FXML
	private void logout(ActionEvent event) {
		try {
			// Xóa thông tin phiên đăng nhập
			Session.clearSession();

			// Chuyển về màn hình đăng nhập
			Stage currentStage = (Stage) logoutButton.getScene().getWindow();
			SceneSwitcher.switchToLoginScene(currentStage);

		} catch (Exception e) {
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể đăng xuất", e.getMessage());
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