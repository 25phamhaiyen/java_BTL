package controllers.admin;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.Customer;
import model.Pet;
import model.PetType;
import service.CustomerService;
import service.PetService;

import java.time.LocalDate;

import enums.GenderEnum;

public class ManageCustomerController {

	@FXML
	private TextField searchTextField;
	@FXML
	private TableView<Customer> customerTable;
	@FXML
	private TableColumn<Customer, Integer> idColumn;
	@FXML
	private TableColumn<Customer, String> fullNameColumn;
	@FXML
	private TableColumn<Customer, String> genderColumn;
	@FXML
	private TableColumn<Customer, String> phoneColumn;
	@FXML
	private TableColumn<Customer, String> emailColumn;
	@FXML
	private TableColumn<Customer, String> petColumn;
	@FXML
	private TableColumn<Customer, Integer> loyaltyPointsColumn;
	@FXML
	private TableColumn<Customer, Integer> serviceHistoryColumn;

	@FXML
	private VBox formBox;
	@FXML
	private VBox mainContentBox;
	@FXML
	private TextField txtFullName, txtPhone, txtAddress, txtEmail, txtLoyaltyPoints;
	@FXML
	private ComboBox<String> cmbGender;
	@FXML
	private TextField txtPetName, txtPetWeight, txtPetNote;
	@FXML
	private ComboBox<GenderEnum> cmbPetGender;
	@FXML
	private ComboBox<PetType> cmbPetType;
	@FXML
	private DatePicker dpPetDob;
	@FXML
	private Button btnCancel; // Thêm nút Hủy

	private final CustomerService customerService = new CustomerService();
	private PetService petService = new PetService();
	private ObservableList<Customer> customerList;
	private Customer selectedCustomer = null;

