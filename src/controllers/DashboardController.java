package controllers;

import java.util.Locale;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import model.Role;
import utils.I18nUtil;
import utils.LanguageManager;
import utils.Session;

public class DashboardController implements I18nUtil.I18nUpdatable {
    @FXML
    private Label lblWelcome;
    @FXML
    private Label lblMessage;
    @FXML
    private Button btnAdminPanel;
    @FXML
    private Button btnEmployeePanel;
    @FXML
    private Button btnLogout;
    @FXML
    private Button btnLanguage;

    @FXML
    public void initialize() {
        // Register for language updates
        I18nUtil.register(this);
        
        System.out.println("Current User: " + Session.getCurrentAccount());
        
        // Set up UI based on current user
        setupUserInterface();
        
        // Set up event handlers
        btnLogout.setOnAction(event -> handleLogout());
        btnLanguage.setOnAction(this::toggleLanguage);
        
        // Initial language setup
        updateLanguage();
    }
    
    @Override
    public void updateLanguage() {
        LanguageManager langManager = LanguageManager.getInstance();
        
        // Update button texts
        btnLogout.setText(langManager.getString("dashboard.logout"));
        btnLanguage.setText(langManager.getString("dashboard.language.toggle"));
        
        // Update info message
        lblMessage.setText(langManager.getString("dashboard.info.message"));
       
            btnAdminPanel.setText(langManager.getString("dashboard.continue"));
        
            btnEmployeePanel.setText(langManager.getString("dashboard.continue"));
        
        // Update welcome message
        updateWelcomeMessage();
    }
    
    private void setupUserInterface() {
        if (Session.getCurrentAccount() != null) {
            Role role = Session.getCurrentAccount().getRole();
            
            // Use Platform.runLater to ensure UI is updated after successful login
            Platform.runLater(() -> {
                if (role != null) {
                    switch (role.getRoleID()) {
                    case 1: // admin
                        btnAdminPanel.setVisible(true);
                        break;
                    case 2: // STAFF_CARE
                    case 3: // STAFF_CASHIER
                    case 4: // STAFF_RECEPTION
                        btnEmployeePanel.setVisible(true);
                        break;
                    case 5: // OUT - terminated
                        // This case will be handled in updateWelcomeMessage()
                        break;
                    default:
                        // This case will be handled in updateWelcomeMessage()
                        break;
                    }
                }
                updateWelcomeMessage();
            });
        } else {
            updateWelcomeMessage();
        }
    }
    
    private void updateWelcomeMessage() {
        LanguageManager langManager = LanguageManager.getInstance();
        
        if (Session.getCurrentAccount() != null) {
            Role role = Session.getCurrentAccount().getRole();
            String username = Session.getCurrentAccount().getUserName();
            
            if (role != null) {
                switch (role.getRoleID()) {
                case 5: // OUT - terminated
                    lblWelcome.setText(langManager.getString("dashboard.account.terminated"));
                    break;
                case 1: // admin
                case 2: // STAFF_CARE
                case 3: // STAFF_CASHIER
                case 4: // STAFF_RECEPTION
                    lblWelcome.setText(langManager.getString("dashboard.welcome", username));
                    break;
                default:
                    lblWelcome.setText(langManager.getString("dashboard.role.undefined"));
                    break;
                }
            } else {
                lblWelcome.setText(langManager.getString("dashboard.role.undefined"));
            }
        } else {
            lblWelcome.setText(langManager.getString("dashboard.please.login"));
        }
    }

    @FXML
    private void handleAdminPanel() {
        SceneSwitcher.switchScene("admin/admin_home.fxml");
    }

    @FXML
    private void handleEmployeePanel() {
        SceneSwitcher.switchScene("staff/staff_home.fxml");
    }

    @FXML
    private void handleLogout() {
        // Show confirmation dialog
        if (I18nUtil.showConfirmation("app.confirm", "msg.confirm.logout")) {
            Session.logout();
            SceneSwitcher.switchScene("login.fxml");
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