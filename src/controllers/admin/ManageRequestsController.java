package controllers.admin;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import model.LeaveRequest;
import model.ShiftChangeRequest;
import enums.RequestStatus;
import enums.Shift;
import service.RequestService;
import utils.RoleChecker;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ManageRequestsController implements Initializable {

	@FXML
	private TabPane requestTabPane;
	@FXML
	private Tab leaveRequestTab;
	@FXML
	private Tab shiftChangeTab;

	// Leave Request Tab
	@FXML
	private TableView<LeaveRequest> leaveRequestTable;
	@FXML
	private TableColumn<LeaveRequest, Integer> leaveRequestIdColumn;
	@FXML
	private TableColumn<LeaveRequest, String> leaveStaffNameColumn;
	@FXML
	private TableColumn<LeaveRequest, String> leaveDateColumn;
	@FXML
	private TableColumn<LeaveRequest, String> leaveReasonColumn;
	@FXML
	private TableColumn<LeaveRequest, String> leaveStatusColumn;
	@FXML
	private TableColumn<LeaveRequest, String> leaveRequestDateColumn;
	@FXML
	private TableColumn<LeaveRequest, String> leaveApprovedByColumn;
	@FXML
	private Button approveLeaveButton;
	@FXML
	private Button rejectLeaveButton;
	@FXML
	private TextArea leaveResponseNoteArea;

	// Shift Change Request Tab
	@FXML
	private TableView<ShiftChangeRequest> shiftChangeTable;
	@FXML
	private TableColumn<ShiftChangeRequest, Integer> shiftChangeIdColumn;
	@FXML
	private TableColumn<ShiftChangeRequest, String> shiftStaffNameColumn;
	@FXML
	private TableColumn<ShiftChangeRequest, String> currentDateColumn;
	@FXML
	private TableColumn<ShiftChangeRequest, String> currentShiftColumn;
	@FXML
	private TableColumn<ShiftChangeRequest, String> desiredDateColumn;
	@FXML
	private TableColumn<ShiftChangeRequest, String> desiredShiftColumn;
	@FXML
	private TableColumn<ShiftChangeRequest, String> shiftReasonColumn;
	@FXML
	private TableColumn<ShiftChangeRequest, String> shiftStatusColumn;
	@FXML
	private TableColumn<ShiftChangeRequest, String> shiftRequestDateColumn;
	@FXML
	private TableColumn<ShiftChangeRequest, String> shiftApprovedByColumn;
	@FXML
	private Button approveShiftButton;
	@FXML
	private Button rejectShiftButton;
	@FXML
	private TextArea shiftResponseNoteArea;

	// Common Controls
	@FXML
	private ComboBox<RequestStatus> leaveStatusFilter;
	@FXML
	private ComboBox<RequestStatus> shiftStatusFilter;
	@FXML
	private Button refreshButton;
	@FXML
	private Label statusLabel;

	private RequestService requestService;
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		if (!RoleChecker.hasPermission("APPROVE_LEAVE") || !RoleChecker.hasPermission("APPROVE_SHIFT_CHANGE")) {
			showAlert(AlertType.ERROR, "Lỗi", "Không có quyền", "Bạn không có quyền truy cập vào chức năng này.");
			return;
		}

		requestService = new RequestService();

		setupLeaveRequestTable();
		setupShiftChangeTable();
		setupStatusFilters();

		loadAllRequests();

		// Setup button states
		setupButtonStates();

		// Add listeners for table selection
		setupTableListeners();

		statusLabel.setText("Trạng thái: Đã tải danh sách yêu cầu");
	}

	private void setupLeaveRequestTable() {
		leaveRequestIdColumn.setCellValueFactory(new PropertyValueFactory<>("leaveRequestId"));

		leaveStaffNameColumn.setCellValueFactory(cellData -> {
			String name = cellData.getValue().getStaff() != null ? cellData.getValue().getStaff().getFullName() : "N/A";
			return new SimpleStringProperty(name);
		});

		leaveDateColumn.setCellValueFactory(cellData -> {
			LocalDate date = cellData.getValue().getLeaveDate();
			return new SimpleStringProperty(date != null ? dateFormatter.format(date) : "N/A");
		});

		leaveReasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));

		leaveStatusColumn.setCellValueFactory(cellData -> {
			String status = cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().getDescription()
					: "N/A";
			return new SimpleStringProperty(status);
		});

		leaveRequestDateColumn.setCellValueFactory(cellData -> {
			String date = cellData.getValue().getRequestDate() != null
					? dateTimeFormatter.format(cellData.getValue().getRequestDate().toLocalDateTime())
					: "N/A";
			return new SimpleStringProperty(date);
		});

		leaveApprovedByColumn.setCellValueFactory(cellData -> {
			String name = cellData.getValue().getApprovedBy() != null
					? cellData.getValue().getApprovedBy().getFullName()
					: "N/A";
			return new SimpleStringProperty(name);
		});
	}

	private void setupShiftChangeTable() {
		shiftChangeIdColumn.setCellValueFactory(new PropertyValueFactory<>("shiftChangeId"));

		shiftStaffNameColumn.setCellValueFactory(cellData -> {
			String name = cellData.getValue().getStaff() != null ? cellData.getValue().getStaff().getFullName() : "N/A";
			return new SimpleStringProperty(name);
		});

		currentDateColumn.setCellValueFactory(cellData -> {
			LocalDate date = cellData.getValue().getCurrentDate();
			return new SimpleStringProperty(date != null ? dateFormatter.format(date) : "N/A");
		});

		currentShiftColumn.setCellValueFactory(cellData -> {
			String shift = cellData.getValue().getCurrentShift() != null ? cellData.getValue().getCurrentShift().name()
					: "N/A";
			return new SimpleStringProperty(shift);
		});

		desiredDateColumn.setCellValueFactory(cellData -> {
			LocalDate date = cellData.getValue().getDesiredDate();
			return new SimpleStringProperty(date != null ? dateFormatter.format(date) : "N/A");
		});

		desiredShiftColumn.setCellValueFactory(cellData -> {
			String shift = cellData.getValue().getDesiredShift() != null ? cellData.getValue().getDesiredShift().name()
					: "N/A";
			return new SimpleStringProperty(shift);
		});

		shiftReasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));

		shiftStatusColumn.setCellValueFactory(cellData -> {
			String status = cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().getDescription()
					: "N/A";
			return new SimpleStringProperty(status);
		});

		shiftRequestDateColumn.setCellValueFactory(cellData -> {
			String date = cellData.getValue().getRequestDate() != null
					? dateTimeFormatter.format(cellData.getValue().getRequestDate().toLocalDateTime())
					: "N/A";
			return new SimpleStringProperty(date);
		});

		shiftApprovedByColumn.setCellValueFactory(cellData -> {
			String name = cellData.getValue().getApprovedBy() != null
					? cellData.getValue().getApprovedBy().getFullName()
					: "N/A";
			return new SimpleStringProperty(name);
		});
	}

	private void setupStatusFilters() {
		leaveStatusFilter.setItems(FXCollections.observableArrayList(RequestStatus.values()));
		shiftStatusFilter.setItems(FXCollections.observableArrayList(RequestStatus.values()));

		leaveStatusFilter.setConverter(new StringConverter<RequestStatus>() {
			@Override
			public String toString(RequestStatus status) {
				return status != null ? status.getDescription() : "";
			}

			@Override
			public RequestStatus fromString(String string) {
				return null;
			}
		});

		shiftStatusFilter.setConverter(new StringConverter<RequestStatus>() {
			@Override
			public String toString(RequestStatus status) {
				return status != null ? status.getDescription() : "";
			}

			@Override
			public RequestStatus fromString(String string) {
				return null;
			}
		});

		// Add listeners for status filters
		leaveStatusFilter.setOnAction(event -> filterLeaveRequests());
		shiftStatusFilter.setOnAction(event -> filterShiftChangeRequests());
	}

	private void setupButtonStates() {
		approveLeaveButton.setDisable(true);
		rejectLeaveButton.setDisable(true);
		approveShiftButton.setDisable(true);
		rejectShiftButton.setDisable(true);
	}

	private void setupTableListeners() {
		leaveRequestTable.getSelectionModel().selectedItemProperty()
				.addListener((obs, oldVal, newVal) -> updateLeaveRequestButtons(newVal));

		shiftChangeTable.getSelectionModel().selectedItemProperty()
				.addListener((obs, oldVal, newVal) -> updateShiftChangeButtons(newVal));
	}

	private void updateLeaveRequestButtons(LeaveRequest request) {
		boolean isSelected = request != null;
		boolean isPending = isSelected && request.getStatus() == RequestStatus.PENDING;

		approveLeaveButton.setDisable(!isPending);
		rejectLeaveButton.setDisable(!isPending);

		if (request != null) {
			leaveResponseNoteArea.setText(request.getNote() != null ? request.getNote() : "");
		} else {
			leaveResponseNoteArea.clear();
		}
	}

	private void updateShiftChangeButtons(ShiftChangeRequest request) {
		boolean isSelected = request != null;
		boolean isPending = isSelected && request.getStatus() == RequestStatus.PENDING;

		approveShiftButton.setDisable(!isPending);
		rejectShiftButton.setDisable(!isPending);

		if (request != null) {
			shiftResponseNoteArea.setText(request.getNote() != null ? request.getNote() : "");
		} else {
			shiftResponseNoteArea.clear();
		}
	}

	@FXML
	private void loadAllRequests() {
		loadLeaveRequests();
		loadShiftChangeRequests();
		statusLabel.setText("Trạng thái: Đã tải danh sách yêu cầu");
	}

	private void loadLeaveRequests() {
		ObservableList<LeaveRequest> requests = FXCollections.observableArrayList(requestService.getAllLeaveRequests());
		leaveRequestTable.setItems(requests);
		filterLeaveRequests();
	}

	private void loadShiftChangeRequests() {
		ObservableList<ShiftChangeRequest> requests = FXCollections
				.observableArrayList(requestService.getAllShiftChangeRequests());
		shiftChangeTable.setItems(requests);
		filterShiftChangeRequests();
	}

	private void filterLeaveRequests() {
		ObservableList<LeaveRequest> allRequests = leaveRequestTable.getItems();
		if (allRequests == null) {
			allRequests = FXCollections.observableArrayList(requestService.getAllLeaveRequests());
			leaveRequestTable.setItems(allRequests);
		}

		RequestStatus selectedStatus = leaveStatusFilter.getValue();
		if (selectedStatus == null) {
			// Show all requests
			leaveRequestTable.setItems(allRequests);
		} else {
			// Filter by status
			ObservableList<LeaveRequest> filteredRequests = allRequests
					.filtered(request -> request.getStatus() == selectedStatus);
			leaveRequestTable.setItems(filteredRequests);
		}
	}

	private void filterShiftChangeRequests() {
		ObservableList<ShiftChangeRequest> allRequests = shiftChangeTable.getItems();
		if (allRequests == null) {
			allRequests = FXCollections.observableArrayList(requestService.getAllShiftChangeRequests());
			shiftChangeTable.setItems(allRequests);
		}

		RequestStatus selectedStatus = shiftStatusFilter.getValue();
		if (selectedStatus == null) {
			// Show all requests
			shiftChangeTable.setItems(allRequests);
		} else {
			// Filter by status
			ObservableList<ShiftChangeRequest> filteredRequests = allRequests
					.filtered(request -> request.getStatus() == selectedStatus);
			shiftChangeTable.setItems(filteredRequests);
		}
	}

	@FXML
	private void handleApproveLeave() {
		LeaveRequest selected = leaveRequestTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(AlertType.WARNING, "Cảnh báo", "Chưa chọn yêu cầu",
					"Vui lòng chọn một yêu cầu nghỉ phép để phê duyệt.");
			return;
		}

		String note = leaveResponseNoteArea.getText().trim();
		boolean success = requestService.approveLeaveRequest(selected.getLeaveRequestId(), note);

		if (success) {
			showAlert(AlertType.INFORMATION, "Thành công", "Phê duyệt thành công",
					"Đã phê duyệt yêu cầu nghỉ phép của " + selected.getStaff().getFullName());
			loadAllRequests();
		} else {
			showAlert(AlertType.ERROR, "Lỗi", "Phê duyệt thất bại", "Không thể phê duyệt yêu cầu. Vui lòng thử lại.");
		}
	}

	@FXML
	private void handleRejectLeave() {
		LeaveRequest selected = leaveRequestTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(AlertType.WARNING, "Cảnh báo", "Chưa chọn yêu cầu",
					"Vui lòng chọn một yêu cầu nghỉ phép để từ chối.");
			return;
		}

		String note = leaveResponseNoteArea.getText().trim();
		if (note.isEmpty()) {
			showAlert(AlertType.WARNING, "Cảnh báo", "Thiếu lý do", "Vui lòng nhập lý do từ chối.");
			return;
		}

		boolean success = requestService.rejectLeaveRequest(selected.getLeaveRequestId(), note);

		if (success) {
			showAlert(AlertType.INFORMATION, "Thành công", "Từ chối thành công",
					"Đã từ chối yêu cầu nghỉ phép của " + selected.getStaff().getFullName());
			loadAllRequests();
		} else {
			showAlert(AlertType.ERROR, "Lỗi", "Từ chối thất bại", "Không thể từ chối yêu cầu. Vui lòng thử lại.");
		}
	}

	@FXML
	private void handleApproveShiftChange() {
		ShiftChangeRequest selected = shiftChangeTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(AlertType.WARNING, "Cảnh báo", "Chưa chọn yêu cầu",
					"Vui lòng chọn một yêu cầu đổi ca để phê duyệt.");
			return;
		}

		String note = shiftResponseNoteArea.getText().trim();
		boolean success = requestService.approveShiftChangeRequest(selected.getShiftChangeId(), note);

		if (success) {
			showAlert(AlertType.INFORMATION, "Thành công", "Phê duyệt thành công",
					"Đã phê duyệt yêu cầu đổi ca của " + selected.getStaff().getFullName());
			loadAllRequests();
		} else {
			showAlert(AlertType.ERROR, "Lỗi", "Phê duyệt thất bại", "Không thể phê duyệt yêu cầu. Vui lòng thử lại.");
		}
	}

	@FXML
	private void handleRejectShiftChange() {
		ShiftChangeRequest selected = shiftChangeTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(AlertType.WARNING, "Cảnh báo", "Chưa chọn yêu cầu",
					"Vui lòng chọn một yêu cầu đổi ca để từ chối.");
			return;
		}

		String note = shiftResponseNoteArea.getText().trim();
		if (note.isEmpty()) {
			showAlert(AlertType.WARNING, "Cảnh báo", "Thiếu lý do", "Vui lòng nhập lý do từ chối.");
			return;
		}

		boolean success = requestService.rejectShiftChangeRequest(selected.getShiftChangeId(), note);

		if (success) {
			showAlert(AlertType.INFORMATION, "Thành công", "Từ chối thành công",
					"Đã từ chối yêu cầu đổi ca của " + selected.getStaff().getFullName());
			loadAllRequests();
		} else {
			showAlert(AlertType.ERROR, "Lỗi", "Từ chối thất bại", "Không thể từ chối yêu cầu. Vui lòng thử lại.");
		}
	}

	private void showAlert(AlertType alertType, String title, String header, String content) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
}