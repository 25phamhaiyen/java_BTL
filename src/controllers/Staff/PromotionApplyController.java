package controllers.Staff;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException; // Añadida importación de SQLException
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import controllers.SceneSwitcher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Promotion;
import model.Service;
import service.PromotionService;
import service.ServiceService;
import utils.RoleChecker;
import repository.InvoiceRepository;
import repository.OrderDetailRepository;
import model.Invoice;
import repository.OrderRepository;

public class PromotionApplyController implements Initializable {

	// FXML components - no changes needed
	@FXML
	private TableView<Service> serviceTable;
	@FXML
	private TableColumn<Service, Integer> idColumn;
	@FXML
	private TableColumn<Service, String> nameColumn;
	@FXML
	private TableColumn<Service, Double> priceColumn;
	@FXML
	private TableColumn<Service, Integer> durationColumn;
	@FXML
	private ComboBox<Promotion> promotionComboBox;
	@FXML
	private TextField promoCodeField;
	@FXML
	private Label discountLabel;
	@FXML
	private Label totalPriceLabel;
	@FXML
	private Label discountedPriceLabel;
	@FXML
	private Button applyButton;
	@FXML
	private Button backButton;
	@FXML
	private Button removePromoButton;
	@FXML
	private Label invoiceIdLabel;
	@FXML
	private Label customerNameLabel;
	@FXML
	private Label subtotalLabel;
	@FXML
	private Label discountAmountLabel;
	@FXML
	private Label pointDiscountLabel;
	@FXML
	private Label availablePointsLabel;
	@FXML
	private TextField pointsField;
	@FXML
	private Button applyPointsButton;
	@FXML
	private Button clearPointsButton;
	@FXML
	private Button saveButton;

	// Class variables
	private Invoice currentInvoice;
	private Runnable onPromotionAppliedCallback;
	private OrderDetailRepository orderDetailRepository;
	private InvoiceRepository invoiceRepository;
	private ServiceService serviceService;
	private PromotionService promotionService;
	private ObservableList<Service> serviceList;
	private ObservableList<Service> selectedServices = FXCollections.observableArrayList();
	private Promotion selectedPromotion;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// No changes needed here
		// Initialize services
		serviceService = new ServiceService();
		promotionService = new PromotionService();

