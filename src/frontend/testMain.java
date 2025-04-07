package frontend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class testMain extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        try {
            primaryStage = stage;
            
            // Load file FXML đăng nhập (đường dẫn phải đúng với cấu trúc project)
            Parent root = FXMLLoader.load(getClass().getResource("/controllers/login.fxml"));
            
            // Thiết lập scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Đăng nhập hệ thống");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi tải file FXML: " + e.getMessage());
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}