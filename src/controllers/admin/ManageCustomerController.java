package controllers.admin;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Customer;
import model.Pet;
import model.PetType;
import repository.PetRepository;
import repository.PetTypeRepository;
import service.CustomerService;
import service.PetService;
import utils.CustomerValidator;
import utils.LanguageChangeListener;
import utils.LanguageManagerAd;
import utils.PetValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import enums.GenderEnum;

public class ManageCustomerController implements LanguageChangeListener{

	@FXML private Label lblTitle, lblFormCus, lblName, lblGender, lblPhone, lblAddress, lblEmail, lblPoints, lblFormPet;
	@FXML private TextField searchTextField;
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
	private HBox formBox;
	@FXML
	private VBox mainContentBox;
	@FXML
	private TextField txtFullName, txtPhone, txtAddress, txtEmail, txtLoyaltyPoints;
	@FXML
	private ComboBox<String> cmbGender;

	@FXML
	private FlowPane petCardPane;
	private Pet selectedPet;
	private ObservableList<Pet> petList = FXCollections.observableArrayList();

	@FXML private Button btnCancel, btnSearch, btnAdd, btnEdit, saveButton, btnAddPet, btnEditPet, btnDelPet ; 

	private final CustomerService customerService = new CustomerService();
	private PetService petService = new PetService();
	private ObservableList<Customer> customerList;
	private List<Pet> deletedPets = new ArrayList<>(); // danh sách thú cưng bị xóa
	private Customer selectedCustomer = null;

	public void initialize() {
		LanguageManagerAd.addListener(this);
		loadTexts();
		
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

		loadCustomerData();
		
		// Đảm bảo form ẩn ban đầu
	    formBox.setVisible(false);
	    formBox.setManaged(false);
	    
	 // Hiển thị bảng chính
	    mainContentBox.setVisible(true);
	    mainContentBox.setManaged(true);

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
			dialog.setTitle(LanguageManagerAd.getString("manageCustomer.showPetForm.title"));
	
			// Tạo các thành phần trong dialog
			VBox dialogVbox = new VBox(10);
			TextField petNameField = new TextField();
			petNameField.setPromptText(LanguageManagerAd.getString("manageCustomer.showPetForm.prompt.name"));
			ComboBox<GenderEnum> petGenderCombo = new ComboBox<>();
			petGenderCombo.getItems().setAll(GenderEnum.values());
			petGenderCombo.setPromptText(LanguageManagerAd.getString("manageCustomer.showPetForm.prompt.gender"));
			PetTypeRepository petTypeRepo = new PetTypeRepository();
			ComboBox<PetType> petTypeCombo = new ComboBox<>();
			List<PetType> petTypes = petTypeRepo.selectAll();
			petTypeCombo.getItems().addAll(petTypes);
			DatePicker petDobPicker = new DatePicker();
			TextField petWeightField = new TextField();
			petWeightField.setPromptText(LanguageManagerAd.getString("manageCustomer.showPetForm.prompt.weight"));
	
			// Thêm các trường vào trong dialog
			dialogVbox.getChildren().addAll(new Label(LanguageManagerAd.getString("manageCustomer.showPetForm.label.name")), petNameField, new Label(LanguageManagerAd.getString("manageCustomer.showPetForm.label.gender")),
					petGenderCombo, new Label(LanguageManagerAd.getString("manageCustomer.showPetForm.label.type")), petTypeCombo, new Label(LanguageManagerAd.getString("manageCustomer.showPetForm.label.dob")), petDobPicker,
					new Label(LanguageManagerAd.getString("manageCustomer.showPetForm.label.weight")), petWeightField);
	
			// Thiết lập button "Lưu" cho dialog
			ButtonType saveButtonType = new ButtonType(LanguageManagerAd.getString("manageCustomer.showPetForm.button.save"), ButtonBar.ButtonData.OK_DONE);
			dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
	
			// Thêm các thành phần vào Dialog
			dialog.getDialogPane().setContent(dialogVbox);
	
			// Khi người dùng nhấn "Lưu", tạo đối tượng Pet và thêm vào petList
			dialog.setResultConverter(button -> {
				if (button.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
					String name = petNameField.getText();
					GenderEnum gender = petGenderCombo.getValue();
					PetType type = petTypeCombo.getValue();
					LocalDate dob = petDobPicker.getValue();
					String weightText = petWeightField.getText();
	
					if (PetValidator.isValid(name, gender, type, dob, weightText)) {
						double weight = PetValidator.parseWeight(weightText);
						return new Pet(name, gender, type, dob, weight);
					}
				}
				return null;
			});
	
			// Hiển thị dialog và xử lý kết quả
			Optional<Pet> result = dialog.showAndWait();
	
			// Nếu có kết quả (người dùng nhấn "Lưu"), thêm pet vào danh sách và cập nhật bảng
			result.ifPresent(pet -> {
				petList.add(pet);
				updatePetFlowPane(); 
			});
		}