	public void initialize() {
		// Khai báo TableColumn cho các thuộc tính
		idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
		fullNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFullName()));
		genderColumn
				.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGender().toString()));
		phoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhone()));
		emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
		loyaltyPointsColumn
				.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getPoint()).asObject());
		cmbPetGender.setItems(FXCollections.observableArrayList(GenderEnum.values()));
		cmbPetType.setItems(FXCollections.observableArrayList(petService.getAllPetTypes())); // Cần viết hàm
																								// getAllPetTypes()

		petColumn.setCellValueFactory(cellData -> {
			int customerId = cellData.getValue().getId();
			String petNames = petService.getPetNamesByCustomerId(customerId);
			return new SimpleStringProperty(petNames);
		});

		// Cài đặt cột mới cho "Số lượng đơn hàng"
		serviceHistoryColumn.setCellValueFactory(cellData -> {
			int customerId = cellData.getValue().getId();
			int orderCount = customerService.getOrderCountByCustomerId(customerId);
			return new SimpleIntegerProperty(orderCount).asObject();
		});

		// Cài đặt ComboBox cho Giới tính
		ObservableList<String> genderOptions = FXCollections.observableArrayList();
		for (GenderEnum g : GenderEnum.values()) {
			genderOptions.add(g.getDescription());
		}
		cmbGender.setItems(genderOptions);

		// Tìm kiếm khách hàng
		searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			handleSearchCustomer();
		});

		loadCustomerData();
	}

	@FXML
	public void loadCustomerData() {
		customerList = FXCollections.observableArrayList(customerService.getAllCustomers());
		customerTable.setItems(customerList);
	}

	@FXML
	public void handleSearchCustomer() {
		String keyword = searchTextField.getText().trim().toLowerCase();
		if (keyword.isEmpty()) {
			customerTable.setItems(customerList);
			return;
		}
		ObservableList<Customer> filteredList = FXCollections.observableArrayList();
		for (Customer customer : customerList) {
			boolean matchesPhone = customer.getPhone() != null && customer.getPhone().contains(keyword);
			boolean matchesName = customer.getFullName() != null
					&& customer.getFullName().toLowerCase().contains(keyword);

			if (matchesPhone || matchesName) {
				filteredList.add(customer);
			}
		}
		customerTable.setItems(filteredList);
	}

	@FXML
	private void handleAddCustomer() {
		formBox.setVisible(true);
		formBox.setManaged(true);
		mainContentBox.setVisible(false);
		mainContentBox.setManaged(false);
	}

	@FXML
	public void handleCancel() {
		// Ẩn form
		formBox.setVisible(false);
		formBox.setManaged(false);

		// Hiện lại bảng danh sách khách hàng
		mainContentBox.setVisible(true);
		mainContentBox.setManaged(true);

		// Xoá dữ liệu form
		clearForm();
	}


	@FXML
	public void handleEditCustomer() {
		selectedCustomer = customerTable.getSelectionModel().getSelectedItem();

		if (selectedCustomer == null) {
			showAlert("Vui lòng chọn khách hàng để sửa.");
			return;
		}

		fillForm(selectedCustomer);

		// Hiển thị form sửa
		formBox.setVisible(true);
		formBox.setManaged(true);

		// Ẩn phần danh sách chính
		mainContentBox.setVisible(false);
		mainContentBox.setManaged(false);

	}

	@FXML
	public void handleSaveCustomer() {
		try {
			String name = txtFullName.getText().trim();
			String genderStr = cmbGender.getValue();
			String phone = txtPhone.getText().trim();
			String email = txtEmail.getText().trim();
			String address = txtAddress.getText().trim();
			int points = Integer.parseInt(txtLoyaltyPoints.getText().trim());

			GenderEnum gender = null;
			for (GenderEnum g : GenderEnum.values()) {
				if (g.getDescription().equalsIgnoreCase(genderStr)) {
					gender = g;
					break;
				}
			}
			if (gender == null) {
				showAlert("Giới tính không hợp lệ");
				return;
			}

			if (selectedCustomer == null) {
				Customer customer = new Customer(0, name, gender, phone, email, address, points);
				customerService.addCustomer(customer);
				Customer insertedCustomer = customerService.findCustomerByPhoneNumber(phone);

				if (insertedCustomer != null) {
					String petName = txtPetName.getText().trim();
					PetType petType = cmbPetType.getValue();
					GenderEnum petGender = cmbPetGender.getValue();
					LocalDate dob = dpPetDob.getValue();
					double weight = Double.parseDouble(txtPetWeight.getText().trim());
					String note = txtPetNote.getText().trim();

					Pet pet = new Pet(0, petName, petType, petGender, dob, weight, note, insertedCustomer);
					int petResult = petService.addPet(pet);

					if (petResult <= 0) {
						showAlert("Thêm khách hàng thành công nhưng thêm thú cưng thất bại!");
					}
				} else {
					showAlert("Thêm khách hàng thành công nhưng không tìm thấy để gán thú cưng!");
				}

			} else {
				selectedCustomer.setFullName(name);
				selectedCustomer.setGender(gender);
				selectedCustomer.setPhone(phone);
				selectedCustomer.setEmail(email);
				selectedCustomer.setAddress(address);
				selectedCustomer.setPoint(points);
				customerService.updateCustomer(selectedCustomer);
			}

			selectedCustomer = null;
			clearForm();
			formBox.setVisible(false);
			formBox.setManaged(false);
			mainContentBox.setVisible(true);
			mainContentBox.setManaged(true);
			loadCustomerData();

		} catch (Exception e) {
			e.printStackTrace();
			showAlert("Lỗi khi lưu khách hàng: " + e.getMessage());
		}
	}

	private void clearForm() {
		txtFullName.clear();
		cmbGender.getSelectionModel().clearSelection();
		txtPhone.clear();
		txtAddress.clear();
		txtEmail.clear();
		txtLoyaltyPoints.clear();
		txtPetName.clear();
		cmbPetType.getSelectionModel().clearSelection();
		cmbPetGender.getSelectionModel().clearSelection();
		dpPetDob.setValue(null);
		txtPetWeight.clear();
		txtPetNote.clear();
	}

	private void fillForm(Customer c) {
		txtFullName.setText(c.getFullName() != null ? c.getFullName() : "");
		cmbGender.setValue(c.getGender() != null ? c.getGender().getDescription() : null);
		txtPhone.setText(c.getPhone() != null ? c.getPhone() : "");
		txtAddress.setText(c.getAddress() != null ? c.getAddress() : "");
		txtEmail.setText(c.getEmail() != null ? c.getEmail() : "");
		txtLoyaltyPoints.setText(String.valueOf(c.getPoint()));

		// Điền thú cưng nếu có
		Pet pet = petService.getPetByCustomerId(c.getId());
		if (pet != null) {
			txtPetName.setText(pet.getName());
			cmbPetType.setValue(pet.getTypePet());
			cmbPetGender.setValue(pet.getGender());
			dpPetDob.setValue(pet.getDob());
			txtPetWeight.setText(String.valueOf(pet.getWeight()));
			txtPetNote.setText(pet.getNote());
		} else {
			// Nếu không có thú cưng, clear các trường
			txtPetName.clear();
			cmbPetType.getSelectionModel().clearSelection();
			cmbPetGender.getSelectionModel().clearSelection();
			dpPetDob.setValue(null);
			txtPetWeight.clear();
			txtPetNote.clear();
		}
	}

	private void showAlert(String msg) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setContentText(msg);
		alert.show();
	}

	private boolean confirm(String msg) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
		alert.showAndWait();
		return alert.getResult() == ButtonType.YES;
	}
}
