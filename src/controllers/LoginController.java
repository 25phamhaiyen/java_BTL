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
import utils.I18nUtil;
import utils.LanguageManager;
import utils.Session;

public class LoginController implements I18nUtil.I18nUpdatable {
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
    private Button btnLogin, btnLanguage;
    @FXML
    private ImageView togglePasswordVisibilityIcon;
    @FXML
    private ImageView logoImage;

    private final AccountService accountService = new AccountService();
    private int failedLoginAttempts = 0;
    private String lastFailedUsername = null;

    @FXML
    public void initialize() {
        // Register for language updates
        I18nUtil.register(this);
        
        // Load images
        logoImage.setImage(new Image(getClass().getResourceAsStream("/images/logo.png")));
        togglePasswordVisibilityIcon.setImage(new Image(getClass().getResourceAsStream("/images/hide.png")));
        
        // Initial language setup
        updateLanguage();
        
        // Set up event handlers
        btnLanguage.setOnAction(this::toggleLanguage);
    }
    
    @Override
    public void updateLanguage() {
        LanguageManager langManager = LanguageManager.getInstance();
        
        // Update title label
        titleLabel.setText(langManager.getString("login.title"));
        
        // Update button texts
        btnLogin.setText(langManager.getString("login.button"));
        btnLanguage.setText(langManager.getString("login.language.toggle"));
        
        // Update placeholder texts
        usernameField.setPromptText(langManager.getString("login.username"));
        passwordField.setPromptText(langManager.getString("login.password"));
        passwordTextField.setPromptText(langManager.getString("login.password"));
        
        // Clear any existing messages when language changes
        if (messageLabel.getText() != null && !messageLabel.getText().isEmpty()) {
            messageLabel.setText("");
        }
    }

    @FXML
    public void handleLogin() {
        LanguageManager langManager = LanguageManager.getInstance();
        String username = usernameField.getText().trim();
        String password = passwordField.isVisible() ? passwordField.getText().trim()
                : passwordTextField.getText().trim();

        try {
            Optional<Account> user = accountService.login(username, password);
            Account account = user.orElseThrow(() -> new BusinessException(langManager.getString("login.error")));

            // Reset failed login attempts on successful login
            failedLoginAttempts = 0;
            lastFailedUsername = null;

            // Check if account is active
            if (!account.isActive()) {
                throw new BusinessException(langManager.getString("login.account.locked"));
            }

            messageLabel.setText(langManager.getString("login.success"));
            messageLabel.setStyle("-fx-text-fill: green;");
            Session.setCurrentAccount(account);

            // Route to appropriate view based on role
            routeBasedOnRole(account.getRole());

        } catch (BusinessException e) {
            failedLoginAttempts++;
            messageLabel.setText(e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");

            // Check if reached failed login threshold
            if (failedLoginAttempts >= 5 && username.equals(lastFailedUsername)) {
                // Lock account
                accountService.lockAccount(username);
                messageLabel.setText(langManager.getString("login.account.locked.attempts", username));
                messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                failedLoginAttempts = 0;
                lastFailedUsername = null;
            } else {
                lastFailedUsername = username;
            }
        }
    }

    private void routeBasedOnRole(Role role) {
        LanguageManager langManager = LanguageManager.getInstance();
        
        if (role == null) {
            SceneSwitcher.switchScene("dashboard.fxml");
            return;
        }

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
            throw new BusinessException(langManager.getString("login.account.out"));
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
    
    private void toggleLanguage(ActionEvent event) {
        LanguageManager langManager = LanguageManager.getInstance();
        Locale currentLocale = langManager.getCurrentLocale();
        
        // Toggle between Vietnamese and English
        if (currentLocale.equals(LanguageManager.VIETNAMESE)) {
            I18nUtil.switchLanguage(LanguageManager.ENGLISH);
        } else {
            I18nUtil.switchLanguage(LanguageManager.VIETNAMESE);
        }
    }
}