package controllers.customer;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import model.Account;
import utils.Session;

public class ViewProfileController {

	@FXML
	private Label lblProfileInfo;

	@FXML
	public void initialize() {
		Account currentUser = Session.getCurrentUser();
		if (currentUser != null) {
			lblProfileInfo.setText("Thông tin cá nhân: " + currentUser.toString());
		} else {
			lblProfileInfo.setText("Bạn cần đăng nhập để xem thông tin.");
		}
	}
}
