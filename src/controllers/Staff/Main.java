package controllers.Staff;

import controllers.Staff.LoginScreen;
import javafx.application.Application;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Pet Shop Management");
        
        LoginScreen loginScreen = new LoginScreen(primaryStage);
        Scene scene = new Scene(loginScreen.getRoot(), 800, 600);
        scene.getStylesheets().add(getClass().getResource("/view/Staff/Staff.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}