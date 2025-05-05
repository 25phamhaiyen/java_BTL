package controllers;

import java.util.Optional;

import exception.BusinessException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Account;
import model.Role;
import service.AccountService;
import utils.Session;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordTextField; // TextField hiển thị mật khẩu
    @FXML
    private Label messageLabel;
    @FXML
    private ImageView togglePasswordVisibilityIcon; // Icon con mắt
    @FXML
    private ImageView logoImage;

    private final AccountService accountService = new AccountService();
    private int failedLoginAttempts = 0; // Biến đếm số lần đăng nhập thất bại
    private String lastFailedUsername = null; // Lưu lại username đăng nhập thất bại gần nhất

    @FXML
    public void initialize() {
        // Tải logo vào ImageView
        logoImage.setImage(new Image(getClass().getResourceAsStream("/images/logo.png")));
        togglePasswordVisibilityIcon.setImage(new Image(getClass().getResourceAsStream("/images/hide.png")));
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.isVisible() ? passwordField.getText().trim()
                : passwordTextField.getText().trim();

        try {
            Optional<Account> user = accountService.login(username, password);
            Account account = user.orElseThrow(() -> new BusinessException("Sai tên đăng nhập hoặc mật khẩu"));

            // Reset số lần đăng nhập thất bại nếu đăng nhập thành công
            failedLoginAttempts = 0;
            lastFailedUsername = null;

            // Kiểm tra trạng thái active của tài khoản (vẫn giữ nguyên)
            if (!account.isActive()) {
                throw new BusinessException("Tài khoản này hiện đang bị khóa!");
            }

            messageLabel.setText("Đăng nhập thành công!");
            messageLabel.setStyle("-fx-text-fill: green;");
            Session.setCurrentAccount(account);

            // Route to appropriate view based on role
            routeBasedOnRole(account.getRole());

        } catch (BusinessException e) {
            failedLoginAttempts++;
            messageLabel.setText(e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");

            // Kiểm tra nếu đạt đến ngưỡng đăng nhập thất bại
            if (failedLoginAttempts >= 5 && username.equals(lastFailedUsername)) {
                // Gọi service để khóa tài khoản
                accountService.lockAccount(username);
                messageLabel.setText("Tài khoản " + username + " đã bị khóa sau 5 lần đăng nhập sai!");
                messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                failedLoginAttempts = 0; // Reset lại sau khi khóa
                lastFailedUsername = null;
            } else {
                lastFailedUsername = username; // Cập nhật username thất bại gần nhất
            }
        }
    }

    /**
     * Điều hướng người dùng đến màn hình tương ứng với vai trò
     * @param role Vai trò của người dùng
     */
    private void routeBasedOnRole(Role role) {
        if (role == null) {
            SceneSwitcher.switchScene("dashboard.fxml");
            return;
        }

        switch (role.getRoleName().toUpperCase()) {
            case "ADMIN":
                SceneSwitcher.switchScene("admin/admin_home.fxml");
                break;
            case "STAFF_CARE":
            case "STAFF_CASHIER":
            case "STAFF_RECEPTION":
                SceneSwitcher.switchScene("staff/staff_home.fxml");
                break;
            case "OUT":
            	throw new BusinessException("Tài khoản này nhân viên đã nghỉ việc!");
		default:
                break;
        }
    }

    @FXML
    public void togglePasswordVisibility() {
        if (passwordField.isVisible()) {
            passwordField.setVisible(false);
            passwordTextField.setVisible(true);
            passwordTextField.setText(passwordField.getText());
            togglePasswordVisibilityIcon.setImage(new Image(getClass().getResourceAsStream("/images/show.png")));
        } else {
            passwordField.setVisible(true);
            passwordTextField.setVisible(false);
            passwordField.setText(passwordTextField.getText());
            togglePasswordVisibilityIcon.setImage(new Image(getClass().getResourceAsStream("/images/hide.png")));
        }
    }
}