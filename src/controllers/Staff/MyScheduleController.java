package controllers.Staff;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;

import enums.RequestType;
import enums.Shift;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.ShiftRequest;
import model.Staff;
import model.WorkSchedule;
import service.ScheduleService;
import utils.RoleChecker;
import utils.Session;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Insets;

public class MyScheduleController implements Initializable {

	@FXML
	private Label dateLabel;
	@FXML
	private Label staffNameLabel;
	@FXML
	private Label positionLabel;
	@FXML
	private DatePicker datePicker;
	@FXML
	private ComboBox<String> viewModeSelector;
	@FXML
	private ComboBox<String> shiftFilter;
	@FXML
	private TableView<WorkSchedule> scheduleTable;
	@FXML
	private TableColumn<WorkSchedule, Integer> idColumn;
	@FXML
	private TableColumn<WorkSchedule, LocalDate> dateColumn;
	@FXML
	private TableColumn<WorkSchedule, String> shiftColumn;
	@FXML
	private TableColumn<WorkSchedule, LocalTime> startTimeColumn;
	@FXML
	private TableColumn<WorkSchedule, LocalTime> endTimeColumn;
	@FXML
	private TableColumn<WorkSchedule, String> locationColumn;
	@FXML
	private TableColumn<WorkSchedule, String> taskColumn;
	@FXML
	private TableColumn<WorkSchedule, String> noteColumn;
	@FXML
	private TextArea additionalInfoArea;
	@FXML
	private Label totalShiftsLabel;
	@FXML
	private Label morningShiftsLabel;
	@FXML
	private Label afternoonShiftsLabel;
	@FXML
	private Label eveningShiftsLabel;
	@FXML
	private DatePicker registrationDatePicker;
	@FXML
	private ComboBox<Shift> shiftSelector;
	@FXML
	private ComboBox<RequestType> typeSelector;
	@FXML
	private TextArea registrationNotes;
	@FXML
	private Label statusLabel;
	@FXML
	private VBox dayView;
	@FXML
	private VBox weekView;
	@FXML
	private VBox monMorning, tueMorning, wedMorning, thuMorning, friMorning, satMorning, sunMorning;
	@FXML
	private VBox monAfternoon, tueAfternoon, wedAfternoon, thuAfternoon, friAfternoon, satAfternoon, sunAfternoon;
	@FXML
	private VBox monEvening, tueEvening, wedEvening, thuEvening, friEvening, satEvening, sunEvening;
	@FXML
	private Button requestLeaveButton;
	@FXML
	private Button requestShiftChangeButton;
	@FXML
	private Button registerShiftButton;
	@FXML
	private Button selectScheduleButton;
	@FXML
	private Button homeButton;
	@FXML
	private Tab myRequestsTab;
	@FXML
	private TableView<ShiftRequest> tblRequests;
	@FXML
	private TableColumn<ShiftRequest, String> colDate, colShift, colType, colStatus, colReason;
	

	private ScheduleService scheduleService;
	private ObservableList<WorkSchedule> scheduleList;
	private int currentStaffId;
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	    scheduleService = new ScheduleService();

	    // Get current staff information from Session
	    Staff currentStaff = Session.getInstance().getCurrentStaff();

	    if (currentStaff == null || !RoleChecker.hasPermission("VIEW_SCHEDULE")) {
	        showAlert(AlertType.ERROR, "Lỗi", "Không có quyền truy cập",
	                "Bạn không có quyền truy cập vào màn hình lịch làm việc.");
	        Stage stage = (Stage) dateLabel.getScene().getWindow();
	        stage.close();
	        return;
	    }

	    currentStaffId = currentStaff.getId();
	    dateLabel.setText("Lịch Làm Việc Của Bạn");
	    staffNameLabel.setText("Nhân viên: " + currentStaff.getFullName());
	    positionLabel
	            .setText("Vị trí: " + (currentStaff.getPosition() != null ? currentStaff.getPosition() : "Nhân viên"));

	    // Initialize table columns
	    setupTableColumns();

	    // Initialize ComboBoxes
	    initializeComboBoxes();

	    // Set default values for date pickers
	    initializeDatePickers();

	    // Load schedule for current date
	    loadScheduleByDate(LocalDate.now()); // Sửa: sử dụng ngày hiện tại

	    // Tải khi mở tab
	    myRequestsTab.setOnSelectionChanged(e -> {
	        if (myRequestsTab.isSelected()) {
	            loadMyRequests();
	        }
	    });
	    // Add selection listener for schedule table
	    scheduleTable.getSelectionModel().selectedItemProperty()
	            .addListener((observable, oldValue, newValue) -> showScheduleDetails(newValue));

	    // Add listener for viewModeSelector
	    viewModeSelector.setOnAction(event -> handleViewModeChange());
	    
	 // Chỉ cho phép LEAVE (xin nghỉ)
	    typeSelector.setItems(FXCollections.observableArrayList(RequestType.WORK));
	    typeSelector.setValue(RequestType.WORK);
	    typeSelector.setDisable(true); // khóa không cho chọn

