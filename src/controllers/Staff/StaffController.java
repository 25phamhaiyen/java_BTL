package controllers.Staff;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import controllers.SceneSwitcher;
import enums.Shift;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.Booking;
import model.Customer;
import model.Pet;
import model.Staff;
import model.WorkSchedule;
import service.BookingService;
import service.ScheduleService;
import service.StaffService;
import utils.RoleChecker;
import javafx.scene.control.ListCell;
import utils.Session;

public class StaffController implements Initializable {

    @FXML
    private BorderPane mainContainer;

    @FXML
    private Label staffNameLabel;


    @FXML
    private Label staffRoleLabel;
    
    @FXML
    private Button myScheduleButton;

    @FXML
    private Button bookingViewButton;


    @FXML
    private Button invoiceViewButton;

    @FXML
    private Button promotionButton;

    @FXML
    private Button editProfileButton;

    @FXML
    private Button logoutButton;
    
    @FXML
    private ListView<WorkSchedule> todayScheduleListView;


    private final ScheduleService scheduleService = new ScheduleService();


    private Staff currentStaff;
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private ListView<String> todayAppointmentListView;

    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new StaffService();

        // Lấy thông tin nhân viên hiện tại từ Session
        currentStaff = Session.getCurrentStaff();

        // Hiển thị thông tin nhân viên trên giao diện
        if (currentStaff != null) {
            staffNameLabel.setText(currentStaff.getFullName());
            staffRoleLabel.setText(currentStaff.getRole().getRoleName());
        }
        
        // Cập nhật tên giao diện
        welcomeLabel.setText("Chào mừng " + (currentStaff != null ? currentStaff.getFullName() : "Nhân viên") + 
                             ", " + (currentStaff != null && currentStaff.getRole() != null ? 
                                    currentStaff.getRole().getRoleName() : "Nhân viên"));
        
        // Kiểm tra quyền và hiển thị/ẩn các nút tương ứng
        setupButtonVisibility();
        
        // Configure role-based default button focus
        setupRoleBasedFocus();

