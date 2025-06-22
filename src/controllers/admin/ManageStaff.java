package controllers.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Alert.AlertType;
import model.Account;
import model.Role;
import model.Staff;
import service.AccountService;
import service.RoleService;
import service.StaffService;
import utils.LanguageChangeListener;
import utils.LanguageManagerAd;

import java.sql.Date;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import enums.GenderEnum;

public class ManageStaff implements LanguageChangeListener{

	@FXML
	private TableView<Staff> tblStaff;

	@FXML
	private TableColumn<Staff, Integer> colStaffId;

	@FXML
	private TableColumn<Staff, String> colStaffName;

	@FXML
	private TableColumn<Staff, String> colStaffRole;

	@FXML
	private TableColumn<Staff, String> colStaffPhone;

	@FXML
	private TableColumn<Staff, String> colStaffEmail;

	@FXML
	private TableColumn<Staff, String> colStartDate;

	@FXML
	private TableColumn<Staff, String> colSalary;

	@FXML
	private TableColumn<Staff, String> colAccount;

	@FXML private Label lblTitle;
    @FXML private Label lblSearch;
	@FXML private TextField txtSearch;
	@FXML private Button btnAdd, btnEdit, btnDelete, btnSearch;
	@FXML
	private Button terminateButton;

	private ObservableList<Staff> staffList = FXCollections.observableArrayList();
	private final StaffService staffService = new StaffService();
	private final AccountService accountService = new AccountService();
	private final RoleService roleService = new RoleService();

	@FXML
	public void initialize() {
		LanguageManagerAd.addListener(this);
		loadTexts();
		
		// Ánh xạ các cột với thuộc tính của lớp Staff
		colStaffId.setCellValueFactory(new PropertyValueFactory<>("id"));
		colStaffName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
		colStaffRole.setCellValueFactory(new PropertyValueFactory<>("roleName"));
		colStaffPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
		colStaffEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
		colStartDate.setCellValueFactory(new PropertyValueFactory<>("hire_date"));
		colSalary.setCellValueFactory(cellData -> {
			double salary = cellData.getValue().getSalary(); // Get the salary
			String formattedSalary = String.format("%,.2f", salary); // Format it
			return new SimpleStringProperty(formattedSalary); // Return as String
		});

		colAccount.setCellValueFactory(new PropertyValueFactory<>("accountName"));

		// Tải dữ liệu
		loadData();
	}

	private void loadData() {
		// Lấy dữ liệu từ cơ sở dữ liệu
		List<Staff> staffFromDB = staffService.getAllStaffs();
		staffList.setAll(staffFromDB);

		tblStaff.setItems(staffList);
	}

	@FXML
	private void handleSearchStaff() {
		String keyword = txtSearch.getText().trim().toLowerCase();
		if (!keyword.isEmpty()) {
			ObservableList<Staff> filteredList = FXCollections.observableArrayList();
			for (Staff staff : staffList) {
				if (staff.getFullName().toLowerCase().contains(keyword)
						|| staff.getRole().getRoleName().toLowerCase().contains(keyword)
						|| staff.getAccount().getUserName().toLowerCase().contains(keyword)) {
					filteredList.add(staff);
				}
			}
			tblStaff.setItems(filteredList);
		} else {
			tblStaff.setItems(staffList);
		}
	}

