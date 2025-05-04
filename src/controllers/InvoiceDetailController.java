
package controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.Invoice;
import model.OrderDetail;
import service.InvoiceService;

public class InvoiceDetailController implements Initializable {
    
    @FXML
    private Label invoiceIdLabel;
    
    @FXML
    private Label orderIdLabel;
    
    @FXML
    private Label customerNameLabel;
    
    @FXML
    private Label paymentDateLabel;
    
    @FXML
    private Label totalLabel;
    
    @FXML
    private Label paymentMethodLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private TableView<OrderDetail> orderDetailsTable;
    
    @FXML
    private TableColumn<OrderDetail, String> serviceNameColumn;
    
    @FXML
    private TableColumn<OrderDetail, Integer> quantityColumn;
    
    @FXML
    private TableColumn<OrderDetail, Double> priceColumn;
    
    @FXML
    private TableColumn<OrderDetail, Double> subtotalColumn;
    
    private InvoiceService invoiceService;
    private int invoiceId;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        invoiceService = new InvoiceService(); // TODO: Cập nhật constructor
    }
    
    /**
     * Khởi tạo dữ liệu cho controller
     * @param invoiceId ID của hóa đơn cần hiển thị
     */
    public void initData(int invoiceId) {
        this.invoiceId = invoiceId;
        loadInvoiceDetails();
    }
    
    /**
     * Tải thông tin chi tiết của hóa đơn
     */
    private void loadInvoiceDetails() {
        try {
            Invoice invoice = invoiceService.getInvoiceById(invoiceId);
            
            if (invoice != null) {
                invoiceIdLabel.setText(String.valueOf(invoice.getInvoiceId()));
                orderIdLabel.setText(String.valueOf(invoice.getOrder().getOrderId()));
                customerNameLabel.setText(invoice.getOrder().getCustomer().getFullName());
                paymentDateLabel.setText(invoice.getPaymentDate().toString());
                totalLabel.setText(String.format("%,.0f VND", invoice.getTotal()));
                paymentMethodLabel.setText(invoice.getPaymentMethod().name());
                statusLabel.setText(invoice.getStatus().name());
                
                // Tải chi tiết đơn hàng
                // List<OrderDetail> orderDetails = orderDetailRepository.getOrderDetailsByOrderId(invoice.getOrder().getOrderId());
                // loadOrderDetails(orderDetails);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Tải chi tiết đơn hàng vào bảng
     */
    private void loadOrderDetails(java.util.List<OrderDetail> orderDetails) {
        // Cấu hình các cột
        serviceNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getService().getName()));
        
        quantityColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(
                cellData.getValue().getQuantity()).asObject());
        
        priceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(
                cellData.getValue().getPrice().doubleValue()).asObject());
        
        subtotalColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(
                cellData.getValue().getPrice().doubleValue() * cellData.getValue().getQuantity()).asObject());
        
        // Format số tiền
        priceColumn.setCellFactory(column -> {
            return new javafx.scene.control.TableCell<OrderDetail, Double>() {
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
        
        subtotalColumn.setCellFactory(column -> {
            return new javafx.scene.control.TableCell<OrderDetail, Double>() {
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
        
        // Thêm dữ liệu vào bảng
        orderDetailsTable.getItems().setAll(orderDetails);
    }
}
