package repository;

import model.Staff;
import model.WorkSchedule;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
    
    public List<WorkSchedule> selectByDateRange(LocalDate startDate, LocalDate endDate) {
        List<WorkSchedule> workSchedules = new ArrayList<>();
        String sql = "SELECT ws.schedule_id, ws.work_date, ws.shift, ws.note, ws.staff_id " +
                     "FROM work_schedule ws " +
                     "WHERE ws.work_date >= ? AND ws.work_date <= ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setDate(1, java.sql.Date.valueOf(startDate));
            preparedStatement.setDate(2, java.sql.Date.valueOf(endDate));
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int scheduleID = resultSet.getInt("schedule_id");
                LocalDate workDate = resultSet.getDate("work_date").toLocalDate();
                Shift shift = Shift.valueOf(resultSet.getString("shift").toUpperCase());
                String note = resultSet.getString("note");
                int staffID = resultSet.getInt("staff_id");

                // Sử dụng StaffRepository để lấy thông tin đầy đủ của nhân viên (bao gồm cả Role)
                Staff staff = StaffRepository.getInstance().selectById(staffID);

                if (staff != null) {
                    WorkSchedule workSchedule = new WorkSchedule(scheduleID, staff, workDate, shift, note);
                    workSchedules.add(workSchedule);
                } else {
                    System.err.println("Không tìm thấy nhân viên với ID: " + staffID + " cho lịch có ID: " + scheduleID);
                    // Xử lý trường hợp không tìm thấy nhân viên (có thể bỏ qua bản ghi hoặc log lỗi)
                }
            }
            System.err.println("Truy vấn lịch làm việc theo khoảng ngày thành công. Số lượng: " + workSchedules.size());
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn lịch làm việc theo khoảng ngày: " + e.getMessage());
        }
        return workSchedules;
    }

}
