
package service;
import model.Staff;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StaffService1 {

    public List<Staff> getStaffByService(int typeServiceID) {
        List<Staff> staffList = new ArrayList<>();
        // Giả định: Lọc nhân viên theo position (cần điều chỉnh nếu có bảng kỹ năng riêng)
        String sql = "SELECT s.PersonID, CONCAT(p.firstName, ' ', p.lastName) AS fullName, s.position " +
                    "FROM staff s JOIN person p ON s.PersonID = p.PersonID " +
                    "WHERE s.position LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Giả định position tương ứng với loại dịch vụ
            String position = switch (typeServiceID) {
                case 1 -> "%Khám%";
                case 2 -> "%Tắm%";
                case 3 -> "%Cắt tỉa%";
                default -> "";
            };
            stmt.setString(1, position);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Staff staff = new Staff(
                    rs.getInt("PersonID"),
                    rs.getString("fullName"),
                    rs.getString("position")
                );
                staffList.add(staff);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return staffList;
    }
}