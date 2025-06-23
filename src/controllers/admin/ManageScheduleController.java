package controllers.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.ShiftAssignment;
import model.ShiftRequest;
import model.Staff;
import model.WorkSchedule;
import service.ScheduleService;
import service.StaffService;
import utils.LanguageChangeListener;
import utils.LanguageManagerAd;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;


import enums.RequestStatus;
import enums.Shift;

public class ManageScheduleController implements Initializable, LanguageChangeListener {

	@FXML private Label lblTitle, lblPick, lblShift, lblMon, lblTue, lblWed, lblThus, lblFri, lblSat, lblSun, lblMorning, lblAfternoon, lblEvening;
	@FXML
	private DatePicker startDatePicker;

	@FXML
	private DatePicker endDatePicker;

	@FXML
	private GridPane scheduleGrid;

	@FXML
	private Button exportPdfButton, btnShow, btnViewRequests;
	@FXML
	private Button autoAssignButton;

	private List<Staff> allAvailableEmployees;
	private Map<LocalDate, Map<Shift, List<WorkSchedule>>> weeklySchedule;

	private final Map<String, Color> roleColors = new HashMap<>();
	private final Map<String, Integer> requiredRoles = new HashMap<>();

	private final StaffService staffService = new StaffService();
	private final ScheduleService scheduleService = new ScheduleService();

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		
		LanguageManagerAd.addListener(this);
		loadTexts();
		
		roleColors.put("STAFF_CARE", Color.BLUEVIOLET);
		roleColors.put("STAFF_CASHIER", Color.DARKORANGE);
		roleColors.put("STAFF_RECEPTION", Color.DEEPPINK);
		roleColors.put("ADMIN", Color.BLUE);

		requiredRoles.put("STAFF_CARE", 3);
		requiredRoles.put("STAFF_CASHIER", 1);
		requiredRoles.put("STAFF_RECEPTION", 1);
		requiredRoles.put("ADMIN", 1);
		System.out.println("Initial requiredRoles: " + requiredRoles);

		loadAllAvailableStaff();

		weeklySchedule = new HashMap<>();

		LocalDate today = LocalDate.now();
		LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		startDatePicker.setValue(startOfWeek);
		endDatePicker.setValue(endOfWeek);