	@FXML
	private void handleAddStaff() {
		// Tạo dialog để nhập thông tin nhân viên
		Dialog<Staff> dialog = new Dialog<>();
		dialog.setTitle(LanguageManagerAd.getString("manageStaff.add.dialog.title"));
		dialog.setHeaderText(LanguageManagerAd.getString("manageStaff.add.dialog.header"));

		// Tạo các trường nhập liệu
		TextField txtFullName = new TextField();
		txtFullName.setPromptText(LanguageManagerAd.getString("manageStaff.add.fullName"));

		ToggleGroup genderGroup = new ToggleGroup();
		RadioButton rbMale = new RadioButton(LanguageManagerAd.getString("manageStaff.add.gender.male"));
		RadioButton rbFemale = new RadioButton(LanguageManagerAd.getString("manageStaff.add.gender.female"));
		RadioButton rbOther = new RadioButton(LanguageManagerAd.getString("manageStaff.add.gender.other"));
		rbMale.setToggleGroup(genderGroup);
		rbFemale.setToggleGroup(genderGroup);
		rbOther.setToggleGroup(genderGroup);

		TextField txtPhone = new TextField();
		txtPhone.setPromptText(LanguageManagerAd.getString("manageStaff.add.phone"));

		TextField txtAddress = new TextField();
		txtAddress.setPromptText(LanguageManagerAd.getString("manageStaff.add.address"));

		TextField txtEmail = new TextField();
		txtEmail.setPromptText(LanguageManagerAd.getString("manageStaff.add.email"));

		DatePicker dpDob = new DatePicker();
		dpDob.setPromptText(LanguageManagerAd.getString("manageStaff.add.dob"));
		dpDob.setDayCellFactory(picker -> new DateCell() {
			@Override
			public void updateItem(LocalDate item, boolean empty) {
				super.updateItem(item, empty);
				if (item != null && item.isAfter(LocalDate.now())) {
					setDisable(true);
					setStyle("-fx-background-color: #ffc0cb;");
				}
			}
		});

		ComboBox<String> cbRole = new ComboBox<>();
		cbRole.getItems().addAll("STAFF_RECEPTION", "STAFF_CASHIER", "STAFF_CARE", "ADMIN");
		cbRole.setPromptText(LanguageManagerAd.getString("manageStaff.add.role"));

		DatePicker dpStartDate = new DatePicker(LocalDate.now());
		dpStartDate.setPromptText(LanguageManagerAd.getString("manageStaff.add.startDate"));
		dpStartDate.setDayCellFactory(picker -> new DateCell() {
			@Override
			public void updateItem(LocalDate item, boolean empty) {
				super.updateItem(item, empty);
				if (item != null && item.isAfter(LocalDate.now())) {
					setDisable(true);
					setStyle("-fx-background-color: #ffc0cb;");
				}
			}
		});

		TextField txtSalary = new TextField();
		txtSalary.setPromptText(LanguageManagerAd.getString("manageStaff.add.salary"));
		txtSalary.setTextFormatter(new TextFormatter<>(change -> {
			String newText = change.getControlNewText();
			return newText.matches("\\d*") ? change : null;
		}));

		// Sắp xếp các trường nhập liệu trong GridPane
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		grid.add(new Label(LanguageManagerAd.getString("manageStaff.add.prompt.fullName")), 0, 0);
		grid.add(txtFullName, 1, 0);
		grid.add(new Label(LanguageManagerAd.getString("manageStaff.add.gender")), 0, 1);
		grid.add(rbMale, 1, 1);
		grid.add(rbFemale, 2, 1);
		grid.add(rbOther, 3, 1);
		grid.add(new Label(LanguageManagerAd.getString("manageStaff.add.prompt.phone")), 0, 2);
		grid.add(txtPhone, 1, 2);
		grid.add(new Label(LanguageManagerAd.getString("manageStaff.add.prompt.address")), 0, 3);
		grid.add(txtAddress, 1, 3);
		grid.add(new Label(LanguageManagerAd.getString("manageStaff.add.prompt.email")), 0, 4);
		grid.add(txtEmail, 1, 4);
		grid.add(new Label(LanguageManagerAd.getString("manageStaff.add.prompt.dob")), 0, 5);
		grid.add(dpDob, 1, 5);
		grid.add(new Label(LanguageManagerAd.getString("manageStaff.add.prompt.role")), 0, 6);
		grid.add(cbRole, 1, 6);
		grid.add(new Label(LanguageManagerAd.getString("manageStaff.add.prompt.startDate")), 0, 7);
		grid.add(dpStartDate, 1, 7);
		grid.add(new Label(LanguageManagerAd.getString("manageStaff.add.prompt.salary")), 0, 8);
		grid.add(txtSalary, 1, 8);

		dialog.getDialogPane().setContent(grid);

		// Thêm nút OK và Cancel
		ButtonType btnOk = new ButtonType(LanguageManagerAd.getString("manageStaff.add.button.ok"), ButtonBar.ButtonData.OK_DONE);
		ButtonType btnCancel = new ButtonType(LanguageManagerAd.getString("manageStaff.add.button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
		dialog.getDialogPane().getButtonTypes().addAll(btnOk, btnCancel);

		// Lấy nút OK từ dialog
		Node okButton = dialog.getDialogPane().lookupButton(btnOk);

		// Thêm bộ lọc sự kiện cho nút OK
		okButton.addEventFilter(ActionEvent.ACTION, event -> {
			try {
				// Lấy thông tin từ các trường nhập liệu
				String fullName = txtFullName.getText().trim();
				String phone = txtPhone.getText().trim();
				String address = txtAddress.getText().trim();
				String email = txtEmail.getText().trim();
				Double salary;
				try {
					salary = Double.parseDouble(txtSalary.getText().trim());
				} catch (NumberFormatException e) {
					// Xử lý trường hợp người dùng nhập không phải là số
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle(LanguageManagerAd.getString("manageStaff.add.alert.error.title"));
					alert.setHeaderText(LanguageManagerAd.getString("manageStaff.add.alert.salary.formatError.header"));
					alert.setContentText(LanguageManagerAd.getString("manageStaff.add.alert.salary.formatError.content"));
					alert.showAndWait();
					event.consume();
					return; // Dừng xử lý nếu không chuyển đổi được
				}
				LocalDate dob = dpDob.getValue();
				LocalDate startDate = dpStartDate.getValue();
				String roleName = cbRole.getValue();
				String gender = ((RadioButton) genderGroup.getSelectedToggle()).getText().trim();

				if (dob == null || dob.isAfter(LocalDate.now())
						|| Period.between(dob, LocalDate.now()).getYears() < 18) {
					throw new IllegalArgumentException(LanguageManagerAd.getString("manageStaff.add.alert.ageError"));
				}
				// Kiểm tra dữ liệu nhập
				validateStaffData(fullName, phone, email, startDate, salary, address, roleName, gender);

				// Nếu tất cả thông tin hợp lệ, cho phép dialog đóng
			} catch (IllegalArgumentException e) {
				// Hiển thị thông báo lỗi và ngăn dialog đóng
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle(LanguageManagerAd.getString("manageStaff.add.alert.error.title"));
				alert.setHeaderText(LanguageManagerAd.getString("manageStaff.add.alert.error.header"));
				alert.setContentText(e.getMessage());
				alert.showAndWait();
				event.consume(); // Ngăn dialog đóng
			} catch (Exception e) {
				// Hiển thị thông báo lỗi chung và ngăn dialog đóng
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle(LanguageManagerAd.getString("manageStaff.add.alert.error.title"));
				alert.setHeaderText(LanguageManagerAd.getString("manageStaff.add.alert.error.header"));
				alert.setContentText(LanguageManagerAd.getString("manageStaff.add.alert.error.content") + e.getMessage());
				alert.showAndWait();
				event.consume();
			}
		});

		// Xử lý khi nhấn nút OK
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == btnOk) {
				try {
					// Lấy thông tin từ các trường nhập liệu
					String fullName = txtFullName.getText().trim();
					String gender = ((RadioButton) genderGroup.getSelectedToggle()).getText().trim();
					String phone = txtPhone.getText().trim();
					String address = txtAddress.getText().trim();
					String email = txtEmail.getText().trim();
					LocalDate dob = dpDob.getValue();
					String roleName = cbRole.getValue();
					double salary = Double.parseDouble(txtSalary.getText().trim());
					LocalDate startDate = dpStartDate.getValue();

					int roleId = roleService.getRoleIdByRoleName(roleName);
					Role role = new Role(roleId, roleName);

					// Tạo tài khoản mặc định
					String accountName = roleName.toLowerCase().replace("staff_", "")
							+ String.format("%02d", staffList.size() + 1);

					accountService.register(accountName, "123456789", role); // Thêm tài khoản vào DB.
					Account account = accountService.getAccountByUsername(accountName);

					// Create the new Staff object
					Staff newStaff = new Staff();
					newStaff.setFullName(fullName);
					newStaff.setGender(GenderEnum.valueOf(gender));
					newStaff.setPhone(phone);
					newStaff.setAddress(address);
					newStaff.setEmail(email);
					newStaff.setDob(Date.valueOf(dob));
					newStaff.setRole(role);
					newStaff.setHire_date(Date.valueOf(startDate));
					newStaff.setSalary(salary);
					newStaff.setAccount(account); // Link with Account

					return newStaff;
				} catch (Exception e) {
					return null;
				}
			}
			return null;
		});

