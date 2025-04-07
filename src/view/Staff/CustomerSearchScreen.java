package view.Staff;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.scene.Scene;
public class CustomerSearchScreen extends Stage {
    private VBox root;
    private TableView<CustomerData> customerTable;
    private TextField searchField;
    private ComboBox<String> searchTypeCombo;
    private Label resultLabel;
    private Button searchButton;

    public CustomerSearchScreen() {
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Tra cứu khách hàng");
        setMinWidth(800);
        setMinHeight(600);

        root = new VBox(15);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root");
        root.getStyleClass().add("dialog-container");

        setupUI();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/view/Staff/Staff.css").toExternalForm());
        setScene(scene);
    }

    private void setupUI() {
        // Title
        Label titleLabel = new Label("Tra cứu khách hàng");
        titleLabel.getStyleClass().add("title-label");

        // Search controls
        HBox searchControls = createSearchControls();

        // Customer table
        customerTable = createCustomerTable();
        VBox.setVgrow(customerTable, Priority.ALWAYS);

        // Result label
        resultLabel = new Label("");
        resultLabel.getStyleClass().add("message-label");

        // Buttons at bottom
        HBox bottomButtons = createBottomButtons();

        root.getChildren().addAll(titleLabel, searchControls, customerTable, resultLabel, bottomButtons);
    }

