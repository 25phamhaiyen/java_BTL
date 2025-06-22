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
import utils.DatabaseConnection;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.LocalTime;

import enums.RequestStatus;
import enums.Shift;

public class ManageScheduleController implements Initializable {

	@FXML
	private DatePicker startDatePicker;

	@FXML
	private DatePicker endDatePicker;

	@FXML
	private GridPane scheduleGrid;

	@FXML
	private Button exportPdfButton;
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
			showAlert("Lỗi", "Vui lòng chọn khoảng thời gian hợp lệ.");
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
		System.out.println("Fetched schedules count: " + (fetchedSchedules != null ? fetchedSchedules.size() : 0));
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
			Label shiftLabel = new Label(shift.toString());
			shiftLabel.setStyle("-fx-font-weight: bold; -fx-alignment: center; -fx-text-alignment: center;");
			shiftLabel.setWrapText(true);
			scheduleGrid.add(shiftLabel, 0, row);
		}

		// ===== Tiêu đề cột (Thứ + Ngày) và dữ liệu từng ô =====
		for (int day = 0; day < 7; day++) {
			LocalDate currentDate = startDate.plusDays(day);
			int columnIndex = day + 1;

			// Tiêu đề cột: Thứ + Ngày
			String headerText = dayOfWeekFormatter.format(currentDate) + "\n" + dateFormatter.format(currentDate);
			Label dayLabel = new Label(headerText);
			dayLabel.setStyle("-fx-font-weight: bold; -fx-alignment: center; -fx-text-alignment: center;");
			dayLabel.setWrapText(true);
			scheduleGrid.add(dayLabel, columnIndex, 0);

			// Các ca trong ngày
			for (int row = 1; row <= 3; row++) {
				Shift shift = Shift.values()[row - 1];
				List<WorkSchedule> schedulesInShift = weeklySchedule.getOrDefault(currentDate, new HashMap<>())
						.getOrDefault(shift, new ArrayList<>());

				StackPane cellPane = createScheduleCell(currentDate, shift, schedulesInShift);
				scheduleGrid.add(cellPane, columnIndex, row);
			}
		}

		// Đánh dấu ô đã đủ nhân sự
		highlightFullRoles(startDate, endDate);
	}

	@FXML
	private void autoAssignShifts() {
		LocalDate selected = startDatePicker.getValue();

		LocalDate weekStart = selected.with(DayOfWeek.MONDAY);
		LocalDate thisWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);

		if (weekStart.isBefore(thisWeekStart)) {
			showAlert("Lỗi", "Không thể phân lịch cho tuần đã qua.");
			return;
		}
		if (scheduleService.isWeekScheduled(weekStart)) {
			showAlert("Lỗi", "Tuần này đã có lịch làm. Không thể phân tự động.");
			return;
		}
		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
				"Bạn có chắc muốn phân lịch tự động cho tuần bắt đầu từ " + weekStart + "?", ButtonType.YES,
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
		MenuItem editItem = new MenuItem("Sửa");
		MenuItem deleteItem = new MenuItem("Xóa");
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
				// Pass the whole schedules list.
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
			tooltip.setText("Role: " + schedule.getStaff().getRole().getRoleName() + "\nNote: "
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
		System.out.println("Phương thức handleEditSchedule được gọi cho ca: " + shift + " ngày: " + date);

		Stage dialogStage = new Stage();
		dialogStage.initModality(Modality.APPLICATION_MODAL);
		dialogStage.setTitle("Sửa thông tin nhân viên");

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
		noteField.setPromptText("Ghi chú");

		Button saveButton = new Button("Lưu");
		saveButton.setOnAction(event -> {
			WorkSchedule selectedSchedule = scheduleListView.getSelectionModel().getSelectedItem();
			if (selectedSchedule != null) {
				String newNote = noteField.getText();
				selectedSchedule.setNote(newNote);
				scheduleService.updateSchedule(selectedSchedule.getScheduleID(), shift.name(), newNote);
				loadScheduleForWeek();
				dialogStage.close();
			} else {
				showAlert("Lỗi", "Vui lòng chọn một nhân viên để sửa.");
			}
		});

		dialogVBox.getChildren().addAll(scheduleListView, noteField, saveButton);

		Scene dialogScene = new Scene(dialogVBox);
		dialogStage.setScene(dialogScene);
		dialogStage.showAndWait();
	}

	private void handleDeleteSchedule(LocalDate date, Shift shift, List<WorkSchedule> schedulesToDelete) {
		System.out.println("Phương thức handleDeleteSchedule được gọi cho ca: " + shift + " ngày: " + date);

		Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
		confirmationAlert.setTitle("Xác nhận xóa");
		confirmationAlert.setHeaderText("Bạn có chắc chắn muốn xóa ca làm của nhân viên?");
		confirmationAlert.setContentText("Hành động này không thể hoàn tác.");

		Optional<ButtonType> result = confirmationAlert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			// Show a dialog to select which schedule to delete
			Stage dialogStage = new Stage();
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.setTitle("Chọn nhân viên để xóa");

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

			Button deleteButton = new Button("Xóa");
			deleteButton.setOnAction(event -> {
				WorkSchedule selectedSchedule = scheduleListView.getSelectionModel().getSelectedItem();
				if (selectedSchedule != null) {
					weeklySchedule.get(date).get(shift).remove(selectedSchedule);
					scheduleService.deleteSchedule(selectedSchedule.getScheduleID());
					loadScheduleForWeek();
					dialogStage.close();
				} else {
					showAlert("Lỗi", "Vui lòng chọn một nhân viên để xóa.");
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
				showAlert("Thông báo", "Không thể thêm lịch trình vào ngày này.");
			} else if (date.isEqual(today)) {
				showAlert("Thông báo", "Không thể thêm lịch trình vào ngày hôm nay.");
			} else {
				showAlert("Thông báo", "Không có nhân viên nào rảnh vào ca này.");
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
			noteField.setPromptText("Ghi chú");

			Button addButton = new Button("Thêm");
			String employeeRole = employee.getRole().getRoleName();
			long currentCountForRole = currentRoleCounts.getOrDefault(employeeRole, 0L);
			int requiredForRole = requiredRoles.getOrDefault(employeeRole, 0);

			System.out.println("Checking role: " + employeeRole + ", currentCount: " + currentCountForRole
					+ ", required: " + requiredForRole);

			if (currentCountForRole >= requiredForRole) {
				addButton.setDisable(true);
				addButton.setText("Đã đủ");
			} else {
				// Disable adding to past dates
				if (date.isBefore(today)) {
					addButton.setDisable(true);
					addButton.setText("Hôm qua");
				} else if (date.isEqual(today)) {
					addButton.setDisable(true);
					addButton.setText("Hôm nay"); // Keep this to disable adding on the current day
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
			showAlert("Lỗi", "Vui lòng chọn khoảng thời gian hợp lệ để xuất lịch.");
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
					"Lịch trình tuần từ " + startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " đến "
							+ endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
					fontHeader);
			title.setAlignment(Paragraph.ALIGN_CENTER);
			document.add(title);
			document.add(new Paragraph(" ")); // Thêm khoảng trống

			// Tạo bảng với 8 cột (1 cột cho ca, 7 cột cho ngày)
			PdfPTable table = new PdfPTable(8);
			table.setWidthPercentage(100);
			table.setWidths(new float[] { 1, 2, 2, 2, 2, 2, 2, 2 }); // Tỷ lệ chiều rộng cột

			// Thêm tiêu đề bảng
			PdfPCell cell = new PdfPCell(new Phrase("Ca", fontHeader));
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

			showAlert("Thành công", "Đã xuất lịch trình tuần ra file PDF.");

			// Mở file PDF sau khi xuất thành công
			File pdfFile = new File(filePath);
			if (pdfFile.exists()) {
				Desktop.getDesktop().open(pdfFile);
			} else {
				showAlert("Lỗi", "Không tìm thấy file PDF vừa xuất.");
			}

		} catch (DocumentException | IOException e) {
			e.printStackTrace();
			showAlert("Lỗi", "Không thể xuất lịch trình ra file PDF.");
		}
	}
	
	@FXML
	private void handleViewRequests() {
	    Dialog<Void> dialog = new Dialog<>();
	    dialog.setTitle("Duyệt yêu cầu ca làm");
	    dialog.setHeaderText("Danh sách yêu cầu ca làm đang chờ duyệt");

	    ButtonType closeButtonType = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
	    dialog.getDialogPane().getButtonTypes().add(closeButtonType);

	    // Tạo bảng
	    TableView<ShiftRequest> table = new TableView<>();
	    table.setPrefHeight(300);
	    table.setPrefWidth(600);

	    TableColumn<ShiftRequest, String> colStaff = new TableColumn<>("Nhân viên");
	    colStaff.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStaff().getFullName()));

	    TableColumn<ShiftRequest, String> colDate = new TableColumn<>("Ngày");
	    colDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRequestDate().toString()));

	    TableColumn<ShiftRequest, String> colShift = new TableColumn<>("Ca");
	    colShift.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getShift().name()));

	    TableColumn<ShiftRequest, String> colType = new TableColumn<>("Loại");
	    colType.setCellValueFactory(cell -> new SimpleStringProperty(
	            switch (cell.getValue().getType()) {
	                case LEAVE -> "Xin nghỉ";
	                case WORK -> "Đăng ký";
	            }
	    ));

	    TableColumn<ShiftRequest, String> colReason = new TableColumn<>("Lý do");
	    colReason.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getReason()));

	    TableColumn<ShiftRequest, String> colStatus = new TableColumn<>("Trạng thái");
	    colStatus.setCellValueFactory(cell -> new SimpleStringProperty(
	            switch (cell.getValue().getStatus()) {
	                case PENDING -> "Đang chờ";
	                case APPROVED -> "Đã duyệt";
	                case REJECTED -> "Từ chối";
	            }
	    ));

	    table.getColumns().addAll(colStaff, colDate, colShift, colType, colReason, colStatus);

	    // Load data
	    ObservableList<ShiftRequest> requests = FXCollections.observableArrayList(scheduleService.getPendingRequests());
	    table.setItems(requests);

	    // Nút hành động
	    Button btnApprove = new Button("Duyệt");
	    btnApprove.setOnAction(e -> {
	        ShiftRequest selected = table.getSelectionModel().getSelectedItem();
	        if (selected != null) {
	            if (scheduleService.approveRequest(selected.getId(), RequestStatus.APPROVED)) {
	                showAlert("Thành công", "Yêu cầu đã được duyệt.");
	                table.getItems().remove(selected);
	            }
	        }
	    });

	    Button btnReject = new Button("Từ chối");
	    btnReject.setOnAction(e -> {
	        ShiftRequest selected = table.getSelectionModel().getSelectedItem();
	        if (selected != null) {
	            if (scheduleService.approveRequest(selected.getId(), RequestStatus.REJECTED)) {
	                showAlert("Đã từ chối", "Yêu cầu đã bị từ chối.");
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


}