		// Initialize table columns
		idColumn.setCellValueFactory(new PropertyValueFactory<>("serviceId"));
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
		durationColumn.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));

		// Format money values
		priceColumn.setCellFactory(column -> {
			return new javafx.scene.control.TableCell<Service, Double>() {
				@Override
				protected void updateItem(Double item, boolean empty) {
					super.updateItem(item, empty);
					if (empty || item == null) {
						setText(null);
					} else {
						setText(String.format("%,.0f VND", item));
					}
				}
			};
		});

		// Load services and promotions
		loadServices();
		loadPromotions();

		// Setup button visibility
		setupButtonVisibility();

		// Handle service selection events
		serviceTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null && !selectedServices.contains(newValue)) {
				selectedServices.add(newValue);
				updateTotalPrice();
			}
		});

		// Handle promotion selection events
		promotionComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			selectedPromotion = newValue;
			if (newValue != null) {
				promoCodeField.setText(newValue.getCode());
				discountLabel.setText(newValue.getDiscountPercent() + "%");
				updateTotalPrice();
			}
		});
	}

	/**
	 * Set button visibility based on user permissions
	 */
	private void setupButtonVisibility() {
		boolean canApplyPromotion = RoleChecker.hasPermission("APPLY_PROMOTION");

		promotionComboBox.setDisable(!canApplyPromotion);
		promoCodeField.setDisable(!canApplyPromotion);
		applyButton.setDisable(!canApplyPromotion);
		removePromoButton.setDisable(!canApplyPromotion);
	}

	/**
	 * Load service list
	 */
	private void loadServices() {
		try {
			List<Service> services = serviceService.getAllServices();
			serviceList = FXCollections.observableArrayList(services);
			serviceTable.setItems(serviceList);
		} catch (Exception e) {
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách dịch vụ", e.getMessage());
		}
	}

	/**
	 * Load active promotions
	 */
	private void loadPromotions() {
		try {
			List<Promotion> promotions = promotionService.getActivePromotions();
			promotionComboBox.setItems(FXCollections.observableArrayList(promotions));

			// Custom ComboBox display
			promotionComboBox.setCellFactory(p -> new javafx.scene.control.ListCell<Promotion>() {
				@Override
				protected void updateItem(Promotion item, boolean empty) {
					super.updateItem(item, empty);
					if (empty || item == null) {
						setText(null);
					} else {
						setText(item.getCode() + " (" + item.getDiscountPercent() + "%)");
					}
				}
			});

			// Custom ComboBox button display
			promotionComboBox.setButtonCell(new javafx.scene.control.ListCell<Promotion>() {
				@Override
				protected void updateItem(Promotion item, boolean empty) {
					super.updateItem(item, empty);
					if (empty || item == null) {
						setText("Chọn khuyến mãi");
					} else {
						setText(item.getCode() + " (" + item.getDiscountPercent() + "%)");
					}
				}
			});

		} catch (Exception e) {
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách khuyến mãi", e.getMessage());
		}
	}

	/**
	 * Update price calculations Combine both updateTotalPrice methods into one
	 */
	private void updateTotalPrice() {
		// Handle regular service selection case
		double totalPrice = 0;
		for (Service service : selectedServices) {
			totalPrice += service.getPrice();
		}
		totalPriceLabel.setText(String.format("%,.0f VND", totalPrice));

		// Handle invoice case
		if (currentInvoice != null && currentInvoice.getSubtotal() != null) {
			double subtotal = currentInvoice.getSubtotal().doubleValue();
			double discountAmount = 0;
			double pointValue = 0;

			// Calculate discount if promotion is selected
			if (selectedPromotion != null) {
				double discountPercent = selectedPromotion.getDiscountPercent();

				// Apply discount to subtotal for invoice mode
				if (currentInvoice != null) {
					discountAmount = subtotal * discountPercent / 100;
					discountAmountLabel.setText(String.format("%,.0f VND", discountAmount));
				}
				// Apply discount to total price for service selection mode
				else {
					double discountedPrice = totalPrice * (1 - discountPercent / 100);
					discountedPriceLabel.setText(String.format("%,.0f VND", discountedPrice));
					return; // Early return for this mode
				}
			} else {
				discountAmountLabel.setText("0 VND");
			}

			// Calculate points value if points are entered
			if (pointsField != null && pointsField.getText() != null && !pointsField.getText().isEmpty()) {
				try {
					int points = Integer.parseInt(pointsField.getText());
					pointValue = points * 1000.0;
					pointDiscountLabel.setText(String.format("%,.0f VND", pointValue));
				} catch (NumberFormatException e) {
					pointDiscountLabel.setText("0 VND");
				}
			} else {
				pointDiscountLabel.setText("0 VND");
			}

			// Calculate final total
			double finalTotal = subtotal - discountAmount - pointValue;
			if (finalTotal < 0)
				finalTotal = 0; // Ensure non-negative

			discountedPriceLabel.setText(String.format("%,.0f VND", finalTotal));
		}
		// Handle regular service selection when no invoice is present
		else if (selectedPromotion != null) {
			double discountPercent = selectedPromotion.getDiscountPercent();
			double discountedPrice = totalPrice * (1 - discountPercent / 100);
			discountedPriceLabel.setText(String.format("%,.0f VND", discountedPrice));
		} else {
			discountedPriceLabel.setText(String.format("%,.0f VND", totalPrice));
		}
	}

	/**
	 * Apply promotion
	 */
	@FXML
	private void applyPromotion(ActionEvent event) {
		if (selectedServices.isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn dịch vụ",
					"Vui lòng chọn ít nhất một dịch vụ để áp dụng khuyến mãi.");
			return;
		}

		if (selectedPromotion == null) {
			showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn khuyến mãi",
					"Vui lòng chọn một mã khuyến mãi để áp dụng.");
			return;
		}

		try {
			// Validate promotion code
			String promoCode = promoCodeField.getText();
			Promotion promotion = promotionService.getPromotionByCode(promoCode);

			if (promotion == null) {
				showAlert(Alert.AlertType.ERROR, "Lỗi", "Mã khuyến mãi không hợp lệ",
						"Mã khuyến mãi không tồn tại hoặc đã hết hạn.");
				return;
			}

			// Check expiration date
			if (promotion.getEndDate() != null && promotion.getEndDate().isBefore(LocalDate.now())) {
				showAlert(Alert.AlertType.ERROR, "Lỗi", "Mã khuyến mãi đã hết hạn",
						"Mã khuyến mãi này đã hết hạn vào ngày " + promotion.getEndDate());
				return;
			}

			// Apply promotion
			selectedPromotion = promotion;
			discountLabel.setText(promotion.getDiscountPercent() + "%");
			updateTotalPrice();

			showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã áp dụng khuyến mãi",
					"Mã khuyến mãi " + promotion.getCode() + " đã được áp dụng thành công.");

		} catch (Exception e) {
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể áp dụng khuyến mãi", e.getMessage());
		}
	}

	/**
	 * Remove selected promotion
	 */
	@FXML
	private void removePromotion(ActionEvent event) {
		selectedPromotion = null;
		promoCodeField.clear();
		discountLabel.setText("0%");
		updateTotalPrice();

		// Clear ComboBox selection
		promotionComboBox.getSelectionModel().clearSelection();

		showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Đã xóa khuyến mãi", "Đã xóa mã khuyến mãi đang áp dụng.");
	}

	/**
	 * Remove selected service
	 */
	@FXML
	private void removeSelectedService() {
		Service selectedService = serviceTable.getSelectionModel().getSelectedItem();
		if (selectedService != null) {
			selectedServices.remove(selectedService);
			updateTotalPrice();
		}
	}

	/**
	 * Clear all selected services
	 */
	@FXML
	private void clearAllServices() {
		selectedServices.clear();
		updateTotalPrice();
	}

	/**
	 * Show alert dialog
	 */
	private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}

	/**
	 * Go back to previous screen
	 */
	@FXML
	private void goBack(ActionEvent event) {
		SceneSwitcher.switchScene("staff/staff_home.fxml");
	}

	/**
	 * Initialize invoice data
	 */
	public void initInvoiceData(Invoice invoice) {
		this.currentInvoice = invoice;
		orderDetailRepository = OrderDetailRepository.getInstance();
		invoiceRepository = InvoiceRepository.getInstance();

		// Display invoice information
		invoiceIdLabel.setText("#" + invoice.getInvoiceId());

		// Display customer name if available
		if (invoice.getOrder() != null && invoice.getOrder().getCustomer() != null) {
			customerNameLabel.setText(invoice.getOrder().getCustomer().getFullName());

			// Display available customer points
			int availablePoints = invoice.getOrder().getCustomer().getPoint();
			availablePointsLabel.setText(String.valueOf(availablePoints));
		} else {
			customerNameLabel.setText("N/A");
			availablePointsLabel.setText("0");
		}

		// Display total amount
		if (invoice.getSubtotal() != null) {
			subtotalLabel.setText(String.format("%,.0f VND", invoice.getSubtotal().doubleValue()));
			totalPriceLabel.setText(String.format("%,.0f VND", invoice.getSubtotal().doubleValue()));
		}

		// Get services from invoice.getOrder()
		if (invoice.getOrder() != null) {
			try {
				// Get services from OrderDetailRepository
				List<Service> services = orderDetailRepository.getServicesByOrderId(invoice.getOrder().getOrderId());
				selectedServices.addAll(services);

				// Display applied promotion (if any)
				if (invoice.getPromotionCode() != null && !invoice.getPromotionCode().isEmpty()) {
					Promotion appliedPromotion = promotionService.getPromotionByCode(invoice.getPromotionCode());
					if (appliedPromotion != null) {
						selectedPromotion = appliedPromotion;
						promoCodeField.setText(appliedPromotion.getCode());
						discountLabel.setText(appliedPromotion.getDiscountPercent() + "%");
						promotionComboBox.setValue(appliedPromotion);

						// Display discount amount
						if (invoice.getDiscountAmount() != null) {
							discountAmountLabel
									.setText(String.format("%,.0f VND", invoice.getDiscountAmount().doubleValue()));
						}
					}
				} else {
					discountAmountLabel.setText("0 VND");
				}

				// Display used points (if any)
				if (invoice.getPointsUsed() != null && invoice.getPointsUsed() > 0) {
					pointsField.setText(String.valueOf(invoice.getPointsUsed()));
					double pointValue = invoice.getPointsUsed() * 1000.0;
					pointDiscountLabel.setText(String.format("%,.0f VND", pointValue));
				} else {
					pointDiscountLabel.setText("0 VND");
				}

				// Display total after discount
				if (invoice.getTotal() != null) {
					discountedPriceLabel.setText(String.format("%,.0f VND", invoice.getTotal().doubleValue()));
				}

				updateTotalPrice();
			} catch (Exception e) {
				showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải thông tin dịch vụ", e.getMessage());
			}
		}
	}

	/**
	 * Set callback for when promotion is applied
	 */
	public void setOnPromotionAppliedCallback(Runnable callback) {
		this.onPromotionAppliedCallback = callback;
	}

	/**
	 * Apply loyalty points
	 */
	@FXML
	private void applyPoints(ActionEvent event) {
		if (currentInvoice == null) {
			showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có hóa đơn được chọn",
					"Không thể áp dụng điểm khi không có hóa đơn.");
			return;
		}

		if (pointsField.getText().isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa nhập số điểm", "Vui lòng nhập số điểm muốn sử dụng.");
			return;
		}

		try {
			// Get points information
			int pointsToUse = Integer.parseInt(pointsField.getText());

			if (pointsToUse <= 0) {
				showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số điểm không hợp lệ",
						"Vui lòng nhập số điểm lớn hơn 0.");
				return;
			}

			// Check if customer has enough points
			int availablePoints = 0;
			if (currentInvoice.getOrder() != null && currentInvoice.getOrder().getCustomer() != null) {
				availablePoints = currentInvoice.getOrder().getCustomer().getPoint();
			}

			if (pointsToUse > availablePoints) {
				showAlert(Alert.AlertType.ERROR, "Lỗi", "Điểm không đủ",
						"Khách hàng chỉ có " + availablePoints + " điểm.");
				return;
			}

			// Update display
			updateTotalPrice();

		} catch (NumberFormatException e) {
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Số điểm không hợp lệ", "Vui lòng nhập số nguyên dương.");
		}
	}

	/**
	 * Clear entered points
	 */
	@FXML
	private void clearPoints() {
		pointsField.clear();
		pointDiscountLabel.setText("0 VND");
		updateTotalPrice();
	}

	/**
	 * Save changes to invoice
	 */
	@FXML
	private void saveChanges() {
		if (currentInvoice == null) {
			showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có hóa đơn được chọn",
					"Không thể lưu thay đổi khi không có hóa đơn.");
			return;
		}

		try {
			double subtotal = currentInvoice.getSubtotal().doubleValue();
			double discountAmount = 0;
			int pointsToUse = 0;
			String promotionCode = null;
			double discountPercent = 0;

			// Get promotion information
			if (selectedPromotion != null) {
				promotionCode = selectedPromotion.getCode();
				discountPercent = selectedPromotion.getDiscountPercent();
				discountAmount = subtotal * discountPercent / 100;
			}

			// Get points information
			if (!pointsField.getText().isEmpty()) {
				pointsToUse = Integer.parseInt(pointsField.getText());
				// Ensure non-negative
				if (pointsToUse < 0)
					pointsToUse = 0;
			}

			// Calculate points value
			double pointValue = pointsToUse * 1000.0;

			// Calculate final total
			double finalTotal = subtotal - discountAmount - pointValue;
			if (finalTotal < 0)
				finalTotal = 0; // Ensure non-negative

			// Update promotion information
			if (selectedPromotion != null) {
				invoiceRepository.updateInvoiceDiscount(currentInvoice.getInvoiceId(), promotionCode, discountPercent,
						discountAmount, finalTotal);
			}

			// Update points information
			if (pointsToUse > 0) {
				invoiceRepository.updateInvoicePoints(currentInvoice.getInvoiceId(), pointsToUse, finalTotal);
			}

			showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã lưu thay đổi",
					"Đã cập nhật thông tin khuyến mãi và điểm tích lũy cho hóa đơn #" + currentInvoice.getInvoiceId());

			// Call callback if available
			if (onPromotionAppliedCallback != null) {
				onPromotionAppliedCallback.run();
			}

			// Close window
			Stage stage = (Stage) saveButton.getScene().getWindow();
			stage.close();

		} catch (NumberFormatException e) {
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Dữ liệu không hợp lệ", e.getMessage());
		} catch (SQLException e) {
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lưu thay đổi", e.getMessage());
		}
	}

	/**
	 * Close dialog window
	 */
	@FXML
	private void closeDialog() {
		Stage stage = (Stage) saveButton.getScene().getWindow();
		stage.close();
	}
}