package frontend;

import controllers.SceneSwitcher;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        SceneSwitcher.setMainStage(primaryStage);
        SceneSwitcher.switchScene("home.fxml"); 
    }

   
    public static void main(String[] args) {
        launch(args);
    }
}
