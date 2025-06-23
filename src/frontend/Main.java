package frontend;

import controllers.SceneSwitcher;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneSwitcher.setMainStage(primaryStage);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
        
        // Sử dụng hàm switchScene với tham số true để chỉ định đây là home (không full màn hình)
        SceneSwitcher.switchScene("home.fxml", true);
    }

    public static void main(String[] args) {
        launch(args);    
    }
}