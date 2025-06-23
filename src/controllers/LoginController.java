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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
        
        // Add key event handlers for Enter key
        setupEnterKeyHandlers();
    }
    
    private void setupEnterKeyHandlers() {
        // When Enter is pressed in username field, move focus to password field
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (passwordField.isVisible()) {
                    passwordField.requestFocus();
                } else {
                    passwordTextField.requestFocus();
                }
            }
        });
        
        // When Enter is pressed in password field (visible or hidden), trigger login
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
        
        passwordTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
    }
    
    @Override
    public void onLanguageChanged() {
        System.out.println("Language changed event received in LoginController");
        loadTexts();
    }
    
    private void loadTexts() {
        try {
            titleLabel.setText(LanguageManagerAd.getString("login.title"));
            btnLogin.setText(LanguageManagerAd.getString("login.button"));
            usernameField.setPromptText(LanguageManagerAd.getString("login.username"));
            passwordField.setPromptText(LanguageManagerAd.getString("login.password"));
            passwordTextField.setPromptText(LanguageManagerAd.getString("login.password"));
            
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

            failedLoginAttempts = 0;
            lastFailedUsername = null;

            if (!account.isActive()) {
                throw new BusinessException(LanguageManagerAd.getString("login.account.locked"));
            }

            messageLabel.setText(LanguageManagerAd.getString("login.success"));
            messageLabel.setStyle("-fx-text-fill: green;");
            Session.setCurrentAccount(account);

            routeBasedOnRole(account.getRole());

        } catch (BusinessException e) {
            failedLoginAttempts++;
            messageLabel.setText(e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");

            if (failedLoginAttempts >= 5 && username.equals(lastFailedUsername)) {
                try {
                    accountService.lockAccount(username);
                    messageLabel.setText(LanguageManagerAd.getString("login.account.locked.attempts", username));
                    messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    failedLoginAttempts = 0;
                    lastFailedUsername = null;
                } catch (Exception lockException) {
                    System.err.println("Error locking account: " + lockException.getMessage());
                }
            } else {
                lastFailedUsername = username;
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
            SceneSwitcher.switchScene("dashboard.fxml, true");
            return;
        }
        
        try {
            switch (role.getRoleName().toUpperCase()) {
                case "ADMIN":
                    SceneSwitcher.switchScene("dashboard.fxml", true);
                    break;
                case "STAFF_CARE":
                case "STAFF_CASHIER":
                case "STAFF_RECEPTION":
                    SceneSwitcher.switchScene("dashboard.fxml", true);
                    break;
                case "OUT":
                    throw new BusinessException(LanguageManagerAd.getString("login.account.out"));
                default:
                    SceneSwitcher.switchScene("dashboard.fxml", true);
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