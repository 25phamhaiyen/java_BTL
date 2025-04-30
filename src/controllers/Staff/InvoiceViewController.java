package controllers.Staff;

import java.io.File;
import java.io.IOException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import enums.PaymentMethodEnum;
import enums.StatusEnum;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Customer;
import model.Invoice;
import model.Order;
import model.Service;
import model.Staff;
import repository.CustomerRepository;
import repository.InvoiceRepository;
import repository.OrderDetailRepository;
import repository.OrderRepository;
import repository.ServiceRepository;
import service.InvoiceService;
import utils.DatabaseConnection;
import utils.RoleChecker;
import utils.Session;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

public class InvoiceViewController implements Initializable {

    @FXML private Label dateTimeLabel;
    @FXML private Label staffNameLabel;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
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
    @FXML private TextField customerSearchField;
    @FXML private TextField customerIdField;
    @FXML private TextField customerNameField;
    @FXML private TextField customerPhoneField;
    
    @FXML private Label customerPointsLabel;
    @FXML private CheckBox usePointsCheckbox;
    @FXML private Label invoiceIdLabel;
    @FXML private Label invoiceDateLabel;
    @FXML private Label cashierNameLabel;
    @FXML private Label subtotalLabel;
    @FXML private TextField discountField;
    @FXML private Label discountAmountLabel;
    @FXML private TextField promotionCodeField;
    @FXML private TextField pointsUsedField;
    @FXML private Label pointsValueLabel;
    @FXML private Label totalAmountLabel;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private TextField amountPaidField;
    @FXML private Label changeAmountLabel;
    @FXML private TextArea invoiceNoteField;
    @FXML private ComboBox<String> serviceSelector;
    @FXML private TextField quantityField;
    @FXML private TableView<InvoiceItem> invoiceItemsTable;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusMessageLabel;
    @FXML private TabPane tabPane;

    @FXML private Button homeButton;
    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ServiceRepository serviceRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceService invoiceService;
    
    private ObservableList<Invoice> invoiceList;
    private ObservableList<InvoiceItem> invoiceItems;
    private Invoice selectedInvoice;
    // Map để lưu trữ danh sách dịch vụ theo customerId
    private Map<Integer, ObservableList<InvoiceItem>> customerServicesMap;
    private Integer currentCustomerId; // Lưu customerId hiện tại

