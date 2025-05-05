package repository;

import model.ShiftRegistrationRequest;
import model.Staff;
import enums.Shift;
import enums.RequestStatus;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ShiftRegistrationRequestRepository implements IRepository<ShiftRegistrationRequest> {
    private static ShiftRegistrationRequestRepository instance;

    public static ShiftRegistrationRequestRepository getInstance() {
        if (instance == null) {
            instance = new ShiftRegistrationRequestRepository();
        }
        return instance;
    }

    @Override
    public int insert(ShiftRegistrationRequest request) {
        String sql = "INSERT INTO shift_registration_requests (staff_id, work_date, shift, location, note, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, request.getStaff().getId());
            pstmt.setDate(2, Date.valueOf(request.getWorkDate()));
            pstmt.setString(3, request.getShift().name());
            pstmt.setString(4, request.getLocation());
            pstmt.setString(5, request.getNote());
            pstmt.setString(6, request.getStatus().name());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        request.setRequestId(rs.getInt(1));
                    }
                }
            }
            return affectedRows;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm yêu cầu đăng ký ca: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int update(ShiftRegistrationRequest request) {
        String sql = "UPDATE shift_registration_requests SET status = ?, approved_by = ?, response_date = ?, note = ? " +
                     "WHERE request_id = ?";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, request.getStatus().name());
            if (request.getApprovedBy() != null) {
                pstmt.setInt(2, request.getApprovedBy().getId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setTimestamp(3, request.getResponseDate());
            pstmt.setString(4, request.getNote());
            pstmt.setInt(5, request.getRequestId());
            
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật yêu cầu đăng ký ca: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int delete(ShiftRegistrationRequest request) {
        String sql = "DELETE FROM shift_registration_requests WHERE request_id = ?";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, request.getRequestId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa yêu cầu đăng ký ca: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<ShiftRegistrationRequest> selectAll() {
        List<ShiftRegistrationRequest> list = new ArrayList<>();
        String sql = "SELECT srr.*, s.full_name AS staff_name, a.full_name AS approver_name " +
                     "FROM shift_registration_requests srr " +
                     "JOIN person s ON srr.staff_id = s.person_id " +
                     "LEFT JOIN person a ON srr.approved_by = a.person_id " +
                     "ORDER BY srr.request_date DESC";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            StaffRepository staffRepo = StaffRepository.getInstance();
            
            while (rs.next()) {
                Staff staff = staffRepo.selectById(rs.getInt("staff_id"));
                Staff approvedBy = null;
                int approvedById = rs.getInt("approved_by");
                if (!rs.wasNull()) {
                    approvedBy = staffRepo.selectById(approvedById);
                }
                
                ShiftRegistrationRequest request = new ShiftRegistrationRequest(
                    rs.getInt("request_id"),
                    staff,
                    rs.getDate("work_date").toLocalDate(),
                    Shift.valueOf(rs.getString("shift")),
                    rs.getString("location"),
                    rs.getString("note"),
                    RequestStatus.valueOf(rs.getString("status")),
                    approvedBy,
                    rs.getTimestamp("request_date"),
                    rs.getTimestamp("response_date")
                );
                
                list.add(request);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách yêu cầu đăng ký ca: " + e.getMessage());
        }
        return list;
    }

    @Override
    public ShiftRegistrationRequest selectById(ShiftRegistrationRequest t) {
        return selectById(t.getRequestId());
    }
    
    public ShiftRegistrationRequest selectById(int id) {
        String sql = "SELECT * FROM shift_registration_requests WHERE request_id = ?";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToShiftRegistrationRequest(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy yêu cầu đăng ký ca: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<ShiftRegistrationRequest> selectByCondition(String condition, Object... params) {
        List<ShiftRegistrationRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM shift_registration_requests WHERE " + condition;
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToShiftRegistrationRequest(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn yêu cầu đăng ký ca: " + e.getMessage());
        }
        return list;
    }
    
    private ShiftRegistrationRequest mapResultSetToShiftRegistrationRequest(ResultSet rs) throws SQLException {
        StaffRepository staffRepo = StaffRepository.getInstance();
        
        Staff staff = staffRepo.selectById(rs.getInt("staff_id"));
        Staff approvedBy = null;
        int approvedById = rs.getInt("approved_by");
        if (!rs.wasNull()) {
            approvedBy = staffRepo.selectById(approvedById);
        }
        
        return new ShiftRegistrationRequest(
            rs.getInt("request_id"),
            staff,
            rs.getDate("work_date").toLocalDate(),
            Shift.valueOf(rs.getString("shift")),
            rs.getString("location"),
            rs.getString("note"),
            RequestStatus.valueOf(rs.getString("status")),
            approvedBy,
            rs.getTimestamp("request_date"),
            rs.getTimestamp("response_date")
        );
    }
}