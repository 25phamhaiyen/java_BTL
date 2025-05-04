
package frontend;

import controllers.SceneSwitcher;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        SceneSwitcher.setMainStage(primaryStage);
        
        // Instead of loading the home.fxml, load the booking view
        SceneSwitcher.switchScene("home.fxml");
        //SceneSwitcher.switchScene("staff/invoice_view.fxml");
       // SceneSwitcher.switchScene("staff/booking_view.fxml");
  
     
    }

    public static void main(String[] args) {
        launch(args);
    }
}