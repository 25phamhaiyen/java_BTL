package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
			lblWelcome.setText("🐾 Xin chào, " + Session.getCurrentAccount().getUserName());
		} else {
			lblWelcome.setText("Vui lòng đăng nhập!");
		}

		btnAdminPanel.setVisible(true);

		btnLogout.setOnAction(event -> handleLogout());
	}

	// Chuyển đến trang Admin
	@FXML
	private void handleAdminPanel() {
		SceneSwitcher.switchScene("staff/staff_home.fxml");
	}

	// Đăng xuất
	@FXML
	private void handleLogout() {
		Session.logout();
		SceneSwitcher.switchScene("login.fxml");
	}

}
