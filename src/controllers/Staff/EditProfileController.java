package controllers.Staff;

import java.net.URL;
import java.util.ResourceBundle;

import controllers.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import model.Account;
import model.Staff;
import service.AuthService;
import service.StaffService;
import utils.Session;

public class EditProfileController implements Initializable {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    
    @FXML private Button updateProfileBtn;
    @FXML private Button backButton;
    @FXML private Button changePasswordBtn;
    @FXML private Button backToProfileBtn;
    
    @FXML private VBox profileForm;
    @FXML private VBox passwordForm;
    @FXML private VBox mainContainer;

    private StaffService staffService;
    private AuthService authService;

    public EditProfileController() {
        staffService = new StaffService();
        authService = new AuthService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Tải thông tin hồ sơ từ cơ sở dữ liệu
        loadProfile();
        
        // Thiết lập responsive design
        setupResponsive();
    }

    /**
     * Thiết lập responsive cho giao diện
     */
    private void setupResponsive() {
        // Phương pháp 1: Lắng nghe sự thay đổi kích thước scene
        mainContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((obs2, oldWidth, newWidth) -> {
                    updateResponsiveClasses(newScene);
                });
                updateResponsiveClasses(newScene);
            }
        });
        
        // Phương pháp 2: Áp dụng class dựa trên kích thước màn hình khi khởi tạo
        // Lưu ý: Phương pháp này chỉ chạy một lần khi khởi tạo và không đáp ứng với thay đổi kích thước
//        mainContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
//            if (newScene != null) {
//                applyResponsiveLayout();
//            }
//        });
    }
    
    /**
     * Áp dụng bố cục đáp ứng dựa trên kích thước màn hình
     */
    private void applyResponsiveLayout() {
        // Lấy kích thước màn hình
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        
        // Lấy root scene để áp dụng style class
        Parent root = mainContainer.getScene().getRoot();
        
        // Xóa class cũ
        root.getStyleClass().removeAll("small-screen", "large-screen");
        
        // Áp dụng class dựa trên kích thước màn hình
        if (screenWidth < 1024) {
            root.getStyleClass().add("small-screen");
        } else if (screenWidth >= 1920) {
            root.getStyleClass().add("large-screen");
        }
    }
    
    /**
     * Cập nhật các class responsive theo kích thước scene
     */
    private void updateResponsiveClasses(Scene scene) {
        double width = scene.getWidth();
        Parent root = scene.getRoot();
        
        // Xóa class cũ
        root.getStyleClass().removeAll("small-screen", "large-screen");
        
        // Thêm class mới dựa trên kích thước
        if (width < 800) {
            root.getStyleClass().add("small-screen");
        } else if (width >= 1600) {
            root.getStyleClass().add("large-screen");
        }
    }

    /**
     * Tải thông tin hồ sơ từ cơ sở dữ liệu
     */
    private void loadProfile() {
        Session.getInstance();
        Account account = Session.getCurrentAccount();
        if (account != null) {
            Staff staff = staffService.getStaffByAccountID(account.getAccountID());
            if (staff != null) {
                fullNameField.setText(staff.getFullName());
                emailField.setText(staff.getEmail());
                phoneField.setText(staff.getPhone());
                addressField.setText(staff.getAddress());
            }
        }
    }

    @FXML
    private void handleUpdateProfile(ActionEvent event) {
        try {
            Session.getInstance();
            Account account = Session.getCurrentAccount();
            Staff staff = staffService.getStaffByAccountID(account.getAccountID());

            // Set updated values
            staff.setFullName(fullNameField.getText());
            staff.setEmail(emailField.getText());
            staff.setPhone(phoneField.getText());
            staff.setAddress(addressField.getText());

            // Validate input before updating
            staffService.validatePerson(staff);

            // Update staff
            boolean success = staffService.updateStaff(staff);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật hồ sơ thành công!");
            } else {
                throw new Exception("Cập nhật không thành công");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật thất bại: " + e.getMessage());
        }
    }

    @FXML
    private void handleChangePasswordForm(ActionEvent event) {
        profileForm.setVisible(false);
        profileForm.setManaged(false);
        passwordForm.setVisible(true);
        passwordForm.setManaged(true);
        
        // Clear các field mật khẩu
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    private void handleBackToProfile(ActionEvent event) {
        passwordForm.setVisible(false);
        passwordForm.setManaged(false);
        profileForm.setVisible(true);
        profileForm.setManaged(true);
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Kiểm tra mật khẩu mới và xác nhận có khớp không
        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu xác nhận không khớp!");
            return;
        }

        try {
            Session.getInstance();
            Account account = Session.getCurrentAccount();
            
            // Kiểm tra mật khẩu hiện tại
            if (!authService.verifyPassword(account.getAccountID(), currentPassword)) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu hiện tại không đúng!");
                return;
            }

            // Đổi mật khẩu
            boolean success = authService.changePassword(account.getAccountID(), newPassword);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đổi mật khẩu thành công!");
                handleBackToProfile(null); // Quay lại form profile
            } else {
                throw new Exception("Đổi mật khẩu thất bại");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đổi mật khẩu thất bại: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneSwitcher.switchScene("staff/staff_home.fxml");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}