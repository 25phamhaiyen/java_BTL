package controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneSwitcher {
    private static Stage mainStage;

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    public static void switchScene(String fxmlFile) {
        try {
        	FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/view/" + fxmlFile));
        	Parent root = loader.load();
        	mainStage.setTitle("BESTPETS");
        	mainStage.getIcons().add(new Image(SceneSwitcher.class.getResourceAsStream("/images/logo.jpg")));            
        	mainStage.setResizable(true);
            mainStage.setScene(new Scene(root));
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
