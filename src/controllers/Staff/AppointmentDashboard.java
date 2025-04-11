
package controllers.Staff;

import javafx.application.Platform;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Appointment;
import utils.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AppointmentDashboard {
    private BorderPane root;
    private Stage primaryStage;
    private String loggedInUsername;
    private DatePicker datePicker;
    private TableView<Appointment> appointmentTable;
    private VBox leftPanel;
    private LocalDate currentDisplayDate = LocalDate.now();
    public AppointmentDashboard(Stage stage, String loggedInUsername) {
        this.primaryStage = stage;
        this.loggedInUsername = loggedInUsername;
        root = new BorderPane();
        root.getStyleClass().addAll("root", "dashboard-root");
        root.setPadding(new Insets(10));

        // Header
        HBox header = createHeader();
        root.setTop(header);

        // Left panel - Controls
        VBox leftPanel = createLeftPanel();
        root.setLeft(leftPanel);

        // Center - Appointment table
        appointmentTable = createAppointmentTable();
        root.setCenter(appointmentTable);
          
        checkAndResetDisplay();
        // Load today's appointments by default
        loadAppointments(LocalDate.now());

        // Apply CSS
        try {
            root.getStylesheets().add(getClass().getResource("/view/Staff/Staff.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Lỗi khi tải CSS: " + e.getMessage());
        }
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");

        Label cashierInfo = new Label(getCashierInfo());
        cashierInfo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        Button backButton = new Button("Quay lại");
        backButton.getStyleClass().addAll("button", "cancel-button");
        backButton.setOnAction(e -> primaryStage.getScene().setRoot(new CashierDashboard(primaryStage, loggedInUsername).getRoot()));

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(cashierInfo, spacer, backButton);

        return header;
    }

    private VBox createLeftPanel() {
        leftPanel = new VBox(20); // Sử dụng biến thành viên thay vì biến cục bộ
        leftPanel.setPadding(new Insets(20));
        leftPanel.setMinWidth(250);
        leftPanel.setStyle("-fx-background-color: #ecf0f1;");

        // Date selection
        Label dateLabel = new Label("Chọn ngày:");
        datePicker = new DatePicker(LocalDate.now());
        datePicker.getStyleClass().add("date-picker");
        datePicker.setOnAction(e -> loadAppointments(datePicker.getValue()));

        // Quick actions
        Button todayButton = new Button("Hôm nay");
        todayButton.getStyleClass().addAll("button", "primary-button");
        todayButton.setMaxWidth(Double.MAX_VALUE);
        todayButton.setOnAction(e -> {
            datePicker.setValue(LocalDate.now());
            loadAppointments(LocalDate.now());
        });

        Button addAppointmentButton = new Button("Thêm lịch hẹn");
        addAppointmentButton.getStyleClass().addAll("button", "register-button");
        addAppointmentButton.setMaxWidth(Double.MAX_VALUE);
        addAppointmentButton.setOnAction(e -> {
            QuickBookingDialog dialog = new QuickBookingDialog(() -> loadAppointments(datePicker.getValue()));
            dialog.show();
        });

        Button refreshButton = new Button("Làm mới");
        refreshButton.getStyleClass().addAll("button", "login-button");
        refreshButton.setMaxWidth(Double.MAX_VALUE);
        refreshButton.setOnAction(e -> loadAppointments(datePicker.getValue()));

        // Status filter
        Label filterLabel = new Label("Lọc theo trạng thái:");
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tất cả", "Đã xác nhận", "Đang chờ", "Đã hoàn thành", "Đã hủy");
        statusFilter.setValue("Tất cả");
        statusFilter.getStyleClass().add("combo-box");
        statusFilter.setOnAction(e -> filterAppointments(statusFilter.getValue()));

        leftPanel.getChildren().addAll(
            dateLabel, datePicker, todayButton,
            new Separator(), filterLabel, statusFilter,
            new Separator(), addAppointmentButton, refreshButton
        );

        return leftPanel;
    }
    private void cancelAppointment(Appointment appointment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận hủy lịch hẹn");
        alert.setHeaderText("Bạn có chắc chắn muốn hủy lịch hẹn này?");
        alert.setContentText("Lịch hẹn #" + appointment.getAppointmentId());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (updateAppointmentStatus(appointment.getAppointmentId(), "Đã hủy")) {
                    // Không cần tải lại ở đây vì đã được gọi trong updateAppointmentStatus nếu thành công
                    showAlert("Thành công", "Đã hủy lịch hẹn thành công!");
                }
            }
        });
    }

    private boolean updateAppointmentStatus(String appointmentId, String statusName) {
        String sql = "UPDATE `order` o " +
                     "JOIN happenstatus hs ON hs.StatusName = ? " +
                     "SET o.HappenStatusID = hs.HappenStatusID " +
                     "WHERE o.orderID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, statusName);
            pstmt.setInt(2, Integer.parseInt(appointmentId));
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Đảm bảo tải lại dữ liệu sau khi cập nhật trạng thái
                Platform.runLater(() -> loadAppointments(datePicker.getValue()));
                return true;
            } else {
                showAlert("Lỗi", "Không thể cập nhật trạng thái lịch hẹn");
                return false;
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi cập nhật trạng thái lịch hẹn: " + ex.getMessage());
            showAlert("Lỗi", "Không thể cập nhật trạng thái lịch hẹn: " + ex.getMessage());
            return false;
        }
    }
    private TableView<Appointment> createAppointmentTable() {
        TableView<Appointment> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Columns
        TableColumn<Appointment, String> idCol = new TableColumn<>("Mã");
        idCol.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        idCol.setStyle("-fx-alignment: CENTER;");
        idCol.setPrefWidth(70);

        TableColumn<Appointment, String> customerCol = new TableColumn<>("Khách hàng");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerCol.setStyle("-fx-alignment: CENTER;");
        customerCol.setPrefWidth(150);

        TableColumn<Appointment, String> petCol = new TableColumn<>("Thú cưng");
        petCol.setCellValueFactory(new PropertyValueFactory<>("petName"));
        petCol.setStyle("-fx-alignment: CENTER;");
        petCol.setPrefWidth(120);

        TableColumn<Appointment, String> serviceCol = new TableColumn<>("Dịch vụ");
        serviceCol.setCellValueFactory(new PropertyValueFactory<>("service"));
        serviceCol.setStyle("-fx-alignment: CENTER-LEFT;");
        serviceCol.setPrefWidth(200);

        TableColumn<Appointment, String> timeCol = new TableColumn<>("Thời gian");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeCol.setStyle("-fx-alignment: CENTER;");
        timeCol.setPrefWidth(120);

        TableColumn<Appointment, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setStyle("-fx-alignment: CENTER;");
        statusCol.setPrefWidth(120);

        // Action column
        TableColumn<Appointment, Void> actionCol = new TableColumn<>("Thao tác");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Sửa");
            private final Button cancelBtn = new Button("Hủy");
            private final HBox pane = new HBox(5, editBtn, cancelBtn);

            {
                editBtn.getStyleClass().addAll("button", "primary-button");
                cancelBtn.getStyleClass().addAll("button", "cancel-button");
                pane.setAlignment(Pos.CENTER);
                
                editBtn.setOnAction(event -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    editAppointment(appointment);
                });
                
                cancelBtn.setOnAction(event -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    cancelAppointment(appointment);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
        actionCol.setPrefWidth(150);

        table.getColumns().addAll(idCol, customerCol, petCol, serviceCol, timeCol, statusCol, actionCol);
        return table;
    }

    private void loadAppointments(LocalDate date) {
        ObservableList<Appointment> appointments = FXCollections.observableArrayList();
        String sql = "SELECT o.orderID, p.firstName, p.lastName, " +
                     "pet.PetName, tp.UN_TypeName as PetType, " +
                     "GROUP_CONCAT(s.serviceName SEPARATOR ', ') AS services, " +
                     "o.appointmentDate, o.orderDate, hs.StatusName " +
                     "FROM `order` o " +
                     "JOIN customer c ON o.Customer_ID = c.PersonID " +
                     "JOIN person p ON c.PersonID = p.PersonID " +
                     "LEFT JOIN pet ON (o.PetID = pet.PetID OR (pet.Customer_ID = c.PersonID AND pet.PetID = (SELECT MIN(PetID) FROM pet WHERE Customer_ID = c.PersonID))) " +
                     "LEFT JOIN typepet tp ON pet.TypePetID = tp.TypePetID " +
                     "JOIN order_detail od ON o.orderID = od.OrderID " +
                     "JOIN service s ON od.ServiceID = s.serviceID " +
                     "JOIN happenstatus hs ON o.HappenStatusID = hs.HappenStatusID " +
                     "WHERE DATE(o.appointmentDate) = ? AND o.orderType = 'Appointment' " +
                     "GROUP BY o.orderID";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();
            
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
            
            while (rs.next()) {
                String appointmentId = String.valueOf(rs.getInt("orderID"));
                String customerName = rs.getString("lastName") + " " + rs.getString("firstName");
                
                // Xử lý thông tin thú cưng
                String petName = rs.getString("PetName");
                String petType = rs.getString("PetType");
                String petInfo;
                if (petName != null) {
                    petInfo = petName;
                    if (petType != null) {
                        petInfo += " (" + petType + ")";
                    }
                } else {
                    petInfo = "N/A";
                }
                
                String services = rs.getString("services");
                
                // Xử lý thời gian (ưu tiên ngày hẹn, nếu không có thì dùng ngày tạo)
                Timestamp appointmentTimestamp = rs.getTimestamp("appointmentDate");
                Timestamp orderTimestamp = rs.getTimestamp("orderDate");
                
                String time;
                if (appointmentTimestamp != null) {
                    LocalDateTime appointmentDateTime = appointmentTimestamp.toLocalDateTime();
                    time = appointmentDateTime.format(timeFormatter);
                } else if (orderTimestamp != null) {
                    LocalDateTime orderDateTime = orderTimestamp.toLocalDateTime();
                    time = orderDateTime.format(timeFormatter);
                } else {
                    time = "N/A";
                }
                
                String status = rs.getString("StatusName");
                
                appointments.add(new Appointment(
                    appointmentId, 
                    customerName, 
                    petInfo, 
                    services, 
                    time, 
                    status
                ));
            }
            
            appointmentTable.setItems(appointments);
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tải lịch hẹn: " + ex.getMessage());
            showAlert("Lỗi", "Không thể tải lịch hẹn: " + ex.getMessage());
        }
    }
    private void filterAppointments(String statusFilter) {
        if (statusFilter.equals("Tất cả")) {
            // Không cần lọc, lấy tất cả từ bảng
            loadAppointments(datePicker.getValue());
            return;
        }
        
        // Lọc từ danh sách đã tải
        ObservableList<Appointment> allAppointments = appointmentTable.getItems();
        ObservableList<Appointment> filtered = FXCollections.observableArrayList();
        
        for (Appointment appt : allAppointments) {
            if (appt.getStatus().equals(statusFilter)) {
                filtered.add(appt);
            }
        }
        
        appointmentTable.setItems(filtered);
    }
 // Cải thiện phương thức editAppointment để đảm bảo refresh UI đúng cách
    private void editAppointment(Appointment appointment) {
        // Tạo dialog để sửa lịch hẹn
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Sửa lịch hẹn");
        dialog.setHeaderText("Sửa thông tin lịch hẹn #" + appointment.getAppointmentId());
        
        // Các button của dialog
        ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Tạo layout cho dialog
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Tạo DatePicker cho ngày hẹn
        DatePicker appointmentDatePicker = new DatePicker();
        
        // Tạo ComboBox cho giờ hẹn
        ComboBox<String> timeCombo = new ComboBox<>();
        timeCombo.getItems().addAll(
            "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
            "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"
        );
        
        // Tạo ComboBox cho trạng thái
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Đang chờ", "Đang xử lý", "Đã hoàn thành", "Đã hủy");
        statusCombo.setValue(appointment.getStatus());
        
        // Lấy thông tin chi tiết từ đơn hàng
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT appointmentDate FROM `order` WHERE orderID = ?")) {
            
            pstmt.setInt(1, Integer.parseInt(appointment.getAppointmentId()));
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Timestamp appointmentTimestamp = rs.getTimestamp("appointmentDate");
                if (appointmentTimestamp != null) {
                    LocalDateTime appointmentDateTime = appointmentTimestamp.toLocalDateTime();
                    appointmentDatePicker.setValue(appointmentDateTime.toLocalDate());
                    timeCombo.setValue(appointmentDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                } else {
                    appointmentDatePicker.setValue(LocalDate.now());
                    timeCombo.setValue("08:00");
                }
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tải thông tin lịch hẹn: " + ex.getMessage());
            appointmentDatePicker.setValue(LocalDate.now());
            timeCombo.setValue("08:00");
        }
        
        // Thêm các control vào grid
        grid.add(new Label("Ngày hẹn:"), 0, 0);
        grid.add(appointmentDatePicker, 1, 0);
        grid.add(new Label("Giờ hẹn:"), 0, 1);
        grid.add(timeCombo, 1, 1);
        grid.add(new Label("Trạng thái:"), 0, 2);
        grid.add(statusCombo, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Xử lý khi người dùng nhấn nút Lưu
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (appointmentDatePicker.getValue() == null || timeCombo.getValue() == null || statusCombo.getValue() == null) {
                    showAlert("Lỗi", "Vui lòng điền đầy đủ thông tin!");
                    return null;
                }
                
                // Cập nhật thông tin lịch hẹn trong cơ sở dữ liệu
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    
                    // 1. Cập nhật ngày giờ hẹn
                    String updateDateSql = "UPDATE `order` SET appointmentDate = ? WHERE orderID = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(updateDateSql)) {
                        // Tạo LocalDateTime từ date và time
                        LocalDateTime appointmentDateTime = LocalDateTime.of(
                            appointmentDatePicker.getValue(),
                            LocalTime.parse(timeCombo.getValue())
                        );
                        
                        pstmt.setTimestamp(1, Timestamp.valueOf(appointmentDateTime));
                        pstmt.setInt(2, Integer.parseInt(appointment.getAppointmentId()));
                        pstmt.executeUpdate();
                    }
                    
                    // 2. Cập nhật trạng thái
                    String updateStatusSql = "UPDATE `order` o " +
                                           "JOIN happenstatus hs ON hs.StatusName = ? " +
                                           "SET o.HappenStatusID = hs.HappenStatusID " +
                                           "WHERE o.orderID = ?";
                    
                    try (PreparedStatement pstmt = conn.prepareStatement(updateStatusSql)) {
                        pstmt.setString(1, statusCombo.getValue());
                        pstmt.setInt(2, Integer.parseInt(appointment.getAppointmentId()));
                        pstmt.executeUpdate();
                    }
                    
                    conn.commit();
                    showAlert("Thành công", "Đã cập nhật lịch hẹn thành công!");
                    
                    // Sử dụng Platform.runLater để đảm bảo UI được cập nhật sau khi dialog đóng
                    Platform.runLater(() -> {
                        // Lưu giá trị DatePicker hiện tại
                        LocalDate currentDate = datePicker.getValue();
                        // Tải lại dữ liệu với ngày đã chọn
                        loadAppointments(currentDate);
                    });
                    
                    return ButtonType.OK;
                } catch (SQLException ex) {
                    System.err.println("Lỗi khi cập nhật lịch hẹn: " + ex.getMessage());
                    showAlert("Lỗi", "Không thể cập nhật lịch hẹn: " + ex.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    // Cập nhật phương thức updateAppointmentStatus để nó luôn tải lại dữ liệu
  
    private String getCashierInfo() {
        String sql = "SELECT p.lastName, p.firstName, s.PersonID " +
                     "FROM staff s " +
                     "JOIN person p ON s.PersonID = p.PersonID " +
                     "JOIN account a ON s.AccountID = a.AccountID " +
                     "WHERE a.UN_Username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, loggedInUsername);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String fullName = rs.getString("lastName") + " " + rs.getString("firstName");
                String personId = String.valueOf(rs.getInt("PersonID"));
                return "Nhân viên: " + fullName + " (ID: " + personId + ")";
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi lấy thông tin nhân viên: " + ex.getMessage());
        }
        return "Nhân viên: Không xác định (ID: N/A)";
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public BorderPane getRoot() {
        return root;
    }
    private void refreshAppointments() {
        // Tìm ComboBox trong các thành phần con của leftPanel
        ComboBox<String> statusFilter = null;
        
        // Tìm ComboBox loại trạng thái trong leftPanel
        // Thường nó nằm ở vị trí index 5, nhưng tốt hơn là tìm kiếm theo loại
        for (int i = 0; i < leftPanel.getChildren().size(); i++) {
            if (leftPanel.getChildren().get(i) instanceof ComboBox) {
                statusFilter = (ComboBox<String>) leftPanel.getChildren().get(i);
                break;
            }
        }
        
        // Nếu không tìm thấy ComboBox, có thể sử dụng chỉ số cố định (nếu biết chắc vị trí)
        if (statusFilter == null && leftPanel.getChildren().size() > 5) {
            try {
                statusFilter = (ComboBox<String>) leftPanel.getChildren().get(5);
            } catch (ClassCastException e) {
                System.err.println("Không thể tìm thấy ComboBox ở vị trí 5: " + e.getMessage());
            }
        }
        
        // Lưu lại giá trị lọc hiện tại từ ComboBox (nếu tìm thấy)
        String currentFilter = statusFilter != null ? statusFilter.getValue() : "Tất cả";
        
        // Tải lại dữ liệu
        loadAppointments(datePicker.getValue());
        
        // Áp dụng lại bộ lọc nếu không phải là "Tất cả"
        if (currentFilter != null && !currentFilter.equals("Tất cả")) {
            filterAppointments(currentFilter);
        }
    }
    // Hàm tiện ích để gỡ lỗi - có thể thêm vào khi cần kiểm tra dữ liệu
    private void debugHappenStatus() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM happenstatus")) {
            
            System.out.println("Danh sách trạng thái hiện có:");
            while (rs.next()) {
                System.out.println(rs.getInt("HappenStatusID") + " - " + 
                                   rs.getString("UN_StatusCode") + " - " + 
                                   rs.getString("StatusName"));
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi kiểm tra bảng happenstatus: " + ex.getMessage());
        }
    }
    private void checkAndResetDisplay() {
        LocalDate today = LocalDate.now();
        if (!today.equals(currentDisplayDate)) {
            // Reset UI và load dữ liệu mới
            currentDisplayDate = today;
            datePicker.setValue(today);
            loadAppointments(today);
        }
    }
    
}