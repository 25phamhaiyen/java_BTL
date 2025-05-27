package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import utils.LanguageChangeListener;
import utils.LanguageManager;
import utils.LanguageManagerAd;

import java.util.Locale;

public class HomeController implements LanguageChangeListener {

	@FXML private Label lblTitle;
	@FXML private Button btnLogin;
	@FXML private ComboBox<String> languageCombo;

	@FXML
	public void initialize() {
		LanguageManagerAd.addListener(this); // Đăng ký lắng nghe sự kiện đổi ngôn ngữ
		
		languageCombo.getItems().addAll("Tiếng Việt", "English");
        languageCombo.setValue("Tiếng Việt");
        languageCombo.setOnAction(e -> {
            String lang = languageCombo.getValue();
            if (lang.equals("English")) {
                LanguageManagerAd.setLocale(new Locale("en", "US"));
            } else {
                LanguageManagerAd.setLocale(new Locale("vi", "VN"));
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
	    lblTitle.setText(LanguageManagerAd.getString("title"));
	    btnLogin.setText(LanguageManagerAd.getString("btn.login"));
	}


}