	private void updatePetFlowPane() {
		setUpPetFlowPane(petList);
	}

	public void setUpPetFlowPane(ObservableList<Pet> pets) {
	    petCardPane.getChildren().clear();
	    for (Pet pet : pets) {
	        VBox card = createPetCard(pet);

	        // Bắt sự kiện click để chọn pet
	        card.setOnMouseClicked(event -> {
	            selectedPet = pet;
	            highlightSelectedCard(card);
	        });

	        petCardPane.getChildren().add(card);
	    }
	}

	private VBox createPetCard(Pet pet) {
	    VBox vbox = new VBox();
	    vbox.setPadding(new Insets(10));
	    vbox.setStyle("-fx-border-color: gray; -fx-border-radius: 5;");

	    Label nameLabel = new Label(pet.getName());
	    Label speciesLabel = new Label(LanguageManagerAd.getString("manageCustomer.pet.species") + pet.getTypePet().getSpecies());
        Label breedLabel = new Label(LanguageManagerAd.getString("manageCustomer.pet.breed") + pet.getTypePet().getBreed());
	    vbox.getChildren().addAll(nameLabel, speciesLabel, breedLabel);

	    return vbox;
	}

	private void highlightSelectedCard(VBox selectedCard) {
	    // Xóa style highlight các card khác
	    for (Node node : petCardPane.getChildren()) {
	        node.setStyle("-fx-border-color: gray; -fx-border-radius: 5;");
	    }
	    // Đánh dấu card được chọn
	    selectedCard.setStyle("-fx-border-color: blue; -fx-border-radius: 5; -fx-background-color: lightblue;");
	}


