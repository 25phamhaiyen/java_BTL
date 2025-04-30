package controllers.admin;

import java.io.IOException;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class AdminHomeController {
	
	@FXML
    private Pane centerContent; 

    @FXML
    private VBox sidebar;

    @FXML
    private HBox btnAccountManagement, btnStaffManagement, btnCreateWorkSchedule, btnServices, btnDetailedDashboard;

    private boolean isCollapsed = false;

    @FXML
    public void initialize() {
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
            setupButtonAction(btnCreateWorkSchedule, "/view/admin/manage_schedule.fxml");
            setupButtonAction(btnServices, "/view/admin/manage_service.fxml");
            setupButtonAction(btnDetailedDashboard, "/view/admin/general_statistics.fxml");
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
                // Đọc FXML và thay thế nội dung centerContent
                Node content = FXMLLoader.load(getClass().getResource(fxmlPath));
                centerContent.getChildren().setAll(content);  // Cập nhật nội dung của centerContent
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }
}
