package controllers;

import java.io.IOException;

import exception.AccountException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Role;
import service.AccountService;

public class RegisterController {
	@FXML
	private TextField usernameField;
	@FXML
	private PasswordField passwordField;
	@FXML
	private TextField passwordTextField;
	@FXML
	private Label messageLabel;
	@FXML
	private ImageView togglePasswordVisibilityIcon;
	@FXML
	private TextField emailField;
	@FXML
	private ImageView logoImage;

	private final AccountService accountService = new AccountService();

	@FXML
	public void initialize() {
		// Tải logo vào ImageView
		Image image = new Image(getClass().getResourceAsStream("/images/logo.png"));
		logoImage.setImage(image);

		togglePasswordVisibilityIcon.setImage(new Image(getClass().getResourceAsStream("/images/hide.png")));

	}

	@FXML
	private void handleRegister() {
		String username = usernameField.getText().trim();
		String password = passwordField.getText().trim();
		String email = emailField.getText().trim();

		try {
			Role customerRole = new Role(4, "CUSTOMER"); // Mặc định là khách hàng
			boolean isRegistered = accountService.register(username, password, email, customerRole);

			if (isRegistered) {
				showMessage("Đăng ký thành công!", "green");
			} else {
				showMessage("Đăng ký thất bại! Vui lòng thử lại.", "red");
			}
		} catch (AccountException e) {
			showMessage(e.getMessage(), "red");
		} catch (Exception e) {
			showMessage("Lỗi hệ thống! Vui lòng thử lại sau.", "red");
			e.printStackTrace();
		}
	}

	private void showMessage(String message, String color) {
		messageLabel.setText(message);
		messageLabel.setStyle("-fx-text-fill: " + color + ";");
	}

	@FXML
	private void switchToLogin() throws IOException {
		SceneSwitcher.switchScene("login.fxml");
	}

	@FXML
	public void togglePasswordVisibility() {
		if (passwordField.isVisible()) {
			// Chuyển sang TextField để hiển thị mật khẩu
			passwordField.setVisible(false);
			passwordTextField.setVisible(true);
			passwordTextField.setText(passwordField.getText());
			// Đổi icon mắt
			togglePasswordVisibilityIcon.setImage(new Image(getClass().getResourceAsStream("/images/show.png")));
		} else {
			// Quay lại PasswordField để ẩn mật khẩu
			passwordField.setVisible(true);
			passwordTextField.setVisible(false);
			passwordField.setText(passwordTextField.getText());
			// Đổi icon mắt bị gạch chéo
			togglePasswordVisibilityIcon.setImage(new Image(getClass().getResourceAsStream("/images/hide.png")));
		}
	}

}
