package controllers;

import backend.AccountService;
import entity.Role;
import exception.BusinessException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.SceneSwitcher;

import java.io.IOException;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private Label messageLabel;

    private final AccountService accountService = new AccountService();

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();

        try {
            Role customerRole = new Role(1, "CUSTOMER"); // Chỉ cho khách hàng
            accountService.register(username, password, email, customerRole);

            messageLabel.setText("Đăng ký thành công!");
            messageLabel.setStyle("-fx-text-fill: green;");
        } catch (BusinessException e) {
            messageLabel.setText(e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void switchToLogin() throws IOException {
    	 SceneSwitcher.switchScene("login.fxml", "Đăng nhập");
    }
}
