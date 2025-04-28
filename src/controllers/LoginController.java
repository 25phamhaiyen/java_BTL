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
			if (failedLoginAttempts >= 5) {
				messageLabel.setText("Tài khoản đã bị khóa tạm thời sau 5 lần sai mật khẩu!");
				messageLabel.setStyle("-fx-text-fill: red;");
				return;
			}

			Optional<Account> user = accountService.login(username, password);
			Account account = user.orElseThrow(() -> new BusinessException("Sai tên đăng nhập hoặc mật khẩu"));

			messageLabel.setText("Đăng nhập thành công!");
			messageLabel.setStyle("-fx-text-fill: green;");
			Session.setCurrentAccount(account);

			failedLoginAttempts = 0; // Reset lại số lần login fail
			SceneSwitcher.switchScene("dashboard.fxml");

		} catch (BusinessException e) {
			failedLoginAttempts++;
			messageLabel.setText(e.getMessage());
			messageLabel.setStyle("-fx-text-fill: red;");
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
