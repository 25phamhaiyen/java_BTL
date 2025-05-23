package service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import enums.Shift;
import model.LeaveRequest;
import model.Staff;
import model.WorkSchedule;
import repository.LeaveRequestRepository;
import repository.WorkScheduleRepository;
import enums.RequestStatus;

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
            // Gọi repository để lấy danh sách lịch làm việc trong khoảng thời gian từ startDate đến endDate
            List<WorkSchedule> workSchedules = scheduleRepository.selectByDateRange(startDate, endDate);
            System.err.println("Lấy lịch làm việc cho tuần từ " + startDate + " đến " + endDate + " thành công. Số lượng: " + workSchedules.size());
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
	private boolean isScheduleExists(int staffId, LocalDate workDate, String shift) {
		String whereClause = "staff_id = ? AND work_date = ? AND shift = ?";
		List<WorkSchedule> results = scheduleRepository.selectByCondition(whereClause, staffId, workDate, shift);
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
	 * Đăng ký ca làm việc (gửi yêu cầu đến admin)
	 * Phương thức này đã được chỉnh sửa để không còn sử dụng ShiftRegistrationRequestRepository
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
			System.err.println("Đã có lịch làm việc cho nhân viên " + staffId + " vào ca " + shift + " ngày " + workDate);
			return false;
		}

		StaffService staffService = new StaffService();
		Staff staff = staffService.getStaffById(staffId);

		if (staff == null) {
			System.err.println("Không tìm thấy nhân viên với ID: " + staffId);
			return false;
		}

		// Thay vì sử dụng ShiftRegistrationRequestRepository, chúng ta tạo trực tiếp một WorkSchedule mới
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
			System.out.println("Đăng ký ca làm việc thành công: Nhân viên " + staffId + ", ca " + shift + ", ngày " + workDate);
			return true;
		} else {
			System.err.println("Không thể đăng ký ca làm việc.");
			return false;
		}
	}

	/**
	 * Yêu cầu đổi ca
	 * Phương thức này được giả lập để tương thích với giao diện người dùng
	 */
	public boolean requestShiftChange(int staffId, LocalDate currentDate, Shift currentShift, LocalDate desiredDate,
									  Shift desiredShift, String reason) {
		System.out.println("Chức năng đổi ca đang được bảo trì. Vui lòng liên hệ admin để được hỗ trợ.");
		return false;
	}
}