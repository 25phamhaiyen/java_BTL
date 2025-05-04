package controllers.Staff;

import java.io.File;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import enums.StatusEnum;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Customer;
import model.Invoice;
import model.Order;
import model.Staff;
import repository.InvoiceRepository;
import repository.OrderDetailRepository;
import repository.OrderRepository;
import service.InvoiceService;
import utils.DatabaseConnection;
import utils.RoleChecker;
import utils.Session;

public class InvoiceViewController implements Initializable {

    @FXML private Label dateTimeLabel;
    @FXML private Label staffNameLabel;
    @FXML private DatePicker fromDatePicker;
    @FXML private Button searchButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> paymentMethodFilter;
    @FXML private TableView<Invoice> invoiceTable;
    @FXML private TableColumn<Invoice, Integer> idColumn;
    @FXML private TableColumn<Invoice, Integer> orderIdColumn;
    @FXML private TableColumn<Invoice, String> customerColumn;
    @FXML private TableColumn<Invoice, String> phoneColumn;
    @FXML private TableColumn<Invoice, LocalDateTime> dateColumn;
    @FXML private TableColumn<Invoice, String> serviceColumn;
    @FXML private TableColumn<Invoice, Double> totalColumn;
    @FXML private TableColumn<Invoice, String> paymentMethodColumn;
    @FXML private TableColumn<Invoice, String> statusColumn;
    @FXML private Label totalInvoicesLabel;
    @FXML private Label paidInvoicesLabel;
    @FXML private Label pendingInvoicesLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Button viewDetailsButton;
    @FXML private Button applyDiscountButton;
    @FXML private Button refundButton;
    @FXML private Button processPaymentButton;
    @FXML private Button processPaymentAndPrintButton;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusMessageLabel;
    @FXML private Button homeButton;
    @FXML private Button createBookingButton;

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final InvoiceService invoiceService;

    private ObservableList<Invoice> invoiceList;
    private Invoice selectedInvoice;

    public InvoiceViewController() {
        this.invoiceRepository = InvoiceRepository.getInstance();
        this.orderRepository = OrderRepository.getInstance();
        this.orderDetailRepository = OrderDetailRepository.getInstance();
        this.invoiceService = new InvoiceService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!RoleChecker.hasPermission("VIEW_INVOICE")) {
            showAlert(AlertType.ERROR, "Lỗi", "Không có quyền truy cập",
                    "Bạn không có quyền truy cập vào màn hình quản lý hóa đơn.");
            Stage stage = (Stage) dateTimeLabel.getScene().getWindow();
            stage.close();
            return;
        }

        updateDateTime();
        Staff currentStaff = Session.getCurrentStaff();
        String staffName = currentStaff != null ? currentStaff.getFullName() : "N/A";
        staffNameLabel.setText("Thu ngân: " + staffName);

        initializeTableColumns();
        setupDatePickers();
        setupComboBoxes();
        loadInvoices();

        invoiceTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> handleInvoiceSelection(newValue));

        setupSearchField();
        setupButtonVisibility();
        updateSummaryLabels();

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateDateTime());
            }
        }, 0, 60000);
    }

    private void updateDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");
        dateTimeLabel.setText(LocalDateTime.now().format(formatter));
    }

    private void initializeTableColumns() {
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getInvoiceId()).asObject());

        orderIdColumn.setCellValueFactory(cellData -> {
            Invoice invoice = cellData.getValue();
            Order order = invoice.getOrder();
            return new SimpleIntegerProperty(order != null ? order.getOrderId() : 0).asObject();
        });

        customerColumn.setCellValueFactory(cellData -> {
            Invoice invoice = cellData.getValue();
            Order order = invoice.getOrder();
            Customer customer = (order != null) ? order.getCustomer() : null;
            return new SimpleStringProperty(customer != null && customer.getFullName() != null ? customer.getFullName() : "N/A");
        });

        phoneColumn.setCellValueFactory(cellData -> {
            Invoice invoice = cellData.getValue();
            Order order = invoice.getOrder();
            Customer customer = (order != null) ? order.getCustomer() : null;
            return new SimpleStringProperty(customer != null && customer.getPhone() != null ? customer.getPhone() : "N/A");
        });

        dateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(
                cellData.getValue().getPaymentDate() != null ?
                        cellData.getValue().getPaymentDate().toLocalDateTime() : null));
        dateColumn.setCellFactory(column -> new TableCell<Invoice, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "N/A" : formatter.format(item));
            }
        });

        serviceColumn.setCellValueFactory(cellData -> {
            Invoice invoice = cellData.getValue();
            Order order = invoice.getOrder();
            if (order == null) {
                return new SimpleStringProperty("N/A");
            }
            try {
                String sql = "SELECT s.name " +
                        "FROM order_detail od " +
                        "JOIN service s ON od.service_id = s.service_id " +
                        "WHERE od.order_id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, order.getOrderId());
                    try (ResultSet rs = stmt.executeQuery()) {
                        StringBuilder serviceNames = new StringBuilder();
                        while (rs.next()) {
                            serviceNames.append(rs.getString("name")).append(", ");
                        }
                        String result = serviceNames.length() > 0 ? serviceNames.substring(0, serviceNames.length() - 2) : "Không có dịch vụ";
                        return new SimpleStringProperty(result);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Lỗi khi lấy danh sách dịch vụ: " + e.getMessage());
                return new SimpleStringProperty("Lỗi: " + e.getMessage());
            }
        });

        totalColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(
                cellData.getValue().getTotal() != null ? cellData.getValue().getTotal().doubleValue() : 0).asObject());
        totalColumn.setCellFactory(column -> new TableCell<Invoice, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "0 VND" : String.format("%,.0f VND", item));
            }
        });

        paymentMethodColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getPaymentMethod() != null
                        ? cellData.getValue().getPaymentMethod().name() : "N/A"));

        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStatus() != null
                        ? cellData.getValue().getStatus().name() : "N/A"));
    }

    private void setupDatePickers() {
        StringConverter<LocalDate> dateConverter = new StringConverter<LocalDate>() {
            final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                return date != null ? dateFormatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
            }
        };

        fromDatePicker.setConverter(dateConverter);
        LocalDate today = LocalDate.now();
        fromDatePicker.setValue(today);

        fromDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                loadInvoices();
            }
        });
    }

    private void setupComboBoxes() {
        ObservableList<String> statusList = FXCollections.observableArrayList("Tất cả");
        for (StatusEnum status : StatusEnum.values()) {
            statusList.add(status.name());
        }
        statusFilter.setItems(statusList);
        statusFilter.setValue("Tất cả");

        ObservableList<String> paymentMethodList = FXCollections.observableArrayList("Tất cả");
        for (enums.PaymentMethodEnum method : enums.PaymentMethodEnum.values()) {
            paymentMethodList.add(method.name());
        }
        paymentMethodFilter.setItems(paymentMethodList);
        paymentMethodFilter.setValue("Tất cả");
    }

    private void setupSearchField() {
        searchField.setOnAction(event -> searchInvoices());
    }

    private void setupButtonVisibility() {
        boolean canViewInvoice = RoleChecker.hasPermission("VIEW_INVOICE");
        boolean canManagePayment = RoleChecker.hasPermission("MANAGE_PAYMENT");
        boolean canPrintReceipt = RoleChecker.hasPermission("PRINT_RECEIPT");
        boolean canApplyPromotion = RoleChecker.hasPermission("APPLY_PROMOTION");
        createBookingButton.setVisible(true);
        viewDetailsButton.setVisible(canViewInvoice);
       
        refundButton.setVisible(canManagePayment);
        processPaymentButton.setVisible(canManagePayment);
      
    }

    public void setFromDatePickerToToday() {
        fromDatePicker.setValue(LocalDate.now());
    }

    public void loadInvoices() {
        try {
            progressBar.setVisible(true);
            List<Invoice> invoices = new ArrayList<>();

            if (fromDatePicker.getValue() != null) {
                LocalDateTime startOfDay = fromDatePicker.getValue().atStartOfDay();
                LocalDateTime endOfDay = fromDatePicker.getValue().atTime(23, 59, 59);

                String sql = "SELECT i.*, o.order_id, o.customer_id, o.order_date, o.total_amount, o.status as order_status, " +
                        "c.customer_id, c.point, p.full_name, p.phone, p.email, s.staff_id, sp.full_name as staff_name " +
                        "FROM invoice i " +
                        "LEFT JOIN `order` o ON i.order_id = o.order_id " +
                        "LEFT JOIN customer c ON o.customer_id = c.customer_id " +
                        "LEFT JOIN person p ON c.customer_id = p.person_id " +
                        "LEFT JOIN staff s ON i.staff_id = s.staff_id " +
                        "LEFT JOIN person sp ON s.staff_id = sp.person_id " +
                        "WHERE i.payment_date BETWEEN ? AND ?";

                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setTimestamp(1, java.sql.Timestamp.valueOf(startOfDay));
                    stmt.setTimestamp(2, java.sql.Timestamp.valueOf(endOfDay));
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            Invoice invoice = new Invoice();
                            invoice.setInvoiceId(rs.getInt("invoice_id"));
                            invoice.setSubtotal(rs.getBigDecimal("subtotal"));
                            invoice.setDiscountPercent(rs.getBigDecimal("discount_percent"));
                            invoice.setDiscountAmount(rs.getBigDecimal("discount_amount"));
                            invoice.setPointsUsed(rs.getInt("points_used"));
                            if (rs.wasNull()) {
                                invoice.setPointsUsed(null);
                            }
                            invoice.setPromotionCode(rs.getString("promotion_code"));
                            invoice.setTotal(rs.getBigDecimal("total"));
                            invoice.setAmountPaid(rs.getBigDecimal("amount_paid"));
                            String paymentMethodStr = rs.getString("payment_method");
                            invoice.setPaymentMethod(paymentMethodStr != null ? enums.PaymentMethodEnum.valueOf(paymentMethodStr) : null);
                            invoice.setPaymentDate(rs.getTimestamp("payment_date"));
                            String statusStr = rs.getString("status");
                            invoice.setStatus(statusStr != null ? StatusEnum.valueOf(statusStr) : null);
                            invoice.setNote(rs.getString("note"));

                            Staff staff = new Staff();
                            staff.setId(rs.getInt("staff_id"));
                            staff.setFullName(rs.getString("staff_name"));
                            invoice.setStaff(staff);

                            Order order = new Order();
                            order.setOrderId(rs.getInt("order_id"));
                            order.setOrderDate(rs.getTimestamp("order_date"));
                            order.setTotalAmount(rs.getDouble("total_amount"));
                            order.setStatus(StatusEnum.valueOf(rs.getString("order_status")));

                            Customer customer = new Customer();
                            customer.setId(rs.getInt("customer_id"));
                            customer.setPoint(rs.getInt("point"));
                            customer.setFullName(rs.getString("full_name"));
                            customer.setPhone(rs.getString("phone"));
                            customer.setEmail(rs.getString("email"));
                            order.setCustomer(customer);

                            invoice.setOrder(order);
                            invoices.add(invoice);
                        }
                    }
                }
            } else {
                LocalDate today = LocalDate.now();
                fromDatePicker.setValue(today);
                return;
            }

            invoiceList = FXCollections.observableArrayList(invoices);
            invoiceTable.setItems(invoiceList);
            applyFilters();
            updateSummaryLabels();
            progressBar.setVisible(false);

            LocalDate displayDate = fromDatePicker.getValue();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            statusMessageLabel.setText("Đã tải " + invoices.size() + " hóa đơn ngày " + displayDate.format(formatter));
        } catch (Exception e) {
            progressBar.setVisible(false);
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải danh sách hóa đơn", e.getMessage());
            statusMessageLabel.setText("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleInvoiceSelection(Invoice invoice) {
        selectedInvoice = invoice;
        boolean hasSelectedInvoice = invoice != null;

        viewDetailsButton.setDisable(!hasSelectedInvoice);
//        applyDiscountButton.setDisable(!hasSelectedInvoice);
        refundButton.setDisable(!hasSelectedInvoice ||
                (hasSelectedInvoice && !StatusEnum.COMPLETED.equals(invoice.getStatus())));
        processPaymentButton.setDisable(!hasSelectedInvoice ||
                (hasSelectedInvoice && !StatusEnum.PENDING.equals(invoice.getStatus())));
//        processPaymentAndPrintButton.setDisable(!hasSelectedInvoice ||
//                (hasSelectedInvoice && !StatusEnum.PENDING.equals(invoice.getStatus())));
    }

    private void applyFilters() {
        String statusValue = statusFilter.getValue();
        String paymentMethodValue = paymentMethodFilter.getValue();

        if (statusValue == null || statusValue.equals("Tất cả") &&
                (paymentMethodValue == null || paymentMethodValue.equals("Tất cả"))) {
            invoiceTable.setItems(invoiceList);
            updateSummaryLabels();
            return;
        }

        ObservableList<Invoice> filteredList = FXCollections.observableArrayList();

        for (Invoice invoice : invoiceList) {
            boolean statusMatch = statusValue == null || statusValue.equals("Tất cả") ||
                    (invoice.getStatus() != null &&
                            invoice.getStatus().name().equals(statusValue));

            boolean paymentMethodMatch = paymentMethodValue == null || paymentMethodValue.equals("Tất cả") ||
                    (invoice.getPaymentMethod() != null &&
                            invoice.getPaymentMethod().name().equals(paymentMethodValue));

            if (statusMatch && paymentMethodMatch) {
                filteredList.add(invoice);
            }
        }

        invoiceTable.setItems(filteredList);
        updateSummaryLabels(filteredList);
    }

    private void updateSummaryLabels() {
        updateSummaryLabels(invoiceTable.getItems());
    }

    private void updateSummaryLabels(ObservableList<Invoice> invoices) {
        int totalCount = invoices.size();
        int paidCount = 0;
        int pendingCount = 0;
        double totalRevenue = 0;

        for (Invoice invoice : invoices) {
            if (StatusEnum.COMPLETED.equals(invoice.getStatus())) {
                paidCount++;
                if (invoice.getTotal() != null) {
                    totalRevenue += invoice.getTotal().doubleValue();
                }
            } else if (StatusEnum.PENDING.equals(invoice.getStatus()) || StatusEnum.CANCELLED.equals(invoice.getStatus())) {
                pendingCount++;
            }
        }

        totalInvoicesLabel.setText(String.valueOf(totalCount));
        paidInvoicesLabel.setText(String.valueOf(paidCount));
        pendingInvoicesLabel.setText(String.valueOf(pendingCount));
        totalRevenueLabel.setText(String.format("%,.0f VND", totalRevenue));
    }

    @FXML
    private void searchInvoices() {
        if (fromDatePicker.getValue() == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Ngày không hợp lệ",
                    "Vui lòng chọn ngày để tìm kiếm.");
            return;
        }

        String searchQuery = searchField.getText().trim().toLowerCase();

        ObservableList<Invoice> filteredList = FXCollections.observableArrayList();

        for (Invoice invoice : invoiceList) {
            boolean matchesFilter = searchQuery.isEmpty() ||
                    String.valueOf(invoice.getInvoiceId()).contains(searchQuery) ||
                    (invoice.getOrder() != null &&
                            String.valueOf(invoice.getOrder().getOrderId()).contains(searchQuery)) ||
                    (invoice.getOrder() != null && invoice.getOrder().getCustomer() != null &&
                            invoice.getOrder().getCustomer().getFullName().toLowerCase().contains(searchQuery)) ||
                    (invoice.getOrder() != null && invoice.getOrder().getCustomer() != null &&
                            invoice.getOrder().getCustomer().getPhone().toLowerCase().contains(searchQuery));

            if (matchesFilter) {
                boolean statusMatch = statusFilter.getValue().equals("Tất cả") ||
                        (invoice.getStatus() != null &&
                                invoice.getStatus().name().equals(statusFilter.getValue()));

                boolean paymentMethodMatch = paymentMethodFilter.getValue().equals("Tất cả") ||
                        (invoice.getPaymentMethod() != null &&
                                invoice.getPaymentMethod().name().equals(paymentMethodFilter.getValue()));

                if (statusMatch && paymentMethodMatch) {
                    filteredList.add(invoice);
                }
            }
        }

        invoiceTable.setItems(filteredList);
        updateSummaryLabels(filteredList);
        statusMessageLabel.setText("Tìm thấy " + filteredList.size() + " kết quả");
    }

    @FXML
    private void onSearchButtonClick() {
        if (fromDatePicker.getValue() == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Chưa chọn ngày",
                    "Vui lòng chọn ngày để tìm kiếm.");
            return;
        }

        loadInvoices();

        String searchQuery = searchField.getText().trim().toLowerCase();
        if (!searchQuery.isEmpty()) {
            searchInvoices();
        }
    }

    @FXML
    private void viewDetails() {
        if (selectedInvoice == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Không có hóa đơn được chọn",
                    "Vui lòng chọn một hóa đơn để xem chi tiết.");
            return;
        }

        displayInvoiceDetails(selectedInvoice, true, false);
    }

    // Phương thức hiển thị chi tiết hóa đơn
    private void displayInvoiceDetails(Invoice invoice, boolean showPrintButton, boolean fromPayment) {
        try {
            Stage detailStage = new Stage();
            detailStage.setTitle("Chi tiết hóa đơn #" + invoice.getInvoiceId());

            VBox root = new VBox(10);
            root.setPadding(new Insets(20));
            root.setAlignment(Pos.CENTER);

            Label storeNameLabel = new Label("PET CARE CENTER");
            storeNameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

            Label storeAddressLabel = new Label("Địa chỉ: 123 Đường ABC, Quận XYZ, TP. HCM");
            Label storePhoneLabel = new Label("Điện thoại: (028) 1234 5678");

            Separator sep1 = new Separator();

            Label invoiceHeaderLabel = new Label("HÓA ĐƠN BÁN HÀNG");
            invoiceHeaderLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            String sql = "SELECT i.invoice_id, i.payment_date, i.subtotal, i.discount_amount, " +
                    "i.points_used, i.total, i.amount_paid, i.payment_method, " +
                    "c.customer_id, p.full_name AS customer_name, p.phone, " +
                    "s.staff_id, sp.full_name AS staff_name " +
                    "FROM invoice i " +
                    "JOIN `order` o ON i.order_id = o.order_id " +
                    "JOIN customer c ON o.customer_id = c.customer_id " +
                    "JOIN person p ON c.customer_id = p.person_id " +
                    "JOIN staff s ON i.staff_id = s.staff_id " +
                    "JOIN person sp ON s.staff_id = sp.person_id " +
                    "WHERE i.invoice_id = ?";

            GridPane infoGrid = new GridPane();
            infoGrid.setHgap(15);
            infoGrid.setVgap(10);
            infoGrid.setPadding(new Insets(10, 0, 10, 0));

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, invoice.getInvoiceId());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        infoGrid.add(new Label("Số hóa đơn:"), 0, 0);
                        infoGrid.add(new Label("#" + invoice.getInvoiceId()), 1, 0);

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                        LocalDateTime paymentDate = rs.getTimestamp("payment_date").toLocalDateTime();

                        infoGrid.add(new Label("Ngày:"), 0, 1);
                        infoGrid.add(new Label(paymentDate.format(formatter)), 1, 1);

                        infoGrid.add(new Label("Thu ngân:"), 0, 2);
                        infoGrid.add(new Label(rs.getString("staff_name")), 1, 2);

                        infoGrid.add(new Label("Khách hàng:"), 2, 0);
                        infoGrid.add(new Label(rs.getString("customer_name")), 3, 0);

                        infoGrid.add(new Label("Số điện thoại:"), 2, 1);
                        infoGrid.add(new Label(rs.getString("phone")), 3, 1);

                        infoGrid.add(new Label("Mã KH:"), 2, 2);
                        infoGrid.add(new Label("KH-" + String.format("%05d", rs.getInt("customer_id"))), 3, 2);
                    }
                }
            }

            Separator sep2 = new Separator();

            TableView<InvoiceDetailItem> detailTable = new TableView<>();
            detailTable.setPrefHeight(200);

            TableColumn<InvoiceDetailItem, Integer> sttCol = new TableColumn<>("STT");
            sttCol.setCellValueFactory(new PropertyValueFactory<>("stt"));
            sttCol.setPrefWidth(50);

            TableColumn<InvoiceDetailItem, String> nameCol = new TableColumn<>("Tên dịch vụ");
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            nameCol.setPrefWidth(300);

            TableColumn<InvoiceDetailItem, Integer> qtyCol = new TableColumn<>("SL");
            qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            qtyCol.setPrefWidth(50);

            TableColumn<InvoiceDetailItem, Double> priceCol = new TableColumn<>("Đơn giá");
            priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
            priceCol.setPrefWidth(100);
            priceCol.setCellFactory(tc -> new TableCell<InvoiceDetailItem, Double>() {
                @Override
                protected void updateItem(Double price, boolean empty) {
                    super.updateItem(price, empty);
                    if (empty || price == null) {
                        setText(null);
                    } else {
                        setText(String.format("%,.0f VND", price));
                    }
                }
            });

            TableColumn<InvoiceDetailItem, Double> totalCol = new TableColumn<>("Thành tiền");
            totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
            totalCol.setPrefWidth(120);
            totalCol.setCellFactory(tc -> new TableCell<InvoiceDetailItem, Double>() {
                @Override
                protected void updateItem(Double total, boolean empty) {
                    super.updateItem(total, empty);
                    if (empty || total == null) {
                        setText(null);
                    } else {
                        setText(String.format("%,.0f VND", total));
                    }
                }
            });

            detailTable.getColumns().addAll(sttCol, nameCol, qtyCol, priceCol, totalCol);

            ObservableList<InvoiceDetailItem> detailItems = FXCollections.observableArrayList();

            String detailSql = "SELECT s.name, od.quantity, od.price " +
                    "FROM order_detail od " +
                    "JOIN service s ON od.service_id = s.service_id " +
                    "WHERE od.order_id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(detailSql)) {
                stmt.setInt(1, invoice.getOrder().getOrderId());
                try (ResultSet rs = stmt.executeQuery()) {
                    int stt = 1;
                    while (rs.next()) {
                        String name = rs.getString("name");
                        int quantity = rs.getInt("quantity");
                        double price = rs.getDouble("price");
                        double total = quantity * price;

                        detailItems.add(new InvoiceDetailItem(stt++, name, quantity, price, total));
                    }
                }
            }

            detailTable.setItems(detailItems);

            final double[] subtotal = {0};
            final double[] discount = {0};
            final int[] pointsUsed = {0};
            final double[] pointsValue = {0};
            final double[] grandTotal = {0};
            final double[] amountPaid = {0};
            final double[] change = {0};
            final String[] paymentMethod = {""};

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT subtotal, discount_amount, points_used, total, amount_paid, payment_method " +
                                 "FROM invoice WHERE invoice_id = ?")) {
                stmt.setInt(1, invoice.getInvoiceId());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        subtotal[0] = rs.getDouble("subtotal");
                        discount[0] = rs.getDouble("discount_amount");
                        pointsUsed[0] = rs.getInt("points_used");
                        pointsValue[0] = pointsUsed[0] * 1000;
                        grandTotal[0] = rs.getDouble("total");
                        amountPaid[0] = rs.getDouble("amount_paid");
                        change[0] = amountPaid[0] - grandTotal[0];
                        paymentMethod[0] = rs.getString("payment_method");
                    }
                }
            }

            GridPane summaryGrid = new GridPane();
            summaryGrid.setHgap(10);
            summaryGrid.setVgap(5);
            summaryGrid.setPadding(new Insets(10, 0, 10, 0));
            summaryGrid.setAlignment(Pos.CENTER_RIGHT);

            Label subtotalLabel = new Label("Tổng tiền hàng:");
            Label discountLabel = new Label("Giảm giá:");
            Label pointsLabel = new Label("Điểm quy đổi:");
            Label grandTotalLabel = new Label("Tổng cộng:");
            Label amountPaidLabel = new Label("Tiền khách trả:");
            Label changeLabel = new Label("Tiền thối lại:");
            Label paymentMethodLabel = new Label("Phương thức thanh toán:");

            grandTotalLabel.setStyle("-fx-font-weight: bold;");

            Label subtotalValue = new Label(String.format("%,.0f VND", subtotal[0]));
            HBox discountBox = new HBox(5);
            TextField discountCodeField = new TextField();
            discountCodeField.setPromptText("Nhập mã KM");
            discountCodeField.setPrefWidth(100);
            Label discountValue = new Label(String.format("%,.0f VND", discount[0]));
            Button applyDiscountButton = new Button("Áp dụng");
            Label grandTotalValue = new Label(String.format("%,.0f VND", grandTotal[0]));
            Label changeValue = new Label(String.format("%,.0f VND", change[0]));
            applyDiscountButton.setOnAction(e -> {
                String code = discountCodeField.getText().trim();
                if (code.isEmpty()) {
                    showAlert(AlertType.WARNING, "Cảnh báo", "Chưa nhập mã khuyến mãi",
                            "Vui lòng nhập mã khuyến mãi để áp dụng.");
                    return;
                }

                try {
                    double discountPercent = 0;
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(
                        		 "SELECT discount_percent FROM promotion WHERE code = ? AND start_date <= NOW() AND end_date >= NOW()")) {
                        stmt.setString(1, code);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                discountPercent = rs.getDouble("discount_percent");
                            } else {
                                showAlert(AlertType.WARNING, "Cảnh báo", "Mã khuyến mãi không hợp lệ",
                                        "Mã khuyến mãi không tồn tại hoặc đã hết hạn.");
                                return;
                            }
                        }
                    }

                    discount[0] = subtotal[0] * discountPercent / 100;
                    grandTotal[0] = subtotal[0] - discount[0];
                    change[0] = amountPaid[0] - grandTotal[0];

                    String updateInvoiceSql = "UPDATE invoice SET promotion_code = ?, discount_percent = ?, discount_amount = ?, total = ? WHERE invoice_id = ?";
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(updateInvoiceSql)) {
                        stmt.setString(1, code);
                        stmt.setDouble(2, discountPercent);
                        stmt.setDouble(3, discount[0]);
                        stmt.setDouble(4, grandTotal[0]);
                        stmt.setInt(5, invoice.getInvoiceId());
                        stmt.executeUpdate();
                    }

                    discountValue.setText(String.format("%,.0f VND", discount[0]));
                    grandTotalValue.setText(String.format("%,.0f VND", grandTotal[0]));
                    changeValue.setText(String.format("%,.0f VND", change[0]));

                    showAlert(AlertType.INFORMATION, "Thành công", "Đã áp dụng mã khuyến mãi",
                            "Mã khuyến mãi " + code + " đã được áp dụng với " + discountPercent + "% giảm giá.");

                } catch (SQLException ex) {
                    showAlert(AlertType.ERROR, "Lỗi", "Không thể áp dụng mã khuyến mãi", ex.getMessage());
                }
            });

            discountBox.getChildren().addAll(discountValue, discountCodeField, applyDiscountButton);

            Label pointsValue1 = new Label(String.format("%,d điểm (%,.0f VND)", pointsUsed[0], pointsValue[0]));
            grandTotalValue.setStyle("-fx-font-weight: bold;");
         // Thay đổi hiển thị tiền khách trả từ Label thành TextField khi là CASH
            HBox amountPaidBox = new HBox(5);
            amountPaidBox.setAlignment(Pos.CENTER_RIGHT);

            // Trường nhập tiền khách trả
            TextField amountPaidField = new TextField();
            amountPaidField.setText(String.format("%,.0f", amountPaid[0]));
            amountPaidField.setPrefWidth(120);
			amountPaidField.setPromptText("Nhập tiền khách trả");
            // Chỉ cho phép nhập khi thanh toán CASH và đang ở trạng thái PENDING
            boolean isCash = "CASH".equals(paymentMethod[0]);
            boolean isPending = invoice.getStatus() == StatusEnum.PENDING;
            amountPaidField.setDisable(!(isCash && isPending));

            // Thêm sự kiện cập nhật tiền thối khi tiền khách trả thay đổi
            amountPaidField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    // Chuyển định dạng số có dấu phẩy thành số thường
                    String normalizedValue = newValue.replaceAll("[,.]", "");
                    double newAmountPaid = Double.parseDouble(normalizedValue);
                    change[0] = newAmountPaid - grandTotal[0];
                    changeValue.setText(String.format("%,.0f VND", change[0]));
                    
                    // Cập nhật giá trị trong mảng (để lưu lại nếu cần)
                    amountPaid[0] = newAmountPaid;
                } catch (NumberFormatException ex) {
                    // Giữ nguyên giá trị cũ nếu định dạng không hợp lệ
                    amountPaidField.setText(oldValue);
                }
            });

            // Thêm đơn vị VND
            Label vndLabel = new Label("VND");
            amountPaidBox.getChildren().addAll(amountPaidField, vndLabel);

            // Thay đổi hiển thị phương thức thanh toán từ Label thành ComboBox
            ComboBox<String> paymentMethodComboBox = new ComboBox<>();
            paymentMethodComboBox.getItems().addAll("CASH", "CARD", "MOMO", "BANKING");
            paymentMethodComboBox.setValue(paymentMethod[0] != null ? paymentMethod[0] : "CASH");
            paymentMethodComboBox.setDisable(!isPending); // Chỉ có thể thay đổi khi đang PENDING

            // Cập nhật trạng thái field tiền khách trả khi thay đổi phương thức thanh toán
            paymentMethodComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                boolean isNewCash = "CASH".equals(newValue);
                amountPaidField.setDisable(!(isNewCash && isPending));
                
                // Nếu không phải CASH, tự động điền số tiền bằng tổng cộng
                if (!isNewCash) {
                    amountPaidField.setText(String.format("%,.0f", grandTotal[0]));
                    change[0] = 0;
                    changeValue.setText("0 VND");
                    amountPaid[0] = grandTotal[0];
                }
                
                // Cập nhật giá trị trong mảng
                paymentMethod[0] = newValue;
            });

            // Thêm các thành phần vào summaryGrid
            summaryGrid.add(subtotalLabel, 0, 0);
            summaryGrid.add(subtotalValue, 1, 0);
            summaryGrid.add(discountLabel, 0, 1);
            summaryGrid.add(discountBox, 1, 1);
            summaryGrid.add(pointsLabel, 0, 2);
            summaryGrid.add(pointsValue1, 1, 2);
            summaryGrid.add(grandTotalLabel, 0, 3);
            summaryGrid.add(grandTotalValue, 1, 3);
            summaryGrid.add(amountPaidLabel, 0, 4);
            summaryGrid.add(amountPaidBox, 1, 4); // Thay thế amountPaidValue
            summaryGrid.add(changeLabel, 0, 5);
            summaryGrid.add(changeValue, 1, 5);
            summaryGrid.add(paymentMethodLabel, 0, 6);
            summaryGrid.add(paymentMethodComboBox, 1, 6); // Thay thế paymentMethodValue

            ColumnConstraints col1 = new ColumnConstraints();
            col1.setHalignment(HPos.RIGHT);
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setHalignment(HPos.RIGHT);
            col2.setPrefWidth(200);
            summaryGrid.getColumnConstraints().addAll(col1, col2);

            Separator sep3 = new Separator();

            Label thanksLabel = new Label("Cảm ơn quý khách đã sử dụng dịch vụ!");
            thanksLabel.setStyle("-fx-font-style: italic;");

            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER);

            if (showPrintButton) {
                Button printButton = new Button("In hóa đơn");
                printButton.setOnAction(e -> reprintInvoice());
                buttonBox.getChildren().add(printButton);
            }

            if (fromPayment && invoice.getStatus() == StatusEnum.PENDING) {
                Button paymentButton = new Button("Thanh toán");
                paymentButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                paymentButton.setOnAction(e -> {
                    try {
                        int orderId = invoice.getOrder().getOrderId();
                        int invoiceId = invoice.getInvoiceId();

                        // Kiểm tra tiền khách trả khi thanh toán bằng CASH
                        if ("CASH".equals(paymentMethod[0]) && amountPaid[0] < grandTotal[0]) {
                            showAlert(AlertType.WARNING, "Cảnh báo", "Tiền khách trả không đủ",
                                    "Tiền khách trả phải lớn hơn hoặc bằng tổng cộng.");
                            return;
                        }

                        String updateInvoiceSql = "UPDATE invoice SET payment_date = ?, status = ?, payment_method = ?, amount_paid = ? WHERE invoice_id = ?";
                        try (Connection conn = DatabaseConnection.getConnection();
                             PreparedStatement stmt = conn.prepareStatement(updateInvoiceSql)) {
                            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                            stmt.setString(2, StatusEnum.COMPLETED.name());
                            stmt.setString(3, paymentMethod[0]); // Lấy phương thức thanh toán từ ComboBox
                            stmt.setBigDecimal(4, new java.math.BigDecimal(amountPaid[0])); // Lấy số tiền từ TextField
                            stmt.setInt(5, invoiceId);
                            stmt.executeUpdate();
                        }

                        showAlert(AlertType.INFORMATION, "Thành công", "Thanh toán thành công",
                                "Hóa đơn #" + invoiceId + " đã được thanh toán.");
                        loadInvoices();
                        detailStage.close();
                    } catch (SQLException ex) {
                        showAlert(AlertType.ERROR, "Lỗi", "Không thể thực hiện thanh toán", ex.getMessage());
                    }
                });

                Button paymentAndPrintButton = new Button("Thanh toán và in");
                paymentAndPrintButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                paymentAndPrintButton.setOnAction(e -> {
                    try {
                        int orderId = invoice.getOrder().getOrderId();
                        int invoiceId = invoice.getInvoiceId();

                        // Kiểm tra tiền khách trả khi thanh toán bằng CASH
                        if ("CASH".equals(paymentMethod[0]) && amountPaid[0] < grandTotal[0]) {
                            showAlert(AlertType.WARNING, "Cảnh báo", "Tiền khách trả không đủ",
                                    "Tiền khách trả phải lớn hơn hoặc bằng tổng cộng.");
                            return;
                        }

                        String updateInvoiceSql = "UPDATE invoice SET payment_date = ?, status = ?, payment_method = ?, amount_paid = ? WHERE invoice_id = ?";
                        try (Connection conn = DatabaseConnection.getConnection();
                             PreparedStatement stmt = conn.prepareStatement(updateInvoiceSql)) {
                            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                            stmt.setString(2, StatusEnum.COMPLETED.name());
                            stmt.setString(3, paymentMethod[0]); // Lấy phương thức thanh toán từ ComboBox
                            stmt.setBigDecimal(4, new java.math.BigDecimal(amountPaid[0])); // Lấy số tiền từ TextField
                            stmt.setInt(5, invoiceId);
                            stmt.executeUpdate();
                        }

                        String fileName = "invoice_" + invoiceId + ".pdf";
                        invoiceService.generateInvoicePDF(orderId, fileName);

                        File file = new File(fileName);
                        if (file.exists()) {
                            java.awt.Desktop.getDesktop().open(file);
                        } else {
                            showAlert(AlertType.ERROR, "Lỗi", "Không thể mở file hóa đơn",
                                    "File không tồn tại: " + fileName);
                            return;
                        }

                        showAlert(AlertType.INFORMATION, "Thành công", "Thanh toán và in thành công",
                                "Hóa đơn #" + invoiceId + " đã được thanh toán và in.");
                        loadInvoices();
                        detailStage.close();
                    } catch (Exception ex) {
                        showAlert(AlertType.ERROR, "Lỗi", "Không thể thực hiện thanh toán và in", ex.getMessage());
                    }
                });

                buttonBox.getChildren().addAll(paymentButton, paymentAndPrintButton);
            }

            Button closeButton = new Button("Đóng");
            closeButton.setOnAction(e -> detailStage.close());

            buttonBox.getChildren().add(closeButton);

            root.getChildren().addAll(
                    storeNameLabel,
                    storeAddressLabel,
                    storePhoneLabel,
                    sep1,
                    invoiceHeaderLabel,
                    infoGrid,
                    sep2,
                    detailTable,
                    summaryGrid,
                    sep3,
                    thanksLabel,
                    buttonBox
            );

            Scene scene = new Scene(root, 700, 600);
            detailStage.setScene(scene);
            detailStage.show();

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể hiển thị chi tiết hóa đơn", e.getMessage());
            e.printStackTrace();
        }
    }

    private void reprintInvoice() {
        try {
            String fileName = "invoice_" + selectedInvoice.getInvoiceId() + ".pdf";
            invoiceService.generateInvoicePDF(selectedInvoice.getOrder().getOrderId(), fileName);

            File file = new File(fileName);
            if (file.exists()) {
                java.awt.Desktop.getDesktop().open(file);
                showAlert(AlertType.INFORMATION, "Thành công", "Đã mở file hóa đơn",
                        "Hóa đơn đã được mở bằng ứng dụng mặc định.");
            } else {
                showAlert(AlertType.ERROR, "Lỗi", "Không thể mở file hóa đơn",
                        "File không tồn tại: " + fileName);
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể in hóa đơn", e.getMessage());
        }
    }

    @FXML
    private void applyDiscount() {
        if (selectedInvoice == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Không có hóa đơn được chọn",
                    "Vui lòng chọn một hóa đơn để áp dụng khuyến mãi.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Áp dụng khuyến mãi");
        dialog.setHeaderText("Nhập mã khuyến mãi");
        dialog.setContentText("Mã khuyến mãi:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(code -> {
            try {
                double discountPercent = 0;
      
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "SELECT discount_percent FROM promotion WHERE code = ? AND start_date <= NOW() AND end_date >= NOW()")) {
                    stmt.setString(1, code);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            discountPercent = rs.getDouble("discount_percent");
                        } else {
                            showAlert(AlertType.WARNING, "Cảnh báo", "Mã khuyến mãi không hợp lệ",
                                    "Mã khuyến mãi không tồn tại hoặc đã hết hạn.");
                            return;
                        }
                    }
                }
                double subtotal;
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "SELECT subtotal FROM invoice WHERE invoice_id = ?")) {
                    stmt.setInt(1, selectedInvoice.getInvoiceId());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            subtotal = rs.getDouble("subtotal");
                        } else {
                            throw new SQLException("Không tìm thấy hóa đơn: " + selectedInvoice.getInvoiceId());
                        }
                    }
                }

                double discountAmount = subtotal * discountPercent / 100;
                double newTotal = subtotal - discountAmount;

                String updateInvoiceSql = "UPDATE invoice SET promotion_code = ?, discount_percent = ?, discount_amount = ?, total = ? WHERE invoice_id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(updateInvoiceSql)) {
                    stmt.setString(1, code);
                    stmt.setDouble(2, discountPercent);
                    stmt.setDouble(3, discountAmount);
                    stmt.setDouble(4, newTotal);
                    stmt.setInt(5, selectedInvoice.getInvoiceId());
                    stmt.executeUpdate();
                }

                showAlert(AlertType.INFORMATION, "Thành công", "Đã áp dụng khuyến mãi",
                        "Đã áp dụng mã khuyến mãi " + code + " với " + discountPercent + "% giảm giá.");

                loadInvoices();
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Lỗi", "Không thể áp dụng khuyến mãi", e.getMessage());
            }
        });
    }

    @FXML
    private void processPayment() {
        processPaymentLogic(false);
    }

    @FXML
    private void processPaymentAndPrint() {
        processPaymentLogic(true);
    }

    private void processPaymentLogic(boolean print) {
        if (selectedInvoice == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Không có hóa đơn được chọn",
                    "Vui lòng chọn một hóa đơn để xử lý thanh toán.");
            return;
        }

        if (!StatusEnum.PENDING.equals(selectedInvoice.getStatus())) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Hóa đơn không thể thanh toán",
                    "Chỉ có thể thanh toán cho hóa đơn ở trạng thái chờ.");
            return;
        }

        displayInvoiceDetails(selectedInvoice, false, true);
    }

    @FXML
    private void processRefund() {
        if (selectedInvoice == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Không có hóa đơn được chọn",
                    "Vui lòng chọn một hóa đơn để hoàn tiền.");
            return;
        }

        if (!StatusEnum.COMPLETED.equals(selectedInvoice.getStatus())) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Hóa đơn chưa thanh toán",
                    "Chỉ có thể hoàn tiền cho hóa đơn đã thanh toán.");
            return;
        }

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận hoàn tiền");
        alert.setHeaderText("Bạn có chắc chắn muốn hoàn tiền?");
        alert.setContentText("Hoàn tiền cho hóa đơn #" + selectedInvoice.getInvoiceId() +
                " với số tiền " + selectedInvoice.getTotal() + " VND?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String updateInvoiceSql = "UPDATE invoice SET status = ? WHERE invoice_id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(updateInvoiceSql)) {
                    stmt.setString(1, StatusEnum.CANCELLED.name());
                    stmt.setInt(2, selectedInvoice.getInvoiceId());
                    stmt.executeUpdate();
                }

                String updateOrderSql = "UPDATE `order` SET status = ? WHERE order_id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(updateOrderSql)) {
                    stmt.setString(1, StatusEnum.CANCELLED.name());
                    stmt.setInt(2, selectedInvoice.getOrder().getOrderId());
                    stmt.executeUpdate();
                }

                Integer pointsUsed = selectedInvoice.getPointsUsed();
                if (pointsUsed != null && pointsUsed > 0) {
                    String updatePointsSql = "UPDATE customer SET point = point + ? WHERE customer_id = ?";
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(updatePointsSql)) {
                        stmt.setInt(1, pointsUsed);
                        stmt.setInt(2, selectedInvoice.getOrder().getCustomer().getId());
                        stmt.executeUpdate();
                    }
                }

                showAlert(AlertType.INFORMATION, "Thành công", "Đã hoàn tiền",
                        "Hóa đơn đã được đánh dấu là đã hoàn tiền.");

                loadInvoices();
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Lỗi", "Không thể hoàn tiền", e.getMessage());
            }
        }
    }

    @FXML
    private void createBooking() {
        try {
            if (!RoleChecker.hasPermission("CREATE_BOOKING")) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Không có quyền",
                        "Bạn không có quyền tạo lịch hẹn mới.");
                return;
            }

            if (selectedInvoice != null &&
                    selectedInvoice.getOrder() != null &&
                    selectedInvoice.getOrder().getCustomer() != null) {
                Session.getInstance().setAttribute("selectedCustomerId",
                        selectedInvoice.getOrder().getCustomer().getId());
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/NewBookingView.fxml"));
            Parent root = loader.load();

            NewBookingController newBookingController = loader.getController();
            newBookingController.setInvoiceViewController(this);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle("Tạo lịch hẹn mới");
            modalStage.setScene(new Scene(root));
            modalStage.showAndWait();

            Session.getInstance().removeAttribute("selectedCustomerId");

            statusMessageLabel.setText("Đã mở form đặt lịch hẹn mới.");

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể mở màn hình đặt lịch", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void resetFilter() {
        fromDatePicker.setValue(LocalDate.now());
        statusFilter.setValue("Tất cả");
        paymentMethodFilter.setValue("Tất cả");
        searchField.clear();
        loadInvoices();
    }

    @FXML
    private void showHelp() {
        Alert helpAlert = new Alert(AlertType.INFORMATION);
        helpAlert.setTitle("Trợ giúp");
        helpAlert.setHeaderText("Hướng dẫn sử dụng màn hình Quản lý hóa đơn");

        String helpContent =
                "1. Xem danh sách hóa đơn: Chọn ngày và nhấn 'Tìm kiếm' để xem danh sách hóa đơn.\n\n" +
                "2. Tìm kiếm hóa đơn: Nhập mã hóa đơn, mã đơn hàng, tên khách hàng hoặc số điện thoại vào ô tìm kiếm.\n\n" +
                "3. Lọc hóa đơn: Sử dụng bộ lọc trạng thái và phương thức thanh toán để thu hẹp danh sách.\n\n" +
                "4. Đặt lịch hẹn mới: Nhấn 'Đặt lịch mới' để mở form đặt lịch hẹn.\n\n" +
                "5. Xem chi tiết: Nhấn 'Xem chi tiết' để xem thông tin chi tiết của hóa đơn và in lại nếu cần.\n\n" +
                "6. Thanh toán: Chọn hóa đơn chờ thanh toán và nhấn 'Thanh toán' hoặc 'Thanh toán và in'.\n\n" +
                "7. Hoàn tiền: Chọn hóa đơn đã thanh toán và nhấn 'Hoàn tiền' để thực hiện hoàn tiền.\n\n" +
                "Để được hỗ trợ thêm, vui lòng liên hệ quản trị viên.";

        helpAlert.setContentText(helpContent);
        helpAlert.showAndWait();
    }

    @FXML
    private void exitApplication() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Staff/MainStaffView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) searchButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi chuyển về màn hình chính: " + e.getMessage());
            Stage stage = (Stage) searchButton.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) homeButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi chuyển về màn hình chính: " + e.getMessage());
            showAlert(AlertType.ERROR, "Lỗi", "Không thể chuyển về trang chủ",
                    "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    private void showAlert(AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class InvoiceDetailItem {
        private final int stt;
        private final String name;
        private final int quantity;
        private final double price;
        private final double total;

        public InvoiceDetailItem(int stt, String name, int quantity, double price, double total) {
            this.stt = stt;
            this.name = name;
            this.quantity = quantity;
            this.price = price;
            this.total = total;
        }

        public int getStt() {
            return stt;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPrice() {
            return price;
        }

        public double getTotal() {
            return total;
        }
    }
}