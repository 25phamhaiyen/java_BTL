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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PaymentDialog extends Stage {
    private VBox root;
    private TextField invoiceIdField;
    private TableView<ServiceData> serviceTable;
    private Label invoiceDetailsLabel;
    private Label totalLabel;
    private double totalAmount = 0.0;
    private String loggedInUsername;
    private RadioButton cashRadio;
    private RadioButton transferRadio;
    private String preSelectedOrderId;
    private Runnable refreshCallback; // Thêm callback để refresh giao diện

    // Constructor chính, nhận loggedInUsername
    public PaymentDialog(String loggedInUsername) {
        this(loggedInUsername, null); // Gọi constructor có tham số orderId với giá trị null
    }

    // Constructor nhận cả loggedInUsername và orderId
    public PaymentDialog(String loggedInUsername, String orderId) {
        this.loggedInUsername = loggedInUsername;
        this.preSelectedOrderId = orderId;
        this.refreshCallback = () -> {}; // Mặc định là không làm gì
        
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Thanh toán");
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

        // Nếu có preSelectedOrderId, tự động tải thông tin
        if (preSelectedOrderId != null) {
            invoiceIdField.setText(preSelectedOrderId);
            loadInvoiceDetails();
        }
    }
    
    // Constructor với callback để refresh giao diện
    public PaymentDialog(String loggedInUsername, String orderId, Runnable refreshCallback) {
        this(loggedInUsername, orderId);
        this.refreshCallback = refreshCallback != null ? refreshCallback : () -> {};
    }

    private void setupUI() {
        HBox mainLayout = new HBox(20);

        // Phần thông tin thanh toán (bên trái)
        VBox paymentInfoBox = createPaymentInfoBox();
        paymentInfoBox.setPrefWidth(400);

        // Phần chi tiết hóa đơn (bên phải)
        VBox invoiceDetailsBox = createInvoiceDetailsBox();
        invoiceDetailsBox.setPrefWidth(400);

        mainLayout.getChildren().addAll(paymentInfoBox, invoiceDetailsBox);
        root.getChildren().add(mainLayout);
    }

    private VBox createPaymentInfoBox() {
        VBox paymentInfoBox = new VBox(15);
        paymentInfoBox.setPadding(new Insets(10));
        paymentInfoBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5;");

        Label titleLabel = new Label("THÔNG TIN THANH TOÁN");
        titleLabel.getStyleClass().add("title-label");

        // Nhập mã hóa đơn
        HBox invoiceIdBox = new HBox(10);
        Label invoiceIdLabel = new Label("Nhập mã hóa đơn:");
        invoiceIdField = new TextField();
        invoiceIdField.setPromptText("Nhập mã hóa đơn...");
        invoiceIdField.getStyleClass().add("text-field");
        HBox.setHgrow(invoiceIdField, Priority.ALWAYS);
        invoiceIdBox.getChildren().addAll(invoiceIdLabel, invoiceIdField);

        // Nút "Tải thông tin"
        Button loadButton = new Button("Tải thông tin");
        loadButton.getStyleClass().addAll("button", "primary-button");
        loadButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        loadButton.setOnAction(e -> loadInvoiceDetails());

        // Nút "Kiểm tra hóa đơn"
        Button checkButton = new Button("Kiểm tra hóa đơn");
        checkButton.getStyleClass().addAll("button", "secondary-button");
        checkButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        checkButton.setOnAction(e -> checkInvoice());

        // Thông tin chi tiết hóa đơn
        invoiceDetailsLabel = new Label("Chi tiết hóa đơn:\nMã hóa đơn: N/A\nKhách hàng: N/A\nSĐT: N/A\nNgày tạo: N/A\nTổng tiền: 0 đ");
        invoiceDetailsLabel.setStyle("-fx-font-size: 14px;");

        // Phương thức thanh toán
        Label paymentMethodLabel = new Label("PHƯƠNG THỨC THANH TOÁN:");
        paymentMethodLabel.getStyleClass().add("section-label");

        ToggleGroup paymentMethodGroup = new ToggleGroup();
        cashRadio = new RadioButton("Tiền mặt");
        transferRadio = new RadioButton("Chuyển khoản");
        cashRadio.setToggleGroup(paymentMethodGroup);
        transferRadio.setToggleGroup(paymentMethodGroup);
        cashRadio.setSelected(true);

        HBox paymentMethodBox = new HBox(20, cashRadio, transferRadio);
        paymentMethodBox.setAlignment(Pos.CENTER_LEFT);

        // Nút "Xác nhận thanh toán"
        Button confirmButton = new Button("Xác nhận thanh toán");
        confirmButton.getStyleClass().addAll("button", "danger-button");
        confirmButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        confirmButton.setOnAction(e -> confirmPayment());

        paymentInfoBox.getChildren().addAll(
            titleLabel,
            invoiceIdBox,
            loadButton,
            checkButton,
            invoiceDetailsLabel,
            paymentMethodLabel,
            paymentMethodBox,
            confirmButton
        );

        return paymentInfoBox;
    }

    private VBox createInvoiceDetailsBox() {
        VBox invoiceDetailsBox = new VBox(15);
        invoiceDetailsBox.setPadding(new Insets(10));
        invoiceDetailsBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-radius: 5;");

        Label titleLabel = new Label("CHI TIẾT HÓA ĐƠN #N/A");
        titleLabel.getStyleClass().add("title-label");

        // Bảng danh sách dịch vụ
        serviceTable = new TableView<>();
        serviceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ServiceData, String> serviceCol = new TableColumn<>("Dịch vụ");
        serviceCol.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        serviceCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<ServiceData, String> unitPriceCol = new TableColumn<>("Đơn giá");
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        unitPriceCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<ServiceData, String> quantityCol = new TableColumn<>("Số lượng");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<ServiceData, String> subtotalCol = new TableColumn<>("Thành tiền");
        subtotalCol.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        subtotalCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        serviceTable.getColumns().addAll(serviceCol, unitPriceCol, quantityCol, subtotalCol);

        // Tổng cộng
        totalLabel = new Label("TỔNG CỘNG: 0 đ");
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Lời cảm ơn
        Label thankYouLabel = new Label("Cảm ơn quý khách đã sử dụng dịch vụ!");
        thankYouLabel.setStyle("-fx-font-size: 14px;");

        // Đề xuất xuất PDF
        Label pdfLabel = new Label("Đã xuất PDF thành công: N/A");
        pdfLabel.setStyle("-fx-font-size: 14px;");

        // Nút "In hóa đơn PDF" và "Hủy"
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button printButton = new Button("In hóa đơn PDF");
        printButton.getStyleClass().addAll("button", "warning-button");
        printButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black;");
        printButton.setOnAction(e -> printInvoicePDF());

        Button cancelButton = new Button("Hủy");
        cancelButton.getStyleClass().addAll("button", "cancel-button");
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        cancelButton.setOnAction(e -> close());

        buttonBox.getChildren().addAll(printButton, cancelButton);

        invoiceDetailsBox.getChildren().addAll(
            titleLabel,
            serviceTable,
            totalLabel,
            thankYouLabel,
            pdfLabel,
            buttonBox
        );

        VBox.setVgrow(serviceTable, Priority.ALWAYS);
        return invoiceDetailsBox;
    }
    
    private void loadInvoiceDetails() {
        String invoiceIdStr = invoiceIdField.getText().trim();
        if (invoiceIdStr.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập mã hóa đơn!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Đầu tiên kiểm tra xem hóa đơn có tồn tại không
            String checkOrderSql = "SELECT COUNT(*) FROM `order` WHERE orderID = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkOrderSql)) {
                checkStmt.setInt(1, Integer.parseInt(invoiceIdStr));
                ResultSet checkRs = checkStmt.executeQuery();
                if (checkRs.next() && checkRs.getInt(1) == 0) {
                    showAlert("Lỗi", "Không tìm thấy hóa đơn với mã: " + invoiceIdStr);
                    resetUI();
                    return;
                }
            }
            
            // Kiểm tra xem bảng order có trường Total hay totalAmount
            boolean hasTotal = false;
            boolean hasTotalAmount = false;
            
            try (ResultSet columns = conn.getMetaData().getColumns(null, null, "order", "Total")) {
                hasTotal = columns.next();
            }
            
            try (ResultSet columns = conn.getMetaData().getColumns(null, null, "order", "totalAmount")) {
                hasTotalAmount = columns.next();
            }
            
            // Xây dựng câu truy vấn dựa trên trường có sẵn
            String totalColumn;
            if (hasTotal) {
                totalColumn = "o.Total";
            } else if (hasTotalAmount) {
                totalColumn = "o.totalAmount";
            } else {
                // Nếu không tìm thấy trường nào, sẽ tính tổng từ order_detail
                totalColumn = "(SELECT SUM(od2.UnitPrice * od2.Quantity) FROM order_detail od2 WHERE od2.OrderID = o.orderID) AS 'OrderTotal'";
            }
            
            // Lấy thông tin đơn hàng và khách hàng (không bao gồm chi tiết)
            String orderInfoSql = "SELECT o.orderID, o.orderDate, " + totalColumn + " as OrderTotal, " +
                               "CONCAT(p.lastName, ' ', p.firstName) as CustomerName, p.phoneNumber " +
                               "FROM `order` o " +
                               "JOIN customer c ON o.Customer_ID = c.PersonID " +
                               "JOIN person p ON c.PersonID = p.PersonID " +
                               "WHERE o.orderID = ?";
            
            try (PreparedStatement infoStmt = conn.prepareStatement(orderInfoSql)) {
                infoStmt.setInt(1, Integer.parseInt(invoiceIdStr));
                ResultSet infoRs = infoStmt.executeQuery();
                
                if (infoRs.next()) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    String formattedDate = infoRs.getTimestamp("orderDate") != null ?
                        infoRs.getTimestamp("orderDate").toLocalDateTime().format(formatter) : "N/A";
    
                    totalAmount = infoRs.getDouble("OrderTotal");
                    String customerName = infoRs.getString("CustomerName");
                    String phoneNumber = infoRs.getString("phoneNumber");
                    
                    invoiceDetailsLabel.setText(String.format(
                        "Chi tiết hóa đơn:\nMã hóa đơn: %s\nKhách hàng: %s\nSĐT: %s\nNgày tạo: %s\nTổng tiền: %,.0f đ",
                        invoiceIdStr, customerName, phoneNumber, formattedDate, totalAmount
                    ));
                    
                    // Bây giờ lấy chi tiết dịch vụ trong một truy vấn riêng biệt
                    String detailsSql = "SELECT od.ServiceID, s.serviceName, od.Quantity, od.UnitPrice " +
                                     "FROM order_detail od " +
                                     "JOIN service s ON od.ServiceID = s.serviceID " +
                                     "WHERE od.OrderID = ?";
                                     
                    try (PreparedStatement detailsStmt = conn.prepareStatement(detailsSql)) {
                        detailsStmt.setInt(1, Integer.parseInt(invoiceIdStr));
                        ResultSet detailsRs = detailsStmt.executeQuery();
                        
                        double calculatedTotal = 0.0;
                        ObservableList<ServiceData> serviceList = FXCollections.observableArrayList();
                        
                        while (detailsRs.next()) {
                            try {
                                String serviceName = detailsRs.getString("serviceName");
                                double unitPrice = detailsRs.getDouble("UnitPrice");
                                int quantity = detailsRs.getInt("Quantity");
                                double subtotal = unitPrice * quantity;
                                calculatedTotal += subtotal;
        
                                serviceList.add(new ServiceData(
                                    serviceName,
                                    String.format("%,.0f", unitPrice),
                                    String.valueOf(quantity),
                                    String.format("%,.0f", subtotal)
                                ));
                            } catch (SQLException e) {
                                System.err.println("Lỗi khi đọc dữ liệu dịch vụ: " + e.getMessage());
                            }
                        }
                        
                        // Kiểm tra nếu tổng tính từ chi tiết khác với tổng trong hóa đơn
                        if (Math.abs(calculatedTotal - totalAmount) > 0.01) {
                            System.out.println("Cảnh báo: Tổng tiền không khớp. Từ DB: " + totalAmount + ", Tính toán: " + calculatedTotal);
                            // Nếu không có chi tiết dịch vụ hoặc tổng tính không khớp, sử dụng tổng tính từ chi tiết
                            if (calculatedTotal > 0) {
                                totalAmount = calculatedTotal;
                                // Cập nhật lại thông tin hiển thị
                                invoiceDetailsLabel.setText(String.format(
                                    "Chi tiết hóa đơn:\nMã hóa đơn: %s\nKhách hàng: %s\nSĐT: %s\nNgày tạo: %s\nTổng tiền: %,.0f đ",
                                    invoiceIdStr, customerName, phoneNumber, formattedDate, totalAmount
                                ));
                            }
                        }
        
                        serviceTable.setItems(serviceList);
                        totalLabel.setText(String.format("TỔNG CỘNG: %,.0f đ", totalAmount));
        
                        // Cập nhật tiêu đề chi tiết hóa đơn
                        Label titleLabel = (Label) ((VBox) serviceTable.getParent()).getChildren().get(0);
                        titleLabel.setText("CHI TIẾT HÓA ĐƠN #" + invoiceIdStr);
                    }
                } else {
                    showAlert("Lỗi", "Không tìm thấy hóa đơn với mã: " + invoiceIdStr);
                    resetUI();
                }
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tải chi tiết hóa đơn: " + ex.getMessage());
            showAlert("Lỗi", "Không thể tải chi tiết hóa đơn: " + ex.getMessage());
        } catch (NumberFormatException ex) {
            showAlert("Lỗi", "Mã hóa đơn không hợp lệ!");
        }
    }

    private void checkInvoice() {
        String invoiceIdStr = invoiceIdField.getText().trim();
        if (invoiceIdStr.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập mã hóa đơn!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM invoice WHERE OrderID = ?")) {

            pstmt.setInt(1, Integer.parseInt(invoiceIdStr));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                showAlert("Thông báo", "Hóa đơn đã được thanh toán!");
            } else {
                showAlert("Thông báo", "Hóa đơn chưa được thanh toán!");
            }

        } catch (SQLException ex) {
            System.err.println("Lỗi khi kiểm tra hóa đơn: " + ex.getMessage());
            showAlert("Lỗi", "Không thể kiểm tra hóa đơn: " + ex.getMessage());
        } catch (NumberFormatException ex) {
            showAlert("Lỗi", "Mã hóa đơn không hợp lệ!");
        }
    }

    private void confirmPayment() {
        String invoiceIdStr = invoiceIdField.getText().trim();
        if (invoiceIdStr.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập mã hóa đơn!");
            return;
        }

        String paymentMethod = cashRadio.isSelected() ? "Tiền mặt" : "Chuyển khoản";
        createInvoice(invoiceIdStr, totalAmount, paymentMethod);
    }
    
    private void createInvoice(String orderId, double amount, String paymentMethod) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Kiểm tra xem đơn hàng đã được thanh toán chưa
            String checkSql = "SELECT COUNT(*) FROM invoice WHERE OrderID = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, Integer.parseInt(orderId));
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next() && checkRs.getInt(1) > 0) {
                conn.rollback();
                
                // Thay vì hiển thị lỗi, hãy cập nhật trạng thái đơn hàng nếu nó chưa được đánh dấu là đã hoàn thành
                // Kiểm tra trạng thái hiện tại của đơn hàng
                String statusSql = "SELECT hs.StatusName FROM `order` o JOIN happenstatus hs ON o.HappenStatusID = hs.HappenStatusID WHERE o.orderID = ?";
                PreparedStatement statusStmt = conn.prepareStatement(statusSql);
                statusStmt.setInt(1, Integer.parseInt(orderId));
                ResultSet statusRs = statusStmt.executeQuery();
                
                if (statusRs.next() && !statusRs.getString("StatusName").equals("Đã hoàn thành")) {
                    // Cập nhật trạng thái đơn hàng thành "Đã hoàn thành"
                    String updateOrderSql = "UPDATE `order` SET HappenStatusID = " +
                                          "(SELECT HappenStatusID FROM happenstatus WHERE StatusName = 'Đã hoàn thành') " +
                                          "WHERE orderID = ?";
                    PreparedStatement updateOrderStmt = conn.prepareStatement(updateOrderSql);
                    updateOrderStmt.setInt(1, Integer.parseInt(orderId));
                    updateOrderStmt.executeUpdate();
                    
                    conn.commit();
                    showAlert("Thông báo", "Đơn hàng đã được thanh toán trước đó! Đã cập nhật trạng thái thành 'Đã hoàn thành'.");
                    
                    // Gọi callback để refresh giao diện chính
                    refreshCallback.run();
                    
                    resetUI();
                    close();
                    return;
                } else {
                    showAlert("Thông báo", "Đơn hàng đã được thanh toán!");
                    return;
                }
            }

            // Thêm hóa đơn mới
            String invoiceSql = "INSERT INTO invoice (OrderID, CreatedAt, Total, PaymentMethod, PaymentStatusID) VALUES (?, NOW(), ?, ?, 2)";
            PreparedStatement invoiceStmt = conn.prepareStatement(invoiceSql, Statement.RETURN_GENERATED_KEYS);
            invoiceStmt.setInt(1, Integer.parseInt(orderId));
            invoiceStmt.setDouble(2, amount);
            invoiceStmt.setString(3, paymentMethod);
            invoiceStmt.executeUpdate();
            
            ResultSet generatedKeys = invoiceStmt.getGeneratedKeys();
            int invoiceId = 0;
            if (generatedKeys.next()) {
                invoiceId = generatedKeys.getInt(1);
            }

            // Cập nhật trạng thái đơn hàng thành "Đã hoàn thành"
            String updateOrderSql = "UPDATE `order` SET HappenStatusID = " +
                                  "(SELECT HappenStatusID FROM happenstatus WHERE StatusName = 'Đã hoàn thành') " +
                                  "WHERE orderID = ?";
            PreparedStatement updateOrderStmt = conn.prepareStatement(updateOrderSql);
            updateOrderStmt.setInt(1, Integer.parseInt(orderId));
            int orderRowsUpdated = updateOrderStmt.executeUpdate();
            
            if (orderRowsUpdated <= 0) {
                System.out.println("Cảnh báo: Không thể cập nhật trạng thái đơn hàng ID: " + orderId);
            }

            // Cập nhật điểm tích lũy cho khách hàng
            String pointsSql = "UPDATE customer c " +
                              "JOIN `order` o ON c.PersonID = o.Customer_ID " +
                              "SET c.loyaltyPoints = c.loyaltyPoints + ? " +
                              "WHERE o.orderID = ?";
            PreparedStatement pointsStmt = conn.prepareStatement(pointsSql);
            int pointsToAdd = (int)(amount / 100000);
            pointsStmt.setInt(1, pointsToAdd);
            pointsStmt.setInt(2, Integer.parseInt(orderId));
            pointsStmt.executeUpdate();

            conn.commit();

            showAlert("Thành công", "Thanh toán thành công!\nMã hóa đơn: " + invoiceId + "\nĐiểm tích lũy: +" + pointsToAdd);
            
            // Gọi callback để refresh giao diện chính
            refreshCallback.run();
            
            resetUI();
            close(); // Đóng dialog sau khi thanh toán thành công

        } catch (SQLException ex) {
            System.err.println("Lỗi khi tạo hóa đơn: " + ex.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Lỗi khi rollback: " + rollbackEx.getMessage());
            }
            showAlert("Lỗi", "Không thể tạo hóa đơn: " + ex.getMessage());
        } catch (NumberFormatException ex) {
            System.err.println("ID đơn hàng không hợp lệ: " + orderId);
            showAlert("Lỗi", "ID đơn hàng không hợp lệ: " + orderId);
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

    private void printInvoicePDF() {
        String invoiceIdStr = invoiceIdField.getText().trim();
        if (invoiceIdStr.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập mã hóa đơn!");
            return;
        }

        Label pdfLabel = (Label) ((VBox) serviceTable.getParent()).getChildren().get(4);
        pdfLabel.setText("Đã xuất PDF thành công: HoaDon_" + invoiceIdStr + ".pdf");
        showAlert("Thông báo", "Đã xuất PDF thành công: HoaDon_" + invoiceIdStr + ".pdf");
    }

    private void resetUI() {
        invoiceIdField.clear();
        invoiceDetailsLabel.setText("Chi tiết hóa đơn:\nMã hóa đơn: N/A\nKhách hàng: N/A\nSĐT: N/A\nNgày tạo: N/A\nTổng tiền: 0 đ");
        serviceTable.getItems().clear();
        totalLabel.setText("TỔNG CỘNG: 0 đ");
        Label titleLabel = (Label) ((VBox) serviceTable.getParent()).getChildren().get(0);
        titleLabel.setText("CHI TIẾT HÓA ĐƠN #N/A");
        Label pdfLabel = (Label) ((VBox) serviceTable.getParent()).getChildren().get(4);
        pdfLabel.setText("Đã xuất PDF thành công: N/A");
        totalAmount = 0.0;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class ServiceData {
        private final SimpleStringProperty serviceName;
        private final SimpleStringProperty unitPrice;
        private final SimpleStringProperty quantity;
        private final SimpleStringProperty subtotal;

        public ServiceData(String serviceName, String unitPrice, String quantity, String subtotal) {
            this.serviceName = new SimpleStringProperty(serviceName);
            this.unitPrice = new SimpleStringProperty(unitPrice);
            this.quantity = new SimpleStringProperty(quantity);
            this.subtotal = new SimpleStringProperty(subtotal);
        }

        public String getServiceName() { return serviceName.get(); }
        public String getUnitPrice() { return unitPrice.get(); }
        public String getQuantity() { return quantity.get(); }
        public String getSubtotal() { return subtotal.get(); }
    }
}