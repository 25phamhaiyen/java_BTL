package frontend;

import javafx.application.Application;
import javafx.stage.Stage;
import utils.SceneSwitcher;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        SceneSwitcher.setMainStage(primaryStage);
        SceneSwitcher.switchScene("login.fxml", "Đăng nhập"); // Mặc định vào màn hình đăng nhập
    }

    public static void main(String[] args) {
        launch(args);
    }
}
