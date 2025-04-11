package controllers.Staff;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CashierDashboard {
    private BorderPane root;
    private Stage primaryStage;
    private String loggedInUsername;
    private TableView<Appointment> table;

    public CashierDashboard(Stage stage, String loggedInUsername) {
        this.primaryStage = stage;
        this.loggedInUsername = loggedInUsername;
        root = new BorderPane();
        root.getStyleClass().add("root");
        root.getStyleClass().add("dashboard-root");
        root.setPadding(new Insets(10));

        // Tạo header (HBox)
        HBox header = createHeader();
        root.setTop(header);

        // Tạo VBox chứa các nút chức năng
        VBox quickActions = createQuickActions();
        root.setLeft(quickActions);

        // Tạo bảng hiển thị đơn hàng
        table = createAppointmentTable();
        root.setCenter(table);

        // Tạo footer với nút in hóa đơn
        VBox footer = createFooter();
        root.setBottom(footer);

        // Liên kết CSS
        try {
            root.getStylesheets().add(getClass().getResource("/view/Staff/Staff.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Lỗi khi tải CSS: " + e.getMessage());
        }
        
        // Load initial data
        table.setItems(loadOrders());
    }

    private HBox createHeader() {
        // Your existing code
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");

        // Thông tin nhân viên
        Label cashierInfo = new Label(getCashierInfo());
        cashierInfo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Nút đăng xuất
        Button logoutButton = new Button("Đăng xuất");
        logoutButton.getStyleClass().addAll("button", "logout-button");
        logoutButton.setOnAction(e -> {
            primaryStage.getScene().setRoot(new LoginScreen(primaryStage).getRoot());
        });

        // Thêm các thành phần vào header
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(cashierInfo, spacer, logoutButton);

        return header;
    }

    // All other methods remain the same...

    public BorderPane getRoot() {
        return root;
    }
    
    // This method was missing in your original code but is used in the application
    public ObservableList<Appointment> loadOrders() {
        ObservableList<Appointment> orders = FXCollections.observableArrayList();
        String sql = 
                "SELECT " +
                "o.orderID, " +
                "pet.PetName, " +
                "tp.UN_TypeName as PetType, " +
                "CONCAT(per.lastName, ' ', per.firstName) as CustomerName, " +
                "GROUP_CONCAT(s.serviceName SEPARATOR ', ') as Services, " +
                "o.orderDate, " +
                "o.appointmentDate, " +
                "o.totalAmount as OrderTotal, " + 
                "hs.StatusName " +
                "FROM `order` o " +
                "JOIN order_detail od ON o.orderID = od.OrderID " +
                "JOIN service s ON od.ServiceID = s.serviceID " +
                "JOIN happenstatus hs ON o.HappenStatusID = hs.HappenStatusID " +
                "JOIN customer c ON o.Customer_ID = c.PersonID " +
                "JOIN person per ON c.PersonID = per.PersonID " +
                "LEFT JOIN pet ON (o.PetID = pet.PetID OR (pet.Customer_ID = c.PersonID AND pet.PetID = (SELECT MIN(PetID) FROM pet WHERE Customer_ID = c.PersonID))) " +
                "LEFT JOIN typepet tp ON pet.TypePetID = tp.TypePetID " +
                "WHERE DATE(COALESCE(o.appointmentDate, o.orderDate)) = CURDATE() " + // Chỉ lấy đơn của ngày hiện tại
                "GROUP BY o.orderID " +
                "ORDER BY COALESCE(o.appointmentDate, o.orderDate) DESC";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
            
            while (rs.next()) {
                String orderId = rs.getString("orderID");
                
                // Tạo thông tin thú cưng với loại thú cưng nếu có
                String petName = rs.getString("PetName");
                String petType = rs.getString("PetType");
                String petInfo = "N/A";
                if (petName != null) {
                    petInfo = petName;
                    if (petType != null) {
                        petInfo += " (" + petType + ")";
                    }
                }
                
                String customerName = rs.getString("CustomerName");
                String services = rs.getString("Services");
                
                // Xử lý thời gian (ưu tiên ngày hẹn, nếu không có thì dùng ngày tạo)
                Timestamp appointmentTimestamp = rs.getTimestamp("appointmentDate");
                Timestamp orderTimestamp = rs.getTimestamp("orderDate");
                
                String timeDisplay;
                if (appointmentTimestamp != null) {
                    LocalDateTime appointmentDateTime = appointmentTimestamp.toLocalDateTime();
                    timeDisplay = appointmentDateTime.format(dateFormatter);
                } else if (orderTimestamp != null) {
                    LocalDateTime orderDateTime = orderTimestamp.toLocalDateTime();
                    timeDisplay = orderDateTime.format(dateFormatter);
                } else {
                    timeDisplay = "N/A";
                }
                
                String status = rs.getString("StatusName") != null ? rs.getString("StatusName") : "N/A";
                
                Appointment appointment = new Appointment(
                    orderId,
                    customerName,
                    petInfo,
                    services.length() > 30 ? services.substring(0, 27) + "..." : services,
                    timeDisplay,
                    status
                );
                
                orders.add(appointment);
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tải đơn hàng: " + ex.getMessage());
            showAlert("Lỗi", "Không thể tải đơn hàng: " + ex.getMessage());
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
        return orders;
    }
    
    // For completeness, implementing the missing methods referenced in the code
    
    private VBox createQuickActions() {
        VBox quickActions = new VBox(20);
        quickActions.setAlignment(Pos.TOP_CENTER);
        quickActions.setPadding(new Insets(20, 10, 10, 10));
        quickActions.setStyle("-fx-background-color: #ecf0f1;");

        // Các nút chức năng
        Button searchButton = new Button("Tra cứu KH");
        searchButton.getStyleClass().addAll("button", "register-button");
        searchButton.setMaxWidth(Double.MAX_VALUE);
        searchButton.setOnAction(e -> {
            CustomerSearchScreen searchScreen = new CustomerSearchScreen();
            searchScreen.showAndWait();
            
            // Refresh table after potential changes
            table.setItems(loadOrders());
        });

        Button bookingButton = new Button("Đặt lịch");
        bookingButton.getStyleClass().addAll("button", "login-button");
        bookingButton.setMaxWidth(Double.MAX_VALUE);
        bookingButton.setOnAction(e -> {
            QuickBookingDialog dialog = new QuickBookingDialog(() -> {
                // Refresh table after successful booking
                table.setItems(loadOrders());
            });
            dialog.show();
        });
        
        Button paymentButton = new Button("Thanh toán");
        paymentButton.getStyleClass().addAll("button", "payment-button");
        paymentButton.setMaxWidth(Double.MAX_VALUE);
        paymentButton.setOnAction(e -> {
            // Sử dụng constructor mới với callback refresh
            PaymentDialog dialog = new PaymentDialog(loggedInUsername, null, () -> {
                // Refresh table after payment processed
                table.setItems(loadOrders());
            });
            dialog.showAndWait();
        });

        Button appointmentButton = new Button("Lịch hẹn");
        appointmentButton.getStyleClass().addAll("button", "primary-button");
        appointmentButton.setMaxWidth(Double.MAX_VALUE);
        appointmentButton.setOnAction(e -> {
            primaryStage.getScene().setRoot(new AppointmentDashboard(primaryStage, loggedInUsername).getRoot());
        });

        quickActions.getChildren().addAll(searchButton, bookingButton, paymentButton, appointmentButton);
        return quickActions;
    }
    
    private TableView<Appointment> createAppointmentTable() {
        TableView<Appointment> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Các cột trong bảng
        TableColumn<Appointment, String> orderCol = new TableColumn<>("Mã đơn");
        orderCol.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        orderCol.setStyle("-fx-alignment: CENTER;");
        orderCol.setPrefWidth(70);

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
        timeCol.setPrefWidth(150);

        TableColumn<Appointment, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setStyle("-fx-alignment: CENTER;");
        statusCol.setPrefWidth(120);
        
        // Action column
        TableColumn<Appointment, Void> actionCol = new TableColumn<>("Thao tác");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("Chi tiết");
            private final Button payBtn = new Button("Thanh toán");
            private final HBox pane = new HBox(5, viewBtn, payBtn);

            {
                viewBtn.getStyleClass().addAll("button", "primary-button");
                payBtn.getStyleClass().addAll("button", "payment-button");
                pane.setAlignment(Pos.CENTER);
                
                viewBtn.setOnAction(event -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    viewOrderDetails(appointment);
                });
                
                payBtn.setOnAction(event -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    processPayment(appointment);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    // Ẩn nút thanh toán nếu đã thanh toán, đã hoàn thành HOẶC đã hủy
                    payBtn.setVisible(!isOrderPaid(appointment) 
                                     && !appointment.getStatus().equals("Đã hoàn thành")
                                     && !appointment.getStatus().equals("Đã hủy"));
                    setGraphic(pane);
                }
            }
        });
        actionCol.setPrefWidth(150);

        table.getColumns().addAll(orderCol, petCol, serviceCol, timeCol, statusCol, actionCol);
        return table;
    }
    
    private VBox createFooter() {
        Button printButton = new Button("In hóa đơn gần nhất");
        printButton.getStyleClass().addAll("button", "print-button");
        printButton.setOnAction(e -> printLatestInvoice());

        VBox footer = new VBox(printButton);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10));
        return footer;
    }
    
    private String getCashierInfo() {
        String sql = "SELECT p.lastName, p.firstName, s.PersonID " +
                     "FROM staff s " +
                     "JOIN person p ON s.PersonID = p.PersonID " +
                     "JOIN account a ON s.AccountID = a.AccountID " +
                     "WHERE a.UN_Username = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, loggedInUsername);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String fullName = rs.getString("lastName") + " " + rs.getString("firstName");
                String personId = String.valueOf(rs.getInt("PersonID"));
                return "Thu ngân: " + fullName + " (ID: " + personId + ")";
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi lấy thông tin nhân viên: " + ex.getMessage());
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
        return "Thu ngân: Không xác định (ID: N/A)";
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Additional required method implementations
    
    private boolean isOrderPaid(Appointment appointment) {
        // Check if the order has an invoice
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM invoice WHERE OrderID = ? AND PaymentStatusID = 2")) {
            
            pstmt.setInt(1, Integer.parseInt(appointment.getAppointmentId()));
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi kiểm tra trạng thái thanh toán: " + ex.getMessage());
        }
        return false;
    }
    
    private void viewOrderDetails(Appointment appointment) {
        // Implementation of order details viewer
        String sql = "SELECT o.orderID, o.orderDate, o.appointmentDate, o.totalAmount as Total, " +
                     "CONCAT(p.lastName, ' ', p.firstName) as CustomerName, p.phoneNumber, " +
                     "pet.PetName, pet.age, tp.UN_TypeName as PetType, " +
                     "s.serviceName, od.Quantity, od.UnitPrice, hs.StatusName " +
                     "FROM `order` o " +
                     "JOIN order_detail od ON o.orderID = od.OrderID " +
                     "JOIN service s ON od.ServiceID = s.serviceID " +
                     "JOIN happenstatus hs ON o.HappenStatusID = hs.HappenStatusID " +
                     "JOIN customer c ON o.Customer_ID = c.PersonID " +
                     "JOIN person p ON c.PersonID = p.PersonID " +
                     "LEFT JOIN pet ON (o.PetID = pet.PetID OR (pet.Customer_ID = c.PersonID AND pet.PetID = (SELECT MIN(PetID) FROM pet WHERE Customer_ID = c.PersonID))) " +
                     "LEFT JOIN typepet tp ON pet.TypePetID = tp.TypePetID " +
                     "WHERE o.orderID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, Integer.parseInt(appointment.getAppointmentId()));
            ResultSet rs = pstmt.executeQuery();
            
            StringBuilder detailsBuilder = new StringBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            if (rs.next()) {
                Timestamp orderDate = rs.getTimestamp("orderDate");
                Timestamp appointmentDate = rs.getTimestamp("appointmentDate");
                
                String formattedOrderDate = orderDate != null ? 
                    LocalDateTime.ofInstant(orderDate.toInstant(), java.time.ZoneId.systemDefault()).format(formatter) : "N/A";
                
                String formattedAppointmentDate = appointmentDate != null ? 
                    LocalDateTime.ofInstant(appointmentDate.toInstant(), java.time.ZoneId.systemDefault()).format(formatter) : "N/A";
                
                String petInfo = rs.getString("PetName");
                if (petInfo != null) {
                    int age = rs.getInt("age");
                    String petType = rs.getString("PetType");
                    petInfo = petInfo + " (" + petType + ", " + age + " tuổi)";
                } else {
                    petInfo = "N/A";
                }
                
                detailsBuilder.append("Thông tin chi tiết đơn hàng\n\n");
                detailsBuilder.append("Mã đơn hàng: ").append(appointment.getAppointmentId()).append("\n");
                detailsBuilder.append("Khách hàng: ").append(rs.getString("CustomerName")).append("\n");
                detailsBuilder.append("Số điện thoại: ").append(rs.getString("phoneNumber")).append("\n");
                detailsBuilder.append("Thú cưng: ").append(petInfo).append("\n");
                detailsBuilder.append("Ngày tạo: ").append(formattedOrderDate).append("\n");
                detailsBuilder.append("Ngày hẹn: ").append(formattedAppointmentDate).append("\n");
                detailsBuilder.append("Trạng thái: ").append(rs.getString("StatusName")).append("\n\n");
                
                detailsBuilder.append("Dịch vụ:\n");
                
                double total = 0;
                do {
                    String serviceName = rs.getString("serviceName");
                    int quantity = rs.getInt("Quantity");
                    double unitPrice = rs.getDouble("UnitPrice");
                    double subtotal = quantity * unitPrice;
                    total += subtotal;
                    
                    detailsBuilder.append("- ").append(serviceName)
                                  .append(" (").append(quantity).append(" x ")
                                  .append(String.format("%,.0f", unitPrice)).append(" đ): ")
                                  .append(String.format("%,.0f đ", subtotal)).append("\n");
                } while (rs.next());
                
                detailsBuilder.append("\nTổng cộng: ").append(String.format("%,.0f đ", total));
            } else {
                detailsBuilder.append("Không tìm thấy thông tin đơn hàng!");
            }
            
            // Display in dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Chi tiết đơn hàng");
            dialog.setHeaderText("Đơn hàng #" + appointment.getAppointmentId());
            
            TextArea textArea = new TextArea(detailsBuilder.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefHeight(400);
            textArea.setPrefWidth(400);
            
            dialog.getDialogPane().setContent(textArea);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
            
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tải chi tiết đơn hàng: " + ex.getMessage());
            showAlert("Lỗi", "Không thể tải chi tiết đơn hàng: " + ex.getMessage());
        }
    }
    
    private void processPayment(Appointment appointment) {
        // Open payment dialog with pre-selected order and refresh callback
        PaymentDialog paymentDialog = new PaymentDialog(loggedInUsername, appointment.getAppointmentId(), () -> {
            // Refresh the table after payment is processed
            table.setItems(loadOrders());
        });
        paymentDialog.showAndWait();
    }
    
    private void printLatestInvoice() {
        String sql = "SELECT i.InvoiceID, i.OrderID, i.CreatedAt, i.Total, i.PaymentMethod, " +
                     "CONCAT(p.lastName, ' ', p.firstName) as CustomerName, p.phoneNumber, " +
                     "od.ServiceID, s.serviceName, od.Quantity, od.UnitPrice " +
                     "FROM invoice i " +
                     "JOIN `order` o ON i.OrderID = o.orderID " +
                     "JOIN customer c ON o.Customer_ID = c.PersonID " +
                     "JOIN person p ON c.PersonID = p.PersonID " +
                     "JOIN order_detail od ON o.orderID = od.OrderID " +
                     "JOIN service s ON od.ServiceID = s.serviceID " +
                     "WHERE i.InvoiceID = (SELECT MAX(InvoiceID) FROM invoice) " +
                     "ORDER BY i.InvoiceID DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            StringBuilder bill = new StringBuilder();
            bill.append("================= HÓA ĐƠN GẦN NHẤT ===================\n");
            
            if (rs.next()) {
                int invoiceId = rs.getInt("InvoiceID");
                Timestamp invoiceDate = rs.getTimestamp("CreatedAt");
                String formattedDate = invoiceDate != null ? 
                    invoiceDate.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : 
                    "N/A";
                String customerName = rs.getString("CustomerName");
                String phoneNumber = rs.getString("phoneNumber");
                String paymentMethod = rs.getString("PaymentMethod");
                
                bill.append("Mã hóa đơn: ").append(invoiceId).append("\n");
                bill.append("Mã đơn hàng: ").append(rs.getInt("OrderID")).append("\n");
                bill.append("Ngày: ").append(formattedDate).append("\n");
                bill.append("Khách hàng: ").append(customerName).append("\n");
                bill.append("Số điện thoại: ").append(phoneNumber).append("\n");
                bill.append("Phương thức: ").append(paymentMethod).append("\n");
                bill.append("=============================================\n");
                bill.append("Danh sách dịch vụ:\n");
                
                double totalAmount = 0;
                do {
                    String serviceName = rs.getString("serviceName");
                    int quantity = rs.getInt("Quantity");
                    double unitPrice = rs.getDouble("UnitPrice");
                    double subtotal = quantity * unitPrice;
                    totalAmount += subtotal;
                    
                    bill.append(String.format("- %s (x%d): %,.0f đ\n", 
                                            serviceName, quantity, subtotal));
                } while (rs.next());
                
                bill.append("=============================================\n");
                bill.append(String.format("TỔNG TIỀN: %,.0f đ\n", totalAmount));
                bill.append("=============================================\n");
                bill.append("Cảm ơn quý khách! Hẹn gặp lại!");
                
                // Print to console for logging
                System.out.println(bill.toString());
                
                // Show in dialog
                TextArea textArea = new TextArea(bill.toString());
                textArea.setEditable(false);
                textArea.setPrefWidth(400);
                textArea.setPrefHeight(500);
                
                Dialog<ButtonType> printDialog = new Dialog<>();
                printDialog.setTitle("In hóa đơn");
                printDialog.setHeaderText("Hóa đơn #" + invoiceId);
                printDialog.getDialogPane().setContent(textArea);
                printDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
                printDialog.showAndWait();
            } else {
                bill.append("Không có hóa đơn nào để in!");
                
                showAlert("Thông báo", "Không có hóa đơn nào để in!");
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi in hóa đơn gần nhất: " + ex.getMessage());
            showAlert("Lỗi", "Không thể in hóa đơn: " + ex.getMessage());
        }
    }
}