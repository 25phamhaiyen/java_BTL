package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;

public class HomeController {

    @FXML private Label title;
    @FXML private Button btnLogin, btnRegister, btnLanguage;
    private boolean isEnglish = false;

    @FXML
    public void initialize() {
        updateLanguage();
        btnLogin.setOnAction(this::handleLogin);
        btnRegister.setOnAction(this::handleRegister);
        btnLanguage.setOnAction(this::toggleLanguage);
    }

    private void handleLogin(ActionEvent event) {
        SceneSwitcher.switchScene("login.fxml");
    }

    private void handleRegister(ActionEvent event) {
        SceneSwitcher.switchScene("register.fxml");
    }

    private void toggleLanguage(ActionEvent event) {
        isEnglish = !isEnglish;
        updateLanguage();
    }

    private void updateLanguage() {
        if (isEnglish) {
            title.setText("Pet Care");
            btnLogin.setText("Login");
            btnRegister.setText("Register");
            btnLanguage.setText("EN|VN");
        } else {
            title.setText("Chăm sóc thú cưng");
            btnLogin.setText("Đăng nhập");
            btnRegister.setText("Đăng ký");
            btnLanguage.setText("VN|GB");
        }
    }
}
