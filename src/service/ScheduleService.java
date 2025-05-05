package service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import enums.Shift;
import model.Staff;
import model.WorkSchedule;
import repository.WorkScheduleRepository;

public class ScheduleService {
    
    private final WorkScheduleRepository scheduleRepository;
    
    public ScheduleService() {
        this.scheduleRepository = WorkScheduleRepository.getInstance();
    }
    
    /**
     * Lấy lịch làm việc của nhân viên theo ngày
     * @param staffId ID của nhân viên
     * @param date Ngày cần xem lịch
     * @return Danh sách lịch làm việc
     */
    public List<WorkSchedule> getSchedulesByStaffAndDate(int staffId, LocalDate date) {
        String whereClause = "staff_id = ? AND work_date = ?";
        return scheduleRepository.selectByCondition(whereClause, staffId, date);
    }
    
    /**
     * Lấy lịch làm việc của nhân viên trong khoảng thời gian
     * @param staffId ID của nhân viên
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách lịch làm việc
     */
    public List<WorkSchedule> getSchedulesByStaffAndDateRange(int staffId, LocalDate startDate, LocalDate endDate) {
        String whereClause = "staff_id = ? AND work_date BETWEEN ? AND ?";
        return scheduleRepository.selectByCondition(whereClause, staffId, startDate, endDate);
    }
    
    /**
     * Lấy toàn bộ lịch làm việc của nhân viên
     * @param staffId ID của nhân viên
     * @return Danh sách lịch làm việc
     */
    public List<WorkSchedule> getAllSchedulesByStaff(int staffId) {
        String whereClause = "staff_id = ?";
        return scheduleRepository.selectByCondition(whereClause, staffId);
    }
    
    /**
     * Lấy lịch làm việc của tất cả nhân viên theo ngày
     * @param date Ngày cần xem lịch
     * @return Danh sách lịch làm việc
     */
    public List<WorkSchedule> getSchedulesByDate(LocalDate date) {
        String whereClause = "work_date = ?";
        return scheduleRepository.selectByCondition(whereClause, date);
    }
    
    /**
     * Lấy danh sách lịch làm việc
     * @return Danh sách lịch làm việc
     */
    public List<WorkSchedule> getAllSchedules() {
        return scheduleRepository.selectAll();
    }

    /**
     * Thêm lịch làm việc mới
     * @param staffId ID của nhân viên
     * @param workDate Ngày làm việc
     * @param shift Ca làm việc (MORNING, AFTERNOON, EVENING)
     * @param location Địa điểm
     * @param note Ghi chú
     * @return true nếu thêm thành công, false nếu thất bại
     */
    public boolean registerShift(int staffId, LocalDate workDate, Shift shift, String location, String note) {
        if (workDate == null || shift == null || location == null) {
            System.err.println("Thông tin đăng ký ca làm không đầy đủ.");
            return false;
        }

        // Kiểm tra xem đã có lịch vào ca này chưa
        if (isScheduleExists(staffId, workDate, shift.name())) {
            System.err.println("Đã có lịch cho nhân viên " + staffId + " vào ca " + shift + " ngày " + workDate);
            return false;
        }

        StaffService staffService = new StaffService();
        Staff staff = staffService.getStaffById(staffId);

        if (staff == null) {
            System.err.println("Không tìm thấy nhân viên với ID: " + staffId);
            return false;
        }

        // Xác định thời gian bắt đầu và kết thúc dựa trên ca làm việc
        LocalTime startTime, endTime;
        switch (shift) {
            case MORNING:
                startTime = LocalTime.of(8, 0);
                endTime = LocalTime.of(12, 0);
                break;
            case AFTERNOON:
                startTime = LocalTime.of(13, 0);
                endTime = LocalTime.of(17, 0);
                break;
            case EVENING:
                startTime = LocalTime.of(18, 0);
                endTime = LocalTime.of(22, 0);
                break;
            default:
                System.err.println("Ca làm việc không hợp lệ: " + shift);
                return false;
        }

        WorkSchedule workSchedule = new WorkSchedule(
            0, // ID sẽ được tự động tạo
            staff,
            workDate,
            shift,
            startTime,
            endTime,
            location,
            "Công việc chung", // Task mặc định
            note
        );

        int result = scheduleRepository.insert(workSchedule);
        return result > 0;
    }

    /**
     * Yêu cầu nghỉ phép
     * @param staffId ID của nhân viên
     * @param leaveDate Ngày nghỉ
     * @param reason Lý do
     * @return true nếu gửi yêu cầu thành công, false nếu thất bại
     */
    public boolean requestLeave(int staffId, LocalDate leaveDate, String reason) {
        // TODO: Triển khai logic gửi yêu cầu nghỉ phép
        // Ví dụ: Lưu yêu cầu vào bảng leave_requests hoặc gửi thông báo
        System.out.println("Yêu cầu nghỉ phép: Nhân viên " + staffId + ", ngày " + leaveDate + ", lý do: " + reason);
        return true; // Giả lập thành công
    }

