package controllers.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Staff;
import model.WorkSchedule;
import service.ScheduleService;
import service.StaffService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import enums.Shift;

public class ManageSchedule {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private GridPane scheduleGrid;
    @FXML private Button addButton, editButton, deleteButton;

    private final ScheduleService scheduleService = new ScheduleService();
    private final StaffService staffService = new StaffService();
    
    private List<WorkSchedule> allSchedules = new ArrayList<>();

    private final String[] shifts = {"MORNING", "AFTERNOON", "EVENING"};

    private Map<String, List<WorkSchedule>> scheduleData = new HashMap<>();

    @FXML
    public void initialize() {
        // Thiết lập ngày mặc định là 7 ngày kể từ hôm nay
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(today);
        endDatePicker.setValue(today.plusDays(6));
        loadSchedules();
        drawScheduleGrid(startDatePicker.getValue(), endDatePicker.getValue());

        // Thiết lập listener cho DatePicker để tự động cập nhật lịch
        startDatePicker.setOnAction(event -> handleShowSchedule());
        endDatePicker.setOnAction(event -> handleShowSchedule());
    }

    private void loadSchedules() {
        allSchedules = scheduleService.getAllSchedules();
    }

    private void drawScheduleGrid(LocalDate start, LocalDate end) {
        scheduleGrid.getChildren().clear();
        scheduleGrid.getColumnConstraints().clear(); // Clear existing column constraints

        // Định nghĩa kích thước cột
        scheduleGrid.getColumnConstraints().add(new ColumnConstraints(80)); // Cột thời gian
        long numDaysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        for (int i = 0; i < numDaysBetween; i++) {
            scheduleGrid.getColumnConstraints().add(new ColumnConstraints(150, 150, Double.MAX_VALUE)); // Cột ngày
        }

        // Tiêu đề các cột
        Label emptyLabel = new Label("");
        GridPane.setRowIndex(emptyLabel, 0);
        GridPane.setColumnIndex(emptyLabel, 0);
        scheduleGrid.getChildren().add(emptyLabel);

        LocalDate current = start;
        int col = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        String headerStyle = "-fx-font-weight: bold; -fx-text-fill: #333; -fx-alignment: center;";

        while (!current.isAfter(end)) {
            Label dayLabel = new Label(current.getDayOfWeek().toString() + "\n" + current.format(formatter));
            dayLabel.setStyle(headerStyle);
            GridPane.setRowIndex(dayLabel, 0);
            GridPane.setColumnIndex(dayLabel, col++);
            scheduleGrid.getChildren().add(dayLabel);
            current = current.plusDays(1);
        }

        // Tiêu đề các dòng (Sáng, Chiều, Tối)
        String shiftLabelStyle = "-fx-font-weight: bold; -fx-text-fill: #333;";
        for (int i = 0; i < shifts.length; i++) {
            Label shiftLabel = new Label(shifts[i]);
            shiftLabel.setStyle(shiftLabelStyle);
            GridPane.setRowIndex(shiftLabel, i + 1);
            GridPane.setColumnIndex(shiftLabel, 0);
            scheduleGrid.getChildren().add(shiftLabel);
        }

        scheduleData.clear();

        for (WorkSchedule ws : allSchedules) {
            if (!ws.getWorkDate().isBefore(start) && !ws.getWorkDate().isAfter(end)) {
                int column = ws.getWorkDate().getDayOfWeek().getValue(); // 1 (Monday) to 7 (Sunday)
                int row = switch (ws.getShift().toString()) {
                    case "MORNING" -> 1;
                    case "AFTERNOON" -> 2;
                    case "EVENING" -> 3;
                    default -> -1;
                };

                if (row != -1) {
                    String cellKey = column + "-" + row;
                    scheduleData.putIfAbsent(cellKey, new ArrayList<>());
                    scheduleData.get(cellKey).add(ws);
                }
            }
        }

        String cellBorderStyle = "-fx-border-color: #ddd; -fx-border-width: 0.5;";
        String cellBackgroundStyle = "-fx-background-color: #f9f9f9; -fx-padding: 2;";

        for (String cellKey : scheduleData.keySet()) {
            String[] parts = cellKey.split("-");
            int column = Integer.parseInt(parts[0]);
            int row = Integer.parseInt(parts[1]);

            VBox vbox = new VBox(2);
            vbox.setStyle(cellBackgroundStyle + cellBorderStyle);
            vbox.setPadding(new Insets(2));
            GridPane.setRowIndex(vbox, row);
            GridPane.setColumnIndex(vbox, column);
            scheduleGrid.getChildren().add(vbox);

            for (WorkSchedule ws : scheduleData.get(cellKey)) {
                HBox staffNoteBox = new HBox(5);
                staffNoteBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label staffLabel = new Label(ws.getStaff().getFullName());
                staffLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

                Label noteLabel = new Label("(" + ws.getNote() + ")");
                noteLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #777;");
                HBox.setHgrow(noteLabel, Priority.SOMETIMES);

                staffNoteBox.getChildren().addAll(staffLabel, noteLabel);
                vbox.getChildren().add(staffNoteBox);
            }
        }
    }

