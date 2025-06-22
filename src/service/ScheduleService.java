package service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import enums.Shift;
import javafx.util.Pair;
import model.LeaveRequest;
import model.Role;
import model.ShiftAssignment;
import model.ShiftRequest;
import model.Staff;
import model.WorkSchedule;
import repository.LeaveRequestRepository;
import repository.WorkScheduleRepository;
import utils.DatabaseConnection;
import enums.RequestStatus;
import enums.RequestType;

public class ScheduleService {

	private final WorkScheduleRepository scheduleRepository;
	private final LeaveRequestRepository leaveRequestRepository;

	public ScheduleService() {
		this.scheduleRepository = WorkScheduleRepository.getInstance();
		this.leaveRequestRepository = LeaveRequestRepository.getInstance();
	}

	/**
	 * Lấy lịch làm việc của nhân viên theo ngày
	 */
	public List<WorkSchedule> getSchedulesByStaffAndDate(int staffId, LocalDate date) {
		String whereClause = "staff_id = ? AND work_date = ?";
		return scheduleRepository.selectByCondition(whereClause, staffId, date);
	}

	/**
	 * Lấy lịch làm việc của nhân viên trong khoảng thời gian
	 */
	public List<WorkSchedule> getSchedulesByStaffAndDateRange(int staffId, LocalDate startDate, LocalDate endDate) {
		String whereClause = "staff_id = ? AND work_date BETWEEN ? AND ?";
		return scheduleRepository.selectByCondition(whereClause, staffId, startDate, endDate);
	}

	/**
	 * Lấy toàn bộ lịch làm việc của nhân viên
	 */
	public List<WorkSchedule> getAllSchedulesByStaff(int staffId) {
		String whereClause = "staff_id = ?";
		return scheduleRepository.selectByCondition(whereClause, staffId);
	}

	/**
	 * Lấy lịch làm việc của tất cả nhân viên theo ngày
	 */
	public List<WorkSchedule> getSchedulesByDate(LocalDate date) {
		String whereClause = "work_date = ?";
		return scheduleRepository.selectByCondition(whereClause, date);
	}

	/**
	 * Lấy danh sách lịch làm việc
	 */
	public List<WorkSchedule> getAllSchedules() {
		return scheduleRepository.selectAll();
	}

	/**
	 * Lấy lịch làm việc của tất cả nhân viên trong khoảng thời gian (cho tuần)
	 */
	public List<WorkSchedule> getWorkSchedulesByWeek(LocalDate startDate, LocalDate endDate) {
		if (startDate == null || endDate == null) {
			return new ArrayList<>();
		}

		if (startDate.isAfter(endDate)) {
			System.err.println("Start date cannot be after end date");
			return new ArrayList<>();
		}

		try {
			// Gọi repository để lấy danh sách lịch làm việc trong khoảng thời gian từ
			// startDate đến endDate
			List<WorkSchedule> workSchedules = scheduleRepository.selectByDateRange(startDate, endDate);
			System.err.println("Lấy lịch làm việc cho tuần từ " + startDate + " đến " + endDate
					+ " thành công. Số lượng: " + workSchedules.size());
			return workSchedules;
		} catch (Exception e) {
			System.err.println("Lỗi khi lấy lịch làm việc theo tuần: " + e.getMessage());
			return List.of(); // Trả về danh sách rỗng nếu có lỗi
		}
	}

	/**
	 * Thêm lịch làm việc mới
	 */
	public void addSchedule(WorkSchedule schedule) {
		if (schedule == null) {
			System.err.println("Schedule cannot be null");
			return;
		}

		// Validate schedule data
		if (schedule.getWorkDate() == null || schedule.getShift() == null) {
			System.err.println("Schedule must have work date and shift");
			return;
		}

		// Kiểm tra xem đã có lịch vào ca này chưa
		String whereClause = "staff_id = ? AND work_date = ? AND shift = ?";
		List<WorkSchedule> existingSchedules = scheduleRepository.selectByCondition(whereClause,
				schedule.getStaff().getId(), schedule.getWorkDate(), schedule.getShift().name());

		if (!existingSchedules.isEmpty()) {
			System.err.println("Schedule already exists for this staff, date and shift");
			return;
		}

		// Add to repository
		int result = scheduleRepository.insert(schedule);
		if (result > 0) {
			System.out.println("Schedule added successfully");
		} else {
			System.err.println("Failed to add schedule");
		}
	}