        todayScheduleListView.setCellFactory(lv -> new ListCell<WorkSchedule>() {
            @Override
            protected void updateItem(WorkSchedule item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getShift() + (item.getWorkDate()==null ? "": " - " + item.getWorkDate()));
                }
            }
        });

        loadTodaySchedule();
        loadTodayAppointments();
    }
    
    /**
     * Configure role-based button focus and styling
     */
    private void setupRoleBasedFocus() {
        if (currentStaff == null || currentStaff.getRole() == null) return;
        
        String roleName = currentStaff.getRole().getRoleName().toUpperCase();
        

//         switch (roleName) {
//             case "STAFF_CARE":
//                 bookingViewButton.setStyle(bookingViewButton.getStyle() + "; -fx-background-color: #d88e3f; -fx-text-fill: white;");
//                 break;
//             case "STAFF_CASHIER":
//                 invoiceViewButton.setStyle(invoiceViewButton.getStyle() + "; -fx-background-color: #d88e3f; -fx-text-fill: white;");
//                 break;
//             case "STAFF_RECEPTION":
//                 myScheduleButton.setStyle(myScheduleButton.getStyle() + "; -fx-background-color: #d88e3f; -fx-text-fill: white;");
//                 break;
//             default:
//                 // No specific highlighting for other roles
//                 break;
//         }
//     }
    
//     private void loadTodaySchedule() {
//         currentStaff = Session.getCurrentStaff(); // Kiểm tra lại thông tin nhân viên từ session

//         LocalDate today = LocalDate.now();
//         List<WorkSchedule> schedules = scheduleService.getSchedulesByStaffAndDate(currentStaff.getId(), today);

//         // Log để kiểm tra dữ liệu
//         if (schedules != null && !schedules.isEmpty()) {
//             System.out.println("Dữ liệu lịch làm việc: ");
//             for (WorkSchedule schedule : schedules) {
//                 System.out.println(schedule); // In ra thông tin của mỗi lịch làm việc
//             }
//             // Đổ vào ListView nếu có lịch
//             todayScheduleListView.getItems().setAll(schedules);
//         } else {
//             System.out.println("Không có lịch làm việc cho hôm nay.");
            
//             // Tạo một đối tượng Schedule đặc biệt với giá trị "NO_SCHEDULE"
//             WorkSchedule noSchedule = new WorkSchedule();
//             noSchedule.setShift(Shift.NOSHIFT); // Sử dụng giá trị đặc biệt để phân biệt

//             // Đổ vào ListView với thông báo này
//             todayScheduleListView.getItems().setAll(noSchedule);
//         }
//     }

//     private void loadTodayAppointments() {
//         ObservableList<String> appointments = FXCollections.observableArrayList();
//         BookingService bookingService = new BookingService();

//         try {
//             currentStaff = Session.getCurrentStaff(); // Đảm bảo có thông tin nhân viên
//             if (currentStaff == null) {
//                 appointments.add("Không thể xác định nhân viên.");
//                 todayAppointmentListView.setItems(appointments);
//                 return;
//             }

//             List<Booking> bookings = bookingService.getBookingsByStaffId(currentStaff.getId());
//             LocalDate today = LocalDate.now();

//             for (Booking booking : bookings) {
//                 if (booking.getBookingTime().toLocalDate().equals(today)) {
//                     Pet pet = booking.getPet();
//                     Customer customer = booking.getCustomer();

//                     String time = booking.getBookingTime().toLocalTime().toString();
//                     String petName = (pet != null) ? pet.getName() : "Không rõ";
//                     String customerName = (customer != null) ? customer.getFullName() : "Không rõ";

//                     String displayText = time + " - " + petName + " (" + customerName + ")";
//                     appointments.add(displayText);
//                 }
//             }

//             if (appointments.isEmpty()) {
//                 appointments.add("Không có lịch hẹn nào hôm nay.");
//             }

//             todayAppointmentListView.setItems(appointments);

//         } catch (Exception e) {
//             appointments.clear();
//             appointments.add("Lỗi khi tải lịch hẹn hôm nay: " + e.getMessage());
//             todayAppointmentListView.setItems(appointments);
//             e.printStackTrace();
//         }
//     }

//     private void setupButtonVisibility() {
//         // Các nút mặc định hiển thị cho tất cả nhân viên
//         editProfileButton.setVisible(true);

//         // Kiểm tra vai trò và hiển thị/ẩn các nút tương ứng
//         if (currentStaff != null && currentStaff.getRole() != null) {
//             String roleName = currentStaff.getRole().getRoleName().toUpperCase();
            
//             switch (roleName) {
//                 case "STAFF_CARE":
//                     myScheduleButton.setVisible(true);
//                     bookingViewButton.setVisible(true);
//                     invoiceViewButton.setVisible(false);
//                     promotionButton.setVisible(false);
//                     break;
//                 case "STAFF_CASHIER":
//                     myScheduleButton.setVisible(true);
//                     bookingViewButton.setVisible(false);
//                     invoiceViewButton.setVisible(true);
//                     promotionButton.setVisible(true);
//                     break;
//                 case "STAFF_RECEPTION":
//                     myScheduleButton.setVisible(true);
//                     bookingViewButton.setVisible(true);
//                     invoiceViewButton.setVisible(true);
//                     promotionButton.setVisible(false);
//                     break;
//                 default:
//                     // Sử dụng kiểm tra quyền dự phòng cho các vai trò khác
//                     myScheduleButton.setVisible(RoleChecker.hasPermission("VIEW_SCHEDULE"));
//                     bookingViewButton.setVisible(
//                             RoleChecker.hasPermission("VIEW_BOOKING_ASSIGNED") || RoleChecker.hasPermission("CREATE_BOOKING"));
//                     invoiceViewButton.setVisible(
//                             RoleChecker.hasPermission("VIEW_INVOICE") || RoleChecker.hasPermission("MANAGE_PAYMENT"));
//                     promotionButton.setVisible(RoleChecker.hasPermission("APPLY_PROMOTION"));
//                     break;
//             }
//         } else {
//             // Fallback to permission-based visibility if role is not available
//             myScheduleButton.setVisible(RoleChecker.hasPermission("VIEW_SCHEDULE"));
//             bookingViewButton.setVisible(
//                     RoleChecker.hasPermission("VIEW_BOOKING_ASSIGNED") || RoleChecker.hasPermission("CREATE_BOOKING"));
//             invoiceViewButton.setVisible(
//                     RoleChecker.hasPermission("VIEW_INVOICE") || RoleChecker.hasPermission("MANAGE_PAYMENT"));
//             promotionButton.setVisible(RoleChecker.hasPermission("APPLY_PROMOTION"));
//         }
//     }

//     @FXML
//     private void showMySchedule(ActionEvent event) {
//         try {
//             SceneSwitcher.switchScene("staff/my_schedule.fxml");
//         } catch (Exception e) {
//             showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình lịch làm việc", e.getMessage());
//         }
//     }

//     @FXML
//     private void showBookingView(ActionEvent event) {
//         try {
//             SceneSwitcher.switchScene("staff/booking_view.fxml");
//         } catch (Exception e) {
//             showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình đặt lịch", e.getMessage());
//         }
//     }

//     @FXML
//     private void showInvoiceView(ActionEvent event) {
//         try {
//             SceneSwitcher.switchScene("staff/invoice_view.fxml");
//         } catch (Exception e) {
//             showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình hóa đơn", e.getMessage());
//         }
//     }

//     @FXML
//     private void showPromotionView(ActionEvent event) {
//         try {
//             SceneSwitcher.switchScene("staff/promotion_apply.fxml");
//         } catch (Exception e) {
//             showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình khuyến mãi", e.getMessage());
//         }
//     }

//     @FXML
//     private void showEditProfile(ActionEvent event) {
//         try {
//             SceneSwitcher.switchScene("staff/edit_profile.fxml");
//         } catch (Exception e) {
//             showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình chỉnh sửa hồ sơ", e.getMessage());
//         }
//     }

//     @FXML
//     private void logout(ActionEvent event) {
//         try {
//             // Xóa thông tin phiên đăng nhập
//             Session.clearSession();

//             // Chuyển về màn hình đăng nhập
//             Stage currentStage = (Stage) logoutButton.getScene().getWindow();
//             SceneSwitcher.switchToLoginScene(currentStage);

//         } catch (Exception e) {
//             showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể đăng xuất", e.getMessage());
//         }
//     }

//     private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
//         Alert alert = new Alert(alertType);
//         alert.setTitle(title);
//         alert.setHeaderText(header);
//         alert.setContentText(content);
//         alert.showAndWait();
//     }

		// Kiểm tra quyền và hiển thị/ẩn các nút tương ứng
		setupButtonVisibility();

		// Mặc định hiển thị màn hình chính của nhân viên
//        loadStaffHomeView();
		todayScheduleListView.setCellFactory(lv -> new ListCell<WorkSchedule>() {
		    @Override
		    protected void updateItem(WorkSchedule item, boolean empty) {
		        super.updateItem(item, empty);
		        if (empty || item == null) {
		            setText(null);
		        } else {
		            setText(item.getShift()  + (item.getWorkDate()==null ? "": "-" + item.getWorkDate()) ); // Hiển thị thông tin lịch
		        }
		    }
		});

		 loadTodaySchedule();
		 loadTodayAppointments();
	}
	
	
	private void loadTodaySchedule() {
	    currentStaff = Session.getCurrentStaff(); // Kiểm tra lại thông tin nhân viên từ session

	    LocalDate today = LocalDate.now();
	    List<WorkSchedule> schedules = scheduleService.getSchedulesByStaffAndDate(currentStaff.getId(), today);

	    // Log để kiểm tra dữ liệu
	    if (schedules != null && !schedules.isEmpty()) {
	        System.out.println("Dữ liệu lịch làm việc: ");
	        for (WorkSchedule schedule : schedules) {
	            System.out.println(schedule); // In ra thông tin của mỗi lịch làm việc
	        }
	        // Đổ vào ListView nếu có lịch
	        todayScheduleListView.getItems().setAll(schedules);
	    } else {
	        System.out.println("Không có lịch làm việc cho hôm nay.");
	        
	        // Tạo một đối tượng Schedule đặc biệt với giá trị "NO_SCHEDULE"
	        WorkSchedule noSchedule = new WorkSchedule();
	        noSchedule.setShift(Shift.NOSHIFT); // Sử dụng giá trị đặc biệt để phân biệt

	        // Đổ vào ListView với thông báo này
	        todayScheduleListView.getItems().setAll(noSchedule);
	    }
	}

	private void loadTodayAppointments() {
	    ObservableList<String> appointments = FXCollections.observableArrayList();
	    BookingService bookingService = new BookingService();

	    try {
	        currentStaff = Session.getCurrentStaff(); // Đảm bảo có thông tin nhân viên
	        if (currentStaff == null) {
	            appointments.add("Không thể xác định nhân viên.");
	            todayAppointmentListView.setItems(appointments);
	            return;
	        }

	        List<Booking> bookings = bookingService.getBookingsByStaffId(currentStaff.getId());
	        LocalDate today = LocalDate.now();

	        for (Booking booking : bookings) {
	            if (booking.getBookingTime().toLocalDate().equals(today)) {
	                Pet pet = booking.getPet();
	                Customer customer = booking.getCustomer();

	                String time = booking.getBookingTime().toLocalTime().toString();
	                String petName = (pet != null) ? pet.getName() : "Không rõ";
	                String customerName = (customer != null) ? customer.getFullName() : "Không rõ";

	                String displayText = time + " - " + petName + " (" + customerName + ")";
	                appointments.add(displayText);
	            }
	        }

	        if (appointments.isEmpty()) {
	            appointments.add("Không có lịch hẹn nào hôm nay.");
	        }

	        todayAppointmentListView.setItems(appointments);

	    } catch (Exception e) {
	        appointments.clear();
	        appointments.add("Lỗi khi tải lịch hẹn hôm nay: " + e.getMessage());
	        todayAppointmentListView.setItems(appointments);
	        e.printStackTrace();
	    }
	}




	private void setupButtonVisibility() {
		// Các nút mặc định hiển thị cho tất cả nhân viên
		editProfileButton.setVisible(true);

		// Kiểm tra quyền và hiển thị/ẩn các nút tương ứng
		myScheduleButton.setVisible(RoleChecker.hasPermission("VIEW_SCHEDULE"));
		bookingViewButton.setVisible(
				RoleChecker.hasPermission("VIEW_BOOKING_ASSIGNED") || RoleChecker.hasPermission("CREATE_BOOKING"));
		invoiceViewButton
				.setVisible(RoleChecker.hasPermission("VIEW_INVOICE") || RoleChecker.hasPermission("MANAGE_PAYMENT"));
		promotionButton.setVisible(RoleChecker.hasPermission("APPLY_PROMOTION"));
	}
