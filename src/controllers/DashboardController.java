package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import model.Role;
import utils.Session;

public class DashboardController {

    @FXML
    private Label lblWelcome;
    @FXML
    private Button btnAdminPanel;
    @FXML
    private Button btnEmployeePanel;
    @FXML
    private Button btnLogout;

    @FXML
    public void initialize() {
        System.out.println("Current User: " + Session.getCurrentAccount());
        if (Session.getCurrentAccount() != null) {
            Role role = Session.getCurrentAccount().getRole();
            lblWelcome.setText("🐾 Xin chào, " + Session.getCurrentAccount().getUserName());
            
            // Sử dụng Platform.runLater để đảm bảo giao diện được cập nhật sau khi đăng
            // nhập thành công
            Platform.runLater(() -> {
                if (role != null) {
                    switch (role.getRoleID()) {
                    case 1: // admin
                        btnAdminPanel.setVisible(true); // Hiển thị nút cho Admin/Manager
                        break;
                    case 2: // STAFF_CARE
                        btnEmployeePanel.setVisible(true); // Hiển thị nút cho Employee
                        break;
                    case 3: // STAFF_CASHIER
                        btnEmployeePanel.setVisible(true); // Hiển thị nút cho Employee
                        break;
                    case 4: // STAFF_RECEPTION
                        btnEmployeePanel.setVisible(true); // Hiển thị nút cho Employee
                        break;
                    case 5: 
                        lblWelcome.setText("Bạn đã nghỉ việc");
                        break;
                    default:
                        lblWelcome.setText("Vai trò không xác định, vui lòng đăng nhập lại!");
                        break;
                    }
                }
            });
        } else {
            lblWelcome.setText("Vui lòng đăng nhập!");
        }

        // Sự kiện đăng xuất
        btnLogout.setOnAction(event -> handleLogout());
    }

    // Chuyển đến trang Admin
    @FXML
    private void handleAdminPanel() {
        SceneSwitcher.switchScene("admin/admin_home.fxml");
    }

    // Chuyển đến trang Employee dựa trên vai trò
    @FXML
    private void handleEmployeePanel() {
        if (Session.getCurrentAccount() != null && Session.getCurrentAccount().getRole() != null) {
            String roleName = Session.getCurrentAccount().getRole().getRoleName().toUpperCase();
            switch (roleName) {
                case "STAFF_CARE":
                    SceneSwitcher.switchScene("staff/booking_view.fxml");
                    break;
                case "STAFF_CASHIER":
                    SceneSwitcher.switchScene("staff/invoice_view.fxml");
                    break;
                case "STAFF_RECEPTION":
                    SceneSwitcher.switchScene("staff/my_schedule.fxml");
                    break;
                default:
                    SceneSwitcher.switchScene("staff/staff_home.fxml");
                    break;
            }
        } else {
            SceneSwitcher.switchScene("staff/staff_home.fxml");
        }
    }

    // Đăng xuất
    @FXML
    private void handleLogout() {
        Session.logout();
        SceneSwitcher.switchScene("login.fxml");
    }
}