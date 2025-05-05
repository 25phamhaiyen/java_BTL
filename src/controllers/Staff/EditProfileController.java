package controllers.Staff;

import java.net.URL;
import java.util.ResourceBundle;

import controllers.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
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

    private StaffService staffService;
    private AuthService authService;

    public EditProfileController() {
        staffService = new StaffService();
        authService = new AuthService();
    }

    @FXML private VBox mainContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadProfile();
        setupResponsive();
    }

    private void setupResponsive() {
        // Lắng nghe thay đổi kích thước scene
        mainContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((obs2, oldWidth, newWidth) -> {
                    updateResponsiveClasses(newScene);
                });
                updateResponsiveClasses(newScene);
            }
        });
    }

    private void updateResponsiveClasses(Scene scene) {
        double width = scene.getWidth();
        
        // Xóa class cũ
        mainContainer.getStyleClass().remove("small-screen");
        
        // Thêm class mới nếu cần
        if (width < 600) {
            mainContainer.getStyleClass().add("small-screen");
        }
    }

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
    	if(Session.getCurrentAccount().getRole().getRoleName().equals("ADMIN")) {
    		SceneSwitcher.switchScene("admin/admin_home.fxml");
    	}
    	else {SceneSwitcher.switchScene("staff/staff_home.fxml");}
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}