package controllers;

import java.text.MessageFormat;
import java.util.Locale;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import model.Role;
import utils.LanguageChangeListener;
import utils.LanguageManagerAd;
import utils.LanguageManagerStaff;
import utils.Session;

public class DashboardController implements LanguageChangeListener {
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
    private ComboBox<String> languageCombo;

    @FXML
    public void initialize() {
        // Register for language updates
        LanguageManagerAd.addListener(this);
        
        System.out.println("Current User: " + Session.getCurrentAccount());
        
        // Set up language combo box
        setupLanguageCombo();
        
        // Set up UI based on current user
        setupUserInterface();
        
        // Set up event handlers
        btnLogout.setOnAction(event -> handleLogout());
        
        // Set up key event handlers
        setupKeyHandlers();
        
        // Initial language setup
        loadTexts();
    }
    
    private void setupKeyHandlers() {
        // Handle Enter key press on language combo box
        languageCombo.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // Focus the appropriate continue button based on user role
                if (btnAdminPanel.isVisible()) {
                    btnAdminPanel.requestFocus();
                } else if (btnEmployeePanel.isVisible()) {
                    btnEmployeePanel.requestFocus();
                }
            }
        });
        
        // Handle Enter key press on admin panel button
        btnAdminPanel.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleAdminPanel();
            }
        });
        
        // Handle Enter key press on employee panel button
        btnEmployeePanel.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleEmployeePanel();
            }
        });
        
        // Handle Enter key press on logout button
        btnLogout.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogout();
            }
        });
    }
    
    private void setupLanguageCombo() {
        languageCombo.getItems().addAll("Tiếng Việt", "English");
        
        // Set current language based on current locale
        Locale currentLocale = LanguageManagerAd.getCurrentLocale();
        if (currentLocale != null && currentLocale.getLanguage().equals("en")) {
            languageCombo.setValue("English");
        } else {
            languageCombo.setValue("Tiếng Việt");
        }
        
        // Handle language change
        languageCombo.setOnAction(e -> {
            String lang = languageCombo.getValue();
            if (lang.equals("English")) {
                LanguageManagerAd.setLocale(new Locale("en", "US"));
                LanguageManagerStaff.setLocale(new Locale("en", "US"));
            } else {
                LanguageManagerAd.setLocale(new Locale("vi", "VN"));
                LanguageManagerStaff.setLocale(new Locale("vi", "VN"));
            }
        });
    }
    
    @Override
    public void onLanguageChanged() {
        loadTexts();
    }
    
    private void loadTexts() {
        // Update button texts
        btnLogout.setText(LanguageManagerAd.getString("dashboard.logout"));
        
        // Update info message
        lblMessage.setText(LanguageManagerAd.getString("dashboard.info.message"));
       
        btnAdminPanel.setText(LanguageManagerAd.getString("dashboard.continue"));
        btnEmployeePanel.setText(LanguageManagerAd.getString("dashboard.continue"));
        
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
                        btnAdminPanel.requestFocus(); // Auto-focus the continue button
                        break;
                    case 2: // STAFF_CARE
                    case 3: // STAFF_CASHIER
                    case 4: // STAFF_RECEPTION
                        btnEmployeePanel.setVisible(true);
                        btnEmployeePanel.requestFocus(); // Auto-focus the continue button
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
        if (Session.getCurrentAccount() != null) {
            Role role = Session.getCurrentAccount().getRole();
            String username = Session.getCurrentAccount().getUserName();
            
            if (role != null) {
                switch (role.getRoleID()) {
                case 5: // OUT - terminated
                    lblWelcome.setText(LanguageManagerAd.getString("dashboard.account.terminated"));
                    break;
                case 1: // admin
                case 2: // STAFF_CARE
                case 3: // STAFF_CASHIER
                case 4: // STAFF_RECEPTION
                    String welcomeMessage = LanguageManagerAd.getString("dashboard.welcome");
                    lblWelcome.setText(MessageFormat.format(welcomeMessage, username));
                    break;
                default:
                    lblWelcome.setText(LanguageManagerAd.getString("dashboard.role.undefined"));
                    break;
                }
            } else {
                lblWelcome.setText(LanguageManagerAd.getString("dashboard.role.undefined"));
            }
        } else {
            lblWelcome.setText(LanguageManagerAd.getString("dashboard.please.login"));
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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(LanguageManagerAd.getString("app.confirm"));
        alert.setContentText(LanguageManagerAd.getString("msg.confirm.logout"));
        
        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                Session.logout();
                SceneSwitcher.switchScene("login.fxml");
            }
        });
    }
}