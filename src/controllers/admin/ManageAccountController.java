package controllers.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.Account;
import model.Permission;
import model.Role;
import service.AccountService;
import service.PermissionService;
import service.RoleService;
import utils.LanguageChangeListener;
import utils.LanguageManagerAd;

public class ManageAccountController implements LanguageChangeListener{

	@FXML private Label lblTitle;
    @FXML private Label lblSearch;
	@FXML private TextField txtSearch;

	@FXML private TableView<Account> tblAccounts;

	@FXML private TableColumn<Account, Integer> colAccountId;

	@FXML private TableColumn<Account, String> colUsername, colRole;

	@FXML private TableColumn<Account, Boolean> colActive;
	@FXML private TableColumn<Account, String> colPermissions;

	@FXML private Button btnAdd, btnEdit, btnDelete, btnResetPassword;
	@FXML private Button btnAssignPermission;
	@FXML private Button btnSearch;

	private final AccountService accountService = new AccountService();
	private final RoleService roleService = new RoleService();

	private ObservableList<Account> accountList = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		
		LanguageManagerAd.addListener(this);
		loadTexts();
        
		// Initialize TableView columns
		colAccountId.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getAccountID()));
		colUsername.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getUserName()));
		colRole.setCellValueFactory(cellData -> {
		    String roleCode = cellData.getValue().getRole().getRoleName(); // VD: STAFF_CARE
		    String localizedRole = LanguageManagerAd.getString("manageAccount.role." + roleCode);
		    return new ReadOnlyStringWrapper(localizedRole);
		});
		colActive.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().isActive()));
		colActive.setCellFactory(column -> new TableCell<Account, Boolean>() {
			@Override
			protected void updateItem(Boolean isActive, boolean empty) {
				super.updateItem(isActive, empty);
				if (empty || isActive == null) {
					setText(null);
					setStyle("");
				} else {
					setText(isActive 
							? LanguageManagerAd.getString("manageAccount.status.active") 
							: LanguageManagerAd.getString("manageAccount.status.inactive"));

					if (isActive) {
						setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
					} else {
						setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
					}
				}
			}
		});

		// Load accounts into TableView
		loadAccounts();

		// Add listeners
		tblAccounts.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			boolean isSelected = newSelection != null;
			btnEdit.setDisable(!isSelected);
			btnDelete.setDisable(!isSelected);
			btnResetPassword.setDisable(!isSelected);
			btnAssignPermission.setDisable(!isSelected);
		});

		txtSearch.textProperty().addListener((observable, oldValue, newValue) -> filterAccounts(newValue));
	}

	private void loadAccounts() {
		Map<Account, String> accountPermissionsMap = accountService.getAllAccountsWithPermissions();
		List<Account> accounts = new ArrayList<>(accountPermissionsMap.keySet());

		// Gán dữ liệu vào accountList
		accountList.setAll(accounts);

		// Gắn danh sách tài khoản vào bảng
		tblAccounts.setItems(accountList);

		// Gắn danh sách quyền vào cột "Quyền"
		colPermissions.setCellValueFactory(cellData -> {
			Account account = cellData.getValue();
			String permissions = accountPermissionsMap.get(account);
			return new SimpleStringProperty(permissions);
		});
	}

	private void filterAccounts(String keyword) {
		if (accountList == null || accountList.isEmpty()) {
			System.err.println(LanguageManagerAd.getString("manageAccount.error.empty_account_list"));
			return;
		}

		if (keyword == null || keyword.isEmpty()) {
			tblAccounts.setItems(accountList); // Hiển thị toàn bộ danh sách nếu không có từ khóa
		} else {
			ObservableList<Account> filteredList = FXCollections.observableArrayList();
			for (Account account : accountList) {
				// Lọc theo username hoặc roleName
				if (account.getUserName().toLowerCase().contains(keyword.toLowerCase())
						|| (account.getRole() != null && account.getRole().getRoleName() != null
								&& account.getRole().getRoleName().toLowerCase().contains(keyword.toLowerCase()))) {
					filteredList.add(account);
				}
			}
			tblAccounts.setItems(filteredList); // Cập nhật danh sách hiển thị
		}
	}

	@FXML
	private void handleEditAccount() {
		Account selectedAccount = tblAccounts.getSelectionModel().getSelectedItem();
		if (selectedAccount != null) {
			// Tạo dialog để chỉnh sửa tài khoản
			Dialog<Account> dialog = new Dialog<>();
			dialog.setTitle(LanguageManagerAd.getString("manageAccount.edit.dialog.title"));
			dialog.setHeaderText(LanguageManagerAd.getString("manageAccount.edit.dialog.header"));
			dialog.setResizable(true);

			// Tạo các trường nhập liệu
			VBox vbox = new VBox(10);

			// ComboBox để chọn vai trò
			ComboBox<String> cbRole = new ComboBox<>();
			// Tạo map ánh xạ giữa tên hiển thị và roleKey
			Map<String, String> displayNameToRoleKey = new HashMap<>();
			List<String> roleKeys = Arrays.asList("ADMIN", "STAFF_CARE", "STAFF_CASHIER", "STAFF_RECEPTION");

			for (String roleKey : roleKeys) {
			    String localizedRoleName = LanguageManagerAd.getString("manageAccount.role." + roleKey);
			    cbRole.getItems().add(localizedRoleName);
			    displayNameToRoleKey.put(localizedRoleName, roleKey);
			}

			// Chọn item theo roleKey của tài khoản
			String selectedRoleKey = selectedAccount.getRole().getRoleName();
			String selectedLocalizedRole = LanguageManagerAd.getString("manageAccount.role." + selectedRoleKey);
			cbRole.getSelectionModel().select(selectedLocalizedRole);

			cbRole.setPromptText(LanguageManagerAd.getString("manageAccount.edit.label.role"));

			// Khi lưu lại, lấy roleKey từ tên hiển thị đã chọn
			String selectedDisplayName = cbRole.getSelectionModel().getSelectedItem();
			String roleKeyToSave = displayNameToRoleKey.get(selectedDisplayName);


			// Nút để chỉnh sửa trạng thái hoạt động
			ToggleGroup toggleGroup = new ToggleGroup();
			RadioButton rbActive = new RadioButton(LanguageManagerAd.getString("manageAccount.edit.status.active"));
			RadioButton rbInactive = new RadioButton(LanguageManagerAd.getString("manageAccount.edit.status.inactive"));
			rbActive.setToggleGroup(toggleGroup);
			rbInactive.setToggleGroup(toggleGroup);

			// Đặt trạng thái ban đầu
			if (selectedAccount.isActive()) {
				rbActive.setSelected(true);
			} else {
				rbInactive.setSelected(true);
			}

			// Thêm các thành phần vào VBox
			vbox.getChildren().addAll(new Label(LanguageManagerAd.getString("manageAccount.edit.label.role")), cbRole, new Label(LanguageManagerAd.getString("manageAccount.edit.label.status")), rbActive,
					rbInactive);

			dialog.getDialogPane().setContent(vbox);
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

			// Xử lý kết quả khi nhấn OK
			dialog.setResultConverter(dialogButton -> {
			    if (dialogButton == ButtonType.OK) {
			        String chosenDisplayName  = cbRole.getValue();
			        String roleKey = displayNameToRoleKey.get(chosenDisplayName);
			        if (roleKey == null) {
			            Alert alert = new Alert(Alert.AlertType.ERROR);
			            alert.setTitle(LanguageManagerAd.getString("manageAccount.edit.error.invalidRole.title"));
			            alert.setHeaderText(LanguageManagerAd.getString("manageAccount.edit.error.invalidRole.header"));
			            alert.setContentText(LanguageManagerAd.getString("manageAccount.edit.error.invalidRole.content"));
			            alert.showAndWait();
			            return null;
			        }

			        int roleId = roleService.getRoleIdByRoleName(roleKey);
			        if (roleId == -1) {
			            Alert alert = new Alert(Alert.AlertType.ERROR);
			            alert.setTitle(LanguageManagerAd.getString("manageAccount.edit.error.invalidRole.title"));
			            alert.setHeaderText(LanguageManagerAd.getString("manageAccount.edit.error.invalidRole.header"));
			            alert.setContentText(LanguageManagerAd.getString("manageAccount.edit.error.invalidRole.content"));
			            alert.showAndWait();
			            return null;
			        }

			        Account updatedAccount = new Account();
			        updatedAccount.setAccountID(selectedAccount.getAccountID());
			        updatedAccount.setUserName(selectedAccount.getUserName());
			        updatedAccount.setPassword(selectedAccount.getPassword());
			        updatedAccount.setRole(new Role(roleId, roleKey));
			        updatedAccount.setActive(rbActive.isSelected());

			        return updatedAccount;
			    }
			    return null;
			});


			// Hiển thị dialog và xử lý kết quả
			Optional<Account> result = dialog.showAndWait();
			result.ifPresent(updatedAccount -> {
				// Gọi service để cập nhật tài khoản
				boolean success = accountService.updateAccount(updatedAccount);
				if (success) {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle(LanguageManagerAd.getString("manageAccount.edit.success.title"));
					alert.setHeaderText(null);
					alert.setContentText(LanguageManagerAd.getString("manageAccount.edit.success.content"));
					alert.showAndWait();
					loadAccounts(); // Làm mới danh sách tài khoản
				} else {
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle(LanguageManagerAd.getString("manageAccount.edit.error.updateFailed.title"));
					alert.setHeaderText(null);
					alert.setContentText(LanguageManagerAd.getString("manageAccount.edit.error.updateFailed.content"));
					alert.showAndWait();
				}
			});
		} else {
			// Hiển thị cảnh báo nếu không có tài khoản nào được chọn
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle(LanguageManagerAd.getString("manageAccount.edit.warning.noSelection.title"));
			alert.setHeaderText(null);
			alert.setContentText(LanguageManagerAd.getString("manageAccount.edit.warning.noSelection.content"));
			alert.showAndWait();
		}
	}

	@FXML
	private void handleDeleteAccount() {
		Account selectedAccount = tblAccounts.getSelectionModel().getSelectedItem();
		if (selectedAccount != null) {
			// Hiển thị hộp thoại xác nhận
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle(LanguageManagerAd.getString("manageAccount.delete.confirm.title"));
			alert.setHeaderText(LanguageManagerAd.getString("manageAccount.delete.confirm.header"));
			alert.setContentText(LanguageManagerAd.getString("manageAccount.delete.confirm.content", selectedAccount.getUserName()));

			// Chờ người dùng chọn OK hoặc Cancel
			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.OK) {
				// Nếu người dùng chọn OK, thực hiện xóa
				accountService.deleteAccount(selectedAccount.getAccountID());
				loadAccounts(); // Làm mới danh sách sau khi xóa
			}
		} else {
			// Hiển thị cảnh báo nếu không có tài khoản nào được chọn
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle(LanguageManagerAd.getString("manageAccount.delete.warning.title"));
			alert.setHeaderText(null);
			alert.setContentText(LanguageManagerAd.getString("manageAccount.delete.warning.content"));
			alert.showAndWait();
		}
	}

	@FXML
	private void handleAssignPermission() {
		Account selectedAccount = tblAccounts.getSelectionModel().getSelectedItem();
		if (selectedAccount != null) {
			PermissionService permissionService = new PermissionService();

			// Lấy danh sách quyền hiện tại và tất cả quyền
			List<String> currentPermissions = permissionService
					.getPermissionsByAccountId(selectedAccount.getAccountID());
			List<Permission> allPermissions = permissionService.getAllPermissions();

			// Tạo hộp thoại gán quyền
			Dialog<List<String>> dialog = new Dialog<>();
			dialog.setTitle(LanguageManagerAd.getString("manageAccount.assignPermission.dialog.title"));
			dialog.setHeaderText(LanguageManagerAd.getString("manageAccount.assignPermission.dialog.header", selectedAccount.getUserName()));

			// Tạo danh sách checkbox cho các quyền
			VBox content = new VBox(10);
			List<CheckBox> checkBoxes = new ArrayList<>();
			for (Permission permission : allPermissions) {
				CheckBox checkBox = new CheckBox(permission.getPermissionCode());
				checkBox.setSelected(currentPermissions.contains(permission.getPermissionCode()));
				checkBoxes.add(checkBox);
				content.getChildren().add(checkBox);
			}
			dialog.getDialogPane().setContent(content);

			// Thêm nút OK và Cancel
			ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
			dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

			// Xử lý khi nhấn OK
			dialog.setResultConverter(dialogButton -> {
				if (dialogButton == okButtonType) {
					List<String> selectedPermissions = new ArrayList<>();
					for (CheckBox checkBox : checkBoxes) {
						if (checkBox.isSelected()) {
							selectedPermissions.add(checkBox.getText());
						}
					}
					return selectedPermissions;
				}
				return null;
			});

			dialog.showAndWait().ifPresent(selectedPermissions -> {
				// Cập nhật quyền trong cơ sở dữ liệu
				for (Permission permission : allPermissions) {
					if (selectedPermissions.contains(permission.getPermissionCode())
							&& !currentPermissions.contains(permission.getPermissionCode())) {
						permissionService.assignPermissionToAccount(selectedAccount.getAccountID(),
								permission.getPermissionCode());
					} else if (!selectedPermissions.contains(permission.getPermissionCode())
							&& currentPermissions.contains(permission.getPermissionCode())) {
						permissionService.removePermissionFromAccount(selectedAccount.getAccountID(),
								permission.getPermissionCode());
					}
				}
				loadAccounts(); // Làm mới bảng
			});
		} else {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle(LanguageManagerAd.getString("manageAccount.assignPermission.warning.title"));
			alert.setHeaderText(null);
			alert.setContentText(LanguageManagerAd.getString("manageAccount.assignPermission.warning.content"));
			alert.showAndWait();
		}
	}

	@FXML
	private void handleResetPassword() {
	    Account selectedAccount = tblAccounts.getSelectionModel().getSelectedItem();
	    if (selectedAccount != null) {
	        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
	        alert.setTitle(LanguageManagerAd.getString("manageAccount.resetPassword.confirm.title"));
	        alert.setHeaderText(LanguageManagerAd.getString("manageAccount.resetPassword.confirm.header"));
	        alert.setContentText(LanguageManagerAd.getString("manageAccount.resetPassword.confirm.content", selectedAccount.getUserName()));

	        Optional<ButtonType> result = alert.showAndWait();
	        if (result.isPresent() && result.get() == ButtonType.OK) {
	            boolean success = accountService.resetPassword(selectedAccount.getAccountID(), "123456789");
	            if (success) {
	                Alert info = new Alert(Alert.AlertType.INFORMATION);
	                info.setTitle(LanguageManagerAd.getString("manageAccount.resetPassword.success.title"));
	                info.setHeaderText(null);
	                info.setContentText(LanguageManagerAd.getString("manageAccount.resetPassword.success.content"));
	                info.showAndWait();
	            } else {
	                Alert error = new Alert(Alert.AlertType.ERROR);
	                error.setTitle(LanguageManagerAd.getString("manageAccount.resetPassword.error.title"));
	                error.setHeaderText(null);
	                error.setContentText(LanguageManagerAd.getString("manageAccount.resetPassword.error.content"));
	                error.showAndWait();
	            }
	        }
	    } else {
	        Alert alert = new Alert(Alert.AlertType.WARNING);
	        alert.setTitle(LanguageManagerAd.getString("manageAccount.resetPassword.warning.title"));
	        alert.setHeaderText(null);
	        alert.setContentText(LanguageManagerAd.getString("manageAccount.resetPassword.warning.content"));
	        alert.showAndWait();
	    }
	}

	@Override
	public void onLanguageChanged() {
		loadTexts();
		
	}
	
	private void loadTexts() {
		lblTitle.setText(LanguageManagerAd.getString("manageAccount.title"));
        lblSearch.setText(LanguageManagerAd.getString("manageAccount.search.label"));
        txtSearch.setPromptText(LanguageManagerAd.getString("manageStaff.search.prompt"));
        btnSearch.setText(LanguageManagerAd.getString("manageAccount.search.button"));
        colAccountId.setText(LanguageManagerAd.getString("manageAccount.table.accountId"));
        colUsername.setText(LanguageManagerAd.getString("manageAccount.table.username"));
        colRole.setText(LanguageManagerAd.getString("manageAccount.table.role"));
        colActive.setText(LanguageManagerAd.getString("manageAccount.table.active"));
        colPermissions.setText(LanguageManagerAd.getString("manageAccount.table.permissions"));
        btnEdit.setText(LanguageManagerAd.getString("manageAccount.button.edit"));
        btnDelete.setText(LanguageManagerAd.getString("manageAccount.button.delete"));
        btnResetPassword.setText(LanguageManagerAd.getString("manageAccount.button.resetPassword"));
        btnAssignPermission.setText(LanguageManagerAd.getString("manageAccount.button.assignPermission"));
        
	}



}