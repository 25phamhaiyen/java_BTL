package repository;

import model.ShiftAssignment;
import model.ShiftRequest;
import model.Staff;
import model.WorkSchedule;
import service.StaffService;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import enums.RequestStatus;
import enums.RequestType;
import enums.Shift;
import javafx.util.Pair;

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
		String sql = "INSERT INTO work_schedule (staff_id, work_date, shift, start_time, end_time, location, task, note) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

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
		String sql = "UPDATE work_schedule SET staff_id=?, work_date=?, shift=?, start_time=?, end_time=?, "
				+ "location=?, task=?, note=? WHERE schedule_id=?";

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

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

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

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

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

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
			System.out.println("QUERY: " + baseQuery);
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
		String sql = "SELECT ws.schedule_id, ws.work_date, ws.shift, ws.note, ws.staff_id " + "FROM work_schedule ws "
				+ "WHERE ws.work_date >= ? AND ws.work_date <= ?";

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

				// Sử dụng StaffRepository để lấy thông tin đầy đủ của nhân viên (bao gồm cả
				// Role)
				Staff staff = StaffRepository.getInstance().selectById(staffID);

				if (staff != null) {
					WorkSchedule workSchedule = new WorkSchedule(scheduleID, staff, workDate, shift, note);
					workSchedules.add(workSchedule);
				} else {
					System.err
							.println("Không tìm thấy nhân viên với ID: " + staffID + " cho lịch có ID: " + scheduleID);
					// Xử lý trường hợp không tìm thấy nhân viên (có thể bỏ qua bản ghi hoặc log
					// lỗi)
				}
			}
			System.err.println("Truy vấn lịch làm việc theo khoảng ngày thành công. Số lượng: " + workSchedules.size());
		} catch (SQLException e) {
			System.err.println("Lỗi khi truy vấn lịch làm việc theo khoảng ngày: " + e.getMessage());
		}
		return workSchedules;
	}
	
	
	
