package controllers.Staff;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.ResourceBundle;

import controllers.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import model.Account;
import model.Staff;
import service.AuthService;
import service.StaffService;
import utils.Session;
import utils.AvatarUtility;
import utils.ValidatorUtil;
import utils.LanguageChangeListener;
import utils.LanguageManagerStaff;

public class EditProfileController implements Initializable, LanguageChangeListener {

	@FXML
	private Button changePasswordConfirmBtn;
	@FXML
	private TextField usernameField;
	@FXML
	private TextField fullNameField;
	@FXML
	private TextField emailField;
	@FXML
	private TextField phoneField;
	@FXML
	private TextField addressField;

	@FXML
	private PasswordField currentPasswordField;
	@FXML
	private PasswordField newPasswordField;
	@FXML
	private PasswordField confirmPasswordField;

	@FXML
	private Button updateProfileBtn;
	@FXML
	private Button backButton;
	@FXML
	private Button changePasswordBtn;
	@FXML
	private Button backToProfileBtn;
	@FXML
	private Button changeAvatarBtn;

	@FXML
	private ImageView avatarImageView;

	// Labels for internationalization
	@FXML
	private Label headerTitle;
	@FXML
	private Label formTitle;
	@FXML
	private Label passwordFormTitle;
	@FXML
	private Label usernameLabel;
	@FXML
	private Label fullNameLabel;
	@FXML
	private Label emailLabel;
	@FXML
	private Label phoneLabel;
	@FXML
	private Label addressLabel;
	@FXML
	private Label currentPasswordLabel;
	@FXML
	private Label newPasswordLabel;
	@FXML
	private Label confirmPasswordLabel;

	@FXML
	private VBox profileForm;
	@FXML
	private VBox passwordForm;
	@FXML
	private VBox mainContainer;

	private StaffService staffService;
	private AuthService authService;
	private String currentAvatarPath;

	public EditProfileController() {
		staffService = new StaffService();
		authService = new AuthService();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Register for language change notifications
		LanguageManagerStaff.addListener(this);
		
		// Initialize UI text
		loadTexts();
		
		// Load profile data
		loadProfile();
		loadAvatar();
		setupResponsive();
	}

	@Override
	public void onLanguageChanged() {
		loadTexts();
	}

	/**
	 * Load all text elements with current language
	 */
	private void loadTexts() {
		// Update all text elements
		if (headerTitle != null) {
			headerTitle.setText(LanguageManagerStaff.getString("editProfile.title"));
		}
		
		if (formTitle != null) {
			formTitle.setText(LanguageManagerStaff.getString("editProfile.personalInfo"));
		}
		
		if (passwordFormTitle != null) {
			passwordFormTitle.setText(LanguageManagerStaff.getString("editProfile.changePassword"));
		}
		
		// Field labels
		if (usernameLabel != null) {
			usernameLabel.setText(LanguageManagerStaff.getString("editProfile.username"));
		}
		if (fullNameLabel != null) {
			fullNameLabel.setText(LanguageManagerStaff.getString("editProfile.fullName"));
		}
		if (emailLabel != null) {
			emailLabel.setText(LanguageManagerStaff.getString("editProfile.email"));
		}
		if (phoneLabel != null) {
			phoneLabel.setText(LanguageManagerStaff.getString("editProfile.phone"));
		}
		if (addressLabel != null) {
			addressLabel.setText(LanguageManagerStaff.getString("editProfile.address"));
		}
		if (currentPasswordLabel != null) {
			currentPasswordLabel.setText(LanguageManagerStaff.getString("editProfile.currentPassword"));
		}
		if (newPasswordLabel != null) {
			newPasswordLabel.setText(LanguageManagerStaff.getString("editProfile.newPassword"));
		}
		if (confirmPasswordLabel != null) {
			confirmPasswordLabel.setText(LanguageManagerStaff.getString("editProfile.confirmPassword"));
		}
		
		// Buttons
	
		if (changeAvatarBtn != null) {
			changeAvatarBtn.setText(LanguageManagerStaff.getString("editProfile.changeAvatar"));
		}
		if (updateProfileBtn != null) {
			updateProfileBtn.setText(LanguageManagerStaff.getString("editProfile.update"));
		}
		if (changePasswordBtn != null) {
			changePasswordBtn.setText(LanguageManagerStaff.getString("editProfile.changePassword"));
		}
		if (backToProfileBtn != null) {
			backToProfileBtn.setText(LanguageManagerStaff.getString("common.back"));
		}
		if (backButton != null) {
			backButton.setText(LanguageManagerStaff.getString("editProfile.backToHome"));
		}
		
		// Prompt texts
		if (usernameField != null) {
			usernameField.setPromptText(LanguageManagerStaff.getString("editProfile.usernamePlaceholder"));
		}
		if (fullNameField != null) {
			fullNameField.setPromptText(LanguageManagerStaff.getString("editProfile.fullNamePlaceholder"));
		}
		if (emailField != null) {
			emailField.setPromptText(LanguageManagerStaff.getString("editProfile.emailPlaceholder"));
		}
		if (phoneField != null) {
			phoneField.setPromptText(LanguageManagerStaff.getString("editProfile.phonePlaceholder"));
		}
		if (addressField != null) {
			addressField.setPromptText(LanguageManagerStaff.getString("editProfile.addressPlaceholder"));
		}
		if (currentPasswordField != null) {
			currentPasswordField.setPromptText(LanguageManagerStaff.getString("editProfile.currentPasswordPlaceholder"));
		}
		if (newPasswordField != null) {
			newPasswordField.setPromptText(LanguageManagerStaff.getString("editProfile.newPasswordPlaceholder"));
		}
		if (confirmPasswordField != null) {
			confirmPasswordField.setPromptText(LanguageManagerStaff.getString("editProfile.confirmPasswordPlaceholder"));
		}
	}

