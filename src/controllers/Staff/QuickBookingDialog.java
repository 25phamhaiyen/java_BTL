package controllers.Staff;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class QuickBookingDialog extends Stage {
    private final VBox root;
    private final ComboBox<String> customerCombo;
    private final TextField phoneField;
    private final DatePicker datePicker;
    private final ComboBox<String> timeCombo;
    private final ListView<ServiceItem> serviceListView;
    private final Label totalLabel;
    private final Runnable successCallback;
    
    private String selectedCustomerId;
    private String selectedCustomerName;
    private Map<String, String> customerMap = new HashMap<>();
    private double total = 0.0;

    // Constructor for when we don't have customer information yet
    public QuickBookingDialog(Runnable successCallback) {
        this.successCallback = successCallback;
        this.selectedCustomerId = null;
        this.selectedCustomerName = null;
        
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Đặt lịch hẹn nhanh");
        setMinWidth(600);
        setMinHeight(700);
        
        root = new VBox(15);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root");
        root.getStyleClass().add("dialog-container");
        
        Label titleLabel = new Label("Đặt lịch hẹn");
        titleLabel.getStyleClass().add("title-label");
        
        // Customer selection section
        Label customerLabel = new Label("Thông tin khách hàng");
        customerLabel.getStyleClass().add("section-label");
        
        GridPane customerGrid = new GridPane();
        customerGrid.setHgap(10);
        customerGrid.setVgap(10);
        
        Label customerNameLabel = new Label("Tên khách hàng:");
        customerCombo = new ComboBox<>();
        customerCombo.setPromptText("Chọn khách hàng");
        customerCombo.setEditable(true);
        customerCombo.setPrefWidth(300);
        customerCombo.setOnAction(e -> {
            if (customerCombo.getValue() != null) {
                String selectedCustomer = customerCombo.getValue();
                selectedCustomerId = customerMap.get(selectedCustomer);
                selectedCustomerName = selectedCustomer;
                if (selectedCustomerId != null) {
                    setCustomerById(selectedCustomerId);
                }
            }
        });
        
        Label phoneLabel = new Label("Số điện thoại:");
        phoneField = new TextField();
        phoneField.setPromptText("Nhập số điện thoại");
        
        Button searchButton = new Button("Tìm");
        searchButton.getStyleClass().addAll("button", "primary-button");
        searchButton.setOnAction(e -> searchCustomerByPhone());
        
        Button registerButton = new Button("Đăng ký KH mới");
        registerButton.getStyleClass().addAll("button", "register-button");
        registerButton.setOnAction(e -> {
            if (phoneField.getText().trim().isEmpty()) {
                showAlert("Lỗi", "Vui lòng nhập số điện thoại trước khi đăng ký!");
                return;
            }
            
            String phone = phoneField.getText().trim();
            if (phone.length() != 10 || !phone.matches("\\d+")) {
                showAlert("Lỗi", "Số điện thoại phải có 10 chữ số!");
                return;
            }
            
            // Check if phone already exists
            if (isPhoneNumberExist(phone)) {
                showAlert("Thông báo", "Số điện thoại này đã được đăng ký. Đã tìm kiếm khách hàng.");
                searchCustomerByPhone();
                return;
            }
            
            // Gọi phương thức đăng ký
            registerNewCustomer(phone);
        });
        
        customerGrid.add(customerNameLabel, 0, 0);
        customerGrid.add(customerCombo, 1, 0, 2, 1);
        customerGrid.add(phoneLabel, 0, 1);
        customerGrid.add(phoneField, 1, 1);
        customerGrid.add(searchButton, 2, 1);
        customerGrid.add(registerButton, 1, 2);
        
        // Appointment section
        Label appointmentLabel = new Label("Thông tin lịch hẹn");
        appointmentLabel.getStyleClass().add("section-label");
        
        GridPane appointmentGrid = new GridPane();
        appointmentGrid.setHgap(10);
        appointmentGrid.setVgap(10);
        
        Label dateLabel = new Label("Ngày hẹn:");
        datePicker = new DatePicker(LocalDate.now());
        
        Label timeLabel = new Label("Giờ hẹn:");
        timeCombo = new ComboBox<>();
        timeCombo.getItems().addAll(
            "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
            "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"
        );
        timeCombo.setValue("08:00");
        
        appointmentGrid.add(dateLabel, 0, 0);
        appointmentGrid.add(datePicker, 1, 0);
        appointmentGrid.add(timeLabel, 0, 1);
        appointmentGrid.add(timeCombo, 1, 1);
        
        // Services section
        Label servicesLabel = new Label("Dịch vụ");
        servicesLabel.getStyleClass().add("section-label");
        
        serviceListView = new ListView<>();
        serviceListView.setPrefHeight(200);
        serviceListView.setCellFactory(param -> new ServiceCell());
        
        HBox serviceHeader = new HBox(10);
        serviceHeader.setAlignment(Pos.CENTER_LEFT);
        
        Button addServiceButton = new Button("Thêm dịch vụ");
        addServiceButton.getStyleClass().addAll("button", "primary-button");
        addServiceButton.setOnAction(e -> addService());
        
        serviceHeader.getChildren().addAll(servicesLabel, addServiceButton);
        
        // Summary
        HBox totalBox = new HBox(10);
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        
        Label totalTextLabel = new Label("Tổng cộng:");
        totalTextLabel.getStyleClass().add("section-label");
        
        totalLabel = new Label("0 đ");
        totalLabel.getStyleClass().add("total-label");
        
        totalBox.getChildren().addAll(totalTextLabel, totalLabel);
        
        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button saveButton = new Button("Đặt lịch");
        saveButton.getStyleClass().addAll("button", "login-button");
        saveButton.setOnAction(e -> saveAppointment());
        
        Button cancelButton = new Button("Hủy");
        cancelButton.getStyleClass().addAll("button", "cancel-button");
        cancelButton.setOnAction(e -> close());
        
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        
        // Add all components to root
        root.getChildren().addAll(
            titleLabel,
            customerLabel, customerGrid,
            appointmentLabel, appointmentGrid,
            serviceHeader, serviceListView,
            totalBox,
            buttonBox
        );
        
        // Load customers
        loadCustomerInfo();
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/view/Staff/Staff.css").toExternalForm());
        setScene(scene);
    }
    
    private void registerNewCustomer(String phone) {
        // Tạo một mảng để lưu trữ tham chiếu đến đối tượng CustomerRegistrationScreen
        final CustomerRegistrationScreen[] screenHolder = new CustomerRegistrationScreen[1];
        
        // Tạo đối tượng CustomerRegistrationScreen với callback
        screenHolder[0] = new CustomerRegistrationScreen(phone, () -> {
            // Lấy ID khách hàng mới
            int newCustomerId = screenHolder[0].getNewCustomerId();
            
            // Kiểm tra và xử lý với ID đã lấy được
            if (newCustomerId > 0) {
                selectedCustomerId = String.valueOf(newCustomerId);
                loadCustomerInfo();
                setCustomerById(selectedCustomerId);
            }
        });
        
        // Hiển thị màn hình đăng ký
        screenHolder[0].showAndWait();
    }
    // Constructor for when we already have customer information
    public QuickBookingDialog(String customerId, String customerName, Runnable successCallback) {
        this(successCallback);
        this.selectedCustomerId = customerId;
        this.selectedCustomerName = customerName;
        
        // Set customer info
        if (customerId != null && !customerId.isEmpty()) {
            setCustomerById(customerId);
        }
    }
    
    private void loadCustomerInfo() {
        String sql = "SELECT c.PersonID, CONCAT(p.lastName, ' ', p.firstName, ' (', p.phoneNumber, ')') as FullName " +
                     "FROM customer c " +
                     "JOIN person p ON c.PersonID = p.PersonID " +
                     "ORDER BY p.lastName, p.firstName";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            ObservableList<String> customerItems = FXCollections.observableArrayList();
            customerMap.clear();
            
            while (rs.next()) {
                String id = rs.getString("PersonID");
                String fullName = rs.getString("FullName");
                customerItems.add(fullName);
                customerMap.put(fullName, id);
            }
            
            customerCombo.setItems(customerItems);
            
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tải danh sách khách hàng: " + ex.getMessage());
            showAlert("Lỗi", "Không thể tải danh sách khách hàng: " + ex.getMessage());
        }
    }
    
    private void searchCustomerByPhone() {
        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập số điện thoại để tìm kiếm!");
            return;
        }
        
        String sql = "SELECT c.PersonID, CONCAT(p.lastName, ' ', p.firstName, ' (', p.phoneNumber, ')') as FullName " +
                     "FROM customer c " +
                     "JOIN person p ON c.PersonID = p.PersonID " +
                     "WHERE p.phoneNumber LIKE ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + phone + "%");
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String id = rs.getString("PersonID");
                String fullName = rs.getString("FullName");
                
                selectedCustomerId = id;
                selectedCustomerName = fullName;
                customerCombo.setValue(fullName);
            } else {
                showAlert("Thông báo", "Không tìm thấy khách hàng với số điện thoại này. Vui lòng đăng ký khách hàng mới.");
                customerCombo.setValue(null);
                selectedCustomerId = null;
                selectedCustomerName = null;
            }
            
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tìm kiếm khách hàng: " + ex.getMessage());
            showAlert("Lỗi", "Không thể tìm kiếm khách hàng: " + ex.getMessage());
        }
    }
    
    private void setCustomerById(String customerId) {
        if (customerId == null || customerId.isEmpty()) {
            return;
        }
        
        String sql = "SELECT c.PersonID, p.phoneNumber, CONCAT(p.lastName, ' ', p.firstName, ' (', p.phoneNumber, ')') as FullName " +
                     "FROM customer c " +
                     "JOIN person p ON c.PersonID = p.PersonID " +
                     "WHERE c.PersonID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, Integer.parseInt(customerId));
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String fullName = rs.getString("FullName");
                String phone = rs.getString("phoneNumber");
                
                customerCombo.setValue(fullName);
                phoneField.setText(phone);
                selectedCustomerName = fullName;
            }
            
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tải thông tin khách hàng: " + ex.getMessage());
            showAlert("Lỗi", "Không thể tải thông tin khách hàng: " + ex.getMessage());
        } catch (NumberFormatException ex) {
            System.err.println("ID khách hàng không hợp lệ: " + customerId);
            showAlert("Lỗi", "ID khách hàng không hợp lệ: " + customerId);
        }
    }
    
    private void addService() {
        Dialog<ServiceItem> dialog = new Dialog<>();
        dialog.setTitle("Thêm dịch vụ");
        dialog.setHeaderText("Chọn dịch vụ");
        
        ButtonType addButtonType = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        ComboBox<ServiceData> serviceCombo = new ComboBox<>();
        TextField quantityField = new TextField("1");
        quantityField.setPrefWidth(80);
        
        grid.add(new Label("Dịch vụ:"), 0, 0);
        grid.add(serviceCombo, 1, 0);
        grid.add(new Label("Số lượng:"), 0, 1);
        grid.add(quantityField, 1, 1);
        
        // Load services
        loadServices(serviceCombo);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                ServiceData selected = serviceCombo.getValue();
                if (selected == null) {
                    showAlert("Lỗi", "Vui lòng chọn dịch vụ!");
                    return null;
                }
                
                int quantity;
                try {
                    quantity = Integer.parseInt(quantityField.getText());
                    if (quantity <= 0) {
                        showAlert("Lỗi", "Số lượng phải lớn hơn 0!");
                        return null;
                    }
                } catch (NumberFormatException e) {
                    showAlert("Lỗi", "Số lượng phải là số!");
                    return null;
                }
                
                return new ServiceItem(
                    selected.getId(),
                    selected.getName(),
                    selected.getPrice(),
                    quantity
                );
            }
            return null;
        });
        
        Optional<ServiceItem> result = dialog.showAndWait();
        
        result.ifPresent(serviceItem -> {
            ObservableList<ServiceItem> items = serviceListView.getItems();
            if (items == null) {
                items = FXCollections.observableArrayList();
                serviceListView.setItems(items);
            }
            
            // Check if service already exists, update quantity if it does
            boolean found = false;
            for (int i = 0; i < items.size(); i++) {
                ServiceItem item = items.get(i);
                if (item.getId().equals(serviceItem.getId())) {
                    item.setQuantity(item.getQuantity() + serviceItem.getQuantity());
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                items.add(serviceItem);
            }
            
            // Refresh list and update total
            serviceListView.refresh();
            updateTotal();
        });
    }
    
    private void loadServices(ComboBox<ServiceData> serviceCombo) {
    	String sql = "SELECT serviceID, serviceName, CostPrice as servicePrice FROM service ORDER BY serviceName";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            ObservableList<ServiceData> services = FXCollections.observableArrayList();
            
            while (rs.next()) {
                services.add(new ServiceData(
                    rs.getString("serviceID"),
                    rs.getString("serviceName"),
                    rs.getDouble("servicePrice")
                ));
            }
            
            serviceCombo.setItems(services);
            
            if (!services.isEmpty()) {
                serviceCombo.setValue(services.get(0));
            }
            
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tải danh sách dịch vụ: " + ex.getMessage());
            showAlert("Lỗi", "Không thể tải danh sách dịch vụ: " + ex.getMessage());
        }
    }
    
    private void updateTotal() {
        total = 0.0;
        ObservableList<ServiceItem> items = serviceListView.getItems();
        if (items != null) {
            for (ServiceItem item : items) {
                total += item.getPrice() * item.getQuantity();
            }
        }
        
        totalLabel.setText(String.format("%,.0f đ", total));
    }
    
    private void saveAppointment() {
        // Kiểm tra khách hàng
        if (selectedCustomerId == null || selectedCustomerId.isEmpty()) {
            showAlert("Lỗi", "Vui lòng chọn khách hàng!");
            return;
        }
        
        // Kiểm tra dịch vụ
        ObservableList<ServiceItem> items = serviceListView.getItems();
        if (items == null || items.isEmpty()) {
            showAlert("Lỗi", "Vui lòng thêm ít nhất một dịch vụ!");
            return;
        }
        
        // Kiểm tra ngày và giờ hẹn
        if (datePicker.getValue() == null || timeCombo.getValue() == null) {
            showAlert("Lỗi", "Vui lòng chọn ngày và giờ hẹn!");
            return;
        }
        
        // Kiểm tra ngày hẹn không được là ngày trong quá khứ
        LocalDate selectedDate = datePicker.getValue();
        LocalDate today = LocalDate.now();
        if (selectedDate.isBefore(today)) {
            showAlert("Lỗi", "Ngày hẹn không được là ngày trong quá khứ!");
            return;
        }
        
        // Kiểm tra giờ hẹn
        LocalTime selectedTime = LocalTime.parse(timeCombo.getValue());
        LocalDateTime selectedDateTime = LocalDateTime.of(selectedDate, selectedTime);
        
        // Kiểm tra giờ làm việc (8:00 - 17:00)
        LocalTime workStartTime = LocalTime.of(8, 0);
        LocalTime workEndTime = LocalTime.of(17, 0);
        
        if (selectedTime.isBefore(workStartTime) || selectedTime.isAfter(workEndTime)) {
            showAlert("Lỗi", "Giờ hẹn phải trong khoảng 8:00 - 17:00!");
            return;
        }
        
        // Kiểm tra trùng lịch hẹn
        if (isAppointmentTimeConflict(selectedDateTime)) {
            showAlert("Lỗi", "Đã có lịch hẹn vào thời gian này. Vui lòng chọn thời gian khác!");
            return;
        }
        
        // Thực hiện đặt lịch hẹn
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Create order record
            String orderSql = "INSERT INTO `order` (Customer_ID, HappenStatusID, orderDate, appointmentDate, totalAmount, orderType) " +
                    "VALUES (?, ?, NOW(), ?, ?, 'Appointment')";
            
            PreparedStatement orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, Integer.parseInt(selectedCustomerId));
            orderStmt.setInt(2, 1); // Status ID (1 = Đang chờ)
            
            // Create appointment date/time
            orderStmt.setTimestamp(3, Timestamp.valueOf(selectedDateTime));
            orderStmt.setDouble(4, total);
            
            orderStmt.executeUpdate();
            
            // Get generated order ID
            ResultSet generatedKeys = orderStmt.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new SQLException("Không thể tạo đơn hàng, không nhận được ID.");
            }
            int orderId = generatedKeys.getInt(1);
            
            // 2. Add order details for each service
            String detailSql = "INSERT INTO order_detail (OrderID, ServiceID, Quantity, UnitPrice) VALUES (?, ?, ?, ?)";
            PreparedStatement detailStmt = conn.prepareStatement(detailSql);
            
            for (ServiceItem service : serviceListView.getItems()) {
                detailStmt.setInt(1, orderId);
                detailStmt.setInt(2, Integer.parseInt(service.getId()));
                detailStmt.setInt(3, service.getQuantity());
                detailStmt.setDouble(4, service.getPrice());
                detailStmt.addBatch();
            }
            
            detailStmt.executeBatch();
            
            // Commit transaction
            conn.commit();
            
            showAlert("Thành công", "Đã đặt lịch hẹn thành công!\nMã đơn hàng: " + orderId);
            
            if (successCallback != null) {
                successCallback.run();
            }
            
            close();
            
        } catch (SQLException ex) {
            // Xử lý ngoại lệ SQL
            System.err.println("Lỗi khi tạo lịch hẹn: " + ex.getMessage());
            
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Lỗi khi rollback: " + rollbackEx.getMessage());
            }
            
            showAlert("Lỗi", "Không thể tạo lịch hẹn: " + ex.getMessage());
        } catch (NumberFormatException ex) {
            showAlert("Lỗi", "ID không hợp lệ: " + ex.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException ex) {
                System.err.println("Lỗi khi đóng kết nối: " + ex.getMessage());
            }
        }
    }

    // Phương thức kiểm tra trùng lịch hẹn
    private boolean isAppointmentTimeConflict(LocalDateTime newAppointmentTime) {
        String sql = "SELECT COUNT(*) FROM `order` " +
                     "WHERE appointmentDate BETWEEN ? AND ? " +
                     "AND orderType = 'Appointment' " +
                     "AND HappenStatusID != (SELECT HappenStatusID FROM happenstatus WHERE StatusName = 'Đã hủy')";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Khoảng thời gian kiểm tra (±30 phút)
            LocalDateTime startTime = newAppointmentTime.minusMinutes(30);
            LocalDateTime endTime = newAppointmentTime.plusMinutes(30);
            
            pstmt.setTimestamp(1, Timestamp.valueOf(startTime));
            pstmt.setTimestamp(2, Timestamp.valueOf(endTime));
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // Nếu có bất kỳ lịch hẹn nào trong khoảng ±30 phút
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException ex) {
            System.err.println("Lỗi khi kiểm tra trùng lịch hẹn: " + ex.getMessage());
            showAlert("Lỗi", "Không thể kiểm tra lịch hẹn: " + ex.getMessage());
        }
        
        return false;
    }
    private boolean isPhoneNumberExist(String phone) {
        String sql = "SELECT COUNT(*) FROM person p JOIN customer c ON p.PersonID = c.PersonID WHERE p.phoneNumber = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi kiểm tra số điện thoại: " + ex.getMessage());
            showAlert("Lỗi", "Không thể kiểm tra số điện thoại: " + ex.getMessage());
        }
        
        return false;
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Service cell for the ListView
    private class ServiceCell extends ListCell<ServiceItem> {
        @Override
        protected void updateItem(ServiceItem item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox container = new HBox(10);
                container.setAlignment(Pos.CENTER_LEFT);
                
                Label nameLabel = new Label(item.getName());
                nameLabel.setPrefWidth(200);
                
                Label priceLabel = new Label(String.format("%,.0f đ", item.getPrice()));
                priceLabel.setPrefWidth(100);
                
                TextField quantityField = new TextField(String.valueOf(item.getQuantity()));
                quantityField.setPrefWidth(60);
                quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
                    try {
                        int quantity = Integer.parseInt(newVal);
                        if (quantity > 0) {
                            item.setQuantity(quantity);
                            updateTotal();
                        }
                    } catch (NumberFormatException e) {
                        // Reset to old value if not a number
                        quantityField.setText(oldVal);
                    }
                });
                
                Label subtotalLabel = new Label(String.format("%,.0f đ", item.getPrice() * item.getQuantity()));
                subtotalLabel.setPrefWidth(100);
                
                Button removeButton = new Button("Xóa");
                removeButton.getStyleClass().addAll("button", "cancel-button");
                removeButton.setOnAction(e -> {
                    getListView().getItems().remove(item);
                    updateTotal();
                });
                
                container.getChildren().addAll(nameLabel, priceLabel, quantityField, subtotalLabel, removeButton);
                setGraphic(container);
            }
        }
    }
    
    // Data classes
    public static class ServiceData {
        private final String id;
        private final String name;
        private final double price;
        
        public ServiceData(String id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        
        @Override
        public String toString() {
            return name + " - " + String.format("%,.0f đ", price);
        }
    }
    
    public static class ServiceItem {
        private final String id;
        private final String name;
        private final double price;
        private int quantity;
        
        public ServiceItem(String id, String name, double price, int quantity) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}