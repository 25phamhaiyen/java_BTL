package controllers;

import backend.AccountService;
import entity.Account;
import exception.BusinessException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import utils.SceneSwitcher;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final AccountService accountService = new AccountService();

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        try {
        	Optional<Account> optionalAccount  = accountService.login(username, password);
        	Account account = optionalAccount.orElseThrow(() -> new BusinessException("Sai tên đăng nhập hoặc mật khẩu"));
            messageLabel.setText("Đăng nhập thành công!");
            messageLabel.setStyle("-fx-text-fill: green;");

        } catch (BusinessException e) {
            messageLabel.setText(e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    public void switchToRegister() throws IOException {
    	 SceneSwitcher.switchScene("register.fxml", "Đăng ký");
    }
}