//	Admin duyệt yêu cầu
	public boolean approveRequest(int requestId, RequestStatus status) {
	    String sql = "UPDATE shift_request SET status = ? WHERE id = ?";
	    try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
	        ps.setString(1, status.name());
	        ps.setInt(2, requestId);
	        return ps.executeUpdate() > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	// Admin xem các yêu cầu chưa duyệt
	public List<ShiftRequest> getPendingRequests() {
	    List<ShiftRequest> requests = new ArrayList<>();
	    String sql = "SELECT * FROM shift_request WHERE status = 'PENDING'";
	    try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
	        while (rs.next()) {
	            ShiftRequest req = new ShiftRequest();
	            req.setId(rs.getInt("id"));
	            int staffId = rs.getInt("staff_id");
	            StaffService staffSer = new StaffService();
	            Staff staff = staffSer.getStaffById(staffId);
	            req.setStaff(staff);
	            req.setRequestDate(rs.getDate("request_date").toLocalDate());
	            req.setShift(Shift.valueOf(rs.getString("shift").toUpperCase()));
	            req.setType(RequestType.valueOf(rs.getString("type")));
	            req.setStatus(RequestStatus.valueOf(rs.getString("status")));
	            req.setReason(rs.getString("reason"));
	            requests.add(req);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return requests;
	}
	
	public Pair<Map<Integer, List<Shift>>, Map<Integer, List<ShiftAssignment>>> getApprovedRequests(LocalDate weekStart) {
	    Map<Integer, List<Shift>> leaveRequests = new HashMap<>();
	    Map<Integer, List<ShiftAssignment>> preferredShifts = new HashMap<>();

	    LocalDate weekEnd = weekStart.plusDays(6);

	    String sql = "SELECT staff_id, request_date, shift, type FROM shift_request "
	               + "WHERE status = 'APPROVED' AND request_date BETWEEN ? AND ?";

	    try (Connection conn = DatabaseConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setDate(1, Date.valueOf(weekStart));
	        stmt.setDate(2, Date.valueOf(weekEnd));
	        ResultSet rs = stmt.executeQuery();

	        while (rs.next()) {
	            int staffId = rs.getInt("staff_id");
	            LocalDate date = rs.getDate("request_date").toLocalDate();
	            Shift shift = Shift.valueOf(rs.getString("shift"));
	            String type = rs.getString("type");

	            if (type.equalsIgnoreCase("LEAVE")) {
	                leaveRequests.computeIfAbsent(staffId, k -> new ArrayList<>()).add(shift);
	            } else if (type.equalsIgnoreCase("WORK")) {
	                preferredShifts.computeIfAbsent(staffId, k -> new ArrayList<>()).add(new ShiftAssignment(date, shift));
	            }
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return new Pair<>(leaveRequests, preferredShifts);
	}



	public boolean assignShift(int staffId, LocalDate date, Shift shift) {
	    String sql = "INSERT IGNORE INTO work_schedule (staff_id, work_date, shift) VALUES (?, ?, ?)";
	    try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
	        ps.setInt(1, staffId);
	        ps.setDate(2, Date.valueOf(date));
	        ps.setString(3, shift.name());
	        return ps.executeUpdate() > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	public boolean insert(ShiftRequest request) throws SQLException {
        String sql = "INSERT INTO shift_request (staff_id, request_date, shift, type, reason) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, request.getStaff().getId());
            ps.setDate(2, Date.valueOf(request.getRequestDate()));
            ps.setString(3, request.getShift().name());
            ps.setString(4, request.getType().name());
            ps.setString(5, request.getReason());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean isDuplicate(Staff staff, LocalDate date, Shift shift) throws SQLException {
        String sql = "SELECT id FROM shift_request WHERE staff_id = ? AND request_date = ? AND shift = ?";
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, staff.getId());
            ps.setDate(2, Date.valueOf(date));
            ps.setString(3, shift.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    public List<ShiftRequest> getRequestsByStaffId(int staffId) {
        List<ShiftRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM shift_request WHERE staff_id = ? ORDER BY request_date DESC";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToShiftRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    private ShiftRequest mapResultSetToShiftRequest(ResultSet rs) throws SQLException {
        ShiftRequest request = new ShiftRequest();

        int staffId = rs.getInt("staff_id");
        StaffService staffService = new StaffService();
        Staff staff = staffService.getStaffById(staffId); // lấy đầy đủ đối tượng Staff

        request.setId(rs.getInt("id"));
        request.setStaff(staff); 

        // Convert chuỗi từ DB sang Enum
        request.setShift(Shift.valueOf(rs.getString("shift")));          
        request.setType(RequestType.valueOf(rs.getString("type")));       
        request.setStatus(RequestStatus.valueOf(rs.getString("status"))); 

        request.setRequestDate(rs.getDate("request_date").toLocalDate());
        request.setReason(rs.getString("reason"));

        return request;
    }

    public ShiftRequest findRequestById(int requestId) {
        String sql = "SELECT * FROM shift_request WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToShiftRequest(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm shift_request theo ID: " + e.getMessage());
        }
        return null;
    }
    
    public boolean deleteWorkSchedule(int staffId, LocalDate date, Shift shift) {
        String sql = "DELETE FROM work_schedule WHERE staff_id = ? AND work_date = ? AND shift = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, staffId);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setString(3, shift.name());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa lịch làm việc: " + e.getMessage());
            return false;
        }
    }

    public boolean insertWorkSchedule(int staffId, LocalDate date, Shift shift, String note) {
        WorkSchedule ws = new WorkSchedule();
        Staff staff = new Staff(); 
        staff.setId(staffId);
        ws.setStaff(staff);
        ws.setWorkDate(date);
        ws.setShift(shift);
        ws.setNote(note);
        ws.setStartTime(null); 
        ws.setEndTime(null);
        ws.setLocation(null);
        ws.setTask(null);
        
        return insert(ws) > 0;
    }



}