    public InvoiceViewController() {
        this.invoiceRepository = InvoiceRepository.getInstance();
        this.orderRepository = OrderRepository.getInstance();
        this.orderDetailRepository = OrderDetailRepository.getInstance();
        this.serviceRepository = ServiceRepository.getInstance();
        this.customerRepository = CustomerRepository.getInstance();
        this.invoiceService = new InvoiceService();
        this.invoiceItems = FXCollections.observableArrayList();
        this.customerServicesMap = new HashMap<>();
        this.currentCustomerId = null;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Kiểm tra quyền truy cập vào controller
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
        cashierNameLabel.setText(staffName);

        initializeTableColumns();
        initializeInvoiceItemsTable();
        setupDatePickers();
        setupComboBoxes();
        loadInvoices();
        initializeInvoiceCreation();
        invoiceTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> handleInvoiceSelection(newValue));
        setupSearchField();
        setupButtonVisibility();
        updateSummaryLabels();
        
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> updateDateTime());
            }
        }, 0, 60000);
        
        setupEventListeners();
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
    
    private void initializeInvoiceItemsTable() {
        invoiceItemsTable.setItems(invoiceItems);
        if (invoiceItemsTable.getColumns().size() >= 6) {
            invoiceItemsTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("index"));
            invoiceItemsTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("serviceName"));
            invoiceItemsTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("quantity"));
            invoiceItemsTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
            invoiceItemsTable.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("total"));
            
            TableColumn<InvoiceItem, Void> deleteColumn = (TableColumn<InvoiceItem, Void>) invoiceItemsTable.getColumns().get(5);
            deleteColumn.setCellFactory(column -> new TableCell<InvoiceItem, Void>() {
                private final Button deleteButton = new Button("Xóa");
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        deleteButton.setOnAction(event -> {
                            InvoiceItem invoiceItem = getTableView().getItems().get(getIndex());
                            invoiceItems.remove(invoiceItem);
                            // Cập nhật lại Map khi xóa dịch vụ
                            if (currentCustomerId != null) {
                                customerServicesMap.put(currentCustomerId, invoiceItems);
                            }
                            updateInvoiceSummary();
                        });
                        setGraphic(deleteButton);
                    }
                }
            });
        }
    }

    private void setupDatePickers() {
        LocalDate now = LocalDate.now();
        fromDatePicker.setValue(now.minusDays(30));
        toDatePicker.setValue(now);
    }

    private void setupComboBoxes() {
        ObservableList<String> statusList = FXCollections.observableArrayList("Tất cả");
        for (StatusEnum status : StatusEnum.values()) {
            statusList.add(status.name());
        }
        statusFilter.setItems(statusList);
        statusFilter.setValue("Tất cả");

        ObservableList<String> paymentMethodList = FXCollections.observableArrayList("Tất cả");
        for (PaymentMethodEnum method : PaymentMethodEnum.values()) {
            paymentMethodList.add(method.name());
        }
        paymentMethodFilter.setItems(paymentMethodList);
        paymentMethodFilter.setValue("Tất cả");

        loadServices();
        
        ObservableList<String> paymentMethods = FXCollections.observableArrayList();
        for (PaymentMethodEnum method : PaymentMethodEnum.values()) {
            paymentMethods.add(method.name());
        }
        paymentMethodComboBox.setItems(paymentMethods);
        paymentMethodComboBox.setValue(PaymentMethodEnum.CASH.name());
    }
    
    private void loadServices() {
        try {
            List<Service> services = serviceRepository.selectAll();
            ObservableList<String> serviceNames = FXCollections.observableArrayList();
            
            for (Service service : services) {
                serviceNames.add(service.getName());
            }
            
            serviceSelector.setItems(serviceNames);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải danh sách dịch vụ", e.getMessage());
        }
    }

    private void setupSearchField() {
        searchField.setOnAction(event -> searchInvoices());
        customerSearchField.setOnAction(event -> searchCustomer());
    }

    private void setupButtonVisibility() {
        boolean canViewInvoice = RoleChecker.hasPermission("VIEW_INVOICE");
        boolean canManagePayment = RoleChecker.hasPermission("MANAGE_PAYMENT");

        viewDetailsButton.setVisible(canViewInvoice);
        applyDiscountButton.setVisible(canManagePayment);
        refundButton.setVisible(canManagePayment);
        
        if (processPaymentButton != null) {
            processPaymentButton.setVisible(canManagePayment);
        }
        if (processPaymentAndPrintButton != null) {
            processPaymentAndPrintButton.setVisible(canManagePayment);
        }
    }
    
    private void setupEventListeners() {
        discountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                discountField.setText(newValue.replaceAll("[^\\d]", ""));
            } else {
                updateInvoiceSummary();
            }
        });
        
        amountPaidField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                amountPaidField.setText(newValue.replaceAll("[^\\d]", ""));
            } else {
                updateChangeAmount();
            }
        });
        
        usePointsCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            pointsUsedField.setDisable(!newValue);
            if (!newValue) {
                pointsUsedField.setText("0");
                pointsValueLabel.setText("0 VND");
            }
            updateInvoiceSummary();
        });
        
        pointsUsedField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                pointsUsedField.setText(newValue.replaceAll("[^\\d]", ""));
            } else {
                updatePointsValue();
                updateInvoiceSummary();
            }
        });
    }

    private void loadInvoices() {
        try {
            progressBar.setVisible(true);
            List<Invoice> invoices = new ArrayList<>();
            
            if (fromDatePicker.getValue() != null && toDatePicker.getValue() != null) {
                LocalDateTime startDate = fromDatePicker.getValue().atStartOfDay();
                LocalDateTime endDate = toDatePicker.getValue().plusDays(1).atStartOfDay();
                
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
                    stmt.setTimestamp(1, java.sql.Timestamp.valueOf(startDate));
                    stmt.setTimestamp(2, java.sql.Timestamp.valueOf(endDate));
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
                            invoice.setPaymentMethod(paymentMethodStr != null ? PaymentMethodEnum.valueOf(paymentMethodStr) : null);
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
                invoices = invoiceService.getRecentInvoices(30);
            }
            
            invoiceList = FXCollections.observableArrayList(invoices);
            invoiceTable.setItems(invoiceList);
            applyFilters();
            updateSummaryLabels();
            progressBar.setVisible(false);
            statusMessageLabel.setText("Đã tải " + invoices.size() + " hóa đơn");
        } catch (Exception e) {
            progressBar.setVisible(false);
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải danh sách hóa đơn", e.getMessage());
            statusMessageLabel.setText("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeInvoiceCreation() {
        Object bookingIdObj = Session.getInstance().getAttribute("selectedBookingId");
        if (bookingIdObj instanceof Integer) {
            int bookingId = (Integer) bookingIdObj;
            
            try {
                String sql = "SELECT b.booking_id, b.booking_time, " +
                             "c.customer_id, c.point, p.person_id, p.full_name, p.phone, p.email, p.address, " +
                             "pet.pet_id, pet.name AS pet_name " +
                             "FROM booking b " +
                             "JOIN customer c ON b.customer_id = c.customer_id " +
                             "JOIN person p ON c.customer_id = p.person_id " +
                             "JOIN pet ON b.pet_id = pet.pet_id " +
                             "WHERE b.booking_id = ?"; 
                
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    
                    stmt.setInt(1, bookingId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            int customerId = rs.getInt("customer_id");
                            String customerName = rs.getString("full_name");
                            String customerPhone = rs.getString("phone");
                            String customerEmail = rs.getString("email");
                            int points = rs.getInt("point");
                            
                            customerIdField.setText("KH-" + String.format("%05d", customerId));
                            customerNameField.setText(customerName);
                            customerPhoneField.setText(customerPhone);
                          
                            customerPointsLabel.setText(String.valueOf(points));
                            
                            invoiceIdLabel.setText("HĐ-" + String.format("%05d", getNextInvoiceId()));
                            invoiceDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                            
                            // Cập nhật currentCustomerId và tải dịch vụ
                            currentCustomerId = customerId;
                            loadCustomerServices(customerId);
                            
                            loadBookingServices(bookingId);
                        }
                    }
                }
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Lỗi", "Không thể tải thông tin booking", e.getMessage());
            }
        } else {
            resetInvoiceForm();
        }
    }
    
    private int getNextInvoiceId() {
        try {
            String sql = "SELECT MAX(invoice_id) AS max_id FROM invoice";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    int maxId = rs.getInt("max_id");
                    return maxId + 1;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy ID hóa đơn tiếp theo: " + e.getMessage());
        }
        return 1;
    }
    
    private void loadBookingServices(int bookingId) {
        try {
            String sql = "SELECT bd.*, s.name, s.price " +
                        "FROM booking_detail bd " +
                        "JOIN service s ON bd.service_id = s.service_id " +
                        "WHERE bd.booking_id = ?";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, bookingId);
                try (ResultSet rs = stmt.executeQuery()) {
                    invoiceItems.clear();
                    int index = 1;
                    
                    while (rs.next()) {
                        String serviceName = rs.getString("name");
                        int quantity = rs.getInt("quantity");
                        double price = rs.getDouble("price");
                        double total = quantity * price;
                        
                        invoiceItems.add(new InvoiceItem(index++, serviceName, quantity, price, total));
                    }
                    
                    // Lưu danh sách dịch vụ vào Map cho khách hàng hiện tại
                    if (currentCustomerId != null) {
                        customerServicesMap.put(currentCustomerId, invoiceItems);
                    }
                    
                    updateInvoiceSummary();
                }
            }
            
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải dịch vụ", e.getMessage());
        }
    }

    private void updateInvoiceSummary() {
        double subtotal = invoiceItems.stream().mapToDouble(InvoiceItem::getTotal).sum();
        double discount = 0;
        double pointsValue = 0;
        double promotionDiscount = 0;
        
        try {
            if (discountField.getText() != null && !discountField.getText().isEmpty()) {
                double discountPercent = Double.parseDouble(discountField.getText());
                if (discountPercent > 0 && discountPercent <= 100) {
                    discount = subtotal * discountPercent / 100;
                }
            }
        } catch (NumberFormatException e) {
            // Bỏ qua nếu không phải số
        }
        
        if (usePointsCheckbox.isSelected() && pointsUsedField.getText() != null && !pointsUsedField.getText().isEmpty()) {
            try {
                int pointsUsed = Integer.parseInt(pointsUsedField.getText());
                pointsValue = pointsUsed * 1000;
                pointsValueLabel.setText(String.format("%,d VND", pointsValue));
            } catch (NumberFormatException e) {
                // Bỏ qua nếu không phải số
            }
        }
        
        if (promotionCodeField.getText() != null && !promotionCodeField.getText().isEmpty()) {
            try {
                String sql = "SELECT discount_percent FROM promotion WHERE code = ? AND active = TRUE AND start_date <= ? AND end_date >= ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, promotionCodeField.getText());
                    stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                    stmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            double promoPercent = rs.getInt("discount_percent");
                            promotionDiscount = subtotal * promoPercent / 100;
                        }
                    }
                }
            } catch (SQLException e) {
                // Bỏ qua nếu mã khuyến mãi không hợp lệ
            }
        }
        
        double total = subtotal - discount - pointsValue - promotionDiscount;
        if (total < 0) total = 0;
        
        subtotalLabel.setText(String.format("%,.0f VND", subtotal));
        discountAmountLabel.setText(String.format("%,.0f VND", discount + promotionDiscount));
        totalAmountLabel.setText(String.format("%,.0f VND", total));
        
        updateChangeAmount();
    }
    
    private void updateChangeAmount() {
        try {
            if (amountPaidField.getText() != null && !amountPaidField.getText().isEmpty()) {
                double amountPaid = Double.parseDouble(amountPaidField.getText());
                String totalText = totalAmountLabel.getText().replaceAll("[^\\d]", "");
                double total = Double.parseDouble(totalText);
                double change = amountPaid - total;
                changeAmountLabel.setText(String.format("%,.0f VND", Math.max(0, change)));
            }
        } catch (NumberFormatException e) {
            changeAmountLabel.setText("0 VND");
        }
    }
    
    private void updatePointsValue() {
        try {
            if (pointsUsedField.getText() != null && !pointsUsedField.getText().isEmpty()) {
                int pointsUsed = Integer.parseInt(pointsUsedField.getText());
                double pointsValue = pointsUsed * 1000;
                pointsValueLabel.setText(String.format("%,d VND", pointsValue));
                
                try {
                    int availablePoints = Integer.parseInt(customerPointsLabel.getText());
                    if (pointsUsed > availablePoints) {
                        showAlert(AlertType.WARNING, "Cảnh báo", 
                                "Điểm sử dụng vượt quá điểm hiện có", 
                                "Khách hàng chỉ có " + availablePoints + " điểm.");
                        pointsUsedField.setText(String.valueOf(availablePoints));
                    }
                } catch (NumberFormatException e) {
                    // Bỏ qua nếu nhãn điểm không phải số
                }
            }
        } catch (NumberFormatException e) {
            pointsValueLabel.setText("0 VND");
        }
    }

    private void handleInvoiceSelection(Invoice invoice) {
        selectedInvoice = invoice;
        boolean hasSelectedInvoice = invoice != null;
        
        viewDetailsButton.setDisable(!hasSelectedInvoice);
        applyDiscountButton.setDisable(!hasSelectedInvoice);
        refundButton.setDisable(!hasSelectedInvoice || 
                (hasSelectedInvoice && !StatusEnum.COMPLETED.equals(invoice.getStatus())));
        if (processPaymentButton != null) {
            processPaymentButton.setDisable(!hasSelectedInvoice || 
                    (hasSelectedInvoice && !StatusEnum.PENDING.equals(invoice.getStatus())));
        }
        if (processPaymentAndPrintButton != null) {
            processPaymentAndPrintButton.setDisable(!hasSelectedInvoice || 
                    (hasSelectedInvoice && !StatusEnum.PENDING.equals(invoice.getStatus())));
        }
        
        if (hasSelectedInvoice) {
            loadInvoiceDetails(invoice);
        }
    }
    
    private void loadInvoiceDetails(Invoice invoice) {
        try {
            invoiceItems.clear();
            
            invoiceIdLabel.setText("HĐ-" + String.format("%05d", invoice.getInvoiceId()));
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            if (invoice.getPaymentDate() != null) {
                invoiceDateLabel.setText(invoice.getPaymentDate().toLocalDateTime().format(formatter));
            } else {
                invoiceDateLabel.setText("N/A");
            }
            
            Staff cashier = invoice.getStaff();
            cashierNameLabel.setText(cashier != null ? cashier.getFullName() : "N/A");
            
            Order order = invoice.getOrder();
            if (order != null && order.getCustomer() != null) {
                Customer customer = order.getCustomer();
                customerIdField.setText("KH-" + String.format("%05d", customer.getId()));
                customerNameField.setText(customer.getFullName() != null ? customer.getFullName() : "N/A");
                customerPhoneField.setText(customer.getPhone() != null ? customer.getPhone() : "N/A");
           
                customerPointsLabel.setText(String.valueOf(customer.getPoint()));
                
                // Cập nhật currentCustomerId và tải dịch vụ
                currentCustomerId = customer.getId();
                loadCustomerServices(currentCustomerId);
            } else {
                customerIdField.clear();
                customerNameField.clear();
                customerPhoneField.clear();
                
                customerPointsLabel.setText("0");
                currentCustomerId = null;
                invoiceItems.clear();
            }
            
            if (order != null) {
                String sql = "SELECT s.name, od.quantity, od.price " +
                            "FROM order_detail od " +
                            "JOIN service s ON od.service_id = s.service_id " +
                            "WHERE od.order_id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, order.getOrderId());
                    try (ResultSet rs = stmt.executeQuery()) {
                        int index = 1;
                        while (rs.next()) {
                            String serviceName = rs.getString("name");
                            int quantity = rs.getInt("quantity");
                            double unitPrice = rs.getDouble("price");
                            double total = unitPrice * quantity;
                            invoiceItems.add(new InvoiceItem(index++, serviceName, quantity, unitPrice, total));
                        }
                    }
                }
                
                // Lưu danh sách dịch vụ vào Map
                if (currentCustomerId != null) {
                    customerServicesMap.put(currentCustomerId, invoiceItems);
                }
            }
            
            subtotalLabel.setText(String.format("%,.0f VND", 
                    invoice.getSubtotal() != null ? invoice.getSubtotal().doubleValue() : 0));
            discountField.setText(invoice.getDiscountPercent() != null ? 
                    invoice.getDiscountPercent().toString() : "0");
            discountAmountLabel.setText(String.format("%,.0f VND", 
                    invoice.getDiscountAmount() != null ? invoice.getDiscountAmount().doubleValue() : 0));
            
            Integer pointsUsed = invoice.getPointsUsed();
            int pointsUsedValue = (pointsUsed != null) ? pointsUsed : 0;
            pointsUsedField.setText(String.valueOf(pointsUsedValue));
            pointsValueLabel.setText(String.format("%,d VND", pointsUsedValue * 1000));
            usePointsCheckbox.setSelected(pointsUsedValue > 0);
            
            promotionCodeField.setText(invoice.getPromotionCode() != null ? 
                    invoice.getPromotionCode() : "");
            
            totalAmountLabel.setText(String.format("%,.0f VND", 
                    invoice.getTotal() != null ? invoice.getTotal().doubleValue() : 0));
            if (invoice.getPaymentMethod() != null) {
                paymentMethodComboBox.setValue(invoice.getPaymentMethod().name());
            }
            
            amountPaidField.setText(invoice.getAmountPaid() != null ? 
                    invoice.getAmountPaid().toString() : "0");
            double amountPaid = invoice.getAmountPaid() != null ? 
                    invoice.getAmountPaid().doubleValue() : 0;
            double total = invoice.getTotal() != null ? 
                    invoice.getTotal().doubleValue() : 0;
            changeAmountLabel.setText(String.format("%,.0f VND", Math.max(0, amountPaid - total)));
            
            invoiceNoteField.setText(invoice.getNote() != null ? invoice.getNote() : "");
            
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải chi tiết hóa đơn", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void addServiceToInvoice() {
        if (currentCustomerId == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Chưa chọn khách hàng", 
                    "Vui lòng chọn hoặc thêm khách hàng trước khi thêm dịch vụ.");
            return;
        }

        if (serviceSelector.getValue() == null || serviceSelector.getValue().isEmpty()) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Dịch vụ trống", 
                    "Vui lòng chọn một dịch vụ.");
            return;
        }
        
        int quantity = 1;
        try {
            if (quantityField.getText() != null && !quantityField.getText().isEmpty()) {
                quantity = Integer.parseInt(quantityField.getText());
                if (quantity <= 0) {
                    showAlert(AlertType.WARNING, "Cảnh báo", "Số lượng không hợp lệ", 
                            "Số lượng phải lớn hơn 0.");
                    return;
                }
            }
        } catch (NumberFormatException e) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Số lượng không hợp lệ", 
                    "Vui lòng nhập một số hợp lệ.");
            return;
        }
        
        try {
            String sql = "SELECT name, price FROM service WHERE name = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, serviceSelector.getValue());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String serviceName = rs.getString("name");
                        double unitPrice = rs.getDouble("price");
                        
                        Optional<InvoiceItem> existingItem = invoiceItems.stream()
                                .filter(item -> item.getServiceName().equals(serviceName))
                                .findFirst();
                        
                        if (existingItem.isPresent()) {
                            InvoiceItem item = existingItem.get();
                            int newQuantity = item.getQuantity() + quantity;
                            item.setQuantity(newQuantity);
                            item.setTotal(item.getUnitPrice() * newQuantity);
                            invoiceItemsTable.refresh();
                        } else {
                            invoiceItems.add(new InvoiceItem(
                                    invoiceItems.size() + 1,
                                    serviceName,
                                    quantity,
                                    unitPrice,
                                    unitPrice * quantity
                            ));
                        }
                        
                        // Cập nhật danh sách dịch vụ vào Map
                        if (currentCustomerId != null) {
                            customerServicesMap.put(currentCustomerId, invoiceItems);
                        }
                        
                        serviceSelector.setValue(null);
                        quantityField.setText("1");
                        
                        updateInvoiceSummary();
                    } else {
                        showAlert(AlertType.WARNING, "Cảnh báo", "Dịch vụ không tồn tại", 
                                "Không tìm thấy dịch vụ " + serviceSelector.getValue());
                    }
                }
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể thêm dịch vụ", e.getMessage());
        }
    }

    @FXML
    private void processPaymentAndPrint() {
        processPaymentLogic(true);
    }

    @FXML
    private void processPayment() {
        processPaymentLogic(false);
    }

    private void processPaymentLogic(boolean print) {
        try {
            if (invoiceItems.isEmpty()) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Hóa đơn trống", 
                        "Vui lòng thêm ít nhất một dịch vụ vào hóa đơn.");
                return;
            }
            
            double total = Double.parseDouble(totalAmountLabel.getText().replaceAll("[^\\d]", ""));
            
            if (amountPaidField.getText().isEmpty() || 
                Double.parseDouble(amountPaidField.getText()) < total) {
                amountPaidField.setText(String.valueOf(total));
            }
            
            double amountPaid = Double.parseDouble(amountPaidField.getText());
            
            if (customerIdField.getText().isEmpty()) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Thiếu thông tin khách hàng", 
                        "Vui lòng tìm hoặc thêm khách hàng trước khi thanh toán.");
                return;
            }

            if (selectedInvoice != null && StatusEnum.PENDING.equals(selectedInvoice.getStatus())) {
                String customerIdStr = customerIdField.getText().replace("KH-", "");
                int customerId = Integer.parseInt(customerIdStr);
                int invoiceId = selectedInvoice.getInvoiceId();
                int orderId = selectedInvoice.getOrder().getOrderId();

                String deleteOrderDetailSql = "DELETE FROM order_detail WHERE order_id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(deleteOrderDetailSql)) {
                    stmt.setInt(1, orderId);
                    stmt.executeUpdate();
                }

                String insertOrderDetailSql = "INSERT INTO order_detail (order_id, service_id, quantity, price) VALUES (?, ?, ?, ?)";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(insertOrderDetailSql)) {
                    for (InvoiceItem item : invoiceItems) {
                        String serviceName = item.getServiceName();
                        String getServiceIdSql = "SELECT service_id FROM service WHERE name = ?";
                        int serviceId;
                        try (PreparedStatement serviceStmt = conn.prepareStatement(getServiceIdSql)) {
                            serviceStmt.setString(1, serviceName);
                            try (ResultSet rs = serviceStmt.executeQuery()) {
                                if (rs.next()) {
                                    serviceId = rs.getInt("service_id");
                                } else {
                                    throw new SQLException("Không tìm thấy dịch vụ: " + serviceName);
                                }
                            }
                        }
                        
                        stmt.setInt(1, orderId);
                        stmt.setInt(2, serviceId);
                        stmt.setInt(3, item.getQuantity());
                        stmt.setDouble(4, item.getUnitPrice());
                        stmt.executeUpdate();
                    }
                }

                String updateInvoiceSql = "UPDATE invoice SET payment_date = ?, subtotal = ?, discount_percent = ?, discount_amount = ?, points_used = ?, promotion_code = ?, total = ?, amount_paid = ?, payment_method = ?, status = ?, note = ? WHERE invoice_id = ?";
                double subtotal = invoiceItems.stream().mapToDouble(InvoiceItem::getTotal).sum();
                double discountPercent = discountField.getText().isEmpty() ? 0 : Double.parseDouble(discountField.getText());
                double discountAmount = discountAmountLabel.getText().isEmpty() ? 0 : 
                        Double.parseDouble(discountAmountLabel.getText().replaceAll("[^\\d]", ""));
                int pointsUsed = pointsUsedField.getText().isEmpty() ? 0 : Integer.parseInt(pointsUsedField.getText());

                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(updateInvoiceSql)) {
                    stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.setBigDecimal(2, BigDecimal.valueOf(subtotal));
                    stmt.setBigDecimal(3, BigDecimal.valueOf(discountPercent));
                    stmt.setBigDecimal(4, BigDecimal.valueOf(discountAmount));
                    stmt.setInt(5, pointsUsed);
                    stmt.setString(6, promotionCodeField.getText().isEmpty() ? null : promotionCodeField.getText());
                    stmt.setDouble(7, total);
                    stmt.setBigDecimal(8, BigDecimal.valueOf(amountPaid));
                    stmt.setString(9, paymentMethodComboBox.getValue());
                    stmt.setString(10, StatusEnum.COMPLETED.name());
                    stmt.setString(11, invoiceNoteField.getText());
                    stmt.setInt(12, invoiceId);
                    stmt.executeUpdate();
                }

                if (usePointsCheckbox.isSelected() && pointsUsed > 0) {
                    String updatePointsSql = "UPDATE customer SET point = point - ? WHERE customer_id = ?";
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(updatePointsSql)) {
                        stmt.setInt(1, pointsUsed);
                        stmt.setInt(2, customerId);
                        stmt.executeUpdate();
                    }
                }

                if (print) {
                    String fileName = "invoice_" + invoiceId + ".pdf";
                    invoiceService.generateInvoicePDF(orderId, fileName);
                    
                    File file = new File(fileName);
                    if (file.exists()) {
                        java.awt.Desktop.getDesktop().open(file);
                    }
                }

                showAlert(AlertType.INFORMATION, "Thành công", print ? "Thanh toán và in hóa đơn" : "Thanh toán hóa đơn", 
                        "Hóa đơn #" + invoiceId + " đã được thanh toán thành công.");

                // Xóa dịch vụ của khách hàng khỏi Map sau khi thanh toán
                if (currentCustomerId != null) {
                    customerServicesMap.remove(currentCustomerId);
                }
            } else {
                String customerIdStr = customerIdField.getText().replace("KH-", "");
                int customerId = Integer.parseInt(customerIdStr);
                
                String insertOrderSql = "INSERT INTO `order` (customer_id, order_date, total_amount, status) VALUES (?, ?, ?, ?)";
                int orderId;
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(insertOrderSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, customerId);
                    stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.setDouble(3, total);
                    stmt.setString(4, StatusEnum.COMPLETED.name());
                    stmt.executeUpdate();
                    
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            orderId = rs.getInt(1);
                        } else {
                            throw new SQLException("Không thể lấy order_id sau khi thêm đơn hàng.");
                        }
                    }
                }
                
                String insertOrderDetailSql = "INSERT INTO order_detail (order_id, service_id, quantity, price) VALUES (?, ?, ?, ?)";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(insertOrderDetailSql)) {
                    for (InvoiceItem item : invoiceItems) {
                        String serviceName = item.getServiceName();
                        String getServiceIdSql = "SELECT service_id FROM service WHERE name = ?";
                        int serviceId;
                        try (PreparedStatement serviceStmt = conn.prepareStatement(getServiceIdSql)) {
                            serviceStmt.setString(1, serviceName);
                            try (ResultSet rs = serviceStmt.executeQuery()) {
                                if (rs.next()) {
                                    serviceId = rs.getInt("service_id");
                                } else {
                                    throw new SQLException("Không tìm thấy dịch vụ: " + serviceName);
                                }
                            }
                        }
                        
                        stmt.setInt(1, orderId);
                        stmt.setInt(2, serviceId);
                        stmt.setInt(3, item.getQuantity());
                        stmt.setDouble(4, item.getUnitPrice());
                        stmt.executeUpdate();
                    }
                }
                
                String insertInvoiceSql = "INSERT INTO invoice (invoice_id, order_id, payment_date, subtotal, discount_percent, discount_amount, points_used, promotion_code, total, amount_paid, payment_method, status, staff_id, note) " +
                                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                int invoiceId = Integer.parseInt(invoiceIdLabel.getText().replace("HĐ-", ""));
                
                double subtotal = invoiceItems.stream().mapToDouble(InvoiceItem::getTotal).sum();
                double discountPercent = discountField.getText().isEmpty() ? 0 : Double.parseDouble(discountField.getText());
                double discountAmount = discountAmountLabel.getText().isEmpty() ? 0 : 
                        Double.parseDouble(discountAmountLabel.getText().replaceAll("[^\\d]", ""));
                int pointsUsed = pointsUsedField.getText().isEmpty() ? 0 : Integer.parseInt(pointsUsedField.getText());
                
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(insertInvoiceSql)) {
                    stmt.setInt(1, invoiceId);
                    stmt.setInt(2, orderId);
                    stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.setBigDecimal(4, BigDecimal.valueOf(subtotal));
                    stmt.setBigDecimal(5, BigDecimal.valueOf(discountPercent));
                    stmt.setBigDecimal(6, BigDecimal.valueOf(discountAmount));
                    stmt.setInt(7, pointsUsed);
                    stmt.setString(8, promotionCodeField.getText().isEmpty() ? null : promotionCodeField.getText());
                    stmt.setDouble(9, total);
                    stmt.setBigDecimal(10, BigDecimal.valueOf(amountPaid));
                    stmt.setString(11, paymentMethodComboBox.getValue());
                    stmt.setString(12, StatusEnum.COMPLETED.name());
                    stmt.setInt(13, Session.getCurrentStaff().getId());
                    stmt.setString(14, invoiceNoteField.getText());
                    stmt.executeUpdate();
                }
                
                if (usePointsCheckbox.isSelected() && pointsUsed > 0) {
                    String updatePointsSql = "UPDATE customer SET point = point - ? WHERE customer_id = ?";
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(updatePointsSql)) {
                        stmt.setInt(1, pointsUsed);
                        stmt.setInt(2, customerId);
                        stmt.executeUpdate();
                    }
                }
                
                if (print) {
                    String fileName = "invoice_" + invoiceId + ".pdf";
                    invoiceService.generateInvoicePDF(orderId, fileName);
                    
                    File file = new File(fileName);
                    if (file.exists()) {
                        java.awt.Desktop.getDesktop().open(file);
                    }
                }
                
                showAlert(AlertType.INFORMATION, "Thành công", print ? "Thanh toán và in hóa đơn" : "Thanh toán hóa đơn", 
                        "Hóa đơn đã được lưu và in thành công.");

                // Xóa dịch vụ của khách hàng khỏi Map sau khi thanh toán
                if (currentCustomerId != null) {
                    customerServicesMap.remove(currentCustomerId);
                }
            }
            
            resetInvoiceForm();
            loadInvoices();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", print ? "Không thể xử lý thanh toán và in" : "Không thể xử lý thanh toán", e.getMessage());
        }
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
        if (fromDatePicker.getValue() == null || toDatePicker.getValue() == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Ngày không hợp lệ", 
                    "Vui lòng chọn khoảng thời gian.");
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
    private void searchCustomer() {
        String searchQuery = customerSearchField.getText().trim();
        
        if (searchQuery.isEmpty()) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Tìm kiếm trống", 
                    "Vui lòng nhập số điện thoại để tìm kiếm khách hàng.");
            return;
        }
        
        try {
            String sql = "SELECT c.customer_id, c.point, p.full_name, p.phone, p.email " +
                        "FROM customer c JOIN person p ON c.customer_id = p.person_id " +
                        "WHERE p.phone = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, searchQuery);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int customerId = rs.getInt("customer_id");
                        customerIdField.setText("KH-" + String.format("%05d", customerId));
                        customerNameField.setText(rs.getString("full_name"));
                        customerPhoneField.setText(rs.getString("phone"));
                        customerPointsLabel.setText(String.valueOf(rs.getInt("point")));
                        
                        // Cập nhật currentCustomerId và tải dịch vụ của khách hàng
                        currentCustomerId = customerId;
                        loadCustomerServices(customerId);
                    } else {
                        showAlert(AlertType.INFORMATION, "Thông báo", "Không tìm thấy", 
                                "Không tìm thấy khách hàng với số điện thoại " + searchQuery);
                    }
                }
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tìm kiếm khách hàng", e.getMessage());
        }
    }

    // Tải danh sách dịch vụ của khách hàng từ Map
    private void loadCustomerServices(int customerId) {
        invoiceItems.clear();
        ObservableList<InvoiceItem> customerItems = customerServicesMap.get(customerId);
        if (customerItems != null) {
            invoiceItems.addAll(customerItems);
        }
        invoiceItemsTable.setItems(invoiceItems);
        updateInvoiceSummary();
    }

    @FXML
    private void onSearchButtonClick() {
        searchInvoices();
    }

    @FXML
    private void viewDetails() {
        if (selectedInvoice == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Không có hóa đơn được chọn", 
                    "Vui lòng chọn một hóa đơn để xem chi tiết.");
            return;
        }
        
        try {
            Stage detailStage = new Stage();
            VBox detailPane = new VBox(10);
            detailPane.setPadding(new Insets(10));
            
            Label titleLabel = new Label("Chi tiết hóa đơn #" + selectedInvoice.getInvoiceId());
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
            
            String customerName = "N/A";
            String customerPhone = "N/A";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT p.full_name, p.phone " +
                     "FROM invoice i " +
                     "JOIN `order` o ON i.order_id = o.order_id " +
                     "JOIN customer c ON o.customer_id = c.customer_id " +
                     "JOIN person p ON c.customer_id = p.person_id " +
                     "WHERE i.invoice_id = ?")) {
                stmt.setInt(1, selectedInvoice.getInvoiceId());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        customerName = rs.getString("full_name");
                        customerPhone = rs.getString("phone");
                    }
                }
            }
            
            Label customerLabel = new Label("Khách hàng: " + customerName);
            Label phoneLabel = new Label("Số điện thoại: " + customerPhone);
            Label dateLabel = new Label("Ngày thanh toán: " + 
                    (selectedInvoice.getPaymentDate() != null ? 
                            selectedInvoice.getPaymentDate().toLocalDateTime().format(
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A"));
            Label totalLabel = new Label("Tổng tiền: " + 
                    String.format("%,.0f VND", selectedInvoice.getTotal() != null ? 
                            selectedInvoice.getTotal().doubleValue() : 0));
            Label servicesLabel = new Label("Dịch vụ: ");
            
            StringBuilder servicesText = new StringBuilder();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT s.name, od.quantity " +
                     "FROM invoice i " +
                     "JOIN `order` o ON i.order_id = o.order_id " +
                     "JOIN order_detail od ON o.order_id = od.order_id " +
                     "JOIN service s ON od.service_id = s.service_id " +
                     "WHERE i.invoice_id = ?")) {
                stmt.setInt(1, selectedInvoice.getInvoiceId());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        servicesText.append(rs.getString("name"))
                                   .append(" (SL: ").append(rs.getInt("quantity")).append("), ");
                    }
                }
            }
            Label servicesDetailLabel = new Label(servicesText.length() > 0 ? 
                    servicesText.substring(0, servicesText.length() - 2) : "Không có dịch vụ");
            
            Button reprintButton = new Button("In lại");
            reprintButton.setOnAction(e -> reprintInvoice());
            
            HBox buttonBox = new HBox(10, reprintButton);
            
            detailPane.getChildren().addAll(titleLabel, customerLabel, phoneLabel, 
                    dateLabel, totalLabel, servicesLabel, servicesDetailLabel, buttonBox);
            
            Scene scene = new Scene(detailPane, 400, 300);
            detailStage.setTitle("Chi tiết hóa đơn");
            detailStage.setScene(scene);
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.showAndWait();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể mở chi tiết hóa đơn", e.getMessage());
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
    private void createNewInvoice() {
        resetInvoiceForm();
        if (tabPane != null) {
            tabPane.getSelectionModel().select(1);
        }
    }

    private void resetInvoiceForm() {
        customerIdField.clear();
        customerNameField.clear();
        customerPhoneField.clear();
       
        customerPointsLabel.setText("0");
        invoiceItems.clear();
        currentCustomerId = null; // Reset customerId
        subtotalLabel.setText("0 VND");
        discountField.setText("0");
        discountAmountLabel.setText("0 VND");
        pointsUsedField.setText("0");
        pointsValueLabel.setText("0 VND");
        usePointsCheckbox.setSelected(false);
        totalAmountLabel.setText("0 VND");
        amountPaidField.setText("0");
        changeAmountLabel.setText("0 VND");
        invoiceNoteField.clear();
        promotionCodeField.clear();
        invoiceIdLabel.setText("HĐ-" + String.format("%05d", getNextInvoiceId()));
        invoiceDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
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
        dialog.setHeaderText("Nhập phần trăm khuyến mãi");
        dialog.setContentText("Phần trăm giảm giá:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(percent -> {
            try {
                int discountPercent = Integer.parseInt(percent);
                if (discountPercent <= 0 || discountPercent > 100) {
                    showAlert(AlertType.WARNING, "Cảnh báo", "Giá trị không hợp lệ", 
                            "Phần trăm khuyến mãi phải nằm trong khoảng 1-100%.");
                    return;
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
                
                String updateInvoiceSql = "UPDATE invoice SET discount_percent = ?, discount_amount = ?, total = ? WHERE invoice_id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(updateInvoiceSql)) {
                    stmt.setDouble(1, discountPercent);
                    stmt.setDouble(2, discountAmount);
                    stmt.setDouble(3, newTotal);
                    stmt.setInt(4, selectedInvoice.getInvoiceId());
                    stmt.executeUpdate();
                }
                
                showAlert(AlertType.INFORMATION, "Thành công", "Đã áp dụng khuyến mãi", 
                        "Đã áp dụng khuyến mãi " + discountPercent + "% vào hóa đơn.");
                
                loadInvoices();
            } catch (NumberFormatException e) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Giá trị không hợp lệ", 
                        "Vui lòng nhập một số nguyên cho phần trăm khuyến mãi.");
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Lỗi", "Không thể áp dụng khuyến mãi", e.getMessage());
            }
        });
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
    private void addNewCustomer() {
        try {
            Dialog<Customer> dialog = new Dialog<>();
            dialog.setTitle("Thêm khách hàng mới");
            dialog.setHeaderText("Nhập thông tin khách hàng mới");
            
            ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            TextField nameField = new TextField();
            nameField.setPromptText("Họ và tên");
            TextField phoneField = new TextField();
            phoneField.setPromptText("Số điện thoại");
            
            grid.add(new Label("Họ và tên:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(new Label("Số điện thoại:"), 0, 1);
            grid.add(phoneField, 1, 1);
            
            Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
            saveButton.setDisable(true);
            
            nameField.textProperty().addListener((observable, oldValue, newValue) -> {
                saveButton.setDisable(newValue.trim().isEmpty() || phoneField.getText().trim().isEmpty());
            });
            
            phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
                saveButton.setDisable(newValue.trim().isEmpty() || nameField.getText().trim().isEmpty());
            });
            
            dialog.getDialogPane().setContent(grid);
            
            Platform.runLater(() -> nameField.requestFocus());
            
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                    	String sql = "INSERT INTO person (full_name, phone, address) VALUES (?, ?, NULL)";
                        try (Connection conn = DatabaseConnection.getConnection();
                             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                            stmt.setString(1, nameField.getText().trim());
                            stmt.setString(2, phoneField.getText().trim());
                            int affectedRows = stmt.executeUpdate();
                            
                            if (affectedRows > 0) {
                                try (ResultSet rs = stmt.getGeneratedKeys()) {
                                    if (rs.next()) {
                                        int personId = rs.getInt(1);
                                        
                                        String customerSql = "INSERT INTO customer (customer_id, point) VALUES (?, 0)";
                                        try (PreparedStatement customerStmt = conn.prepareStatement(customerSql)) {
                                            customerStmt.setInt(1, personId);
                                            customerStmt.executeUpdate();
                                            
                                            Customer newCustomer = new Customer();
                                            newCustomer.setId(personId);
                                            newCustomer.setFullName(nameField.getText().trim());
                                            newCustomer.setPhone(phoneField.getText().trim());
                                            newCustomer.setPoint(0);
                                            
                                            return newCustomer;
                                        }
                                    }
                                }
                            }
                        }
                    } catch (SQLException ex) {
                        showAlert(AlertType.ERROR, "Lỗi", "Không thể thêm khách hàng", ex.getMessage());
                        ex.printStackTrace();
                    }
                }
                return null;
            });
            
            Optional<Customer> result = dialog.showAndWait();
            
            result.ifPresent(customer -> {
                customerIdField.setText("KH-" + String.format("%05d", customer.getId()));
                customerNameField.setText(customer.getFullName());
                customerPhoneField.setText(customer.getPhone());
                customerPointsLabel.setText("0");
                
                customerSearchField.setText(customer.getPhone());
                
                // Cập nhật currentCustomerId và tải dịch vụ
                currentCustomerId = customer.getId();
                loadCustomerServices(currentCustomerId);
                
                showAlert(AlertType.INFORMATION, "Thành công", "Thêm khách hàng thành công", 
                        "Đã thêm khách hàng " + customer.getFullName() + " vào hệ thống.");
            });
            
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể hiển thị form thêm khách hàng", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void applyPromotionCode() {
        String code = promotionCodeField.getText().trim();
        if (code.isEmpty()) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Mã khuyến mãi trống", 
                    "Vui lòng nhập mã khuyến mãi.");
            return;
        }
        
        try {
            String sql = "SELECT discount_percent FROM promotion WHERE code = ? AND active = TRUE AND start_date <= ? AND end_date >= ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, code);
                stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                stmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        double promoPercent = rs.getInt("discount_percent");
                        showAlert(AlertType.INFORMATION, "Thành công", "Áp dụng mã khuyến mãi", 
                                "Mã " + code + " được áp dụng với giảm giá " + promoPercent + "%.");
                        updateInvoiceSummary();
                    } else {
                        showAlert(AlertType.WARNING, "Cảnh báo", "Mã khuyến mãi không hợp lệ", 
                                "Mã " + code + " không tồn tại hoặc đã hết hạn.");
                        promotionCodeField.clear();
                    }
                }
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể áp dụng mã khuyến mãi", e.getMessage());
        }
    }

    @FXML
    private void cancelInvoice() {
        resetInvoiceForm();
    }

    @FXML
    private void resetFilter() {
        fromDatePicker.setValue(LocalDate.now().minusDays(30));
        toDatePicker.setValue(LocalDate.now());
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
                "1. Xem danh sách hóa đơn: Chọn khoảng thời gian và nhấn 'Tìm kiếm' để xem danh sách hóa đơn.\n\n" +
                "2. Tìm kiếm hóa đơn: Nhập mã hóa đơn, mã đơn hàng, tên khách hàng hoặc số điện thoại vào ô tìm kiếm.\n\n" +
                "3. Lọc hóa đơn: Sử dụng bộ lọc trạng thái và phương thức thanh toán để thu hẹp danh sách.\n\n" +
                "4. Tạo hóa đơn mới: Nhấn 'Tạo hóa đơn mới' để chuyển sang tab tạo hóa đơn.\n\n" +
                "5. Tìm khách hàng: Nhập số điện thoại vào ô tìm kiếm khách hàng trong tab 'Tạo hóa đơn mới'.\n\n" +
                "6. Thêm dịch vụ: Chọn dịch vụ, nhập số lượng và nhấn 'Thêm' để thêm vào hóa đơn.\n\n" +
                "7. Áp dụng khuyến mãi: Nhập mã khuyến mãi hoặc phần trăm giảm giá và áp dụng.\n\n" +
                "8. Thanh toán: Nhập số tiền khách trả, chọn phương thức thanh toán và nhấn 'Thanh toán' hoặc 'Thanh toán và in'.\n\n" +
                "9. Hoàn tiền: Chọn hóa đơn đã thanh toán và nhấn 'Hoàn tiền' để thực hiện hoàn tiền.\n\n" +
                "10. Xem chi tiết: Nhấn 'Xem chi tiết' để xem thông tin chi tiết của hóa đơn và in lại nếu cần.\n\n" +
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

    public static class InvoiceItem {
        private final int index;
        private final String serviceName;
        private int quantity;
        private final double unitPrice;
        private double total;

        public InvoiceItem(int index, String serviceName, int quantity, double unitPrice, double total) {
            this.index = index;
            this.serviceName = serviceName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.total = total;
        }

        public int getIndex() { return index; }
        public String getServiceName() { return serviceName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getUnitPrice() { return unitPrice; }
        public double getTotal() { return total; }
        public void setTotal(double total) { this.total = total; }
    }
}