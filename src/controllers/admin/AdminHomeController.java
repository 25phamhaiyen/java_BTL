package controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class AdminHomeController {

    @FXML
    private Pane centerContent;

    @FXML
    private Button btnAccountManagement, btnStaffManagement, btnCreateWorkSchedule, btnServices, btnFinance, btnDetailedDashboard;

    @FXML
    public void initialize() {
        // Gắn sự kiện cho các nút sidebar
        btnAccountManagement.setOnAction(e -> switchContent("/view/admin/manage_account.fxml"));
        btnStaffManagement.setOnAction(e -> switchContent("/view/admin/manage_staff.fxml"));
        btnCreateWorkSchedule.setOnAction(e -> switchContent("/view/admin/manage_schedule.fxml"));
        btnServices.setOnAction(e -> switchContent("/view/admin/manage_service.fxml"));
        btnFinance.setOnAction(e -> switchContent("/view/admin/manage_finance.fxml"));
        btnDetailedDashboard.setOnAction(e -> switchContent("/view/admin/general_statistics.fxml"));

        // Hiển thị nội dung mặc định
        showWelcomeMessage();
    }

    private void showWelcomeMessage() {
        // Hiển thị lời chào mặc định
        VBox welcomeContent = new VBox(20);
        welcomeContent.getChildren().add(new javafx.scene.control.Label("Chào Admin!"));
        centerContent.getChildren().setAll(welcomeContent);
    }

    private void switchContent(String fxmlPath) {
        try {
            // Tải nội dung mới từ file FXML
            Pane newContent = FXMLLoader.load(getClass().getResource(fxmlPath));
            centerContent.getChildren().setAll(newContent); // Thay thế nội dung trong phần center
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}