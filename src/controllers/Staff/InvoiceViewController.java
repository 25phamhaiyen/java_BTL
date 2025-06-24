package controllers.Staff;

import java.io.File;
import java.math.BigDecimal;
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
import javafx.scene.layout.GridPane;

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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
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
import utils.PaymentLogger;
import utils.RoleChecker;
import utils.Session;
import utils.LanguageChangeListener;
import utils.LanguageManagerStaff;
import enums.PaymentMethodEnum;
import enums.StatusEnum;
import java.io.IOException;
import java.awt.Desktop;

/**
 * Controller cho giao diện quản lý hóa đơn
 */
public class InvoiceViewController implements Initializable, LanguageChangeListener {

    // Header Labels
    @FXML private Label headerTitle;
    @FXML private Label dateTimeLabel;
    @FXML private Label staffNameLabel;
    
    // Filter Labels
    @FXML private Label dateFilterLabel;
    @FXML private Label statusFilterLabel;
    @FXML private Label paymentMethodFilterLabel;
    @FXML private Label searchLabel;
    
    // Summary Labels
    @FXML private Label totalInvoicesTextLabel;
    @FXML private Label paidInvoicesTextLabel;
    @FXML private Label pendingInvoicesTextLabel;
    @FXML private Label totalRevenueTextLabel;
    
    // Existing controls
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
    @FXML private Button qrPaymentButton;
    @FXML private Button resetFilterButton;
    @FXML private Button helpButton;
    @FXML private Button exitButton;

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
        this.invoiceService = InvoiceService.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Register for language change notifications
        LanguageManagerStaff.addListener(this);
        
        if (!RoleChecker.hasPermission("VIEW_INVOICE")) {
            showAlert(AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("invoice.error.noPermission"),
                LanguageManagerStaff.getString("invoice.error.noPermissionMessage"));
            Stage stage = (Stage) dateTimeLabel.getScene().getWindow();
            stage.close();
            return;
        }

        // Initialize UI text
        loadTexts();
        
        updateDateTime();
        Staff currentStaff = Session.getCurrentStaff();
        String staffName = currentStaff != null ? currentStaff.getFullName() : "N/A";
        updateStaffNameLabel(staffName);

        initializeTableColumns();
        setupDatePickers();
        setupComboBoxes();
        loadInvoices();

