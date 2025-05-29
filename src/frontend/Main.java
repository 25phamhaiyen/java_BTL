
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
		SceneSwitcher.switchScene("home.fxml");
	}

	public static void main(String[] args) {
		launch(args);	
	}
}