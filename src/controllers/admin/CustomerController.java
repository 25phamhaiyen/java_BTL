package controllers.admin;

import javafx.fxml.FXML;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import model.Customer;
import model.Account;
import enums.GenderEnum;
import repository.CustomerRepository;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomerController {

    @FXML private TextField txtLastName;
    @FXML private TextField txtFirstName;
    @FXML private ComboBox<String> cmbGender;
    @FXML private TextField txtPhone;
    @FXML private TextField txtCitizenNumber;
    @FXML private TextField txtAddress;
    @FXML private TextField txtEmail;
    @FXML private DatePicker dpRegistrationDate; 
    @FXML private TextField txtLoyaltyPoints; 

    
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> lastNameColumn;
    @FXML private TableColumn<Customer, String> firstNameColumn;
    @FXML private TableColumn<Customer, String> genderColumn;
    @FXML private TableColumn<Customer, String> phoneColumn;
    @FXML private TableColumn<Customer, String> emailColumn;
    @FXML private TableColumn<Customer, String> registrationDateColumn;
    @FXML private TableColumn<Customer, String> loyaltyPointsColumn;

    
    private CustomerRepository customerRepo = new CustomerRepository();

    public void initialize() {
        // Khởi tạo các ComboBox với dữ liệu
        cmbGender.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));

        // Load dữ liệu khách hàng ban đầu
        loadCustomerData();

        // Bind các cột với thuộc tính tương ứng trong Customer
        lastNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastName()));
        firstNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName()));
        genderColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGender().toString()));
        phoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhoneNumber()));
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        registrationDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRegistrationDate().toString())); // Nếu `registrationDate` là Date, cần định dạng lại
        loyaltyPointsColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getLoyaltyPoints())));
        
        // Sự kiện cho khi chọn một khách hàng từ bảng
        customerTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showCustomerDetails(newValue));
    }

    private void loadCustomerData() {
        // Giả sử có phương thức lấy danh sách khách hàng từ repository
        customerTable.getItems().setAll(customerRepo.selectAll());
    }

    private void showCustomerDetails(Customer customer) {
        if (customer != null) {
            txtLastName.setText(customer.getLastName());
            txtFirstName.setText(customer.getFirstName());
            cmbGender.setValue(customer.getGender().toString());
            txtPhone.setText(customer.getPhoneNumber());
            txtCitizenNumber.setText(customer.getCitizenNumber());
            txtAddress.setText(customer.getAddress());
            txtEmail.setText(customer.getEmail());
            registrationDateColumn.setCellValueFactory(cellData -> {
                Date registrationDate = cellData.getValue().getRegistrationDate();
                if (registrationDate != null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    return new SimpleStringProperty(dateFormat.format(registrationDate));
                } else {
                    return new SimpleStringProperty("");
                }
            });            txtLoyaltyPoints.setText(String.valueOf(customer.getLoyaltyPoints()));
        }
    }

    @FXML
    private void handleAddCustomer() {
        if (txtLastName.getText().isEmpty() || txtFirstName.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "First Name and Last Name are required.");
            return;
        }

        if (cmbGender.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select gender.");
            return;
        }

        // Lấy dữ liệu từ các trường nhập liệu
        String lastName = txtLastName.getText();
        String firstName = txtFirstName.getText();
        GenderEnum gender = GenderEnum.valueOf(cmbGender.getValue().toUpperCase());
        String phone = txtPhone.getText();
        String citizenNumber = txtCitizenNumber.getText();
        String address = txtAddress.getText();
        String email = txtEmail.getText();
        Date registrationDate = dpRegistrationDate.getValue() != null ? java.sql.Date.valueOf(dpRegistrationDate.getValue()) : null;
        int loyaltyPoints = txtLoyaltyPoints.getText().isEmpty() ? 0 : Integer.parseInt(txtLoyaltyPoints.getText());

        Account account = null;

        // Tạo đối tượng Customer mới
        Customer newCustomer = new Customer(
            0,
            lastName,
            firstName,
            gender,
            phone,
            citizenNumber,
            address,
            email,
            account,
            registrationDate,
            loyaltyPoints
        );

        int result = customerRepo.insert(newCustomer);
        if (result > 0) {
            loadCustomerData();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Customer added successfully.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add customer!");
        }
    }

    @FXML
    private void handleEditCustomer() {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            // Chỉnh sửa thông tin khách hàng
            selectedCustomer.setLastName(txtLastName.getText());
            selectedCustomer.setFirstName(txtFirstName.getText());
            selectedCustomer.setGender(GenderEnum.valueOf(cmbGender.getValue().toUpperCase()));
            selectedCustomer.setPhoneNumber(txtPhone.getText());
            selectedCustomer.setCitizenNumber(txtCitizenNumber.getText());
            selectedCustomer.setAddress(txtAddress.getText());
            selectedCustomer.setEmail(txtEmail.getText());
            selectedCustomer.setRegistrationDate(dpRegistrationDate.getValue() != null ? java.sql.Date.valueOf(dpRegistrationDate.getValue()) : null);
            selectedCustomer.setLoyaltyPoints(txtLoyaltyPoints.getText().isEmpty() ? 0 : Integer.parseInt(txtLoyaltyPoints.getText()));

            int result = customerRepo.update(selectedCustomer);
            if (result > 0) {
                loadCustomerData();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Customer details updated.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update customer.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "No Customer selected for editing.");
        }
    }
    
    @FXML
    private void handleDeleteCustomer() {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            int result = customerRepo.delete(selectedCustomer);
            if (result > 0) {
                loadCustomerData();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Customer deleted successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete customer.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "No Customer selected for deletion.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