	/**
	 * Xóa lịch làm việc
	 */
	public void deleteSchedule(int scheduleId) {
		// Find the schedule by ID
		WorkSchedule schedule = scheduleRepository.selectById(scheduleId);
		if (schedule == null) {
			System.err.println("Schedule not found with ID: " + scheduleId);
			return;
		}

		// Delete from repository
		int result = scheduleRepository.delete(schedule);
		if (result > 0) {
			System.out.println("Schedule deleted successfully");
		} else {
			System.err.println("Failed to delete schedule");
		}
	}

	/**
	 * Yêu cầu nghỉ phép
	 */
	public boolean requestLeave(int staffId, LocalDate leaveDate, String reason) {
		if (leaveDate == null || reason == null || reason.trim().isEmpty()) {
			System.err.println("Thông tin yêu cầu nghỉ phép không đầy đủ.");
			return false;
		}

		// Kiểm tra ngày hợp lệ
		LocalDate today = LocalDate.now();
		if (leaveDate.isBefore(today)) {
			System.err.println("Không thể yêu cầu nghỉ phép cho ngày quá khứ: " + leaveDate);
			return false;
		}

		// Kiểm tra xem nhân viên có lịch làm việc vào ngày này không
		String whereClause = "staff_id = ? AND work_date = ?";
		List<WorkSchedule> schedules = scheduleRepository.selectByCondition(whereClause, staffId, leaveDate);
		if (!schedules.isEmpty()) {
			System.err.println("Nhân viên có lịch làm việc vào ngày " + leaveDate + ". Không thể yêu cầu nghỉ phép.");
			return false;
		}

		// Kiểm tra xem đã có yêu cầu nghỉ phép cho ngày này chưa
		String leaveWhereClause = "staff_id = ? AND leave_date = ? AND status = ?";
		List<LeaveRequest> existingRequests = leaveRequestRepository.selectByCondition(leaveWhereClause, staffId,
				leaveDate, RequestStatus.PENDING.name());
		if (!existingRequests.isEmpty()) {
			System.err.println("Đã có yêu cầu nghỉ phép đang chờ xử lý cho ngày " + leaveDate);
			return false;
		}

		StaffService staffService = new StaffService();
		Staff staff = staffService.getStaffById(staffId);

		if (staff == null) {
			System.err.println("Không tìm thấy nhân viên với ID: " + staffId);
			return false;
		}

		LeaveRequest leaveRequest = new LeaveRequest();
		leaveRequest.setStaff(staff);
		leaveRequest.setLeaveDate(leaveDate);
		leaveRequest.setReason(reason);
		leaveRequest.setStatus(RequestStatus.PENDING);
		leaveRequest.setRequestDate(new Timestamp(System.currentTimeMillis()));

		int result = leaveRequestRepository.insert(leaveRequest);
		if (result > 0) {
			System.out.println("Yêu cầu nghỉ phép được gửi thành công: Nhân viên " + staffId + ", ngày " + leaveDate);
			return true;
		} else {
			System.err.println("Không thể gửi yêu cầu nghỉ phép.");
			return false;
		}
	}

	/**
	 * Lấy thống kê giờ làm theo tháng
	 */
	public Map<String, Object> getMonthlyStatistics(int staffId, int month, int year) {
		Map<String, Object> stats = new HashMap<>();

		LocalDate startDate = LocalDate.of(year, month, 1);
		LocalDate endDate = startDate.withDayOfMonth(startDate.getMonth().length(startDate.isLeapYear()));

		List<WorkSchedule> schedules = getSchedulesByStaffAndDateRange(staffId, startDate, endDate);

		int totalHours = 0;
		int overtimeHours = 0;
		int standardWorkdays = 0;
		int leaveCount = 0;

		for (WorkSchedule schedule : schedules) {
			if (schedule.getStartTime() != null && schedule.getEndTime() != null) {
				int hours = schedule.getEndTime().getHour() - schedule.getStartTime().getHour();
				totalHours += hours;
				if (hours > 8) {
					overtimeHours += (hours - 8);
				}
				standardWorkdays++;
			}
		}

		// Tính số ngày nghỉ phép
		String leaveWhereClause = "staff_id = ? AND leave_date BETWEEN ? AND ? AND status = ?";
		List<LeaveRequest> leaveRequests = leaveRequestRepository.selectByCondition(leaveWhereClause, staffId,
				startDate, endDate, RequestStatus.APPROVED.name());
		leaveCount = leaveRequests.size();

		stats.put("totalHours", totalHours);
		stats.put("overtimeHours", overtimeHours);
		stats.put("standardWorkdays", standardWorkdays);
		stats.put("leaveCount", leaveCount);

		return stats;
	}