//
//	private void loadStaffHomeView() {
//		try {
//			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/staff_home.fxml"));
//			AnchorPane view = loader.load();
//			mainContainer.setCenter(view);
//		} catch (IOException e) {
//			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình chính", e.getMessage());
//		}
//	}

	@FXML
	private void showMySchedule(ActionEvent event) {
		try {
			if (!RoleChecker.hasPermission("VIEW_SCHEDULE")) {
				showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có quyền",
						"Bạn không có quyền xem lịch làm việc.");
				return;
			}

			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/my_schedule.fxml"));
			Parent view = loader.load();
			mainContainer.setCenter(view);
		} catch (IOException e) {
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình lịch làm việc", e.getMessage());
		}
	}

	@FXML
	private void showBookingView(ActionEvent event) {
		try {
			if (!RoleChecker.hasPermission("VIEW_BOOKING_ASSIGNED") && !RoleChecker.hasPermission("CREATE_BOOKING")) {
				showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có quyền",
						"Bạn không có quyền xem danh sách đặt lịch.");
				return;
			}

			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/booking_view.fxml"));
			Parent view = loader.load();
			mainContainer.setCenter(view);
		} catch (IOException e) {
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình đặt lịch", e.getMessage());
		}
	}

	@FXML
	private void showInvoiceView(ActionEvent event) {
		try {
			if (!RoleChecker.hasPermission("VIEW_INVOICE") && !RoleChecker.hasPermission("MANAGE_PAYMENT")) {
				showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có quyền", "Bạn không có quyền xem hóa đơn.");
				return;
			}

			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/invoice_view.fxml"));
			Parent view = loader.load();
			mainContainer.setCenter(view);
		} catch (IOException e) {
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình hóa đơn", e.getMessage());
		}
	}

	@FXML
	private void showPromotionView(ActionEvent event) {
		try {
			if (!RoleChecker.hasPermission("APPLY_PROMOTION")) {
				showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có quyền",
						"Bạn không có quyền áp dụng khuyến mãi.");
				return;
			}

			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/promotion_apply.fxml"));
			Parent view = loader.load();
			mainContainer.setCenter(view);
		} catch (IOException e) {
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình khuyến mãi", e.getMessage());
		}
	}

	@FXML
	private void showEditProfile(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/edit_profile.fxml"));
			Parent view = loader.load();
			mainContainer.setCenter(view);
		} catch (IOException e) {
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình chỉnh sửa hồ sơ", e.getMessage());
		}
	}

	@FXML
	private void logout(ActionEvent event) {
		try {
			// Xóa thông tin phiên đăng nhập
			Session.clearSession();

			// Chuyển về màn hình đăng nhập
			Stage currentStage = (Stage) logoutButton.getScene().getWindow();
			SceneSwitcher.switchToLoginScene(currentStage);

		} catch (Exception e) {
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể đăng xuất", e.getMessage());
		}
	}

	private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}

}