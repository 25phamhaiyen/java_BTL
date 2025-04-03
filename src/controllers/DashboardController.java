package controllers;

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
    private Button btnCustomerView;
    @FXML
    private Button btnEmployeeView;
    @FXML
    private Button btnLogout;

    @FXML
    public void initialize() {
    	// Trước khi truy cập Session.getCurrentUser()
    	System.out.println("Current User: " + Session.getCurrentUser());

    	// Kiểm tra nếu currentUser là null
        if (Session.getCurrentUser() != null) {
            // Lấy vai trò người dùng từ Session
            Role role = Session.getUserRole();
            
            lblWelcome.setText("Chào mừng, " + Session.getCurrentUser().getUserName());

            // Ẩn tất cả các nút trước, sau đó hiển thị theo quyền
            btnAdminPanel.setVisible(false);
            btnCustomerView.setVisible(false);
            btnEmployeeView.setVisible(false);

            
            // Kiểm tra vai trò và hiển thị các nút phù hợp
            if (role != null) {
                switch (role.getRoleID()) {
                    case 1: // ADMIN
                    case 4: // Manager
                        btnAdminPanel.setVisible(true); // Hiển thị quản lý hệ thống
                        break;
                    case 3: // CUSTOMER
                        btnCustomerView.setVisible(true); // Hiển thị trang khách hàng
                        break;
                    case 2: // EMPLOYEE
                        btnEmployeeView.setVisible(true); // Hiển thị trang nhân viên
                        break;
                    default:
                    	System.out.println(role.getRoleName());
                        lblWelcome.setText("Vui lòng đăng nhập!");
                        break;
                }
            }
        } else {
            lblWelcome.setText("Vui lòng đăng nhập!");
        }

        // Xử lý sự kiện đăng xuất
        btnLogout.setOnAction(event -> handleLogout());
    }

    private void handleLogout() {
        Session.logout(); // Đăng xuất người dùng
        SceneSwitcher.switchScene("login.fxml");
    }
}