		loadScheduleForWeek();
	}

	private void loadAllAvailableStaff() {
		allAvailableEmployees = staffService.getAllStaffs().stream()
				.filter(staff -> !"OUT".equalsIgnoreCase(staff.getRole().getRoleName())).collect(Collectors.toList());
	}

	@FXML
	private void loadScheduleForWeek() {
		LocalDate startDate = startDatePicker.getValue();
		LocalDate endDate = endDatePicker.getValue();

		if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
			showAlert(LanguageManagerAd.getString("manageStaff.delete.failed.title"), LanguageManagerAd.getString("manageSchedule.alert.invalidTimeRange"));
			return;
		}

		weeklySchedule.clear();
		for (int i = 0; i < 7; i++) {
			LocalDate currentDate = startDate.plusDays(i);
			weeklySchedule.put(currentDate, new HashMap<>());
			weeklySchedule.get(currentDate).put(Shift.MORNING, new ArrayList<>());
			weeklySchedule.get(currentDate).put(Shift.AFTERNOON, new ArrayList<>());
			weeklySchedule.get(currentDate).put(Shift.EVENING, new ArrayList<>());
		}

		List<WorkSchedule> fetchedSchedules = scheduleService.getWorkSchedulesByWeek(startDate, endDate);
		System.out.println(LanguageManagerAd.getString("manageSchedule.fetchedCount") + (fetchedSchedules != null ? fetchedSchedules.size() : 0));
		if (fetchedSchedules != null) {
			for (WorkSchedule schedule : fetchedSchedules) {
				LocalDate date = schedule.getWorkDate();
				Shift shift = schedule.getShift();
				if (weeklySchedule.containsKey(date) && weeklySchedule.get(date).containsKey(shift)) {
					weeklySchedule.get(date).get(shift).add(schedule);
				}
			}
		}

		populateScheduleGrid(startDate, endDate);
	}

	private void populateScheduleGrid(LocalDate startDate, LocalDate endDate) {
		// Xóa toàn bộ node cũ trong grid
		scheduleGrid.getChildren().clear();
		scheduleGrid.setHgap(10);
		scheduleGrid.setVgap(5);

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		DateTimeFormatter dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE", new Locale("vi"));

		// ===== Tiêu đề hàng (Ca làm) ở cột 0 =====
		for (int row = 1; row <= 3; row++) {
			Shift shift = Shift.values()[row - 1];
		    String shiftKey = switch (shift.name()) {
		        case "MORNING" -> "manageSchedule.shift.morning";
		        case "AFTERNOON" -> "manageSchedule.shift.afternoon";
		        case "EVENING" -> "manageSchedule.shift.evening";
		        default -> throw new IllegalArgumentException("Unexpected value: " + shift.name());
		    };
		    Label shiftLabel = new Label(LanguageManagerAd.getString(shiftKey));
		    shiftLabel.getStyleClass().add("schedule-title");
		    shiftLabel.setWrapText(true);
		    scheduleGrid.add(shiftLabel, 0, row);

		}

		// ===== Tiêu đề cột (Thứ + Ngày) và dữ liệu từng ô =====
		for (int day = 0; day < 7; day++) {
		    LocalDate currentDate = startDate.plusDays(day);
		    int columnIndex = day + 1;

		    String dayKey = getDayKey(currentDate);
		    String headerText = LanguageManagerAd.getString(dayKey) + "\n" +
		                        currentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

		    Label dayLabel = new Label(headerText);
		    dayLabel.getStyleClass().add("schedule-title");
		    dayLabel.setWrapText(true);
		    scheduleGrid.add(dayLabel, columnIndex, 0);
		    

		    for (int row = 1; row <= 3; row++) {
		        Shift shift = Shift.values()[row - 1];
		        List<WorkSchedule> schedulesInShift = weeklySchedule
		                .getOrDefault(currentDate, new HashMap<>())
		                .getOrDefault(shift, new ArrayList<>());

		        StackPane cellPane = createScheduleCell(currentDate, shift, schedulesInShift);
		        scheduleGrid.add(cellPane, columnIndex, row);
		    }
		}

		// Đánh dấu ô đã đủ nhân sự
		highlightFullRoles(startDate, endDate);
	}
	private String getDayKey(LocalDate date) {
	    DayOfWeek dow = date.getDayOfWeek();
	    return switch (dow) {
	        case MONDAY -> "manageSchedule.column.mon";
	        case TUESDAY -> "manageSchedule.column.tue";
	        case WEDNESDAY -> "manageSchedule.column.wed";
	        case THURSDAY -> "manageSchedule.column.thu";
	        case FRIDAY -> "manageSchedule.column.fri";
	        case SATURDAY -> "manageSchedule.column.sat";
	        case SUNDAY -> "manageSchedule.column.sun";
	    };
	}


	@FXML
	private void autoAssignShifts() {
		LocalDate selected = startDatePicker.getValue();

		LocalDate weekStart = selected.with(DayOfWeek.MONDAY);
		LocalDate thisWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);

		if (weekStart.isBefore(thisWeekStart)) {
			showAlert(LanguageManagerAd.getString("manageStaff.delete.failed.title"), LanguageManagerAd.getString("manageSchedule.alert.pastWeek"));
			return;
		}
		if (scheduleService.isWeekScheduled(weekStart)) {
			showAlert(LanguageManagerAd.getString("manageStaff.delete.failed.title"), LanguageManagerAd.getString("manageSchedule.alert.weekAlreadyScheduled"));
			return;
		}
		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
				LanguageManagerAd.getString("manageSchedule.confirm.autoAssignMessage", weekStart), ButtonType.YES,
				ButtonType.NO);
		Optional<ButtonType> result = confirm.showAndWait();

		if (result.isPresent() && result.get() == ButtonType.YES) {
			List<Staff> staffList = staffService.getAllStaffs();

			// Lấy dữ liệu từ service
			Pair<Map<Integer, List<Shift>>, Map<Integer, List<ShiftAssignment>>> requestData = scheduleService
					.getApprovedRequests(weekStart);

			Map<Integer, List<Shift>> leaveRequests = requestData.getKey();
			Map<Integer, List<ShiftAssignment>> preferredShifts = requestData.getValue();

			scheduleService.autoAssignWeekShifts(staffList, weekStart, leaveRequests, preferredShifts);
			loadScheduleForWeek();
		}
	}

	private StackPane createScheduleCell(LocalDate date, Shift shift, List<WorkSchedule> schedules) {
		StackPane cellPane = new StackPane();
		cellPane.setStyle("-fx-border-color: lightgray; -fx-border-width: 0.5px;");
		cellPane.setPadding(new Insets(5));

		ContextMenu contextMenu = new ContextMenu();
		MenuItem editItem = new MenuItem(LanguageManagerAd.getString("manageSchedule.editSchedule"));
		MenuItem deleteItem = new MenuItem(LanguageManagerAd.getString("manageSchedule.deleteSchedule.button"));
		contextMenu.getItems().addAll(editItem, deleteItem);

		final WorkSchedule[] selectedSchedule = { null };
		editItem.setOnAction(e -> {
			if (e.getSource() instanceof MenuItem) {
				// Pass the whole schedules list.
				handleEditSchedule(date, shift, schedules);
			}
		});

		deleteItem.setOnAction(e -> {
			if (e.getSource() instanceof MenuItem) {
				handleDeleteSchedule(date, shift, schedules);
			}
		});

		cellPane.setOnContextMenuRequested(event -> {
			if (!schedules.isEmpty()) {
				contextMenu.show(cellPane, event.getScreenX(), event.getScreenY());
			}
		});

		VBox employeeListVBox = new VBox(2);
		for (WorkSchedule schedule : schedules) {
			Label employeeLabel = new Label(schedule.getStaff().getFullName().toUpperCase());
			employeeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 0.9em;");
			employeeLabel
					.setTextFill(roleColors.getOrDefault(schedule.getStaff().getRole().getRoleName(), Color.BLACK));
			employeeLabel.setUserData(schedule);
			employeeLabel.setWrapText(true);

			// Create tooltip
			Tooltip tooltip = new Tooltip();
			tooltip.setText(LanguageManagerAd.getString("manageSchedule.tooltip.role", schedule.getStaff().getRole().getRoleName()) +"\n" + LanguageManagerAd.getString("manageSchedule.tooltip.note")
					+ (schedule.getNote() != null ? schedule.getNote() : "N/A"));
			employeeLabel.setTooltip(tooltip);

			employeeListVBox.getChildren().add(employeeLabel);
		}
		cellPane.getChildren().add(employeeListVBox);
		StackPane.setAlignment(employeeListVBox, Pos.TOP_LEFT);

		cellPane.setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY) {
				showEmployeeSelectionDialog(date, shift);
			}
		});

		return cellPane;
	}

	private void handleEditSchedule(LocalDate date, Shift shift, List<WorkSchedule> existingSchedules) {
		System.out.println(LanguageManagerAd.getString("manageSchedule.editScheduleCalled", shift, date));

		Stage dialogStage = new Stage();
		dialogStage.initModality(Modality.APPLICATION_MODAL);
		dialogStage.setTitle(LanguageManagerAd.getString("manageSchedule.editScheduleTitle"));

		VBox dialogVBox = new VBox(10);
		dialogVBox.setPadding(new Insets(10));

		// Use a ListView to display and select employees.
		ListView<WorkSchedule> scheduleListView = new ListView<>();
		scheduleListView.getItems().addAll(existingSchedules);
		scheduleListView.setCellFactory(param -> new ListCell<WorkSchedule>() {
			@Override
			protected void updateItem(WorkSchedule item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
				} else {
					setText(item.getStaff().getFullName() + " (" + item.getStaff().getRole().getRoleName() + ")");
					setTextFill(roleColors.getOrDefault(item.getStaff().getRole().getRoleName(), Color.BLACK));
				}
			}
		});

		TextField noteField = new TextField();
		noteField.setPromptText(LanguageManagerAd.getString("manageSchedule.editSchedule.notePrompt"));

		Button saveButton = new Button(LanguageManagerAd.getString("manageSchedule.editSchedule.saveButton"));
		saveButton.setOnAction(event -> {
			WorkSchedule selectedSchedule = scheduleListView.getSelectionModel().getSelectedItem();
			if (selectedSchedule != null) {
				String newNote = noteField.getText();
				selectedSchedule.setNote(newNote);
				scheduleService.updateSchedule(selectedSchedule.getScheduleID(), shift.name(), newNote);
				loadScheduleForWeek();
				dialogStage.close();
			} else {
				showAlert(LanguageManagerAd.getString("manageStaff.delete.failed.title"), LanguageManagerAd.getString("manageSchedule.editSchedule.selectStaff"));
			}
		});

		dialogVBox.getChildren().addAll(scheduleListView, noteField, saveButton);

		Scene dialogScene = new Scene(dialogVBox);
		dialogStage.setScene(dialogScene);
		dialogStage.showAndWait();
	}

	private void handleDeleteSchedule(LocalDate date, Shift shift, List<WorkSchedule> schedulesToDelete) {
		System.out.println(LanguageManagerAd.getString("manageSchedule.deleteScheduleCalled", shift, date));

		Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
		confirmationAlert.setTitle(LanguageManagerAd.getString("manageSchedule.deleteSchedule.title"));
		confirmationAlert.setHeaderText(LanguageManagerAd.getString("manageSchedule.deleteSchedule.header"));
		confirmationAlert.setContentText(LanguageManagerAd.getString("manageSchedule.deleteSchedule.content"));

		Optional<ButtonType> result = confirmationAlert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			// Show a dialog to select which schedule to delete
			Stage dialogStage = new Stage();
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.setTitle(LanguageManagerAd.getString("manageSchedule.deleteSchedule.dialogTitle"));

			VBox dialogVBox = new VBox(10);
			dialogVBox.setPadding(new Insets(10));

			ListView<WorkSchedule> scheduleListView = new ListView<>();
			scheduleListView.getItems().addAll(schedulesToDelete);
			scheduleListView.setCellFactory(param -> new ListCell<WorkSchedule>() {
				@Override
				protected void updateItem(WorkSchedule item, boolean empty) {
					super.updateItem(item, empty);
					if (empty || item == null) {
						setText(null);
					} else {
						setText(item.getStaff().getFullName() + " (" + item.getStaff().getRole().getRoleName() + ")");
						setTextFill(roleColors.getOrDefault(item.getStaff().getRole().getRoleName(), Color.BLACK));
					}
				}
			});

			Button deleteButton = new Button(LanguageManagerAd.getString("manageSchedule.deleteSchedule.button"));
			deleteButton.setOnAction(event -> {
				WorkSchedule selectedSchedule = scheduleListView.getSelectionModel().getSelectedItem();
				if (selectedSchedule != null) {
					weeklySchedule.get(date).get(shift).remove(selectedSchedule);
					scheduleService.deleteSchedule(selectedSchedule.getScheduleID());
					loadScheduleForWeek();
					dialogStage.close();
				} else {
					showAlert(LanguageManagerAd.getString("manageStaff.delete.failed.title"), LanguageManagerAd.getString("manageSchedule.deleteSchedule.selectStaff"));
				}
			});

			dialogVBox.getChildren().addAll(scheduleListView, deleteButton);
			Scene dialogScene = new Scene(dialogVBox);
			dialogStage.setScene(dialogScene);
			dialogStage.showAndWait();
		}
	}

	private void showEmployeeSelectionDialog(LocalDate date, Shift shift) {
		Stage dialogStage = new Stage();
		dialogStage.initModality(Modality.APPLICATION_MODAL);
		dialogStage.setTitle("Chọn nhân viên");

		List<Staff> availableEmployees = allAvailableEmployees.stream()
				.filter(employee -> !weeklySchedule.getOrDefault(date, new HashMap<>())
						.getOrDefault(shift, new ArrayList<>()).stream()
						.anyMatch(scheduled -> scheduled.getStaff().getId() == employee.getId()))
				.collect(Collectors.toList());

		// Filter out past dates.
		final LocalDate today = LocalDate.now();
		availableEmployees = availableEmployees.stream().filter(employee -> !date.isBefore(today))
				.collect(Collectors.toList());

		if (availableEmployees.isEmpty()) {
			if (date.isBefore(today)) {
				showAlert(LanguageManagerAd.getString("manageSchedule.alert.info"), LanguageManagerAd.getString("manageSchedule.pastNotAllowed"));
			} else if (date.isEqual(today)) {
				showAlert(LanguageManagerAd.getString("manageSchedule.alert.info"), LanguageManagerAd.getString("manageSchedule.todayNotAllowed"));
			} else {
				showAlert(LanguageManagerAd.getString("manageSchedule.alert.info"), LanguageManagerAd.getString("manageSchedule.noStaffAvailable"));
			}
			return;
		}

		List<WorkSchedule> currentSchedules = weeklySchedule.getOrDefault(date, new HashMap<>()).getOrDefault(shift,
				new ArrayList<>());
		Map<String, Long> currentRoleCounts = currentSchedules.stream()
				.collect(Collectors.groupingBy(ws -> ws.getStaff().getRole().getRoleName(), Collectors.counting()));

		VBox dialogVBox = new VBox(10);
		dialogVBox.setPadding(new Insets(10));

		for (Staff employee : availableEmployees) {
			HBox employeeBox = new HBox(10);
			Label employeeLabel = new Label(employee.getFullName() + " (" + employee.getRole().getRoleName() + ")");
			employeeLabel.setTextFill(roleColors.getOrDefault(employee.getRole().getRoleName(), Color.BLACK));

			TextField noteField = new TextField();
			noteField.setPromptText(LanguageManagerAd.getString("manageSchedule.note.placeholder"));

			Button addButton = new Button(LanguageManagerAd.getString("manageSchedule.button.add"));
			String employeeRole = employee.getRole().getRoleName();
			long currentCountForRole = currentRoleCounts.getOrDefault(employeeRole, 0L);
			int requiredForRole = requiredRoles.getOrDefault(employeeRole, 0);

			if (currentCountForRole >= requiredForRole) {
				addButton.setDisable(true);
				addButton.setText(LanguageManagerAd.getString("manageSchedule.button.full"));
			} else {
				// Disable adding to past dates
				if (date.isBefore(today)) {
					addButton.setDisable(true);
					addButton.setText(LanguageManagerAd.getString("manageSchedule.button.yesterday"));
				} else if (date.isEqual(today)) {
					addButton.setDisable(true);
					addButton.setText(LanguageManagerAd.getString("manageSchedule.button.today")); 
				} else {
					addButton.setOnAction(event -> {
						String note = noteField.getText();
						addEmployeeToSchedule(date, shift, employee, note);
						populateScheduleGrid(startDatePicker.getValue(), endDatePicker.getValue());
						dialogStage.close();
					});
				}
			}

			employeeBox.getChildren().addAll(employeeLabel, noteField, addButton);
			dialogVBox.getChildren().add(employeeBox);
		}

		Scene dialogScene = new Scene(dialogVBox);
		dialogStage.setScene(dialogScene);
		dialogStage.showAndWait();
	}

	private void addEmployeeToSchedule(LocalDate date, Shift shift, Staff employee, String note) {
		if (!weeklySchedule.containsKey(date)) {
			weeklySchedule.put(date, new HashMap<>());
		}
		if (!weeklySchedule.get(date).containsKey(shift)) {
			weeklySchedule.get(date).put(shift, new ArrayList<>());
		}
		WorkSchedule newSchedule = new WorkSchedule(employee, date, shift, note);
		weeklySchedule.get(date).get(shift).add(newSchedule);
		scheduleService.addSchedule(newSchedule);
	}

	private void showAlert(String title, String content) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}

	private void highlightFullRoles(LocalDate startDate, LocalDate endDate) {
		for (int day = 0; day < 7; day++) {
			LocalDate currentDate = startDate.plusDays(day);
			int columnIndex = day + 1;

			for (int row = 1; row <= 3; row++) {
				Shift shift = Shift.values()[row - 1];
				List<WorkSchedule> schedulesInShift = weeklySchedule.getOrDefault(currentDate, new HashMap<>())
						.getOrDefault(shift, new ArrayList<>());
				// Correctly retrieve the StackPane
				StackPane cell = null;
				for (Node node : scheduleGrid.getChildren()) {
					if (GridPane.getColumnIndex(node) == columnIndex && GridPane.getRowIndex(node) == row) {
						cell = (StackPane) node;
						break;
					}
				}

				if (cell != null) {
					cell.setStyle("-fx-border-color: lightgray; -fx-border-width: 0.5px;");
					Map<String, Long> roleCounts = schedulesInShift.stream().collect(
							Collectors.groupingBy(se -> se.getStaff().getRole().getRoleName(), Collectors.counting()));

					boolean allRolesMet = true;
					boolean allRolesFull = true;
					boolean hasStaff = !schedulesInShift.isEmpty();

					for (Map.Entry<String, Integer> entry : requiredRoles.entrySet()) {
						String role = entry.getKey();
						int required = entry.getValue();
						long currentCount = roleCounts.getOrDefault(role, 0L);
						if (currentCount < required) {
							allRolesFull = false;
						}
						if (currentCount != required) {
							allRolesMet = false;
						}
					}

					if (!hasStaff) {
						cell.setStyle(
								"-fx-background-color: lightcoral; -fx-border-color: lightgray; -fx-border-width: 0.5px;");
					} else if (allRolesMet) {
						cell.setStyle(
								"-fx-background-color: lightgreen; -fx-border-color: lightgray; -fx-border-width: 0.5px;");
					} else {
						cell.setStyle(
								"-fx-background-color: lightyellow; -fx-border-color: lightgray; -fx-border-width: 0.5px;");
					}
				}
			}
		}
	}

	private Node getNodeByRowColumnIndex(final int row, final int column, GridPane gridPane) {
		Node result = null;
		ObservableList<Node> children = gridPane.getChildren();

		for (Node node : children) {
			if (GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null
					&& GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
				result = node;
				break;
			}
		}

		return result;
	}

	@FXML
	private void exportScheduleToPdf() {
		LocalDate startDate = startDatePicker.getValue();
		LocalDate endDate = endDatePicker.getValue();

		if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
			showAlert(LanguageManagerAd.getString("manageStaff.delete.failed.title"), LanguageManagerAd.getString("manageSchedule.invalidRange"));
			return;
		}

		String filePath = "Schedule_" + startDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")) + "_"
				+ endDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".pdf";

		try {
			// Tạo đối tượng Document với kích thước trang A4 ngang
			Document document = new Document(PageSize.A4.rotate());
			PdfWriter.getInstance(document, new FileOutputStream(filePath));
			document.open();

			// Sử dụng font hỗ trợ tiếng Việt (Arial)
			String path = "lib/LiberationSerif-Regular.ttf";
			BaseFont bf = BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			Font fontHeader = new Font(bf, 12, Font.BOLD);
			Font fontContent = new Font(bf, 10, Font.NORMAL);

			// Thêm tiêu đề
			Paragraph title = new Paragraph(
					LanguageManagerAd.getString("manageSchedule.pdf.header", startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 
							endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))),
					fontHeader);
			title.setAlignment(Paragraph.ALIGN_CENTER);
			document.add(title);
			document.add(new Paragraph(" ")); // Thêm khoảng trống

			// Tạo bảng với 8 cột (1 cột cho ca, 7 cột cho ngày)
			PdfPTable table = new PdfPTable(8);
			table.setWidthPercentage(100);
			table.setWidths(new float[] { 1, 2, 2, 2, 2, 2, 2, 2 }); // Tỷ lệ chiều rộng cột

			// Thêm tiêu đề bảng
			PdfPCell cell = new PdfPCell(new Phrase(LanguageManagerAd.getString("manageSchedule.table.header.shift"), fontHeader));
			cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
			table.addCell(cell);

			DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd/MM\nEEE");
			for (int i = 0; i < 7; i++) {
				LocalDate currentDate = startDate.plusDays(i);
				cell = new PdfPCell(new Phrase(currentDate.format(dayFormatter), fontHeader));
				cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
				cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
				table.addCell(cell);
			}

			// Thêm dữ liệu lịch trình
			for (Shift shift : Shift.values()) {
				if (shift != Shift.NOSHIFT) {
					cell = new PdfPCell(new Phrase(shift.name(), fontContent));
					cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
					cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
					table.addCell(cell);

					for (int i = 0; i < 7; i++) {
						LocalDate currentDate = startDate.plusDays(i);
						List<WorkSchedule> schedulesForShift = weeklySchedule.getOrDefault(currentDate, new HashMap<>())
								.getOrDefault(shift, new ArrayList<>());
						StringBuilder employeeNames = new StringBuilder();
						for (WorkSchedule schedule : schedulesForShift) {
							employeeNames.append(schedule.getStaff().getFullName().toUpperCase());
							if (schedule.getNote() != null && !schedule.getNote().isEmpty()) {
								employeeNames.append(" (").append(schedule.getNote()).append(")");
							}
							employeeNames.append("\n");
						}
						cell = new PdfPCell(new Phrase(employeeNames.toString().trim(), fontContent));
						cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
						cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
						table.addCell(cell);
					}
				}
			}

			document.add(table);
			document.close();

			showAlert(LanguageManagerAd.getString("manageSchedule.pdf.success.title"), LanguageManagerAd.getString("manageSchedule.pdf.success.message"));

			// Mở file PDF sau khi xuất thành công
			File pdfFile = new File(filePath);
			if (pdfFile.exists()) {
				Desktop.getDesktop().open(pdfFile);
			} else {
				showAlert(LanguageManagerAd.getString("manageStaff.delete.failed.title"), LanguageManagerAd.getString("manageSchedule.pdf.fileNotFound"));
			}

		} catch (DocumentException | IOException e) {
			e.printStackTrace();
			showAlert(LanguageManagerAd.getString("manageStaff.delete.failed.title"), LanguageManagerAd.getString("manageSchedule.pdf.fail.message"));
		}
	}
	
	@FXML
	private void handleViewRequests() {
	    Dialog<Void> dialog = new Dialog<>();
	    dialog.setTitle(LanguageManagerAd.getString("manageSchedule.dialog.approve.title"));
	    dialog.setHeaderText(LanguageManagerAd.getString("manageSchedule.dialog.approve.header"));

	    ButtonType closeButtonType = new ButtonType(LanguageManagerAd.getString("manageSchedule.dialog.button.close"), ButtonBar.ButtonData.CANCEL_CLOSE);
	    dialog.getDialogPane().getButtonTypes().add(closeButtonType);

	    // Tạo bảng
	    TableView<ShiftRequest> table = new TableView<>();
	    table.setPrefHeight(300);
	    table.setPrefWidth(600);

	    TableColumn<ShiftRequest, String> colStaff = new TableColumn<>(LanguageManagerAd.getString("shiftRequest.table.header.staff"));
	    colStaff.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStaff().getFullName()));

	    TableColumn<ShiftRequest, String> colDate = new TableColumn<>(LanguageManagerAd.getString("shiftRequest.table.header.date"));
	    colDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRequestDate().toString()));

	    TableColumn<ShiftRequest, String> colShift = new TableColumn<>(LanguageManagerAd.getString("shiftRequest.table.header.shift"));
	    colShift.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getShift().name()));

	    TableColumn<ShiftRequest, String> colType = new TableColumn<>(LanguageManagerAd.getString("shiftRequest.table.header.type"));
	    colType.setCellValueFactory(cell -> new SimpleStringProperty(
	            switch (cell.getValue().getType()) {
	                case LEAVE -> LanguageManagerAd.getString("shiftRequest.type.leave");
	                case WORK -> LanguageManagerAd.getString("shiftRequest.type.work");
	            }
	    ));

	    TableColumn<ShiftRequest, String> colReason = new TableColumn<>( LanguageManagerAd.getString("shiftRequest.table.header.reason"));
	    colReason.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getReason()));

	    TableColumn<ShiftRequest, String> colStatus = new TableColumn<>( LanguageManagerAd.getString("shiftRequest.table.header.status"));
	    colStatus.setCellValueFactory(cell -> new SimpleStringProperty(
	            switch (cell.getValue().getStatus()) {
	                case PENDING ->  LanguageManagerAd.getString("shiftRequest.status.pending");
	                case APPROVED ->  LanguageManagerAd.getString("shiftRequest.status.approved");
	                case REJECTED ->  LanguageManagerAd.getString("shiftRequest.status.rejected");
	            }
	    ));

	    table.getColumns().addAll(colStaff, colDate, colShift, colType, colReason, colStatus);

	    // Load data
	    ObservableList<ShiftRequest> requests = FXCollections.observableArrayList(scheduleService.getPendingRequests());
	    table.setItems(requests);

	    // Nút hành động
	    Button btnApprove = new Button( LanguageManagerAd.getString("shiftRequest.button.approve"));
	    btnApprove.setOnAction(e -> {
	        ShiftRequest selected = table.getSelectionModel().getSelectedItem();
	        if (selected != null) {
	            if (scheduleService.approveRequest(selected.getId(), RequestStatus.APPROVED)) {
	                showAlert( LanguageManagerAd.getString("shiftRequest.alert.approve.success.title"),  LanguageManagerAd.getString("shiftRequest.alert.approve.success.message"));
	                table.getItems().remove(selected);
	            }
	        }
	    });

	    Button btnReject = new Button( LanguageManagerAd.getString("shiftRequest.button.reject"));
	    btnReject.setOnAction(e -> {
	        ShiftRequest selected = table.getSelectionModel().getSelectedItem();
	        if (selected != null) {
	            if (scheduleService.approveRequest(selected.getId(), RequestStatus.REJECTED)) {
	                showAlert(LanguageManagerAd.getString("shiftRequest.alert.reject.success.title"),  LanguageManagerAd.getString("shiftRequest.alert.reject.success.message"));
	                table.getItems().remove(selected);
	            }
	        }
	    });

	    HBox actionBox = new HBox(10, btnApprove, btnReject);
	    actionBox.setAlignment(Pos.CENTER_RIGHT);
	    actionBox.setPadding(new Insets(10, 0, 0, 0));

	    VBox content = new VBox(10, table, actionBox);
	    content.setPadding(new Insets(10));

	    dialog.getDialogPane().setContent(content);
	    dialog.showAndWait();
	}

	@Override
	public void onLanguageChanged() {
		loadTexts();
		loadScheduleForWeek();
	}
	private void loadTexts() {
		lblTitle.setText(LanguageManagerAd.getString("manageSchedule.title"));
		lblPick.setText(LanguageManagerAd.getString("manageSchedule.week.select"));
		startDatePicker.setPromptText(LanguageManagerAd.getString("manageSchedule.week.from"));
		endDatePicker.setPromptText(LanguageManagerAd.getString("manageSchedule.week.to"));
		btnShow.setText(LanguageManagerAd.getString("manageSchedule.week.view"));
		lblShift.setText(LanguageManagerAd.getString("manageSchedule.column.shift"));
		lblMon.setText(LanguageManagerAd.getString("manageSchedule.column.mon"));
		lblTue.setText(LanguageManagerAd.getString("manageSchedule.column.tue"));
		lblWed.setText(LanguageManagerAd.getString("manageSchedule.column.wed"));
		lblThus.setText(LanguageManagerAd.getString("manageSchedule.column.thu"));
		lblFri.setText(LanguageManagerAd.getString("manageSchedule.column.fri"));
		lblSat.setText(LanguageManagerAd.getString("manageSchedule.column.sat"));
		lblSun.setText(LanguageManagerAd.getString("manageSchedule.column.sun"));
		lblMorning.setText(LanguageManagerAd.getString("manageSchedule.shift.morning"));
		lblAfternoon.setText(LanguageManagerAd.getString("manageSchedule.shift.afternoon"));
		lblEvening.setText(LanguageManagerAd.getString("manageSchedule.shift.evening"));
		exportPdfButton.setText(LanguageManagerAd.getString("manageSchedule.pdf.export"));
		autoAssignButton.setText(LanguageManagerAd.getString("manageSchedule.auto.assign"));
		btnViewRequests.setText(LanguageManagerAd.getString("manageSchedule.view.requests"));
	}

}