    private HBox createSearchControls() {
        searchTypeCombo = new ComboBox<>();
        searchTypeCombo.getItems().addAll("Tên", "Số điện thoại", "CCCD", "ID");
        searchTypeCombo.setValue("Số điện thoại");
        searchTypeCombo.getStyleClass().add("combo-box");

        searchField = new TextField();
        searchField.setPromptText("Nhập thông tin tìm kiếm...");
        searchField.getStyleClass().add("text-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchButton = new Button("Tìm kiếm");
        searchButton.getStyleClass().addAll("button", "primary-button");
        searchButton.setOnAction(e -> searchCustomers());

        HBox searchControls = new HBox(10, new Label("Tìm theo:"), searchTypeCombo, searchField, searchButton);
        searchControls.setAlignment(Pos.CENTER_LEFT);
        return searchControls;
    }

    private TableView<CustomerData> createCustomerTable() {
        TableView<CustomerData> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Columns
        TableColumn<CustomerData, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setStyle("-fx-alignment: CENTER;");
        idCol.setPrefWidth(50);

        TableColumn<CustomerData, String> nameCol = new TableColumn<>("Họ tên");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameCol.setStyle("-fx-alignment: CENTER-LEFT;");
        nameCol.setPrefWidth(200);

        TableColumn<CustomerData, String> phoneCol = new TableColumn<>("Số điện thoại");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setStyle("-fx-alignment: CENTER;");
        phoneCol.setPrefWidth(120);

        TableColumn<CustomerData, String> cccdCol = new TableColumn<>("CCCD");
        cccdCol.setCellValueFactory(new PropertyValueFactory<>("citizenNumber"));
        cccdCol.setStyle("-fx-alignment: CENTER;");
        cccdCol.setPrefWidth(130);

        TableColumn<CustomerData, String> addressCol = new TableColumn<>("Địa chỉ");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        addressCol.setStyle("-fx-alignment: CENTER-LEFT;");
        addressCol.setPrefWidth(200);

        TableColumn<CustomerData, String> pointsCol = new TableColumn<>("Điểm tích lũy");
        pointsCol.setCellValueFactory(new PropertyValueFactory<>("loyaltyPoints"));
        pointsCol.setStyle("-fx-alignment: CENTER;");
        pointsCol.setPrefWidth(100);

        // Action column
        TableColumn<CustomerData, Void> actionCol = new TableColumn<>("Thao tác");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("Chi tiết");
            private final Button bookingButton = new Button("Đặt lịch");
            private final HBox pane = new HBox(5, viewButton, bookingButton);

            {
                viewButton.getStyleClass().addAll("button", "primary-button");
                bookingButton.getStyleClass().addAll("button", "login-button");
                pane.setAlignment(Pos.CENTER);
                
                viewButton.setOnAction(event -> {
                    CustomerData customer = getTableView().getItems().get(getIndex());
                    showCustomerDetails(customer);
                });
                
                bookingButton.setOnAction(event -> {
                    CustomerData customer = getTableView().getItems().get(getIndex());
                    createBookingForCustomer(customer);
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

        table.getColumns().addAll(idCol, nameCol, phoneCol, cccdCol, addressCol, pointsCol, actionCol);
        return table;
    }

    private HBox createBottomButtons() {
        Button closeButton = new Button("Đóng");
        closeButton.getStyleClass().addAll("button", "cancel-button");
        closeButton.setOnAction(e -> close());

        Button registerButton = new Button("Đăng ký KH mới");
        registerButton.getStyleClass().addAll("button", "register-button");
        registerButton.setOnAction(e -> openCustomerRegistration());

        HBox bottomButtons = new HBox(10, registerButton, closeButton);
        bottomButtons.setAlignment(Pos.CENTER_RIGHT);
        return bottomButtons;
    }

    private void searchCustomers() {
        String searchValue = searchField.getText().trim();
        String searchType = searchTypeCombo.getValue();
        
        if (searchValue.isEmpty()) {
            resultLabel.setText("Vui lòng nhập thông tin tìm kiếm");
            resultLabel.getStyleClass().remove("message-success");
            resultLabel.getStyleClass().add("message-error");
            return;
        }

        ObservableList<CustomerData> customerList = FXCollections.observableArrayList();
        String sql = buildSearchQuery(searchType);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            setSearchParameters(pstmt, searchType, searchValue);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                CustomerData customer = new CustomerData(
                    rs.getString("PersonID"),
                    rs.getString("lastName") + " " + rs.getString("firstName"),
                    rs.getString("phoneNumber"),
                    rs.getString("citizenNumber"),
                    rs.getString("address"),
                    rs.getString("loyaltyPoints")
                );
                customerList.add(customer);
            }
            
            customerTable.setItems(customerList);
            
            if (customerList.isEmpty()) {
                resultLabel.setText("Không tìm thấy khách hàng phù hợp");
                resultLabel.getStyleClass().remove("message-success");
                resultLabel.getStyleClass().add("message-error");
            } else {
                resultLabel.setText("Tìm thấy " + customerList.size() + " kết quả");
                resultLabel.getStyleClass().remove("message-error");
                resultLabel.getStyleClass().add("message-success");
            }
            
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tìm kiếm khách hàng: " + ex.getMessage());
            resultLabel.setText("Đã xảy ra lỗi khi tìm kiếm: " + ex.getMessage());
            resultLabel.getStyleClass().remove("message-success");
            resultLabel.getStyleClass().add("message-error");
        }
    }

    private String buildSearchQuery(String searchType) {
        String baseQuery = "SELECT c.PersonID, p.lastName, p.firstName, p.phoneNumber, p.citizenNumber, " +
                           "p.address, c.loyaltyPoints " +
                           "FROM customer c " +
                           "JOIN person p ON c.PersonID = p.PersonID " +
                           "WHERE ";
        
        switch (searchType) {
            case "Tên":
                return baseQuery + "CONCAT(p.lastName, ' ', p.firstName) LIKE ?";
            case "Số điện thoại":
                return baseQuery + "p.phoneNumber LIKE ?";
            case "CCCD":
                return baseQuery + "p.citizenNumber LIKE ?";
            case "ID":
                return baseQuery + "c.PersonID = ?";
            default:
                return baseQuery + "p.phoneNumber LIKE ?";
        }
    }

    private void setSearchParameters(PreparedStatement pstmt, String searchType, String searchValue) throws SQLException {
        if (searchType.equals("ID")) {
            try {
                int id = Integer.parseInt(searchValue);
                pstmt.setInt(1, id);
            } catch (NumberFormatException e) {
                pstmt.setInt(1, -1); // No results will be found for ID -1
            }
        } else if (searchType.equals("Tên")) {
            pstmt.setString(1, "%" + searchValue + "%");
        } else {
            // For phone and CCCD, we can do both exact and partial matches
            pstmt.setString(1, searchValue.contains("%") ? searchValue : "%" + searchValue + "%");
        }
    }

    private void showCustomerDetails(CustomerData customer) {
        CustomerDetailDialog dialog = new CustomerDetailDialog(customer.getId());
        dialog.showAndWait();
    }

    private void createBookingForCustomer(CustomerData customer) {
        QuickBookingDialog dialog = new QuickBookingDialog(
            customer.getId(),
            customer.getFullName(),
            () -> searchCustomers() // Refresh after booking
        );
        dialog.show();
    }

    private void openCustomerRegistration() {
        TextInputDialog phoneDialog = new TextInputDialog();
        phoneDialog.setTitle("Nhập số điện thoại");
        phoneDialog.setHeaderText("Đăng ký khách hàng mới");
        phoneDialog.setContentText("Vui lòng nhập số điện thoại của khách hàng:");
        
        phoneDialog.showAndWait().ifPresent(phone -> {
            if (phone.matches("\\d{10}")) {
                // Kiểm tra xem số điện thoại đã tồn tại chưa
                if (isPhoneNumberExist(phone)) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Số điện thoại đã tồn tại");
                    alert.setHeaderText(null);
                    alert.setContentText("Số điện thoại này đã được đăng ký. Vui lòng tìm kiếm bằng số điện thoại.");
                    alert.showAndWait();
                    
                    // Tự động tìm kiếm số điện thoại đó
                    searchTypeCombo.setValue("Số điện thoại");
                    searchField.setText(phone);
                    searchButton.fire();
                } else {
                    // Mở form đăng ký
                    CustomerRegistrationScreen registrationScreen = new CustomerRegistrationScreen(phone, () -> {
                        // Sau khi đăng ký thành công, hiển thị thông tin khách hàng mới
                        searchTypeCombo.setValue("Số điện thoại");
                        searchField.setText(phone);
                        searchButton.fire();
                    });
                    registrationScreen.showAndWait();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Số điện thoại phải có 10 chữ số!");
                alert.showAndWait();
            }
        });
    }

    private boolean isPhoneNumberExist(String phoneNumber) {
        String sql = "SELECT COUNT(*) FROM person p JOIN customer c ON p.PersonID = c.PersonID WHERE p.phoneNumber = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, phoneNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi kiểm tra số điện thoại: " + ex.getMessage());
        }
        return false;
    }

    // Customer data class for the table
    public static class CustomerData {
        private final SimpleStringProperty id;
        private final SimpleStringProperty fullName;
        private final SimpleStringProperty phone;
        private final SimpleStringProperty citizenNumber;
        private final SimpleStringProperty address;
        private final SimpleStringProperty loyaltyPoints;

        public CustomerData(String id, String fullName, String phone, String citizenNumber, String address, String loyaltyPoints) {
            this.id = new SimpleStringProperty(id);
            this.fullName = new SimpleStringProperty(fullName);
            this.phone = new SimpleStringProperty(phone);
            this.citizenNumber = new SimpleStringProperty(citizenNumber);
            this.address = new SimpleStringProperty(address);
            this.loyaltyPoints = new SimpleStringProperty(loyaltyPoints);
        }

        public String getId() { return id.get(); }
        public String getFullName() { return fullName.get(); }
        public String getPhone() { return phone.get(); }
        public String getCitizenNumber() { return citizenNumber.get(); }
        public String getAddress() { return address.get(); }
        public String getLoyaltyPoints() { return loyaltyPoints.get(); }
    }
}