	/**
	 * Kiểm tra xem nhân viên đã có lịch vào ca này chưa
	 */
	public boolean isScheduleExists(int staffId, LocalDate workDate, String shift) {
		String whereClause = "staff_id = ? AND work_date = ? AND shift = ?";
		List<WorkSchedule> results = scheduleRepository.selectByCondition(whereClause, staffId, workDate, shift);
		System.out.println("CHECK scheduleExists with: staffId=" + staffId + ", date=" + workDate + ", shift=" + shift);
		return !results.isEmpty();
	}

	/**
	 * Kiểm tra ngày có thể đăng ký được không
	 */
	public boolean validateScheduleDate(LocalDate date) {
		LocalDate today = LocalDate.now();

		if (date.isBefore(today)) {
			System.err.println("Không thể đăng ký lịch cho ngày quá khứ: " + date);
			return false;
		}

		if (date.isEqual(today)) {
			System.err.println("Không thể đăng ký lịch cho ngày hôm nay: " + date);
			return false;
		}

		return true;
	}

	/**
	 * Cập nhật thông tin lịch làm việc
	 */
	public void updateSchedule(int scheduleID, String name, String newNote) {
		WorkSchedule schedule = scheduleRepository.selectById(scheduleID);

		if (schedule != null) {
			schedule.setNote(newNote);
			int result = scheduleRepository.update(schedule);
			if (result > 0) {
				System.out.println("Cập nhật lịch thành công.");
			} else {
				System.err.println("Cập nhật lịch thất bại.");
			}
		} else {
			System.err.println("Không tìm thấy lịch làm việc với ID: " + scheduleID);
		}
	}

	/**
	 * Đăng ký ca làm việc (gửi yêu cầu đến admin) Phương thức này đã được chỉnh sửa
	 * để không còn sử dụng ShiftRegistrationRequestRepository
	 */
	public boolean registerShift(int staffId, LocalDate workDate, Shift shift, String location, String note) {
		if (workDate == null || shift == null || location == null) {
			System.err.println("Thông tin đăng ký ca làm không đầy đủ.");
			return false;
		}

		// Kiểm tra ngày hợp lệ
		if (!validateScheduleDate(workDate)) {
			return false;
		}

		// Kiểm tra xem đã có lịch làm việc vào ca này chưa
		if (isScheduleExists(staffId, workDate, shift.name())) {
			System.err
					.println("Đã có lịch làm việc cho nhân viên " + staffId + " vào ca " + shift + " ngày " + workDate);
			return false;
		}

		StaffService staffService = new StaffService();
		Staff staff = staffService.getStaffById(staffId);

		if (staff == null) {
			System.err.println("Không tìm thấy nhân viên với ID: " + staffId);
			return false;
		}

		// Thay vì sử dụng ShiftRegistrationRequestRepository, chúng ta tạo trực tiếp
		// một WorkSchedule mới
		WorkSchedule newSchedule = new WorkSchedule();
		newSchedule.setStaff(staff);
		newSchedule.setWorkDate(workDate);
		newSchedule.setShift(shift);
		newSchedule.setLocation(location);
		newSchedule.setNote(note);

		// Đặt thời gian bắt đầu và kết thúc dựa trên ca làm việc
		switch (shift) {
		case MORNING:
			newSchedule.setStartTime(LocalTime.of(8, 0));
			newSchedule.setEndTime(LocalTime.of(12, 0));
			break;
		case AFTERNOON:
			newSchedule.setStartTime(LocalTime.of(13, 0));
			newSchedule.setEndTime(LocalTime.of(17, 0));
			break;
		case EVENING:
			newSchedule.setStartTime(LocalTime.of(18, 0));
			newSchedule.setEndTime(LocalTime.of(22, 0));
			break;
		}

		// Thêm lịch làm việc vào cơ sở dữ liệu
		int result = scheduleRepository.insert(newSchedule);
		if (result > 0) {
			System.out.println(
					"Đăng ký ca làm việc thành công: Nhân viên " + staffId + ", ca " + shift + ", ngày " + workDate);
			return true;
		} else {
			System.err.println("Không thể đăng ký ca làm việc.");
			return false;
		}
	}

