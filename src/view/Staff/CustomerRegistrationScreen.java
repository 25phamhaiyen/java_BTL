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
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerRegistrationScreen extends Stage {
    private VBox root;
    private Runnable successCallback;
    private int newCustomerId = -1;

    public CustomerRegistrationScreen(String phoneNumber, Runnable successCallback) {
        this.successCallback = successCallback;
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Đăng ký khách hàng mới");
        setMinWidth(400);
        setMinHeight(500);

        root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root");
        root.getStyleClass().add("dialog-container");

        Label titleLabel = new Label("Đăng ký khách hàng mới");
        titleLabel.getStyleClass().add("title-label");

        // Thông tin tài khoản
        Label accountSectionLabel = new Label("Thông tin tài khoản");
        accountSectionLabel.getStyleClass().add("section-label");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Tên đăng nhập");
        usernameField.getStyleClass().add("text-field");
        // Tạo username mặc định từ số điện thoại
        usernameField.setText("customer_" + phoneNumber);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mật khẩu");
        passwordField.getStyleClass().add("password-field");
        // Mật khẩu mặc định là 8 số đầu tiên của SĐT
        passwordField.setText(phoneNumber.length() >= 8 ? phoneNumber.substring(0, 8) : phoneNumber);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Xác nhận mật khẩu");
        confirmPasswordField.getStyleClass().add("password-field");
        confirmPasswordField.setText(passwordField.getText());

        // Thông tin cá nhân
        Label personalSectionLabel = new Label("Thông tin cá nhân");
        personalSectionLabel.getStyleClass().add("section-label");

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Họ");
        lastNameField.getStyleClass().add("text-field");

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Tên");
        firstNameField.getStyleClass().add("text-field");

        TextField phoneField = new TextField(phoneNumber);
        phoneField.setPromptText("Số điện thoại");
        phoneField.setDisable(true); // Đã có số điện thoại, không cho sửa
        phoneField.getStyleClass().add("text-field");

        TextField citizenNumberField = new TextField();
        citizenNumberField.setPromptText("Số CCCD (12 chữ số)");
        citizenNumberField.getStyleClass().add("text-field");

        TextField addressField = new TextField();
        addressField.setPromptText("Địa chỉ");
        addressField.getStyleClass().add("text-field");

        ComboBox<String> sexCombo = new ComboBox<>();
        sexCombo.getItems().addAll("Nam", "Nữ");
        sexCombo.setValue("Nam"); // Mặc định là Nam
        sexCombo.getStyleClass().add("combo-box");

        // Nút đăng ký và hủy
        Button registerButton = new Button("Đăng ký");
        registerButton.getStyleClass().addAll("button", "register-button");

        Button cancelButton = new Button("Hủy");
        cancelButton.getStyleClass().addAll("button", "cancel-button");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(registerButton, cancelButton);

        Label messageLabel = new Label("");
        messageLabel.getStyleClass().add("message-label");

        registerButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            String lastName = lastNameField.getText();
            String firstName = firstNameField.getText();
            String phone = phoneField.getText();
            String citizenNumber = citizenNumberField.getText();
            String address = addressField.getText();
            String sex = sexCombo.getValue();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() 
                || lastName.isEmpty() || firstName.isEmpty() || phone.isEmpty() 
                || citizenNumber.isEmpty() || address.isEmpty() || sex == null) {
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
                if (registerCustomer(username, password, lastName, firstName, phone, citizenNumber, address, sex)) {
                    messageLabel.getStyleClass().remove("message-error");
                    messageLabel.getStyleClass().add("message-success");
                    messageLabel.setText("Đăng ký thành công!");
                    
                    // Chờ 1.5 giây rồi đóng dialog và gọi callback
                    new Thread(() -> {
                        try {
                            Thread.sleep(1500);
                            javafx.application.Platform.runLater(() -> {
                                if (successCallback != null) {
                                    successCallback.run();
                                }
                                close();
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
            }
        });

        cancelButton.setOnAction(e -> close());

        // Thêm tất cả các thành phần vào root
        root.getChildren().addAll(
            titleLabel,
            accountSectionLabel,
            usernameField, passwordField, confirmPasswordField,
            personalSectionLabel,
            lastNameField, firstNameField, phoneField,
            sexCombo, citizenNumberField, addressField,
            buttonBox, messageLabel
        );

        // Tạo scene và set CSS
        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        scene.getStylesheets().add(getClass().getResource("/view/Staff/Staff.css").toExternalForm());
        setScene(scene);
    }

    private boolean registerCustomer(String username, String password, String lastName, String firstName, 
                                   String phone, String citizenNumber, String address, String sex) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction

            // 1. Kiểm tra xem tên đăng nhập đã tồn tại chưa
            String checkUserSql = "SELECT COUNT(*) FROM account WHERE UN_Username = ?";
            PreparedStatement checkUserStmt = conn.prepareStatement(checkUserSql);
            checkUserStmt.setString(1, username);
            ResultSet userRs = checkUserStmt.executeQuery();
            userRs.next();
            if (userRs.getInt(1) > 0) {
                return false; // Tên đăng nhập đã tồn tại
            }

            // 2. Kiểm tra xem số điện thoại đã tồn tại chưa
            String checkPhoneSql = "SELECT COUNT(*) FROM person WHERE phoneNumber = ?";
            PreparedStatement checkPhoneStmt = conn.prepareStatement(checkPhoneSql);
            checkPhoneStmt.setString(1, phone);
            ResultSet phoneRs = checkPhoneStmt.executeQuery();
            phoneRs.next();
            if (phoneRs.getInt(1) > 0) {
                return false; // Số điện thoại đã tồn tại
            }

            // 3. Thêm vào bảng person
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
            newCustomerId = personRs.getInt(1);

            // 4. Thêm vào bảng account
            String accountSql = "INSERT INTO account (UN_Username, Password, Email, Role_ID) VALUES (?, ?, ?, ?)";
            PreparedStatement accountStmt = conn.prepareStatement(accountSql, PreparedStatement.RETURN_GENERATED_KEYS);
            accountStmt.setString(1, username);
            accountStmt.setString(2, password); 
            accountStmt.setString(3, null); // Email: null
            accountStmt.setInt(4, 4); // Role_ID: 4 (Khách hàng)
            accountStmt.executeUpdate();

            // Lấy AccountID vừa tạo
            ResultSet accountRs = accountStmt.getGeneratedKeys();
            if (!accountRs.next()) {
                throw new SQLException("Không thể lấy AccountID.");
            }
            int accountId = accountRs.getInt(1);

            // 5. Thêm vào bảng customer
            String customerSql = "INSERT INTO customer (PersonID, AccountID, registrationDate, loyaltyPoints) VALUES (?, ?, NOW(), 0)";
            PreparedStatement customerStmt = conn.prepareStatement(customerSql);
            customerStmt.setInt(1, newCustomerId);
            customerStmt.setInt(2, accountId);
            customerStmt.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException ex) {
            System.err.println("Lỗi khi đăng ký khách hàng: " + ex.getMessage());
            ex.printStackTrace();
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
                    conn.close();
                }
            } catch (SQLException ex) {
                System.err.println("Lỗi khi đóng kết nối: " + ex.getMessage());
            }
        }
    }

    public int getNewCustomerId() {
        return newCustomerId;
    }
}