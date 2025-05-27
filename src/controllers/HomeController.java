package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import utils.LanguageChangeListener;
import utils.LanguageManager;

import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;

public class HomeController implements LanguageChangeListener {

	@FXML private Label lblTitle;
	@FXML private Button btnLogin;
	@FXML private ComboBox<String> languageCombo;

	@FXML
	public void initialize() {
		LanguageManager.addListener(this); // Đăng ký lắng nghe sự kiện đổi ngôn ngữ
		
		languageCombo.getItems().addAll("Tiếng Việt", "English");
        languageCombo.setValue("Tiếng Việt");
        languageCombo.setOnAction(e -> {
            String lang = languageCombo.getValue();
            if (lang.equals("English")) {
                LanguageManager.setLocale(new Locale("en", "US"));
            } else {
                LanguageManager.setLocale(new Locale("vi", "VN"));
            }
        });

        loadTexts(); // Gọi khi khởi tạo
        
		btnLogin.setOnAction(this::handleLogin);
	}

	private void handleLogin(ActionEvent event) {
		SceneSwitcher.switchScene("login.fxml");
	}

	@Override
	public void onLanguageChanged() {
		loadTexts();
	}
	private void loadTexts() {
	    lblTitle.setText(LanguageManager.getString("title"));
	    btnLogin.setText(LanguageManager.getString("btn.login"));
	}


}