	/**
	 * Yêu cầu đổi ca Phương thức này được giả lập để tương thích với giao diện
	 * người dùng
	 */
	public boolean requestShiftChange(int staffId, LocalDate currentDate, Shift currentShift, LocalDate desiredDate,
			Shift desiredShift, String reason) {
		System.out.println("Chức năng đổi ca đang được bảo trì. Vui lòng liên hệ admin để được hỗ trợ.");
		return false;
	}

	// Xử lý đăng ký từ nhân viên
	public boolean sendShiftRequest(int staffId, LocalDate date, Shift shift, RequestType type, String reason) {
		StaffService staffService = new StaffService();
		Staff staff = staffService.getStaffById(staffId);
		try {
            // Validate
            if (date == null || shift == null || type == null) return false;
            if (date.isBefore(LocalDate.now())) return false;
            if (type == RequestType.LEAVE && (reason == null || reason.isEmpty())) return false;

            if (scheduleRepository.isDuplicate(staff, date, shift)) return false;

            ShiftRequest request = new ShiftRequest();
            request.setStaff(staff);
            request.setRequestDate(date);
            request.setShift(shift);
            request.setType(type);
            request.setReason(reason);
            request.setStatus(RequestStatus.PENDING);

            return scheduleRepository.insert(request);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
	}

//	Admin duyệt yêu cầu
	public boolean approveRequest(int requestId, RequestStatus status) {
	    boolean updated = scheduleRepository.approveRequest(requestId, status);
	    if (!updated || status != RequestStatus.APPROVED) return updated;

	    ShiftRequest request = scheduleRepository.findRequestById(requestId);
	    if (request == null) return false;

	    System.out.println("Thực hiện xử lý yêu cầu: " + request.getType());

	    if (request.getType() == RequestType.LEAVE) {
	        System.out.println("Xoá lịch làm việc: " + request.getStaff().getId() + " " + request.getRequestDate() + " " + request.getShift());
	        return scheduleRepository.deleteWorkSchedule(
	            request.getStaff().getId(),
	            request.getRequestDate(),
	            request.getShift()
	        );
	    } else if (request.getType() == RequestType.WORK) {
	        System.out.println("Thêm lịch làm việc: " + request.getStaff().getId() + " " + request.getRequestDate() + " " + request.getShift());
	        return scheduleRepository.insertWorkSchedule(
	            request.getStaff().getId(),
	            request.getRequestDate(),
	            request.getShift(),
	            "ĐĂNG KÝ"
	        );
	    }

	    return true;
	}



	// Admin xem các yêu cầu chưa duyệt
	public List<ShiftRequest> getPendingRequests() {
		return scheduleRepository.getPendingRequests();

	}

	// Lấy danh sách nghỉ:
	public Pair<Map<Integer, List<Shift>>, Map<Integer, List<ShiftAssignment>>> getApprovedRequests(LocalDate weekStart) {
		return scheduleRepository.getApprovedRequests(weekStart);

	}

	public boolean assignShift(int staffId, LocalDate date, Shift shift) {
		return scheduleRepository.assignShift(staffId, date, shift);

	}

	public void autoAssignWeekShifts(List<Staff> staffList, LocalDate weekStart,
			Map<Integer, List<Shift>> leaveRequests, Map<Integer, List<ShiftAssignment>> preferredShifts) {

		Map<Integer, List<ShiftAssignment>> weeklyAssignments = new HashMap<>();

		for (int i = 0; i < 7; i++) {
			LocalDate currentDate = weekStart.plusDays(i);

			for (Shift shift : Shift.values()) {
				if (shift == Shift.NOSHIFT)
					continue;

				List<Staff> assigned = new ArrayList<>();

// Bước 1: Chọn 2–3 nhân viên chăm sóc
				List<Staff> caregivers = selectStaffForRole(staffList, "STAFF_CARE", shift, currentDate, 3,
						leaveRequests, weeklyAssignments, preferredShifts);
				if (caregivers.size() < 2) {
					System.out.println("Không đủ nhân viên chăm sóc cho " + shift + " ngày " + currentDate);
					continue;
				}
				assigned.addAll(caregivers.subList(0, Math.min(3, caregivers.size())));

				int remainingSlots = 5 - assigned.size();

// Bước 2: Chọn 1 thu ngân nếu còn slot
				if (remainingSlots > 0) {
					List<Staff> cashiers = selectStaffForRole(staffList, "STAFF_CASHIER", shift, currentDate, 1,
							leaveRequests, weeklyAssignments, preferredShifts);
					if (!cashiers.isEmpty()) {
						assigned.add(cashiers.get(0));
						remainingSlots--;
					}
				}

// Bước 3: Chọn 1 lễ tân nếu còn slot
				if (remainingSlots > 0) {
					List<Staff> receptionists = selectStaffForRole(staffList, "STAFF_RECEPTION", shift, currentDate, 1,
							leaveRequests, weeklyAssignments, preferredShifts);
					if (!receptionists.isEmpty()) {
						assigned.add(receptionists.get(0));
						remainingSlots--;
					}
				}

// Bước 4: Nếu còn slot, thêm admin
				if (remainingSlots > 0) {
					List<Staff> admins = selectStaffForRole(staffList, "ADMIN", shift, currentDate, 1, leaveRequests,
							weeklyAssignments, preferredShifts);
					if (!admins.isEmpty()) {
						assigned.add(admins.get(0));
						remainingSlots--;
					}
				}

// Phân công
				for (Staff s : assigned) {
					assignShift(s.getId(), currentDate, shift);
					weeklyAssignments.computeIfAbsent(s.getId(), k -> new ArrayList<>())
							.add(new ShiftAssignment(currentDate, shift));
				}

				System.out.println(currentDate + " - " + shift + ": Đã phân " + assigned.size() + " người.");
			}
		}
	}

	private List<Staff> selectStaffForRole(List<Staff> staffList, String roleName, Shift shift, LocalDate date,
	        int maxNeeded, Map<Integer, List<Shift>> leaveRequests,
	        Map<Integer, List<ShiftAssignment>> weeklyAssignments,
	        Map<Integer, List<ShiftAssignment>> preferredShifts) {

		return staffList.stream()
		        .filter(s -> s.getRole().getRoleName().equalsIgnoreCase(roleName))
		        .filter(s -> {
		            List<Shift> leaves = leaveRequests.getOrDefault(s.getId(), List.of());
		            return !leaves.contains(shift);
		        })
		        .sorted(Comparator
		            // Ưu tiên người có nguyện vọng làm ca này
		            .comparing((Staff s) -> !hasPreferredShift(s.getId(), date, shift, preferredShifts))
		            // Ưu tiên người chưa làm trong ngày
		            .thenComparingInt(s -> countShiftsInDay(s.getId(), date, weeklyAssignments))
		            // Ưu tiên người làm ít ca đó trong tuần
		            .thenComparingInt(s -> countShiftFrequency(s.getId(), shift, weeklyAssignments)))
		        .limit(maxNeeded)
		        .collect(Collectors.toList());

	}
	private boolean hasPreferredShift(int staffId, LocalDate date, Shift shift,
	        Map<Integer, List<ShiftAssignment>> preferredShifts) {
	    return preferredShifts.getOrDefault(staffId, List.of()).stream()
	            .anyMatch(a -> a.getDate().equals(date) && a.getShift() == shift);
	}

	private int countShiftsInDay(int staffId, LocalDate date, Map<Integer, List<ShiftAssignment>> assignments) {
	    return (int) assignments.getOrDefault(staffId, List.of()).stream()
	            .filter(a -> a.getDate().equals(date))
	            .count();
	}
	
	private int countShiftFrequency(int staffId, Shift shift, Map<Integer, List<ShiftAssignment>> assignments) {
	    return (int) assignments.getOrDefault(staffId, List.of()).stream()
	                            .filter(a -> a.getShift() == shift)
	                            .count();
	}
	
	public boolean isWeekScheduled(LocalDate weekStart) {
	    LocalDate weekEnd = weekStart.plusDays(6);

	    String sql = "SELECT COUNT(*) FROM work_schedule WHERE work_date BETWEEN ? AND ?";
	    try (Connection conn = DatabaseConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setDate(1, Date.valueOf(weekStart));
	        stmt.setDate(2, Date.valueOf(weekEnd));

	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            int count = rs.getInt(1);
	            return count > 0; // Nếu có dòng => đã có lịch
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return false;
	}

	public List<ShiftRequest> getRequestsByStaffId(int staffId) {
	    return scheduleRepository.getRequestsByStaffId(staffId);
	}

}