	/**
	 * Setup responsive design
	 */
	private void setupResponsive() {
		mainContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
			if (newScene != null) {
				newScene.widthProperty().addListener((obs2, oldWidth, newWidth) -> {
					updateResponsiveClasses(newScene);
				});
				updateResponsiveClasses(newScene);
			}
		});
		
		mainContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
			if (newScene != null) {
				applyResponsiveLayout();
			}
		});
	}

	/**
	 * Apply responsive layout based on screen size
	 */
	private void applyResponsiveLayout() {
		double screenWidth = Screen.getPrimary().getBounds().getWidth();
		Parent root = mainContainer.getScene().getRoot();

		root.getStyleClass().removeAll("small-screen", "large-screen");

		if (screenWidth < 1024) {
			root.getStyleClass().add("small-screen");
		} else if (screenWidth >= 1920) {
			root.getStyleClass().add("large-screen");
		}
	}

	/**
	 * Update responsive classes based on scene size
	 */
	private void updateResponsiveClasses(Scene scene) {
		double width = scene.getWidth();
		Parent root = scene.getRoot();

		root.getStyleClass().removeAll("small-screen", "large-screen");

		if (width < 800) {
			root.getStyleClass().add("small-screen");
		} else if (width >= 1600) {
			root.getStyleClass().add("large-screen");
		}
	}

	/**
	 * Load profile information from database
	 */
	private void loadProfile() {
		Session.getInstance();
		Account account = Session.getCurrentAccount();
		if (account != null) {
			usernameField.setText(account.getUserName());
			
			Staff staff = staffService.getStaffByAccountID(account.getAccountID());
			if (staff != null) {
				fullNameField.setText(staff.getFullName());
				emailField.setText(staff.getEmail());
				phoneField.setText(staff.getPhone());
				addressField.setText(staff.getAddress());
			}
		}
	}

	@FXML
	private void handleUpdateProfile(ActionEvent event) {
		try {
			Session.getInstance();
			Account account = Session.getCurrentAccount();
			Staff staff = staffService.getStaffByAccountID(account.getAccountID());

			// Update username
			String newUsername = usernameField.getText().trim();
			String oldUsername = account.getUserName();
			
			if (!newUsername.equals(oldUsername)) {
				// Validate username format
				try {
					ValidatorUtil.validateUsername(newUsername);
				} catch (IllegalArgumentException e) {
					showAlert(Alert.AlertType.ERROR, 
						LanguageManagerStaff.getString("error.title"), 
						LanguageManagerStaff.getString("editProfile.error.usernameFormat"));
					return;
				}
				
				// Check if username already exists
				if (authService.isUsernameExists(newUsername)) {
					showAlert(Alert.AlertType.ERROR, 
						LanguageManagerStaff.getString("error.title"), 
						LanguageManagerStaff.getString("editProfile.error.usernameExists"));
					return;
				}
				
				// Release image resource before renaming file
				releaseAvatarImageResource();
				
				// Update username in database
				boolean usernameUpdated = authService.updateUsername(account.getAccountID(), newUsername);
				if (!usernameUpdated) {
					throw new Exception(LanguageManagerStaff.getString("editProfile.error.updateUsername"));
				}
				
				// Rename avatar file if exists
				boolean avatarRenamed = renameAvatarFile(oldUsername, newUsername);
				if (!avatarRenamed) {
					System.err.println("Warning: Could not rename avatar file, but username was updated");
				}
				
				// Update account object
				account.setUserName(newUsername);
			}

			// Update staff information
			staff.setFullName(fullNameField.getText());
			staff.setEmail(emailField.getText());
			staff.setPhone(phoneField.getText());
			staff.setAddress(addressField.getText());

			// Validate input before updating
			staffService.validatePerson(staff);

			// Update staff
			boolean success = staffService.updateStaff(staff);

			if (success) {
				// Update session with new information
				Session.setCurrentAccount(account);
				
				// Reload avatar with new username
				loadAvatar();
				
				showAlert(Alert.AlertType.INFORMATION, 
					LanguageManagerStaff.getString("success.title"), 
					LanguageManagerStaff.getString("editProfile.success.update"));
			} else {
				throw new Exception(LanguageManagerStaff.getString("editProfile.error.updateFailed"));
			}
		} catch (Exception e) {
			showAlert(Alert.AlertType.ERROR, 
				LanguageManagerStaff.getString("error.title"), 
				LanguageManagerStaff.getString("editProfile.error.updateFailed"));
			// Reload original information if error occurs
			loadProfile();
			loadAvatar();
		}
	}

	/**
	 * Release image resource to avoid file lock
	 */
	private void releaseAvatarImageResource() {
		try {
			avatarImageView.setImage(null);
			System.gc();
			Thread.sleep(100);
			System.out.println("Released avatar image resource");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			System.err.println("Interrupted while releasing image resource");
		}
	}

	/**
	 * Rename avatar file when username changes
	 */
	private boolean renameAvatarFile(String oldUsername, String newUsername) {
		try {
			Thread.sleep(200);
			
			boolean success = AvatarUtility.renameAvatar(oldUsername, newUsername);
			
			if (success) {
				System.out.println("Successfully renamed avatar file from " + oldUsername + " to " + newUsername);
			} else {
				System.err.println("Failed to rename avatar file from " + oldUsername + " to " + newUsername);
			}
			
			return success;
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			System.err.println("Interrupted while renaming avatar file");
			return false;
		}
	}

	/**
	 * Load avatar from avatars folder
	 */
	private void loadAvatar() {
		try {
			Session.getInstance();
			Account account = Session.getCurrentAccount();
			if (account != null) {
				String username = account.getUserName();
				
				File avatarFile = AvatarUtility.findAvatarFile(username);
				
				if (avatarFile != null && avatarFile.exists()) {
					String imageUrl = avatarFile.toURI().toString();
					Image avatarImage = new Image(imageUrl);
					
					if (!avatarImage.isError()) {
						avatarImageView.setImage(avatarImage);
						currentAvatarPath = avatarFile.getAbsolutePath();
						System.out.println("Successfully loaded avatar: " + currentAvatarPath);
					} else {
						System.err.println("Error loading avatar image");
						loadDefaultAvatar();
					}
				} else {
					System.out.println("Avatar file not found for user: " + username);
					loadDefaultAvatar();
				}
			}
		} catch (Exception e) {
			System.err.println("Error loading avatar: " + e.getMessage());
			loadDefaultAvatar();
		}
	}

	/**
	 * Load default avatar
	 */
	private void loadDefaultAvatar() {
		try {
			String[] possiblePaths = {
				"/resources/images/avatar_placeholder.png",
				"/images/avatar_placeholder.png", 
				"/avatar_placeholder.png"
			};
			
			Image defaultImage = null;
			String successPath = null;
			
			for (String path : possiblePaths) {
				try {
					var inputStream = getClass().getResourceAsStream(path);
					if (inputStream != null) {
						defaultImage = new Image(inputStream);
						if (!defaultImage.isError()) {
							successPath = path;
							break;
						}
					}
				} catch (Exception e) {
					continue;
				}
			}
			
			if (defaultImage != null && !defaultImage.isError()) {
				avatarImageView.setImage(defaultImage);
				currentAvatarPath = null;
				System.out.println("Successfully loaded default avatar from: " + successPath);
			} else {
				createPlaceholderAvatar();
			}
			
		} catch (Exception e) {
			System.err.println("Could not load default avatar: " + e.getMessage());
			createPlaceholderAvatar();
		}
	}

	/**
	 * Create simple placeholder avatar programmatically
	 */
	private void createPlaceholderAvatar() {
		try {
			javafx.scene.image.WritableImage placeholderImage = 
				new javafx.scene.image.WritableImage(100, 100);
			
			javafx.scene.image.PixelWriter pixelWriter = placeholderImage.getPixelWriter();
			
			javafx.scene.paint.Color backgroundColor = javafx.scene.paint.Color.LIGHTGRAY;
			javafx.scene.paint.Color borderColor = javafx.scene.paint.Color.GRAY;
			
			for (int x = 0; x < 100; x++) {
				for (int y = 0; y < 100; y++) {
					if (x < 2 || x > 97 || y < 2 || y > 97) {
						pixelWriter.setColor(x, y, borderColor);
					} else {
						pixelWriter.setColor(x, y, backgroundColor);
					}
				}
			}
			
			// Draw simple user icon (circle for head, rectangle for body)
			// Head (circle)
			for (int x = 35; x < 65; x++) {
				for (int y = 20; y < 45; y++) {
					double distance = Math.sqrt(Math.pow(x - 50, 2) + Math.pow(y - 32, 2));
					if (distance <= 12) {
						pixelWriter.setColor(x, y, javafx.scene.paint.Color.DARKGRAY);
					}
				}
			}
			
			// Body (rectangle)
			for (int x = 40; x < 60; x++) {
				for (int y = 45; y < 75; y++) {
					pixelWriter.setColor(x, y, javafx.scene.paint.Color.DARKGRAY);
				}
			}
			
			avatarImageView.setImage(placeholderImage);
			currentAvatarPath = null;
			System.out.println("Successfully created placeholder avatar");
			
		} catch (Exception e) {
			System.err.println("Could not create placeholder avatar: " + e.getMessage());
			avatarImageView.setImage(null);
		}
	}

	@FXML
	private void handleChangeAvatar(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		Stage stage = (Stage) changeAvatarBtn.getScene().getWindow();
		stage.setTitle(LanguageManagerStaff.getString("editProfile.selectAvatar"));
		
		// Set filters for image files
		fileChooser.getExtensionFilters().addAll(
			new FileChooser.ExtensionFilter(
				LanguageManagerStaff.getString("editProfile.imageFiles"), 
				"*.png", "*.jpg", "*.jpeg", "*.gif"
			),
			new FileChooser.ExtensionFilter("PNG", "*.png"),
			new FileChooser.ExtensionFilter("JPG", "*.jpg"),
			new FileChooser.ExtensionFilter("JPEG", "*.jpeg"),
			new FileChooser.ExtensionFilter("GIF", "*.gif")
		);
		
		File selectedFile = fileChooser.showOpenDialog(stage);
		
		if (selectedFile != null) {
			try {
				saveAvatar(selectedFile);
				loadAvatar();
				showAlert(Alert.AlertType.INFORMATION, 
					LanguageManagerStaff.getString("success.title"), 
					LanguageManagerStaff.getString("editProfile.success.avatarChanged"));
			} catch (Exception e) {
				showAlert(Alert.AlertType.ERROR, 
					LanguageManagerStaff.getString("error.title"), 
					LanguageManagerStaff.getString("editProfile.error.avatarChange"));
			}
		}
	}

	/**
	 * Save avatar to avatars folder
	 */
	private void saveAvatar(File selectedFile) throws IOException {
		Session.getInstance();
		Account account = Session.getCurrentAccount();
		if (account != null) {
			String username = usernameField.getText().trim();
			
			boolean success = AvatarUtility.saveAvatar(selectedFile, username);
			if (!success) {
				throw new IOException(LanguageManagerStaff.getString("editProfile.error.saveAvatar"));
			}
			
			File newAvatarFile = AvatarUtility.findAvatarFile(username);
			if (newAvatarFile != null) {
				currentAvatarPath = newAvatarFile.getAbsolutePath();
			}
		}
	}

	@FXML
	private void handleChangePasswordForm(ActionEvent event) {
		profileForm.setVisible(false);
		profileForm.setManaged(false);
		passwordForm.setVisible(true);
		passwordForm.setManaged(true);

		// Clear password fields
		currentPasswordField.clear();
		newPasswordField.clear();
		confirmPasswordField.clear();
	}

	@FXML
	private void handleBackToProfile(ActionEvent event) {
		passwordForm.setVisible(false);
		passwordForm.setManaged(false);
		profileForm.setVisible(true);
		profileForm.setManaged(true);
	}

	@FXML
	private void handleChangePassword(ActionEvent event) {
		String currentPassword = currentPasswordField.getText();
		String newPassword = newPasswordField.getText();
		String confirmPassword = confirmPasswordField.getText();

		// Check if new password and confirmation match
		if (!newPassword.equals(confirmPassword)) {
			showAlert(Alert.AlertType.ERROR, 
				LanguageManagerStaff.getString("error.title"), 
				LanguageManagerStaff.getString("editProfile.error.passwordMismatch"));
			return;
		}

		try {
			Session.getInstance();
			Account account = Session.getCurrentAccount();

			// Verify current password
			if (!authService.verifyPassword(account.getAccountID(), currentPassword)) {
				showAlert(Alert.AlertType.ERROR, 
					LanguageManagerStaff.getString("error.title"), 
					LanguageManagerStaff.getString("editProfile.error.currentPasswordWrong"));
				return;
			}

			// Change password
			boolean success = authService.changePassword(account.getAccountID(), newPassword);

			if (success) {
				showAlert(Alert.AlertType.INFORMATION, 
					LanguageManagerStaff.getString("success.title"), 
					LanguageManagerStaff.getString("editProfile.success.passwordChanged"));
				handleBackToProfile(null);
			} else {
				throw new Exception(LanguageManagerStaff.getString("editProfile.error.passwordChangeFailed"));
			}
		} catch (Exception e) {
			showAlert(Alert.AlertType.ERROR, 
				LanguageManagerStaff.getString("error.title"), 
				LanguageManagerStaff.getString("editProfile.error.passwordChangeFailed"));
		}
	}

	@FXML
	private void handleBack(ActionEvent event) {
		if (Session.getCurrentAccount().getRole().getRoleName().equals("ADMIN")) {
			SceneSwitcher.switchScene("admin/admin_home.fxml");
		} else {
			SceneSwitcher.switchScene("staff/staff_home.fxml");
		}
	}

//	@FXML
//	private void toggleLanguage(ActionEvent event) {
//		Locale currentLocale = LanguageManagerStaff.getCurrentLocale();
//		
//		// Toggle between Vietnamese and English
//		if (currentLocale.equals(new Locale("vi", "VN"))) {
//			LanguageManagerStaff.setLocale(new Locale("en", "US"));
//		} else {
//			LanguageManagerStaff.setLocale(new Locale("vi", "VN"));
//		}
//	}

	private void showAlert(Alert.AlertType type, String title, String message) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}