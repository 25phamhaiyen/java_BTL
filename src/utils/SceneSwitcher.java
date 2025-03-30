package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneSwitcher {
    private static Stage mainStage;

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    public static void switchScene(String fxmlFile, String title) {
        try {
        	FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/views/" + fxmlFile));

//        	FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("views/" + fxmlFile));
            Parent root = loader.load();
            mainStage.setTitle(title);
            mainStage.setScene(new Scene(root));
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
