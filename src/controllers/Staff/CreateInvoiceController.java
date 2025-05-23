package controllers.Staff;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import enums.StatusEnum;
import enums.PaymentMethodEnum;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Booking;
import utils.DatabaseConnection;
import utils.Session;

/**
 * Controller cho màn hình tạo hóa đơn
 */
public class CreateInvoiceController implements Initializable {

	@FXML
	private Label invoiceIdLabel;
	@FXML
	private Label invoiceDateLabel;
	@FXML
	private Label cashierNameLabel;
	@FXML
	private TextField customerIdField;
	@FXML
	private TextField customerNameField;
	@FXML
	private TextField customerPhoneField;
	@FXML
	private Label customerPointLabel;
	@FXML
	private TableView<ServiceRow> invoiceItemsTable;
	@FXML
	private ComboBox<String> serviceSelector;
	@FXML
	private TextField quantityField;
	@FXML
	private Label subtotalLabel;
	@FXML
	private TextField discountField;
	@FXML
	private Label discountAmountLabel;
	@FXML
	private TextField promotionCodeField;
	@FXML
	private TextField pointsUsedField; // Sửa từ pointUsedField
	@FXML
	private Label pointValueLabel;
	@FXML
	private Label usePointCheckbox; // Note: Should be CheckBox in FXML
	@FXML
	private Label totalAmountLabel;
	@FXML
	private ComboBox<String> paymentMethodComboBox;
	@FXML
	private TextField amountPaidField;
	@FXML
	private Label changeAmountLabel;
	@FXML
	private TextArea invoiceNoteField;
	@FXML
	private Button processButton;
	@FXML
	private Button processPaymentAndPrintButton;
	@FXML
	private Button cancelButton;

	private Booking booking;
	private ObservableList<ServiceRow> serviceRows = FXCollections.observableArrayList();
	private double subtotal = 0.0;
	private double discountAmount = 0.0;
	private double pointValue = 0.0;
	private double total = 0.0;

