package controllers.Staff;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class CustomerDetailDialog extends Stage {
    private final String customerId;
    private VBox root;
    private Label nameLabel;
    private Label phoneLabel;
    private Label addressLabel;
    private Label registerDateLabel;
    private Label loyaltyPointsLabel;
    private TableView<PetData> petTable;
    private TableView<OrderHistoryData> orderTable;
    private String customerFullName;

    public CustomerDetailDialog(String customerId) {
        this.customerId = customerId;
        
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Thông tin chi tiết khách hàng");
        setMinWidth(900);
        setMinHeight(700);
        
        root = new VBox(15);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root");
        root.getStyleClass().add("dialog-container");
        
        setupUI();
        loadCustomerData();
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/view/Staff/Staff.css").toExternalForm());
        setScene(scene);
    }
    
    private void setupUI() {
        // Title
        Label titleLabel = new Label("Thông tin chi tiết khách hàng");
        titleLabel.getStyleClass().add("title-label");
        
        // Customer info section
        GridPane infoGrid = createCustomerInfoSection();
        
        // Tab pane for pets and orders
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        
        // Pet tab
        Tab petTab = new Tab("Thú cưng");
        VBox petContainer = new VBox(10);
        petContainer.setPadding(new Insets(10));
        
        HBox petHeader = new HBox(10);
        petHeader.setAlignment(Pos.CENTER_LEFT);
        Label petSectionLabel = new Label("Danh sách thú cưng");
        petSectionLabel.getStyleClass().add("section-label");
        Button addPetButton = new Button("Thêm thú cưng");
        addPetButton.getStyleClass().addAll("button", "register-button");
        addPetButton.setOnAction(e -> addNewPet());
        petHeader.getChildren().addAll(petSectionLabel, addPetButton);
        
        petTable = createPetTable();
        VBox.setVgrow(petTable, Priority.ALWAYS);
        
        petContainer.getChildren().addAll(petHeader, petTable);
        petTab.setContent(petContainer);
        
        // Orders tab
        Tab orderTab = new Tab("Lịch sử giao dịch");
        VBox orderContainer = new VBox(10);
        orderContainer.setPadding(new Insets(10));
        
        Label orderSectionLabel = new Label("Lịch sử giao dịch");
        orderSectionLabel.getStyleClass().add("section-label");
        
        orderTable = createOrderHistoryTable();
        VBox.setVgrow(orderTable, Priority.ALWAYS);
        
        orderContainer.getChildren().addAll(orderSectionLabel, orderTable);
        orderTab.setContent(orderContainer);
        
        tabPane.getTabs().addAll(petTab, orderTab);
        
        // Bottom buttons
        HBox bottomButtons = createBottomButtons();
        
        root.getChildren().addAll(titleLabel, infoGrid, tabPane, bottomButtons);
    }
    
    private GridPane createCustomerInfoSection() {
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(10, 0, 10, 0));
        infoGrid.getStyleClass().add("info-grid");
        
        // Left column - Basic info
        Label customerInfoLabel = new Label("Thông tin cơ bản");
        customerInfoLabel.getStyleClass().add("section-label");
        GridPane.setColumnSpan(customerInfoLabel, 2);
        infoGrid.add(customerInfoLabel, 0, 0);
        
        infoGrid.add(new Label("Họ tên:"), 0, 1);
        nameLabel = new Label("Đang tải...");
        nameLabel.getStyleClass().add("info-value");
        infoGrid.add(nameLabel, 1, 1);
        
        infoGrid.add(new Label("Số điện thoại:"), 0, 2);
        phoneLabel = new Label("Đang tải...");
        phoneLabel.getStyleClass().add("info-value");
        infoGrid.add(phoneLabel, 1, 2);
        
        infoGrid.add(new Label("Địa chỉ:"), 0, 3);
        addressLabel = new Label("Đang tải...");
        addressLabel.getStyleClass().add("info-value");
        infoGrid.add(addressLabel, 1, 3);
        
        // Right column - Account info
        Label accountInfoLabel = new Label("Thông tin tài khoản");
        accountInfoLabel.getStyleClass().add("section-label");
        GridPane.setColumnSpan(accountInfoLabel, 2);
        infoGrid.add(accountInfoLabel, 2, 0);
        
        infoGrid.add(new Label("Ngày đăng ký:"), 2, 1);
        registerDateLabel = new Label("Đang tải...");
        registerDateLabel.getStyleClass().add("info-value");
        infoGrid.add(registerDateLabel, 3, 1);
        
        infoGrid.add(new Label("Điểm tích lũy:"), 2, 2);
        loyaltyPointsLabel = new Label("Đang tải...");
        loyaltyPointsLabel.getStyleClass().add("info-value");
        infoGrid.add(loyaltyPointsLabel, 3, 2);
        
        // Some styling to make it look nice
        for (int i = 0; i < 4; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(25);
            infoGrid.getColumnConstraints().add(column);
        }
        
        return infoGrid;
    }
    
    private TableView<PetData> createPetTable() {
        TableView<PetData> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Columns
        TableColumn<PetData, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setStyle("-fx-alignment: CENTER;");
        idCol.setPrefWidth(50);
        
        TableColumn<PetData, String> nameCol = new TableColumn<>("Tên thú cưng");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setStyle("-fx-alignment: CENTER-LEFT;");
        nameCol.setPrefWidth(150);
        
        TableColumn<PetData, String> typeCol = new TableColumn<>("Loại");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setStyle("-fx-alignment: CENTER;");
        typeCol.setPrefWidth(100);
        
        TableColumn<PetData, String> breedCol = new TableColumn<>("Giống");
        breedCol.setCellValueFactory(new PropertyValueFactory<>("breed"));
        breedCol.setStyle("-fx-alignment: CENTER-LEFT;");
        breedCol.setPrefWidth(150);
        
        TableColumn<PetData, String> ageCol = new TableColumn<>("Tuổi");
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
        ageCol.setStyle("-fx-alignment: CENTER;");
        ageCol.setPrefWidth(80);
        
        TableColumn<PetData, String> colorCol = new TableColumn<>("Màu sắc");
        colorCol.setCellValueFactory(new PropertyValueFactory<>("color"));
        colorCol.setStyle("-fx-alignment: CENTER;");
        colorCol.setPrefWidth(100);
        
        // Action column
        TableColumn<PetData, Void> actionCol = new TableColumn<>("Thao tác");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Sửa");
            private final Button deleteButton = new Button("Xóa");
            private final HBox pane = new HBox(5, editButton, deleteButton);
            
            {
                editButton.getStyleClass().addAll("button", "primary-button");
                deleteButton.getStyleClass().addAll("button", "cancel-button");
                pane.setAlignment(Pos.CENTER);
                
                editButton.setOnAction(event -> {
                    PetData pet = getTableView().getItems().get(getIndex());
                    editPet(pet);
                });
                
                deleteButton.setOnAction(event -> {
                    PetData pet = getTableView().getItems().get(getIndex());
                    deletePet(pet);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        actionCol.setPrefWidth(120);
        
        table.getColumns().addAll(idCol, nameCol, typeCol, breedCol, ageCol, colorCol, actionCol);
        return table;
    }
    
    private TableView<OrderHistoryData> createOrderHistoryTable() {
        TableView<OrderHistoryData> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Columns
        TableColumn<OrderHistoryData, String> orderIdCol = new TableColumn<>("Mã đơn");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderIdCol.setStyle("-fx-alignment: CENTER;");
        orderIdCol.setPrefWidth(80);
        
        TableColumn<OrderHistoryData, String> dateCol = new TableColumn<>("Ngày");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setStyle("-fx-alignment: CENTER;");
        dateCol.setPrefWidth(100);
        
        TableColumn<OrderHistoryData, String> typeCol = new TableColumn<>("Loại");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setStyle("-fx-alignment: CENTER;");
        typeCol.setPrefWidth(100);
        
        TableColumn<OrderHistoryData, String> servicesCol = new TableColumn<>("Dịch vụ");
        servicesCol.setCellValueFactory(new PropertyValueFactory<>("services"));
        servicesCol.setStyle("-fx-alignment: CENTER-LEFT;");
        servicesCol.setPrefWidth(200);
        
        TableColumn<OrderHistoryData, String> totalCol = new TableColumn<>("Tổng tiền");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        totalCol.setPrefWidth(120);
        
        TableColumn<OrderHistoryData, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setStyle("-fx-alignment: CENTER;");
        statusCol.setPrefWidth(120);
        
        // Action column
        TableColumn<OrderHistoryData, Void> actionCol = new TableColumn<>("Thao tác");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("Chi tiết");
            
            {
                viewButton.getStyleClass().addAll("button", "primary-button");
                viewButton.setOnAction(event -> {
                    OrderHistoryData order = getTableView().getItems().get(getIndex());
                    viewOrderDetails(order);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewButton);
            }
        });
        actionCol.setPrefWidth(100);
        
        table.getColumns().addAll(orderIdCol, dateCol, typeCol, servicesCol, totalCol, statusCol, actionCol);
        return table;
    }
    
    private HBox createBottomButtons() {
        Button closeButton = new Button("Đóng");
        closeButton.getStyleClass().addAll("button", "cancel-button");
        closeButton.setOnAction(e -> close());
        
        Button bookingButton = new Button("Đặt lịch");
        bookingButton.getStyleClass().addAll("button", "login-button");
        bookingButton.setOnAction(e -> createBookingForCustomer());
        
        Button editButton = new Button("Sửa thông tin");
        editButton.getStyleClass().addAll("button", "primary-button");
        editButton.setOnAction(e -> editCustomerInfo());
        
        HBox bottomButtons = new HBox(10, editButton, bookingButton, closeButton);
        bottomButtons.setAlignment(Pos.CENTER_RIGHT);
        return bottomButtons;
    }
    
    private void loadCustomerData() {
        String customerSql = "SELECT p.PersonID, p.lastName, p.firstName, p.phoneNumber, p.citizenNumber, "
                + "p.address, c.registrationDate, c.loyaltyPoints "
                + "FROM customer c "
                + "JOIN person p ON c.PersonID = p.PersonID "
                + "WHERE c.PersonID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(customerSql)) {
            
            pstmt.setInt(1, Integer.parseInt(customerId));
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                customerFullName = rs.getString("lastName") + " " + rs.getString("firstName");
                nameLabel.setText(customerFullName);
                phoneLabel.setText(rs.getString("phoneNumber"));
                addressLabel.setText(rs.getString("address"));
                
                Date registerDate = rs.getDate("registrationDate");
                if (registerDate != null) {
                    registerDateLabel.setText(registerDate.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                } else {
                    registerDateLabel.setText("N/A");
                }
                
                loyaltyPointsLabel.setText(rs.getString("loyaltyPoints"));
            }
            
            // Load pets for this customer
            loadPetData();
            
            // Load order history
            loadOrderHistory();
            
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tải thông tin khách hàng: " + ex.getMessage());
            showAlert("Lỗi", "Không thể tải thông tin khách hàng: " + ex.getMessage());
        }
    }
    private void loadPetData() {
        ObservableList<PetData> petList = FXCollections.observableArrayList();
        String sql = "SELECT p.PetID, p.PetName, tp.UN_TypeName as UN_TypeName, " +
                     "COALESCE(p.Breed, 'N/A') as Breed, p.age, " +
                     "COALESCE(p.Color, 'N/A') as Color " +
                     "FROM pet p " +
                     "JOIN typepet tp ON p.TypePetID = tp.TypePetID " +
                     "WHERE p.Customer_ID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, Integer.parseInt(customerId));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                PetData pet = new PetData(
                    rs.getString("PetID"),
                    rs.getString("PetName"),
                    rs.getString("UN_TypeName"),
                    rs.getString("Breed"),
                    rs.getInt("age") + " tuổi",
                    rs.getString("Color")
                );
                petList.add(pet);
            }
            
            petTable.setItems(petList);
            
            // If no pets found, print a debug message
            if (petList.isEmpty()) {
                System.out.println("Không tìm thấy thú cưng cho khách hàng ID: " + customerId);
            }
            
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tải thông tin thú cưng: " + ex.getMessage());
            ex.printStackTrace(); // In chi tiết stack trace để debug
        } catch (NumberFormatException ex) {
            System.err.println("ID khách hàng không hợp lệ: " + customerId);
        }
    }
    private void loadOrderHistory() {
        ObservableList<OrderHistoryData> orderList = FXCollections.observableArrayList();
        String sql = "SELECT o.orderID, o.orderDate, o.orderType, o.totalAmount as totalAmount, hs.StatusName, " +
                "GROUP_CONCAT(s.serviceName SEPARATOR ', ') as services " +
                "FROM `order` o " +
                "JOIN order_detail od ON o.orderID = od.OrderID " +
                "JOIN service s ON od.ServiceID = s.serviceID " +
                "JOIN happenstatus hs ON o.HappenStatusID = hs.HappenStatusID " +
                "WHERE o.Customer_ID = ? " +
                "GROUP BY o.orderID " +
                "ORDER BY o.orderDate DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, Integer.parseInt(customerId));
            ResultSet rs = pstmt.executeQuery();
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            while (rs.next()) {
                Timestamp orderDate = rs.getTimestamp("orderDate");
                String formattedDate = orderDate != null ? 
                    orderDate.toLocalDateTime().toLocalDate().format(formatter) : "N/A";
                
                OrderHistoryData order = new OrderHistoryData(
                    rs.getString("orderID"),
                    formattedDate,
                    rs.getString("orderType"),
                    rs.getString("services"),
                    String.format("%,.0f đ", rs.getDouble("totalAmount")),
                    rs.getString("StatusName")
                );
                orderList.add(order);
            }
            
            orderTable.setItems(orderList);
            
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tải lịch sử đơn hàng: " + ex.getMessage());
        }
    }
    
    private void addNewPet() {
        // Create a dialog for adding a new pet
        Dialog<PetData> dialog = new Dialog<>();
        dialog.setTitle("Thêm thú cưng mới");
        dialog.setHeaderText("Thông tin thú cưng");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the pet form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Tên thú cưng");
        
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Chó", "Mèo", "Chim", "Chuột", "Thỏ", "Khác");
        typeCombo.setPromptText("Loại thú cưng");
        
        TextField breedField = new TextField();
        breedField.setPromptText("Giống");
        
        TextField ageField = new TextField();
        ageField.setPromptText("Tuổi");
        
        TextField colorField = new TextField();
        colorField.setPromptText("Màu sắc");
        
        grid.add(new Label("Tên:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Loại:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Giống:"), 0, 2);
        grid.add(breedField, 1, 2);
        grid.add(new Label("Tuổi:"), 0, 3);
        grid.add(ageField, 1, 3);
        grid.add(new Label("Màu sắc:"), 0, 4);
        grid.add(colorField, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the name field by default
        nameField.requestFocus();
        
        // Convert the result to a pet data object when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText().isEmpty() || typeCombo.getValue() == null) {
                    showAlert("Lỗi", "Vui lòng điền tên và chọn loại thú cưng!");
                    return null;
                }
                
                int age;
                try {
                    age = Integer.parseInt(ageField.getText());
                    if (age < 0 || age > 30) {
                        showAlert("Lỗi", "Tuổi phải nằm trong khoảng từ 0 đến 30!");
                        return null;
                    }
                } catch (NumberFormatException e) {
                    showAlert("Lỗi", "Tuổi phải là số!");
                    return null;
                }
                
                // Save to database
                if (savePet("", nameField.getText(), typeCombo.getValue(), 
                           breedField.getText(), age, colorField.getText())) {
                    // Reload pet data
                    loadPetData();
                    return new PetData("", nameField.getText(), typeCombo.getValue(), 
                                     breedField.getText(), age + " tuổi", colorField.getText());
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    private boolean savePet(String petId, String name, String type, String breed, int age, String color) {
        String sql;
        boolean isUpdate = !petId.isEmpty();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Lấy TypePetID từ UN_TypeName
            int typePetId;
            String typeIdSql = "SELECT TypePetID FROM typepet WHERE UN_TypeName = ?";
            try (PreparedStatement typeStmt = conn.prepareStatement(typeIdSql)) {
                typeStmt.setString(1, type);
                ResultSet typeRs = typeStmt.executeQuery();
                if (typeRs.next()) {
                    typePetId = typeRs.getInt("TypePetID");
                } else {
                    // Nếu không tìm thấy loại thú cưng, thêm mới
                    String insertTypeSql = "INSERT INTO typepet (UN_TypeName) VALUES (?)";
                    try (PreparedStatement insertTypeStmt = conn.prepareStatement(insertTypeSql, Statement.RETURN_GENERATED_KEYS)) {
                        insertTypeStmt.setString(1, type);
                        insertTypeStmt.executeUpdate();
                        ResultSet generatedKeys = insertTypeStmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            typePetId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Không thể tạo loại thú cưng mới");
                        }
                    }
                }
            }
            
            // Thực hiện update hoặc insert
            if (isUpdate) {
                sql = "UPDATE pet SET PetName = ?, TypePetID = ?, Breed = ?, age = ?, Color = ? WHERE PetID = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, name);
                    pstmt.setInt(2, typePetId);
                    pstmt.setString(3, breed);
                    pstmt.setInt(4, age);
                    pstmt.setString(5, color);
                    pstmt.setInt(6, Integer.parseInt(petId));
                    
                    int rowsAffected = pstmt.executeUpdate();
                    return rowsAffected > 0;
                }
            } else {
                sql = "INSERT INTO pet (PetName, TypePetID, Breed, age, Color, Customer_ID) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, name);
                    pstmt.setInt(2, typePetId);
                    pstmt.setString(3, breed);
                    pstmt.setInt(4, age);
                    pstmt.setString(5, color);
                    pstmt.setInt(6, Integer.parseInt(customerId));
                    
                    int rowsAffected = pstmt.executeUpdate();
                    return rowsAffected > 0;
                }
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi lưu thông tin thú cưng: " + ex.getMessage());
            showAlert("Lỗi", "Không thể lưu thông tin thú cưng: " + ex.getMessage());
            return false;
        }
    }
    private void editPet(PetData pet) {
        // Similar to addNewPet but pre-fill fields with pet data
        Dialog<PetData> dialog = new Dialog<>();
        dialog.setTitle("Sửa thông tin thú cưng");
        dialog.setHeaderText("Chỉnh sửa thông tin: " + pet.getName());
        
        ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField(pet.getName());
        
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Chó", "Mèo", "Chim", "Chuột", "Thỏ", "Khác");
        typeCombo.setValue(pet.getType());
        
        TextField breedField = new TextField(pet.getBreed());
        
        // Extract numeric age from "X tuổi" format
        String ageStr = pet.getAge().replace(" tuổi", "").trim();
        TextField ageField = new TextField(ageStr);
        
        TextField colorField = new TextField(pet.getColor());
        
        grid.add(new Label("Tên:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Loại:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Giống:"), 0, 2);
        grid.add(breedField, 1, 2);
        grid.add(new Label("Tuổi:"), 0, 3);
        grid.add(ageField, 1, 3);
        grid.add(new Label("Màu sắc:"), 0, 4);
        grid.add(colorField, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        nameField.requestFocus();
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText().isEmpty() || typeCombo.getValue() == null) {
                    showAlert("Lỗi", "Vui lòng điền tên và chọn loại thú cưng!");
                    return null;
                }
                
                int age;
                try {
                    age = Integer.parseInt(ageField.getText());
                    if (age < 0 || age > 30) {
                        showAlert("Lỗi", "Tuổi phải nằm trong khoảng từ 0 đến 30!");
                        return null;
                    }
                } catch (NumberFormatException e) {
                    showAlert("Lỗi", "Tuổi phải là số!");
                    return null;
                }
                
                // Update database
                if (savePet(pet.getId(), nameField.getText(), typeCombo.getValue(), 
                           breedField.getText(), age, colorField.getText())) {
                    // Reload pet data
                    loadPetData();
                    return new PetData(pet.getId(), nameField.getText(), typeCombo.getValue(), 
                                     breedField.getText(), age + " tuổi", colorField.getText());
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    private void deletePet(PetData pet) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận xóa");
        confirmDialog.setHeaderText("Xóa thú cưng: " + pet.getName());
        confirmDialog.setContentText("Bạn có chắc chắn muốn xóa thú cưng này?");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM pet WHERE PetID = ?";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, Integer.parseInt(pet.getId()));
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    showAlert("Thành công", "Đã xóa thú cưng thành công!");
                    loadPetData();
                } else {
                    showAlert("Lỗi", "Không thể xóa thú cưng!");
                }
                
            } catch (SQLException ex) {
                System.err.println("Lỗi khi xóa thú cưng: " + ex.getMessage());
                showAlert("Lỗi", "Không thể xóa thú cưng: " + ex.getMessage());
            }
        }
    }
    
    private void viewOrderDetails(OrderHistoryData order) {
        // Show detailed order information
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Chi tiết đơn hàng");
        dialog.setHeaderText("Thông tin đơn hàng #" + order.getOrderId());
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        StringBuilder detailsBuilder = new StringBuilder();
        
        // Load detailed order information
        String sql = "SELECT s.serviceName, od.Quantity, od.UnitPrice, (od.Quantity * od.UnitPrice) as Subtotal " +
                     "FROM order_detail od " +
                     "JOIN service s ON od.ServiceID = s.serviceID " +
                     "WHERE od.OrderID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, Integer.parseInt(order.getOrderId()));
            ResultSet rs = pstmt.executeQuery();
            
            detailsBuilder.append("Ngày đặt hàng: ").append(order.getDate()).append("\n");
            detailsBuilder.append("Loại đơn hàng: ").append(order.getType()).append("\n");
            detailsBuilder.append("Trạng thái: ").append(order.getStatus()).append("\n\n");
            detailsBuilder.append("Chi tiết dịch vụ:\n");
            
            double total = 0;
            while (rs.next()) {
                String serviceName = rs.getString("serviceName");
                int quantity = rs.getInt("Quantity");
                double unitPrice = rs.getDouble("UnitPrice");
                double subtotal = rs.getDouble("Subtotal");
                total += subtotal;
                
                detailsBuilder.append(" - ").append(serviceName)
                              .append(" (").append(quantity).append(" x ")
                              .append(String.format("%,.0f đ", unitPrice)).append("): ")
                              .append(String.format("%,.0f đ", subtotal)).append("\n");
            }
            
            detailsBuilder.append("\nTổng cộng: ").append(String.format("%,.0f đ", total));
            
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tải chi tiết đơn hàng: " + ex.getMessage());
            detailsBuilder.append("Không thể tải chi tiết đơn hàng: ").append(ex.getMessage());
        }
        
        TextArea textArea = new TextArea(detailsBuilder.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(300);
        textArea.setPrefWidth(400);
        
        content.getChildren().add(textArea);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }
    
    private void createBookingForCustomer() {
        QuickBookingDialog dialog = new QuickBookingDialog(
            customerId,
            customerFullName,
            () -> loadOrderHistory() // Refresh after booking
        );
        dialog.show();
    }
    
    private void editCustomerInfo() {
        // Create a dialog for editing customer information
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Sửa thông tin khách hàng");
        dialog.setHeaderText("Chỉnh sửa thông tin: " + customerFullName);
        
        ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Load current customer data
        String sql = "SELECT p.lastName, p.firstName, p.phoneNumber, p.citizenNumber, p.address, p.sex " +
                     "FROM person p " +
                     "WHERE p.PersonID = ?";
        
        TextField lastNameField = new TextField();
        TextField firstNameField = new TextField();
        TextField phoneField = new TextField();
        TextField citizenNumberField = new TextField();
        TextField addressField = new TextField();
        ComboBox<String> sexCombo = new ComboBox<>();
        sexCombo.getItems().addAll("Nam", "Nữ");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, Integer.parseInt(customerId));
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                lastNameField.setText(rs.getString("lastName"));
                firstNameField.setText(rs.getString("firstName"));
                phoneField.setText(rs.getString("phoneNumber"));
                citizenNumberField.setText(rs.getString("citizenNumber"));
                addressField.setText(rs.getString("address"));
                sexCombo.setValue(rs.getInt("sex") == 1 ? "Nam" : "Nữ");
            }
            
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tải thông tin khách hàng: " + ex.getMessage());
        }
        
        grid.add(new Label("Họ:"), 0, 0);
        grid.add(lastNameField, 1, 0);
        grid.add(new Label("Tên:"), 0, 1);
        grid.add(firstNameField, 1, 1);
        grid.add(new Label("Số điện thoại:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Số CCCD:"), 0, 3);
        grid.add(citizenNumberField, 1, 3);
        grid.add(new Label("Địa chỉ:"), 0, 4);
        grid.add(addressField, 1, 4);
        grid.add(new Label("Giới tính:"), 0, 5);
        grid.add(sexCombo, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        lastNameField.requestFocus();
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            if (lastNameField.getText().isEmpty() || firstNameField.getText().isEmpty() || 
                phoneField.getText().isEmpty() || citizenNumberField.getText().isEmpty() || 
                addressField.getText().isEmpty() || sexCombo.getValue() == null) {
                showAlert("Lỗi", "Vui lòng điền đầy đủ thông tin!");
                return;
            }
            
            if (phoneField.getText().length() != 10 || !phoneField.getText().matches("\\d+")) {
                showAlert("Lỗi", "Số điện thoại phải có 10 chữ số!");
                return;
            }
            
            if (citizenNumberField.getText().length() != 12 || !citizenNumberField.getText().matches("\\d+")) {
                showAlert("Lỗi", "Số CCCD phải có 12 chữ số!");
                return;
            }
            
            // Update customer information
            String updateSql = "UPDATE person SET lastName = ?, firstName = ?, phoneNumber = ?, " +
                              "citizenNumber = ?, address = ?, sex = ? WHERE PersonID = ?";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                
                pstmt.setString(1, lastNameField.getText());
                pstmt.setString(2, firstNameField.getText());
                pstmt.setString(3, phoneField.getText());
                pstmt.setString(4, citizenNumberField.getText());
                pstmt.setString(5, addressField.getText());
                pstmt.setInt(6, sexCombo.getValue().equals("Nam") ? 1 : 2);
                pstmt.setInt(7, Integer.parseInt(customerId));
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    showAlert("Thành công", "Đã cập nhật thông tin khách hàng thành công!");
                    loadCustomerData(); // Reload customer data
                } else {
                    showAlert("Lỗi", "Không thể cập nhật thông tin khách hàng!");
                }
                
            } catch (SQLException ex) {
                System.err.println("Lỗi khi cập nhật thông tin khách hàng: " + ex.getMessage());
                showAlert("Lỗi", "Không thể cập nhật thông tin khách hàng: " + ex.getMessage());
            }
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Data classes for the tables
    public static class PetData {
        private final SimpleStringProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty type;
        private final SimpleStringProperty breed;
        private final SimpleStringProperty age;
        private final SimpleStringProperty color;
        
        public PetData(String id, String name, String type, String breed, String age, String color) {
            this.id = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);
            this.type = new SimpleStringProperty(type);
            this.breed = new SimpleStringProperty(breed);
            this.age = new SimpleStringProperty(age);
            this.color = new SimpleStringProperty(color);
        }
        
        public String getId() { return id.get(); }
        public String getName() { return name.get(); }
        public String getType() { return type.get(); }
        public String getBreed() { return breed.get(); }
        public String getAge() { return age.get(); }
        public String getColor() { return color.get(); }
    }
    
    public static class OrderHistoryData {
        private final SimpleStringProperty orderId;
        private final SimpleStringProperty date;
        private final SimpleStringProperty type;
        private final SimpleStringProperty services;
        private final SimpleStringProperty total;
        private final SimpleStringProperty status;
        
        public OrderHistoryData(String orderId, String date, String type, String services,
                                String total, String status) {
            this.orderId = new SimpleStringProperty(orderId);
            this.date = new SimpleStringProperty(date);
            this.type = new SimpleStringProperty(type);
            this.services = new SimpleStringProperty(services);
            this.total = new SimpleStringProperty(total);
            this.status = new SimpleStringProperty(status);
        }
        
        public String getOrderId() { return orderId.get(); }
        public String getDate() { return date.get(); }
        public String getType() { return type.get(); }
        public String getServices() { return services.get(); }
        public String getTotal() { return total.get(); }
        public String getStatus() { return status.get(); }
    }
}