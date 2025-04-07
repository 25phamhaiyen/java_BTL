package controllers.customer;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class BookAppointmentController {

	@FXML
	private Label lblBookingInfo;

	@FXML
	public void initialize() {
		lblBookingInfo.setText("Chức năng đặt lịch hẹn sẽ ở đây.");
	}
}