	@FXML
	private void handleEditPet() {
		if (selectedPet == null) {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle(LanguageManagerAd.getString("manageStaff.delete.alert.noSelection.title"));
			alert.setHeaderText(null);
			alert.setContentText(LanguageManagerAd.getString("manageCustomer.alert.noPetSelectedEdit"));
			alert.showAndWait();
			return;
		}

		Dialog<Pet> dialog = new Dialog<>();
		dialog.setTitle(LanguageManagerAd.getString("manageCustomer.dialog.editPet.title"));

		VBox dialogVbox = new VBox(10);

		TextField petNameField = new TextField(selectedPet.getName());
		ComboBox<GenderEnum> petGenderCombo = new ComboBox<>();
		ComboBox<PetType> petTypeCombo = new ComboBox<>();
		DatePicker petDobPicker = new DatePicker(selectedPet.getDob());
		TextField petWeightField = new TextField(String.valueOf(selectedPet.getWeight()));

		petGenderCombo.getItems().setAll(GenderEnum.values());
		petGenderCombo.setValue(selectedPet.getGender());

		PetTypeRepository petTypeRepo = new PetTypeRepository();
		List<PetType> petTypes = petTypeRepo.selectAll();
		petTypeCombo.getItems().addAll(petTypes);
		petTypeCombo.setValue(selectedPet.getTypePet());

		dialogVbox.getChildren().addAll(new Label(LanguageManagerAd.getString("manageCustomer.dialog.editPet.label.name")), petNameField, new Label(LanguageManagerAd.getString("manageCustomer.dialog.editPet.label.gender")),
				petGenderCombo, new Label(LanguageManagerAd.getString("manageCustomer.dialog.editPet.label.type")), petTypeCombo, new Label(LanguageManagerAd.getString("manageCustomer.dialog.editPet.label.dob")), petDobPicker,
				new Label(LanguageManagerAd.getString("manageCustomer.dialog.editPet.label.weight")), petWeightField);

		dialog.getDialogPane().getButtonTypes().addAll(new ButtonType(LanguageManagerAd.getString("manageCustomer.showPetForm.button.save"), ButtonBar.ButtonData.OK_DONE),
				ButtonType.CANCEL);
		dialog.getDialogPane().setContent(dialogVbox);

		dialog.setResultConverter(button -> {
			if (button.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
				String name = petNameField.getText();
				GenderEnum gender = petGenderCombo.getValue();
				PetType type = petTypeCombo.getValue();
				LocalDate dob = petDobPicker.getValue();
				String weightText = petWeightField.getText();

				if (PetValidator.isValid(name, gender, type, dob, weightText)) {
					selectedPet.setName(name);
					selectedPet.setGender(gender);
					selectedPet.setTypePet(type);
					selectedPet.setDob(dob);
					selectedPet.setWeight(PetValidator.parseWeight(weightText));
					return selectedPet;
				}
			}
			return null;
		});
		Optional<Pet> result = dialog.showAndWait();

		result.ifPresent(editedPet -> {
			PetRepository petRepo = new PetRepository();
			petRepo.update(editedPet);

			updatePetFlowPane();
		});
		selectedPet = null;

	}