        // Thiết lập sự kiện chọn hóa đơn từ TableView
        invoiceTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> handleInvoiceSelection(newValue));

        // Ban đầu, vô hiệu hóa các nút liên quan đến hóa đơn
        viewDetailsButton.setDisable(true);
        refundButton.setDisable(true);
        processPaymentButton.setDisable(true);
        qrPaymentButton.setDisable(true);
        applyDiscountButton.setDisable(true);

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

    @Override
    public void onLanguageChanged() {
        loadTexts();
        setupComboBoxes(); // Reload combo box items with new language
        updateSummaryLabels(); // Update summary labels
        
        // Update staff name label
        Staff currentStaff = Session.getCurrentStaff();
        String staffName = currentStaff != null ? currentStaff.getFullName() : "N/A";
        updateStaffNameLabel(staffName);
        
        // Update status message if exists
        if (statusMessageLabel.getText() != null && !statusMessageLabel.getText().isEmpty()) {
            statusMessageLabel.setText(LanguageManagerStaff.getString("invoice.status.ready"));
        }
    }

    /**
     * Load all text elements with current language
     */
    private void loadTexts() {
        // Header
        if (headerTitle != null) {
            headerTitle.setText(LanguageManagerStaff.getString("invoice.title"));
        }
        
        // Filter labels
        if (dateFilterLabel != null) {
            dateFilterLabel.setText(LanguageManagerStaff.getString("invoice.filter.date"));
        }
        if (statusFilterLabel != null) {
            statusFilterLabel.setText(LanguageManagerStaff.getString("invoice.filter.status"));
        }
        if (paymentMethodFilterLabel != null) {
            paymentMethodFilterLabel.setText(LanguageManagerStaff.getString("invoice.filter.paymentMethod"));
        }
        if (searchLabel != null) {
            searchLabel.setText(LanguageManagerStaff.getString("invoice.filter.search"));
        }
        
        // Table columns
        if (idColumn != null) {
            idColumn.setText(LanguageManagerStaff.getString("invoice.table.id"));
        }
        if (orderIdColumn != null) {
            orderIdColumn.setText(LanguageManagerStaff.getString("invoice.table.orderId"));
        }
        if (customerColumn != null) {
            customerColumn.setText(LanguageManagerStaff.getString("invoice.table.customer"));
        }
        if (phoneColumn != null) {
            phoneColumn.setText(LanguageManagerStaff.getString("invoice.table.phone"));
        }
        if (dateColumn != null) {
            dateColumn.setText(LanguageManagerStaff.getString("invoice.table.date"));
        }
        if (serviceColumn != null) {
            serviceColumn.setText(LanguageManagerStaff.getString("invoice.table.service"));
        }
        if (totalColumn != null) {
            totalColumn.setText(LanguageManagerStaff.getString("invoice.table.total"));
        }
        if (paymentMethodColumn != null) {
            paymentMethodColumn.setText(LanguageManagerStaff.getString("invoice.table.paymentMethod"));
        }
        if (statusColumn != null) {
            statusColumn.setText(LanguageManagerStaff.getString("invoice.table.status"));
        }
        
        // Summary labels
        if (totalInvoicesTextLabel != null) {
            totalInvoicesTextLabel.setText(LanguageManagerStaff.getString("invoice.summary.totalInvoices"));
        }
        if (paidInvoicesTextLabel != null) {
            paidInvoicesTextLabel.setText(LanguageManagerStaff.getString("invoice.summary.paidInvoices"));
        }
        if (pendingInvoicesTextLabel != null) {
            pendingInvoicesTextLabel.setText(LanguageManagerStaff.getString("invoice.summary.pendingInvoices"));
        }
        if (totalRevenueTextLabel != null) {
            totalRevenueTextLabel.setText(LanguageManagerStaff.getString("invoice.summary.totalRevenue"));
        }
        
        // Buttons
        if (searchButton != null) {
            searchButton.setText(LanguageManagerStaff.getString("invoice.button.search"));
        }
        if (resetFilterButton != null) {
            resetFilterButton.setText(LanguageManagerStaff.getString("invoice.button.resetFilter"));
        }
        if (createBookingButton != null) {
            createBookingButton.setText(LanguageManagerStaff.getString("invoice.button.createBooking"));
        }
        if (applyDiscountButton != null) {
            applyDiscountButton.setText(LanguageManagerStaff.getString("invoice.button.applyDiscount"));
        }
        if (viewDetailsButton != null) {
            viewDetailsButton.setText(LanguageManagerStaff.getString("invoice.button.viewDetails"));
        }
        if (processPaymentButton != null) {
            processPaymentButton.setText(LanguageManagerStaff.getString("invoice.button.processPayment"));
        }
        if (processPaymentAndPrintButton != null) {
            processPaymentAndPrintButton.setText(LanguageManagerStaff.getString("invoice.button.processPaymentAndPrint"));
        }
        if (qrPaymentButton != null) {
            qrPaymentButton.setText(LanguageManagerStaff.getString("invoice.button.qrPayment"));
        }
        if (refundButton != null) {
            refundButton.setText(LanguageManagerStaff.getString("invoice.button.refund"));
        }
        if (homeButton != null) {
            homeButton.setText(LanguageManagerStaff.getString("invoice.button.home"));
        }
        if (helpButton != null) {
            helpButton.setText(LanguageManagerStaff.getString("invoice.button.help"));
        }
        if (exitButton != null) {
            exitButton.setText(LanguageManagerStaff.getString("invoice.button.exit"));
        }
        
        // Prompt texts
        if (searchField != null) {
            searchField.setPromptText(LanguageManagerStaff.getString("invoice.search.placeholder"));
        }
    }

    private void updateStaffNameLabel(String staffName) {
        if (staffNameLabel != null) {
            staffNameLabel.setText(LanguageManagerStaff.getString("invoice.staff.cashier") + ": " + staffName);
        }
    }

    private void updateDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");
        dateTimeLabel.setText(LocalDateTime.now().format(formatter));
    }

    private void initializeTableColumns() {
        idColumn.setCellValueFactory(
                cellData -> new SimpleIntegerProperty(cellData.getValue().getInvoiceId()).asObject());

        orderIdColumn.setCellValueFactory(cellData -> {
            Invoice invoice = cellData.getValue();
            Order order = invoice.getOrder();
            return new SimpleIntegerProperty(order != null ? order.getOrderId() : 0).asObject();
        });

        customerColumn.setCellValueFactory(cellData -> {
            Invoice invoice = cellData.getValue();
            Order order = invoice.getOrder();
            Customer customer = (order != null) ? order.getCustomer() : null;
            return new SimpleStringProperty(
                    customer != null && customer.getFullName() != null ? customer.getFullName() : "N/A");
        });

        phoneColumn.setCellValueFactory(cellData -> {
            Invoice invoice = cellData.getValue();
            Order order = invoice.getOrder();
            Customer customer = (order != null) ? order.getCustomer() : null;
            return new SimpleStringProperty(
                    customer != null && customer.getPhone() != null ? customer.getPhone() : "N/A");
        });

        dateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(
                cellData.getValue().getPaymentDate() != null ? cellData.getValue().getPaymentDate().toLocalDateTime()
                        : null));
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
                String sql = "SELECT s.name FROM order_detail od " +
                        "JOIN service s ON od.service_id = s.service_id WHERE od.order_id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, order.getOrderId());
                    try (ResultSet rs = stmt.executeQuery()) {
                        StringBuilder serviceNames = new StringBuilder();
                        while (rs.next()) {
                            serviceNames.append(rs.getString("name")).append(", ");
                        }
                        String result = serviceNames.length() > 0 ? serviceNames.substring(0, serviceNames.length() - 2)
                                : LanguageManagerStaff.getString("invoice.noService");
                        return new SimpleStringProperty(result);
                    }
                }
            } catch (SQLException e) {
                PaymentLogger.error("Lỗi khi lấy danh sách dịch vụ: " + e.getMessage(), e);
                return new SimpleStringProperty(LanguageManagerStaff.getString("error.title") + ": " + e.getMessage());
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
                cellData.getValue().getPaymentMethod() != null ? 
                getLocalizedPaymentMethod(cellData.getValue().getPaymentMethod()) : "N/A"));

        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStatus() != null ? 
                getLocalizedStatus(cellData.getValue().getStatus()) : "N/A"));
    }

    private String getLocalizedPaymentMethod(PaymentMethodEnum paymentMethod) {
        switch (paymentMethod) {
            case CASH:
                return LanguageManagerStaff.getString("invoice.paymentMethod.cash");
            case QR:
                return LanguageManagerStaff.getString("invoice.paymentMethod.qr");
            case CARD:
                return LanguageManagerStaff.getString("invoice.paymentMethod.card");
            default:
                return paymentMethod.toString();
        }
    }

    private String getLocalizedStatus(StatusEnum status) {
        switch (status) {
            case PENDING:
                return LanguageManagerStaff.getString("invoice.status.pending");
            case COMPLETED:
                return LanguageManagerStaff.getString("invoice.status.completed");
            case CANCELLED:
                return LanguageManagerStaff.getString("invoice.status.cancelled");
            default:
                return status.toString();
        }
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
        ObservableList<String> statusList = FXCollections.observableArrayList();
        statusList.add(LanguageManagerStaff.getString("invoice.filter.all"));
        for (StatusEnum status : StatusEnum.values()) {
            statusList.add(getLocalizedStatus(status));
        }
        statusFilter.setItems(statusList);
        statusFilter.setValue(LanguageManagerStaff.getString("invoice.filter.all"));

        ObservableList<String> paymentMethodList = FXCollections.observableArrayList();
        paymentMethodList.add(LanguageManagerStaff.getString("invoice.filter.all"));
        for (PaymentMethodEnum method : PaymentMethodEnum.values()) {
            paymentMethodList.add(getLocalizedPaymentMethod(method));
        }
        paymentMethodFilter.setItems(paymentMethodList);
        paymentMethodFilter.setValue(LanguageManagerStaff.getString("invoice.filter.all"));
    }

    private void setupSearchField() {
        searchField.setOnAction(event -> searchInvoices());
    }

    private void setupButtonVisibility() {
        boolean canViewInvoice = RoleChecker.hasPermission("VIEW_INVOICE");
        boolean canManagePayment = RoleChecker.hasPermission("MANAGE_PAYMENT");
        boolean canApplyPromotion = RoleChecker.hasPermission("APPLY_PROMOTION");
        createBookingButton.setVisible(true);
        viewDetailsButton.setVisible(canViewInvoice);
        refundButton.setVisible(canManagePayment);
        processPaymentButton.setVisible(canManagePayment);
        qrPaymentButton.setVisible(canManagePayment);
        applyDiscountButton.setVisible(canApplyPromotion);
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
                            invoice.setPaymentMethod(
                                    paymentMethodStr != null ? PaymentMethodEnum.valueOf(paymentMethodStr) : null);
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
            statusMessageLabel.setText(LanguageManagerStaff.getString("invoice.status.loaded") + " " + 
                invoices.size() + " " + LanguageManagerStaff.getString("invoice.status.invoicesOn") + " " + 
                displayDate.format(formatter));
        } catch (Exception e) {
            progressBar.setVisible(false);
            showAlert(AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("invoice.error.loadFailed"), 
                e.getMessage());
            statusMessageLabel.setText(LanguageManagerStaff.getString("error.title") + ": " + e.getMessage());
            PaymentLogger.error("Lỗi tải danh sách hóa đơn: " + e.getMessage(), e);
        }
    }

    private void handleInvoiceSelection(Invoice invoice) {
        selectedInvoice = invoice;
        boolean hasSelectedInvoice = invoice != null;

        // Kiểm tra và vô hiệu hóa các nút nếu không có hóa đơn được chọn
        viewDetailsButton.setDisable(!hasSelectedInvoice);
        refundButton.setDisable(
                !hasSelectedInvoice || (hasSelectedInvoice && !StatusEnum.COMPLETED.equals(invoice.getStatus())));
        processPaymentButton.setDisable(
                !hasSelectedInvoice || (hasSelectedInvoice && !StatusEnum.PENDING.equals(invoice.getStatus())));
        qrPaymentButton.setDisable(
                !hasSelectedInvoice || (hasSelectedInvoice && !StatusEnum.PENDING.equals(invoice.getStatus())));
        applyDiscountButton.setDisable(
                !hasSelectedInvoice || (hasSelectedInvoice && !StatusEnum.PENDING.equals(invoice.getStatus())));
    }

    private void applyFilters() {
        String statusValue = statusFilter.getValue();
        String paymentMethodValue = paymentMethodFilter.getValue();
        String allText = LanguageManagerStaff.getString("invoice.filter.all");

        if (statusValue == null || statusValue.equals(allText) &&
                (paymentMethodValue == null || paymentMethodValue.equals(allText))) {
            invoiceTable.setItems(invoiceList);
            updateSummaryLabels();
            return;
        }

        ObservableList<Invoice> filteredList = FXCollections.observableArrayList();

        for (Invoice invoice : invoiceList) {
            boolean statusMatch = statusValue == null || statusValue.equals(allText)
                    || (invoice.getStatus() != null && getLocalizedStatus(invoice.getStatus()).equals(statusValue));

            boolean paymentMethodMatch = paymentMethodValue == null || paymentMethodValue.equals(allText)
                    || (invoice.getPaymentMethod() != null &&
                    getLocalizedPaymentMethod(invoice.getPaymentMethod()).equals(paymentMethodValue));

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
            } else if (StatusEnum.PENDING.equals(invoice.getStatus()) ||
                    StatusEnum.CANCELLED.equals(invoice.getStatus())) {
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
            showAlert(AlertType.WARNING, 
                LanguageManagerStaff.getString("warning.title"), 
                LanguageManagerStaff.getString("invoice.warning.invalidDate"), 
                LanguageManagerStaff.getString("invoice.warning.selectDate"));
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
                String allText = LanguageManagerStaff.getString("invoice.filter.all");
                boolean statusMatch = statusFilter.getValue().equals(allText) ||
                        (invoice.getStatus() != null && getLocalizedStatus(invoice.getStatus()).equals(statusFilter.getValue()));

                boolean paymentMethodMatch = paymentMethodFilter.getValue().equals(allText) ||
                        (invoice.getPaymentMethod() != null &&
                                getLocalizedPaymentMethod(invoice.getPaymentMethod()).equals(paymentMethodFilter.getValue()));

                if (statusMatch && paymentMethodMatch) {
                    filteredList.add(invoice);
                }
            }
        }

        invoiceTable.setItems(filteredList);
        updateSummaryLabels(filteredList);
        statusMessageLabel.setText(LanguageManagerStaff.getString("invoice.status.found") + " " + 
            filteredList.size() + " " + LanguageManagerStaff.getString("invoice.status.results"));
    }

    @FXML
    private void onSearchButtonClick() {
        if (fromDatePicker.getValue() == null) {
            showAlert(AlertType.WARNING, 
                LanguageManagerStaff.getString("warning.title"), 
                LanguageManagerStaff.getString("invoice.warning.noDateSelected"), 
                LanguageManagerStaff.getString("invoice.warning.selectDateToSearch"));
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
            showAlert(AlertType.WARNING, 
                LanguageManagerStaff.getString("warning.title"), 
                LanguageManagerStaff.getString("invoice.warning.noInvoiceSelected"),
                LanguageManagerStaff.getString("invoice.warning.selectInvoiceToView"));
            return;
        }
        displayInvoiceDetails(selectedInvoice, true, false);
    }

    private void displayInvoiceDetails(Invoice invoice, boolean showPrintButton, boolean fromPayment) {
        try {
            Stage detailStage = new Stage();
            detailStage.setTitle(LanguageManagerStaff.getString("invoice.details.title") + " #" + invoice.getInvoiceId());

            VBox root = new VBox(10);
            root.setPadding(new Insets(20));
            root.setAlignment(Pos.CENTER);

            Label storeNameLabel = new Label("PET CARE CENTER");
            storeNameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

            Label storeAddressLabel = new Label(LanguageManagerStaff.getString("invoice.details.address") + ": 123 Đường ABC, Quận XYZ, TP. HCM");
            Label storePhoneLabel = new Label(LanguageManagerStaff.getString("invoice.details.phone") + ": (028) 1234 5678");

            Separator sep1 = new Separator();

            Label invoiceHeaderLabel = new Label(LanguageManagerStaff.getString("invoice.details.header"));
            invoiceHeaderLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            GridPane infoGrid = new GridPane();
            infoGrid.setHgap(15);
            infoGrid.setVgap(10);
            infoGrid.setPadding(new Insets(10, 0, 10, 0));

            Staff currentStaff = Session.getCurrentStaff();
            String staffName = currentStaff != null ? currentStaff.getFullName() : "N/A";

            infoGrid.add(new Label(LanguageManagerStaff.getString("invoice.details.invoiceNumber") + ":"), 0, 0);
            infoGrid.add(new Label("#" + invoice.getInvoiceId()), 1, 0);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime paymentDate = invoice.getPaymentDate() != null ?
                    invoice.getPaymentDate().toLocalDateTime() : LocalDateTime.now();

            infoGrid.add(new Label(LanguageManagerStaff.getString("invoice.details.date") + ":"), 0, 1);
            infoGrid.add(new Label(paymentDate.format(formatter)), 1, 1);

            infoGrid.add(new Label(LanguageManagerStaff.getString("invoice.details.cashier") + ":"), 0, 2);
            infoGrid.add(new Label(staffName), 1, 2);

            Customer customer = invoice.getOrder() != null && invoice.getOrder().getCustomer() != null ?
                    invoice.getOrder().getCustomer() : null;

            if (customer != null) {
                infoGrid.add(new Label(LanguageManagerStaff.getString("invoice.details.customer") + ":"), 2, 0);
                infoGrid.add(new Label(customer.getFullName()), 3, 0);
                infoGrid.add(new Label(LanguageManagerStaff.getString("invoice.details.customerPhone") + ":"), 2, 1);
                infoGrid.add(new Label(customer.getPhone()), 3, 1);
                infoGrid.add(new Label(LanguageManagerStaff.getString("invoice.details.customerId") + ":"), 2, 2);
                infoGrid.add(new Label("KH-" + String.format("%05d", customer.getId())), 3, 2);
            }

            Separator sep2 = new Separator();

            TableView<InvoiceDetailItem> detailTable = new TableView<>();
            detailTable.setPrefHeight(200);

            TableColumn<InvoiceDetailItem, Integer> sttCol = new TableColumn<>(LanguageManagerStaff.getString("invoice.details.stt"));
            sttCol.setCellValueFactory(new PropertyValueFactory<>("stt"));
            sttCol.setPrefWidth(50);

            TableColumn<InvoiceDetailItem, String> nameCol = new TableColumn<>(LanguageManagerStaff.getString("invoice.details.serviceName"));
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            nameCol.setPrefWidth(300);

            TableColumn<InvoiceDetailItem, Integer> qtyCol = new TableColumn<>(LanguageManagerStaff.getString("invoice.details.quantity"));
            qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            qtyCol.setPrefWidth(50);

            TableColumn<InvoiceDetailItem, Double> priceCol = new TableColumn<>(LanguageManagerStaff.getString("invoice.details.unitPrice"));
            priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
            priceCol.setPrefWidth(100);
            priceCol.setCellFactory(tc -> new TableCell<InvoiceDetailItem, Double>() {
                @Override
                protected void updateItem(Double price, boolean empty) {
                    super.updateItem(price, empty);
                    setText(empty || price == null ? null : String.format("%,.0f VND", price));
                }
            });

            TableColumn<InvoiceDetailItem, Double> totalCol = new TableColumn<>(LanguageManagerStaff.getString("invoice.details.amount"));
            totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
            totalCol.setPrefWidth(120);
            totalCol.setCellFactory(tc -> new TableCell<InvoiceDetailItem, Double>() {
                @Override
                protected void updateItem(Double total, boolean empty) {
                    super.updateItem(total, empty);
                    setText(empty || total == null ? null : String.format("%,.0f VND", total));
                }
            });

            detailTable.getColumns().addAll(sttCol, nameCol, qtyCol, priceCol, totalCol);

            ObservableList<InvoiceDetailItem> detailItems = FXCollections.observableArrayList();
            if (invoice.getOrder() != null) {
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
            }
            detailTable.setItems(detailItems);

            final double[] subtotal = { invoice.getSubtotal() != null ? invoice.getSubtotal().doubleValue() : 0 };
            final double[] discount = { invoice.getDiscountAmount() != null ? invoice.getDiscountAmount().doubleValue() : 0 };
            final int[] pointsUsed = { invoice.getPointsUsed() != null ? invoice.getPointsUsed() : 0 };
            final double[] pointsValue = { pointsUsed[0] * 1000 };
            final double[] grandTotal = { invoice.getTotal() != null ? invoice.getTotal().doubleValue() : 0 };
            final double[] amountPaid = { invoice.getAmountPaid() != null ? invoice.getAmountPaid().doubleValue() : 0 };
            final double[] change = { amountPaid[0] - grandTotal[0] };
            final PaymentMethodEnum[] paymentMethod = { invoice.getPaymentMethod() != null ?
                    invoice.getPaymentMethod() : PaymentMethodEnum.CASH };

            GridPane summaryGrid = new GridPane();
            summaryGrid.setHgap(10);
            summaryGrid.setVgap(5);
            summaryGrid.setPadding(new Insets(10, 0, 10, 0));
            summaryGrid.setAlignment(Pos.CENTER_RIGHT);
            Separator sep3 = new Separator();

            Label subtotalLabel = new Label(LanguageManagerStaff.getString("invoice.details.subtotal") + ":");
            Label discountLabel = new Label(LanguageManagerStaff.getString("invoice.details.discount") + ":");
            Label pointsLabel = new Label(LanguageManagerStaff.getString("invoice.details.pointsUsed") + ":");
            Label grandTotalLabel = new Label(LanguageManagerStaff.getString("invoice.details.grandTotal") + ":");
            Label amountPaidLabel = new Label(LanguageManagerStaff.getString("invoice.details.amountPaid") + ":");
            Label changeLabel = new Label(LanguageManagerStaff.getString("invoice.details.change") + ":");
            Label paymentMethodLabel = new Label(LanguageManagerStaff.getString("invoice.details.paymentMethod") + ":");
            Label thanksLabel = new Label(LanguageManagerStaff.getString("invoice.details.thanks"));
            thanksLabel.setStyle("-fx-font-style: italic; -fx-font-size: 14px;");
            grandTotalLabel.setStyle("-fx-font-weight: bold;");

            Label subtotalValue = new Label(String.format("%,.0f VND", subtotal[0]));
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER);

            HBox discountBox = new HBox(5);
            TextField discountCodeField = new TextField();
            discountCodeField.setPromptText(LanguageManagerStaff.getString("invoice.details.enterPromotionCode"));
            discountCodeField.setPrefWidth(100);
            Label discountValue = new Label(String.format("%,.0f VND", discount[0]));
            Button applyDiscountButton = new Button(LanguageManagerStaff.getString("invoice.details.apply"));

            Label pointsValueLabel = new Label(String.format("%,d " + LanguageManagerStaff.getString("invoice.details.points") + " (%,.0f VND)", pointsUsed[0], pointsValue[0]));
            Label grandTotalValue = new Label(String.format("%,.0f VND", grandTotal[0]));
            grandTotalValue.setStyle("-fx-font-weight: bold;");
            Label changeValue = new Label(String.format("%,.0f VND", change[0]));

            boolean canApplyDiscount = RoleChecker.hasPermission("APPLY_PROMOTION") &&
                    invoice.getStatus() == StatusEnum.PENDING;
            discountCodeField.setDisable(!canApplyDiscount);
            applyDiscountButton.setDisable(!canApplyDiscount);

            applyDiscountButton.setOnAction(e -> {
                String code = discountCodeField.getText().trim();
                if (code.isEmpty()) {
                    showAlert(AlertType.WARNING, 
                        LanguageManagerStaff.getString("warning.title"), 
                        LanguageManagerStaff.getString("invoice.details.warning.noPromotionCode"),
                        LanguageManagerStaff.getString("invoice.details.warning.enterPromotionCode"));
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
                                showAlert(AlertType.WARNING, 
                                    LanguageManagerStaff.getString("warning.title"), 
                                    LanguageManagerStaff.getString("invoice.details.warning.invalidPromotionCode"),
                                    LanguageManagerStaff.getString("invoice.details.warning.promotionCodeNotExist"));
                                return;
                            }
                        }
                    }

                    discount[0] = subtotal[0] * discountPercent / 100;
                    grandTotal[0] = subtotal[0] - discount[0] - pointsValue[0];
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

                    showAlert(AlertType.INFORMATION, 
                        LanguageManagerStaff.getString("success.title"), 
                        LanguageManagerStaff.getString("invoice.details.success.promotionApplied"),
                        LanguageManagerStaff.getString("invoice.details.promotionCode") + " " + code + " " + 
                        LanguageManagerStaff.getString("invoice.details.appliedWith") + " " + discountPercent + "% " + 
                        LanguageManagerStaff.getString("invoice.details.discount"));
                    loadInvoices();
                } catch (SQLException ex) {
                    PaymentLogger.error("Lỗi áp dụng mã khuyến mãi: " + ex.getMessage(), ex);
                    showAlert(AlertType.ERROR, 
                        LanguageManagerStaff.getString("error.title"), 
                        LanguageManagerStaff.getString("invoice.details.error.cannotApplyPromotion"), 
                        ex.getMessage());
                }
            });

            discountBox.getChildren().addAll(discountValue, discountCodeField, applyDiscountButton);

            HBox amountPaidBox = new HBox(5);
            amountPaidBox.setAlignment(Pos.CENTER_RIGHT);

            TextField amountPaidField = new TextField();
            amountPaidField.setText(String.format("%,.0f", amountPaid[0]));
            amountPaidField.setPrefWidth(120);
            amountPaidField.setPromptText(LanguageManagerStaff.getString("invoice.details.enterAmountPaid"));

            boolean isPending = invoice.getStatus() == StatusEnum.PENDING;
            boolean isCash = paymentMethod[0] == PaymentMethodEnum.CASH;
            amountPaidField.setDisable(!(isCash && isPending));

            amountPaidField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    String normalizedValue = newValue.replaceAll("[,.]", "");
                    if (normalizedValue.isEmpty()) {
                        changeValue.setText("0 VND");
                        return;
                    }
                    double newAmountPaid = Double.parseDouble(normalizedValue);
                    change[0] = newAmountPaid - grandTotal[0];

                    if (change[0] < 0) {
                        changeValue.setText(LanguageManagerStaff.getString("invoice.details.shortage") + " " + 
                            String.format("%,.0f VND", -change[0]));
                        changeValue.setStyle("-fx-text-fill: red;");
                    } else {
                        changeValue.setText(String.format("%,.0f VND", change[0]));
                        changeValue.setStyle("-fx-text-fill: green;");
                    }
                    amountPaid[0] = newAmountPaid;
                } catch (NumberFormatException ex) {
                    amountPaidField.setText(oldValue);
                } catch (Exception ex) {
                    PaymentLogger.error("Lỗi xử lý tiền khách trả: " + ex.getMessage(), ex);
                    amountPaidField.setText(oldValue);
                }
            });

            ComboBox<PaymentMethodEnum> paymentMethodComboBox = new ComboBox<>();
            paymentMethodComboBox.getItems().addAll(PaymentMethodEnum.values());
            paymentMethodComboBox.setValue(paymentMethod[0]);
            paymentMethodComboBox.setDisable(!isPending || !RoleChecker.hasPermission("MANAGE_PAYMENT"));

            // Custom cell factory for payment method combo box
            paymentMethodComboBox.setCellFactory(listView -> new ListCell<PaymentMethodEnum>() {
                @Override
                protected void updateItem(PaymentMethodEnum item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(getLocalizedPaymentMethod(item));
                    }
                }
            });

            paymentMethodComboBox.setButtonCell(new ListCell<PaymentMethodEnum>() {
                @Override
                protected void updateItem(PaymentMethodEnum item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(getLocalizedPaymentMethod(item));
                    }
                }
            });

            paymentMethodComboBox.setOnAction(e -> {
                PaymentMethodEnum selectedMethod = paymentMethodComboBox.getValue();
                paymentMethod[0] = selectedMethod;
                amountPaidField.setDisable(selectedMethod != PaymentMethodEnum.CASH || !isPending);

                if (selectedMethod == PaymentMethodEnum.QR) {
                    showAlert(AlertType.INFORMATION, 
                        LanguageManagerStaff.getString("info.title"), 
                        LanguageManagerStaff.getString("invoice.details.info.useQRPayment"),
                        LanguageManagerStaff.getString("invoice.details.info.useQRButton"));
                    openQRPaymentWindow();
                }
            });

            int row = 0;
            summaryGrid.add(subtotalLabel, 0, row);
            summaryGrid.add(subtotalValue, 1, row++);

            summaryGrid.add(discountLabel, 0, row);
            summaryGrid.add(discountBox, 1, row++);

            summaryGrid.add(pointsLabel, 0, row);
            summaryGrid.add(pointsValueLabel, 1, row++);

            summaryGrid.add(grandTotalLabel, 0, row);
            summaryGrid.add(grandTotalValue, 1, row++);

            summaryGrid.add(paymentMethodLabel, 0, row);
            summaryGrid.add(paymentMethodComboBox, 1, row++);

            summaryGrid.add(amountPaidLabel, 0, row);
            amountPaidBox.getChildren().add(amountPaidField);
            summaryGrid.add(amountPaidBox, 1, row++);

            summaryGrid.add(changeLabel, 0, row);
            summaryGrid.add(changeValue, 1, row++);

            if (fromPayment && invoice.getStatus() == StatusEnum.PENDING && RoleChecker.hasPermission("MANAGE_PAYMENT")) {
                Button paymentButton = new Button(LanguageManagerStaff.getString("invoice.details.payment"));
                paymentButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                paymentButton.setOnAction(e -> processPaymentAction(invoice, paymentMethod[0], amountPaid[0], false, detailStage));

                Button paymentAndPrintButton = new Button(LanguageManagerStaff.getString("invoice.details.paymentAndPrint"));
                paymentAndPrintButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                paymentAndPrintButton.setOnAction(e -> processPaymentAction(invoice, paymentMethod[0], amountPaid[0], true, detailStage));

                buttonBox.getChildren().addAll(paymentButton, paymentAndPrintButton);
            }

            Button closeButton = new Button(LanguageManagerStaff.getString("common.close"));
            closeButton.setOnAction(e -> detailStage.close());
            buttonBox.getChildren().add(closeButton);

            if (showPrintButton) {
                Button printButton = new Button(LanguageManagerStaff.getString("invoice.details.reprint"));
                printButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                printButton.setOnAction(e -> reprintInvoice());
                buttonBox.getChildren().add(printButton);
            }

            root.getChildren().addAll(
                    storeNameLabel, storeAddressLabel, storePhoneLabel, sep1,
                    invoiceHeaderLabel, infoGrid, sep2, detailTable, summaryGrid, sep3,
                    thanksLabel, buttonBox);

            Scene scene = new Scene(root, 700, 650);
            detailStage.setScene(scene);
            detailStage.show();
        } catch (Exception e) {
            PaymentLogger.error("Lỗi hiển thị chi tiết hóa đơn: " + e.getMessage(), e);
            showAlert(AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("invoice.details.error.cannotDisplay"), 
                e.getMessage());
        }
    }

    private void processPaymentAction(Invoice invoice, PaymentMethodEnum paymentMethod, double amountPaid,
                                     boolean print, Stage detailStage) {
        try {
            int orderId = invoice.getOrder().getOrderId();
            int invoiceId = invoice.getInvoiceId();

            if (paymentMethod == PaymentMethodEnum.CASH && amountPaid < invoice.getTotal().doubleValue()) {
                showAlert(AlertType.WARNING, 
                    LanguageManagerStaff.getString("warning.title"), 
                    LanguageManagerStaff.getString("invoice.payment.warning.insufficientAmount"),
                    LanguageManagerStaff.getString("invoice.payment.warning.amountMustBeGreater"));
                return;
            }

            if (paymentMethod == PaymentMethodEnum.QR) {
                openQRPaymentWindow();
                return;
            }

            String checkStatusSql = "SELECT status FROM invoice WHERE invoice_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(checkStatusSql)) {
                stmt.setInt(1, invoiceId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String currentStatus = rs.getString("status");
                        if (!StatusEnum.PENDING.name().equals(currentStatus)) {
                            showAlert(AlertType.WARNING, 
                                LanguageManagerStaff.getString("warning.title"), 
                                LanguageManagerStaff.getString("invoice.payment.warning.alreadyPaid"),
                                LanguageManagerStaff.getString("invoice.payment.warning.invoiceAlreadyProcessed"));
                            return;
                        }
                    }
                }
            }

            String updateInvoiceSql = "UPDATE invoice SET payment_date = ?, status = ?, " +
                    "payment_method = ?, amount_paid = ?, staff_id = ? WHERE invoice_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(updateInvoiceSql)) {
                stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(2, StatusEnum.COMPLETED.name());
                stmt.setString(3, paymentMethod.name());
                stmt.setBigDecimal(4, new BigDecimal(amountPaid));
                stmt.setInt(5, Session.getCurrentStaff().getId());
                stmt.setInt(6, invoiceId);
                stmt.executeUpdate();
            }

            String updateOrderSql = "UPDATE `order` SET status = ? WHERE order_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(updateOrderSql)) {
                stmt.setString(1, StatusEnum.COMPLETED.name());
                stmt.setInt(2, orderId);
                stmt.executeUpdate();
            }

            if (print) {
                String fileName = "invoice_" + invoiceId + ".pdf";
                invoiceService.generateInvoicePDF(orderId, fileName);
                File file = new File(fileName);
                if (file.exists()) {
                    Desktop.getDesktop().open(file);
                } else {
                    showAlert(AlertType.ERROR, 
                        LanguageManagerStaff.getString("error.title"), 
                        LanguageManagerStaff.getString("invoice.payment.error.cannotOpenFile"),
                        LanguageManagerStaff.getString("invoice.payment.error.fileNotExist") + ": " + fileName);
                }
            }

            showAlert(AlertType.INFORMATION, 
                LanguageManagerStaff.getString("success.title"), 
                LanguageManagerStaff.getString("invoice.payment.success.paymentCompleted"),
                LanguageManagerStaff.getString("invoice.payment.invoice") + " #" + invoiceId + " " + 
                LanguageManagerStaff.getString("invoice.payment.paidWith") + " " + getLocalizedPaymentMethod(paymentMethod) +
                        (print ? " " + LanguageManagerStaff.getString("invoice.payment.andPrinted") : "."));
            loadInvoices();
            detailStage.close();
        } catch (SQLException | IOException ex) {
            PaymentLogger.error("Lỗi thực hiện thanh toán" + (print ? " và in" : "") + ": " + ex.getMessage(), ex);
            showAlert(AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("invoice.payment.error.cannotProcess") + (print ? " " + LanguageManagerStaff.getString("invoice.payment.error.orPrint") : ""),
                ex.getMessage());
        }
    }

    private void reprintInvoice() {
        try {
            String fileName = "invoice_" + selectedInvoice.getInvoiceId() + ".pdf";
            invoiceService.generateInvoicePDF(selectedInvoice.getOrder().getOrderId(), fileName);
            File file = new File(fileName);
            if (file.exists()) {
                Desktop.getDesktop().open(file);
                showAlert(AlertType.INFORMATION, 
                    LanguageManagerStaff.getString("success.title"), 
                    LanguageManagerStaff.getString("invoice.reprint.success.fileOpened"),
                    LanguageManagerStaff.getString("invoice.reprint.success.openedWithDefault"));
            } else {
                showAlert(AlertType.ERROR, 
                    LanguageManagerStaff.getString("error.title"), 
                    LanguageManagerStaff.getString("invoice.reprint.error.cannotOpen"),
                    LanguageManagerStaff.getString("invoice.reprint.error.fileNotExist") + ": " + fileName);
            }
        } catch (Exception e) {
            PaymentLogger.error("Lỗi in lại hóa đơn: " + e.getMessage(), e);
            showAlert(AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("invoice.reprint.error.cannotPrint"), 
                e.getMessage());
        }
    }

    @FXML
    public void applyDiscount() {
        if (selectedInvoice == null) {
            showAlert(AlertType.WARNING, 
                LanguageManagerStaff.getString("warning.title"), 
                LanguageManagerStaff.getString("invoice.discount.warning.noInvoiceSelected"),
                LanguageManagerStaff.getString("invoice.discount.warning.selectInvoiceToApply"));
            return;
        }

        if (selectedInvoice.getStatus() != StatusEnum.PENDING) {
            showAlert(AlertType.WARNING, 
                LanguageManagerStaff.getString("warning.title"), 
                LanguageManagerStaff.getString("invoice.discount.warning.invalidStatus"),
                LanguageManagerStaff.getString("invoice.discount.warning.onlyPendingInvoices"));
            return;
        }

        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle(LanguageManagerStaff.getString("invoice.discount.dialog.title"));
        dialog.setHeaderText(LanguageManagerStaff.getString("invoice.discount.dialog.header"));
        dialog.setContentText(LanguageManagerStaff.getString("invoice.discount.dialog.content"));

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(code -> {
            if (code.trim().isEmpty()) {
                showAlert(AlertType.WARNING, 
                    LanguageManagerStaff.getString("warning.title"), 
                    LanguageManagerStaff.getString("invoice.discount.warning.emptyCode"),
                    LanguageManagerStaff.getString("invoice.discount.warning.enterValidCode"));
                return;
            }

            try {
                double discountPercent = 0;
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "SELECT discount_percent FROM promotion WHERE code = ? AND start_date <= NOW() AND end_date >= NOW() AND active = true")) {
                    stmt.setString(1, code);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            discountPercent = rs.getDouble("discount_percent");
                        } else {
                            showAlert(AlertType.WARNING, 
                                LanguageManagerStaff.getString("warning.title"), 
                                LanguageManagerStaff.getString("invoice.discount.warning.invalidCode"),
                                LanguageManagerStaff.getString("invoice.discount.warning.codeNotExistOrExpired"));
                            return;
                        }
                    }
                }

                double subtotal;
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("SELECT subtotal FROM invoice WHERE invoice_id = ?")) {
                    stmt.setInt(1, selectedInvoice.getInvoiceId());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            subtotal = rs.getDouble("subtotal");
                        } else {
                            throw new SQLException(LanguageManagerStaff.getString("invoice.discount.error.invoiceNotFound") + ": " + selectedInvoice.getInvoiceId());
                        }
                    }
                }

                if (subtotal <= 0) {
                    showAlert(AlertType.WARNING, 
                        LanguageManagerStaff.getString("warning.title"), 
                        LanguageManagerStaff.getString("invoice.discount.warning.invalidTotal"),
                        LanguageManagerStaff.getString("invoice.discount.warning.totalMustBeGreaterThanZero"));
                    return;
                }

                double discountAmount = subtotal * discountPercent / 100;
                double newTotal = subtotal - discountAmount;
                if (newTotal < 0) newTotal = 0;

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

                selectedInvoice.setPromotionCode(code);
                selectedInvoice.setDiscountPercent(new BigDecimal(discountPercent));
                selectedInvoice.setDiscountAmount(new BigDecimal(discountAmount));
                selectedInvoice.setTotal(new BigDecimal(newTotal));

                showAlert(AlertType.INFORMATION, 
                    LanguageManagerStaff.getString("success.title"), 
                    LanguageManagerStaff.getString("invoice.discount.success.applied"),
                    LanguageManagerStaff.getString("invoice.discount.success.appliedCode") + " " + code + " " + 
                    LanguageManagerStaff.getString("invoice.discount.success.withDiscount") + " " + discountPercent + "% " + 
                    LanguageManagerStaff.getString("invoice.discount.success.discountAmount") + ": " +
                            String.format("%,.0f VND", discountAmount));
                loadInvoices();
            } catch (SQLException e) {
                PaymentLogger.error("Lỗi áp dụng khuyến mãi: " + e.getMessage(), e);
                showAlert(AlertType.ERROR, 
                    LanguageManagerStaff.getString("error.title"), 
                    LanguageManagerStaff.getString("invoice.discount.error.cannotApply"), 
                    e.getMessage());
            }
        });
    }

    @FXML
    private void openQRPaymentWindow() {
        if (selectedInvoice == null) {
            showAlert(AlertType.WARNING, 
                LanguageManagerStaff.getString("warning.title"), 
                LanguageManagerStaff.getString("invoice.qr.warning.noInvoiceSelected"),
                LanguageManagerStaff.getString("invoice.qr.warning.selectInvoiceForQR"));
            return;
        }

        if (selectedInvoice.getStatus() != StatusEnum.PENDING) {
            showAlert(AlertType.WARNING, 
                LanguageManagerStaff.getString("warning.title"), 
                LanguageManagerStaff.getString("invoice.qr.warning.invalidStatus"),
                LanguageManagerStaff.getString("invoice.qr.warning.onlyPendingInvoices"));
            return;
        }

        try {
            // Cập nhật phương thức thanh toán của hóa đơn thành QR nếu chưa phải
            if (selectedInvoice.getPaymentMethod() != PaymentMethodEnum.QR) {
                System.out.println(selectedInvoice.getInvoiceId());
                invoiceService.updatePaymentMethod(selectedInvoice.getInvoiceId(), PaymentMethodEnum.QR);
                // Tải lại thông tin hóa đơn
                selectedInvoice = invoiceService.getInvoiceById(selectedInvoice.getInvoiceId());
            }
            
            // Mở cửa sổ thanh toán QR
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/QRPaymentView.fxml"));
            Parent root = loader.load();
            QRPaymentViewController controller = loader.getController();
            controller.setInvoice(selectedInvoice);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle(LanguageManagerStaff.getString("invoice.qr.title") + " - " + 
                LanguageManagerStaff.getString("invoice.qr.invoice") + " #" + selectedInvoice.getInvoiceId());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Làm mới danh sách hóa đơn sau khi đóng cửa sổ thanh toán
            loadInvoices();
        } catch (IOException e) {
            PaymentLogger.error("Lỗi mở cửa sổ thanh toán QR PayOS: " + e.getMessage(), e);
            showAlert(AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("invoice.qr.error.cannotOpen"), 
                e.getMessage());
        }
    }

    @FXML
    private void processPayment() {
        processPaymentLogic(false);
    }

    @FXML
    public void processPaymentAndPrint() {
        processPaymentLogic(true);
    }

    private void processPaymentLogic(boolean print) {
        if (selectedInvoice == null) {
            showAlert(AlertType.WARNING, 
                LanguageManagerStaff.getString("warning.title"), 
                LanguageManagerStaff.getString("invoice.payment.warning.noInvoiceSelected"),
                LanguageManagerStaff.getString("invoice.payment.warning.selectInvoiceToProcess"));
            return;
        }

        if (!StatusEnum.PENDING.equals(selectedInvoice.getStatus())) {
            showAlert(AlertType.WARNING, 
                LanguageManagerStaff.getString("warning.title"), 
                LanguageManagerStaff.getString("invoice.payment.warning.cannotPay"),
                LanguageManagerStaff.getString("invoice.payment.warning.onlyPendingInvoices"));
            return;
        }

        displayInvoiceDetails(selectedInvoice, false, true);
    }

    @FXML
    private void processRefund() {
        if (selectedInvoice == null) {
            showAlert(AlertType.WARNING, 
                LanguageManagerStaff.getString("warning.title"), 
                LanguageManagerStaff.getString("invoice.refund.warning.noInvoiceSelected"),
                LanguageManagerStaff.getString("invoice.refund.warning.selectInvoiceToRefund"));
            return;
        }

        if (!StatusEnum.COMPLETED.equals(selectedInvoice.getStatus())) {
            showAlert(AlertType.WARNING, 
                LanguageManagerStaff.getString("warning.title"), 
                LanguageManagerStaff.getString("invoice.refund.warning.notPaid"),
                LanguageManagerStaff.getString("invoice.refund.warning.onlyPaidInvoices"));
            return;
        }

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(LanguageManagerStaff.getString("invoice.refund.confirmation.title"));
        alert.setHeaderText(LanguageManagerStaff.getString("invoice.refund.confirmation.header"));
        alert.setContentText(LanguageManagerStaff.getString("invoice.refund.confirmation.content") + " #" + 
            selectedInvoice.getInvoiceId() + " " + LanguageManagerStaff.getString("invoice.refund.confirmation.withAmount") + " " +
                selectedInvoice.getTotal() + " VND?");

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

                showAlert(AlertType.INFORMATION, 
                    LanguageManagerStaff.getString("success.title"), 
                    LanguageManagerStaff.getString("invoice.refund.success.refunded"),
                    LanguageManagerStaff.getString("invoice.refund.success.markedAsRefunded"));
                loadInvoices();
            } catch (SQLException e) {
                PaymentLogger.error("Lỗi hoàn tiền: " + e.getMessage(), e);
                showAlert(AlertType.ERROR, 
                    LanguageManagerStaff.getString("error.title"), 
                    LanguageManagerStaff.getString("invoice.refund.error.cannotRefund"), 
                    e.getMessage());
            }
        }
    }

    @FXML
    private void createBooking() {
        try {
            if (!RoleChecker.hasPermission("CREATE_BOOKING")) {
                showAlert(AlertType.WARNING, 
                    LanguageManagerStaff.getString("warning.title"), 
                    LanguageManagerStaff.getString("invoice.booking.warning.noPermission"), 
                    LanguageManagerStaff.getString("invoice.booking.warning.noPermissionToCreate"));
                return;
            }

            if (selectedInvoice != null && selectedInvoice.getOrder() != null &&
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
            modalStage.setTitle(LanguageManagerStaff.getString("invoice.booking.title"));
            modalStage.setScene(new Scene(root));
            modalStage.showAndWait();

            Session.getInstance().removeAttribute("selectedCustomerId");
            statusMessageLabel.setText(LanguageManagerStaff.getString("invoice.booking.success.opened"));
        } catch (Exception e) {
            PaymentLogger.error("Lỗi mở màn hình đặt lịch: " + e.getMessage(), e);
            showAlert(AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("invoice.booking.error.cannotOpen"), 
                e.getMessage());
        }
    }

    @FXML
    private void resetFilter() {
        fromDatePicker.setValue(LocalDate.now());
        statusFilter.setValue(LanguageManagerStaff.getString("invoice.filter.all"));
        paymentMethodFilter.setValue(LanguageManagerStaff.getString("invoice.filter.all"));
        searchField.clear();
        loadInvoices();
    }

    @FXML
    private void showHelp() {
        Alert helpAlert = new Alert(AlertType.INFORMATION);
        helpAlert.setTitle(LanguageManagerStaff.getString("invoice.help.title"));
        helpAlert.setHeaderText(LanguageManagerStaff.getString("invoice.help.header"));
        String helpContent = LanguageManagerStaff.getString("invoice.help.content1") + "\n\n" +
                LanguageManagerStaff.getString("invoice.help.content2") + "\n\n" +
                LanguageManagerStaff.getString("invoice.help.content3") + "\n\n" +
                LanguageManagerStaff.getString("invoice.help.content4") + "\n\n" +
                LanguageManagerStaff.getString("invoice.help.content5") + "\n\n" +
                LanguageManagerStaff.getString("invoice.help.content6") + "\n\n" +
                LanguageManagerStaff.getString("invoice.help.content7") + "\n\n" +
                LanguageManagerStaff.getString("invoice.help.content8") + "\n\n" +
                LanguageManagerStaff.getString("invoice.help.contact");
        helpAlert.setContentText(helpContent);
        helpAlert.showAndWait();
    }

    public void setFromDatePickerValue(LocalDate date) {
        if (fromDatePicker != null) {
            fromDatePicker.setValue(date);
            loadInvoices();
        }
    }

    public void setFromDatePickerToSpecificDate(LocalDate date) {
        if (fromDatePicker != null) {
            fromDatePicker.setValue(date);
            loadInvoices();
        }
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
            PaymentLogger.error("Lỗi chuyển về màn hình chính: " + e.getMessage(), e);
            Stage stage = (Stage) searchButton.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/staff_home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) homeButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            PaymentLogger.error("Lỗi chuyển về trang chủ: " + e.getMessage(), e);
            showAlert(AlertType.ERROR, 
                LanguageManagerStaff.getString("error.title"), 
                LanguageManagerStaff.getString("invoice.error.cannotGoHome"), 
                LanguageManagerStaff.getString("invoice.error.occurred") + ": " + e.getMessage());
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