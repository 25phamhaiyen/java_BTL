package controllers.admin;

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
import model.Staff;
import model.WorkSchedule;
import service.ScheduleService;
import service.StaffService;

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

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;

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

        String filePath = "weekly_schedule_itext9_" +
                startDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")) + "_" +
                endDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".pdf";
        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument, PageSize.A4.rotate());
            document.setMargins(20, 20, 20, 20);

         // Sử dụng font hỗ trợ tiếng Việt (ví dụ: Arial Unicode MS)
            PdfFont fontHeader = PdfFontFactory.createFont("C:\\Windows\\Fonts\\Arial.ttf"); // Đường dẫn tới file font
            PdfFont fontContent = PdfFontFactory.createFont("C:\\Windows\\Fonts\\Arial.ttf");

            Paragraph title = new Paragraph("Lịch trình tuần từ " +
                    startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " đến " +
                    endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFont(fontHeader)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 2, 2, 2, 2, 2, 2}));
            table.setWidth(UnitValue.createPercentValue(100f));

            // Thêm header của bảng
            table.addHeaderCell(createCell("Ca", fontHeader));
            DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd/MM\nEEE");
            for (int i = 0; i < 7; i++) {
                LocalDate currentDate = startDate.plusDays(i);
                table.addHeaderCell(createCell(currentDate.format(dayFormatter), fontHeader));
            }

            // Thêm dữ liệu lịch trình
            for (Shift shift : Shift.values()) {
                if (shift != Shift.NOSHIFT) { 
                    table.addCell(createCell(shift.name(), fontContent));
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
                        table.addCell(createCell(employeeNames.toString().trim(), fontContent));
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

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể xuất lịch trình ra file PDF.");
        }
    }

    // Helper method để tạo ô trong bảng PDF
    private Cell createCell(String content, PdfFont font) {
        return new Cell()
                .add(new Paragraph(content).setFont(font))
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
    }




}
