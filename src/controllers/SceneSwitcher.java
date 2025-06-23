package controllers;

import java.io.IOException;
import java.util.ResourceBundle;

import controllers.Staff.CreateInvoiceController;
import controllers.Staff.InvoiceViewController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Booking;
import service.BookingService;
import utils.LanguageManagerAd;

public class SceneSwitcher {

    private static Stage mainStage;

    public static void setMainStage(Stage stage) {
        mainStage = stage;
        
        LanguageManagerAd.addListener(() -> {
            String title = LanguageManagerAd.getString("title");
            if (mainStage != null) {
                mainStage.setTitle(title);
            }
        });
        
        if (mainStage != null) {
            mainStage.setTitle(LanguageManagerAd.getString("title"));
        }
    }

    /**
     * Chuyển scene mặc định (full màn hình)
     * @param fxmlPath Đường dẫn FXML
     */
    public static void switchScene(String fxmlPath) {
        switchScene(fxmlPath, false); // Mặc định là full màn hình
    }

    /**
     * Chuyển scene với tùy chọn không full màn hình (dành cho home)
     * @param fxmlPath Đường dẫn FXML
     * @param isHomeScreen Có phải màn hình home không (nếu true thì không full màn hình)
     */
    public static void switchScene(String fxmlPath, boolean isHomeScreen) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/view/" + fxmlPath));
            loader.setResources(ResourceBundle.getBundle("lang.messages", LanguageManagerAd.getCurrentLocale()));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            mainStage.setScene(scene);
            
            // Thiết lập không full màn hình nếu là home
            if (isHomeScreen) {
                mainStage.setMaximized(false);
                mainStage.setWidth(800);
                mainStage.setHeight(700);
            } else {
                mainStage.setMaximized(true);
            }
            
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

