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
import model.ShiftChangeRequest;
import model.ShiftRegistrationRequest;
import model.Staff;
import model.WorkSchedule;
import repository.LeaveRequestRepository;
import repository.ShiftChangeRequestRepository;
import repository.ShiftRegistrationRequestRepository;
import repository.WorkScheduleRepository;
import enums.RequestStatus;

public class ScheduleService {
    
    private final WorkScheduleRepository scheduleRepository;
    private final ShiftChangeRequestRepository shiftChangeRequestRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final ShiftRegistrationRequestRepository shiftRegistrationRequestRepository;
    
    public ScheduleService() {
        this.scheduleRepository = WorkScheduleRepository.getInstance();
        this.shiftChangeRequestRepository = ShiftChangeRequestRepository.getInstance();
        this.leaveRequestRepository = LeaveRequestRepository.getInstance();
        this.shiftRegistrationRequestRepository = ShiftRegistrationRequestRepository.getInstance();
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
        
        String whereClause = "work_date BETWEEN ? AND ?";
        return scheduleRepository.selectByCondition(whereClause, startDate, endDate);
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
        List<WorkSchedule> existingSchedules = scheduleRepository.selectByCondition(
            whereClause, schedule.getStaff().getId(), schedule.getWorkDate(), schedule.getShift().name());
        
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
     * Cập nhật thông tin lịch làm việc
     */
    public void updateSchedule(int scheduleId, String shift, String note) {
        // Find the schedule by ID
        WorkSchedule schedule = scheduleRepository.selectById(scheduleId);
        if (schedule == null) {
            System.err.println("Schedule not found with ID: " + scheduleId);
            return;
        }
        
        // Update the schedule fields
        try {
            if (shift != null && !shift.trim().isEmpty()) {
                // Don't update shift for now, only update note
                // schedule.setShift(Shift.valueOf(shift.toUpperCase()));
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid shift value: " + shift);
            return;
        }
        
        schedule.setNote(note);
        
        // Update in repository
        int result = scheduleRepository.update(schedule);
        if (result > 0) {
            System.out.println("Schedule updated successfully");
        } else {
            System.err.println("Failed to update schedule");
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
     * Đăng ký ca làm việc (gửi yêu cầu đến admin)
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

        // Kiểm tra xem đã có yêu cầu đăng ký ca tương tự chưa
        String whereClause = "staff_id = ? AND work_date = ? AND shift = ? AND status = ?";
        List<ShiftRegistrationRequest> existingRequests = shiftRegistrationRequestRepository.selectByCondition(
            whereClause, staffId, workDate, shift.name(), RequestStatus.PENDING.name());
        if (!existingRequests.isEmpty()) {
            System.err.println("Đã có yêu cầu đăng ký ca đang chờ xử lý cho nhân viên " + staffId + 
                               " vào ca " + shift + " ngày " + workDate);
            return false;
        }

        // Kiểm tra xem đã có lịch làm việc vào ca này chưa
        String scheduleWhereClause = "staff_id = ? AND work_date = ? AND shift = ?";
        List<WorkSchedule> existingSchedules = scheduleRepository.selectByCondition(
            scheduleWhereClause, staffId, workDate, shift.name());
        if (!existingSchedules.isEmpty()) {
            System.err.println("Đã có lịch làm việc cho nhân viên " + staffId + 
                               " vào ca " + shift + " ngày " + workDate);
            return false;
        }

        StaffService staffService = new StaffService();
        Staff staff = staffService.getStaffById(staffId);

        if (staff == null) {
            System.err.println("Không tìm thấy nhân viên với ID: " + staffId);
            return false;
        }

        ShiftRegistrationRequest request = new ShiftRegistrationRequest();
        request.setStaff(staff);
        request.setWorkDate(workDate);
        request.setShift(shift);
        request.setLocation(location);
        request.setNote(note);
        request.setStatus(RequestStatus.PENDING);
        request.setRequestDate(new Timestamp(System.currentTimeMillis()));

        int result = shiftRegistrationRequestRepository.insert(request);
        if (result > 0) {
            System.out.println("Yêu cầu đăng ký ca được gửi thành công: Nhân viên " + staffId + 
                               ", ca " + shift + ", ngày " + workDate);
            return true;
        } else {
            System.err.println("Không thể gửi yêu cầu đăng ký ca.");
            return false;
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
        List<LeaveRequest> existingRequests = leaveRequestRepository.selectByCondition(
            leaveWhereClause, staffId, leaveDate, RequestStatus.PENDING.name());
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
     * Yêu cầu đổi ca
     */
    public boolean requestShiftChange(int staffId, LocalDate currentDate, Shift currentShift,
                                     LocalDate desiredDate, Shift desiredShift, String reason) {
        if (currentDate == null || currentShift == null || desiredDate == null || desiredShift == null ||
            reason == null || reason.trim().isEmpty()) {
            System.err.println("Thông tin yêu cầu đổi ca không đầy đủ.");
            return false;
        }

        // Kiểm tra ngày hợp lệ
        LocalDate today = LocalDate.now();
        if (desiredDate.isBefore(today)) {
            System.err.println("Không thể yêu cầu đổi ca cho ngày quá khứ: " + desiredDate);
            return false;
        }

        // Kiểm tra xem ca hiện tại có tồn tại không
        String whereClause = "staff_id = ? AND work_date = ? AND shift = ?";
        List<WorkSchedule> currentSchedules = scheduleRepository.selectByCondition(
            whereClause, staffId, currentDate, currentShift.name());
        
        if (currentSchedules.isEmpty()) {
            System.err.println("Không tìm thấy ca làm việc hiện tại để đổi: Nhân viên " + staffId + 
                               ", ngày " + currentDate + ", ca " + currentShift);
            return false;
        }

        // Kiểm tra xem ca mong muốn đã được đăng ký bởi nhân viên này chưa
        if (isScheduleExists(staffId, desiredDate, desiredShift.name())) {
            System.err.println("Nhân viên " + staffId + " đã đăng ký ca " + desiredShift + 
                               " vào ngày " + desiredDate);
            return false;
        }

        // Kiểm tra xem đã có yêu cầu đổi ca tương tự chưa
        String shiftChangeWhereClause = "staff_id = ? AND current_date = ? AND current_shift = ? AND status = ?";
        List<ShiftChangeRequest> existingRequests = shiftChangeRequestRepository.selectByCondition(
            shiftChangeWhereClause, staffId, currentDate, currentShift.name(), RequestStatus.PENDING.name());
        if (!existingRequests.isEmpty()) {
            System.err.println("Đã có yêu cầu đổi ca đang chờ xử lý cho ngày " + currentDate + ", ca " + currentShift);
            return false;
        }

        StaffService staffService = new StaffService();
        Staff staff = staffService.getStaffById(staffId);
        
        if (staff == null) {
            System.err.println("Không tìm thấy nhân viên với ID: " + staffId);
            return false;
        }
        
        ShiftChangeRequest request = new ShiftChangeRequest();
        request.setStaff(staff);
        request.setCurrentDate(currentDate);
        request.setCurrentShift(currentShift);
        request.setDesiredDate(desiredDate);
        request.setDesiredShift(desiredShift);
        request.setReason(reason);
        request.setStatus(RequestStatus.PENDING);
        request.setRequestDate(new Timestamp(System.currentTimeMillis()));
        
        int result = shiftChangeRequestRepository.insert(request);
        if (result > 0) {
            System.out.println("Yêu cầu đổi ca được gửi thành công: Nhân viên " + staffId + 
                               ", từ ca " + currentShift + " ngày " + currentDate + 
                               " sang ca " + desiredShift + " ngày " + desiredDate);
            return true;
        } else {
            System.err.println("Không thể gửi yêu cầu đổi ca.");
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
        List<LeaveRequest> leaveRequests = leaveRequestRepository.selectByCondition(
            leaveWhereClause, staffId, startDate, endDate, RequestStatus.APPROVED.name());
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
}