package controllers.admin;

import java.util.ArrayList;
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

public class ManageAccountController {

	@FXML
	private TextField txtSearch;

	@FXML
	private TableView<Account> tblAccounts;

	@FXML
	private TableColumn<Account, Integer> colAccountId;

	@FXML
	private TableColumn<Account, String> colUsername, colRole;

	@FXML
	private TableColumn<Account, Boolean> colActive;
	@FXML
	private TableColumn<Account, String> colPermissions;

	@FXML
	private Button btnAdd, btnEdit, btnDelete, btnResetPassword;
	@FXML
	private Button btnAssignPermission;


	private final AccountService accountService = new AccountService();

	private ObservableList<Account> accountList = FXCollections.observableArrayList();
	@FXML
	public void initialize() {
		// Initialize TableView columns
		colAccountId.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getAccountID()));
		colUsername.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getUserName()));
		colRole.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getRole().getRoleName()));
		colActive.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().isActive()));
		colActive.setCellFactory(column -> new TableCell<Account, Boolean>() {
			@Override
			protected void updateItem(Boolean isActive, boolean empty) {
				super.updateItem(isActive, empty);
				if (empty || isActive == null) {
					setText(null);
					setStyle("");
				} else {
					setText(isActive ? "Đang hoạt động" : "Bị khóa");

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
	        System.err.println("Danh sách tài khoản trống hoặc chưa được khởi tạo.");
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

//	@FXML
//	private void handleAddAccount() {
//		// Tạo dialog để nhập thông tin tài khoản mới
//		Dialog<Account> dialog = new Dialog<>();
//		dialog.setTitle("Thêm tài khoản");
//		dialog.setHeaderText("Nhập thông tin tài khoản mới");
//		dialog.setResizable(true);
//	
//		// Tạo các trường nhập liệu
//		VBox vbox = new VBox(10);
//		TextField txtUsername = new TextField();
//		txtUsername.setPromptText("Tên đăng nhập");
//		PasswordField txtPassword = new PasswordField();
//		txtPassword.setPromptText("Mật khẩu");
//	
//		ComboBox<String> cbRole = new ComboBox<>();
//		cbRole.getItems().addAll("ADMIN", "STAFF_CARE", "STAFF_CASHIER", "STAFF_RECEIPTION");
//		cbRole.getSelectionModel().selectFirst();
//		cbRole.setPromptText("Chọn vai trò");
//	
//		CheckBox cbActive = new CheckBox("Kích hoạt tài khoản");
//		cbActive.setSelected(true);
//	
//		Label lblError = new Label();
//		lblError.setStyle("-fx-text-fill: red;"); // Hiển thị lỗi bằng màu đỏ
//	
//		vbox.getChildren().addAll(new Label("Tên đăng nhập:"), txtUsername,
//								  new Label("Mật khẩu:"), txtPassword,
//								  new Label("Vai trò:"), cbRole,
//								  cbActive,
//								  lblError);
//	
//		dialog.getDialogPane().setContent(vbox);
//		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
//	
//		// Xử lý kết quả khi nhấn OK
//		dialog.setResultConverter(dialogButton -> {
//			if (dialogButton == ButtonType.OK) {
//				String username = txtUsername.getText().trim();
//				String password = txtPassword.getText().trim();
//				String roleName = cbRole.getValue();
//				boolean isActive = cbActive.isSelected();
//	
//				if (username.isEmpty() || password.isEmpty()) {
//					lblError.setText("Tên đăng nhập và mật khẩu không được để trống.");
//					return null; // Không đóng dialog
//				}
//	
//				// Tạo đối tượng Account mới
//				Account newAccount = new Account();
//				newAccount.setUserName(username);
//				newAccount.setPassword(password);
//				newAccount.setRole(new Role(0, roleName)); // Gán vai trò
//				newAccount.setActive(isActive);
//	
//				// Gọi service để kiểm tra lỗi
//				String validationError = accountService.validateAccountData(username, password);
//				if (validationError != null) {
//					lblError.setText(validationError); // Hiển thị lỗi
//					return null; // Không đóng dialog
//				}
//	
//				return newAccount; // Nếu không có lỗi, trả về tài khoản mới
//			}
//			return null;
//		});
//	
//		Optional<Account> result = dialog.showAndWait();
//		result.ifPresent(newAccount -> {
//			// Gọi service để thêm tài khoản
//			boolean success = accountService.register(newAccount.getUserName(), newAccount.getPassword(), newAccount.getRole());
//			if (success) {
//				Alert alert = new Alert(Alert.AlertType.INFORMATION);
//				alert.setTitle("Thành công");
//				alert.setHeaderText(null);
//				alert.setContentText("Tài khoản mới đã được thêm thành công.");
//				alert.showAndWait();
//				loadAccounts(); // Làm mới danh sách tài khoản
//			} else {
//				Alert alert = new Alert(Alert.AlertType.ERROR);
//				alert.setTitle("Lỗi");
//				alert.setHeaderText(null);
//				alert.setContentText("Không thể thêm tài khoản. Vui lòng thử lại.");
//				alert.showAndWait();
//			}
//		});
//	}

	@FXML
private void handleEditAccount() {
    Account selectedAccount = tblAccounts.getSelectionModel().getSelectedItem();
    if (selectedAccount != null) {
        // Tạo dialog để chỉnh sửa tài khoản
        Dialog<Account> dialog = new Dialog<>();
        dialog.setTitle("Chỉnh sửa tài khoản");
        dialog.setHeaderText("Chỉnh sửa vai trò và trạng thái hoạt động của tài khoản");
        dialog.setResizable(true);

        // Tạo các trường nhập liệu
        VBox vbox = new VBox(10);

        // ComboBox để chọn vai trò
        ComboBox<String> cbRole = new ComboBox<>();
        cbRole.getItems().addAll("ADMIN", "STAFF_CARE", "STAFF_CASHIER", "STAFF_RECEIPTION");
        cbRole.getSelectionModel().select(selectedAccount.getRole().getRoleName());
        cbRole.setPromptText("Chọn vai trò");

        // Nút để chỉnh sửa trạng thái hoạt động
        ToggleGroup toggleGroup = new ToggleGroup();
        RadioButton rbActive = new RadioButton("Mở (Đang hoạt động)");
        RadioButton rbInactive = new RadioButton("Khóa (Bị khóa)");
        rbActive.setToggleGroup(toggleGroup);
        rbInactive.setToggleGroup(toggleGroup);

        // Đặt trạng thái ban đầu
        if (selectedAccount.isActive()) {
            rbActive.setSelected(true);
        } else {
            rbInactive.setSelected(true);
        }

        // Thêm các thành phần vào VBox
        vbox.getChildren().addAll(
            new Label("Vai trò:"),
            cbRole,
            new Label("Trạng thái hoạt động:"),
            rbActive,
            rbInactive
        );

        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Xử lý kết quả khi nhấn OK
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String roleName = cbRole.getValue();
                boolean isActive = rbActive.isSelected(); // Lấy trạng thái từ RadioButton

                // Tạo đối tượng Account mới với thông tin đã chỉnh sửa
                Account updatedAccount = new Account();
                updatedAccount.setAccountID(selectedAccount.getAccountID());
				updatedAccount.setUserName(selectedAccount.getUserName()); 
				updatedAccount.setPassword(selectedAccount.getPassword()); 
				updatedAccount.setRole(new Role(0, roleName)); 
				updatedAccount.setActive(isActive);

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
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Tài khoản đã được cập nhật thành công.");
                alert.showAndWait();
                loadAccounts(); // Làm mới danh sách tài khoản
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Không thể cập nhật tài khoản. Vui lòng thử lại.");
                alert.showAndWait();
            }
        });
    } else {
        // Hiển thị cảnh báo nếu không có tài khoản nào được chọn
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText("Vui lòng chọn một tài khoản để chỉnh sửa.");
        alert.showAndWait();
    }
}

	@FXML
private void handleDeleteAccount() {
    Account selectedAccount = tblAccounts.getSelectionModel().getSelectedItem();
    if (selectedAccount != null) {
        // Hiển thị hộp thoại xác nhận
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa tài khoản này?");
        alert.setContentText("Tài khoản: " + selectedAccount.getUserName());

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
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText("Vui lòng chọn một tài khoản để xóa.");
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
			dialog.setTitle("Gán quyền");
			dialog.setHeaderText("Gán quyền cho tài khoản: " + selectedAccount.getUserName());

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
					if (selectedPermissions.contains(permission.getPermissionCode()) && !currentPermissions.contains(permission.getPermissionCode())) {
						permissionService.assignPermissionToAccount(selectedAccount.getAccountID(),
								permission.getPermissionCode());
					} else if (!selectedPermissions.contains(permission.getPermissionCode()) && currentPermissions.contains(permission.getPermissionCode())) {
						permissionService.removePermissionFromAccount(selectedAccount.getAccountID(),
								permission.getPermissionCode());
					}
				}
				loadAccounts(); // Làm mới bảng
			});
		} else {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("Cảnh báo");
			alert.setHeaderText(null);
			alert.setContentText("Vui lòng chọn một tài khoản để gán quyền.");
			alert.showAndWait();
		}
	}

	@FXML
	private void handleResetPassword() {
		Account selectedAccount = tblAccounts.getSelectionModel().getSelectedItem();
		if (selectedAccount != null) {
			// Confirm and reset password
			System.out.println("Reset mật khẩu cho tài khoản: " + selectedAccount.getUserName());
		}
	}
}