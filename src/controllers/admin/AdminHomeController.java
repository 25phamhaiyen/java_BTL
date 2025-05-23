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

public class AdminHomeController {

	@FXML
	private Pane centerContent;

	@FXML
	private VBox sidebar;

	@FXML
	private HBox btnAccountManagement, btnStaffManagement, btnCreateWorkSchedule, btnServices, btnDetailedDashboard,
			btnEditProfile, btnCustomerManagement, btnLogout;

	private boolean isCollapsed = false;

	@FXML
	public void initialize() throws IOException {
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
				alert.setTitle("Xác nhận đăng xuất");
				alert.setHeaderText(null);
				alert.setContentText("Bạn có chắc chắn muốn đăng xuất?");

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
