package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import utils.I18nUtil;
import utils.LanguageManager;
import java.util.Locale;

public class HomeController implements I18nUtil.I18nUpdatable {
    @FXML
    private Label title;
    @FXML
    private Button btnLogin, btnLanguage;

    @FXML
    public void initialize() {
        // Register for language updates
        I18nUtil.register(this);
        
        // Initial language setup
        updateLanguage();
        
        // Set up event handlers
        btnLogin.setOnAction(this::handleLogin);
        btnLanguage.setOnAction(this::toggleLanguage);
    }
    
    @Override
    public void updateLanguage() {
        LanguageManager langManager = LanguageManager.getInstance();
        
        // Update text based on current language
        title.setText(langManager.getString("home.title"));
        btnLogin.setText(langManager.getString("home.login"));
        btnLanguage.setText(langManager.getString("home.language"));
    }

    private void handleLogin(ActionEvent event) {
        SceneSwitcher.switchScene("login.fxml");
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