	    // Hiển thị tiếng Việt
	    typeSelector.setConverter(new StringConverter<>() {
	        @Override
	        public String toString(RequestType type) {
	            if (type == RequestType.LEAVE) return "Xin nghỉ";
	            if (type == RequestType.WORK) return "Đăng kí ca làm";
	            return "";
	        }

	        @Override
	        public RequestType fromString(String string) {
	            return switch (string) {
	                case "Xin nghỉ" -> RequestType.LEAVE;
	                case "Đăng kí ca làm" -> RequestType.WORK;
	                default -> null;
	            };
	        }
	    });

	    // Thiết lập hiển thị nút dựa trên quyền
	    setupButtonVisibility();
	}
	
	private void setupButtonVisibility() {
		if (requestLeaveButton != null) {
			requestLeaveButton.setDisable(!RoleChecker.hasPermission("REQUEST_LEAVE"));
		}

		if (requestShiftChangeButton != null) {
			requestShiftChangeButton.setDisable(!RoleChecker.hasPermission("REQUEST_LEAVE"));
		}

		if (registerShiftButton != null) {
			registerShiftButton.setDisable(!RoleChecker.hasPermission("REGISTER_SHIFT"));
		}

		if (selectScheduleButton != null) {
			selectScheduleButton.setDisable(!RoleChecker.hasPermission("REGISTER_SHIFT"));
		}
	}

	private void setupTableColumns() {
		idColumn.setCellValueFactory(new PropertyValueFactory<>("scheduleID"));
		dateColumn.setCellValueFactory(new PropertyValueFactory<>("workDate"));
		shiftColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getShift() != null ? cellData.getValue().getShift().name() : ""));
		startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
		endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
		locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
		taskColumn.setCellValueFactory(new PropertyValueFactory<>("task"));
		noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));

		// Format date column
		dateColumn.setCellFactory(column -> new TableCell<WorkSchedule, LocalDate>() {
			@Override
			protected void updateItem(LocalDate item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : dateFormatter.format(item));
			}
		});

		// Format time columns
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
		startTimeColumn.setCellFactory(column -> new TableCell<WorkSchedule, LocalTime>() {
			@Override
			protected void updateItem(LocalTime item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : timeFormatter.format(item));
			}
		});
		endTimeColumn.setCellFactory(column -> new TableCell<WorkSchedule, LocalTime>() {
			@Override
			protected void updateItem(LocalTime item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : timeFormatter.format(item));
			}
		});
	}

	private void initializeComboBoxes() {
		// Shift filter ComboBox
		shiftFilter.getItems().addAll("Tất cả", Shift.MORNING.name(), Shift.AFTERNOON.name(), Shift.EVENING.name());
		shiftFilter.setValue("Tất cả");

		// Shift selector for registration
		shiftSelector.getItems().addAll(Shift.MORNING, Shift.AFTERNOON, Shift.EVENING);
		shiftSelector.setConverter(new StringConverter<Shift>() {
			@Override
			public String toString(Shift shift) {
				if (shift == null)
					return null;
				switch (shift) {
				case MORNING:
					return "Ca sáng (8:00 - 12:00)";
				case AFTERNOON:
					return "Ca chiều (13:00 - 17:00)";
				case EVENING:
					return "Ca tối (18:00 - 22:00)";
				default:
					return shift.name();
				}
			}

			@Override
			public Shift fromString(String string) {
				return null; // Not needed for ComboBox
			}
		});

		// Location selector
		typeSelector.getItems().addAll(RequestType.WORK, RequestType.LEAVE);

		// View mode selector
		viewModeSelector.getItems().addAll("Hôm nay", "Tuần", "Tháng");
		viewModeSelector.setValue("Hôm nay");
	}

	private void initializeDatePickers() {
	    // Set date picker format
	    StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
	        @Override
	        public String toString(LocalDate date) {
	            return (date != null) ? dateFormatter.format(date) : "";
	        }

	        @Override
	        public LocalDate fromString(String string) {
	            return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
	        }
	    };

	    datePicker.setConverter(converter);
	    registrationDatePicker.setConverter(converter);

	    // Set default values
	    LocalDate defaultDate = LocalDate.now(); // Sửa: sử dụng ngày hiện tại
	    datePicker.setValue(defaultDate);
	    registrationDatePicker.setValue(defaultDate);

	    // Add listener for date picker changes
	    datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
	        if (newValue != null) {
	            loadScheduleByDate(newValue);
	        }
	    });
	}
	private void handleViewModeChange() {
	    String mode = viewModeSelector.getValue();
	    LocalDate date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now(); // Sửa: sử dụng ngày hiện tại

	    switch (mode) {
	    case "Hôm nay":
	        loadScheduleByDate(date);
	        break;
	    case "Tuần":
	        loadWeekSchedule();
	        break;
	    case "Tháng":
	        loadMonthSchedule();
	        break;
	    }
	}
	private void loadScheduleByDate(LocalDate date) {
	    try {
	        List<WorkSchedule> schedules = scheduleService.getSchedulesByStaffAndDate(currentStaffId, date);
	        scheduleList = FXCollections.observableArrayList(schedules);

	        // Kiểm tra nếu không có dữ liệu
	        if (schedules.isEmpty()) {
	            showAlert(AlertType.INFORMATION, "Thông báo", "Không có lịch làm việc",
	                    "Không có lịch làm việc vào ngày " + date.format(dateFormatter));
	            scheduleTable.setItems(FXCollections.observableArrayList());
	        } else {
	            scheduleTable.setItems(scheduleList);
	        }

	        dateLabel.setText("Lịch làm việc ngày: " + date.format(dateFormatter));
	        updateShiftSummary(schedules);
	        showDayView();
	        statusLabel.setText("Trạng thái: Đã tải lịch làm việc ngày " + date.format(dateFormatter));
	    } catch (Exception e) {
	        showAlert(AlertType.ERROR, "Lỗi", "Không thể tải lịch làm việc", e.getMessage());
	        statusLabel.setText("Trạng thái: Lỗi khi tải lịch làm việc");
	    }
	}

	private void loadWeekSchedule() {
	    try {
	        LocalDate today = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now(); // Sửa: sử dụng ngày hiện tại
	        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
	        LocalDate endOfWeek = startOfWeek.plusDays(6);

	        List<WorkSchedule> schedules = scheduleService.getSchedulesByStaffAndDateRange(currentStaffId, startOfWeek,
	                endOfWeek);

	        scheduleList = FXCollections.observableArrayList(schedules);

	        // Kiểm tra nếu không có dữ liệu
	        if (schedules.isEmpty()) {
	            showAlert(AlertType.INFORMATION, "Thông báo", "Không có lịch làm việc",
	                    "Không có lịch làm việc trong tuần từ " + startOfWeek.format(dateFormatter) + " đến "
	                            + endOfWeek.format(dateFormatter));
	            scheduleTable.setItems(FXCollections.observableArrayList());
	        } else {
	            scheduleTable.setItems(scheduleList);
	        }

	        dateLabel.setText("Lịch làm việc từ: " + startOfWeek.format(dateFormatter) + " đến "
	                + endOfWeek.format(dateFormatter));

	        updateShiftSummary(schedules);
	        populateWeekView(schedules);
	        showWeekView();

	        statusLabel.setText("Trạng thái: Đã tải lịch làm việc tuần từ " + startOfWeek.format(dateFormatter)
	                + " đến " + endOfWeek.format(dateFormatter));
	    } catch (Exception e) {
	        showAlert(AlertType.ERROR, "Lỗi", "Không thể tải lịch làm việc theo tuần", e.getMessage());
	        statusLabel.setText("Trạng thái: Lỗi khi tải lịch làm việc tuần");
	    }
	}

	private void loadMonthSchedule() {
	    try {
	        LocalDate date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now(); // Sửa: sử dụng ngày hiện tại
	        LocalDate startOfMonth = date.withDayOfMonth(1);
	        LocalDate endOfMonth = date.withDayOfMonth(date.getMonth().length(date.isLeapYear()));

	        List<WorkSchedule> schedules = scheduleService.getSchedulesByStaffAndDateRange(currentStaffId, startOfMonth,
	                endOfMonth);

	        scheduleList = FXCollections.observableArrayList(schedules);

	        // Kiểm tra nếu không có dữ liệu
	        if (schedules.isEmpty()) {
	            showAlert(AlertType.INFORMATION, "Thông báo", "Không có lịch làm việc",
	                    "Không có lịch làm việc trong tháng " + date.getMonthValue() + "/" + date.getYear());
	            scheduleTable.setItems(FXCollections.observableArrayList());
	        } else {
	            scheduleTable.setItems(scheduleList);
	        }

	        dateLabel.setText("Lịch làm việc tháng: " + date.getMonthValue() + "/" + date.getYear());
	        updateShiftSummary(schedules);
	        showDayView();

	        statusLabel
	                .setText("Trạng thái: Đã tải lịch làm việc tháng " + date.getMonthValue() + "/" + date.getYear());
	    } catch (Exception e) {
	        showAlert(AlertType.ERROR, "Lỗi", "Không thể tải lịch làm việc theo tháng", e.getMessage());
	        statusLabel.setText("Trạng thái: Lỗi khi tải lịch làm việc tháng");
	    }
	}
	private void loadMyRequests() {
	    List<ShiftRequest> requests = scheduleService.getRequestsByStaffId(currentStaffId);
	    ObservableList<ShiftRequest> data = FXCollections.observableArrayList(requests);

	    colDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRequestDate().toString()));
	    colShift.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getShift().toString()));
	    colType.setCellValueFactory(cell -> new SimpleStringProperty(
	    	    cell.getValue().getType() == RequestType.LEAVE ? "Xin nghỉ" : "Đăng ký làm"));
	    colStatus.setCellValueFactory(cell -> {
	        String display;
	        switch (cell.getValue().getStatus()) {
	            case PENDING -> display = "Đang chờ xác nhận";
	            case APPROVED -> display = "Đã duyệt";
	            case REJECTED -> display = "Từ chối";
	            default -> display = "Không xác định";
	        }
	        return new SimpleStringProperty(display);
	    });
	    colReason.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getReason()));

	    tblRequests.setItems(data);
	}
	private void populateWeekView(List<WorkSchedule> schedules) {
		clearWeekViewContainers();

		for (WorkSchedule schedule : schedules) {
			LocalDate date = schedule.getWorkDate();
			Shift shift = schedule.getShift();

			if (date == null || shift == null)
				continue;

			String task = schedule.getTask() != null ? schedule.getTask() : "Ca làm việc";
			String time = schedule.getStartTime() != null && schedule.getEndTime() != null
					? schedule.getStartTime() + " - " + schedule.getEndTime()
					: "";
			Label label = new Label(task + "\n" + time);
			label.setStyle(
					"-fx-padding: 5; -fx-background-color: #f8f9fa; -fx-background-radius: 3; -fx-text-alignment: center;");

			int dayOfWeek = date.getDayOfWeek().getValue();
			addToWeekViewContainer(dayOfWeek, shift, label);
		}
	}

	private void clearWeekViewContainers() {
		monMorning.getChildren().clear();
		tueMorning.getChildren().clear();
		wedMorning.getChildren().clear();
		thuMorning.getChildren().clear();
		friMorning.getChildren().clear();
		satMorning.getChildren().clear();
		sunMorning.getChildren().clear();

		monAfternoon.getChildren().clear();
		tueAfternoon.getChildren().clear();
		wedAfternoon.getChildren().clear();
		thuAfternoon.getChildren().clear();
		friAfternoon.getChildren().clear();
		satAfternoon.getChildren().clear();
		sunAfternoon.getChildren().clear();

		monEvening.getChildren().clear();
		tueEvening.getChildren().clear();
		wedEvening.getChildren().clear();
		thuEvening.getChildren().clear();
		friEvening.getChildren().clear();
		satEvening.getChildren().clear();
		sunEvening.getChildren().clear();
	}

	private void addToWeekViewContainer(int dayOfWeek, Shift shift, Label label) {
		if (shift == Shift.MORNING) {
			switch (dayOfWeek) {
			case 1:
				monMorning.getChildren().add(label);
				break;
			case 2:
				tueMorning.getChildren().add(label);
				break;
			case 3:
				wedMorning.getChildren().add(label);
				break;
			case 4:
				thuMorning.getChildren().add(label);
				break;
			case 5:
				friMorning.getChildren().add(label);
				break;
			case 6:
				satMorning.getChildren().add(label);
				break;
			case 7:
				sunMorning.getChildren().add(label);
				break;
			}
		} else if (shift == Shift.AFTERNOON) {
			switch (dayOfWeek) {
			case 1:
				monAfternoon.getChildren().add(label);
				break;
			case 2:
				tueAfternoon.getChildren().add(label);
				break;
			case 3:
				wedAfternoon.getChildren().add(label);
				break;
			case 4:
				thuAfternoon.getChildren().add(label);
				break;
			case 5:
				friAfternoon.getChildren().add(label);
				break;
			case 6:
				satAfternoon.getChildren().add(label);
				break;
			case 7:
				sunAfternoon.getChildren().add(label);
				break;
			}
		} else if (shift == Shift.EVENING) {
			switch (dayOfWeek) {
			case 1:
				monEvening.getChildren().add(label);
				break;
			case 2:
				tueEvening.getChildren().add(label);
				break;
			case 3:
				wedEvening.getChildren().add(label);
				break;
			case 4:
				thuEvening.getChildren().add(label);
				break;
			case 5:
				friEvening.getChildren().add(label);
				break;
			case 6:
				satEvening.getChildren().add(label);
				break;
			case 7:
				sunEvening.getChildren().add(label);
				break;
			}
		}
	}

	private void showScheduleDetails(WorkSchedule schedule) {
		if (schedule == null) {
			additionalInfoArea.clear();
			return;
		}

		StringBuilder details = new StringBuilder();
		details.append("Mã lịch: ").append(schedule.getScheduleID()).append("\n");
		details.append("Ngày: ").append(schedule.getWorkDate().format(dateFormatter)).append("\n");
		details.append("Ca: ").append(schedule.getShift() != null ? schedule.getShift().name() : "N/A").append("\n");

		if (schedule.getStartTime() != null) {
			details.append("Giờ bắt đầu: ").append(schedule.getStartTime()).append("\n");
		}

		if (schedule.getEndTime() != null) {
			details.append("Giờ kết thúc: ").append(schedule.getEndTime()).append("\n");
		}

		if (schedule.getLocation() != null && !schedule.getLocation().isEmpty()) {
			details.append("Địa điểm: ").append(schedule.getLocation()).append("\n");
		}

		if (schedule.getTask() != null && !schedule.getTask().isEmpty()) {
			details.append("Công việc: ").append(schedule.getTask()).append("\n");
		}

		if (schedule.getNote() != null && !schedule.getNote().isEmpty()) {
			details.append("Ghi chú: ").append(schedule.getNote());
		}

		additionalInfoArea.setText(details.toString());
	}

	@FXML
	private void applyFilter() {
		applyShiftFilter();
		statusLabel.setText("Trạng thái: Đã lọc danh sách theo ca làm việc");
	}

	private void applyShiftFilter() {
		if (scheduleList == null)
			return;

		ObservableList<WorkSchedule> filteredList = FXCollections.observableArrayList(scheduleList);
		String selectedShift = shiftFilter.getValue();

		if (selectedShift != null && !selectedShift.equals("Tất cả")) {
			filteredList.removeIf(schedule -> schedule.getShift() == null
					|| !schedule.getShift().name().equalsIgnoreCase(selectedShift));
		}

		scheduleTable.setItems(filteredList);
	}

	private void updateShiftSummary(List<WorkSchedule> schedules) {
		int totalShifts = schedules.size();
		int morningShifts = 0;
		int afternoonShifts = 0;
		int eveningShifts = 0;

		for (WorkSchedule schedule : schedules) {
			Shift shift = schedule.getShift();
			if (shift == Shift.MORNING)
				morningShifts++;
			else if (shift == Shift.AFTERNOON)
				afternoonShifts++;
			else if (shift == Shift.EVENING)
				eveningShifts++;
		}

		totalShiftsLabel.setText(String.valueOf(totalShifts));
		morningShiftsLabel.setText(String.valueOf(morningShifts));
		afternoonShiftsLabel.setText(String.valueOf(afternoonShifts));
		eveningShiftsLabel.setText(String.valueOf(eveningShifts));
	}

	private void showDayView() {
		dayView.setVisible(true);
		dayView.setManaged(true);
		weekView.setVisible(false);
		weekView.setManaged(false);
	}

	private void showWeekView() {
		dayView.setVisible(false);
		dayView.setManaged(false);
		weekView.setVisible(true);
		weekView.setManaged(true);
	}

	@FXML
	private void requestLeave() {
	    Dialog<Map<String, Object>> dialog = new Dialog<>();
	    dialog.setTitle("Yêu cầu nghỉ phép");
	    dialog.setHeaderText("Đăng ký nghỉ phép");

	    ButtonType requestButtonType = new ButtonType("Gửi yêu cầu", ButtonBar.ButtonData.OK_DONE);
	    dialog.getDialogPane().getButtonTypes().addAll(requestButtonType, ButtonType.CANCEL);

	    GridPane grid = new GridPane();
	    grid.setHgap(10);
	    grid.setVgap(10);
	    grid.setPadding(new Insets(20, 150, 10, 10));

	    DatePicker leaveDatePicker = new DatePicker(LocalDate.now().plusDays(1));
	    leaveDatePicker.setConverter(new StringConverter<>() {
	        @Override
	        public String toString(LocalDate date) {
	            return date != null ? dateFormatter.format(date) : "";
	        }

	        @Override
	        public LocalDate fromString(String string) {
	            return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
	        }
	    });

	    ComboBox<Shift> shiftComboBox = new ComboBox<>();
	    shiftComboBox.setItems(FXCollections.observableArrayList(
	    	    Shift.MORNING, Shift.AFTERNOON, Shift.EVENING
	    	));
	    shiftComboBox.setPromptText("Chọn ca cần nghỉ");

	    TextArea reasonTextArea = new TextArea();
	    reasonTextArea.setPromptText("Nhập lý do nghỉ phép");
	    reasonTextArea.setPrefRowCount(3);

	    grid.add(new Label("Ngày nghỉ:"), 0, 0);
	    grid.add(leaveDatePicker, 1, 0);
	    grid.add(new Label("Ca nghỉ:"), 0, 1);
	    grid.add(shiftComboBox, 1, 1);
	    grid.add(new Label("Lý do:"), 0, 2);
	    grid.add(reasonTextArea, 1, 2);

	    dialog.getDialogPane().setContent(grid);

	    dialog.setResultConverter(dialogButton -> {
	        if (dialogButton == requestButtonType) {
	            Map<String, Object> result = new HashMap<>();
	            result.put("date", leaveDatePicker.getValue());
	            result.put("shift", shiftComboBox.getValue());
	            result.put("reason", reasonTextArea.getText().trim());
	            return result;
	        }
	        return null;
	    });

	    Optional<Map<String, Object>> result = dialog.showAndWait();

	    result.ifPresent(data -> {
	        LocalDate leaveDate = (LocalDate) data.get("date");
	        Shift shift = (Shift) data.get("shift");
	        String reason = (String) data.get("reason");
	     // Kiểm tra xem có lịch làm việc không trước khi cho nghỉ
	        if (leaveDate == null || shift == null || reason == null || reason.isEmpty()) {
	            showAlert(AlertType.WARNING, "Cảnh báo", "Thiếu thông tin",
	                "Vui lòng chọn ngày, ca nghỉ và nhập lý do nghỉ phép.");
	            return;
	        }
	        if (!scheduleService.isScheduleExists(currentStaffId, leaveDate, shift.name())) {
	            showAlert(AlertType.WARNING, "Cảnh báo", "Không có lịch làm việc",
	                    "Bạn không có lịch làm việc vào ca " + shift.name() + " ngày " + leaveDate.format(dateFormatter) + ". Không thể gửi yêu cầu nghỉ.");
	            return;
	        }

	        // Gọi service để lưu vào bảng shift_request với type = LEAVE
	        boolean success = scheduleService.sendShiftRequest(
	            currentStaffId, leaveDate, shift, RequestType.LEAVE, reason
	        );

	        if (success) {
	            showAlert(AlertType.INFORMATION, "Thành công", "Đã gửi yêu cầu nghỉ phép",
	                    "Yêu cầu nghỉ ca " + shift.name() + " ngày " + leaveDate.format(dateFormatter) + " đã được gửi.");
	            statusLabel.setText("Trạng thái: Đã gửi yêu cầu nghỉ phép");
	        } else {
	            showAlert(AlertType.ERROR, "Lỗi", "Không thể gửi yêu cầu",
	                    "Có thể đã có yêu cầu trùng hoặc ngày không hợp lệ.");
	            statusLabel.setText("Trạng thái: Lỗi khi gửi yêu cầu nghỉ phép");
	        }
	    });
	}


	@FXML
	private void requestShiftChange() {
		Dialog<Map<String, Object>> dialog = new Dialog<>();
		dialog.setTitle("Yêu cầu đổi ca");
		dialog.setHeaderText("Đăng ký đổi ca làm việc");

		ButtonType requestButtonType = new ButtonType("Gửi yêu cầu", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(requestButtonType, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		DatePicker currentDatePicker = new DatePicker(LocalDate.now().plusDays(1));
		currentDatePicker.setConverter(new StringConverter<LocalDate>() {
			@Override
			public String toString(LocalDate date) {
				return date != null ? dateFormatter.format(date) : "";
			}

			@Override
			public LocalDate fromString(String string) {
				return string != null && !string.isEmpty() ? LocalDate.parse(string, dateFormatter) : null;
			}
		});

		ComboBox<Shift> currentShiftComboBox = new ComboBox<>();
		currentShiftComboBox.getItems().addAll(Shift.MORNING, Shift.AFTERNOON, Shift.EVENING);
		currentShiftComboBox.setConverter(new StringConverter<Shift>() {
			@Override
			public String toString(Shift shift) {
				if (shift == null)
					return null;
				switch (shift) {
				case MORNING:
					return "Ca sáng (8:00 - 12:00)";
				case AFTERNOON:
					return "Ca chiều (13:00 - 17:00)";
				case EVENING:
					return "Ca tối (18:00 - 22:00)";
				default:
					return shift.name();
				}
			}

			@Override
			public Shift fromString(String string) {
				return null;
			}
		});

		DatePicker desiredDatePicker = new DatePicker(LocalDate.now().plusDays(2));
		desiredDatePicker.setConverter(new StringConverter<LocalDate>() {
			@Override
			public String toString(LocalDate date) {
				return date != null ? dateFormatter.format(date) : "";
			}

			@Override
			public LocalDate fromString(String string) {
				return string != null && !string.isEmpty() ? LocalDate.parse(string, dateFormatter) : null;
			}
		});

		ComboBox<Shift> desiredShiftComboBox = new ComboBox<>();
		desiredShiftComboBox.getItems().addAll(Shift.MORNING, Shift.AFTERNOON, Shift.EVENING);
		desiredShiftComboBox.setConverter(currentShiftComboBox.getConverter());

		TextArea reasonTextArea = new TextArea();
		reasonTextArea.setPromptText("Nhập lý do đổi ca");
		reasonTextArea.setPrefRowCount(3);

		grid.add(new Label("Ngày hiện tại:"), 0, 0);
		grid.add(currentDatePicker, 1, 0);
		grid.add(new Label("Ca hiện tại:"), 0, 1);
		grid.add(currentShiftComboBox, 1, 1);
		grid.add(new Label("Ngày muốn đổi:"), 0, 2);
		grid.add(desiredDatePicker, 1, 2);
		grid.add(new Label("Ca muốn đổi:"), 0, 3);
		grid.add(desiredShiftComboBox, 1, 3);
		grid.add(new Label("Lý do:"), 0, 4);
		grid.add(reasonTextArea, 1, 4);

		dialog.getDialogPane().setContent(grid);

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == requestButtonType) {
				Map<String, Object> result = new HashMap<>();
				result.put("currentDate", currentDatePicker.getValue());
				result.put("currentShift", currentShiftComboBox.getValue());
				result.put("desiredDate", desiredDatePicker.getValue());
				result.put("desiredShift", desiredShiftComboBox.getValue());
				result.put("reason", reasonTextArea.getText().trim());
				return result;
			}
			return null;
		});

		Optional<Map<String, Object>> result = dialog.showAndWait();

		result.ifPresent(data -> {
			LocalDate currentDate = (LocalDate) data.get("currentDate");
			Shift currentShift = (Shift) data.get("currentShift");
			LocalDate desiredDate = (LocalDate) data.get("desiredDate");
			Shift desiredShift = (Shift) data.get("desiredShift");
			String reason = (String) data.get("reason");

			if (currentDate == null || currentShift == null || desiredDate == null || desiredShift == null) {
				showAlert(AlertType.WARNING, "Cảnh báo", "Thiếu thông tin",
						"Vui lòng điền đầy đủ thông tin ca làm việc.");
				return;
			}

			if (reason == null || reason.isEmpty()) {
				showAlert(AlertType.WARNING, "Cảnh báo", "Thiếu thông tin", "Vui lòng nhập lý do đổi ca.");
				return;
			}

			boolean success = scheduleService.requestShiftChange(currentStaffId, currentDate, currentShift, desiredDate,
					desiredShift, reason);

			if (success) {
				showAlert(AlertType.INFORMATION, "Thành công", "Đã gửi yêu cầu đổi ca",
						"Yêu cầu đổi từ ca " + currentShift + " ngày " + currentDate.format(dateFormatter) + " sang ca "
								+ desiredShift + " ngày " + desiredDate.format(dateFormatter)
								+ " đã được gửi và đang chờ xét duyệt.");
				statusLabel.setText("Trạng thái: Đã gửi yêu cầu đổi ca");
			} else {
				showAlert(AlertType.ERROR, "Lỗi", "Không thể gửi yêu cầu đổi ca",
						"Ca hiện tại không tồn tại, ca mong muốn đã được đăng ký, hoặc có yêu cầu đổi ca đang chờ xử lý.");
				statusLabel.setText("Trạng thái: Lỗi khi gửi yêu cầu đổi ca");
			}
		});
	}

	@FXML
	private void selectSchedule() {
		if (scheduleList == null || scheduleList.isEmpty()) {
			showAlert(AlertType.WARNING, "Cảnh báo", "Không có ca làm việc",
					"Hiện tại không có ca làm việc nào để lựa chọn.");
			return;
		}

		// Tạo dialog để hiển thị danh sách ca làm việc
		Dialog<WorkSchedule> dialog = new Dialog<>();
		dialog.setTitle("Lựa chọn ca làm việc");
		dialog.setHeaderText("Chọn một ca làm việc từ lịch làm việc hiện tại");

		ButtonType selectButtonType = new ButtonType("Chọn", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

		// Tạo TableView để hiển thị danh sách ca làm việc
		TableView<WorkSchedule> scheduleSelectionTable = new TableView<>();
		scheduleSelectionTable.setItems(scheduleList);

		TableColumn<WorkSchedule, LocalDate> dateCol = new TableColumn<>("Ngày");
		dateCol.setCellValueFactory(new PropertyValueFactory<>("workDate"));
		dateCol.setCellFactory(column -> new TableCell<WorkSchedule, LocalDate>() {
			@Override
			protected void updateItem(LocalDate item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : dateFormatter.format(item));
			}
		});

		TableColumn<WorkSchedule, String> shiftCol = new TableColumn<>("Ca làm việc");
		shiftCol.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getShift() != null ? cellData.getValue().getShift().name() : ""));

		TableColumn<WorkSchedule, LocalTime> startTimeCol = new TableColumn<>("Giờ bắt đầu");
		startTimeCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
		startTimeCol.setCellFactory(column -> new TableCell<WorkSchedule, LocalTime>() {
			@Override
			protected void updateItem(LocalTime item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : timeFormatter.format(item));
			}
		});

		TableColumn<WorkSchedule, LocalTime> endTimeCol = new TableColumn<>("Giờ kết thúc");
		endTimeCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
		endTimeCol.setCellFactory(column -> new TableCell<WorkSchedule, LocalTime>() {
			@Override
			protected void updateItem(LocalTime item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : timeFormatter.format(item));
			}
		});

		TableColumn<WorkSchedule, String> locationCol = new TableColumn<>("Store");
		locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));

		scheduleSelectionTable.getColumns().addAll(dateCol, shiftCol, startTimeCol, endTimeCol, locationCol);
		scheduleSelectionTable.setPrefWidth(600);
		scheduleSelectionTable.setPrefHeight(400);

		dialog.getDialogPane().setContent(scheduleSelectionTable);

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == selectButtonType) {
				return scheduleSelectionTable.getSelectionModel().getSelectedItem();
			}
			return null;
		});

		Optional<WorkSchedule> result = dialog.showAndWait();
		result.ifPresent(schedule -> {
			// Điền thông tin từ ca làm việc được chọn vào form đăng ký
			registrationDatePicker.setValue(schedule.getWorkDate());
			shiftSelector.setValue(schedule.getShift());
//			locationSelector.setValue(schedule.getLocation());
			registrationNotes.setText(schedule.getNote() != null ? schedule.getNote() : "");
			statusLabel.setText("Trạng thái: Đã chọn ca làm việc ngày " + schedule.getWorkDate().format(dateFormatter));
		});
	}

	@FXML
	private void refreshSchedule() {
		String mode = viewModeSelector.getValue();
		switch (mode) {
		case "Hôm nay":
			loadScheduleByDate(datePicker.getValue());
			break;
		case "Tuần":
			loadWeekSchedule();
			break;
		case "Tháng":
			loadMonthSchedule();
			break;
		}
		statusLabel.setText("Trạng thái: Đã làm mới lịch làm việc");
	}

	@FXML
	private void registerShift() {
	    if (!RoleChecker.hasPermission("REGISTER_SHIFT")) {
	        showAlert(AlertType.ERROR, "Lỗi", "Không có quyền", "Bạn không có quyền đăng ký ca làm việc.");
	        return;
	    }

	    LocalDate date = registrationDatePicker.getValue();
	    Shift shift = shiftSelector.getValue();
	    RequestType type = typeSelector.getValue(); // Luôn là WORK do bạn đã khóa
	    String notes = registrationNotes.getText().trim();

	    // Kiểm tra điều kiện
	    if (date == null || shift == null) {
	        showAlert(AlertType.WARNING, "Cảnh báo", "Chưa đủ thông tin", "Vui lòng chọn ngày và ca làm.");
	        return;
	    }

	    if (date.isBefore(LocalDate.now().plusDays(1))) { // Phải đăng ký trước 1 ngày
	        showAlert(AlertType.WARNING, "Cảnh báo", "Ngày không hợp lệ", 
	            "Phải đăng ký trước ít nhất 1 ngày so với ngày làm việc.");
	        return;
	    }

	    try {
	        // Kiểm tra xem đã có đăng ký cho ca này chưa
	        if (scheduleService.isScheduleExists(currentStaffId, date, shift.name())) {
	            showAlert(AlertType.WARNING, "Cảnh báo", "Đã đăng ký", 
	                "Bạn đã đăng ký ca này rồi. Vui lòng chọn ca khác.");
	            return;
	        }

	        boolean success = scheduleService.registerShift(currentStaffId, date, shift, "Cửa hàng chính", notes);

	        if (success) {
	            showAlert(AlertType.INFORMATION, "Thành công", "Đã đăng ký ca làm", 
	                "Ca làm " + shift + " ngày " + date.format(dateFormatter) + " đã được đăng ký.");

	            // Reset form
	            registrationDatePicker.setValue(LocalDate.now().plusDays(1));
	            shiftSelector.setValue(null);
	            registrationNotes.clear();

	            statusLabel.setText("Trạng thái: Đã đăng ký ca làm thành công");
	        } else {
	            showAlert(AlertType.ERROR, "Lỗi", "Không thể đăng ký ca làm", 
	                "Có lỗi xảy ra khi đăng ký. Vui lòng thử lại.");
	        }
	    } catch (Exception e) {
	        showAlert(AlertType.ERROR, "Lỗi", "Không thể đăng ký ca làm", e.getMessage());
	        e.printStackTrace();
	    }
	}

	@FXML
	private void cancelRegistration() {
		registrationDatePicker.setValue(LocalDate.now().plusDays(1));
		shiftSelector.setValue(null);
		typeSelector.setValue(null);
		registrationNotes.clear();
		statusLabel.setText("Trạng thái: Đã hủy đăng ký ca làm");
	}

	@FXML
	private void showHelp() {
		showAlert(AlertType.INFORMATION, "Trợ giúp", "Hướng dẫn sử dụng",
				"Phần quản lý lịch làm việc cho phép bạn:\n\n" + "- Xem lịch làm việc theo ngày, tuần, tháng\n"
						+ "- Đăng ký ca làm việc mới\n" + "- Yêu cầu nghỉ phép hoặc đổi ca\n\n"
						+ "Liên hệ quản trị viên để được hỗ trợ thêm.");
	}

	@FXML
	private void goToHome() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/staff_home.fxml"));
			Parent root = loader.load();
			Stage stage = (Stage) homeButton.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
			showAlert(AlertType.ERROR, "Lỗi", "Không thể trở về trang chủ", "Đã xảy ra lỗi: " + e.getMessage());
		}
	}

	@FXML
	private void exitApplication() {
		Stage stage = (Stage) scheduleTable.getScene().getWindow();
		stage.close();
	}

	private void showAlert(AlertType alertType, String title, String header, String content) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
}