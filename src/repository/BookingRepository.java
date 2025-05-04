package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import enums.StatusEnum;
import javafx.scene.chart.XYChart;
import model.Booking;
import model.Customer;
import model.Pet;
import model.Staff;
import utils.DBUtil;
import utils.DatabaseConnection;

public class BookingRepository implements IRepository<Booking> {

    private static BookingRepository instance;

    public static BookingRepository getInstance() {
        if (instance == null) {
            instance = new BookingRepository();
        }
        return instance;
    }

    /**
     * Lấy danh sách booking theo ngày
     * @param date Ngày cần lấy booking
     * @return Danh sách booking trong ngày
     */
    public List<Booking> getBookingsByDate(LocalDate date) {
        String condition = "DATE(booking_time) = ?";
        return selectByCondition(condition, java.sql.Date.valueOf(date));
    }
    
    /**
     * Lấy danh sách booking trong khoảng thời gian
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách booking trong khoảng thời gian
     */
    public List<Booking> getBookingsByDateRange(LocalDate startDate, LocalDate endDate) {
        String condition = "DATE(booking_time) BETWEEN ? AND ?";
        return selectByCondition(condition, java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate));
    }
    
    /**
     * Tìm kiếm booking theo số điện thoại khách hàng
     * @param phone Số điện thoại cần tìm
     * @return Danh sách booking có số điện thoại trùng khớp
     */
    public List<Booking> searchBookingsByPhone(String phone) {
        String condition = "cp.phone LIKE ?";
        return selectByCondition(condition, "%" + phone + "%");
    }

    // Các phương thức đã tồn tại
    @Override
    public int insert(Booking t) {
        int ketQua = 0;
        String sql = "INSERT INTO booking (customer_id, pet_id, staff_id, booking_time, status, note) VALUES (?, ?, ?, ?, ?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setInt(1, t.getCustomer().getId());
            pstmt.setInt(2, t.getPet().getPetId());
            pstmt.setInt(3, t.getStaff() != null ? t.getStaff().getId() : Types.NULL);  // Cho phép NULL
            pstmt.setTimestamp(4, Timestamp.valueOf(t.getBookingTime()));
            pstmt.setString(5, t.getStatus().name());
            pstmt.setString(6, t.getNote());

            ketQua = pstmt.executeUpdate();
            System.out.println("INSERT thành công, " + ketQua + " dòng bị thay đổi.");

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm booking: " + e.getMessage());
        } finally {
            DBUtil.closeResources(con, pstmt);
        }
        return ketQua;
    }

    @Override
    public int update(Booking t) {
        int ketQua = 0;
        String sql = "UPDATE booking SET staff_id = ?, booking_time = ?, status = ?, note = ? WHERE booking_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setInt(1, t.getStaff() != null ? t.getStaff().getId() : Types.NULL);
            pstmt.setTimestamp(2, Timestamp.valueOf(t.getBookingTime()));
            pstmt.setString(3, t.getStatus().name());
            pstmt.setString(4, t.getNote());
            pstmt.setInt(5, t.getBookingId());

            ketQua = pstmt.executeUpdate();
            System.out.println("UPDATE thành công, " + ketQua + " dòng bị thay đổi.");

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật booking: " + e.getMessage());
        } finally {
            DBUtil.closeResources(con, pstmt);
        }
        return ketQua;
    }


    @Override
    public int delete(Booking t) {
        int ketQua = 0;
        String sql = "DELETE FROM booking WHERE booking_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, t.getBookingId());
            ketQua = pstmt.executeUpdate();
            System.out.println("DELETE thành công, " + ketQua + " dòng bị xóa.");

        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa booking: " + e.getMessage());
        }
        return ketQua;
    }

    @Override
    public List<Booking> selectAll() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM booking";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

            CustomerRepository customerRepo = new CustomerRepository();
            PetRepository petRepo = new PetRepository();
            StaffRepository staffRepo = new StaffRepository();

            while (rs.next()) {
                Customer customer = customerRepo.selectById(rs.getInt("customer_id"));
                Pet pet = petRepo.selectById(rs.getInt("pet_id"));
                Staff staff = rs.getObject("staff_id") != null ? staffRepo.selectById(rs.getInt("staff_id")) : null;

                Booking booking = new Booking(
                        rs.getInt("booking_id"),
                        customer,
                        pet,
                        staff,
                        rs.getTimestamp("booking_time").toLocalDateTime(),
                        StatusEnum.valueOf(rs.getString("status")),
                        rs.getString("note")
                );

                list.add(booking);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách booking: " + e.getMessage());
        } finally {
            DBUtil.closeResources(con, pstmt, rs);
        }

        return list;
    }
    public Booking getNewestBookingByPhone(String phone) {
        String condition = "cp.phone = ? ORDER BY b.booking_id DESC LIMIT 1";
        List<Booking> bookings = selectByCondition(condition, phone);
        return bookings.isEmpty() ? null : bookings.get(0);
    }
    public Booking selectById(int bookingID) {
        Booking ketQua = null;
        String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, " +
                     "c.customer_id, cp.full_name AS customer_name, cp.phone AS customer_phone, cp.email AS customer_email, " +
                     "p.pet_id, p.name AS pet_name, pt.species AS pet_species, pt.breed AS pet_breed, " +
                     "s.staff_id, sp.full_name AS staff_name, sp.phone AS staff_phone " +
                     "FROM booking b " +
                     "JOIN customer c ON b.customer_id = c.customer_id " +
                     "JOIN person cp ON c.customer_id = cp.person_id " +
                     "JOIN pet p ON b.pet_id = p.pet_id " +
                     "JOIN pet_type pt ON p.type_id = pt.type_id " +
                     "LEFT JOIN staff s ON b.staff_id = s.staff_id " +
                     "LEFT JOIN person sp ON s.staff_id = sp.person_id " +
                     "WHERE b.booking_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, bookingID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;

                    ketQua = new Booking(
                            rs.getInt("booking_id"),
                            customer,
                            pet,
                            staff,
                            rs.getTimestamp("booking_time").toLocalDateTime(),
                            StatusEnum.valueOf(rs.getString("status")),
                            rs.getString("note")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm booking: " + e.getMessage());
        }

        return ketQua;
    }

    @Override
    public Booking selectById(Booking t) {
        return selectById(t.getBookingId());
    }

    @Override
    public List<Booking> selectByCondition(String condition, Object... params) {
        List<Booking> bookings = new ArrayList<>();

        if (condition == null || condition.trim().isEmpty()) {
            throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
        }

        String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, " +
                     "c.customer_id, cp.full_name AS customer_name, cp.phone AS customer_phone, cp.email AS customer_email, " +
                     "p.pet_id, p.name AS pet_name, pt.species AS pet_species, pt.breed AS pet_breed, " +
                     "s.staff_id, sp.full_name AS staff_name, sp.phone AS staff_phone " +
                     "FROM booking b " +
                     "JOIN customer c ON b.customer_id = c.customer_id " +
                     "JOIN person cp ON c.customer_id = cp.person_id " +
                     "JOIN pet p ON b.pet_id = p.pet_id " +
                     "JOIN pet_type pt ON p.type_id = pt.type_id " +
                     "LEFT JOIN staff s ON b.staff_id = s.staff_id " +
                     "LEFT JOIN person sp ON s.staff_id = sp.person_id " +
                     "WHERE " + condition;

        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;

                    bookings.add(new Booking(
                            rs.getInt("booking_id"),
                            customer,
                            pet,
                            staff,
                            rs.getTimestamp("booking_time").toLocalDateTime(),
                            StatusEnum.valueOf(rs.getString("status")),
                            rs.getString("note")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
        }
        return bookings;
    }

    public int getMonthlyBookings() {
        String query = "SELECT COUNT(*) AS total_bookings " +
                       "FROM booking " +
                       "WHERE MONTH(booking_time) = MONTH(CURRENT_DATE) " +
                       "AND YEAR(booking_time) = YEAR(CURRENT_DATE)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt("total_bookings");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Return 0 if no data is found or an error occurs
    }

    public XYChart.Series<String, Number> getBookingData(String timeUnit) {
        String query = "";
        if (timeUnit.equals("WEEK")) {
            query = "SELECT WEEK(booking_time) AS time, COUNT(*) AS total_bookings " +
                    "FROM booking " +
                    "GROUP BY WEEK(booking_time)";
        } else if (timeUnit.equals("MONTH")) {
            query = "SELECT MONTH(booking_time) AS time, COUNT(*) AS total_bookings " +
                    "FROM booking " +
                    "GROUP BY MONTH(booking_time)";
        } else if (timeUnit.equals("YEAR")) {
            query = "SELECT YEAR(booking_time) AS time, COUNT(*) AS total_bookings " +
                    "FROM booking " +
                    "GROUP BY YEAR(booking_time)";
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String time = resultSet.getString("time");
                int totalBookings = resultSet.getInt("total_bookings");
                series.getData().add(new XYChart.Data<>(time, totalBookings));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return series;
    }
}