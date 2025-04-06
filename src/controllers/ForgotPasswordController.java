package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.AccountService;
import exception.AccountException;

public class ForgotPasswordController {
    @FXML
    private TextField emailField;
    @FXML
    private Label messageLabel;

    private final AccountService accountService = new AccountService();

    @FXML
    public void handleSubmit() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            messageLabel.setText("Vui lòng nhập email!");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            accountService.forgotPassword(email);
            messageLabel.setText("Hãy kiểm tra email của bạn!");
            messageLabel.setStyle("-fx-text-fill: green;");
        } catch (AccountException e) {
            messageLabel.setText(e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    public void closeWindow() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.close();
    }
}
