package controllers.admin;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import model.Customer;
import model.Pet;
import model.PetType;
import repository.PetTypeRepository;
import service.CustomerService;
import service.PetService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
//	@FXML
//	private TextField txtPetName, txtPetWeight, txtPetNote;
//	@FXML
//	private ComboBox<GenderEnum> cmbPetGender;
//	@FXML
//	private ComboBox<PetType> cmbPetType;
//	@FXML
//	private DatePicker dpPetDob;
	
	@FXML
	private TableView<Pet> petTable;
	@FXML
	private TableColumn<Pet, Integer> petIdColumn;
	@FXML
	private TableColumn<Pet, String> petNameColumn;
	@FXML
	private TableColumn<Pet, String> petGenderColumn;
	@FXML
	private TableColumn<Pet, String> petTypeColumn;
	@FXML
	private TableColumn<Pet, LocalDate> petDobColumn;
	@FXML
	private TableColumn<Pet, Double> petWeightColumn;

	private ObservableList<Pet> petList = FXCollections.observableArrayList();

	@FXML
	private Button btnCancel; // Thêm nút Hủy

	private final CustomerService customerService = new CustomerService();
	private PetService petService = new PetService();
	private ObservableList<Customer> customerList;
	private List<Pet> deletedPets = new ArrayList<>(); // danh sách thú cưng bị xóa
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
		setupPetTable();
		loadCustomerData();
		
	}
	
	private void setupPetTable() {
	    petIdColumn.setCellValueFactory(new PropertyValueFactory<>("petId"));
	    petNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
	    petGenderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
	    petTypeColumn.setCellValueFactory(new PropertyValueFactory<>("typePet"));
	    petDobColumn.setCellValueFactory(new PropertyValueFactory<>("dob"));
	    petWeightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));

	    petTable.setItems(petList);
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
	private void handleShowPetForm() {
	    // Tạo dialog nhập thông tin thú cưng
	    Dialog<Pet> dialog = new Dialog<>();
	    dialog.setTitle("Thêm thú cưng");

	    // Tạo các thành phần trong dialog
	    VBox dialogVbox = new VBox(10);
	    TextField petNameField = new TextField();
	    petNameField.setPromptText("Tên thú cưng");
	    ComboBox<GenderEnum> petGenderCombo = new ComboBox<>();
	    petGenderCombo.getItems().setAll(GenderEnum.values());
	    petGenderCombo.setPromptText("Giới tính");
	    PetTypeRepository petTypeRepo = new PetTypeRepository();
	    ComboBox<PetType> petTypeCombo = new ComboBox<>();
	    List<PetType> petTypes = petTypeRepo.selectAll();
	    petTypeCombo.getItems().addAll(petTypes);
	    DatePicker petDobPicker = new DatePicker();
	    TextField petWeightField = new TextField();
	    petWeightField.setPromptText("Cân nặng");

	    // Thêm các trường vào trong dialog
	    dialogVbox.getChildren().addAll(
	            new Label("Tên thú cưng:"), petNameField,
	            new Label("Giới tính:"), petGenderCombo,
	            new Label("Giống loài:"), petTypeCombo,
	            new Label("Ngày sinh:"), petDobPicker,
	            new Label("Cân nặng (kg):"), petWeightField
	        );

	    // Thiết lập button "Lưu" cho dialog
	    ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
	    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

	    // Thêm các thành phần vào Dialog
	    dialog.getDialogPane().setContent(dialogVbox);

	    // Khi người dùng nhấn "Lưu", tạo đối tượng Pet và thêm vào petList
	    dialog.setResultConverter(button -> {
	        if (button.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
	            try {
	                return new Pet(
	                    petNameField.getText(),
	                    petGenderCombo.getValue(),
	                    petTypeCombo.getValue(),
	                    petDobPicker.getValue(),
	                    Double.parseDouble(petWeightField.getText())
	                );
	            } catch (Exception ex) {
	                ex.printStackTrace(); // Hoặc hiện alert lỗi
	                return null;
	            }
	        }
	        return null;
	    });

	    // Hiển thị dialog và xử lý kết quả
	    Optional<Pet> result = dialog.showAndWait();

	    // Nếu có kết quả (người dùng nhấn "Lưu"), thêm pet vào danh sách và cập nhật bảng
	    result.ifPresent(pet -> {
	        petList.add(pet);
	        updatePetTable(); // Cập nhật bảng hiển thị
	    });
	}

	private void updatePetTable() {
	    petTable.setItems(FXCollections.observableArrayList(petList));
	}

	@FXML
	private void handleEditPet() {
	    Pet selectedPet = petTable.getSelectionModel().getSelectedItem();
	    if (selectedPet == null) {
	        Alert alert = new Alert(Alert.AlertType.WARNING);
	        alert.setTitle("Chưa chọn thú cưng");
	        alert.setHeaderText(null);
	        alert.setContentText("Vui lòng chọn một thú cưng để sửa.");
	        alert.showAndWait();
	        return;
	    }

	    // Tạo dialog và truyền thú cưng cần sửa
	    Dialog<Pet> dialog = new Dialog<>();
	    dialog.setTitle("Sửa thông tin thú cưng");

	    VBox dialogVbox = new VBox(10);

	    TextField petNameField = new TextField(selectedPet.getName());
	    ComboBox<GenderEnum> petGenderCombo = new ComboBox<>();
	    ComboBox<PetType> petTypeCombo = new ComboBox<>();
	    DatePicker petDobPicker = new DatePicker(selectedPet.getDob());
	    TextField petWeightField = new TextField(String.valueOf(selectedPet.getWeight()));

	    petGenderCombo.getItems().setAll(GenderEnum.values());
	    petGenderCombo.setValue(selectedPet.getGender());

	    // Load danh sách giống loài từ database
	    PetTypeRepository petTypeRepo = new PetTypeRepository();
	    List<PetType> petTypes = petTypeRepo.selectAll();
	    petTypeCombo.getItems().addAll(petTypes);
	    petTypeCombo.setValue(selectedPet.getTypePet());

	    dialogVbox.getChildren().addAll(
	        new Label("Tên thú cưng:"), petNameField,
	        new Label("Giới tính:"), petGenderCombo,
	        new Label("Giống loài:"), petTypeCombo,
	        new Label("Ngày sinh:"), petDobPicker,
	        new Label("Cân nặng (kg):"), petWeightField
	    );

	    dialog.getDialogPane().getButtonTypes().addAll(
	        new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE),
	        ButtonType.CANCEL
	    );
	    dialog.getDialogPane().setContent(dialogVbox);

	    dialog.setResultConverter(button -> {
	        if (button.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
	            try {
	                selectedPet.setName(petNameField.getText());
	                selectedPet.setGender(petGenderCombo.getValue());
	                PetType petType = selectedPet.getTypePet();
	                selectedPet.setTypePet(petType);
	                selectedPet.setDob(petDobPicker.getValue());
	                selectedPet.setWeight(Double.parseDouble(petWeightField.getText()));
	                return selectedPet;
	            } catch (Exception ex) {
	                ex.printStackTrace(); // hoặc hiện alert
	                return null;
	            }
	        }
	        return null;
	    });

	    Optional<Pet> result = dialog.showAndWait();
	    result.ifPresent(editedPet -> {
	        petTable.refresh();
	    });
	}


	@FXML
	private void handleDeletePet() {
	    Pet selectedPet = petTable.getSelectionModel().getSelectedItem();
	    if (selectedPet == null) {
	        showAlert("Vui lòng chọn thú cưng để xóa.");
	        return;
	    }

	    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn xóa thú cưng này?", ButtonType.YES, ButtonType.NO);
	    alert.setHeaderText(null);
	    Optional<ButtonType> result = alert.showAndWait();
	    if (result.isPresent() && result.get() == ButtonType.YES) {
	        if (selectedPet.getPetId() != 0) {
	            deletedPets.add(selectedPet);
	        }
	        petList.remove(selectedPet);
	    }
	}




	@FXML
	private void handleAddCustomer() {
	    // Hiển thị form nhập liệu khách hàng
	    formBox.setVisible(true);
	    formBox.setManaged(true);

	    // Hiển thị bảng thú cưng (petTable)
	    petTable.setVisible(true);
	    petTable.setManaged(true);

	    // Ẩn phần bảng dữ liệu chính và các nút chức năng
	    mainContentBox.setVisible(false);
	    mainContentBox.setManaged(false);
	}


	@FXML
	public void handleCancel() {
		// Ẩn form
		formBox.setVisible(false);
		formBox.setManaged(false);
		
		petList.clear(); 
		
		// Ẩn bảng thú cưng (petTable)
	    petTable.setVisible(false);
	    petTable.setManaged(false);
	    

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
		
		petList = FXCollections.observableArrayList(
			    petService.findPetsByCustomerId(selectedCustomer.getId())
			);
			petTable.setItems(petList);

		// Hiển thị form sửa
		formBox.setVisible(true);
		formBox.setManaged(true);
		
		// Hiển thị bảng thú cưng (petTable)
	    petTable.setVisible(true);
	    petTable.setManaged(true);

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
					// Lưu thú cưng vào database
				    for (Pet pet : petList) {
				        pet.setOwner(insertedCustomer);
				        petService.addPet(pet);
				    }
				    petList.clear(); 

				} else {
					showAlert("Thêm khách hàng thành công nhưng không tìm thấy để gán thú cưng!");
				}

			} else {
			    // Cập nhật thông tin khách hàng
			    selectedCustomer.setFullName(name);
			    selectedCustomer.setGender(gender);
			    selectedCustomer.setPhone(phone);
			    selectedCustomer.setEmail(email);
			    selectedCustomer.setAddress(address);
			    selectedCustomer.setPoint(points);
			    customerService.updateCustomer(selectedCustomer);

			    // Lưu thú cưng mới hoặc đã sửa
			    for (Pet pet : petList) {
			        pet.setOwner(selectedCustomer);
			        if (pet.getPetId() == 0) {
			            petService.addPet(pet); // thêm mới
			        } else {
			            petService.updatePet(pet);
			        }
			    }

			    for (Pet deleted : deletedPets) {
			        petService.deletePet(deleted.getPetId());
			    }
			    deletedPets.clear();

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
	}

	private void fillForm(Customer c) {
		txtFullName.setText(c.getFullName() != null ? c.getFullName() : "");
		cmbGender.setValue(c.getGender() != null ? c.getGender().getDescription() : null);
		txtPhone.setText(c.getPhone() != null ? c.getPhone() : "");
		txtAddress.setText(c.getAddress() != null ? c.getAddress() : "");
		txtEmail.setText(c.getEmail() != null ? c.getEmail() : "");
		txtLoyaltyPoints.setText(String.valueOf(c.getPoint()));

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