	@FXML
	private void handleDeletePet() {
		if (selectedPet == null) {
			showAlert(LanguageManagerAd.getString("manageCustomer.alert.noPetSelectedDelete"));
			return;
		}

		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, LanguageManagerAd.getString("manageCustomer.alert.confirmDeletePetMessage"), ButtonType.YES,
				ButtonType.NO);
		alert.setHeaderText(null);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.YES) {
			if (selectedPet.getPetId() != 0) {
				deletedPets.add(selectedPet);
			}
			petList.remove(selectedPet);
		}
		updatePetFlowPane();
		selectedPet = null;
	}
	

	@FXML
	private void handleAddCustomer() {
		// Hiển thị form nhập liệu khách hàng
		formBox.setVisible(true);
		formBox.setManaged(true);

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
			showAlert(LanguageManagerAd.getString("manageCustomer.alert.noCustomerSelectedEdit"));
			return;
		}

		fillForm(selectedCustomer);

		petList = FXCollections.observableArrayList(petService.findPetsByCustomerId(selectedCustomer.getId()));
		setUpPetFlowPane(petList);

		// Hiển thị form sửa
		formBox.setVisible(true);
		formBox.setManaged(true);

		
		// Hiển thị bảng thú cưng 
		petCardPane.setVisible(true);
		petCardPane.setManaged(true);

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
				showAlert(LanguageManagerAd.getString("manageCustomer.alert.invalidGender"));
				return;
			}
			if (!CustomerValidator.isValid(name, phone, email, address, txtLoyaltyPoints.getText().trim())) {
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
					showAlert(LanguageManagerAd.getString("manageCustomer.alert.cannotAssignPet"));
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
					if (petService.hasBooking(deleted.getPetId())) {
						Alert alert = new Alert(Alert.AlertType.WARNING);
						alert.setTitle(LanguageManagerAd.getString("manageCustomer.alert.deletePetFailedTitle"));
						alert.setHeaderText(LanguageManagerAd.getString("manageCustomer.alert.deletePetFailedHeader"));
						alert.setContentText(LanguageManagerAd.getString("manageCustomer.alert.deletePetFailedContent", deleted.getName()));
						alert.showAndWait();
						continue; // bỏ qua xóa thú cưng này
					}
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
			showAlert(LanguageManagerAd.getString("manageCustomer.alert.saveCustomerError", e.getMessage()));
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


	@Override
	public void onLanguageChanged() {
		loadTexts();
		
	}
	
	private void loadTexts() {
		lblTitle.setText(LanguageManagerAd.getString("manageCustomer.title"));
        searchTextField.setPromptText(LanguageManagerAd.getString("manageCustomer.search.prompt"));
        btnSearch.setText(LanguageManagerAd.getString("manageCustomer.button.search"));
        idColumn.setText(LanguageManagerAd.getString("manageCustomer.table.column.id"));
        fullNameColumn.setText(LanguageManagerAd.getString("manageCustomer.table.column.fullName"));
        genderColumn.setText(LanguageManagerAd.getString("manageCustomer.table.column.gender"));
        phoneColumn.setText(LanguageManagerAd.getString("manageCustomer.table.column.phone"));
        emailColumn.setText(LanguageManagerAd.getString("manageCustomer.table.column.email"));
        btnEdit.setText(LanguageManagerAd.getString("manageAccount.button.edit"));
        petColumn.setText(LanguageManagerAd.getString("manageCustomer.table.column.pet"));
        loyaltyPointsColumn.setText(LanguageManagerAd.getString("manageCustomer.table.column.loyaltyPoints"));
        serviceHistoryColumn.setText(LanguageManagerAd.getString("manageCustomer.table.column.serviceHistory"));
        btnAdd.setText(LanguageManagerAd.getString("manageCustomer.button.addCustomer"));
        btnEdit.setText(LanguageManagerAd.getString("manageCustomer.button.editCustomer"));
        lblFormCus.setText(LanguageManagerAd.getString("manageCustomer.form.title.customer"));
        lblName.setText(LanguageManagerAd.getString("manageCustomer.form.label.fullName"));
        txtFullName.setPromptText(LanguageManagerAd.getString("manageCustomer.form.prompt.fullName"));
        lblGender.setText(LanguageManagerAd.getString("manageCustomer.form.label.gender"));
        cmbGender.setPromptText(LanguageManagerAd.getString("manageCustomer.form.prompt.gender"));
        lblPhone.setText(LanguageManagerAd.getString("manageCustomer.form.label.phone"));
        txtPhone.setPromptText(LanguageManagerAd.getString("manageCustomer.form.prompt.phone"));
        lblAddress.setText(LanguageManagerAd.getString("manageCustomer.form.label.address"));
        txtAddress.setPromptText(LanguageManagerAd.getString("manageCustomer.form.prompt.address"));
        lblEmail.setText(LanguageManagerAd.getString("manageCustomer.form.label.email"));
        txtEmail.setPromptText(LanguageManagerAd.getString("manageCustomer.form.prompt.email"));
        lblPoints.setText(LanguageManagerAd.getString("manageCustomer.form.label.loyaltyPoints"));
        txtLoyaltyPoints.setPromptText(LanguageManagerAd.getString("manageCustomer.form.prompt.loyaltyPoints"));
        saveButton.setText(LanguageManagerAd.getString("manageCustomer.button.save"));
        btnCancel.setText(LanguageManagerAd.getString("manageCustomer.button.cancel"));
        lblFormPet.setText(LanguageManagerAd.getString("manageCustomer.form.title.pet"));
        btnAddPet.setText(LanguageManagerAd.getString("manageCustomer.button.addPet"));
        btnEditPet.setText(LanguageManagerAd.getString("manageCustomer.button.editPet"));
        btnDelPet.setText(LanguageManagerAd.getString("manageCustomer.button.deletePet"));
	}
}