    @FXML
    private void handleShowSchedule() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start == null || end == null || end.isBefore(start)) {
            showAlert(AlertType.WARNING, "Khoảng thời gian không hợp lệ", "Vui lòng chọn ngày bắt đầu và kết thúc hợp lệ.");
            return;
        }

        if (java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1 != 7) {
            endDatePicker.setValue(start.plusDays(6));
            return; 
        }

        drawScheduleGrid(start, end);
    }

    @FXML
    private void handleAddSchedule() {
        Dialog<WorkSchedule> dialog = new Dialog<>();
        dialog.setTitle("Thêm lịch làm việc");
        dialog.setResizable(true);
        dialog.initOwner(addButton.getScene().getWindow());

        final int[] currentStep = {1};
        // Biến để lưu trữ ngày đã chọn
        final LocalDate[] selectedDate = new LocalDate[1];
        // Biến để lưu trữ nhân viên đã chọn
        final Staff[] selectedStaff = new Staff[1];
        // Biến để lưu trữ TableView nhân viên
        TableView<Staff> staffTableView = createStaffTableView();

        // Nội dung cho bước 1: Chọn ngày
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setEditable(false);
        datePicker.setPromptText("Chọn ngày làm việc");
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });
        VBox step1Content = new VBox(10, new Label("Chọn ngày làm việc:"), datePicker);
        step1Content.setPadding(new Insets(15));

        // Nút Tiếp tục
        ButtonType nextButtonType = new ButtonType("Tiếp tục", ButtonBar.ButtonData.NEXT_FORWARD);
        dialog.getDialogPane().getButtonTypes().addAll(nextButtonType, ButtonType.CANCEL);
        Button nextButton = (Button) dialog.getDialogPane().lookupButton(nextButtonType);
        nextButton.setDisable(true);
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            nextButton.setDisable(newValue == null);
        });

        // Xử lý khi bấm "Tiếp tục"
        nextButton.setOnAction(okEvent -> {
            VBox step1Content = new VBox(10, new Label("Chọn nhân viên:"), staffTableView);
            step1Content.setPadding(new Insets(15));
        
            // Bước 3: Chọn ca và ghi chú (tạo dialog mới hoặc thay đổi nội dung dialog hiện tại)
            Dialog<WorkSchedule> finalDialog = new Dialog<>();
            finalDialog.setTitle("Thêm lịch làm việc");
            finalDialog.setHeaderText("Chọn ca làm và ghi chú:");
            finalDialog.initOwner(dialog.getOwner());
        
            ComboBox<String> shiftChoiceBox = new ComboBox<>(FXCollections.observableArrayList(shifts));
            shiftChoiceBox.setPromptText("Chọn ca");
        
            TextField noteTextField = new TextField();
            noteTextField.setPromptText("Nhập ghi chú");
        
            VBox finalContent = new VBox(10, new Label("Chọn ca làm:"), shiftChoiceBox, new Label("Ghi chú:"), noteTextField);
            finalContent.setPadding(new Insets(15));
            finalDialog.getDialogPane().setContent(finalContent);
            finalDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
            finalDialog.setResultConverter(button -> {
                if (button == ButtonType.OK) {
                    String selectedShiftString = shiftChoiceBox.getValue();
                    String note = noteTextField.getText();
        
                    if (selectedShiftString == null || selectedShiftString.isEmpty()) {
                        showAlert(AlertType.WARNING, "Lỗi", "Vui lòng chọn ca làm.");
                        return null;
                    }
                    Shift shift;
                    try {
                        shift = Shift.valueOf(selectedShiftString.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        showAlert(AlertType.ERROR, "Lỗi", "Ca làm không hợp lệ. Vui lòng chọn lại.");
                        return null;
                    }
        
                    WorkSchedule newSchedule = new WorkSchedule();
                    newSchedule.setStaff(selectedStaff[0]);
                    newSchedule.setShift(shift);
                    newSchedule.setNote(note);
                    newSchedule.setWorkDate(selectedDate[0]); // Ngày đã chọn từ bước trước
                    return newSchedule;
                }
                return null;
            });
        
            Optional<WorkSchedule> result = finalDialog.showAndWait();
            result.ifPresent(workSchedule -> {
                // Thêm lịch làm việc mới vào danh sách
                allSchedules.add(workSchedule);
        
                // Cập nhật lại GridPane
                drawScheduleGrid(startDatePicker.getValue(), endDatePicker.getValue());
            });
        });
        
        dialog.getDialogPane().setContent(step1Content);
        dialog.showAndWait();
    }

    private TableView<Staff> createStaffTableView() {
        TableView<Staff> staffTableView = new TableView<>();

        // Tạo cột tên nhân viên
        TableColumn<Staff, String> staffNameColumn = new TableColumn<>("Tên");
        staffNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        // Tạo cột vai trò
        TableColumn<Staff, String> staffRoleColumn = new TableColumn<>("Vai trò");
        staffRoleColumn.setCellValueFactory(new PropertyValueFactory<>("roleName"));

        // Thêm các cột vào bảng
        staffTableView.getColumns().addAll(staffNameColumn, staffRoleColumn);

        // Gắn dữ liệu vào bảng
        ObservableList<Staff> staffData = FXCollections.observableArrayList(staffService.getAllStaffs());
        staffTableView.setItems(staffData);

        return staffTableView;
    }
    
    @FXML
    private void handleEditSchedule() {
        showAlert(AlertType.INFORMATION, "Sửa lịch", "Chức năng đang phát triển.");
    }

    @FXML
    private void handleDeleteSchedule() {
        showAlert(AlertType.INFORMATION, "Xóa lịch", "Chức năng đang phát triển.");
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}