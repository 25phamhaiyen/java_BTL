package controllers;

import exception.BusinessException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.Account;
import service.AccountService;
import utils.Session;

import java.io.IOException;
import java.util.Optional;


public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final AccountService accountService = new AccountService();

    @FXML private ImageView logoImage;

    @FXML
    public void initialize() {
    	Image image = new Image(getClass().getResourceAsStream("/images/logo.jpg"));
        logoImage.setImage(image);
    }
    
    
    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        try {
        	Optional<Account> user  = accountService.login(username, password);
        	Account account = user.orElseThrow(() -> new BusinessException("Sai tên đăng nhập hoặc mật khẩu"));
            messageLabel.setText("Đăng nhập thành công!");
            Session.setCurrentUser(account); 
            messageLabel.setStyle("-fx-text-fill: green;");
            
            SceneSwitcher.switchScene("dashboard.fxml");


        } catch (BusinessException e) {
            messageLabel.setText(e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    public void switchToRegister() throws IOException {
    	 SceneSwitcher.switchScene("register.fxml");
    }
}