		// Lấy kết quả từ dialog
		Optional<Staff> result = dialog.showAndWait();
		result.ifPresent(newStaff -> {
			// Gọi StaffService để thêm nhân viên vào cơ sở dữ liệu
			boolean isAdded = staffService.addStaff(newStaff);

			if (isAdded) {
				loadData(); // Làm mới danh sách nhân viên
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle(LanguageManagerAd.getString("manageStaff.add.alert.success.title"));
				alert.setHeaderText(LanguageManagerAd.getString("manageStaff.add.alert.success.header"));
				alert.setContentText(LanguageManagerAd.getString("manageStaff.add.alert.success.content", newStaff.getFullName()) );
				alert.showAndWait();
			} else {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle(LanguageManagerAd.getString("manageStaff.add.alert.fail.title"));
				alert.setHeaderText(LanguageManagerAd.getString("manageStaff.add.alert.fail.header"));
				alert.setContentText(LanguageManagerAd.getString("manageStaff.add.alert.fail.content"));
				alert.showAndWait();
			}
		});
	}

	@FXML
	private void handleEditStaff() {
		// Lấy nhân viên được chọn từ bảng
		Staff selectedStaff = tblStaff.getSelectionModel().getSelectedItem();
		if (selectedStaff == null) {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle(LanguageManagerAd.getString("manageStaff.edit.alert.noSelection.title"));
			alert.setHeaderText(LanguageManagerAd.getString("manageStaff.edit.alert.noSelection.header"));
			alert.setContentText(LanguageManagerAd.getString("manageStaff.edit.alert.noSelection.content"));
			alert.showAndWait();
			return;
		}

		// Tạo dialog để chỉnh sửa thông tin nhân viên
		Dialog<Staff> dialog = new Dialog<>();
		dialog.setTitle(LanguageManagerAd.getString("manageStaff.edit.dialog.title"));
		dialog.setHeaderText(LanguageManagerAd.getString("manageStaff.edit.dialog.header"));

		// Tạo các trường nhập liệu và khởi tạo với thông tin của nhân viên được chọn
		TextField txtFullName = new TextField(selectedStaff.getFullName());
		txtFullName.setPromptText(LanguageManagerAd.getString("manageStaff.edit.field.fullName"));

		TextField txtPhone = new TextField(selectedStaff.getPhone());
		txtPhone.setPromptText(LanguageManagerAd.getString("manageStaff.edit.field.phone"));

		TextField txtEmail = new TextField(selectedStaff.getEmail());
		txtEmail.setPromptText(LanguageManagerAd.getString("manageStaff.add.alert.fail.content"));

		DatePicker dpStartDate = new DatePicker();
		if (selectedStaff.getHire_date() != null) {
			dpStartDate.setValue(new java.sql.Date(selectedStaff.getHire_date().getTime()).toLocalDate());
		}
		dpStartDate.setPromptText(LanguageManagerAd.getString("manageStaff.edit.field.startDate"));
		dpStartDate.setDayCellFactory(picker -> new DateCell() {
			@Override
			public void updateItem(LocalDate item, boolean empty) {
				super.updateItem(item, empty);
				if (item != null && item.isAfter(LocalDate.now())) {
					setDisable(true);
					setStyle("-fx-background-color: #ffc0cb;");
				}
			}
		});

		TextField txtSalary = new TextField(String.valueOf(selectedStaff.getSalary()));
		txtSalary.setPromptText(LanguageManagerAd.getString("manageStaff.edit.field.salary"));
		txtSalary.setTextFormatter(new TextFormatter<>(change -> {
			String newText = change.getControlNewText();
			return newText.matches("\\d*") ? change : null;
		}));

		ComboBox<String> cbRole = new ComboBox<>();
		cbRole.getItems().addAll("STAFF_RECEPTION", "STAFF_CASHIER", "STAFF_CARE", "ADMIN");
		cbRole.setValue(selectedStaff.getRole().getRoleName());
		cbRole.setPromptText(LanguageManagerAd.getString("manageStaff.edit.field.role"));

		// Tìm RadioButton tương ứng với giới tính của nhân viên
		ToggleGroup genderGroup = new ToggleGroup();
		RadioButton rbMale = new RadioButton(LanguageManagerAd.getString("manageStaff.add.gender.male"));
		RadioButton rbFemale = new RadioButton(LanguageManagerAd.getString("manageStaff.add.gender.female"));
		RadioButton rbOther = new RadioButton(LanguageManagerAd.getString("manageStaff.add.gender.other"));
		rbMale.setToggleGroup(genderGroup);
		rbFemale.setToggleGroup(genderGroup);
		rbOther.setToggleGroup(genderGroup);
		if (selectedStaff.getGender() != null) {
			switch (selectedStaff.getGender()) {
			case MALE:
				rbMale.setSelected(true);
				break;
			case FEMALE:
				rbFemale.setSelected(true);
				break;
			case OTHER:
				rbOther.setSelected(true);
				break;
			}
		}

		TextField txtAddress = new TextField(selectedStaff.getAddress());
		txtAddress.setPromptText(LanguageManagerAd.getString("manageStaff.edit.field.address"));

		// Sắp xếp các trường nhập liệu trong GridPane
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		grid.add(new Label(LanguageManagerAd.getString("manageStaff.edit.field.fullName")), 0, 0);
		grid.add(txtFullName, 1, 0);
		grid.add(new Label(LanguageManagerAd.getString("manageStaff.edit.field.gender")), 0, 1);
		grid.add(rbMale, 1, 1);
		grid.add(rbFemale, 2, 1);
		grid.add(rbOther, 3, 1);
		grid.add(new Label(LanguageManagerAd.getString("manageStaff.edit.field.phone")), 0, 2);
		grid.add(txtPhone, 1, 2);
		grid.add(new Label(LanguageManagerAd.getString("manageStaff.edit.field.address")), 0, 3);
		grid.add(txtAddress, 1, 3);
		grid.add(new Label(LanguageManagerAd.getString("manageStaff.edit.field.email")), 0, 4);
		grid.add(txtEmail, 1, 4);
		grid.add(new Label(LanguageManagerAd.getString("manageStaff.edit.field.startDate")), 0, 5);
		grid.add(dpStartDate, 1, 5);
		grid.add(new Label(LanguageManagerAd.getString("manageStaff.edit.field.salary")), 0, 6);
		grid.add(txtSalary, 1, 6);
		grid.add(new Label(LanguageManagerAd.getString("manageStaff.edit.field.role")), 0, 7);
		grid.add(cbRole, 1, 7);

		dialog.getDialogPane().setContent(grid);

		// Thêm nút OK và Cancel
		ButtonType btnOk = new ButtonType(LanguageManagerAd.getString("manageStaff.edit.button.ok"), ButtonBar.ButtonData.OK_DONE);
		ButtonType btnCancel = new ButtonType(LanguageManagerAd.getString("manageStaff.edit.button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
		dialog.getDialogPane().getButtonTypes().addAll(btnOk, btnCancel);

		// Xử lý khi nhấn nút OK
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == btnOk) {
				try {
					// Lấy thông tin từ các trường nhập liệu
					String fullName = txtFullName.getText().trim();
					String phone = txtPhone.getText().trim();
					String email = txtEmail.getText().trim();
					LocalDate startDate = dpStartDate.getValue();
					double salary = Double.parseDouble(txtSalary.getText().trim());
					String roleName = cbRole.getValue();
					String gender = ((RadioButton) genderGroup.getSelectedToggle()).getText().trim();
					String address = txtAddress.getText().trim();

					// Lấy roleId từ roleName
					int roleId = roleService.getRoleIdByRoleName(roleName);
					Role role = new Role(roleId, roleName);

					// Cập nhật thông tin nhân viên
					selectedStaff.setFullName(fullName);
					selectedStaff.setPhone(phone);
					selectedStaff.setEmail(email);
					selectedStaff.setHire_date(Date.valueOf(startDate));
					selectedStaff.setSalary(salary);
					selectedStaff.setRole(role);
					selectedStaff.setGender(GenderEnum.valueOf(gender));
					selectedStaff.setAddress(address);

					return selectedStaff;
				} catch (IllegalArgumentException e) {
					// Hiển thị thông báo lỗi
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle(LanguageManagerAd.getString("manageStaff.edit.alert.invalid.title"));
					alert.setHeaderText(LanguageManagerAd.getString("manageStaff.edit.alert.invalid.header"));
					alert.setContentText(e.getMessage());
					alert.showAndWait();
				} catch (Exception e) {
					// Hiển thị thông báo lỗi chung
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle(LanguageManagerAd.getString("manageStaff.edit.alert.invalid.title"));
					alert.setHeaderText(LanguageManagerAd.getString("manageStaff.edit.alert.invalid.header"));
					alert.setContentText(LanguageManagerAd.getString("manageStaff.edit.alert.invalid.content", e.getMessage()));
					alert.showAndWait();
				}
			}
			return null;
		});
		// Thêm bộ lọc sự kiện cho nút OK
		Node okButton = dialog.getDialogPane().lookupButton(btnOk);
		okButton.addEventFilter(ActionEvent.ACTION, event -> {
			try {
				// Lấy thông tin từ các trường nhập liệu
				String fullName = txtFullName.getText().trim();
				String phone = txtPhone.getText().trim();
				String email = txtEmail.getText().trim();
				String address = txtAddress.getText().trim();
				LocalDate startDate = dpStartDate.getValue();
				double salary = Double.parseDouble(txtSalary.getText().trim());
				String roleName = cbRole.getValue();
				String gender = ((RadioButton) genderGroup.getSelectedToggle()).getText().trim();

				// Kiểm tra dữ liệu nhập
				validateStaffData(fullName, phone, email, startDate, salary, address, roleName, gender);

				// Nếu dữ liệu hợp lệ, cho phép dialog đóng
			} catch (IllegalArgumentException e) {
				// Hiển thị thông báo lỗi và ngăn dialog đóng
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle(LanguageManagerAd.getString("manageStaff.edit.alert.invalid.title"));
				alert.setHeaderText(LanguageManagerAd.getString("manageStaff.edit.alert.invalid.header"));
				alert.setContentText(e.getMessage());
				alert.showAndWait();
				event.consume(); // Ngăn dialog đóng
			} catch (Exception e) {
				// Hiển thị thông báo lỗi chung và ngăn dialog đóng
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle(LanguageManagerAd.getString("manageStaff.edit.alert.invalid.title"));
				alert.setHeaderText(LanguageManagerAd.getString("manageStaff.edit.alert.invalid.header"));
				alert.setContentText(LanguageManagerAd.getString("manageStaff.edit.alert.invalid.content", e.getMessage()));
				alert.showAndWait();
			}
		});

		// Lấy kết quả từ dialog
		Optional<Staff> result = dialog.showAndWait();
		result.ifPresent(updatedStaff -> {
			boolean isUpdated = staffService.updateStaff(updatedStaff);

			if (isUpdated) {
				// Cập nhật role trong tài khoản liên kết
				Account account = updatedStaff.getAccount();
				if (account != null) {
					account.setRole(updatedStaff.getRole()); // Cập nhật role trong tài khoản
					account.setActive(true);
					boolean isAccountUpdated = accountService.updateAccount(account);

					if (!isAccountUpdated) {
						Alert alert = new Alert(Alert.AlertType.WARNING);
						alert.setTitle(LanguageManagerAd.getString("manageStaff.edit.alert.accountUpdate.failed.title"));
						alert.setHeaderText(LanguageManagerAd.getString("manageStaff.edit.alert.accountUpdate.failed.header"));
						alert.setContentText(
								LanguageManagerAd.getString("manageStaff.edit.alert.accountUpdate.failed.content"));
						alert.showAndWait();
					}
				}
				loadData(); // Làm mới danh sách nhân viên
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle(LanguageManagerAd.getString("manageStaff.edit.alert.update.success.title"));
				alert.setHeaderText(LanguageManagerAd.getString("manageStaff.edit.alert.update.success.header"));
				alert.setContentText(LanguageManagerAd.getString("manageStaff.edit.alert.update.success.content", updatedStaff.getFullName()));
				alert.showAndWait();
			} else {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle(LanguageManagerAd.getString("manageStaff.edit.alert.update.failed.title"));
				alert.setHeaderText(LanguageManagerAd.getString("manageStaff.edit.alert.update.failed.header"));
				alert.setContentText(LanguageManagerAd.getString("manageStaff.edit.alert.update.failed.content"));
				alert.showAndWait();
			}
		});
	}

	private void validateStaffData(String fullName, String phone, String email, LocalDate startDate, double salary,
			String address, String roleName, String gender) { // Thêm tham số dob
		if (fullName == null || fullName.trim().isEmpty() || !Pattern.matches("^[\\p{L}\\s]+$", fullName.trim())) {
			throw new IllegalArgumentException(LanguageManagerAd.getString("staff.validation.fullName.invalid"));
		}
		if (phone == null || !phone.matches("^[0-9]{10}$")) {
			throw new IllegalArgumentException(LanguageManagerAd.getString("staff.validation.phone.invalid"));
		}
		if (email == null || !email.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
			throw new IllegalArgumentException(LanguageManagerAd.getString("staff.validation.email.invalid"));
		}
		if (salary < 0 || salary > 99999999.99) {
			throw new IllegalArgumentException(LanguageManagerAd.getString("staff.validation.salary.invalid"));
		}
		if (roleName == null || roleName.trim().isEmpty()) {
			throw new IllegalArgumentException(LanguageManagerAd.getString("staff.validation.role.empty"));
		}
		if (address == null || address.trim().isEmpty()) {
			throw new IllegalArgumentException(LanguageManagerAd.getString("staff.validation.address.empty"));
		}
		if (gender == null || gender.trim().isEmpty()) {
			throw new IllegalArgumentException(LanguageManagerAd.getString("staff.validation.gender.empty"));
		}
		if (startDate.isAfter(LocalDate.now())) {
			throw new IllegalArgumentException(LanguageManagerAd.getString("staff.validation.startDate.future"));
		}
	}

	// thêm nút cho nghỉ việc role_name = OUT, tức là chỉnh sửa role thành OUT và
	// khi chuyển thành OUT thì account tương ứng cũng bị khóa
	@FXML
	private void handleTerminateStaff() {
		// Lấy nhân viên được chọn từ bảng
		Staff selectedStaff = tblStaff.getSelectionModel().getSelectedItem();
		if (selectedStaff == null) {
			showAlert(AlertType.WARNING, LanguageManagerAd.getString("manageStaff.terminate.alert.noSelection.title"), LanguageManagerAd.getString("manageStaff.terminate.alert.noSelection.content"));
			return;
		}

		// Xác nhận hành động
		Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
		confirmationAlert.setTitle(LanguageManagerAd.getString("manageStaff.terminate.confirm.title"));
		confirmationAlert.setHeaderText(LanguageManagerAd.getString("manageStaff.terminate.confirm.header"));
		confirmationAlert.setContentText(LanguageManagerAd.getString("manageStaff.terminate.confirm.content", selectedStaff.getFullName()));
		Optional<ButtonType> result = confirmationAlert.showAndWait();

		if (result.isPresent() && result.get() == ButtonType.OK) {
			// Cập nhật role thành OUT
			Role outRole = new Role(roleService.getRoleIdByRoleName("OUT"), "OUT"); // Lấy Role từ cơ sở dữ liệu
			selectedStaff.setRole(outRole);

			// Khóa tài khoản tương ứng
			Account account = selectedStaff.getAccount();
			if (account != null) {
				account.setActive(false);
			}

			// Gọi service để lưu thay đổi
			staffService.updateStaff(selectedStaff);
			accountService.updateAccount(account);

			// Cập nhật lại bảng
			tblStaff.refresh();

			// Hiển thị thông báo thành công
			showAlert(AlertType.INFORMATION, LanguageManagerAd.getString("manageStaff.terminate.success.title"), LanguageManagerAd.getString("manageStaff.terminate.success.header"));
		}
	}

	private void showAlert(Alert.AlertType alertType, String title, String content) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(null); // Không cần tiêu đề phụ
		alert.setContentText(content);
		alert.showAndWait();
	}

	@FXML
	private void handleDeleteStaff() {
		Staff selectedStaff = tblStaff.getSelectionModel().getSelectedItem();
		if (selectedStaff != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle(LanguageManagerAd.getString("manageStaff.delete.confirm.title"));
			alert.setHeaderText(LanguageManagerAd.getString("manageStaff.delete.confirm.header"));
			alert.setContentText(LanguageManagerAd.getString("manageStaff.delete.confirm.content", selectedStaff.getFullName()));

			// xóa trong cơ sở dữ liệu luôn, khi xóa thì account chuyển sang bị khóa
			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.OK) {
				// Xóa nhân viên
				// Cập nhật trạng thái tài khoản liên kết
				Account account = selectedStaff.getAccount();
				if (account != null) {
					account.setActive(false); // Đánh dấu tài khoản là không hoạt động
					accountService.updateAccount(account);
				}
				// Xóa nhân viên
				boolean isDeleted = staffService.deleteStaff(selectedStaff.getId());
				if (isDeleted) {
					loadData(); // Làm mới danh sách nhân viên
					Alert alertSuccess = new Alert(Alert.AlertType.INFORMATION);
					alertSuccess.setTitle(LanguageManagerAd.getString("manageStaff.delete.success.title"));
					alertSuccess.setHeaderText(LanguageManagerAd.getString("manageStaff.delete.success.header"));
					alertSuccess.setContentText(LanguageManagerAd.getString("manageStaff.delete.success.content", selectedStaff.getFullName()));
					alertSuccess.showAndWait();
				} else {
					Alert alertError = new Alert(Alert.AlertType.ERROR);
					alertError.setTitle(LanguageManagerAd.getString("manageStaff.delete.failed.title"));
					alertError.setHeaderText(LanguageManagerAd.getString("manageStaff.delete.failed.header"));
					alertError.setContentText(LanguageManagerAd.getString("manageStaff.delete.failed.content"));
					alertError.showAndWait();
				}
			}

		} else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle(LanguageManagerAd.getString("manageStaff.delete.alert.noSelection.title"));
			alert.setHeaderText(null);
			alert.setContentText(LanguageManagerAd.getString("manageStaff.delete.alert.noSelection.content"));
			alert.showAndWait();
		}
	}

	@Override
	public void onLanguageChanged() {
		loadTexts();
		
	}
	
	private void loadTexts() {
		lblTitle.setText(LanguageManagerAd.getString("manageStaff.title"));
        lblSearch.setText(LanguageManagerAd.getString("manageStaff.search.label"));
        txtSearch.setPromptText(LanguageManagerAd.getString("manageAccount.search.prompt"));
        btnSearch.setText(LanguageManagerAd.getString("manageStaff.search.button"));
        colStaffId.setText(LanguageManagerAd.getString("manageStaff.table.staffId"));
        colStaffName.setText(LanguageManagerAd.getString("manageStaff.table.staffName"));
        colStaffRole.setText(LanguageManagerAd.getString("manageStaff.table.role"));
        colStaffPhone.setText(LanguageManagerAd.getString("manageStaff.table.phone"));
        colStartDate.setText(LanguageManagerAd.getString("manageStaff.table.startDate"));
        colStaffEmail.setText(LanguageManagerAd.getString("manageStaff.table.email"));
        colSalary.setText(LanguageManagerAd.getString("manageStaff.table.salary"));
        colAccount.setText(LanguageManagerAd.getString("manageStaff.table.accountName"));
        btnAdd.setText(LanguageManagerAd.getString("manageStaff.button.add"));
        btnEdit.setText(LanguageManagerAd.getString("manageStaff.button.edit"));
        terminateButton.setText(LanguageManagerAd.getString("manageStaff.button.terminate"));
        btnDelete.setText(LanguageManagerAd.getString("manageStaff.button.delete"));
	}
}
