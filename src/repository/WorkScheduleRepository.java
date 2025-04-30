package repository;

import model.Staff;
import model.WorkSchedule;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import enums.Shift;

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
        String sql = "INSERT INTO work_schedule (staff_id, work_date, shift, start_time, end_time, location, task, note) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, workSchedule.getStaff().getId());
            pstmt.setDate(2, java.sql.Date.valueOf(workSchedule.getWorkDate()));
            pstmt.setString(3, workSchedule.getShift().name());
            pstmt.setTime(4, workSchedule.getStartTime() != null ? Time.valueOf(workSchedule.getStartTime()) : null);
            pstmt.setTime(5, workSchedule.getEndTime() != null ? Time.valueOf(workSchedule.getEndTime()) : null);
            pstmt.setString(6, workSchedule.getLocation());
            pstmt.setString(7, workSchedule.getTask());
            pstmt.setString(8, workSchedule.getNote());

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
        String sql = "UPDATE work_schedule SET staff_id=?, work_date=?, shift=?, start_time=?, end_time=?, " +
                     "location=?, task=?, note=? WHERE schedule_id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, workSchedule.getStaff().getId());
            pstmt.setDate(2, java.sql.Date.valueOf(workSchedule.getWorkDate()));
            pstmt.setString(3, workSchedule.getShift().name());
            pstmt.setTime(4, workSchedule.getStartTime() != null ? Time.valueOf(workSchedule.getStartTime()) : null);
            pstmt.setTime(5, workSchedule.getEndTime() != null ? Time.valueOf(workSchedule.getEndTime()) : null);
            pstmt.setString(6, workSchedule.getLocation());
            pstmt.setString(7, workSchedule.getTask());
            pstmt.setString(8, workSchedule.getNote());
            pstmt.setInt(9, workSchedule.getScheduleID());

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật lịch làm việc: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int delete(WorkSchedule workSchedule) {
        String sql = "DELETE FROM work_schedule WHERE schedule_id=?";

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
        String sql = "SELECT * FROM work_schedule WHERE schedule_id=?";

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
        String baseQuery = "SELECT * FROM work_schedule WHERE " + whereClause;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(baseQuery)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToWorkSchedule(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn WorkSchedule theo điều kiện: " + e.getMessage());
        }

        return list;
    }

    private WorkSchedule mapResultSetToWorkSchedule(ResultSet rs) throws SQLException {
        int scheduleID = rs.getInt("schedule_id");
        int staffID = rs.getInt("staff_id");
        LocalDate workDate = rs.getDate("work_date").toLocalDate();
        Shift shift = Shift.valueOf(rs.getString("shift"));
        LocalTime startTime = rs.getTime("start_time") != null ? rs.getTime("start_time").toLocalTime() : null;
        LocalTime endTime = rs.getTime("end_time") != null ? rs.getTime("end_time").toLocalTime() : null;
        String location = rs.getString("location");
        String task = rs.getString("task");
        String note = rs.getString("note");

        Staff staff = StaffRepository.getInstance().selectById(staffID);

        return new WorkSchedule(scheduleID, staff, workDate, shift, startTime, endTime, location, task, note);
    }
}