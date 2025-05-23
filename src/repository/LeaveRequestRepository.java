package repository;

import model.LeaveRequest;
import model.Staff;
import enums.RequestStatus;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LeaveRequestRepository implements IRepository<LeaveRequest> {
	private static LeaveRequestRepository instance;

	public static LeaveRequestRepository getInstance() {
		if (instance == null) {
			instance = new LeaveRequestRepository();
		}
		return instance;
	}

	@Override
	public int insert(LeaveRequest leaveRequest) {
		String sql = "INSERT INTO leave_requests (staff_id, leave_date, reason, status) VALUES (?, ?, ?, ?)";

		try (Connection con = DatabaseConnection.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setInt(1, leaveRequest.getStaff().getId());
			pstmt.setDate(2, Date.valueOf(leaveRequest.getLeaveDate()));
			pstmt.setString(3, leaveRequest.getReason());
			pstmt.setString(4, leaveRequest.getStatus().name());

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				try (ResultSet rs = pstmt.getGeneratedKeys()) {
					if (rs.next()) {
						leaveRequest.setLeaveRequestId(rs.getInt(1));
					}
				}
			}
			return affectedRows;
		} catch (SQLException e) {
			System.err.println("Lỗi khi thêm yêu cầu nghỉ phép: " + e.getMessage());
			return 0;
		}
	}

	@Override
	public int update(LeaveRequest leaveRequest) {
		String sql = "UPDATE leave_requests SET status = ?, approved_by = ?, response_date = ?, note = ? WHERE leave_request_id = ?";

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setString(1, leaveRequest.getStatus().name());
			if (leaveRequest.getApprovedBy() != null) {
				pstmt.setInt(2, leaveRequest.getApprovedBy().getId());
			} else {
				pstmt.setNull(2, Types.INTEGER);
			}
			pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			pstmt.setString(4, leaveRequest.getNote());
			pstmt.setInt(5, leaveRequest.getLeaveRequestId());

			return pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Lỗi khi cập nhật yêu cầu nghỉ phép: " + e.getMessage());
			return 0;
		}
	}

	@Override
	public int delete(LeaveRequest leaveRequest) {
		String sql = "DELETE FROM leave_requests WHERE leave_request_id = ?";

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, leaveRequest.getLeaveRequestId());
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Lỗi khi xóa yêu cầu nghỉ phép: " + e.getMessage());
			return 0;
		}
	}

	@Override
	public List<LeaveRequest> selectAll() {
		List<LeaveRequest> list = new ArrayList<>();
		String sql = "SELECT lr.*, s.full_name as staff_name, a.full_name as approver_name " + "FROM leave_requests lr "
				+ "JOIN person p ON lr.staff_id = p.person_id " + "JOIN person s ON lr.staff_id = s.person_id "
				+ "LEFT JOIN person a ON lr.approved_by = a.person_id " + "ORDER BY lr.request_date DESC";

		try (Connection con = DatabaseConnection.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			StaffRepository staffRepo = StaffRepository.getInstance();

			while (rs.next()) {
				Staff staff = staffRepo.selectById(rs.getInt("staff_id"));
				Staff approvedBy = null;
				int approvedById = rs.getInt("approved_by");
				if (!rs.wasNull() && approvedById > 0) {
					approvedBy = staffRepo.selectById(approvedById);
				}

				LeaveRequest leaveRequest = new LeaveRequest(rs.getInt("leave_request_id"), staff,
						rs.getDate("leave_date").toLocalDate(), rs.getString("reason"),
						RequestStatus.valueOf(rs.getString("status")), approvedBy, rs.getTimestamp("request_date"),
						rs.getTimestamp("response_date"), rs.getString("note"));

				list.add(leaveRequest);
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy danh sách yêu cầu nghỉ phép: " + e.getMessage());
		}
		return list;
	}

	@Override
	public LeaveRequest selectById(LeaveRequest t) {
		return selectById(t.getLeaveRequestId());
	}

	public LeaveRequest selectById(int id) {
		String sql = "SELECT * FROM leave_requests WHERE leave_request_id = ?";

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, id);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToLeaveRequest(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy yêu cầu nghỉ phép: " + e.getMessage());
		}
		return null;
	}

	@Override
	public List<LeaveRequest> selectByCondition(String condition, Object... params) {
		List<LeaveRequest> list = new ArrayList<>();
		String sql = "SELECT * FROM leave_requests WHERE " + condition;

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			for (int i = 0; i < params.length; i++) {
				pstmt.setObject(i + 1, params[i]);
			}

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					list.add(mapResultSetToLeaveRequest(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi truy vấn yêu cầu nghỉ phép: " + e.getMessage());
		}
		return list;
	}

	private LeaveRequest mapResultSetToLeaveRequest(ResultSet rs) throws SQLException {
		StaffRepository staffRepo = StaffRepository.getInstance();

		Staff staff = staffRepo.selectById(rs.getInt("staff_id"));
		Staff approvedBy = null;
		int approvedById = rs.getInt("approved_by");
		if (!rs.wasNull() && approvedById > 0) {
			approvedBy = staffRepo.selectById(approvedById);
		}

		return new LeaveRequest(rs.getInt("leave_request_id"), staff, rs.getDate("leave_date").toLocalDate(),
				rs.getString("reason"), RequestStatus.valueOf(rs.getString("status")), approvedBy,
				rs.getTimestamp("request_date"), rs.getTimestamp("response_date"), rs.getString("note"));
	}
}