package repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import enums.GenderEnum;
import exception.BusinessException;
import javafx.scene.chart.XYChart;
import model.Customer;
import utils.DBUtil;
import utils.DatabaseConnection;

public class CustomerRepository implements IRepository<Customer> {

    public static CustomerRepository getInstance() {
        return new CustomerRepository();
    }

    @Override
    public int insert(Customer t) {
        int ketQua = 0;

        String insertPersonSql = "INSERT INTO person (full_name, gender, phone, address, email) VALUES (?, ?, ?, ?, ?)";
        String customerSql = "INSERT INTO customer (customer_id, point) VALUES (?, ?)";

        Connection con = null;
        PreparedStatement personPstmt = null;
        PreparedStatement customerPstmt = null;
        ResultSet personRs = null;

        try {
            con = DatabaseConnection.getConnection();
            con.setAutoCommit(false); // Bắt đầu transaction

            // 1. Thêm vào bảng person
            personPstmt = con.prepareStatement(insertPersonSql, Statement.RETURN_GENERATED_KEYS);
            personPstmt.setString(1, t.getFullName());
            personPstmt.setString(2, t.getGender().getDescription());
            personPstmt.setString(3, t.getPhone());
            personPstmt.setString(4, t.getAddress());
            personPstmt.setString(5, t.getEmail());

            int personRowsAffected = personPstmt.executeUpdate();
            if (personRowsAffected > 0) {
                personRs = personPstmt.getGeneratedKeys();
                if (personRs.next()) {
                    int personID = personRs.getInt(1);

                    // 2. Thêm vào bảng customer
                    customerPstmt = con.prepareStatement(customerSql);
                    customerPstmt.setInt(1, personID);
                    customerPstmt.setInt(2, t.getPoint());

                    ketQua = customerPstmt.executeUpdate();

                    if (ketQua > 0) {
                        t.setId(personID);
                    }

                    con.commit(); // Commit toàn bộ
                    System.out.println("INSERT thành công, " + ketQua + " dòng bị thay đổi.");
                }
            }

        } catch (SQLException e) {
            try {
                if (con != null) con.rollback(); // Rollback nếu lỗi
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            throw new BusinessException("Lỗi khi thêm khách hàng: " + e.getMessage());

        } finally {
            DBUtil.closeResources(null, customerPstmt, null);
            DBUtil.closeResources(null, personPstmt, personRs);
            DatabaseConnection.closeConnection(con);
        }

        return ketQua;
    }

    @Override
    public int update(Customer t) {
        int ketQua = 0;
        String updatePersonSql = "UPDATE person SET full_name=?, gender=?, phone=?, address=?, email=? WHERE person_id=?";
        String updateCustomerSql = "UPDATE customer SET point = ? WHERE customer_id = ?";

        Connection con = null;

        try {
            con = DatabaseConnection.getConnection();
            con.setAutoCommit(false);

            // 1. Update the 'person' table
            try (PreparedStatement pstmt = con.prepareStatement(updatePersonSql)) {
                pstmt.setString(1, t.getFullName());
                pstmt.setString(2, t.getGender().getDescription());
                pstmt.setString(3, t.getPhone());
                pstmt.setString(4, t.getAddress());
                pstmt.setString(5, t.getEmail());
                pstmt.setInt(6, t.getId());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Person update thành công.");
                }
            }

            // 2. Update the 'customer' table
            try (PreparedStatement pstmt = con.prepareStatement(updateCustomerSql)) {
                pstmt.setInt(1, t.getPoint());
                pstmt.setInt(2, t.getId());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Customer update thành công.");
                }
            }

            con.commit();
            ketQua = 1;
        } catch (SQLException e) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException rollbackEx) {
                throw new BusinessException("Lỗi khi rollback giao dịch: " + rollbackEx.getMessage());
            }
            if (e.getMessage().contains("Duplicate entry")) {
                throw new BusinessException("Email hoặc số điện thoại đã tồn tại.");
            }
            throw new BusinessException("Lỗi SQL khi cập nhật khách hàng: " + e.getMessage());
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException e) {
                throw new BusinessException("Lỗi khi reset auto-commit: " + e.getMessage());
            }
            DatabaseConnection.closeConnection(con);
        }

        return ketQua;
    }

    @Override
    public int delete(Customer t) {
        int ketQua = 0;
        String deleteCustomerSql = "DELETE FROM customer WHERE customer_id=?";
        String deletePersonSql = "DELETE FROM person WHERE person_id=?";

        Connection con = null;

        try {
            con = DatabaseConnection.getConnection();
            con.setAutoCommit(false);

            // 1. Delete from customer table
            try (PreparedStatement pstmt = con.prepareStatement(deleteCustomerSql)) {
                pstmt.setInt(1, t.getId());
                ketQua = pstmt.executeUpdate();
                System.out.println("DELETE from customer thành công, " + ketQua + " dòng bị thay đổi.");
            }

            // 2. Delete from person table
            try (PreparedStatement pstmt = con.prepareStatement(deletePersonSql)) {
                pstmt.setInt(1, t.getId());
                int personDeleteCount = pstmt.executeUpdate();
                System.out.println("DELETE from person thành công, " + personDeleteCount + " dòng bị thay đổi.");
            }

            con.commit();
        } catch (SQLException e) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Lỗi khi rollback giao dịch: " + rollbackEx.getMessage());
            }
            System.err.println("Lỗi khi xóa khách hàng: " + e.getMessage());
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("Lỗi khi reset auto-commit: " + e.getMessage());
            }
            DBUtil.closeResources(con, null);
        }

        return ketQua;
    }

    @Override
    public List<Customer> selectAll() {
        List<Customer> ketQua = new ArrayList<>();
        String sql = "SELECT p.*, c.point, c.created_at " +
                     "FROM customer c " +
                     "JOIN person p ON c.customer_id = p.person_id";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ketQua.add(getCustomerFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách khách hàng: " + e.getMessage());
        } finally {
            DBUtil.closeResources(con, pstmt, rs);
        }
        return ketQua;
    }

    @Override
    public Customer selectById(Customer t) {
        return selectById(t.getId());
    }

    public Customer selectById(int customerID) {
        Customer ketQua = null;
        String sql = "SELECT p.*, c.point, c.created_at " +
                     "FROM customer c " +
                     "JOIN person p ON c.customer_id = p.person_id " +
                     "WHERE c.customer_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, customerID);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                ketQua = getCustomerFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm khách hàng theo ID: " + e.getMessage());
        } finally {
            DBUtil.closeResources(con, pstmt, rs);
        }
        return ketQua;
    }

    @Override
    public List<Customer> selectByCondition(String condition, Object... params) {
        List<Customer> customers = new ArrayList<>();

        if (condition == null || condition.trim().isEmpty()) {
            throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
        }

        String sql = "SELECT p.*, c.point, c.created_at " +
                     "FROM customer c " +
                     "JOIN person p ON c.customer_id = p.person_id " +
                     "WHERE " + condition;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(getCustomerFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn Customer theo điều kiện: " + e.getMessage());
        }
        return customers;
    }

    public Customer findByPhone(String phone) {
        Customer ketQua = null;
        String sql = "SELECT p.*, c.point, c.created_at " +
                     "FROM customer c " +
                     "JOIN person p ON c.customer_id = p.person_id " +
                     "WHERE p.phone = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, phone);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                ketQua = getCustomerFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm khách hàng theo số điện thoại: " + e.getMessage());
        } finally {
            DBUtil.closeResources(con, pstmt, rs);
        }
        return ketQua;
    }

    public void deleteAll(Connection conn) throws SQLException {
        String sql = "DELETE FROM customer";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }

    public void resetAutoIncrement(Connection conn) throws SQLException {
        String sql = "ALTER TABLE customer AUTO_INCREMENT = 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        }
    }

    private Customer getCustomerFromResultSet(ResultSet rs) throws SQLException {
        int personID = rs.getInt("person_id");
        String fullName = rs.getString("full_name");
        GenderEnum gender = GenderEnum.valueOf(rs.getString("gender"));
        String phoneNumber = rs.getString("phone");
        String address = rs.getString("address");
        String email = rs.getString("email");
        int point = rs.getInt("point");
        Timestamp created_at = rs.getTimestamp("created_at");

        return new Customer(personID, fullName, gender, phoneNumber, address, email, point, created_at);
    }

 // Lấy tổng số khách hàng mới theo từng đơn vị thời gian (WEEK, MONTH, YEAR)
    public int getTotalNewCustomers(String timeUnit) {
        String query = "";
        
        if (timeUnit.equals("WEEK")) {
            // Truy vấn cho WEEK
            query = "SELECT COUNT(*) AS total_new_customers " +
                    "FROM customer " +
                    "WHERE WEEK(created_at) = WEEK(CURRENT_DATE) " +
                    "AND YEAR(created_at) = YEAR(CURRENT_DATE)";
        } else if (timeUnit.equals("MONTH")) {
            // Truy vấn cho MONTH
            query = "SELECT COUNT(*) AS total_new_customers " +
                    "FROM customer " +
                    "WHERE MONTH(created_at) = MONTH(CURRENT_DATE) " +
                    "AND YEAR(created_at) = YEAR(CURRENT_DATE)";
        } else if (timeUnit.equals("YEAR")) {
            // Truy vấn cho YEAR
            query = "SELECT COUNT(*) AS total_new_customers " +
                    "FROM customer " +
                    "WHERE YEAR(created_at) = YEAR(CURRENT_DATE)";
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt("total_new_customers");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


 // Lấy dữ liệu khách hàng theo từng đơn vị thời gian (WEEK, MONTH, YEAR)
 // Lấy dữ liệu khách hàng theo từng đơn vị thời gian (WEEK, MONTH, YEAR)
    public XYChart.Series<String, Number> getCustomerData(String timeUnit) {
        String query = "";

        if (timeUnit.equals("WEEK")) {
            // Lấy 4 tuần gần nhất
            query = "SELECT"
                    + "    CONCAT(DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL (n - 1) WEEK), '%d/%m'), ' - ', "
                    + "           DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL (n - 1) WEEK) + INTERVAL 6 DAY, '%d/%m')) AS label, "
                    + "    COUNT(c.customer_id) AS total_customers \r\n"
                    + "FROM \r\n"
                    + "    (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) AS weeks \r\n"
                    + "LEFT JOIN  \r\n"
                    + "    customer c \r\n"
                    + "ON \r\n"
                    + "    YEARWEEK(c.created_at, 1) = YEARWEEK(DATE_SUB(CURDATE(), INTERVAL (weeks.n - 1) WEEK), 1) \r\n"
                    + "GROUP BY label \r\n"
                    + "ORDER BY MIN(DATE_SUB(CURDATE(), INTERVAL (weeks.n - 1) WEEK))\r\n";
        }
        else if (timeUnit.equals("MONTH")) {
            // Lấy tháng từ 1 đến tháng hiện tại của năm hiện tại
            query = "SELECT YEAR(created_at) AS year, MONTH(created_at) AS month, COUNT(*) AS total_customers " +
                    "FROM customer " +
                    "WHERE YEAR(created_at) = YEAR(CURRENT_DATE) " +
                    "GROUP BY YEAR(created_at), MONTH(created_at) " +
                    "ORDER BY YEAR(created_at), MONTH(created_at)";

        } else if (timeUnit.equals("YEAR")) {
            // Lấy 4 năm gần nhất
            query = "SELECT YEAR(created_at) AS year, COUNT(*) AS total_customers " +
                    "FROM customer " +
                    "WHERE YEAR(created_at) >= YEAR(CURRENT_DATE) - 4 " +
                    "GROUP BY YEAR(created_at) " +
                    "ORDER BY YEAR(created_at)";
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String time = "";
                int totalCustomers = resultSet.getInt("total_customers");

                if (timeUnit.equals("WEEK")) {
                    time = resultSet.getString("label");
                } else if (timeUnit.equals("MONTH")) {
                    int year = resultSet.getInt("year");
                    int month = resultSet.getInt("month");
                    time = String.format("%02d/%d", month, year);
                } else if (timeUnit.equals("YEAR")) {
                    time = String.valueOf(resultSet.getInt("year"));
                }
                series.getData().add(new XYChart.Data<>(time, totalCustomers));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return series;
    }

    public int getOrderCountByCustomerId(int customerId) {
        String sql = "SELECT COUNT(*) FROM `order` WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);  // Trả về số lượng đơn hàng
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;  // Nếu không tìm thấy, trả về 0
    }

    // Lấy tổng số khách hàng theo từng đơn vị thời gian (WEEK, MONTH, YEAR)
    public int getTotalCustomers(String timeUnit) {
        String procedureName = switch (timeUnit) {
            case "WEEK" -> "sp_get_total_customers_week";
            case "MONTH" -> "sp_get_total_customers_month";
            case "YEAR" -> "sp_get_total_customers_year";
            default -> null;
        };

        if (procedureName == null) return 0;

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL " + procedureName + "()}");
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }


}