package controllers.Staff;

import javafx.geometry.Insets;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginScreen {
    private VBox root;
    private Stage primaryStage;
    private String loggedInUsername; // Lưu username của người đăng nhập

    public LoginScreen(Stage stage) {
        this.primaryStage = stage;
        root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root");
        root.getStyleClass().add("login-container");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Tên đăng nhập");
        usernameField.getStyleClass().add("text-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mật khẩu");
        passwordField.getStyleClass().add("password-field");

        Button loginButton = new Button("Đăng nhập");
        loginButton.getStyleClass().addAll("button", "login-button");

        Button registerButton = new Button("Đăng ký");
        registerButton.getStyleClass().addAll("button", "register-button");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(loginButton, registerButton);

        Label errorLabel = new Label("");
        errorLabel.getStyleClass().add("message-label");

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (validateLogin(username, password)) {
                loggedInUsername = username; // Lưu username để sử dụng sau này
                CashierDashboard dashboard = new CashierDashboard(primaryStage, loggedInUsername);
                primaryStage.getScene().setRoot(dashboard.getRoot());
            } else {
                errorLabel.getStyleClass().remove("message-success");
                errorLabel.getStyleClass().add("message-error");
                errorLabel.setText("Sai tên đăng nhập hoặc mật khẩu");
            }
        });

        registerButton.setOnAction(e -> {
            RegistrationScreen registrationScreen = new RegistrationScreen(primaryStage);
            primaryStage.getScene().setRoot(registrationScreen.getRoot());
        });

        root.getChildren().addAll(usernameField, passwordField, buttonBox, errorLabel);
    }

    private boolean validateLogin(String username, String password) {
        String sql = "SELECT a.*, r.RoleName " +
                     "FROM account a " +
                     "JOIN role r ON a.Role_ID = r.Role_ID " +
                     "WHERE a.UN_Username = ? AND a.Password = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String roleName = rs.getString("RoleName");
                // Chỉ cho phép nhân viên (role "Nhân viên") đăng nhập vào giao diện này
                return roleName.equals("Nhân viên");
            }
            return false;
        } catch (SQLException ex) {
            System.err.println("Lỗi khi kiểm tra đăng nhập: " + ex.getMessage());
            return false;
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
    }

    public VBox getRoot() {
        return root;
    }
}