package controllers.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Service;
import service.ServiceService;

public class ManageService {

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
	private Button addButton;

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
					setText(active ? "Hoạt động" : "Ngừng hoạt động");
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
			if (service.getName().toLowerCase().contains(keyword.toLowerCase())
					|| service.getDescription().toLowerCase().contains(keyword.toLowerCase())) {
				filteredList.add(service);
			}
		}

		tableView.setItems(filteredList);
	}

	@FXML
	private void handleAddService() {
		Dialog<Service> dialog = new Dialog<>();
		dialog.setTitle("Thêm dịch vụ");

		// Tạo các trường nhập liệu
		TextField nameField = new TextField();
		nameField.setPromptText("Tên dịch vụ");

		TextField descriptionField = new TextField();
		descriptionField.setPromptText("Mô tả dịch vụ");

		TextField priceField = new TextField();
		priceField.setPromptText("Giá dịch vụ");

		TextField durationField = new TextField();
		durationField.setPromptText("Thời gian (phút)");

		// Bố trí các trường nhập liệu
		VBox content = new VBox(10, new HBox(10, new Label("Tên:"), nameField),
				new HBox(10, new Label("Mô tả:"), descriptionField), new HBox(10, new Label("Giá:"), priceField),
				new HBox(10, new Label("Thời gian:"), durationField));
		dialog.getDialogPane().setContent(content);

		// Thêm các nút Thêm và Hủy
		ButtonType addButtonType = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

		// Xử lý khi nhấn nút Thêm
		dialog.setResultConverter(button -> {
			if (button == addButtonType) {
				try {
					String name = nameField.getText();
					String description = descriptionField.getText();
					double price = Double.parseDouble(priceField.getText());
					int duration = Integer.parseInt(durationField.getText());

					// Tạo đối tượng Service mới
					Service newService = new Service(0, name, description, price, duration, true);

					// Thêm dịch vụ vào cơ sở dữ liệu
					if (serviceService.addService(newService)) {
						loadServicesFromDatabase();
					} else {
						showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm dịch vụ.");
					}
				} catch (NumberFormatException e) {
					showAlert(Alert.AlertType.ERROR, "Lỗi", "Giá và thời gian phải là số hợp lệ.");
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
			showAlert(Alert.AlertType.WARNING, "Chưa chọn dịch vụ", "Vui lòng chọn dịch vụ để sửa.");
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
		statusComboBox.getItems().addAll("Hoạt động", "Ngừng hoạt động");
		statusComboBox.setValue(selectedService.isActive() ? "Hoạt động" : "Ngừng hoạt động");

		// Bố trí các trường nhập liệu
		VBox content = new VBox(10, new HBox(10, new Label("Tên:"), nameField),
				new HBox(10, new Label("Mô tả:"), descriptionField), new HBox(10, new Label("Giá:"), priceField),
				new HBox(10, new Label("Thời gian:"), durationField),
				new HBox(10, new Label("Trạng thái:"), statusComboBox));
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
						showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể sửa dịch vụ.");
					}
				} catch (NumberFormatException e) {
					showAlert(Alert.AlertType.ERROR, "Lỗi", "Giá và thời gian phải là số hợp lệ.");
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

		if (serviceService.deleteService(selectedService)) {
			loadServicesFromDatabase(); // Làm mới danh sách hiển thị
		} else {
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa dịch vụ.");
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
			if (service.getName().toLowerCase().contains(searchText)
					|| service.getDescription().toLowerCase().contains(searchText)) {
				filteredList.add(service);
			}
		}

		tableView.setItems(filteredList);
	}

}