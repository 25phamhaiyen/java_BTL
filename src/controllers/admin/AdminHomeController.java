package controllers.admin;

import java.io.IOException;

import controllers.SceneSwitcher;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import utils.LanguageManager;

public class AdminHomeController {

	@FXML
	private Pane centerContent;

	@FXML
	private VBox sidebar;

	@FXML private Label lblLogo;
	@FXML private Label lblAccountManagement;
	@FXML private Label lblStaffManagement;
	@FXML private Label lblCustomerManagement;
	@FXML private Label lblWorkSchedule;
	@FXML private Label lblServices;
	@FXML private Label lblDashboard;
	@FXML private Label lblProfile;
	@FXML private Label lblLogout;
	@FXML private Label lblWelcome;
	@FXML
	private HBox btnAccountManagement, btnStaffManagement, btnCreateWorkSchedule, btnServices, btnDetailedDashboard,
			btnEditProfile, btnCustomerManagement, btnLogout;

	private boolean isCollapsed = false;

	@FXML
	public void initialize() throws IOException {
		lblLogo.setText(LanguageManager.getString("logo.text"));
	    lblAccountManagement.setText(LanguageManager.getString("admin.account.management"));
	    lblStaffManagement.setText(LanguageManager.getString("admin.staff.management"));
	    lblCustomerManagement.setText(LanguageManager.getString("admin.customer.management"));
	    lblWorkSchedule.setText(LanguageManager.getString("admin.schedule.create"));
	    lblServices.setText(LanguageManager.getString("admin.services"));
	    lblDashboard.setText(LanguageManager.getString("admin.dashboard"));
	    lblProfile.setText(LanguageManager.getString("admin.profile"));
	    lblLogout.setText(LanguageManager.getString("admin.logout"));
	    lblWelcome.setText(LanguageManager.getString("admin.welcome"));
		    
		Platform.runLater(() -> {
			// Sự kiện di chuột vào sidebar để mở rộng
			sidebar.setOnMouseEntered(e -> {
				if (isCollapsed) {
					expandSidebar();
				}
			});

			// Sự kiện di chuột ra khỏi sidebar để thu nhỏ
			sidebar.setOnMouseExited(e -> {
				if (!isCollapsed) {
					collapseSidebar();
				}
			});

			// Thiết lập hành động cho các nút bấm
			setupButtonAction(btnAccountManagement, "/view/admin/manage_account.fxml");
			setupButtonAction(btnStaffManagement, "/view/admin/manage_staff.fxml");
			setupButtonAction(btnCustomerManagement, "/view/admin/manage_customer.fxml");
			setupButtonAction(btnCreateWorkSchedule, "/view/admin/manage_schedule.fxml");
			setupButtonAction(btnServices, "/view/admin/manage_service.fxml");
			setupButtonAction(btnDetailedDashboard, "/view/admin/general_statistics.fxml");
			setupButtonAction(btnEditProfile, "/view/staff/edit_profile.fxml");
			btnLogout.setOnMouseClicked(e -> {
				Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
				alert.setTitle(LanguageManager.getString("logout.confirm.title"));
				alert.setHeaderText(null);
				alert.setContentText(LanguageManager.getString("logout.confirm.message"));

				alert.showAndWait().ifPresent(response -> {
					if (response == ButtonType.OK) {
						SceneSwitcher.switchScene("login.fxml");
					}
				});
			});
		});
	}

	private void collapseSidebar() {
		sidebar.setPrefWidth(60);
		for (Node node : sidebar.getChildren()) {
			if (node instanceof HBox hbox) {
				for (Node subNode : hbox.getChildren()) {
					if (subNode instanceof Label label) {
						label.setVisible(false);
						label.setManaged(false);
					}
				}
			}
		}
		isCollapsed = true;
	}

	private void expandSidebar() {
		sidebar.setPrefWidth(200);
		for (Node node : sidebar.getChildren()) {
			if (node instanceof HBox hbox) {
				for (Node subNode : hbox.getChildren()) {
					if (subNode instanceof Label label) {
						label.setVisible(true);
						label.setManaged(true);
					}
				}
			}
		}
		isCollapsed = false;
	}

	private void setupButtonAction(HBox button, String fxmlPath) {
		button.setOnMouseClicked(e -> {
			try {
				Node content = FXMLLoader.load(getClass().getResource(fxmlPath));
				centerContent.getChildren().setAll(content);
				highlightSelectedButton(button);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		});
	}

	private void highlightSelectedButton(HBox selectedButton) {
		for (Node node : sidebar.getChildren()) {
			if (node instanceof HBox hbox) {
				hbox.getStyleClass().remove("sidebar-active");
			}
		}
		selectedButton.getStyleClass().add("sidebar-active");
	}

}
