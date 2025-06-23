package controllers.admin;

import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Service;
import service.ServiceService;
import utils.LanguageChangeListener;
import utils.LanguageManagerAd;

public class ManageService implements LanguageChangeListener{
	@FXML private Label lblTitle;
    @FXML
    private TableView<Service> tableView;

    @FXML
    private TextField searchField;

    @FXML
    private TableColumn<Service, Integer> idColumn;

    @FXML
    private TableColumn<Service, String> nameColumn;

    @FXML
    private TableColumn<Service, Double> priceColumn;

    @FXML
    private TableColumn<Service, String> descriptionColumn;

    @FXML
    private TableColumn<Service, String> durationColumn;
    
    @FXML
    private TableColumn<Service, Boolean> statusColumn;

    @FXML
    private Button addButton, searchButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    private ObservableList<Service> serviceList;
    private final ServiceService serviceService;

    public ManageService() {
        this.serviceService = new ServiceService();
    }

    @FXML
    public void initialize() {
    	LanguageManagerAd.addListener(this);
    	loadTexts();
    	
        idColumn.setCellValueFactory(new PropertyValueFactory<>("serviceId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));

        // Hiển thị trạng thái "Hoạt động" hoặc "Ngừng hoạt động"
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        statusColumn.setCellFactory(column -> new TableCell<Service, Boolean>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setText(null);
                } else {
                    setText(active ? LanguageManagerAd.getString("manageService.dialog.status.active") : LanguageManagerAd.getString("manageService.dialog.status.inactive"));
                    setStyle(active ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });

        // Lấy danh sách từ cơ sở dữ liệu
        serviceList = FXCollections.observableArrayList(serviceService.getAllServices());
        tableView.setItems(serviceList);

        // Tìm kiếm theo thời gian thực
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterServices(newValue));
    }

    private void filterServices(String keyword) {
        ObservableList<Service> filteredList = FXCollections.observableArrayList();

        for (Service service : serviceList) {
            if (service.getName().toLowerCase().contains(keyword.toLowerCase()) || 
                service.getDescription().toLowerCase().contains(keyword.toLowerCase())) {
                filteredList.add(service);
            }
        }

        tableView.setItems(filteredList);
    }

    @FXML
    private void handleAddService() {
        Dialog<Service> dialog = new Dialog<>();
        dialog.setTitle(LanguageManagerAd.getString("manageService.dialog.add.title"));

        // Tạo các trường nhập liệu
        TextField nameField = new TextField();
        nameField.setPromptText(LanguageManagerAd.getString("manageService.column.name"));

        TextField descriptionField = new TextField();
        descriptionField.setPromptText(LanguageManagerAd.getString("manageService.column.description"));

        TextField priceField = new TextField();
        priceField.setPromptText(LanguageManagerAd.getString("manageService.column.price"));

        TextField durationField = new TextField();
        durationField.setPromptText(LanguageManagerAd.getString("manageService.column.duration"));

        // Bố trí các trường nhập liệu
        VBox content = new VBox(10, 
            new HBox(10, new Label(LanguageManagerAd.getString("manageService.column.name")), nameField),
            new HBox(10, new Label(LanguageManagerAd.getString("manageService.column.description")), descriptionField),
            new HBox(10, new Label(LanguageManagerAd.getString("manageService.column.price")), priceField),
            new HBox(10, new Label(LanguageManagerAd.getString("manageService.column.duration")), durationField)
        );
        dialog.getDialogPane().setContent(content);

        // Thêm các nút Thêm và Hủy
        ButtonType addButtonType = new ButtonType(LanguageManagerAd.getString("manageService.button.add"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Xử lý khi nhấn nút Thêm
        dialog.setResultConverter(button -> {
        	if (button == addButtonType) {
        	    String name = nameField.getText().trim();
        	    String description = descriptionField.getText().trim();
        	    String priceText = priceField.getText().trim();
        	    String durationText = durationField.getText().trim();

        	    if (name.isEmpty()) {
        	        showAlert(Alert.AlertType.WARNING, LanguageManagerAd.getString("manageService.alert.warning.title"), LanguageManagerAd.getString("manageService.alert.warning.empty.name"));
        	        return null;
        	    }

        	    if (serviceService.isServiceExists(name)) {
        	        showAlert(Alert.AlertType.WARNING, LanguageManagerAd.getString("manageService.alert.warning.title"), LanguageManagerAd.getString("manageService.alert.warning.name.exists"));
        	        return null;
        	    }

        	    try {
        	        double price = Double.parseDouble(priceText);
        	        int duration = Integer.parseInt(durationText);

        	        if (price <= 10000 || duration <= 5 || price >= 100000000 || duration > 61) {
        	            showAlert(Alert.AlertType.WARNING, LanguageManagerAd.getString("manageService.alert.warning.title"), LanguageManagerAd.getString("manageService.alert.warning.price.duration"));
        	            return null;
        	        }

        	        Service newService = new Service(0, name, description, price, duration, true);

        	        if (serviceService.addService(newService)) {
        	            loadServicesFromDatabase();
        	        } else {
        	            showAlert(Alert.AlertType.ERROR, LanguageManagerAd.getString("manageService.alert.error.title"), LanguageManagerAd.getString("manageService.alert.error.add.fail"));
        	        }

        	    } catch (NumberFormatException e) {
        	        showAlert(Alert.AlertType.ERROR, LanguageManagerAd.getString("manageService.alert.error.title"), LanguageManagerAd.getString("manageService.alert.error.invalid.number"));
        	    }
        	}
        	return null;

        });

        dialog.showAndWait();
    }

    private void loadServicesFromDatabase() {
        serviceList = FXCollections.observableArrayList(serviceService.getAllServices());
        tableView.setItems(serviceList);
    }

    @FXML
    private void handleEditService() {
        Service selectedService = tableView.getSelectionModel().getSelectedItem();
        if (selectedService == null) {
            showAlert(Alert.AlertType.WARNING, LanguageManagerAd.getString("manageService.alert.warning.title"), LanguageManagerAd.getString("manageService.alert.warning.select.edit"));
            return;
        }

        Dialog<Service> dialog = new Dialog<>();
        dialog.setTitle("Sửa dịch vụ");

        // Tạo các trường nhập liệu
        TextField nameField = new TextField(selectedService.getName());
        TextField descriptionField = new TextField(selectedService.getDescription());
        TextField priceField = new TextField(String.valueOf(selectedService.getPrice()));
        TextField durationField = new TextField(String.valueOf(selectedService.getDurationMinutes()));

        // Tạo ComboBox để chọn trạng thái
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll(LanguageManagerAd.getString("manageService.dialog.status.active"), LanguageManagerAd.getString("manageService.dialog.status.inactive"));
        statusComboBox.setValue(selectedService.isActive() ? LanguageManagerAd.getString("manageService.dialog.status.active") : LanguageManagerAd.getString("manageService.dialog.status.inactive"));

        // Bố trí các trường nhập liệu
        VBox content = new VBox(10, 
    		new HBox(10, new Label(LanguageManagerAd.getString("manageService.column.name")), nameField),
            new HBox(10, new Label(LanguageManagerAd.getString("manageService.column.description")), descriptionField),
            new HBox(10, new Label(LanguageManagerAd.getString("manageService.column.price")), priceField),
            new HBox(10, new Label(LanguageManagerAd.getString("manageService.column.duration")), durationField),
            new HBox(10, new Label("Trạng thái:"), statusComboBox)
        );
        dialog.getDialogPane().setContent(content);

        // Thêm các nút Sửa và Hủy
        ButtonType editButtonType = new ButtonType("Sửa", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(editButtonType, ButtonType.CANCEL);

        // Xử lý khi nhấn nút Sửa
        dialog.setResultConverter(button -> {
            if (button == editButtonType) {
                try {
                    selectedService.setName(nameField.getText());
                    selectedService.setDescription(descriptionField.getText());
                    selectedService.setPrice(Double.parseDouble(priceField.getText()));
                    selectedService.setDurationMinutes(Integer.parseInt(durationField.getText()));
                    selectedService.setActive(statusComboBox.getValue().equals("Hoạt động"));

                    // Cập nhật dịch vụ trong cơ sở dữ liệu
                    if (serviceService.updateService(selectedService)) {
                        loadServicesFromDatabase(); // Làm mới danh sách hiển thị
                    } else {
                        showAlert(Alert.AlertType.ERROR, LanguageManagerAd.getString("manageService.alert.error.title"), "Không thể sửa dịch vụ.");
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, LanguageManagerAd.getString("manageService.alert.error.title"), "Giá và thời gian phải là số hợp lệ.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }
    
    @FXML
    private void handleDeleteService() {
        Service selectedService = tableView.getSelectionModel().getSelectedItem();
        if (selectedService == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn dịch vụ", "Vui lòng chọn dịch vụ để xóa.");
            return;
        }

        // Tạo hộp thoại xác nhận xóa
        Alert confirmDeleteAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDeleteAlert.setTitle("Xác nhận xóa");
        confirmDeleteAlert.setHeaderText("Bạn có chắc chắn muốn xóa dịch vụ này?");
        confirmDeleteAlert.setContentText("Dịch vụ: " + selectedService.getName());

        // Hiển thị hộp thoại và chờ người dùng phản hồi
        Optional<ButtonType> result = confirmDeleteAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Nếu người dùng nhấn "OK", thực hiện xóa
            if (serviceService.deleteService(selectedService)) {
                loadServicesFromDatabase(); // Làm mới danh sách hiển thị
            } else {
                showAlert(Alert.AlertType.ERROR, LanguageManagerAd.getString("manageService.alert.error.title"), "Không thể xóa dịch vụ.");
            }
        } else {
            // Nếu người dùng nhấn "Cancel" hoặc đóng hộp thoại, không làm gì cả
            return;
        }
    }


    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        ObservableList<Service> filteredList = FXCollections.observableArrayList();

        for (Service service : serviceList) {
            if (service.getName().toLowerCase().contains(searchText) || 
                service.getDescription().toLowerCase().contains(searchText)) {
                filteredList.add(service);
            }
        }

        tableView.setItems(filteredList);
    }

    @Override
	public void onLanguageChanged() {
		loadTexts();
		
	}
	
	private void loadTexts() {
		lblTitle.setText(LanguageManagerAd.getString("manageService.title"));
		searchField.setPromptText(LanguageManagerAd.getString("manageService.search.placeholder"));
		searchButton.setText(LanguageManagerAd.getString("manageService.search.button"));
		idColumn.setText(LanguageManagerAd.getString("manageService.column.id"));
		nameColumn.setText(LanguageManagerAd.getString("manageService.column.name"));
		descriptionColumn.setText(LanguageManagerAd.getString("manageService.column.description"));
		priceColumn.setText(LanguageManagerAd.getString("manageService.column.price"));
		durationColumn.setText(LanguageManagerAd.getString("manageService.column.duration"));
		statusColumn.setText(LanguageManagerAd.getString("manageService.column.status"));
		addButton.setText(LanguageManagerAd.getString("manageService.button.add"));
		editButton.setText(LanguageManagerAd.getString("manageService.button.edit"));
		deleteButton.setText(LanguageManagerAd.getString("manageService.button.delete"));
	}
    
}