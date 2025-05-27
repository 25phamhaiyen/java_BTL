package controllers;

import java.util.Optional;
import java.util.ResourceBundle;

import exception.BusinessException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Account;
import model.Role;
import service.AccountService;
import utils.LanguageChangeListener;
import utils.LanguageManager;
import utils.Session;

public class LoginController implements LanguageChangeListener{
	@FXML private Label titleLabel;
	@FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
	@FXML
	private TextField passwordTextField; // TextField hiển thị mật khẩu
	@FXML
	private Label messageLabel;
	@FXML
	private ImageView togglePasswordVisibilityIcon; // Icon con mắt
	@FXML
	private ImageView logoImage;
	@FXML private Button loginButton;

	private final AccountService accountService = new AccountService();
	private int failedLoginAttempts = 0; // Biến đếm số lần đăng nhập thất bại
	private String lastFailedUsername = null; // Lưu lại username đăng nhập thất bại gần nhất

	@FXML
	public void initialize() {
		LanguageManager.addListener(this); // Đăng ký lắng nghe nếu muốn thay đổi sau này
        loadTexts();
		// Tải logo vào ImageView
		logoImage.setImage(new Image(getClass().getResourceAsStream("/images/logo.png")));
		togglePasswordVisibilityIcon.setImage(new Image(getClass().getResourceAsStream("/images/hide.png")));
	}
	private void loadTexts() {
	    titleLabel.setText(LanguageManager.getString("login.title"));
	    usernameField.setPromptText(LanguageManager.getString("username.prompt"));
	    passwordField.setPromptText(LanguageManager.getString("password.prompt"));
	    passwordTextField.setPromptText(LanguageManager.getString("password.prompt"));
	    loginButton.setText(LanguageManager.getString("login.button"));
	}


	@FXML
	public void handleLogin() {
		String username = usernameField.getText().trim();
		String password = passwordField.isVisible() ? passwordField.getText().trim()
				: passwordTextField.getText().trim();

		try {
			Optional<Account> user = accountService.login(username, password);
	        Account account = user.orElseThrow(() -> new BusinessException(LanguageManager.getString("login.error.invalid_credentials")));

			// Reset số lần đăng nhập thất bại nếu đăng nhập thành công
			failedLoginAttempts = 0;
			lastFailedUsername = null;

			// Kiểm tra trạng thái active của tài khoản (vẫn giữ nguyên)
			if (!account.isActive()) {
				throw new BusinessException(LanguageManager.getString("login.error.locked_account"));
			}

			messageLabel.setText(LanguageManager.getString("login.success"));
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
				messageLabel.setText(LanguageManager.getString("login.error.account_locked_after_attempts", username));
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
	 * 
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
			throw new BusinessException(LanguageManager.getString("login.error.staff_quit"));
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

	@Override
	public void onLanguageChanged() {
		loadTexts();
	}
}