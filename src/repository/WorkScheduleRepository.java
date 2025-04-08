package repository;


import model.WorkSchedule;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkScheduleRepository implements IRepository<WorkSchedule> {

    private static WorkScheduleRepository instance;

    public static WorkScheduleRepository getInstance() {
        if (instance == null) {
            synchronized (WorkScheduleRepository.class) {
                if (instance == null) {
                    instance = new WorkScheduleRepository();
                }
            }
        }
        return instance;
    }

    @Override
    public int insert(WorkSchedule workSchedule) {
        String sql = "INSERT INTO work_schedule (staffID, workDate, shift, note) VALUES (?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, workSchedule.getStaffID());
            pstmt.setDate(2, new java.sql.Date(workSchedule.getWorkDate().getTime()));
            pstmt.setString(3, workSchedule.getShift());
            pstmt.setString(4, workSchedule.getNote());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        workSchedule.setScheduleID(rs.getInt(1));
                    }
                }
            }
            return affectedRows;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm lịch làm việc: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int update(WorkSchedule workSchedule) {
        String sql = "UPDATE work_schedule SET staffID=?, workDate=?, shift=?, note=? WHERE scheduleID=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, workSchedule.getStaffID());
            pstmt.setDate(2, new java.sql.Date(workSchedule.getWorkDate().getTime()));
            pstmt.setString(3, workSchedule.getShift());
            pstmt.setString(4, workSchedule.getNote());
            pstmt.setInt(5, workSchedule.getScheduleID());

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật lịch làm việc: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int delete(WorkSchedule workSchedule) {
        String sql = "DELETE FROM work_schedule WHERE scheduleID=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, workSchedule.getScheduleID());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa lịch làm việc: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<WorkSchedule> selectAll() {
        List<WorkSchedule> list = new ArrayList<>();
        String sql = "SELECT * FROM work_schedule";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToWorkSchedule(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách lịch làm việc: " + e.getMessage());
        }
        return list;
    }

    @Override
    public WorkSchedule selectById(WorkSchedule workSchedule) {
        return selectById(workSchedule.getScheduleID());
    }

    public WorkSchedule selectById(int scheduleID) {
        String sql = "SELECT * FROM work_schedule WHERE scheduleID=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, scheduleID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToWorkSchedule(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm lịch làm việc theo ID: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public List<WorkSchedule> selectByCondition(String whereClause, Object... params) {
        List<WorkSchedule> list = new ArrayList<>();
        String baseQuery = "SELECT * FROM work_schedule WHERE " + whereClause;  // Thay đổi "work_schedule" thành tên bảng thực tế của bạn.

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(baseQuery)) {

            // Đặt tham số động
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToWorkSchedule(rs)); // Triển khai phương thức này để ánh xạ kết quả thành đối tượng WorkSchedule
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn WorkSchedule theo điều kiện: " + e.getMessage());
        }

        return list;
    }


    private WorkSchedule mapResultSetToWorkSchedule(ResultSet rs) throws SQLException {
        int scheduleID = rs.getInt("scheduleID");
        int staffID = rs.getInt("staffID");
        Date workDate = rs.getDate("workDate");
        String shift = rs.getString("shift");
        String note = rs.getString("note");

        return new WorkSchedule(scheduleID, staffID, workDate, shift, note);
    }

}
