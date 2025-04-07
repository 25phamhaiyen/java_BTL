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
	private Button btnCustomerPanel;
	@FXML
	private Button btnLogout;

	@FXML
	public void initialize() {
		System.out.println("Current User: " + Session.getCurrentUser());
		if (Session.getCurrentUser() != null) {
			Role role = Session.getUserRole();
			lblWelcome.setText("🐾 Xin chào, " + Session.getCurrentUser().getUserName());
			// Sử dụng Platform.runLater để đảm bảo giao diện được cập nhật sau khi đăng
			// nhập thành công
			Platform.runLater(() -> {
				if (role != null) {
					switch (role.getRoleID()) {
					case 1: // Admin
					case 2: // Manager
						btnAdminPanel.setVisible(true); // Hiển thị nút cho Admin/Manager
						break;
					case 3: // Employee
						btnEmployeePanel.setVisible(true); // Hiển thị nút cho Employee
						break;
					case 4: // Customer
						btnCustomerPanel.setVisible(true); // Hiển thị nút cho Customer
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
		SceneSwitcher.switchScene("admin/adminDashboard.fxml");
	}

	// Chuyển đến trang Employee
	@FXML
	private void handleEmployeePanel() {
		SceneSwitcher.switchScene("employee/employeeDashboard.fxml");
	}

	// Chuyển đến trang Customer
	@FXML
	private void handleCustomerPanel() {
		SceneSwitcher.switchScene("customer/customerDashboard.fxml");
	}

	// Đăng xuất
	@FXML
	private void handleLogout() {
		Session.logout();
		SceneSwitcher.switchScene("login.fxml");
	}

}