    /**
     * Yêu cầu đổi ca
     * @param staffId ID của nhân viên
     * @param currentDate Ngày hiện tại
     * @param currentShift Ca hiện tại
     * @param desiredDate Ngày mong muốn
     * @param desiredShift Ca mong muốn
     * @param reason Lý do
     * @return true nếu gửi yêu cầu thành công, false nếu thất bại
     */
    public boolean requestShiftChange(int staffId, LocalDate currentDate, Shift currentShift,
                                     LocalDate desiredDate, Shift desiredShift, String reason) {
        // Kiểm tra xem ca hiện tại có tồn tại không
        String whereClause = "staff_id = ? AND work_date = ? AND shift = ?";
        List<WorkSchedule> currentSchedules = scheduleRepository.selectByCondition(
            whereClause, staffId, currentDate, currentShift.name());
        
        if (currentSchedules.isEmpty()) {
            System.err.println("Không tìm thấy ca làm việc hiện tại để đổi.");
            return false;
        }

        // Kiểm tra xem ca mong muốn đã được đăng ký chưa
        if (isScheduleExists(staffId, desiredDate, desiredShift.name())) {
            System.err.println("Ca mong muốn đã được đăng ký.");
            return false;
        }

        // TODO: Triển khai logic gửi yêu cầu đổi ca
        // Ví dụ: Lưu yêu cầu vào bảng shift_change_requests hoặc gửi thông báo
        System.out.println("Yêu cầu đổi ca: Nhân viên " + staffId + ", từ ca " + currentShift + " ngày " + 
                           currentDate + " sang ca " + desiredShift + " ngày " + desiredDate + ", lý do: " + reason);
        return true; // Giả lập thành công
    }

    /**
     * Lấy thống kê giờ làm theo tháng
     * @param staffId ID của nhân viên
     * @param month Tháng
     * @param year Năm
     * @return Map chứa các thông tin thống kê
     */
    public Map<String, Object> getMonthlyStatistics(int staffId, int month, int year) {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.getMonth().length(startDate.isLeapYear()));
        
        List<WorkSchedule> schedules = getSchedulesByStaffAndDateRange(staffId, startDate, endDate);
        
        int totalHours = 0;
        int overtimeHours = 0;
        int standardWorkdays = 0;
        int leaveCount = 0; // Giả định cần bảng leave_requests để tính
        
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
        
        // Giả lập số ngày nghỉ phép
        leaveCount = 0; // Cần triển khai thực tế
        
        stats.put("totalHours", totalHours);
        stats.put("overtimeHours", overtimeHours);
        stats.put("standardWorkdays", standardWorkdays);
        stats.put("leaveCount", leaveCount);
        
        return stats;
    }

    /**
     * Kiểm tra xem nhân viên đã có lịch vào ca này chưa
     * @param staffId ID của nhân viên
     * @param workDate Ngày làm việc
     * @param shift Ca làm việc
     * @return true nếu đã có lịch, false nếu chưa có
     */
    private boolean isScheduleExists(int staffId, LocalDate workDate, String shift) {
        String whereClause = "staff_id = ? AND work_date = ? AND shift = ?";
        List<WorkSchedule> results = scheduleRepository.selectByCondition(whereClause, staffId, workDate, shift);
        return !results.isEmpty();
    }
    

    public void updateSchedule(int scheduleID, String name, String newNote) {
        WorkScheduleRepository repository = WorkScheduleRepository.getInstance();
        WorkSchedule schedule = repository.selectById(scheduleID);

        if (schedule != null) {
            schedule.setNote(newNote);
            int result = repository.update(schedule);
            if (result > 0) {
                System.out.println("Cập nhật lịch thành công.");
            } else {
                System.err.println("Cập nhật lịch thất bại.");
            }
        } else {
            System.err.println("Không tìm thấy lịch làm việc với ID: " + scheduleID);
        }
    }
    public void addSchedule(WorkSchedule newSchedule) {
        int result = WorkScheduleRepository.getInstance().insert(newSchedule);
        if (result > 0) {
            System.out.println("Thêm lịch làm việc thành công.");
        } else {
            System.err.println("Thêm lịch làm việc thất bại.");
        }
    }
    public void deleteSchedule(int scheduleID) {
        WorkScheduleRepository repository = WorkScheduleRepository.getInstance();
        WorkSchedule schedule = repository.selectById(scheduleID);
        if (schedule != null) {
            int result = repository.delete(schedule);
            if (result > 0) {
                System.out.println("Xóa lịch làm việc thành công.");
            } else {
                System.err.println("Xóa lịch làm việc thất bại.");
            }
        } else {
            System.err.println("Không tìm thấy lịch làm việc với ID: " + scheduleID);
        }
    }
    public List<WorkSchedule> getWorkSchedulesByWeek(LocalDate startDate, LocalDate endDate) {
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

}