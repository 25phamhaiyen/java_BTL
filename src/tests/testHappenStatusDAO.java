//package tests;
//
//import java.util.List;
//
//
//import enums.StatusEnum;
//import model.HappenStatus;
//import repository.HappenStatusRepository;
//
//public class testHappenStatusDAO {
//    public static void main(String[] args) {
//        HappenStatusRepository happenStatusDAO = new HappenStatusRepository();
//
//        // 1. Thêm mới HappenStatus với kiểm tra trùng lặp
//        System.out.println("INSERT");
//        StatusEnum newStatusCode = StatusEnum.COMPLETED;
//
//        try {
//            // Kiểm tra xem status code đã tồn tại chưa
//            List<HappenStatus> existing = happenStatusDAO.selectByCondition(
//                "UN_StatusCode = ?",
//                newStatusCode.ordinal()
//            );
//
//            if (!existing.isEmpty()) {
//                System.out.println("Status code " + newStatusCode + " đã tồn tại. Sử dụng bản ghi hiện có.");
//                HappenStatus newStatus  = existing.get(0);
//            } else {
//                HappenStatus newStatus = new HappenStatus(
//                    0, // ID tự động tăng
//                    newStatusCode,
//                    "Đang xử lý"
//                );
//
//                int insertResult = happenStatusDAO.insert(newStatus);
//                System.out.println("Insert Result: " + insertResult + " row(s) affected");
//                System.out.println("Inserted Status ID: " + newStatus.getHappenStatusID());
//            }
//        } catch (Exception e) {
//            System.err.println("Lỗi khi thêm mới: " + e.getMessage());
//        }
//        System.out.println("\n");
//
//        // 2. Lấy danh sách tất cả HappenStatus
//        System.out.println("SELECT ALL");
//        List<HappenStatus> statusList = happenStatusDAO.selectAll();
//        System.out.println("Total statuses: " + statusList.size());
//        for (HappenStatus status : statusList) {
//            System.out.println(status);
//        }
//        System.out.println("\n");
//
//        // 3. Cập nhật HappenStatus với kiểm tra trùng lặp
//        if (!statusList.isEmpty()) {
//            System.out.println("UPDATE");
//            HappenStatus updateStatus = statusList.get(0);
//            System.out.println("Before update:");
//            System.out.println(updateStatus);
//
//            StatusEnum newCode = StatusEnum.COMPLETED;
//
//            try {
//                // Kiểm tra xem status code mới đã tồn tại chưa (trừ bản ghi hiện tại)
//                List<HappenStatus> checkDup = happenStatusDAO.selectByCondition(
//                    "UN_StatusCode = ? AND HappenStatusID != ?",
//                    newCode.ordinal(),
//                    updateStatus.getHappenStatusID()
//                );
//
//                if (!checkDup.isEmpty()) {
//                    System.out.println("Không thể cập nhật - Status code " + newCode + " đã tồn tại");
//                } else {
//                    updateStatus.setStatusName("Đang xử lý (đã cập nhật)");
//                    updateStatus.setStatusCode(newCode);
//
//                    int updateResult = happenStatusDAO.update(updateStatus);
//                    System.out.println("Update Result: " + updateResult + " row(s) affected");
//
//                    // In ra thông tin sau khi cập nhật
//                    HappenStatus updatedStatus = happenStatusDAO.selectById(updateStatus.getHappenStatusID());
//                    System.out.println("After update:");
//                    System.out.println(updatedStatus);
//                }
//            } catch (Exception e) {
//                System.err.println("Lỗi khi cập nhật: " + e.getMessage());
//            }
//            System.out.println("\n");
//        }
//
//        // 4. Tìm HappenStatus theo ID
//        if (!statusList.isEmpty()) {
//            System.out.println("SELECT BY ID");
//            int testId = statusList.get(0).getHappenStatusID();
//            HappenStatus foundStatus = happenStatusDAO.selectById(testId);
//            System.out.println("Status found by ID " + testId + ":");
//            System.out.println(foundStatus);
//            System.out.println("\n");
//        }
//
//        // 5. Tìm HappenStatus theo điều kiện
//        System.out.println("SELECT BY CONDITION");
//        StatusEnum searchCode = StatusEnum.COMPLETED;
//        List<HappenStatus> filteredStatuses = happenStatusDAO.selectByCondition(
//            "UN_StatusCode = ?",
//            searchCode.ordinal()
//        );
//        System.out.println("Found " + filteredStatuses.size() + " status(es) with " + searchCode + " status:");
//        for (HappenStatus status : filteredStatuses) {
//            System.out.println(status);
//        }
//        System.out.println("\n");
//
//        // 6. Xóa HappenStatus với kiểm tra trước khi tạo bản ghi tạm
//        System.out.println("DELETE");
//        StatusEnum tempCode = StatusEnum.PENDING;
//
//        try {
//            // Kiểm tra xem status code tạm đã tồn tại chưa
//            List<HappenStatus> checkTemp = happenStatusDAO.selectByCondition(
//                "UN_StatusCode = ?",
//                tempCode.ordinal()
//            );
//
//            HappenStatus tempStatus;
//            if (!checkTemp.isEmpty()) {
//                System.out.println("Sử dụng bản ghi có sẵn để test xóa");
//                tempStatus = checkTemp.get(0);
//            } else {
//                tempStatus = new HappenStatus();
//                tempStatus.setStatusCode(tempCode);
//                tempStatus.setStatusName("Tạm để xóa");
//                happenStatusDAO.insert(tempStatus);
//                System.out.println("Đã tạo bản ghi tạm với ID: " + tempStatus.getHappenStatusID());
//            }
//
//            int deleteResult = happenStatusDAO.delete(tempStatus);
//            System.out.println("Delete Result: " + deleteResult + " row(s) affected");
//
//            // Kiểm tra lại sau khi xóa
//            HappenStatus deletedStatus = happenStatusDAO.selectById(tempStatus.getHappenStatusID());
//            System.out.println("Status after deletion: " + (deletedStatus == null ? "Not found (deleted successfully)" : "Still exists"));
//        } catch (Exception e) {
//            System.err.println("Lỗi trong quá trình xóa: " + e.getMessage());
//        }
//        System.out.println("\n");
//    }
//}
//package repository;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.sql.Types;
//import java.util.ArrayList;
//import java.util.List;
//
//import enums.GenderEnum;
//import enums.StatusEnum;
//import javafx.scene.chart.XYChart;
//import model.Booking;
//import model.Customer;
//import model.Pet;
//import model.PetType;
//import model.Staff;
//import utils.DBUtil;
//import utils.DatabaseConnection;
//
//public class BookingRepository implements IRepository<Booking> {
//
//	private static BookingRepository instance;
//
//	public static BookingRepository getInstance() {
//		if (instance == null) {
//			instance = new BookingRepository();
//		}
//		return instance;
//	}
//
//	@Override
//	public int insert(Booking t) {
//		int ketQua = 0;
//		String sql = "INSERT INTO booking (customer_id, pet_id, staff_id, booking_time, status, note) VALUES (?, ?, ?, ?, ?, ?)";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getCustomer().getId());
//			pstmt.setInt(2, t.getPet().getPetId());
//			pstmt.setInt(3, t.getStaff() != null ? t.getStaff().getId() : Types.NULL); // Cho phép NULL
//			pstmt.setTimestamp(4, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(5, t.getStatus().name());
//			pstmt.setString(6, t.getNote());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("INSERT thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi thêm booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int update(Booking t) {
//		int ketQua = 0;
//		String sql = "UPDATE booking SET staff_id = ?, booking_time = ?, status = ?, note = ? WHERE booking_id = ?";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getStaff() != null ? t.getStaff().getId() : Types.NULL);
//			pstmt.setTimestamp(2, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(3, t.getStatus().name());
//			pstmt.setString(4, t.getNote());
//			pstmt.setInt(5, t.getBookingId());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("UPDATE thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi cập nhật booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int delete(Booking t) {
//		int ketQua = 0;
//		String sql = "DELETE FROM booking WHERE booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, t.getBookingId());
//			ketQua = pstmt.executeUpdate();
//			System.out.println("DELETE thành công, " + ketQua + " dòng bị xóa.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi xóa booking: " + e.getMessage());
//		}
//		return ketQua;
//	}
//
//	@Override
//	public List<Booking> selectAll() {
//		List<Booking> list = new ArrayList<>();
//		String sql = "SELECT * FROM booking";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//			rs = pstmt.executeQuery();
//
//			CustomerRepository customerRepo = new CustomerRepository();
//			PetRepository petRepo = new PetRepository();
//			StaffRepository staffRepo = new StaffRepository();
//
//			while (rs.next()) {
//				Customer customer = customerRepo.selectById(rs.getInt("customer_id"));
//				Pet pet = petRepo.selectById(rs.getInt("pet_id"));
//				Staff staff = rs.getObject("staff_id") != null ? staffRepo.selectById(rs.getInt("staff_id")) : null;
//
//				Booking booking = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//						rs.getTimestamp("booking_time").toLocalDateTime(), StatusEnum.valueOf(rs.getString("status")),
//						rs.getString("note"));
//
//				list.add(booking);
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi lấy danh sách booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt, rs);
//		}
//
//		return list;
//	}
//
////    public Booking selectById(int bookingID) {
////        Booking ketQua = null;
////        String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////                + "WHERE b.booking_id = ?";
////
////        try (Connection con = DatabaseConnection.getConnection();
////             PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////            pstmt.setInt(1, bookingID);
////            try (ResultSet rs = pstmt.executeQuery()) {
////                if (rs.next()) {
////                    Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    ketQua = new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    );
////                }
////            }
////
////        } catch (SQLException e) {
////            System.err.println("Lỗi khi tìm booking: " + e.getMessage());
////        }
////
////        return ketQua;
////    }
//	public Booking selectById(int bookingID) {
//		Booking ketQua = null;
//		String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//				+ "c.customer_id, c.point, c.created_at, "
//				+ "p.full_name AS customer_name, p.phone AS customer_phone, p.email AS customer_email, "
//				+ "p.gender AS customer_gender, " + "p.address AS customer_address, "
//				+ "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
//				+ "s.staff_id, s.name AS staff_name, s.phone AS staff_phone " + "FROM booking b "
//				+ "JOIN customer c ON b.customer_id = c.customer_id " + "JOIN pet p ON b.pet_id = p.pet_id "
//				+ "LEFT JOIN staff s ON b.staff_id = s.staff_id " + "WHERE b.booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, bookingID);
//			try (ResultSet rs = pstmt.executeQuery()) {
//				if (rs.next()) {
//					Customer customer = new Customer(rs.getInt("customer_id"), rs.getString("customer_name"),
//							GenderEnum.valueOf(rs.getString("customer_gender")), rs.getString("customer_phone"),
//							rs.getString("customer_address"), rs.getString("customer_email"), rs.getInt("point"),
//							rs.getTimestamp("created_at"));
//					Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
//					Staff staff = rs.getObject("staff_id") != null
//							? new StaffRepository().selectById(rs.getInt("staff_id"))
//							: null;
//
//					ketQua = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//							rs.getTimestamp("booking_time").toLocalDateTime(),
//							StatusEnum.valueOf(rs.getString("status")), rs.getString("note"));
//				}
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi tìm booking: " + e.getMessage());
//		}
//
//		return ketQua;
//	}
//
//	@Override
//	public Booking selectById(Booking t) {
//		return selectById(t.getBookingId());
//	}
////    
////    @Override
////    public List<Booking> selectByCondition(String condition, Object... params) {
////    	List<Booking> bookings = new ArrayList<>();
////
////	    if (condition == null || condition.trim().isEmpty()) {
////	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
////	    }
////
////	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////	            + "WHERE " + condition;
////
////	    try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////	        for (int i = 0; i < params.length; i++) {
////	            pstmt.setObject(i + 1, params[i]);
////	        }
////
////	        try (ResultSet rs = pstmt.executeQuery()) {
////	            while (rs.next()) {
////	            	Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    bookings.add( new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    ));
////	            }
////	        }
////	    } catch (SQLException e) {
////	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
////	    }
////	    return bookings;
////    }
//	@Override
//	public List<Booking> selectByCondition(String condition, Object... params) {
//	    List<Booking> bookings = new ArrayList<>();
//
//	    if (condition == null || condition.trim().isEmpty()) {
//	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
//	    }
//
//	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//	               + "c.customer_id, c.point, c.created_at, "
//	               + "p.pet_id, p.name AS pet_name, p.pet_gender AS pet_gender, p.dob AS pet_dob, p.weight AS pet_weight, p.note AS pet_note, "
//	               + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
//	               + "FROM booking b "
//	               + "JOIN customer c ON b.customer_id = c.customer_id "
//	               + "JOIN pet p ON b.pet_id = p.pet_id "
//	               + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
//	               + "WHERE " + condition;
//
//	    try (Connection con = DatabaseConnection.getConnection(); 
//	         PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//	        for (int i = 0; i < params.length; i++) {
//	            pstmt.setObject(i + 1, params[i]);
//	        }
//
//	        try (ResultSet rs = pstmt.executeQuery()) {
//	            while (rs.next()) {
//	                // Lấy thông tin từ customer
//	                Customer customer = new Customer(
//	                    rs.getInt("customer_id"),
//	                    rs.getString("customer_name"),
//	                    GenderEnum.valueOf(rs.getString("customer_gender")), // Nếu có trường gender
//	                    rs.getString("customer_phone"),
//	                    rs.getString("customer_address"),
//	                    rs.getString("customer_email"),
//	                    rs.getInt("point"),
//	                    rs.getTimestamp("created_at")
//	                );
//
//	                // Lấy thông tin từ pet
//	                Pet pet = new Pet(
//	                    rs.getInt("pet_id"),
//	                    rs.getString("pet_name"),
//	                    PetType.valueOf(rs.getString("pet_species")), // Giả sử species là loại pet
//	                    GenderEnum.valueOf(rs.getString("pet_gender")), // Sử dụng giá trị từ pet_gender
//	                    rs.getDate("pet_dob").toLocalDate(),
//	                    rs.getDouble("pet_weight"),
//	                    rs.getString("pet_note"),
//	                    customer // Gán owner là customer
//	                );
//
//	                // Lấy thông tin từ staff (nếu có)
//	                Staff staff = rs.getObject("staff_id") != null 
//	                    ? new StaffRepository().selectById(rs.getInt("staff_id"))
//	                    : null;
//
//	                // Tạo đối tượng Booking và thêm vào danh sách
//	                bookings.add(new Booking(
//	                    rs.getInt("booking_id"),
//	                    customer,
//	                    pet,
//	                    staff,
//	                    rs.getTimestamp("booking_time").toLocalDateTime(),
//	                    StatusEnum.valueOf(rs.getString("status")),
//	                    rs.getString("note")
//	                ));
//	            }
//	        }
//	    } catch (SQLException e) {
//	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
//	    }
//	    return bookings;
//	}
//
//	public int getMonthlyBookings() {
//		String query = "SELECT COUNT(*) AS total_bookings " + "FROM booking "
//				+ "WHERE MONTH(booking_time) = MONTH(CURRENT_DATE) " + "AND YEAR(booking_time) = YEAR(CURRENT_DATE)";
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			if (resultSet.next()) {
//				return resultSet.getInt("total_bookings");
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return 0; // Return 0 if no data is found or an error occurs
//	}
//
//	public XYChart.Series<String, Number> getBookingData(String timeUnit) {
//		String query = "";
//		if (timeUnit.equals("WEEK")) {
//			query = "SELECT WEEK(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY WEEK(booking_time)";
//		} else if (timeUnit.equals("MONTH")) {
//			query = "SELECT MONTH(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY MONTH(booking_time)";
//		} else if (timeUnit.equals("YEAR")) {
//			query = "SELECT YEAR(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY YEAR(booking_time)";
//		}
//
//		XYChart.Series<String, Number> series = new XYChart.Series<>();
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			while (resultSet.next()) {
//				String time = resultSet.getString("time");
//				int totalBookings = resultSet.getInt("total_bookings");
//				series.getData().add(new XYChart.Data<>(time, totalBookings));
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return series;
//	}
//}
//package repository;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.sql.Types;
//import java.util.ArrayList;
//import java.util.List;
//
//import enums.GenderEnum;
//import enums.StatusEnum;
//import javafx.scene.chart.XYChart;
//import model.Booking;
//import model.Customer;
//import model.Pet;
//import model.PetType;
//import model.Staff;
//import utils.DBUtil;
//import utils.DatabaseConnection;
//
//public class BookingRepository implements IRepository<Booking> {
//
//	private static BookingRepository instance;
//
//	public static BookingRepository getInstance() {
//		if (instance == null) {
//			instance = new BookingRepository();
//		}
//		return instance;
//	}
//
//	@Override
//	public int insert(Booking t) {
//		int ketQua = 0;
//		String sql = "INSERT INTO booking (customer_id, pet_id, staff_id, booking_time, status, note) VALUES (?, ?, ?, ?, ?, ?)";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getCustomer().getId());
//			pstmt.setInt(2, t.getPet().getPetId());
//			pstmt.setInt(3, t.getStaff() != null ? t.getStaff().getId() : Types.NULL); // Cho phép NULL
//			pstmt.setTimestamp(4, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(5, t.getStatus().name());
//			pstmt.setString(6, t.getNote());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("INSERT thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi thêm booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int update(Booking t) {
//		int ketQua = 0;
//		String sql = "UPDATE booking SET staff_id = ?, booking_time = ?, status = ?, note = ? WHERE booking_id = ?";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getStaff() != null ? t.getStaff().getId() : Types.NULL);
//			pstmt.setTimestamp(2, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(3, t.getStatus().name());
//			pstmt.setString(4, t.getNote());
//			pstmt.setInt(5, t.getBookingId());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("UPDATE thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi cập nhật booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int delete(Booking t) {
//		int ketQua = 0;
//		String sql = "DELETE FROM booking WHERE booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, t.getBookingId());
//			ketQua = pstmt.executeUpdate();
//			System.out.println("DELETE thành công, " + ketQua + " dòng bị xóa.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi xóa booking: " + e.getMessage());
//		}
//		return ketQua;
//	}
//
//	@Override
//	public List<Booking> selectAll() {
//		List<Booking> list = new ArrayList<>();
//		String sql = "SELECT * FROM booking";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//			rs = pstmt.executeQuery();
//
//			CustomerRepository customerRepo = new CustomerRepository();
//			PetRepository petRepo = new PetRepository();
//			StaffRepository staffRepo = new StaffRepository();
//
//			while (rs.next()) {
//				Customer customer = customerRepo.selectById(rs.getInt("customer_id"));
//				Pet pet = petRepo.selectById(rs.getInt("pet_id"));
//				Staff staff = rs.getObject("staff_id") != null ? staffRepo.selectById(rs.getInt("staff_id")) : null;
//
//				Booking booking = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//						rs.getTimestamp("booking_time").toLocalDateTime(), StatusEnum.valueOf(rs.getString("status")),
//						rs.getString("note"));
//
//				list.add(booking);
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi lấy danh sách booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt, rs);
//		}
//
//		return list;
//	}
//
////    public Booking selectById(int bookingID) {
////        Booking ketQua = null;
////        String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////                + "WHERE b.booking_id = ?";
////
////        try (Connection con = DatabaseConnection.getConnection();
////             PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////            pstmt.setInt(1, bookingID);
////            try (ResultSet rs = pstmt.executeQuery()) {
////                if (rs.next()) {
////                    Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    ketQua = new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    );
////                }
////            }
////
////        } catch (SQLException e) {
////            System.err.println("Lỗi khi tìm booking: " + e.getMessage());
////        }
////
////        return ketQua;
////    }
//	public Booking selectById(int bookingID) {
//		Booking ketQua = null;
//		String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//				+ "c.customer_id, c.point, c.created_at, "
//				+ "p.full_name AS customer_name, p.phone AS customer_phone, p.email AS customer_email, "
//				+ "p.gender AS customer_gender, " + "p.address AS customer_address, "
//				+ "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
//				+ "s.staff_id, s.name AS staff_name, s.phone AS staff_phone " + "FROM booking b "
//				+ "JOIN customer c ON b.customer_id = c.customer_id " + "JOIN pet p ON b.pet_id = p.pet_id "
//				+ "LEFT JOIN staff s ON b.staff_id = s.staff_id " + "WHERE b.booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, bookingID);
//			try (ResultSet rs = pstmt.executeQuery()) {
//				if (rs.next()) {
//					Customer customer = new Customer(rs.getInt("customer_id"), rs.getString("customer_name"),
//							GenderEnum.valueOf(rs.getString("customer_gender")), rs.getString("customer_phone"),
//							rs.getString("customer_address"), rs.getString("customer_email"), rs.getInt("point"),
//							rs.getTimestamp("created_at"));
//					Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
//					Staff staff = rs.getObject("staff_id") != null
//							? new StaffRepository().selectById(rs.getInt("staff_id"))
//							: null;
//
//					ketQua = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//							rs.getTimestamp("booking_time").toLocalDateTime(),
//							StatusEnum.valueOf(rs.getString("status")), rs.getString("note"));
//				}
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi tìm booking: " + e.getMessage());
//		}
//
//		return ketQua;
//	}
//
//	@Override
//	public Booking selectById(Booking t) {
//		return selectById(t.getBookingId());
//	}
////    
////    @Override
////    public List<Booking> selectByCondition(String condition, Object... params) {
////    	List<Booking> bookings = new ArrayList<>();
////
////	    if (condition == null || condition.trim().isEmpty()) {
////	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
////	    }
////
////	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////	            + "WHERE " + condition;
////
////	    try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////	        for (int i = 0; i < params.length; i++) {
////	            pstmt.setObject(i + 1, params[i]);
////	        }
////
////	        try (ResultSet rs = pstmt.executeQuery()) {
////	            while (rs.next()) {
////	            	Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    bookings.add( new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    ));
////	            }
////	        }
////	    } catch (SQLException e) {
////	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
////	    }
////	    return bookings;
////    }
//	@Override
//	public List<Booking> selectByCondition(String condition, Object... params) {
//	    List<Booking> bookings = new ArrayList<>();
//
//	    if (condition == null || condition.trim().isEmpty()) {
//	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
//	    }
//
//	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//	               + "c.customer_id, c.point, c.created_at, "
//	               + "p.pet_id, p.name AS pet_name, p.pet_gender AS pet_gender, p.dob AS pet_dob, p.weight AS pet_weight, p.note AS pet_note, "
//	               + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
//	               + "FROM booking b "
//	               + "JOIN customer c ON b.customer_id = c.customer_id "
//	               + "JOIN pet p ON b.pet_id = p.pet_id "
//	               + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
//	               + "WHERE " + condition;
//
//	    try (Connection con = DatabaseConnection.getConnection(); 
//	         PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//	        for (int i = 0; i < params.length; i++) {
//	            pstmt.setObject(i + 1, params[i]);
//	        }
//
//	        try (ResultSet rs = pstmt.executeQuery()) {
//	            while (rs.next()) {
//	                // Lấy thông tin từ customer
//	                Customer customer = new Customer(
//	                    rs.getInt("customer_id"),
//	                    rs.getString("customer_name"),
//	                    GenderEnum.valueOf(rs.getString("customer_gender")), // Nếu có trường gender
//	                    rs.getString("customer_phone"),
//	                    rs.getString("customer_address"),
//	                    rs.getString("customer_email"),
//	                    rs.getInt("point"),
//	                    rs.getTimestamp("created_at")
//	                );
//
//	                // Lấy thông tin từ pet
//	                Pet pet = new Pet(
//	                    rs.getInt("pet_id"),
//	                    rs.getString("pet_name"),
//	                    PetType.valueOf(rs.getString("pet_species")), // Giả sử species là loại pet
//	                    GenderEnum.valueOf(rs.getString("pet_gender")), // Sử dụng giá trị từ pet_gender
//	                    rs.getDate("pet_dob").toLocalDate(),
//	                    rs.getDouble("pet_weight"),
//	                    rs.getString("pet_note"),
//	                    customer // Gán owner là customer
//	                );
//
//	                // Lấy thông tin từ staff (nếu có)
//	                Staff staff = rs.getObject("staff_id") != null 
//	                    ? new StaffRepository().selectById(rs.getInt("staff_id"))
//	                    : null;
//
//	                // Tạo đối tượng Booking và thêm vào danh sách
//	                bookings.add(new Booking(
//	                    rs.getInt("booking_id"),
//	                    customer,
//	                    pet,
//	                    staff,
//	                    rs.getTimestamp("booking_time").toLocalDateTime(),
//	                    StatusEnum.valueOf(rs.getString("status")),
//	                    rs.getString("note")
//	                ));
//	            }
//	        }
//	    } catch (SQLException e) {
//	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
//	    }
//	    return bookings;
//	}
//
//	public int getMonthlyBookings() {
//		String query = "SELECT COUNT(*) AS total_bookings " + "FROM booking "
//				+ "WHERE MONTH(booking_time) = MONTH(CURRENT_DATE) " + "AND YEAR(booking_time) = YEAR(CURRENT_DATE)";
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			if (resultSet.next()) {
//				return resultSet.getInt("total_bookings");
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return 0; // Return 0 if no data is found or an error occurs
//	}
//
//	public XYChart.Series<String, Number> getBookingData(String timeUnit) {
//		String query = "";
//		if (timeUnit.equals("WEEK")) {
//			query = "SELECT WEEK(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY WEEK(booking_time)";
//		} else if (timeUnit.equals("MONTH")) {
//			query = "SELECT MONTH(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY MONTH(booking_time)";
//		} else if (timeUnit.equals("YEAR")) {
//			query = "SELECT YEAR(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY YEAR(booking_time)";
//		}
//
//		XYChart.Series<String, Number> series = new XYChart.Series<>();
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			while (resultSet.next()) {
//				String time = resultSet.getString("time");
//				int totalBookings = resultSet.getInt("total_bookings");
//				series.getData().add(new XYChart.Data<>(time, totalBookings));
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return series;
//	}
//}
//package repository;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.sql.Types;
//import java.util.ArrayList;
//import java.util.List;
//
//import enums.GenderEnum;
//import enums.StatusEnum;
//import javafx.scene.chart.XYChart;
//import model.Booking;
//import model.Customer;
//import model.Pet;
//import model.PetType;
//import model.Staff;
//import utils.DBUtil;
//import utils.DatabaseConnection;
//
//public class BookingRepository implements IRepository<Booking> {
//
//	private static BookingRepository instance;
//
//	public static BookingRepository getInstance() {
//		if (instance == null) {
//			instance = new BookingRepository();
//		}
//		return instance;
//	}
//
//	@Override
//	public int insert(Booking t) {
//		int ketQua = 0;
//		String sql = "INSERT INTO booking (customer_id, pet_id, staff_id, booking_time, status, note) VALUES (?, ?, ?, ?, ?, ?)";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getCustomer().getId());
//			pstmt.setInt(2, t.getPet().getPetId());
//			pstmt.setInt(3, t.getStaff() != null ? t.getStaff().getId() : Types.NULL); // Cho phép NULL
//			pstmt.setTimestamp(4, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(5, t.getStatus().name());
//			pstmt.setString(6, t.getNote());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("INSERT thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi thêm booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int update(Booking t) {
//		int ketQua = 0;
//		String sql = "UPDATE booking SET staff_id = ?, booking_time = ?, status = ?, note = ? WHERE booking_id = ?";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getStaff() != null ? t.getStaff().getId() : Types.NULL);
//			pstmt.setTimestamp(2, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(3, t.getStatus().name());
//			pstmt.setString(4, t.getNote());
//			pstmt.setInt(5, t.getBookingId());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("UPDATE thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi cập nhật booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int delete(Booking t) {
//		int ketQua = 0;
//		String sql = "DELETE FROM booking WHERE booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, t.getBookingId());
//			ketQua = pstmt.executeUpdate();
//			System.out.println("DELETE thành công, " + ketQua + " dòng bị xóa.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi xóa booking: " + e.getMessage());
//		}
//		return ketQua;
//	}
//
//	@Override
//	public List<Booking> selectAll() {
//		List<Booking> list = new ArrayList<>();
//		String sql = "SELECT * FROM booking";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//			rs = pstmt.executeQuery();
//
//			CustomerRepository customerRepo = new CustomerRepository();
//			PetRepository petRepo = new PetRepository();
//			StaffRepository staffRepo = new StaffRepository();
//
//			while (rs.next()) {
//				Customer customer = customerRepo.selectById(rs.getInt("customer_id"));
//				Pet pet = petRepo.selectById(rs.getInt("pet_id"));
//				Staff staff = rs.getObject("staff_id") != null ? staffRepo.selectById(rs.getInt("staff_id")) : null;
//
//				Booking booking = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//						rs.getTimestamp("booking_time").toLocalDateTime(), StatusEnum.valueOf(rs.getString("status")),
//						rs.getString("note"));
//
//				list.add(booking);
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi lấy danh sách booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt, rs);
//		}
//
//		return list;
//	}
//
////    public Booking selectById(int bookingID) {
////        Booking ketQua = null;
////        String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////                + "WHERE b.booking_id = ?";
////
////        try (Connection con = DatabaseConnection.getConnection();
////             PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////            pstmt.setInt(1, bookingID);
////            try (ResultSet rs = pstmt.executeQuery()) {
////                if (rs.next()) {
////                    Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    ketQua = new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    );
////                }
////            }
////
////        } catch (SQLException e) {
////            System.err.println("Lỗi khi tìm booking: " + e.getMessage());
////        }
////
////        return ketQua;
////    }
//	public Booking selectById(int bookingID) {
//		Booking ketQua = null;
//		String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//				+ "c.customer_id, c.point, c.created_at, "
//				+ "p.full_name AS customer_name, p.phone AS customer_phone, p.email AS customer_email, "
//				+ "p.gender AS customer_gender, " + "p.address AS customer_address, "
//				+ "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
//				+ "s.staff_id, s.name AS staff_name, s.phone AS staff_phone " + "FROM booking b "
//				+ "JOIN customer c ON b.customer_id = c.customer_id " + "JOIN pet p ON b.pet_id = p.pet_id "
//				+ "LEFT JOIN staff s ON b.staff_id = s.staff_id " + "WHERE b.booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, bookingID);
//			try (ResultSet rs = pstmt.executeQuery()) {
//				if (rs.next()) {
//					Customer customer = new Customer(rs.getInt("customer_id"), rs.getString("customer_name"),
//							GenderEnum.valueOf(rs.getString("customer_gender")), rs.getString("customer_phone"),
//							rs.getString("customer_address"), rs.getString("customer_email"), rs.getInt("point"),
//							rs.getTimestamp("created_at"));
//					Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
//					Staff staff = rs.getObject("staff_id") != null
//							? new StaffRepository().selectById(rs.getInt("staff_id"))
//							: null;
//
//					ketQua = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//							rs.getTimestamp("booking_time").toLocalDateTime(),
//							StatusEnum.valueOf(rs.getString("status")), rs.getString("note"));
//				}
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi tìm booking: " + e.getMessage());
//		}
//
//		return ketQua;
//	}
//
//	@Override
//	public Booking selectById(Booking t) {
//		return selectById(t.getBookingId());
//	}
////    
////    @Override
////    public List<Booking> selectByCondition(String condition, Object... params) {
////    	List<Booking> bookings = new ArrayList<>();
////
////	    if (condition == null || condition.trim().isEmpty()) {
////	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
////	    }
////
////	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////	            + "WHERE " + condition;
////
////	    try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////	        for (int i = 0; i < params.length; i++) {
////	            pstmt.setObject(i + 1, params[i]);
////	        }
////
////	        try (ResultSet rs = pstmt.executeQuery()) {
////	            while (rs.next()) {
////	            	Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    bookings.add( new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    ));
////	            }
////	        }
////	    } catch (SQLException e) {
////	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
////	    }
////	    return bookings;
////    }
//	@Override
//	public List<Booking> selectByCondition(String condition, Object... params) {
//	    List<Booking> bookings = new ArrayList<>();
//
//	    if (condition == null || condition.trim().isEmpty()) {
//	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
//	    }
//
//	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//	               + "c.customer_id, c.point, c.created_at, "
//	               + "p.pet_id, p.name AS pet_name, p.pet_gender AS pet_gender, p.dob AS pet_dob, p.weight AS pet_weight, p.note AS pet_note, "
//	               + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
//	               + "FROM booking b "
//	               + "JOIN customer c ON b.customer_id = c.customer_id "
//	               + "JOIN pet p ON b.pet_id = p.pet_id "
//	               + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
//	               + "WHERE " + condition;
//
//	    try (Connection con = DatabaseConnection.getConnection(); 
//	         PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//	        for (int i = 0; i < params.length; i++) {
//	            pstmt.setObject(i + 1, params[i]);
//	        }
//
//	        try (ResultSet rs = pstmt.executeQuery()) {
//	            while (rs.next()) {
//	                // Lấy thông tin từ customer
//	                Customer customer = new Customer(
//	                    rs.getInt("customer_id"),
//	                    rs.getString("customer_name"),
//	                    GenderEnum.valueOf(rs.getString("customer_gender")), // Nếu có trường gender
//	                    rs.getString("customer_phone"),
//	                    rs.getString("customer_address"),
//	                    rs.getString("customer_email"),
//	                    rs.getInt("point"),
//	                    rs.getTimestamp("created_at")
//	                );
//
//	                // Lấy thông tin từ pet
//	                Pet pet = new Pet(
//	                    rs.getInt("pet_id"),
//	                    rs.getString("pet_name"),
//	                    PetType.valueOf(rs.getString("pet_species")), // Giả sử species là loại pet
//	                    GenderEnum.valueOf(rs.getString("pet_gender")), // Sử dụng giá trị từ pet_gender
//	                    rs.getDate("pet_dob").toLocalDate(),
//	                    rs.getDouble("pet_weight"),
//	                    rs.getString("pet_note"),
//	                    customer // Gán owner là customer
//	                );
//
//	                // Lấy thông tin từ staff (nếu có)
//	                Staff staff = rs.getObject("staff_id") != null 
//	                    ? new StaffRepository().selectById(rs.getInt("staff_id"))
//	                    : null;
//
//	                // Tạo đối tượng Booking và thêm vào danh sách
//	                bookings.add(new Booking(
//	                    rs.getInt("booking_id"),
//	                    customer,
//	                    pet,
//	                    staff,
//	                    rs.getTimestamp("booking_time").toLocalDateTime(),
//	                    StatusEnum.valueOf(rs.getString("status")),
//	                    rs.getString("note")
//	                ));
//	            }
//	        }
//	    } catch (SQLException e) {
//	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
//	    }
//	    return bookings;
//	}
//
//	public int getMonthlyBookings() {
//		String query = "SELECT COUNT(*) AS total_bookings " + "FROM booking "
//				+ "WHERE MONTH(booking_time) = MONTH(CURRENT_DATE) " + "AND YEAR(booking_time) = YEAR(CURRENT_DATE)";
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			if (resultSet.next()) {
//				return resultSet.getInt("total_bookings");
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return 0; // Return 0 if no data is found or an error occurs
//	}
//
//	public XYChart.Series<String, Number> getBookingData(String timeUnit) {
//		String query = "";
//		if (timeUnit.equals("WEEK")) {
//			query = "SELECT WEEK(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY WEEK(booking_time)";
//		} else if (timeUnit.equals("MONTH")) {
//			query = "SELECT MONTH(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY MONTH(booking_time)";
//		} else if (timeUnit.equals("YEAR")) {
//			query = "SELECT YEAR(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY YEAR(booking_time)";
//		}
//
//		XYChart.Series<String, Number> series = new XYChart.Series<>();
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			while (resultSet.next()) {
//				String time = resultSet.getString("time");
//				int totalBookings = resultSet.getInt("total_bookings");
//				series.getData().add(new XYChart.Data<>(time, totalBookings));
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return series;
//	}
//}
//package repository;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.sql.Types;
//import java.util.ArrayList;
//import java.util.List;
//
//import enums.GenderEnum;
//import enums.StatusEnum;
//import javafx.scene.chart.XYChart;
//import model.Booking;
//import model.Customer;
//import model.Pet;
//import model.PetType;
//import model.Staff;
//import utils.DBUtil;
//import utils.DatabaseConnection;
//
//public class BookingRepository implements IRepository<Booking> {
//
//	private static BookingRepository instance;
//
//	public static BookingRepository getInstance() {
//		if (instance == null) {
//			instance = new BookingRepository();
//		}
//		return instance;
//	}
//
//	@Override
//	public int insert(Booking t) {
//		int ketQua = 0;
//		String sql = "INSERT INTO booking (customer_id, pet_id, staff_id, booking_time, status, note) VALUES (?, ?, ?, ?, ?, ?)";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getCustomer().getId());
//			pstmt.setInt(2, t.getPet().getPetId());
//			pstmt.setInt(3, t.getStaff() != null ? t.getStaff().getId() : Types.NULL); // Cho phép NULL
//			pstmt.setTimestamp(4, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(5, t.getStatus().name());
//			pstmt.setString(6, t.getNote());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("INSERT thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi thêm booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int update(Booking t) {
//		int ketQua = 0;
//		String sql = "UPDATE booking SET staff_id = ?, booking_time = ?, status = ?, note = ? WHERE booking_id = ?";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getStaff() != null ? t.getStaff().getId() : Types.NULL);
//			pstmt.setTimestamp(2, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(3, t.getStatus().name());
//			pstmt.setString(4, t.getNote());
//			pstmt.setInt(5, t.getBookingId());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("UPDATE thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi cập nhật booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int delete(Booking t) {
//		int ketQua = 0;
//		String sql = "DELETE FROM booking WHERE booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, t.getBookingId());
//			ketQua = pstmt.executeUpdate();
//			System.out.println("DELETE thành công, " + ketQua + " dòng bị xóa.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi xóa booking: " + e.getMessage());
//		}
//		return ketQua;
//	}
//
//	@Override
//	public List<Booking> selectAll() {
//		List<Booking> list = new ArrayList<>();
//		String sql = "SELECT * FROM booking";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//			rs = pstmt.executeQuery();
//
//			CustomerRepository customerRepo = new CustomerRepository();
//			PetRepository petRepo = new PetRepository();
//			StaffRepository staffRepo = new StaffRepository();
//
//			while (rs.next()) {
//				Customer customer = customerRepo.selectById(rs.getInt("customer_id"));
//				Pet pet = petRepo.selectById(rs.getInt("pet_id"));
//				Staff staff = rs.getObject("staff_id") != null ? staffRepo.selectById(rs.getInt("staff_id")) : null;
//
//				Booking booking = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//						rs.getTimestamp("booking_time").toLocalDateTime(), StatusEnum.valueOf(rs.getString("status")),
//						rs.getString("note"));
//
//				list.add(booking);
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi lấy danh sách booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt, rs);
//		}
//
//		return list;
//	}
//
////    public Booking selectById(int bookingID) {
////        Booking ketQua = null;
////        String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////                + "WHERE b.booking_id = ?";
////
////        try (Connection con = DatabaseConnection.getConnection();
////             PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////            pstmt.setInt(1, bookingID);
////            try (ResultSet rs = pstmt.executeQuery()) {
////                if (rs.next()) {
////                    Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    ketQua = new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    );
////                }
////            }
////
////        } catch (SQLException e) {
////            System.err.println("Lỗi khi tìm booking: " + e.getMessage());
////        }
////
////        return ketQua;
////    }
//	public Booking selectById(int bookingID) {
//		Booking ketQua = null;
//		String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//				+ "c.customer_id, c.point, c.created_at, "
//				+ "p.full_name AS customer_name, p.phone AS customer_phone, p.email AS customer_email, "
//				+ "p.gender AS customer_gender, " + "p.address AS customer_address, "
//				+ "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
//				+ "s.staff_id, s.name AS staff_name, s.phone AS staff_phone " + "FROM booking b "
//				+ "JOIN customer c ON b.customer_id = c.customer_id " + "JOIN pet p ON b.pet_id = p.pet_id "
//				+ "LEFT JOIN staff s ON b.staff_id = s.staff_id " + "WHERE b.booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, bookingID);
//			try (ResultSet rs = pstmt.executeQuery()) {
//				if (rs.next()) {
//					Customer customer = new Customer(rs.getInt("customer_id"), rs.getString("customer_name"),
//							GenderEnum.valueOf(rs.getString("customer_gender")), rs.getString("customer_phone"),
//							rs.getString("customer_address"), rs.getString("customer_email"), rs.getInt("point"),
//							rs.getTimestamp("created_at"));
//					Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
//					Staff staff = rs.getObject("staff_id") != null
//							? new StaffRepository().selectById(rs.getInt("staff_id"))
//							: null;
//
//					ketQua = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//							rs.getTimestamp("booking_time").toLocalDateTime(),
//							StatusEnum.valueOf(rs.getString("status")), rs.getString("note"));
//				}
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi tìm booking: " + e.getMessage());
//		}
//
//		return ketQua;
//	}
//
//	@Override
//	public Booking selectById(Booking t) {
//		return selectById(t.getBookingId());
//	}
////    
////    @Override
////    public List<Booking> selectByCondition(String condition, Object... params) {
////    	List<Booking> bookings = new ArrayList<>();
////
////	    if (condition == null || condition.trim().isEmpty()) {
////	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
////	    }
////
////	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////	            + "WHERE " + condition;
////
////	    try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////	        for (int i = 0; i < params.length; i++) {
////	            pstmt.setObject(i + 1, params[i]);
////	        }
////
////	        try (ResultSet rs = pstmt.executeQuery()) {
////	            while (rs.next()) {
////	            	Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    bookings.add( new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    ));
////	            }
////	        }
////	    } catch (SQLException e) {
////	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
////	    }
////	    return bookings;
////    }
//	@Override
//	public List<Booking> selectByCondition(String condition, Object... params) {
//	    List<Booking> bookings = new ArrayList<>();
//
//	    if (condition == null || condition.trim().isEmpty()) {
//	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
//	    }
//
//	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//	               + "c.customer_id, c.point, c.created_at, "
//	               + "p.pet_id, p.name AS pet_name, p.pet_gender AS pet_gender, p.dob AS pet_dob, p.weight AS pet_weight, p.note AS pet_note, "
//	               + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
//	               + "FROM booking b "
//	               + "JOIN customer c ON b.customer_id = c.customer_id "
//	               + "JOIN pet p ON b.pet_id = p.pet_id "
//	               + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
//	               + "WHERE " + condition;
//
//	    try (Connection con = DatabaseConnection.getConnection(); 
//	         PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//	        for (int i = 0; i < params.length; i++) {
//	            pstmt.setObject(i + 1, params[i]);
//	        }
//
//	        try (ResultSet rs = pstmt.executeQuery()) {
//	            while (rs.next()) {
//	                // Lấy thông tin từ customer
//	                Customer customer = new Customer(
//	                    rs.getInt("customer_id"),
//	                    rs.getString("customer_name"),
//	                    GenderEnum.valueOf(rs.getString("customer_gender")), // Nếu có trường gender
//	                    rs.getString("customer_phone"),
//	                    rs.getString("customer_address"),
//	                    rs.getString("customer_email"),
//	                    rs.getInt("point"),
//	                    rs.getTimestamp("created_at")
//	                );
//
//	                // Lấy thông tin từ pet
//	                Pet pet = new Pet(
//	                    rs.getInt("pet_id"),
//	                    rs.getString("pet_name"),
//	                    PetType.valueOf(rs.getString("pet_species")), // Giả sử species là loại pet
//	                    GenderEnum.valueOf(rs.getString("pet_gender")), // Sử dụng giá trị từ pet_gender
//	                    rs.getDate("pet_dob").toLocalDate(),
//	                    rs.getDouble("pet_weight"),
//	                    rs.getString("pet_note"),
//	                    customer // Gán owner là customer
//	                );
//
//	                // Lấy thông tin từ staff (nếu có)
//	                Staff staff = rs.getObject("staff_id") != null 
//	                    ? new StaffRepository().selectById(rs.getInt("staff_id"))
//	                    : null;
//
//	                // Tạo đối tượng Booking và thêm vào danh sách
//	                bookings.add(new Booking(
//	                    rs.getInt("booking_id"),
//	                    customer,
//	                    pet,
//	                    staff,
//	                    rs.getTimestamp("booking_time").toLocalDateTime(),
//	                    StatusEnum.valueOf(rs.getString("status")),
//	                    rs.getString("note")
//	                ));
//	            }
//	        }
//	    } catch (SQLException e) {
//	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
//	    }
//	    return bookings;
//	}
//
//	public int getMonthlyBookings() {
//		String query = "SELECT COUNT(*) AS total_bookings " + "FROM booking "
//				+ "WHERE MONTH(booking_time) = MONTH(CURRENT_DATE) " + "AND YEAR(booking_time) = YEAR(CURRENT_DATE)";
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			if (resultSet.next()) {
//				return resultSet.getInt("total_bookings");
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return 0; // Return 0 if no data is found or an error occurs
//	}
//
//	public XYChart.Series<String, Number> getBookingData(String timeUnit) {
//		String query = "";
//		if (timeUnit.equals("WEEK")) {
//			query = "SELECT WEEK(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY WEEK(booking_time)";
//		} else if (timeUnit.equals("MONTH")) {
//			query = "SELECT MONTH(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY MONTH(booking_time)";
//		} else if (timeUnit.equals("YEAR")) {
//			query = "SELECT YEAR(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY YEAR(booking_time)";
//		}
//
//		XYChart.Series<String, Number> series = new XYChart.Series<>();
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			while (resultSet.next()) {
//				String time = resultSet.getString("time");
//				int totalBookings = resultSet.getInt("total_bookings");
//				series.getData().add(new XYChart.Data<>(time, totalBookings));
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return series;
//	}
//}
//package repository;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.sql.Types;
//import java.util.ArrayList;
//import java.util.List;
//
//import enums.GenderEnum;
//import enums.StatusEnum;
//import javafx.scene.chart.XYChart;
//import model.Booking;
//import model.Customer;
//import model.Pet;
//import model.PetType;
//import model.Staff;
//import utils.DBUtil;
//import utils.DatabaseConnection;
//
//public class BookingRepository implements IRepository<Booking> {
//
//	private static BookingRepository instance;
//
//	public static BookingRepository getInstance() {
//		if (instance == null) {
//			instance = new BookingRepository();
//		}
//		return instance;
//	}
//
//	@Override
//	public int insert(Booking t) {
//		int ketQua = 0;
//		String sql = "INSERT INTO booking (customer_id, pet_id, staff_id, booking_time, status, note) VALUES (?, ?, ?, ?, ?, ?)";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getCustomer().getId());
//			pstmt.setInt(2, t.getPet().getPetId());
//			pstmt.setInt(3, t.getStaff() != null ? t.getStaff().getId() : Types.NULL); // Cho phép NULL
//			pstmt.setTimestamp(4, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(5, t.getStatus().name());
//			pstmt.setString(6, t.getNote());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("INSERT thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi thêm booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int update(Booking t) {
//		int ketQua = 0;
//		String sql = "UPDATE booking SET staff_id = ?, booking_time = ?, status = ?, note = ? WHERE booking_id = ?";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getStaff() != null ? t.getStaff().getId() : Types.NULL);
//			pstmt.setTimestamp(2, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(3, t.getStatus().name());
//			pstmt.setString(4, t.getNote());
//			pstmt.setInt(5, t.getBookingId());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("UPDATE thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi cập nhật booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int delete(Booking t) {
//		int ketQua = 0;
//		String sql = "DELETE FROM booking WHERE booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, t.getBookingId());
//			ketQua = pstmt.executeUpdate();
//			System.out.println("DELETE thành công, " + ketQua + " dòng bị xóa.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi xóa booking: " + e.getMessage());
//		}
//		return ketQua;
//	}
//
//	@Override
//	public List<Booking> selectAll() {
//		List<Booking> list = new ArrayList<>();
//		String sql = "SELECT * FROM booking";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//			rs = pstmt.executeQuery();
//
//			CustomerRepository customerRepo = new CustomerRepository();
//			PetRepository petRepo = new PetRepository();
//			StaffRepository staffRepo = new StaffRepository();
//
//			while (rs.next()) {
//				Customer customer = customerRepo.selectById(rs.getInt("customer_id"));
//				Pet pet = petRepo.selectById(rs.getInt("pet_id"));
//				Staff staff = rs.getObject("staff_id") != null ? staffRepo.selectById(rs.getInt("staff_id")) : null;
//
//				Booking booking = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//						rs.getTimestamp("booking_time").toLocalDateTime(), StatusEnum.valueOf(rs.getString("status")),
//						rs.getString("note"));
//
//				list.add(booking);
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi lấy danh sách booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt, rs);
//		}
//
//		return list;
//	}
//
////    public Booking selectById(int bookingID) {
////        Booking ketQua = null;
////        String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////                + "WHERE b.booking_id = ?";
////
////        try (Connection con = DatabaseConnection.getConnection();
////             PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////            pstmt.setInt(1, bookingID);
////            try (ResultSet rs = pstmt.executeQuery()) {
////                if (rs.next()) {
////                    Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    ketQua = new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    );
////                }
////            }
////
////        } catch (SQLException e) {
////            System.err.println("Lỗi khi tìm booking: " + e.getMessage());
////        }
////
////        return ketQua;
////    }
//	public Booking selectById(int bookingID) {
//		Booking ketQua = null;
//		String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//				+ "c.customer_id, c.point, c.created_at, "
//				+ "p.full_name AS customer_name, p.phone AS customer_phone, p.email AS customer_email, "
//				+ "p.gender AS customer_gender, " + "p.address AS customer_address, "
//				+ "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
//				+ "s.staff_id, s.name AS staff_name, s.phone AS staff_phone " + "FROM booking b "
//				+ "JOIN customer c ON b.customer_id = c.customer_id " + "JOIN pet p ON b.pet_id = p.pet_id "
//				+ "LEFT JOIN staff s ON b.staff_id = s.staff_id " + "WHERE b.booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, bookingID);
//			try (ResultSet rs = pstmt.executeQuery()) {
//				if (rs.next()) {
//					Customer customer = new Customer(rs.getInt("customer_id"), rs.getString("customer_name"),
//							GenderEnum.valueOf(rs.getString("customer_gender")), rs.getString("customer_phone"),
//							rs.getString("customer_address"), rs.getString("customer_email"), rs.getInt("point"),
//							rs.getTimestamp("created_at"));
//					Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
//					Staff staff = rs.getObject("staff_id") != null
//							? new StaffRepository().selectById(rs.getInt("staff_id"))
//							: null;
//
//					ketQua = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//							rs.getTimestamp("booking_time").toLocalDateTime(),
//							StatusEnum.valueOf(rs.getString("status")), rs.getString("note"));
//				}
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi tìm booking: " + e.getMessage());
//		}
//
//		return ketQua;
//	}
//
//	@Override
//	public Booking selectById(Booking t) {
//		return selectById(t.getBookingId());
//	}
////    
////    @Override
////    public List<Booking> selectByCondition(String condition, Object... params) {
////    	List<Booking> bookings = new ArrayList<>();
////
////	    if (condition == null || condition.trim().isEmpty()) {
////	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
////	    }
////
////	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////	            + "WHERE " + condition;
////
////	    try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////	        for (int i = 0; i < params.length; i++) {
////	            pstmt.setObject(i + 1, params[i]);
////	        }
////
////	        try (ResultSet rs = pstmt.executeQuery()) {
////	            while (rs.next()) {
////	            	Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    bookings.add( new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    ));
////	            }
////	        }
////	    } catch (SQLException e) {
////	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
////	    }
////	    return bookings;
////    }
//	@Override
//	public List<Booking> selectByCondition(String condition, Object... params) {
//	    List<Booking> bookings = new ArrayList<>();
//
//	    if (condition == null || condition.trim().isEmpty()) {
//	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
//	    }
//
//	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//	               + "c.customer_id, c.point, c.created_at, "
//	               + "p.pet_id, p.name AS pet_name, p.pet_gender AS pet_gender, p.dob AS pet_dob, p.weight AS pet_weight, p.note AS pet_note, "
//	               + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
//	               + "FROM booking b "
//	               + "JOIN customer c ON b.customer_id = c.customer_id "
//	               + "JOIN pet p ON b.pet_id = p.pet_id "
//	               + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
//	               + "WHERE " + condition;
//
//	    try (Connection con = DatabaseConnection.getConnection(); 
//	         PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//	        for (int i = 0; i < params.length; i++) {
//	            pstmt.setObject(i + 1, params[i]);
//	        }
//
//	        try (ResultSet rs = pstmt.executeQuery()) {
//	            while (rs.next()) {
//	                // Lấy thông tin từ customer
//	                Customer customer = new Customer(
//	                    rs.getInt("customer_id"),
//	                    rs.getString("customer_name"),
//	                    GenderEnum.valueOf(rs.getString("customer_gender")), // Nếu có trường gender
//	                    rs.getString("customer_phone"),
//	                    rs.getString("customer_address"),
//	                    rs.getString("customer_email"),
//	                    rs.getInt("point"),
//	                    rs.getTimestamp("created_at")
//	                );
//
//	                // Lấy thông tin từ pet
//	                Pet pet = new Pet(
//	                    rs.getInt("pet_id"),
//	                    rs.getString("pet_name"),
//	                    PetType.valueOf(rs.getString("pet_species")), // Giả sử species là loại pet
//	                    GenderEnum.valueOf(rs.getString("pet_gender")), // Sử dụng giá trị từ pet_gender
//	                    rs.getDate("pet_dob").toLocalDate(),
//	                    rs.getDouble("pet_weight"),
//	                    rs.getString("pet_note"),
//	                    customer // Gán owner là customer
//	                );
//
//	                // Lấy thông tin từ staff (nếu có)
//	                Staff staff = rs.getObject("staff_id") != null 
//	                    ? new StaffRepository().selectById(rs.getInt("staff_id"))
//	                    : null;
//
//	                // Tạo đối tượng Booking và thêm vào danh sách
//	                bookings.add(new Booking(
//	                    rs.getInt("booking_id"),
//	                    customer,
//	                    pet,
//	                    staff,
//	                    rs.getTimestamp("booking_time").toLocalDateTime(),
//	                    StatusEnum.valueOf(rs.getString("status")),
//	                    rs.getString("note")
//	                ));
//	            }
//	        }
//	    } catch (SQLException e) {
//	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
//	    }
//	    return bookings;
//	}
//
//	public int getMonthlyBookings() {
//		String query = "SELECT COUNT(*) AS total_bookings " + "FROM booking "
//				+ "WHERE MONTH(booking_time) = MONTH(CURRENT_DATE) " + "AND YEAR(booking_time) = YEAR(CURRENT_DATE)";
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			if (resultSet.next()) {
//				return resultSet.getInt("total_bookings");
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return 0; // Return 0 if no data is found or an error occurs
//	}
//
//	public XYChart.Series<String, Number> getBookingData(String timeUnit) {
//		String query = "";
//		if (timeUnit.equals("WEEK")) {
//			query = "SELECT WEEK(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY WEEK(booking_time)";
//		} else if (timeUnit.equals("MONTH")) {
//			query = "SELECT MONTH(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY MONTH(booking_time)";
//		} else if (timeUnit.equals("YEAR")) {
//			query = "SELECT YEAR(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY YEAR(booking_time)";
//		}
//
//		XYChart.Series<String, Number> series = new XYChart.Series<>();
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			while (resultSet.next()) {
//				String time = resultSet.getString("time");
//				int totalBookings = resultSet.getInt("total_bookings");
//				series.getData().add(new XYChart.Data<>(time, totalBookings));
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return series;
//	}
//}
//package repository;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.sql.Types;
//import java.util.ArrayList;
//import java.util.List;
//
//import enums.GenderEnum;
//import enums.StatusEnum;
//import javafx.scene.chart.XYChart;
//import model.Booking;
//import model.Customer;
//import model.Pet;
//import model.PetType;
//import model.Staff;
//import utils.DBUtil;
//import utils.DatabaseConnection;
//
//public class BookingRepository implements IRepository<Booking> {
//
//	private static BookingRepository instance;
//
//	public static BookingRepository getInstance() {
//		if (instance == null) {
//			instance = new BookingRepository();
//		}
//		return instance;
//	}
//
//	@Override
//	public int insert(Booking t) {
//		int ketQua = 0;
//		String sql = "INSERT INTO booking (customer_id, pet_id, staff_id, booking_time, status, note) VALUES (?, ?, ?, ?, ?, ?)";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getCustomer().getId());
//			pstmt.setInt(2, t.getPet().getPetId());
//			pstmt.setInt(3, t.getStaff() != null ? t.getStaff().getId() : Types.NULL); // Cho phép NULL
//			pstmt.setTimestamp(4, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(5, t.getStatus().name());
//			pstmt.setString(6, t.getNote());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("INSERT thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi thêm booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int update(Booking t) {
//		int ketQua = 0;
//		String sql = "UPDATE booking SET staff_id = ?, booking_time = ?, status = ?, note = ? WHERE booking_id = ?";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getStaff() != null ? t.getStaff().getId() : Types.NULL);
//			pstmt.setTimestamp(2, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(3, t.getStatus().name());
//			pstmt.setString(4, t.getNote());
//			pstmt.setInt(5, t.getBookingId());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("UPDATE thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi cập nhật booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int delete(Booking t) {
//		int ketQua = 0;
//		String sql = "DELETE FROM booking WHERE booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, t.getBookingId());
//			ketQua = pstmt.executeUpdate();
//			System.out.println("DELETE thành công, " + ketQua + " dòng bị xóa.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi xóa booking: " + e.getMessage());
//		}
//		return ketQua;
//	}
//
//	@Override
//	public List<Booking> selectAll() {
//		List<Booking> list = new ArrayList<>();
//		String sql = "SELECT * FROM booking";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//			rs = pstmt.executeQuery();
//
//			CustomerRepository customerRepo = new CustomerRepository();
//			PetRepository petRepo = new PetRepository();
//			StaffRepository staffRepo = new StaffRepository();
//
//			while (rs.next()) {
//				Customer customer = customerRepo.selectById(rs.getInt("customer_id"));
//				Pet pet = petRepo.selectById(rs.getInt("pet_id"));
//				Staff staff = rs.getObject("staff_id") != null ? staffRepo.selectById(rs.getInt("staff_id")) : null;
//
//				Booking booking = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//						rs.getTimestamp("booking_time").toLocalDateTime(), StatusEnum.valueOf(rs.getString("status")),
//						rs.getString("note"));
//
//				list.add(booking);
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi lấy danh sách booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt, rs);
//		}
//
//		return list;
//	}
//
////    public Booking selectById(int bookingID) {
////        Booking ketQua = null;
////        String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////                + "WHERE b.booking_id = ?";
////
////        try (Connection con = DatabaseConnection.getConnection();
////             PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////            pstmt.setInt(1, bookingID);
////            try (ResultSet rs = pstmt.executeQuery()) {
////                if (rs.next()) {
////                    Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    ketQua = new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    );
////                }
////            }
////
////        } catch (SQLException e) {
////            System.err.println("Lỗi khi tìm booking: " + e.getMessage());
////        }
////
////        return ketQua;
////    }
//	public Booking selectById(int bookingID) {
//		Booking ketQua = null;
//		String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//				+ "c.customer_id, c.point, c.created_at, "
//				+ "p.full_name AS customer_name, p.phone AS customer_phone, p.email AS customer_email, "
//				+ "p.gender AS customer_gender, " + "p.address AS customer_address, "
//				+ "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
//				+ "s.staff_id, s.name AS staff_name, s.phone AS staff_phone " + "FROM booking b "
//				+ "JOIN customer c ON b.customer_id = c.customer_id " + "JOIN pet p ON b.pet_id = p.pet_id "
//				+ "LEFT JOIN staff s ON b.staff_id = s.staff_id " + "WHERE b.booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, bookingID);
//			try (ResultSet rs = pstmt.executeQuery()) {
//				if (rs.next()) {
//					Customer customer = new Customer(rs.getInt("customer_id"), rs.getString("customer_name"),
//							GenderEnum.valueOf(rs.getString("customer_gender")), rs.getString("customer_phone"),
//							rs.getString("customer_address"), rs.getString("customer_email"), rs.getInt("point"),
//							rs.getTimestamp("created_at"));
//					Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
//					Staff staff = rs.getObject("staff_id") != null
//							? new StaffRepository().selectById(rs.getInt("staff_id"))
//							: null;
//
//					ketQua = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//							rs.getTimestamp("booking_time").toLocalDateTime(),
//							StatusEnum.valueOf(rs.getString("status")), rs.getString("note"));
//				}
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi tìm booking: " + e.getMessage());
//		}
//
//		return ketQua;
//	}
//
//	@Override
//	public Booking selectById(Booking t) {
//		return selectById(t.getBookingId());
//	}
////    
////    @Override
////    public List<Booking> selectByCondition(String condition, Object... params) {
////    	List<Booking> bookings = new ArrayList<>();
////
////	    if (condition == null || condition.trim().isEmpty()) {
////	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
////	    }
////
////	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////	            + "WHERE " + condition;
////
////	    try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////	        for (int i = 0; i < params.length; i++) {
////	            pstmt.setObject(i + 1, params[i]);
////	        }
////
////	        try (ResultSet rs = pstmt.executeQuery()) {
////	            while (rs.next()) {
////	            	Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    bookings.add( new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    ));
////	            }
////	        }
////	    } catch (SQLException e) {
////	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
////	    }
////	    return bookings;
////    }
//	@Override
//	public List<Booking> selectByCondition(String condition, Object... params) {
//	    List<Booking> bookings = new ArrayList<>();
//
//	    if (condition == null || condition.trim().isEmpty()) {
//	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
//	    }
//
//	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//	               + "c.customer_id, c.point, c.created_at, "
//	               + "p.pet_id, p.name AS pet_name, p.pet_gender AS pet_gender, p.dob AS pet_dob, p.weight AS pet_weight, p.note AS pet_note, "
//	               + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
//	               + "FROM booking b "
//	               + "JOIN customer c ON b.customer_id = c.customer_id "
//	               + "JOIN pet p ON b.pet_id = p.pet_id "
//	               + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
//	               + "WHERE " + condition;
//
//	    try (Connection con = DatabaseConnection.getConnection(); 
//	         PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//	        for (int i = 0; i < params.length; i++) {
//	            pstmt.setObject(i + 1, params[i]);
//	        }
//
//	        try (ResultSet rs = pstmt.executeQuery()) {
//	            while (rs.next()) {
//	                // Lấy thông tin từ customer
//	                Customer customer = new Customer(
//	                    rs.getInt("customer_id"),
//	                    rs.getString("customer_name"),
//	                    GenderEnum.valueOf(rs.getString("customer_gender")), // Nếu có trường gender
//	                    rs.getString("customer_phone"),
//	                    rs.getString("customer_address"),
//	                    rs.getString("customer_email"),
//	                    rs.getInt("point"),
//	                    rs.getTimestamp("created_at")
//	                );
//
//	                // Lấy thông tin từ pet
//	                Pet pet = new Pet(
//	                    rs.getInt("pet_id"),
//	                    rs.getString("pet_name"),
//	                    PetType.valueOf(rs.getString("pet_species")), // Giả sử species là loại pet
//	                    GenderEnum.valueOf(rs.getString("pet_gender")), // Sử dụng giá trị từ pet_gender
//	                    rs.getDate("pet_dob").toLocalDate(),
//	                    rs.getDouble("pet_weight"),
//	                    rs.getString("pet_note"),
//	                    customer // Gán owner là customer
//	                );
//
//	                // Lấy thông tin từ staff (nếu có)
//	                Staff staff = rs.getObject("staff_id") != null 
//	                    ? new StaffRepository().selectById(rs.getInt("staff_id"))
//	                    : null;
//
//	                // Tạo đối tượng Booking và thêm vào danh sách
//	                bookings.add(new Booking(
//	                    rs.getInt("booking_id"),
//	                    customer,
//	                    pet,
//	                    staff,
//	                    rs.getTimestamp("booking_time").toLocalDateTime(),
//	                    StatusEnum.valueOf(rs.getString("status")),
//	                    rs.getString("note")
//	                ));
//	            }
//	        }
//	    } catch (SQLException e) {
//	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
//	    }
//	    return bookings;
//	}
//
//	public int getMonthlyBookings() {
//		String query = "SELECT COUNT(*) AS total_bookings " + "FROM booking "
//				+ "WHERE MONTH(booking_time) = MONTH(CURRENT_DATE) " + "AND YEAR(booking_time) = YEAR(CURRENT_DATE)";
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			if (resultSet.next()) {
//				return resultSet.getInt("total_bookings");
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return 0; // Return 0 if no data is found or an error occurs
//	}
//
//	public XYChart.Series<String, Number> getBookingData(String timeUnit) {
//		String query = "";
//		if (timeUnit.equals("WEEK")) {
//			query = "SELECT WEEK(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY WEEK(booking_time)";
//		} else if (timeUnit.equals("MONTH")) {
//			query = "SELECT MONTH(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY MONTH(booking_time)";
//		} else if (timeUnit.equals("YEAR")) {
//			query = "SELECT YEAR(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY YEAR(booking_time)";
//		}
//
//		XYChart.Series<String, Number> series = new XYChart.Series<>();
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			while (resultSet.next()) {
//				String time = resultSet.getString("time");
//				int totalBookings = resultSet.getInt("total_bookings");
//				series.getData().add(new XYChart.Data<>(time, totalBookings));
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return series;
//	}
//}
//package repository;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.sql.Types;
//import java.util.ArrayList;
//import java.util.List;
//
//import enums.GenderEnum;
//import enums.StatusEnum;
//import javafx.scene.chart.XYChart;
//import model.Booking;
//import model.Customer;
//import model.Pet;
//import model.PetType;
//import model.Staff;
//import utils.DBUtil;
//import utils.DatabaseConnection;
//
//public class BookingRepository implements IRepository<Booking> {
//
//	private static BookingRepository instance;
//
//	public static BookingRepository getInstance() {
//		if (instance == null) {
//			instance = new BookingRepository();
//		}
//		return instance;
//	}
//
//	@Override
//	public int insert(Booking t) {
//		int ketQua = 0;
//		String sql = "INSERT INTO booking (customer_id, pet_id, staff_id, booking_time, status, note) VALUES (?, ?, ?, ?, ?, ?)";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getCustomer().getId());
//			pstmt.setInt(2, t.getPet().getPetId());
//			pstmt.setInt(3, t.getStaff() != null ? t.getStaff().getId() : Types.NULL); // Cho phép NULL
//			pstmt.setTimestamp(4, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(5, t.getStatus().name());
//			pstmt.setString(6, t.getNote());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("INSERT thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi thêm booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int update(Booking t) {
//		int ketQua = 0;
//		String sql = "UPDATE booking SET staff_id = ?, booking_time = ?, status = ?, note = ? WHERE booking_id = ?";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getStaff() != null ? t.getStaff().getId() : Types.NULL);
//			pstmt.setTimestamp(2, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(3, t.getStatus().name());
//			pstmt.setString(4, t.getNote());
//			pstmt.setInt(5, t.getBookingId());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("UPDATE thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi cập nhật booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int delete(Booking t) {
//		int ketQua = 0;
//		String sql = "DELETE FROM booking WHERE booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, t.getBookingId());
//			ketQua = pstmt.executeUpdate();
//			System.out.println("DELETE thành công, " + ketQua + " dòng bị xóa.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi xóa booking: " + e.getMessage());
//		}
//		return ketQua;
//	}
//
//	@Override
//	public List<Booking> selectAll() {
//		List<Booking> list = new ArrayList<>();
//		String sql = "SELECT * FROM booking";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//			rs = pstmt.executeQuery();
//
//			CustomerRepository customerRepo = new CustomerRepository();
//			PetRepository petRepo = new PetRepository();
//			StaffRepository staffRepo = new StaffRepository();
//
//			while (rs.next()) {
//				Customer customer = customerRepo.selectById(rs.getInt("customer_id"));
//				Pet pet = petRepo.selectById(rs.getInt("pet_id"));
//				Staff staff = rs.getObject("staff_id") != null ? staffRepo.selectById(rs.getInt("staff_id")) : null;
//
//				Booking booking = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//						rs.getTimestamp("booking_time").toLocalDateTime(), StatusEnum.valueOf(rs.getString("status")),
//						rs.getString("note"));
//
//				list.add(booking);
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi lấy danh sách booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt, rs);
//		}
//
//		return list;
//	}
//
////    public Booking selectById(int bookingID) {
////        Booking ketQua = null;
////        String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////                + "WHERE b.booking_id = ?";
////
////        try (Connection con = DatabaseConnection.getConnection();
////             PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////            pstmt.setInt(1, bookingID);
////            try (ResultSet rs = pstmt.executeQuery()) {
////                if (rs.next()) {
////                    Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    ketQua = new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    );
////                }
////            }
////
////        } catch (SQLException e) {
////            System.err.println("Lỗi khi tìm booking: " + e.getMessage());
////        }
////
////        return ketQua;
////    }
//	public Booking selectById(int bookingID) {
//		Booking ketQua = null;
//		String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//				+ "c.customer_id, c.point, c.created_at, "
//				+ "p.full_name AS customer_name, p.phone AS customer_phone, p.email AS customer_email, "
//				+ "p.gender AS customer_gender, " + "p.address AS customer_address, "
//				+ "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
//				+ "s.staff_id, s.name AS staff_name, s.phone AS staff_phone " + "FROM booking b "
//				+ "JOIN customer c ON b.customer_id = c.customer_id " + "JOIN pet p ON b.pet_id = p.pet_id "
//				+ "LEFT JOIN staff s ON b.staff_id = s.staff_id " + "WHERE b.booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, bookingID);
//			try (ResultSet rs = pstmt.executeQuery()) {
//				if (rs.next()) {
//					Customer customer = new Customer(rs.getInt("customer_id"), rs.getString("customer_name"),
//							GenderEnum.valueOf(rs.getString("customer_gender")), rs.getString("customer_phone"),
//							rs.getString("customer_address"), rs.getString("customer_email"), rs.getInt("point"),
//							rs.getTimestamp("created_at"));
//					Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
//					Staff staff = rs.getObject("staff_id") != null
//							? new StaffRepository().selectById(rs.getInt("staff_id"))
//							: null;
//
//					ketQua = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//							rs.getTimestamp("booking_time").toLocalDateTime(),
//							StatusEnum.valueOf(rs.getString("status")), rs.getString("note"));
//				}
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi tìm booking: " + e.getMessage());
//		}
//
//		return ketQua;
//	}
//
//	@Override
//	public Booking selectById(Booking t) {
//		return selectById(t.getBookingId());
//	}
////    
////    @Override
////    public List<Booking> selectByCondition(String condition, Object... params) {
////    	List<Booking> bookings = new ArrayList<>();
////
////	    if (condition == null || condition.trim().isEmpty()) {
////	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
////	    }
////
////	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////	            + "WHERE " + condition;
////
////	    try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////	        for (int i = 0; i < params.length; i++) {
////	            pstmt.setObject(i + 1, params[i]);
////	        }
////
////	        try (ResultSet rs = pstmt.executeQuery()) {
////	            while (rs.next()) {
////	            	Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    bookings.add( new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    ));
////	            }
////	        }
////	    } catch (SQLException e) {
////	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
////	    }
////	    return bookings;
////    }
//	@Override
//	public List<Booking> selectByCondition(String condition, Object... params) {
//	    List<Booking> bookings = new ArrayList<>();
//
//	    if (condition == null || condition.trim().isEmpty()) {
//	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
//	    }
//
//	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//	               + "c.customer_id, c.point, c.created_at, "
//	               + "p.pet_id, p.name AS pet_name, p.pet_gender AS pet_gender, p.dob AS pet_dob, p.weight AS pet_weight, p.note AS pet_note, "
//	               + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
//	               + "FROM booking b "
//	               + "JOIN customer c ON b.customer_id = c.customer_id "
//	               + "JOIN pet p ON b.pet_id = p.pet_id "
//	               + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
//	               + "WHERE " + condition;
//
//	    try (Connection con = DatabaseConnection.getConnection(); 
//	         PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//	        for (int i = 0; i < params.length; i++) {
//	            pstmt.setObject(i + 1, params[i]);
//	        }
//
//	        try (ResultSet rs = pstmt.executeQuery()) {
//	            while (rs.next()) {
//	                // Lấy thông tin từ customer
//	                Customer customer = new Customer(
//	                    rs.getInt("customer_id"),
//	                    rs.getString("customer_name"),
//	                    GenderEnum.valueOf(rs.getString("customer_gender")), // Nếu có trường gender
//	                    rs.getString("customer_phone"),
//	                    rs.getString("customer_address"),
//	                    rs.getString("customer_email"),
//	                    rs.getInt("point"),
//	                    rs.getTimestamp("created_at")
//	                );
//
//	                // Lấy thông tin từ pet
//	                Pet pet = new Pet(
//	                    rs.getInt("pet_id"),
//	                    rs.getString("pet_name"),
//	                    PetType.valueOf(rs.getString("pet_species")), // Giả sử species là loại pet
//	                    GenderEnum.valueOf(rs.getString("pet_gender")), // Sử dụng giá trị từ pet_gender
//	                    rs.getDate("pet_dob").toLocalDate(),
//	                    rs.getDouble("pet_weight"),
//	                    rs.getString("pet_note"),
//	                    customer // Gán owner là customer
//	                );
//
//	                // Lấy thông tin từ staff (nếu có)
//	                Staff staff = rs.getObject("staff_id") != null 
//	                    ? new StaffRepository().selectById(rs.getInt("staff_id"))
//	                    : null;
//
//	                // Tạo đối tượng Booking và thêm vào danh sách
//	                bookings.add(new Booking(
//	                    rs.getInt("booking_id"),
//	                    customer,
//	                    pet,
//	                    staff,
//	                    rs.getTimestamp("booking_time").toLocalDateTime(),
//	                    StatusEnum.valueOf(rs.getString("status")),
//	                    rs.getString("note")
//	                ));
//	            }
//	        }
//	    } catch (SQLException e) {
//	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
//	    }
//	    return bookings;
//	}
//
//	public int getMonthlyBookings() {
//		String query = "SELECT COUNT(*) AS total_bookings " + "FROM booking "
//				+ "WHERE MONTH(booking_time) = MONTH(CURRENT_DATE) " + "AND YEAR(booking_time) = YEAR(CURRENT_DATE)";
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			if (resultSet.next()) {
//				return resultSet.getInt("total_bookings");
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return 0; // Return 0 if no data is found or an error occurs
//	}
//
//	public XYChart.Series<String, Number> getBookingData(String timeUnit) {
//		String query = "";
//		if (timeUnit.equals("WEEK")) {
//			query = "SELECT WEEK(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY WEEK(booking_time)";
//		} else if (timeUnit.equals("MONTH")) {
//			query = "SELECT MONTH(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY MONTH(booking_time)";
//		} else if (timeUnit.equals("YEAR")) {
//			query = "SELECT YEAR(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY YEAR(booking_time)";
//		}
//
//		XYChart.Series<String, Number> series = new XYChart.Series<>();
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			while (resultSet.next()) {
//				String time = resultSet.getString("time");
//				int totalBookings = resultSet.getInt("total_bookings");
//				series.getData().add(new XYChart.Data<>(time, totalBookings));
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return series;
//	}
//}
//package repository;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.sql.Types;
//import java.util.ArrayList;
//import java.util.List;
//
//import enums.GenderEnum;
//import enums.StatusEnum;
//import javafx.scene.chart.XYChart;
//import model.Booking;
//import model.Customer;
//import model.Pet;
//import model.PetType;
//import model.Staff;
//import utils.DBUtil;
//import utils.DatabaseConnection;
//
//public class BookingRepository implements IRepository<Booking> {
//
//	private static BookingRepository instance;
//
//	public static BookingRepository getInstance() {
//		if (instance == null) {
//			instance = new BookingRepository();
//		}
//		return instance;
//	}
//
//	@Override
//	public int insert(Booking t) {
//		int ketQua = 0;
//		String sql = "INSERT INTO booking (customer_id, pet_id, staff_id, booking_time, status, note) VALUES (?, ?, ?, ?, ?, ?)";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getCustomer().getId());
//			pstmt.setInt(2, t.getPet().getPetId());
//			pstmt.setInt(3, t.getStaff() != null ? t.getStaff().getId() : Types.NULL); // Cho phép NULL
//			pstmt.setTimestamp(4, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(5, t.getStatus().name());
//			pstmt.setString(6, t.getNote());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("INSERT thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi thêm booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int update(Booking t) {
//		int ketQua = 0;
//		String sql = "UPDATE booking SET staff_id = ?, booking_time = ?, status = ?, note = ? WHERE booking_id = ?";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getStaff() != null ? t.getStaff().getId() : Types.NULL);
//			pstmt.setTimestamp(2, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(3, t.getStatus().name());
//			pstmt.setString(4, t.getNote());
//			pstmt.setInt(5, t.getBookingId());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("UPDATE thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi cập nhật booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int delete(Booking t) {
//		int ketQua = 0;
//		String sql = "DELETE FROM booking WHERE booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, t.getBookingId());
//			ketQua = pstmt.executeUpdate();
//			System.out.println("DELETE thành công, " + ketQua + " dòng bị xóa.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi xóa booking: " + e.getMessage());
//		}
//		return ketQua;
//	}
//
//	@Override
//	public List<Booking> selectAll() {
//		List<Booking> list = new ArrayList<>();
//		String sql = "SELECT * FROM booking";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//			rs = pstmt.executeQuery();
//
//			CustomerRepository customerRepo = new CustomerRepository();
//			PetRepository petRepo = new PetRepository();
//			StaffRepository staffRepo = new StaffRepository();
//
//			while (rs.next()) {
//				Customer customer = customerRepo.selectById(rs.getInt("customer_id"));
//				Pet pet = petRepo.selectById(rs.getInt("pet_id"));
//				Staff staff = rs.getObject("staff_id") != null ? staffRepo.selectById(rs.getInt("staff_id")) : null;
//
//				Booking booking = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//						rs.getTimestamp("booking_time").toLocalDateTime(), StatusEnum.valueOf(rs.getString("status")),
//						rs.getString("note"));
//
//				list.add(booking);
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi lấy danh sách booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt, rs);
//		}
//
//		return list;
//	}
//
////    public Booking selectById(int bookingID) {
////        Booking ketQua = null;
////        String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////                + "WHERE b.booking_id = ?";
////
////        try (Connection con = DatabaseConnection.getConnection();
////             PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////            pstmt.setInt(1, bookingID);
////            try (ResultSet rs = pstmt.executeQuery()) {
////                if (rs.next()) {
////                    Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    ketQua = new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    );
////                }
////            }
////
////        } catch (SQLException e) {
////            System.err.println("Lỗi khi tìm booking: " + e.getMessage());
////        }
////
////        return ketQua;
////    }
//	public Booking selectById(int bookingID) {
//		Booking ketQua = null;
//		String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//				+ "c.customer_id, c.point, c.created_at, "
//				+ "p.full_name AS customer_name, p.phone AS customer_phone, p.email AS customer_email, "
//				+ "p.gender AS customer_gender, " + "p.address AS customer_address, "
//				+ "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
//				+ "s.staff_id, s.name AS staff_name, s.phone AS staff_phone " + "FROM booking b "
//				+ "JOIN customer c ON b.customer_id = c.customer_id " + "JOIN pet p ON b.pet_id = p.pet_id "
//				+ "LEFT JOIN staff s ON b.staff_id = s.staff_id " + "WHERE b.booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, bookingID);
//			try (ResultSet rs = pstmt.executeQuery()) {
//				if (rs.next()) {
//					Customer customer = new Customer(rs.getInt("customer_id"), rs.getString("customer_name"),
//							GenderEnum.valueOf(rs.getString("customer_gender")), rs.getString("customer_phone"),
//							rs.getString("customer_address"), rs.getString("customer_email"), rs.getInt("point"),
//							rs.getTimestamp("created_at"));
//					Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
//					Staff staff = rs.getObject("staff_id") != null
//							? new StaffRepository().selectById(rs.getInt("staff_id"))
//							: null;
//
//					ketQua = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//							rs.getTimestamp("booking_time").toLocalDateTime(),
//							StatusEnum.valueOf(rs.getString("status")), rs.getString("note"));
//				}
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi tìm booking: " + e.getMessage());
//		}
//
//		return ketQua;
//	}
//
//	@Override
//	public Booking selectById(Booking t) {
//		return selectById(t.getBookingId());
//	}
////    
////    @Override
////    public List<Booking> selectByCondition(String condition, Object... params) {
////    	List<Booking> bookings = new ArrayList<>();
////
////	    if (condition == null || condition.trim().isEmpty()) {
////	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
////	    }
////
////	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////	            + "WHERE " + condition;
////
////	    try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////	        for (int i = 0; i < params.length; i++) {
////	            pstmt.setObject(i + 1, params[i]);
////	        }
////
////	        try (ResultSet rs = pstmt.executeQuery()) {
////	            while (rs.next()) {
////	            	Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    bookings.add( new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    ));
////	            }
////	        }
////	    } catch (SQLException e) {
////	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
////	    }
////	    return bookings;
////    }
//	@Override
//	public List<Booking> selectByCondition(String condition, Object... params) {
//	    List<Booking> bookings = new ArrayList<>();
//
//	    if (condition == null || condition.trim().isEmpty()) {
//	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
//	    }
//
//	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//	               + "c.customer_id, c.point, c.created_at, "
//	               + "p.pet_id, p.name AS pet_name, p.pet_gender AS pet_gender, p.dob AS pet_dob, p.weight AS pet_weight, p.note AS pet_note, "
//	               + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
//	               + "FROM booking b "
//	               + "JOIN customer c ON b.customer_id = c.customer_id "
//	               + "JOIN pet p ON b.pet_id = p.pet_id "
//	               + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
//	               + "WHERE " + condition;
//
//	    try (Connection con = DatabaseConnection.getConnection(); 
//	         PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//	        for (int i = 0; i < params.length; i++) {
//	            pstmt.setObject(i + 1, params[i]);
//	        }
//
//	        try (ResultSet rs = pstmt.executeQuery()) {
//	            while (rs.next()) {
//	                // Lấy thông tin từ customer
//	                Customer customer = new Customer(
//	                    rs.getInt("customer_id"),
//	                    rs.getString("customer_name"),
//	                    GenderEnum.valueOf(rs.getString("customer_gender")), // Nếu có trường gender
//	                    rs.getString("customer_phone"),
//	                    rs.getString("customer_address"),
//	                    rs.getString("customer_email"),
//	                    rs.getInt("point"),
//	                    rs.getTimestamp("created_at")
//	                );
//
//	                // Lấy thông tin từ pet
//	                Pet pet = new Pet(
//	                    rs.getInt("pet_id"),
//	                    rs.getString("pet_name"),
//	                    PetType.valueOf(rs.getString("pet_species")), // Giả sử species là loại pet
//	                    GenderEnum.valueOf(rs.getString("pet_gender")), // Sử dụng giá trị từ pet_gender
//	                    rs.getDate("pet_dob").toLocalDate(),
//	                    rs.getDouble("pet_weight"),
//	                    rs.getString("pet_note"),
//	                    customer // Gán owner là customer
//	                );
//
//	                // Lấy thông tin từ staff (nếu có)
//	                Staff staff = rs.getObject("staff_id") != null 
//	                    ? new StaffRepository().selectById(rs.getInt("staff_id"))
//	                    : null;
//
//	                // Tạo đối tượng Booking và thêm vào danh sách
//	                bookings.add(new Booking(
//	                    rs.getInt("booking_id"),
//	                    customer,
//	                    pet,
//	                    staff,
//	                    rs.getTimestamp("booking_time").toLocalDateTime(),
//	                    StatusEnum.valueOf(rs.getString("status")),
//	                    rs.getString("note")
//	                ));
//	            }
//	        }
//	    } catch (SQLException e) {
//	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
//	    }
//	    return bookings;
//	}
//
//	public int getMonthlyBookings() {
//		String query = "SELECT COUNT(*) AS total_bookings " + "FROM booking "
//				+ "WHERE MONTH(booking_time) = MONTH(CURRENT_DATE) " + "AND YEAR(booking_time) = YEAR(CURRENT_DATE)";
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			if (resultSet.next()) {
//				return resultSet.getInt("total_bookings");
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return 0; // Return 0 if no data is found or an error occurs
//	}
//
//	public XYChart.Series<String, Number> getBookingData(String timeUnit) {
//		String query = "";
//		if (timeUnit.equals("WEEK")) {
//			query = "SELECT WEEK(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY WEEK(booking_time)";
//		} else if (timeUnit.equals("MONTH")) {
//			query = "SELECT MONTH(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY MONTH(booking_time)";
//		} else if (timeUnit.equals("YEAR")) {
//			query = "SELECT YEAR(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY YEAR(booking_time)";
//		}
//
//		XYChart.Series<String, Number> series = new XYChart.Series<>();
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			while (resultSet.next()) {
//				String time = resultSet.getString("time");
//				int totalBookings = resultSet.getInt("total_bookings");
//				series.getData().add(new XYChart.Data<>(time, totalBookings));
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return series;
//	}
//}
//package repository;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.sql.Types;
//import java.util.ArrayList;
//import java.util.List;
//
//import enums.GenderEnum;
//import enums.StatusEnum;
//import javafx.scene.chart.XYChart;
//import model.Booking;
//import model.Customer;
//import model.Pet;
//import model.PetType;
//import model.Staff;
//import utils.DBUtil;
//import utils.DatabaseConnection;
//
//public class BookingRepository implements IRepository<Booking> {
//
//	private static BookingRepository instance;
//
//	public static BookingRepository getInstance() {
//		if (instance == null) {
//			instance = new BookingRepository();
//		}
//		return instance;
//	}
//
//	@Override
//	public int insert(Booking t) {
//		int ketQua = 0;
//		String sql = "INSERT INTO booking (customer_id, pet_id, staff_id, booking_time, status, note) VALUES (?, ?, ?, ?, ?, ?)";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getCustomer().getId());
//			pstmt.setInt(2, t.getPet().getPetId());
//			pstmt.setInt(3, t.getStaff() != null ? t.getStaff().getId() : Types.NULL); // Cho phép NULL
//			pstmt.setTimestamp(4, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(5, t.getStatus().name());
//			pstmt.setString(6, t.getNote());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("INSERT thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi thêm booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int update(Booking t) {
//		int ketQua = 0;
//		String sql = "UPDATE booking SET staff_id = ?, booking_time = ?, status = ?, note = ? WHERE booking_id = ?";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getStaff() != null ? t.getStaff().getId() : Types.NULL);
//			pstmt.setTimestamp(2, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(3, t.getStatus().name());
//			pstmt.setString(4, t.getNote());
//			pstmt.setInt(5, t.getBookingId());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("UPDATE thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi cập nhật booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int delete(Booking t) {
//		int ketQua = 0;
//		String sql = "DELETE FROM booking WHERE booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, t.getBookingId());
//			ketQua = pstmt.executeUpdate();
//			System.out.println("DELETE thành công, " + ketQua + " dòng bị xóa.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi xóa booking: " + e.getMessage());
//		}
//		return ketQua;
//	}
//
//	@Override
//	public List<Booking> selectAll() {
//		List<Booking> list = new ArrayList<>();
//		String sql = "SELECT * FROM booking";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//			rs = pstmt.executeQuery();
//
//			CustomerRepository customerRepo = new CustomerRepository();
//			PetRepository petRepo = new PetRepository();
//			StaffRepository staffRepo = new StaffRepository();
//
//			while (rs.next()) {
//				Customer customer = customerRepo.selectById(rs.getInt("customer_id"));
//				Pet pet = petRepo.selectById(rs.getInt("pet_id"));
//				Staff staff = rs.getObject("staff_id") != null ? staffRepo.selectById(rs.getInt("staff_id")) : null;
//
//				Booking booking = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//						rs.getTimestamp("booking_time").toLocalDateTime(), StatusEnum.valueOf(rs.getString("status")),
//						rs.getString("note"));
//
//				list.add(booking);
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi lấy danh sách booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt, rs);
//		}
//
//		return list;
//	}
//
////    public Booking selectById(int bookingID) {
////        Booking ketQua = null;
////        String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////                + "WHERE b.booking_id = ?";
////
////        try (Connection con = DatabaseConnection.getConnection();
////             PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////            pstmt.setInt(1, bookingID);
////            try (ResultSet rs = pstmt.executeQuery()) {
////                if (rs.next()) {
////                    Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    ketQua = new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    );
////                }
////            }
////
////        } catch (SQLException e) {
////            System.err.println("Lỗi khi tìm booking: " + e.getMessage());
////        }
////
////        return ketQua;
////    }
//	public Booking selectById(int bookingID) {
//		Booking ketQua = null;
//		String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//				+ "c.customer_id, c.point, c.created_at, "
//				+ "p.full_name AS customer_name, p.phone AS customer_phone, p.email AS customer_email, "
//				+ "p.gender AS customer_gender, " + "p.address AS customer_address, "
//				+ "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
//				+ "s.staff_id, s.name AS staff_name, s.phone AS staff_phone " + "FROM booking b "
//				+ "JOIN customer c ON b.customer_id = c.customer_id " + "JOIN pet p ON b.pet_id = p.pet_id "
//				+ "LEFT JOIN staff s ON b.staff_id = s.staff_id " + "WHERE b.booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, bookingID);
//			try (ResultSet rs = pstmt.executeQuery()) {
//				if (rs.next()) {
//					Customer customer = new Customer(rs.getInt("customer_id"), rs.getString("customer_name"),
//							GenderEnum.valueOf(rs.getString("customer_gender")), rs.getString("customer_phone"),
//							rs.getString("customer_address"), rs.getString("customer_email"), rs.getInt("point"),
//							rs.getTimestamp("created_at"));
//					Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
//					Staff staff = rs.getObject("staff_id") != null
//							? new StaffRepository().selectById(rs.getInt("staff_id"))
//							: null;
//
//					ketQua = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//							rs.getTimestamp("booking_time").toLocalDateTime(),
//							StatusEnum.valueOf(rs.getString("status")), rs.getString("note"));
//				}
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi tìm booking: " + e.getMessage());
//		}
//
//		return ketQua;
//	}
//
//	@Override
//	public Booking selectById(Booking t) {
//		return selectById(t.getBookingId());
//	}
////    
////    @Override
////    public List<Booking> selectByCondition(String condition, Object... params) {
////    	List<Booking> bookings = new ArrayList<>();
////
////	    if (condition == null || condition.trim().isEmpty()) {
////	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
////	    }
////
////	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////	            + "WHERE " + condition;
////
////	    try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////	        for (int i = 0; i < params.length; i++) {
////	            pstmt.setObject(i + 1, params[i]);
////	        }
////
////	        try (ResultSet rs = pstmt.executeQuery()) {
////	            while (rs.next()) {
////	            	Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    bookings.add( new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    ));
////	            }
////	        }
////	    } catch (SQLException e) {
////	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
////	    }
////	    return bookings;
////    }
//	@Override
//	public List<Booking> selectByCondition(String condition, Object... params) {
//	    List<Booking> bookings = new ArrayList<>();
//
//	    if (condition == null || condition.trim().isEmpty()) {
//	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
//	    }
//
//	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//	               + "c.customer_id, c.point, c.created_at, "
//	               + "p.pet_id, p.name AS pet_name, p.pet_gender AS pet_gender, p.dob AS pet_dob, p.weight AS pet_weight, p.note AS pet_note, "
//	               + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
//	               + "FROM booking b "
//	               + "JOIN customer c ON b.customer_id = c.customer_id "
//	               + "JOIN pet p ON b.pet_id = p.pet_id "
//	               + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
//	               + "WHERE " + condition;
//
//	    try (Connection con = DatabaseConnection.getConnection(); 
//	         PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//	        for (int i = 0; i < params.length; i++) {
//	            pstmt.setObject(i + 1, params[i]);
//	        }
//
//	        try (ResultSet rs = pstmt.executeQuery()) {
//	            while (rs.next()) {
//	                // Lấy thông tin từ customer
//	                Customer customer = new Customer(
//	                    rs.getInt("customer_id"),
//	                    rs.getString("customer_name"),
//	                    GenderEnum.valueOf(rs.getString("customer_gender")), // Nếu có trường gender
//	                    rs.getString("customer_phone"),
//	                    rs.getString("customer_address"),
//	                    rs.getString("customer_email"),
//	                    rs.getInt("point"),
//	                    rs.getTimestamp("created_at")
//	                );
//
//	                // Lấy thông tin từ pet
//	                Pet pet = new Pet(
//	                    rs.getInt("pet_id"),
//	                    rs.getString("pet_name"),
//	                    PetType.valueOf(rs.getString("pet_species")), // Giả sử species là loại pet
//	                    GenderEnum.valueOf(rs.getString("pet_gender")), // Sử dụng giá trị từ pet_gender
//	                    rs.getDate("pet_dob").toLocalDate(),
//	                    rs.getDouble("pet_weight"),
//	                    rs.getString("pet_note"),
//	                    customer // Gán owner là customer
//	                );
//
//	                // Lấy thông tin từ staff (nếu có)
//	                Staff staff = rs.getObject("staff_id") != null 
//	                    ? new StaffRepository().selectById(rs.getInt("staff_id"))
//	                    : null;
//
//	                // Tạo đối tượng Booking và thêm vào danh sách
//	                bookings.add(new Booking(
//	                    rs.getInt("booking_id"),
//	                    customer,
//	                    pet,
//	                    staff,
//	                    rs.getTimestamp("booking_time").toLocalDateTime(),
//	                    StatusEnum.valueOf(rs.getString("status")),
//	                    rs.getString("note")
//	                ));
//	            }
//	        }
//	    } catch (SQLException e) {
//	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
//	    }
//	    return bookings;
//	}
//
//	public int getMonthlyBookings() {
//		String query = "SELECT COUNT(*) AS total_bookings " + "FROM booking "
//				+ "WHERE MONTH(booking_time) = MONTH(CURRENT_DATE) " + "AND YEAR(booking_time) = YEAR(CURRENT_DATE)";
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			if (resultSet.next()) {
//				return resultSet.getInt("total_bookings");
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return 0; // Return 0 if no data is found or an error occurs
//	}
//
//	public XYChart.Series<String, Number> getBookingData(String timeUnit) {
//		String query = "";
//		if (timeUnit.equals("WEEK")) {
//			query = "SELECT WEEK(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY WEEK(booking_time)";
//		} else if (timeUnit.equals("MONTH")) {
//			query = "SELECT MONTH(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY MONTH(booking_time)";
//		} else if (timeUnit.equals("YEAR")) {
//			query = "SELECT YEAR(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY YEAR(booking_time)";
//		}
//
//		XYChart.Series<String, Number> series = new XYChart.Series<>();
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			while (resultSet.next()) {
//				String time = resultSet.getString("time");
//				int totalBookings = resultSet.getInt("total_bookings");
//				series.getData().add(new XYChart.Data<>(time, totalBookings));
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return series;
//	}
//}
//package repository;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.sql.Types;
//import java.util.ArrayList;
//import java.util.List;
//
//import enums.GenderEnum;
//import enums.StatusEnum;
//import javafx.scene.chart.XYChart;
//import model.Booking;
//import model.Customer;
//import model.Pet;
//import model.PetType;
//import model.Staff;
//import utils.DBUtil;
//import utils.DatabaseConnection;
//
//public class BookingRepository implements IRepository<Booking> {
//
//	private static BookingRepository instance;
//
//	public static BookingRepository getInstance() {
//		if (instance == null) {
//			instance = new BookingRepository();
//		}
//		return instance;
//	}
//
//	@Override
//	public int insert(Booking t) {
//		int ketQua = 0;
//		String sql = "INSERT INTO booking (customer_id, pet_id, staff_id, booking_time, status, note) VALUES (?, ?, ?, ?, ?, ?)";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getCustomer().getId());
//			pstmt.setInt(2, t.getPet().getPetId());
//			pstmt.setInt(3, t.getStaff() != null ? t.getStaff().getId() : Types.NULL); // Cho phép NULL
//			pstmt.setTimestamp(4, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(5, t.getStatus().name());
//			pstmt.setString(6, t.getNote());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("INSERT thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi thêm booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int update(Booking t) {
//		int ketQua = 0;
//		String sql = "UPDATE booking SET staff_id = ?, booking_time = ?, status = ?, note = ? WHERE booking_id = ?";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getStaff() != null ? t.getStaff().getId() : Types.NULL);
//			pstmt.setTimestamp(2, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(3, t.getStatus().name());
//			pstmt.setString(4, t.getNote());
//			pstmt.setInt(5, t.getBookingId());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("UPDATE thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi cập nhật booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int delete(Booking t) {
//		int ketQua = 0;
//		String sql = "DELETE FROM booking WHERE booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, t.getBookingId());
//			ketQua = pstmt.executeUpdate();
//			System.out.println("DELETE thành công, " + ketQua + " dòng bị xóa.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi xóa booking: " + e.getMessage());
//		}
//		return ketQua;
//	}
//
//	@Override
//	public List<Booking> selectAll() {
//		List<Booking> list = new ArrayList<>();
//		String sql = "SELECT * FROM booking";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//			rs = pstmt.executeQuery();
//
//			CustomerRepository customerRepo = new CustomerRepository();
//			PetRepository petRepo = new PetRepository();
//			StaffRepository staffRepo = new StaffRepository();
//
//			while (rs.next()) {
//				Customer customer = customerRepo.selectById(rs.getInt("customer_id"));
//				Pet pet = petRepo.selectById(rs.getInt("pet_id"));
//				Staff staff = rs.getObject("staff_id") != null ? staffRepo.selectById(rs.getInt("staff_id")) : null;
//
//				Booking booking = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//						rs.getTimestamp("booking_time").toLocalDateTime(), StatusEnum.valueOf(rs.getString("status")),
//						rs.getString("note"));
//
//				list.add(booking);
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi lấy danh sách booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt, rs);
//		}
//
//		return list;
//	}
//
////    public Booking selectById(int bookingID) {
////        Booking ketQua = null;
////        String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////                + "WHERE b.booking_id = ?";
////
////        try (Connection con = DatabaseConnection.getConnection();
////             PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////            pstmt.setInt(1, bookingID);
////            try (ResultSet rs = pstmt.executeQuery()) {
////                if (rs.next()) {
////                    Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    ketQua = new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    );
////                }
////            }
////
////        } catch (SQLException e) {
////            System.err.println("Lỗi khi tìm booking: " + e.getMessage());
////        }
////
////        return ketQua;
////    }
//	public Booking selectById(int bookingID) {
//		Booking ketQua = null;
//		String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//				+ "c.customer_id, c.point, c.created_at, "
//				+ "p.full_name AS customer_name, p.phone AS customer_phone, p.email AS customer_email, "
//				+ "p.gender AS customer_gender, " + "p.address AS customer_address, "
//				+ "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
//				+ "s.staff_id, s.name AS staff_name, s.phone AS staff_phone " + "FROM booking b "
//				+ "JOIN customer c ON b.customer_id = c.customer_id " + "JOIN pet p ON b.pet_id = p.pet_id "
//				+ "LEFT JOIN staff s ON b.staff_id = s.staff_id " + "WHERE b.booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, bookingID);
//			try (ResultSet rs = pstmt.executeQuery()) {
//				if (rs.next()) {
//					Customer customer = new Customer(rs.getInt("customer_id"), rs.getString("customer_name"),
//							GenderEnum.valueOf(rs.getString("customer_gender")), rs.getString("customer_phone"),
//							rs.getString("customer_address"), rs.getString("customer_email"), rs.getInt("point"),
//							rs.getTimestamp("created_at"));
//					Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
//					Staff staff = rs.getObject("staff_id") != null
//							? new StaffRepository().selectById(rs.getInt("staff_id"))
//							: null;
//
//					ketQua = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//							rs.getTimestamp("booking_time").toLocalDateTime(),
//							StatusEnum.valueOf(rs.getString("status")), rs.getString("note"));
//				}
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi tìm booking: " + e.getMessage());
//		}
//
//		return ketQua;
//	}
//
//	@Override
//	public Booking selectById(Booking t) {
//		return selectById(t.getBookingId());
//	}
////    
////    @Override
////    public List<Booking> selectByCondition(String condition, Object... params) {
////    	List<Booking> bookings = new ArrayList<>();
////
////	    if (condition == null || condition.trim().isEmpty()) {
////	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
////	    }
////
////	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////	            + "WHERE " + condition;
////
////	    try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////	        for (int i = 0; i < params.length; i++) {
////	            pstmt.setObject(i + 1, params[i]);
////	        }
////
////	        try (ResultSet rs = pstmt.executeQuery()) {
////	            while (rs.next()) {
////	            	Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    bookings.add( new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    ));
////	            }
////	        }
////	    } catch (SQLException e) {
////	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
////	    }
////	    return bookings;
////    }
//	@Override
//	public List<Booking> selectByCondition(String condition, Object... params) {
//	    List<Booking> bookings = new ArrayList<>();
//
//	    if (condition == null || condition.trim().isEmpty()) {
//	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
//	    }
//
//	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//	               + "c.customer_id, c.point, c.created_at, "
//	               + "p.pet_id, p.name AS pet_name, p.pet_gender AS pet_gender, p.dob AS pet_dob, p.weight AS pet_weight, p.note AS pet_note, "
//	               + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
//	               + "FROM booking b "
//	               + "JOIN customer c ON b.customer_id = c.customer_id "
//	               + "JOIN pet p ON b.pet_id = p.pet_id "
//	               + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
//	               + "WHERE " + condition;
//
//	    try (Connection con = DatabaseConnection.getConnection(); 
//	         PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//	        for (int i = 0; i < params.length; i++) {
//	            pstmt.setObject(i + 1, params[i]);
//	        }
//
//	        try (ResultSet rs = pstmt.executeQuery()) {
//	            while (rs.next()) {
//	                // Lấy thông tin từ customer
//	                Customer customer = new Customer(
//	                    rs.getInt("customer_id"),
//	                    rs.getString("customer_name"),
//	                    GenderEnum.valueOf(rs.getString("customer_gender")), // Nếu có trường gender
//	                    rs.getString("customer_phone"),
//	                    rs.getString("customer_address"),
//	                    rs.getString("customer_email"),
//	                    rs.getInt("point"),
//	                    rs.getTimestamp("created_at")
//	                );
//
//	                // Lấy thông tin từ pet
//	                Pet pet = new Pet(
//	                    rs.getInt("pet_id"),
//	                    rs.getString("pet_name"),
//	                    PetType.valueOf(rs.getString("pet_species")), // Giả sử species là loại pet
//	                    GenderEnum.valueOf(rs.getString("pet_gender")), // Sử dụng giá trị từ pet_gender
//	                    rs.getDate("pet_dob").toLocalDate(),
//	                    rs.getDouble("pet_weight"),
//	                    rs.getString("pet_note"),
//	                    customer // Gán owner là customer
//	                );
//
//	                // Lấy thông tin từ staff (nếu có)
//	                Staff staff = rs.getObject("staff_id") != null 
//	                    ? new StaffRepository().selectById(rs.getInt("staff_id"))
//	                    : null;
//
//	                // Tạo đối tượng Booking và thêm vào danh sách
//	                bookings.add(new Booking(
//	                    rs.getInt("booking_id"),
//	                    customer,
//	                    pet,
//	                    staff,
//	                    rs.getTimestamp("booking_time").toLocalDateTime(),
//	                    StatusEnum.valueOf(rs.getString("status")),
//	                    rs.getString("note")
//	                ));
//	            }
//	        }
//	    } catch (SQLException e) {
//	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
//	    }
//	    return bookings;
//	}
//
//	public int getMonthlyBookings() {
//		String query = "SELECT COUNT(*) AS total_bookings " + "FROM booking "
//				+ "WHERE MONTH(booking_time) = MONTH(CURRENT_DATE) " + "AND YEAR(booking_time) = YEAR(CURRENT_DATE)";
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			if (resultSet.next()) {
//				return resultSet.getInt("total_bookings");
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return 0; // Return 0 if no data is found or an error occurs
//	}
//
//	public XYChart.Series<String, Number> getBookingData(String timeUnit) {
//		String query = "";
//		if (timeUnit.equals("WEEK")) {
//			query = "SELECT WEEK(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY WEEK(booking_time)";
//		} else if (timeUnit.equals("MONTH")) {
//			query = "SELECT MONTH(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY MONTH(booking_time)";
//		} else if (timeUnit.equals("YEAR")) {
//			query = "SELECT YEAR(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY YEAR(booking_time)";
//		}
//
//		XYChart.Series<String, Number> series = new XYChart.Series<>();
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			while (resultSet.next()) {
//				String time = resultSet.getString("time");
//				int totalBookings = resultSet.getInt("total_bookings");
//				series.getData().add(new XYChart.Data<>(time, totalBookings));
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return series;
//	}
//}
//package repository;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.sql.Types;
//import java.util.ArrayList;
//import java.util.List;
//
//import enums.GenderEnum;
//import enums.StatusEnum;
//import javafx.scene.chart.XYChart;
//import model.Booking;
//import model.Customer;
//import model.Pet;
//import model.PetType;
//import model.Staff;
//import utils.DBUtil;
//import utils.DatabaseConnection;
//
//public class BookingRepository implements IRepository<Booking> {
//
//	private static BookingRepository instance;
//
//	public static BookingRepository getInstance() {
//		if (instance == null) {
//			instance = new BookingRepository();
//		}
//		return instance;
//	}
//
//	@Override
//	public int insert(Booking t) {
//		int ketQua = 0;
//		String sql = "INSERT INTO booking (customer_id, pet_id, staff_id, booking_time, status, note) VALUES (?, ?, ?, ?, ?, ?)";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getCustomer().getId());
//			pstmt.setInt(2, t.getPet().getPetId());
//			pstmt.setInt(3, t.getStaff() != null ? t.getStaff().getId() : Types.NULL); // Cho phép NULL
//			pstmt.setTimestamp(4, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(5, t.getStatus().name());
//			pstmt.setString(6, t.getNote());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("INSERT thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi thêm booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int update(Booking t) {
//		int ketQua = 0;
//		String sql = "UPDATE booking SET staff_id = ?, booking_time = ?, status = ?, note = ? WHERE booking_id = ?";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getStaff() != null ? t.getStaff().getId() : Types.NULL);
//			pstmt.setTimestamp(2, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(3, t.getStatus().name());
//			pstmt.setString(4, t.getNote());
//			pstmt.setInt(5, t.getBookingId());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("UPDATE thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi cập nhật booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int delete(Booking t) {
//		int ketQua = 0;
//		String sql = "DELETE FROM booking WHERE booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, t.getBookingId());
//			ketQua = pstmt.executeUpdate();
//			System.out.println("DELETE thành công, " + ketQua + " dòng bị xóa.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi xóa booking: " + e.getMessage());
//		}
//		return ketQua;
//	}
//
//	@Override
//	public List<Booking> selectAll() {
//		List<Booking> list = new ArrayList<>();
//		String sql = "SELECT * FROM booking";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//			rs = pstmt.executeQuery();
//
//			CustomerRepository customerRepo = new CustomerRepository();
//			PetRepository petRepo = new PetRepository();
//			StaffRepository staffRepo = new StaffRepository();
//
//			while (rs.next()) {
//				Customer customer = customerRepo.selectById(rs.getInt("customer_id"));
//				Pet pet = petRepo.selectById(rs.getInt("pet_id"));
//				Staff staff = rs.getObject("staff_id") != null ? staffRepo.selectById(rs.getInt("staff_id")) : null;
//
//				Booking booking = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//						rs.getTimestamp("booking_time").toLocalDateTime(), StatusEnum.valueOf(rs.getString("status")),
//						rs.getString("note"));
//
//				list.add(booking);
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi lấy danh sách booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt, rs);
//		}
//
//		return list;
//	}
//
////    public Booking selectById(int bookingID) {
////        Booking ketQua = null;
////        String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////                + "WHERE b.booking_id = ?";
////
////        try (Connection con = DatabaseConnection.getConnection();
////             PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////            pstmt.setInt(1, bookingID);
////            try (ResultSet rs = pstmt.executeQuery()) {
////                if (rs.next()) {
////                    Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    ketQua = new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    );
////                }
////            }
////
////        } catch (SQLException e) {
////            System.err.println("Lỗi khi tìm booking: " + e.getMessage());
////        }
////
////        return ketQua;
////    }
//	public Booking selectById(int bookingID) {
//		Booking ketQua = null;
//		String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//				+ "c.customer_id, c.point, c.created_at, "
//				+ "p.full_name AS customer_name, p.phone AS customer_phone, p.email AS customer_email, "
//				+ "p.gender AS customer_gender, " + "p.address AS customer_address, "
//				+ "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
//				+ "s.staff_id, s.name AS staff_name, s.phone AS staff_phone " + "FROM booking b "
//				+ "JOIN customer c ON b.customer_id = c.customer_id " + "JOIN pet p ON b.pet_id = p.pet_id "
//				+ "LEFT JOIN staff s ON b.staff_id = s.staff_id " + "WHERE b.booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, bookingID);
//			try (ResultSet rs = pstmt.executeQuery()) {
//				if (rs.next()) {
//					Customer customer = new Customer(rs.getInt("customer_id"), rs.getString("customer_name"),
//							GenderEnum.valueOf(rs.getString("customer_gender")), rs.getString("customer_phone"),
//							rs.getString("customer_address"), rs.getString("customer_email"), rs.getInt("point"),
//							rs.getTimestamp("created_at"));
//					Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
//					Staff staff = rs.getObject("staff_id") != null
//							? new StaffRepository().selectById(rs.getInt("staff_id"))
//							: null;
//
//					ketQua = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//							rs.getTimestamp("booking_time").toLocalDateTime(),
//							StatusEnum.valueOf(rs.getString("status")), rs.getString("note"));
//				}
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi tìm booking: " + e.getMessage());
//		}
//
//		return ketQua;
//	}
//
//	@Override
//	public Booking selectById(Booking t) {
//		return selectById(t.getBookingId());
//	}
////    
////    @Override
////    public List<Booking> selectByCondition(String condition, Object... params) {
////    	List<Booking> bookings = new ArrayList<>();
////
////	    if (condition == null || condition.trim().isEmpty()) {
////	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
////	    }
////
////	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////	            + "WHERE " + condition;
////
////	    try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////	        for (int i = 0; i < params.length; i++) {
////	            pstmt.setObject(i + 1, params[i]);
////	        }
////
////	        try (ResultSet rs = pstmt.executeQuery()) {
////	            while (rs.next()) {
////	            	Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    bookings.add( new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    ));
////	            }
////	        }
////	    } catch (SQLException e) {
////	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
////	    }
////	    return bookings;
////    }
//	@Override
//	public List<Booking> selectByCondition(String condition, Object... params) {
//	    List<Booking> bookings = new ArrayList<>();
//
//	    if (condition == null || condition.trim().isEmpty()) {
//	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
//	    }
//
//	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//	               + "c.customer_id, c.point, c.created_at, "
//	               + "p.pet_id, p.name AS pet_name, p.pet_gender AS pet_gender, p.dob AS pet_dob, p.weight AS pet_weight, p.note AS pet_note, "
//	               + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
//	               + "FROM booking b "
//	               + "JOIN customer c ON b.customer_id = c.customer_id "
//	               + "JOIN pet p ON b.pet_id = p.pet_id "
//	               + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
//	               + "WHERE " + condition;
//
//	    try (Connection con = DatabaseConnection.getConnection(); 
//	         PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//	        for (int i = 0; i < params.length; i++) {
//	            pstmt.setObject(i + 1, params[i]);
//	        }
//
//	        try (ResultSet rs = pstmt.executeQuery()) {
//	            while (rs.next()) {
//	                // Lấy thông tin từ customer
//	                Customer customer = new Customer(
//	                    rs.getInt("customer_id"),
//	                    rs.getString("customer_name"),
//	                    GenderEnum.valueOf(rs.getString("customer_gender")), // Nếu có trường gender
//	                    rs.getString("customer_phone"),
//	                    rs.getString("customer_address"),
//	                    rs.getString("customer_email"),
//	                    rs.getInt("point"),
//	                    rs.getTimestamp("created_at")
//	                );
//
//	                // Lấy thông tin từ pet
//	                Pet pet = new Pet(
//	                    rs.getInt("pet_id"),
//	                    rs.getString("pet_name"),
//	                    PetType.valueOf(rs.getString("pet_species")), // Giả sử species là loại pet
//	                    GenderEnum.valueOf(rs.getString("pet_gender")), // Sử dụng giá trị từ pet_gender
//	                    rs.getDate("pet_dob").toLocalDate(),
//	                    rs.getDouble("pet_weight"),
//	                    rs.getString("pet_note"),
//	                    customer // Gán owner là customer
//	                );
//
//	                // Lấy thông tin từ staff (nếu có)
//	                Staff staff = rs.getObject("staff_id") != null 
//	                    ? new StaffRepository().selectById(rs.getInt("staff_id"))
//	                    : null;
//
//	                // Tạo đối tượng Booking và thêm vào danh sách
//	                bookings.add(new Booking(
//	                    rs.getInt("booking_id"),
//	                    customer,
//	                    pet,
//	                    staff,
//	                    rs.getTimestamp("booking_time").toLocalDateTime(),
//	                    StatusEnum.valueOf(rs.getString("status")),
//	                    rs.getString("note")
//	                ));
//	            }
//	        }
//	    } catch (SQLException e) {
//	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
//	    }
//	    return bookings;
//	}
//
//	public int getMonthlyBookings() {
//		String query = "SELECT COUNT(*) AS total_bookings " + "FROM booking "
//				+ "WHERE MONTH(booking_time) = MONTH(CURRENT_DATE) " + "AND YEAR(booking_time) = YEAR(CURRENT_DATE)";
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			if (resultSet.next()) {
//				return resultSet.getInt("total_bookings");
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return 0; // Return 0 if no data is found or an error occurs
//	}
//
//	public XYChart.Series<String, Number> getBookingData(String timeUnit) {
//		String query = "";
//		if (timeUnit.equals("WEEK")) {
//			query = "SELECT WEEK(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY WEEK(booking_time)";
//		} else if (timeUnit.equals("MONTH")) {
//			query = "SELECT MONTH(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY MONTH(booking_time)";
//		} else if (timeUnit.equals("YEAR")) {
//			query = "SELECT YEAR(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY YEAR(booking_time)";
//		}
//
//		XYChart.Series<String, Number> series = new XYChart.Series<>();
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			while (resultSet.next()) {
//				String time = resultSet.getString("time");
//				int totalBookings = resultSet.getInt("total_bookings");
//				series.getData().add(new XYChart.Data<>(time, totalBookings));
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return series;
//	}
//}
//package repository;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.sql.Types;
//import java.util.ArrayList;
//import java.util.List;
//
//import enums.GenderEnum;
//import enums.StatusEnum;
//import javafx.scene.chart.XYChart;
//import model.Booking;
//import model.Customer;
//import model.Pet;
//import model.PetType;
//import model.Staff;
//import utils.DBUtil;
//import utils.DatabaseConnection;
//
//public class BookingRepository implements IRepository<Booking> {
//
//	private static BookingRepository instance;
//
//	public static BookingRepository getInstance() {
//		if (instance == null) {
//			instance = new BookingRepository();
//		}
//		return instance;
//	}
//
//	@Override
//	public int insert(Booking t) {
//		int ketQua = 0;
//		String sql = "INSERT INTO booking (customer_id, pet_id, staff_id, booking_time, status, note) VALUES (?, ?, ?, ?, ?, ?)";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getCustomer().getId());
//			pstmt.setInt(2, t.getPet().getPetId());
//			pstmt.setInt(3, t.getStaff() != null ? t.getStaff().getId() : Types.NULL); // Cho phép NULL
//			pstmt.setTimestamp(4, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(5, t.getStatus().name());
//			pstmt.setString(6, t.getNote());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("INSERT thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi thêm booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int update(Booking t) {
//		int ketQua = 0;
//		String sql = "UPDATE booking SET staff_id = ?, booking_time = ?, status = ?, note = ? WHERE booking_id = ?";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getStaff() != null ? t.getStaff().getId() : Types.NULL);
//			pstmt.setTimestamp(2, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(3, t.getStatus().name());
//			pstmt.setString(4, t.getNote());
//			pstmt.setInt(5, t.getBookingId());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("UPDATE thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi cập nhật booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int delete(Booking t) {
//		int ketQua = 0;
//		String sql = "DELETE FROM booking WHERE booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, t.getBookingId());
//			ketQua = pstmt.executeUpdate();
//			System.out.println("DELETE thành công, " + ketQua + " dòng bị xóa.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi xóa booking: " + e.getMessage());
//		}
//		return ketQua;
//	}
//
//	@Override
//	public List<Booking> selectAll() {
//		List<Booking> list = new ArrayList<>();
//		String sql = "SELECT * FROM booking";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//			rs = pstmt.executeQuery();
//
//			CustomerRepository customerRepo = new CustomerRepository();
//			PetRepository petRepo = new PetRepository();
//			StaffRepository staffRepo = new StaffRepository();
//
//			while (rs.next()) {
//				Customer customer = customerRepo.selectById(rs.getInt("customer_id"));
//				Pet pet = petRepo.selectById(rs.getInt("pet_id"));
//				Staff staff = rs.getObject("staff_id") != null ? staffRepo.selectById(rs.getInt("staff_id")) : null;
//
//				Booking booking = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//						rs.getTimestamp("booking_time").toLocalDateTime(), StatusEnum.valueOf(rs.getString("status")),
//						rs.getString("note"));
//
//				list.add(booking);
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi lấy danh sách booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt, rs);
//		}
//
//		return list;
//	}
//
////    public Booking selectById(int bookingID) {
////        Booking ketQua = null;
////        String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////                + "WHERE b.booking_id = ?";
////
////        try (Connection con = DatabaseConnection.getConnection();
////             PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////            pstmt.setInt(1, bookingID);
////            try (ResultSet rs = pstmt.executeQuery()) {
////                if (rs.next()) {
////                    Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    ketQua = new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    );
////                }
////            }
////
////        } catch (SQLException e) {
////            System.err.println("Lỗi khi tìm booking: " + e.getMessage());
////        }
////
////        return ketQua;
////    }
//	public Booking selectById(int bookingID) {
//		Booking ketQua = null;
//		String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//				+ "c.customer_id, c.point, c.created_at, "
//				+ "p.full_name AS customer_name, p.phone AS customer_phone, p.email AS customer_email, "
//				+ "p.gender AS customer_gender, " + "p.address AS customer_address, "
//				+ "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
//				+ "s.staff_id, s.name AS staff_name, s.phone AS staff_phone " + "FROM booking b "
//				+ "JOIN customer c ON b.customer_id = c.customer_id " + "JOIN pet p ON b.pet_id = p.pet_id "
//				+ "LEFT JOIN staff s ON b.staff_id = s.staff_id " + "WHERE b.booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, bookingID);
//			try (ResultSet rs = pstmt.executeQuery()) {
//				if (rs.next()) {
//					Customer customer = new Customer(rs.getInt("customer_id"), rs.getString("customer_name"),
//							GenderEnum.valueOf(rs.getString("customer_gender")), rs.getString("customer_phone"),
//							rs.getString("customer_address"), rs.getString("customer_email"), rs.getInt("point"),
//							rs.getTimestamp("created_at"));
//					Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
//					Staff staff = rs.getObject("staff_id") != null
//							? new StaffRepository().selectById(rs.getInt("staff_id"))
//							: null;
//
//					ketQua = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//							rs.getTimestamp("booking_time").toLocalDateTime(),
//							StatusEnum.valueOf(rs.getString("status")), rs.getString("note"));
//				}
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi tìm booking: " + e.getMessage());
//		}
//
//		return ketQua;
//	}
//
//	@Override
//	public Booking selectById(Booking t) {
//		return selectById(t.getBookingId());
//	}
////    
////    @Override
////    public List<Booking> selectByCondition(String condition, Object... params) {
////    	List<Booking> bookings = new ArrayList<>();
////
////	    if (condition == null || condition.trim().isEmpty()) {
////	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
////	    }
////
////	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////	            + "WHERE " + condition;
////
////	    try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////	        for (int i = 0; i < params.length; i++) {
////	            pstmt.setObject(i + 1, params[i]);
////	        }
////
////	        try (ResultSet rs = pstmt.executeQuery()) {
////	            while (rs.next()) {
////	            	Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    bookings.add( new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    ));
////	            }
////	        }
////	    } catch (SQLException e) {
////	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
////	    }
////	    return bookings;
////    }
//	@Override
//	public List<Booking> selectByCondition(String condition, Object... params) {
//	    List<Booking> bookings = new ArrayList<>();
//
//	    if (condition == null || condition.trim().isEmpty()) {
//	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
//	    }
//
//	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//	               + "c.customer_id, c.point, c.created_at, "
//	               + "p.pet_id, p.name AS pet_name, p.pet_gender AS pet_gender, p.dob AS pet_dob, p.weight AS pet_weight, p.note AS pet_note, "
//	               + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
//	               + "FROM booking b "
//	               + "JOIN customer c ON b.customer_id = c.customer_id "
//	               + "JOIN pet p ON b.pet_id = p.pet_id "
//	               + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
//	               + "WHERE " + condition;
//
//	    try (Connection con = DatabaseConnection.getConnection(); 
//	         PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//	        for (int i = 0; i < params.length; i++) {
//	            pstmt.setObject(i + 1, params[i]);
//	        }
//
//	        try (ResultSet rs = pstmt.executeQuery()) {
//	            while (rs.next()) {
//	                // Lấy thông tin từ customer
//	                Customer customer = new Customer(
//	                    rs.getInt("customer_id"),
//	                    rs.getString("customer_name"),
//	                    GenderEnum.valueOf(rs.getString("customer_gender")), // Nếu có trường gender
//	                    rs.getString("customer_phone"),
//	                    rs.getString("customer_address"),
//	                    rs.getString("customer_email"),
//	                    rs.getInt("point"),
//	                    rs.getTimestamp("created_at")
//	                );
//
//	                // Lấy thông tin từ pet
//	                Pet pet = new Pet(
//	                    rs.getInt("pet_id"),
//	                    rs.getString("pet_name"),
//	                    PetType.valueOf(rs.getString("pet_species")), // Giả sử species là loại pet
//	                    GenderEnum.valueOf(rs.getString("pet_gender")), // Sử dụng giá trị từ pet_gender
//	                    rs.getDate("pet_dob").toLocalDate(),
//	                    rs.getDouble("pet_weight"),
//	                    rs.getString("pet_note"),
//	                    customer // Gán owner là customer
//	                );
//
//	                // Lấy thông tin từ staff (nếu có)
//	                Staff staff = rs.getObject("staff_id") != null 
//	                    ? new StaffRepository().selectById(rs.getInt("staff_id"))
//	                    : null;
//
//	                // Tạo đối tượng Booking và thêm vào danh sách
//	                bookings.add(new Booking(
//	                    rs.getInt("booking_id"),
//	                    customer,
//	                    pet,
//	                    staff,
//	                    rs.getTimestamp("booking_time").toLocalDateTime(),
//	                    StatusEnum.valueOf(rs.getString("status")),
//	                    rs.getString("note")
//	                ));
//	            }
//	        }
//	    } catch (SQLException e) {
//	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
//	    }
//	    return bookings;
//	}
//
//	public int getMonthlyBookings() {
//		String query = "SELECT COUNT(*) AS total_bookings " + "FROM booking "
//				+ "WHERE MONTH(booking_time) = MONTH(CURRENT_DATE) " + "AND YEAR(booking_time) = YEAR(CURRENT_DATE)";
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			if (resultSet.next()) {
//				return resultSet.getInt("total_bookings");
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return 0; // Return 0 if no data is found or an error occurs
//	}
//
//	public XYChart.Series<String, Number> getBookingData(String timeUnit) {
//		String query = "";
//		if (timeUnit.equals("WEEK")) {
//			query = "SELECT WEEK(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY WEEK(booking_time)";
//		} else if (timeUnit.equals("MONTH")) {
//			query = "SELECT MONTH(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY MONTH(booking_time)";
//		} else if (timeUnit.equals("YEAR")) {
//			query = "SELECT YEAR(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY YEAR(booking_time)";
//		}
//
//		XYChart.Series<String, Number> series = new XYChart.Series<>();
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			while (resultSet.next()) {
//				String time = resultSet.getString("time");
//				int totalBookings = resultSet.getInt("total_bookings");
//				series.getData().add(new XYChart.Data<>(time, totalBookings));
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return series;
//	}
//}
//package repository;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.sql.Types;
//import java.util.ArrayList;
//import java.util.List;
//
//import enums.GenderEnum;
//import enums.StatusEnum;
//import javafx.scene.chart.XYChart;
//import model.Booking;
//import model.Customer;
//import model.Pet;
//import model.PetType;
//import model.Staff;
//import utils.DBUtil;
//import utils.DatabaseConnection;
//
//public class BookingRepository implements IRepository<Booking> {
//
//	private static BookingRepository instance;
//
//	public static BookingRepository getInstance() {
//		if (instance == null) {
//			instance = new BookingRepository();
//		}
//		return instance;
//	}
//
//	@Override
//	public int insert(Booking t) {
//		int ketQua = 0;
//		String sql = "INSERT INTO booking (customer_id, pet_id, staff_id, booking_time, status, note) VALUES (?, ?, ?, ?, ?, ?)";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getCustomer().getId());
//			pstmt.setInt(2, t.getPet().getPetId());
//			pstmt.setInt(3, t.getStaff() != null ? t.getStaff().getId() : Types.NULL); // Cho phép NULL
//			pstmt.setTimestamp(4, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(5, t.getStatus().name());
//			pstmt.setString(6, t.getNote());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("INSERT thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi thêm booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int update(Booking t) {
//		int ketQua = 0;
//		String sql = "UPDATE booking SET staff_id = ?, booking_time = ?, status = ?, note = ? WHERE booking_id = ?";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//
//			pstmt.setInt(1, t.getStaff() != null ? t.getStaff().getId() : Types.NULL);
//			pstmt.setTimestamp(2, Timestamp.valueOf(t.getBookingTime()));
//			pstmt.setString(3, t.getStatus().name());
//			pstmt.setString(4, t.getNote());
//			pstmt.setInt(5, t.getBookingId());
//
//			ketQua = pstmt.executeUpdate();
//			System.out.println("UPDATE thành công, " + ketQua + " dòng bị thay đổi.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi cập nhật booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt);
//		}
//		return ketQua;
//	}
//
//	@Override
//	public int delete(Booking t) {
//		int ketQua = 0;
//		String sql = "DELETE FROM booking WHERE booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, t.getBookingId());
//			ketQua = pstmt.executeUpdate();
//			System.out.println("DELETE thành công, " + ketQua + " dòng bị xóa.");
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi xóa booking: " + e.getMessage());
//		}
//		return ketQua;
//	}
//
//	@Override
//	public List<Booking> selectAll() {
//		List<Booking> list = new ArrayList<>();
//		String sql = "SELECT * FROM booking";
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//
//		try {
//			con = DatabaseConnection.getConnection();
//			pstmt = con.prepareStatement(sql);
//			rs = pstmt.executeQuery();
//
//			CustomerRepository customerRepo = new CustomerRepository();
//			PetRepository petRepo = new PetRepository();
//			StaffRepository staffRepo = new StaffRepository();
//
//			while (rs.next()) {
//				Customer customer = customerRepo.selectById(rs.getInt("customer_id"));
//				Pet pet = petRepo.selectById(rs.getInt("pet_id"));
//				Staff staff = rs.getObject("staff_id") != null ? staffRepo.selectById(rs.getInt("staff_id")) : null;
//
//				Booking booking = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//						rs.getTimestamp("booking_time").toLocalDateTime(), StatusEnum.valueOf(rs.getString("status")),
//						rs.getString("note"));
//
//				list.add(booking);
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi lấy danh sách booking: " + e.getMessage());
//		} finally {
//			DBUtil.closeResources(con, pstmt, rs);
//		}
//
//		return list;
//	}
//
////    public Booking selectById(int bookingID) {
////        Booking ketQua = null;
////        String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////                + "WHERE b.booking_id = ?";
////
////        try (Connection con = DatabaseConnection.getConnection();
////             PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////            pstmt.setInt(1, bookingID);
////            try (ResultSet rs = pstmt.executeQuery()) {
////                if (rs.next()) {
////                    Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    ketQua = new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    );
////                }
////            }
////
////        } catch (SQLException e) {
////            System.err.println("Lỗi khi tìm booking: " + e.getMessage());
////        }
////
////        return ketQua;
////    }
//	public Booking selectById(int bookingID) {
//		Booking ketQua = null;
//		String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//				+ "c.customer_id, c.point, c.created_at, "
//				+ "p.full_name AS customer_name, p.phone AS customer_phone, p.email AS customer_email, "
//				+ "p.gender AS customer_gender, " + "p.address AS customer_address, "
//				+ "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
//				+ "s.staff_id, s.name AS staff_name, s.phone AS staff_phone " + "FROM booking b "
//				+ "JOIN customer c ON b.customer_id = c.customer_id " + "JOIN pet p ON b.pet_id = p.pet_id "
//				+ "LEFT JOIN staff s ON b.staff_id = s.staff_id " + "WHERE b.booking_id = ?";
//
//		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//			pstmt.setInt(1, bookingID);
//			try (ResultSet rs = pstmt.executeQuery()) {
//				if (rs.next()) {
//					Customer customer = new Customer(rs.getInt("customer_id"), rs.getString("customer_name"),
//							GenderEnum.valueOf(rs.getString("customer_gender")), rs.getString("customer_phone"),
//							rs.getString("customer_address"), rs.getString("customer_email"), rs.getInt("point"),
//							rs.getTimestamp("created_at"));
//					Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
//					Staff staff = rs.getObject("staff_id") != null
//							? new StaffRepository().selectById(rs.getInt("staff_id"))
//							: null;
//
//					ketQua = new Booking(rs.getInt("booking_id"), customer, pet, staff,
//							rs.getTimestamp("booking_time").toLocalDateTime(),
//							StatusEnum.valueOf(rs.getString("status")), rs.getString("note"));
//				}
//			}
//
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi tìm booking: " + e.getMessage());
//		}
//
//		return ketQua;
//	}
//
//	@Override
//	public Booking selectById(Booking t) {
//		return selectById(t.getBookingId());
//	}
////    
////    @Override
////    public List<Booking> selectByCondition(String condition, Object... params) {
////    	List<Booking> bookings = new ArrayList<>();
////
////	    if (condition == null || condition.trim().isEmpty()) {
////	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
////	    }
////
////	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
////                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
////                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
////                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
////                + "FROM booking b "
////                + "JOIN customer c ON b.customer_id = c.customer_id "
////                + "JOIN pet p ON b.pet_id = p.pet_id "
////                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
////	            + "WHERE " + condition;
////
////	    try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
////
////	        for (int i = 0; i < params.length; i++) {
////	            pstmt.setObject(i + 1, params[i]);
////	        }
////
////	        try (ResultSet rs = pstmt.executeQuery()) {
////	            while (rs.next()) {
////	            	Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
////                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
////                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;
////
////                    bookings.add( new Booking(
////                            rs.getInt("booking_id"),
////                            customer,
////                            pet,
////                            staff,
////                            rs.getTimestamp("booking_time").toLocalDateTime(),
////                            StatusEnum.valueOf(rs.getString("status")),
////                            rs.getString("note")
////                    ));
////	            }
////	        }
////	    } catch (SQLException e) {
////	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
////	    }
////	    return bookings;
////    }
//	@Override
//	public List<Booking> selectByCondition(String condition, Object... params) {
//	    List<Booking> bookings = new ArrayList<>();
//
//	    if (condition == null || condition.trim().isEmpty()) {
//	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
//	    }
//
//	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
//	               + "c.customer_id, c.point, c.created_at, "
//	               + "p.pet_id, p.name AS pet_name, p.pet_gender AS pet_gender, p.dob AS pet_dob, p.weight AS pet_weight, p.note AS pet_note, "
//	               + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
//	               + "FROM booking b "
//	               + "JOIN customer c ON b.customer_id = c.customer_id "
//	               + "JOIN pet p ON b.pet_id = p.pet_id "
//	               + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
//	               + "WHERE " + condition;
//
//	    try (Connection con = DatabaseConnection.getConnection(); 
//	         PreparedStatement pstmt = con.prepareStatement(sql)) {
//
//	        for (int i = 0; i < params.length; i++) {
//	            pstmt.setObject(i + 1, params[i]);
//	        }
//
//	        try (ResultSet rs = pstmt.executeQuery()) {
//	            while (rs.next()) {
//	                // Lấy thông tin từ customer
//	                Customer customer = new Customer(
//	                    rs.getInt("customer_id"),
//	                    rs.getString("customer_name"),
//	                    GenderEnum.valueOf(rs.getString("customer_gender")), // Nếu có trường gender
//	                    rs.getString("customer_phone"),
//	                    rs.getString("customer_address"),
//	                    rs.getString("customer_email"),
//	                    rs.getInt("point"),
//	                    rs.getTimestamp("created_at")
//	                );
//
//	                // Lấy thông tin từ pet
//	                Pet pet = new Pet(
//	                    rs.getInt("pet_id"),
//	                    rs.getString("pet_name"),
//	                    PetType.valueOf(rs.getString("pet_species")), // Giả sử species là loại pet
//	                    GenderEnum.valueOf(rs.getString("pet_gender")), // Sử dụng giá trị từ pet_gender
//	                    rs.getDate("pet_dob").toLocalDate(),
//	                    rs.getDouble("pet_weight"),
//	                    rs.getString("pet_note"),
//	                    customer // Gán owner là customer
//	                );
//
//	                // Lấy thông tin từ staff (nếu có)
//	                Staff staff = rs.getObject("staff_id") != null 
//	                    ? new StaffRepository().selectById(rs.getInt("staff_id"))
//	                    : null;
//
//	                // Tạo đối tượng Booking và thêm vào danh sách
//	                bookings.add(new Booking(
//	                    rs.getInt("booking_id"),
//	                    customer,
//	                    pet,
//	                    staff,
//	                    rs.getTimestamp("booking_time").toLocalDateTime(),
//	                    StatusEnum.valueOf(rs.getString("status")),
//	                    rs.getString("note")
//	                ));
//	            }
//	        }
//	    } catch (SQLException e) {
//	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
//	    }
//	    return bookings;
//	}
//
//	public int getMonthlyBookings() {
//		String query = "SELECT COUNT(*) AS total_bookings " + "FROM booking "
//				+ "WHERE MONTH(booking_time) = MONTH(CURRENT_DATE) " + "AND YEAR(booking_time) = YEAR(CURRENT_DATE)";
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			if (resultSet.next()) {
//				return resultSet.getInt("total_bookings");
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return 0; // Return 0 if no data is found or an error occurs
//	}
//
//	public XYChart.Series<String, Number> getBookingData(String timeUnit) {
//		String query = "";
//		if (timeUnit.equals("WEEK")) {
//			query = "SELECT WEEK(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY WEEK(booking_time)";
//		} else if (timeUnit.equals("MONTH")) {
//			query = "SELECT MONTH(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY MONTH(booking_time)";
//		} else if (timeUnit.equals("YEAR")) {
//			query = "SELECT YEAR(booking_time) AS time, COUNT(*) AS total_bookings " + "FROM booking "
//					+ "GROUP BY YEAR(booking_time)";
//		}
//
//		XYChart.Series<String, Number> series = new XYChart.Series<>();
//		try (Connection connection = DatabaseConnection.getConnection();
//				PreparedStatement statement = connection.prepareStatement(query);
//				ResultSet resultSet = statement.executeQuery()) {
//
//			while (resultSet.next()) {
//				String time = resultSet.getString("time");
//				int totalBookings = resultSet.getInt("total_bookings");
//				series.getData().add(new XYChart.Data<>(time, totalBookings));
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return series;
//	}
//}
