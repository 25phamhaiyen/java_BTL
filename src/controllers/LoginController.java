package controllers;

import java.util.Locale;
import java.util.Optional;

import exception.BusinessException;
import javafx.event.ActionEvent;
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
import utils.LanguageManagerAd;
import utils.Session;

public class LoginController implements LanguageChangeListener {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private Label messageLabel;
    @FXML
    private Label titleLabel;
    @FXML
    private Button btnLogin;
    @FXML
    private ImageView togglePasswordVisibilityIcon;
    @FXML
    private ImageView logoImage;

    private final AccountService accountService = new AccountService();
    private int failedLoginAttempts = 0;
    private String lastFailedUsername = null;

    @FXML
    public void initialize() {
        // Register for language updates with LanguageManagerAd
        LanguageManagerAd.addListener(this);
        
        // Load images
        try {
            logoImage.setImage(new Image(getClass().getResourceAsStream("/images/logo.png")));
            togglePasswordVisibilityIcon.setImage(new Image(getClass().getResourceAsStream("/images/hide.png")));
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
        }
        
        // Initial language setup
        loadTexts();
    }
    
    @Override
    public void onLanguageChanged() {
        // Được gọi khi ngôn ngữ thay đổi từ HomeController hoặc bất kỳ đâu
        System.out.println("Language changed event received in LoginController");
        loadTexts();
    }
    
    private void loadTexts() {
        try {
            // Update title label
            titleLabel.setText(LanguageManagerAd.getString("login.title"));
            
            // Update button texts
            btnLogin.setText(LanguageManagerAd.getString("login.button"));
            
            // Update placeholder texts
            usernameField.setPromptText(LanguageManagerAd.getString("login.username"));
            passwordField.setPromptText(LanguageManagerAd.getString("login.password"));
            passwordTextField.setPromptText(LanguageManagerAd.getString("login.password"));
            
            // Clear any existing messages when language changes
            if (messageLabel.getText() != null && !messageLabel.getText().isEmpty()) {
                messageLabel.setText("");
                messageLabel.setStyle("");
            }
            
            System.out.println("Login texts updated successfully");
        } catch (Exception e) {
            System.err.println("Error updating login texts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.isVisible() ? passwordField.getText().trim()
                : passwordTextField.getText().trim();

        try {
            Optional<Account> user = accountService.login(username, password);
            Account account = user.orElseThrow(() -> new BusinessException(LanguageManagerAd.getString("login.error")));

            // Reset failed login attempts on successful login
            failedLoginAttempts = 0;
            lastFailedUsername = null;

            // Check if account is active
            if (!account.isActive()) {
                throw new BusinessException(LanguageManagerAd.getString("login.account.locked"));
            }

            messageLabel.setText(LanguageManagerAd.getString("login.success"));
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
                try {
                    accountService.lockAccount(username);
                    messageLabel.setText(LanguageManagerAd.getString("login.account.locked.attempts", username));
                    messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    failedLoginAttempts = 0; // Reset lại sau khi khóa
                    lastFailedUsername = null;
                } catch (Exception lockException) {
                    System.err.println("Error locking account: " + lockException.getMessage());
                }
            } else {
                lastFailedUsername = username; // Cập nhật username thất bại gần nhất
            }
        } catch (Exception e) {
            messageLabel.setText(LanguageManagerAd.getString("login.error.general"));
            messageLabel.setStyle("-fx-text-fill: red;");
            System.err.println("Unexpected error during login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void routeBasedOnRole(Role role) {
        if (role == null) {
            SceneSwitcher.switchScene("dashboard.fxml");
            return;
        }
        
        try {
            switch (role.getRoleName().toUpperCase()) {
                case "ADMIN":
                    SceneSwitcher.switchScene("dashboard.fxml");
                    break;
                case "STAFF_CARE":
                case "STAFF_CASHIER":
                case "STAFF_RECEPTION":
                    SceneSwitcher.switchScene("dashboard.fxml");
                    break;
                case "OUT":
                    throw new BusinessException(LanguageManagerAd.getString("login.account.out"));
                default:
                    SceneSwitcher.switchScene("dashboard.fxml");
                    break;
            }
        } catch (BusinessException e) {
            messageLabel.setText(e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        } catch (Exception e) {
            messageLabel.setText(LanguageManagerAd.getString("login.error.navigation"));
            messageLabel.setStyle("-fx-text-fill: red;");
            System.err.println("Error during navigation: " + e.getMessage());
        }
    }

    @FXML
    public void togglePasswordVisibility() {
        try {
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
        } catch (Exception e) {
            System.err.println("Error toggling password visibility: " + e.getMessage());
        }
    }
    
}