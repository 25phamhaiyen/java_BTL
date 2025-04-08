package view.Staff;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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

public class RegistrationScreen {
    private VBox root;
    private Stage primaryStage;

    public RegistrationScreen(Stage stage) {
        this.primaryStage = stage;
        root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root");
        root.getStyleClass().add("dialog-container");

        Label titleLabel = new Label("Đăng ký tài khoản");
        titleLabel.getStyleClass().add("title-label");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Tên đăng nhập");
        usernameField.getStyleClass().add("text-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mật khẩu");
        passwordField.getStyleClass().add("password-field");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Xác nhận mật khẩu");
        confirmPasswordField.getStyleClass().add("password-field");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Họ và tên");
        fullNameField.getStyleClass().add("text-field");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Số điện thoại");
        phoneField.getStyleClass().add("text-field");

        TextField citizenNumberField = new TextField();
        citizenNumberField.setPromptText("Số CCCD");
        citizenNumberField.getStyleClass().add("text-field");

        TextField addressField = new TextField();
        addressField.setPromptText("Địa chỉ");
        addressField.getStyleClass().add("text-field");

        ComboBox<String> sexCombo = new ComboBox<>();
        sexCombo.getItems().addAll("Nam", "Nữ");
        sexCombo.setPromptText("Giới tính");
        sexCombo.getStyleClass().add("combo-box");

        TextField salaryField = new TextField();
        salaryField.setPromptText("Lương (VND)");
        salaryField.getStyleClass().add("text-field");

        ComboBox<String> workShiftCombo = new ComboBox<>();
        workShiftCombo.getItems().addAll("Morning", "Afternoon", "Evening");
        workShiftCombo.setPromptText("Ca làm việc");
        workShiftCombo.getStyleClass().add("combo-box");

        TextField positionField = new TextField();
        positionField.setPromptText("Vị trí");
        positionField.getStyleClass().add("text-field");

        Button registerButton = new Button("Đăng ký");
        registerButton.getStyleClass().addAll("button", "register-button");

        Button backButton = new Button("Quay lại");
        backButton.getStyleClass().addAll("button", "cancel-button");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(registerButton, backButton);

        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");

        registerButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            String fullName = fullNameField.getText();
            String phone = phoneField.getText();
            String citizenNumber = citizenNumberField.getText();
            String address = addressField.getText();
            String sex = sexCombo.getValue();
            String salaryStr = salaryField.getText();
            String workShift = workShiftCombo.getValue();
            String position = positionField.getText();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || fullName.isEmpty() || phone.isEmpty() || citizenNumber.isEmpty() || address.isEmpty() || sex == null || salaryStr.isEmpty() || workShift == null || position.isEmpty()) {
                messageLabel.getStyleClass().remove("message-success");
                messageLabel.getStyleClass().add("message-error");
                messageLabel.setText("Vui lòng điền đầy đủ thông tin!");
            } else if (!password.equals(confirmPassword)) {
                messageLabel.getStyleClass().remove("message-success");
                messageLabel.getStyleClass().add("message-error");
                messageLabel.setText("Mật khẩu xác nhận không khớp!");
            } else if (phone.length() != 10 || !phone.matches("\\d+")) {
                messageLabel.getStyleClass().remove("message-success");
                messageLabel.getStyleClass().add("message-error");
                messageLabel.setText("Số điện thoại phải có 10 chữ số!");
            } else if (citizenNumber.length() != 12 || !citizenNumber.matches("\\d+")) {
                messageLabel.getStyleClass().remove("message-success");
                messageLabel.getStyleClass().add("message-error");
                messageLabel.setText("Số CCCD phải có 12 chữ số!");
            } else {
                try {
                    double salary = Double.parseDouble(salaryStr);
                    if (registerStaff(username, password, fullName, phone, citizenNumber, address, sex, salary, workShift, position)) {
                        messageLabel.getStyleClass().remove("message-error");
                        messageLabel.getStyleClass().add("message-success");
                        messageLabel.setText("Đăng ký thành công!");
                        new Thread(() -> {
                            try {
                                Thread.sleep(2000);
                                javafx.application.Platform.runLater(() -> {
                                    primaryStage.getScene().setRoot(new LoginScreen(primaryStage).getRoot());
                                });
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }).start();
                    } else {
                        messageLabel.getStyleClass().remove("message-success");
                        messageLabel.getStyleClass().add("message-error");
                        messageLabel.setText("Tên đăng nhập hoặc số điện thoại đã tồn tại!");
                    }
                } catch (NumberFormatException ex) {
                    messageLabel.getStyleClass().remove("message-success");
                    messageLabel.getStyleClass().add("message-error");
                    messageLabel.setText("Lương phải là một số hợp lệ!");
                }
            }
        });

        backButton.setOnAction(e -> {
            primaryStage.getScene().setRoot(new LoginScreen(primaryStage).getRoot());
        });

        root.getChildren().addAll(titleLabel, usernameField, passwordField, confirmPasswordField, fullNameField, phoneField, citizenNumberField, addressField, sexCombo, salaryField, workShiftCombo, positionField, buttonBox, messageLabel);
    }

    private boolean registerStaff(String username, String password, String fullName, String phone, String citizenNumber, String address, String sex, double salary, String workShift, String position) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction

            // Tách họ và tên
            String[] nameParts = fullName.trim().split("\\s+");
            String lastName = nameParts[0];
            String firstName = nameParts.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(nameParts, 1, nameParts.length)) : "";

            // Thêm vào bảng person
            String personSql = "INSERT INTO person (lastName, firstName, phoneNumber, sex, citizenNumber, address) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement personStmt = conn.prepareStatement(personSql, PreparedStatement.RETURN_GENERATED_KEYS);
            personStmt.setString(1, lastName);
            personStmt.setString(2, firstName);
            personStmt.setString(3, phone);
            personStmt.setInt(4, sex.equals("Nam") ? 1 : 2); // 1: Nam, 2: Nữ
            personStmt.setString(5, citizenNumber);
            personStmt.setString(6, address);
            personStmt.executeUpdate();

            // Lấy PersonID vừa tạo
            ResultSet personRs = personStmt.getGeneratedKeys();
            if (!personRs.next()) {
                throw new SQLException("Không thể lấy PersonID.");
            }
            int personId = personRs.getInt(1);

            // Thêm vào bảng account
            String accountSql = "INSERT INTO account (UN_Username, Password, Email, Role_ID) VALUES (?, ?, ?, ?)";
            PreparedStatement accountStmt = conn.prepareStatement(accountSql, PreparedStatement.RETURN_GENERATED_KEYS);
            accountStmt.setString(1, username);
            accountStmt.setString(2, password); // Nên mã hóa password trong thực tế
            accountStmt.setString(3, null); // Email: null
            accountStmt.setInt(4, 3); // Role_ID: 3 (Nhân viên)
            accountStmt.executeUpdate();

            // Lấy AccountID vừa tạo
            ResultSet accountRs = accountStmt.getGeneratedKeys();
            if (!accountRs.next()) {
                throw new SQLException("Không thể lấy AccountID.");
            }
            int accountId = accountRs.getInt(1);

            // Thêm vào bảng staff
            String staffSql = "INSERT INTO staff (PersonID, Role_ID, AccountID, startDate, salary, workShift, position) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement staffStmt = conn.prepareStatement(staffSql);
            staffStmt.setInt(1, personId);
            staffStmt.setInt(2, 3); // Role_ID: 3 (Nhân viên)
            staffStmt.setInt(3, accountId);
            staffStmt.setDate(4, new java.sql.Date(System.currentTimeMillis())); // startDate: hôm nay
            staffStmt.setDouble(5, salary);
            staffStmt.setString(6, workShift);
            staffStmt.setString(7, position);
            staffStmt.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException ex) {
            System.err.println("Lỗi khi đăng ký: " + ex.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Lỗi khi rollback: " + rollbackEx.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    DatabaseConnection.closeConnection(conn);
                }
            } catch (SQLException ex) {
                System.err.println("Lỗi khi đóng kết nối: " + ex.getMessage());
            }
        }
    }

    public VBox getRoot() {
        return root;
    }
}