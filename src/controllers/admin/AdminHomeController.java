package controllers.admin;

import java.io.IOException;
import java.util.Locale;

import controllers.SceneSwitcher;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import utils.LanguageChangeListener;
import utils.LanguageManagerAd;
import utils.LanguageManagerStaff;

public class AdminHomeController implements LanguageChangeListener{

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
	@FXML private ComboBox<String> languageCombo;

	private boolean isCollapsed = false;

	@FXML
	public void initialize() throws IOException {
		LanguageManagerAd.addListener(this); // Đăng ký lắng nghe sự kiện đổi ngôn ngữ
				
		languageCombo.getItems().addAll("Tiếng Việt", "English");
        languageCombo.setValue("Tiếng Việt");
        languageCombo.setOnAction(e -> {
            String lang = languageCombo.getValue();
            if (lang.equals("English")) {
                LanguageManagerAd.setLocale(new Locale("en", "US"));
                LanguageManagerStaff.setLocale(new Locale("en", "US"));
            } else {
                LanguageManagerAd.setLocale(new Locale("vi", "VN"));
                LanguageManagerStaff.setLocale(new Locale("vi", "VN"));
            }
        });
        
        loadTexts();
		    
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
				alert.setTitle(LanguageManagerAd.getString("logout.confirm.title"));
				alert.setHeaderText(null);
				alert.setContentText(LanguageManagerAd.getString("logout.confirm.message"));

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

	@Override
	public void onLanguageChanged() {
		loadTexts();
		
	}
	
	private void loadTexts() {
		lblLogo.setText(LanguageManagerAd.getString("logo.text"));
	    lblAccountManagement.setText(LanguageManagerAd.getString("admin.account.management"));
	    lblStaffManagement.setText(LanguageManagerAd.getString("admin.staff.management"));
	    lblCustomerManagement.setText(LanguageManagerAd.getString("admin.customer.management"));
	    lblWorkSchedule.setText(LanguageManagerAd.getString("admin.schedule.create"));
	    lblServices.setText(LanguageManagerAd.getString("admin.services"));
	    lblDashboard.setText(LanguageManagerAd.getString("admin.dashboard"));
	    lblProfile.setText(LanguageManagerAd.getString("admin.profile"));
	    lblLogout.setText(LanguageManagerAd.getString("admin.logout"));
	    lblWelcome.setText(LanguageManagerAd.getString("admin.welcome"));
	}

}