	/**
	 * Khởi tạo dữ liệu từ booking
	 */
	public void initData(Booking booking) {
		this.booking = booking;

		if (booking != null) {
			// Hiển thị thông tin booking
			customerIdField.setText("KH-" + String.format("%05d", booking.getCustomer().getId()));
			customerNameField.setText(booking.getCustomer() != null ? booking.getCustomer().getFullName() : "N/A");
			customerPhoneField.setText(booking.getCustomer() != null ? booking.getCustomer().getPhone() : "N/A");
			customerPointLabel.setText(String.valueOf(getCustomerPoints(booking.getCustomer().getId())));

			// Tải dịch vụ từ booking
			loadServicesFromBooking();

			// Cập nhật tổng tiền
			updateTotals();
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Khởi tạo ngày hiện tại
		invoiceDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

		// Khởi tạo mã hóa đơn
		try {
			int nextId = getNextInvoiceId();
			invoiceIdLabel.setText("HD" + String.format("%05d", nextId));
		} catch (SQLException e) {
			invoiceIdLabel.setText("HDXXXXX");
			e.printStackTrace();
		}

		// Khởi tạo tên thu ngân
		cashierNameLabel.setText(Session.getCurrentStaff() != null ? Session.getCurrentStaff().getFullName() : "N/A");

		// Khởi tạo bảng dịch vụ
		initializeServicesTable();

		// Khởi tạo combobox phương thức thanh toán
		ObservableList<String> paymentMethods = FXCollections.observableArrayList();
		for (PaymentMethodEnum method : PaymentMethodEnum.values()) {
			paymentMethods.add(method.name());
		}
		paymentMethodComboBox.setItems(paymentMethods);
		paymentMethodComboBox.setValue(PaymentMethodEnum.CASH.name());

		// Khởi tạo combobox dịch vụ
		loadServicesToComboBox();

		// Thiết lập listeners
		setupListeners();
	}

	/**
	 * Khởi tạo bảng dịch vụ
	 */
	private void initializeServicesTable() {
		if (invoiceItemsTable.getColumns().size() >= 6) {
			TableColumn<ServiceRow, Integer> indexCol = (TableColumn<ServiceRow, Integer>) invoiceItemsTable
					.getColumns().get(0);
			TableColumn<ServiceRow, String> serviceNameCol = (TableColumn<ServiceRow, String>) invoiceItemsTable
					.getColumns().get(1);
			TableColumn<ServiceRow, Integer> quantityCol = (TableColumn<ServiceRow, Integer>) invoiceItemsTable
					.getColumns().get(2);
			TableColumn<ServiceRow, Double> priceCol = (TableColumn<ServiceRow, Double>) invoiceItemsTable.getColumns()
					.get(3);
			TableColumn<ServiceRow, Double> totalCol = (TableColumn<ServiceRow, Double>) invoiceItemsTable.getColumns()
					.get(4);
			TableColumn<ServiceRow, Void> deleteCol = (TableColumn<ServiceRow, Void>) invoiceItemsTable.getColumns()
					.get(5);

			// Cột STT
			indexCol.setCellValueFactory(
					cellData -> new SimpleIntegerProperty(invoiceItemsTable.getItems().indexOf(cellData.getValue()) + 1)
							.asObject());

			// Cột Tên dịch vụ
			serviceNameCol.setCellValueFactory(new PropertyValueFactory<>("serviceName"));

			// Cột Số lượng
			quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

			// Cột Đơn giá
			priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
			priceCol.setCellFactory(column -> new TableCell<ServiceRow, Double>() {
				@Override
				protected void updateItem(Double item, boolean empty) {
					super.updateItem(item, empty);
					setText(empty || item == null ? null : String.format("%,.0f VND", item));
				}
			});

			// Cột Thành tiền
			totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
			totalCol.setCellFactory(column -> new TableCell<ServiceRow, Double>() {
				@Override
				protected void updateItem(Double item, boolean empty) {
					super.updateItem(item, empty);
					setText(empty || item == null ? null : String.format("%,.0f VND", item));
				}
			});

			// Cột Xóa
			deleteCol.setCellFactory(column -> new TableCell<ServiceRow, Void>() {
				private final Button deleteButton = new Button("Xóa");

				{
					deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
					deleteButton.setOnAction(event -> {
						ServiceRow row = getTableView().getItems().get(getIndex());
						serviceRows.remove(row);
						updateTotals();
					});
				}

				@Override
				protected void updateItem(Void item, boolean empty) {
					super.updateItem(item, empty);
					setGraphic(empty ? null : deleteButton);
				}
			});

			invoiceItemsTable.setItems(serviceRows);
		}
	}

	/**
	 * Thiết lập listeners cho các trường nhập liệu
	 */
	private void setupListeners() {
		// Cập nhật giảm giá
		discountField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*(\\.\\d*)?")) {
				discountField.setText(oldValue);
			} else {
				updateDiscountAmount();
				updateTotals();
			}
		});

		// Cập nhật điểm sử dụng
		pointsUsedField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) {
				pointsUsedField.setText(oldValue);
			} else {
				updatePointsValue();
				updateTotals();
			}
		});

		// Cập nhật tiền thối lại
		amountPaidField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) {
				amountPaidField.setText(oldValue);
			} else {
				updateChange();
			}
		});
	}

	/**
	 * Lấy ID hóa đơn tiếp theo
	 */
	private int getNextInvoiceId() throws SQLException {
		String sql = "SELECT MAX(invoice_id) AS max_id FROM invoice";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			if (rs.next()) {
				return rs.getInt("max_id") + 1;
			}
			return 1;
		}
	}

	/**
	 * Tải dịch vụ từ booking
	 */
	private void loadServicesFromBooking() {
		try {
			String sql = "SELECT bd.service_id, s.name, bd.quantity, bd.price " + "FROM booking_detail bd "
					+ "JOIN service s ON bd.service_id = s.service_id " + "WHERE bd.booking_id = ?";
			try (Connection conn = DatabaseConnection.getConnection();
					PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, booking.getBookingId());
				try (ResultSet rs = stmt.executeQuery()) {
					serviceRows.clear();
					while (rs.next()) {
						String serviceName = rs.getString("name");
						int quantity = rs.getInt("quantity");
						double price = rs.getDouble("price");
						double total = quantity * price;

						serviceRows.add(new ServiceRow(serviceName, quantity, price, total));
						subtotal += total;
					}
				}
			}
		} catch (SQLException e) {
			showAlert(AlertType.ERROR, "Lỗi", "Không thể tải dịch vụ",
					"Đã xảy ra lỗi khi tải dịch vụ từ booking: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Tải danh sách dịch vụ vào ComboBox
	 */
	private void loadServicesToComboBox() {
		ObservableList<String> services = FXCollections.observableArrayList();
		String sql = "SELECT name FROM service";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				services.add(rs.getString("name"));
			}
			serviceSelector.setItems(services);
		} catch (SQLException e) {
			showAlert(AlertType.ERROR, "Lỗi", "Không thể tải danh sách dịch vụ", "Đã xảy ra lỗi: " + e.getMessage());
		}
	}

	/**
	 * Cập nhật số tiền giảm giá
	 */
	private void updateDiscountAmount() {
		try {
			double discountPercent = discountField.getText().isEmpty() ? 0.0
					: Double.parseDouble(discountField.getText());
			if (discountPercent < 0)
				discountPercent = 0;
			if (discountPercent > 100)
				discountPercent = 100;

			discountAmount = subtotal * discountPercent / 100.0;
			discountAmountLabel.setText(String.format("%,.0f VND", discountAmount));
		} catch (NumberFormatException e) {
			discountAmount = 0.0;
			discountAmountLabel.setText("0 VND");
		}
	}

	/**
	 * Cập nhật giá trị điểm sử dụng
	 */
	private void updatePointsValue() {
		try {
			int pointsUsed = pointsUsedField.getText().isEmpty() ? 0 : Integer.parseInt(pointsUsedField.getText());
			int availablePoints = Integer.parseInt(customerPointLabel.getText());
			if (pointsUsed < 0)
				pointsUsed = 0;
			if (pointsUsed > availablePoints)
				pointsUsed = availablePoints;

			// Giả sử 1 điểm = 1000 VND
			pointValue = pointsUsed * 1000.0;
			pointValueLabel.setText(String.format("%,.0f VND", pointValue));
		} catch (NumberFormatException e) {
			pointValue = 0.0;
			pointValueLabel.setText("0 VND");
		}
	}

	/**
	 * Cập nhật tổng tiền
	 */
	private void updateTotals() {
		subtotalLabel.setText(String.format("%,.0f VND", subtotal));
		total = subtotal - discountAmount - pointValue;
		if (total < 0)
			total = 0;
		totalAmountLabel.setText(String.format("%,.0f VND", total));

		updateChange();
	}

	/**
	 * Cập nhật tiền thối lại
	 */
	private void updateChange() {
		try {
			double amountPaid = amountPaidField.getText().isEmpty() ? 0.0
					: Double.parseDouble(amountPaidField.getText());
			double change = amountPaid - total;
			changeAmountLabel.setText(String.format("%,.0f VND", Math.max(0, change)));
		} catch (NumberFormatException e) {
			changeAmountLabel.setText("0 VND");
		}
	}

	/**
	 * Thêm dịch vụ vào hóa đơn
	 */
	@FXML
	private void addServiceToInvoice() {
		try {
			String serviceName = serviceSelector.getValue();
			int quantity = Integer.parseInt(quantityField.getText().trim());
			if (serviceName == null || quantity <= 0) {
				showAlert(AlertType.WARNING, "Cảnh báo", "Dữ liệu không hợp lệ",
						"Vui lòng chọn dịch vụ và nhập số lượng hợp lệ.");
				return;
			}

			double price = getServicePriceByName(serviceName);
			double total = price * quantity;

			serviceRows.add(new ServiceRow(serviceName, quantity, price, total));
			subtotal += total;
			updateTotals();

			serviceSelector.setValue(null);
			quantityField.setText("1");
		} catch (NumberFormatException e) {
			showAlert(AlertType.WARNING, "Cảnh báo", "Số lượng không hợp lệ", "Vui lòng nhập số lượng hợp lệ.");
		} catch (SQLException e) {
			showAlert(AlertType.ERROR, "Lỗi", "Không thể thêm dịch vụ", "Đã xảy ra lỗi: " + e.getMessage());
		}
	}

	/**
	 * Áp dụng mã khuyến mãi
	 */
	@FXML
	private void applyPromotionCode() {
		String promotionCode = promotionCodeField.getText().trim();
		if (promotionCode.isEmpty()) {
			showAlert(AlertType.WARNING, "Cảnh báo", "Mã khuyến mãi rỗng", "Vui lòng nhập mã khuyến mãi.");
			return;
		}

		try {
			double promotionDiscount = getPromotionDiscount(promotionCode);
			discountAmount = subtotal * promotionDiscount / 100.0;
			discountAmountLabel.setText(String.format("%,.0f VND", discountAmount));
			updateTotals();
		} catch (SQLException e) {
			showAlert(AlertType.ERROR, "Lỗi", "Không thể áp dụng mã khuyến mãi", "Đã xảy ra lỗi: " + e.getMessage());
		}
	}

	/**
	 * Xử lý thanh toán
	 */
	@FXML
	private void processPayment() {
		try {
			if (serviceRows.isEmpty()) {
				showAlert(AlertType.WARNING, "Cảnh báo", "Không có dịch vụ", "Không có dịch vụ nào để thanh toán.");
				return;
			}

			double amountPaid;
			try {
				amountPaid = amountPaidField.getText().isEmpty() ? 0.0 : Double.parseDouble(amountPaidField.getText());
			} catch (NumberFormatException e) {
				showAlert(AlertType.WARNING, "Cảnh báo", "Số tiền không hợp lệ", "Vui lòng nhập số tiền hợp lệ.");
				return;
			}

			if (amountPaid < total) {
				showAlert(AlertType.WARNING, "Cảnh báo", "Thiếu tiền", "Số tiền trả không đủ để thanh toán.");
				return;
			}

			int orderId = createOrder();
			if (orderId <= 0) {
				showAlert(AlertType.ERROR, "Lỗi", "Không thể tạo đơn hàng", "Đã xảy ra lỗi khi tạo đơn hàng.");
				return;
			}

			boolean success = createInvoice(orderId, amountPaid);
			if (!success) {
				showAlert(AlertType.ERROR, "Lỗi", "Không thể tạo hóa đơn", "Đã xảy ra lỗi khi tạo hóa đơn.");
				return;
			}

			if (booking != null) {
				booking.setStatus(StatusEnum.COMPLETED);
				updateBookingStatus();
			}

			showAlert(AlertType.INFORMATION, "Thành công", "Thanh toán thành công", "Đả tạo hóa đơn thành công.");

			Stage stage = (Stage) processButton.getScene().getWindow();
			stage.close();

		} catch (Exception e) {
			showAlert(AlertType.ERROR, "Lỗi", "Không thể xử lý thanh toán", "Đã xảy ra lỗi: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Xử lý thanh toán và in hóa đơn
	 */
	@FXML
	private void processPaymentAndPrint() {
		processPayment();
		// TODO: Thêm logic in hóa đơn
		showAlert(AlertType.INFORMATION, "Thông báo", "In hóa đơn", "Hóa đơn đã được in.");
	}

	/**
	 * Hủy hóa đơn
	 */
	@FXML
	private void cancelInvoice() {
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		stage.close();
	}

	/**
	 * Tạo đơn hàng mới
	 */
	private int createOrder() throws SQLException {
		String sql = "INSERT INTO `order` (customer_id, staff_id, order_date, total_amount, status) "
				+ "VALUES (?, ?, NOW(), ?, ?)";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
			stmt.setInt(1, booking != null ? booking.getCustomer().getId() : 0);
			stmt.setInt(2, Session.getCurrentStaff().getId());
			stmt.setDouble(3, total);
			stmt.setString(4, StatusEnum.COMPLETED.name());

			int rows = stmt.executeUpdate();
			if (rows > 0) {
				try (ResultSet rs = stmt.getGeneratedKeys()) {
					if (rs.next()) {
						int orderId = rs.getInt(1);
						addOrderDetails(orderId);
						return orderId;
					}
				}
			}
		}
		return -1;
	}

	/**
	 * Thêm chi tiết đơn hàng
	 */
	private void addOrderDetails(int orderId) throws SQLException {
		String sql = "INSERT INTO order_detail (order_id, service_id, quantity, price) VALUES (?, ?, ?, ?)";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			for (ServiceRow row : serviceRows) {
				int serviceId = getServiceIdByName(row.getServiceName());
				stmt.setInt(1, orderId);
				stmt.setInt(2, serviceId);
				stmt.setInt(3, row.getQuantity());
				stmt.setDouble(4, row.getPrice());
				stmt.addBatch();
			}
			stmt.executeBatch();
		}
	}

	/**
	 * Lấy service_id từ tên dịch vụ
	 */
	private int getServiceIdByName(String serviceName) throws SQLException {
		String sql = "SELECT service_id FROM service WHERE name = ?";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, serviceName);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("service_id");
				}
			}
		}
		throw new SQLException("Không tìm thấy dịch vụ: " + serviceName);
	}

	/**
	 * Lấy giá dịch vụ từ tên dịch vụ
	 */
	private double getServicePriceByName(String serviceName) throws SQLException {
		String sql = "SELECT price FROM service WHERE name = ?";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, serviceName);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getDouble("price");
				}
			}
		}
		throw new SQLException("Không tìm thấy dịch vụ: " + serviceName);
	}

	/**
	 * Lấy điểm tích lũy của khách hàng
	 */
	private int getCustomerPoints(int customerId) {
		String sql = "SELECT point FROM customer WHERE customer_id = ?";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, customerId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("point");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * Lấy giá trị giảm giá từ mã khuyến mãi
	 */
	private double getPromotionDiscount(String promotionCode) throws SQLException {
		// TODO: Thay bằng truy vấn database thực tế
		return 10.0; // Ví dụ: giảm giá 10%
	}

	/**
	 * Tạo hóa đơn
	 */
	private boolean createInvoice(int orderId, double amountPaid) {
		String sql = "INSERT INTO invoice (order_id, payment_date, subtotal, discount_percent, "
				+ "discount_amount, total, amount_paid, payment_method, status, staff_id, note) "
				+ "VALUES (?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			double discountPercent = discountField.getText().isEmpty() ? 0.0
					: Double.parseDouble(discountField.getText());
			stmt.setInt(1, orderId);
			stmt.setDouble(2, subtotal);
			stmt.setDouble(3, discountPercent);
			stmt.setDouble(4, discountAmount);
			stmt.setDouble(5, total);
			stmt.setDouble(6, amountPaid);
			stmt.setString(7, paymentMethodComboBox.getValue());
			stmt.setString(8, StatusEnum.COMPLETED.name());
			stmt.setInt(9, Session.getCurrentStaff().getId());
			stmt.setString(10, invoiceNoteField.getText());

			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Cập nhật trạng thái booking
	 */
	private void updateBookingStatus() {
		String sql = "UPDATE booking SET status = ? WHERE booking_id = ?";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, StatusEnum.COMPLETED.name());
			stmt.setInt(2, booking.getBookingId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Hiển thị thông báo
	 */
	private void showAlert(AlertType alertType, String title, String header, String content) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}

	/**
	 * Lớp đại diện cho một dòng trong bảng dịch vụ
	 */
	public static class ServiceRow {
		private final String serviceName;
		private final int quantity;
		private final double price;
		private final double total;

		public ServiceRow(String serviceName, int quantity, double price, double total) {
			this.serviceName = serviceName;
			this.quantity = quantity;
			this.price = price;
			this.total = total;
		}

		public String getServiceName() {
			return serviceName;
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