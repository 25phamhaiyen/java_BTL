package controllers;

import java.io.IOException;

import controllers.Staff.CreateInvoiceController;
import controllers.Staff.InvoiceViewController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Booking;
import service.BookingService;
import utils.LanguageManager;

/**
 * Lớp tiện ích dùng để chuyển đổi giữa các màn hình (scene) trong ứng dụng
 */
public class SceneSwitcher {

	private static Stage mainStage;

	/**
	 * Thiết lập stage chính của ứng dụng
	 * 
	 * @param stage Stage chính
	 */
	public static void setMainStage(Stage stage) {
		mainStage = stage;
		mainStage.setTitle("Cửa hàng chăm sóc thú cưng BESTPETS");

	}

	/**
	 * Chuyển đến một scene khác dựa trên đường dẫn file FXML
	 * 
	 * @param fxmlPath Đường dẫn tới file FXML
	 */
	public static void switchScene(String fxmlPath) {
		try {
			Parent root = FXMLLoader.load(SceneSwitcher.class.getResource("/view/" + fxmlPath));
			Scene scene = new Scene(root, 800, 700);
			mainStage.setScene(scene);
			// Set window title based on current language
//            LanguageManager langManager = LanguageManager.getInstance();
//            mainStage.setTitle(langManager.getString("app.title"));
			mainStage.show();
		} catch (IOException e) {
			e.printStackTrace();
			showErrorDialog("Không thể tải màn hình: " + e.getMessage());
		}
	}

	/**
	 * Chuyển đến màn hình đăng nhập
	 * 
	 * @param currentStage Stage hiện tại
	 */
	public static void switchToLoginScene(Stage currentStage) {
		try {
			Parent root = FXMLLoader.load(SceneSwitcher.class.getResource("/view/login.fxml"));
//			Scene scene = new Scene(root);
			currentStage.setTitle("Đăng nhập");
			currentStage.setScene(new Scene(root, 500, 500));
			currentStage.show();
		} catch (IOException e) {
			e.printStackTrace();
			showErrorDialog("Không thể tải màn hình đăng nhập: " + e.getMessage());
		}
	}

	/**
	 * Chuyển đến màn hình dashboard
	 * 
	 * @param currentStage Stage hiện tại
	 */
	public static void switchToDashboardScene(Stage currentStage) {
		try {
			Parent root = FXMLLoader.load(SceneSwitcher.class.getResource("/view/dashboard.fxml"));
			Scene scene = new Scene(root);
			currentStage.setScene(scene);
			currentStage.setTitle("Trang chính");
			currentStage.show();
		} catch (IOException e) {
			e.printStackTrace();
			showErrorDialog("Không thể tải màn hình chính: " + e.getMessage());
		}
	}

	/**
	 * Chuyển đến màn hình nhân viên
	 * 
	 * @param currentStage Stage hiện tại
	 */
	public static void switchToStaffScene(Stage currentStage) {
		try {
			Parent root = FXMLLoader.load(SceneSwitcher.class.getResource("/view/staff/staff_home.fxml"));
			Scene scene = new Scene(root);
			currentStage.setScene(scene);
			currentStage.setTitle("Nhân viên");
			currentStage.show();
		} catch (IOException e) {
			e.printStackTrace();
			showErrorDialog("Không thể tải màn hình nhân viên: " + e.getMessage());
		}
	}

	/**
	 * Chuyển đến màn hình chi tiết booking
	 * 
	 * @param currentStage Stage hiện tại
	 * @param bookingId    ID của booking
	 */
//	public static void switchToBookingDetailScene(Stage currentStage, int bookingId) {
//		try {
//			FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/view/staff/bookingDetail.fxml"));
//			Parent root = loader.load();
//
//			// Truyền bookingId cho controller
//			BookingDetailController controller = loader.getController();
//			controller.initData(bookingId);
//
//			Scene scene = new Scene(root);
//			currentStage.setScene(scene);
//			currentStage.setTitle("Chi tiết đặt lịch #" + bookingId);
//			currentStage.show();
//		} catch (IOException e) {
//			e.printStackTrace();
//			showErrorDialog("Không thể tải màn hình chi tiết đặt lịch: " + e.getMessage());
//		}
//	}

	/**
	 * Chuyển đến màn hình xử lý hóa đơn
	 * 
	 * @param currentStage Stage hiện tại
	 * @param bookingId    ID của booking
	 */
	public static void switchToInvoiceScene(Stage currentStage, int bookingId) {
		try {
			FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/view/staff/createInvoice.fxml"));
			Parent root = loader.load();

			// Truyền bookingId cho controller
			CreateInvoiceController controller = loader.getController();
			BookingService bookingService = new BookingService();
			Booking booking;
			try {
				booking = bookingService.getBookingById(bookingId);
				controller.initData(booking);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Scene scene = new Scene(root);
			currentStage.setScene(scene);
			currentStage.setTitle("Tạo hóa đơn cho đặt lịch #" + bookingId);
			currentStage.show();
		} catch (IOException e) {
			e.printStackTrace();
			showErrorDialog("Không thể tải màn hình tạo hóa đơn: " + e.getMessage());
		}
	}

//	/**
//	 * Chuyển đến màn hình chi tiết hóa đơn
//	 * 
//	 * @param currentStage Stage hiện tại
//	 * @param invoiceId    ID của hóa đơn
//	 */
//	public static void switchToInvoiceDetailScene(Stage currentStage, int invoiceId) {
//		try {
//			FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/view/staff/invoiceDetail.fxml"));
//			Parent root = loader.load();
//
//			// Truyền invoiceId cho controller
//			InvoiceViewController controller = loader.getController();
//			controller.initData(invoiceId);
//
//			Scene scene = new Scene(root);
//			currentStage.setScene(scene);
//			currentStage.setTitle("Chi tiết hóa đơn #" + invoiceId);
//			currentStage.show();
//		} catch (IOException e) {
//			e.printStackTrace();
//			showErrorDialog("Không thể tải màn hình chi tiết hóa đơn: " + e.getMessage());
//		}
//	}

	/**
	 * Hiển thị hộp thoại thông báo lỗi
	 * 
	 * @param message Thông báo lỗi
	 */
	private static void showErrorDialog(String message) {
		javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
		alert.setTitle("Lỗi");
		alert.setHeaderText("Đã xảy ra lỗi");
		alert.setContentText(message);
		alert.showAndWait();
	}
}