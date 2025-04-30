package service;

import java.time.LocalDate;
import java.util.List;

import model.Schedule;
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
    public List<Schedule> getSchedulesByStaffAndDate(int staffId, LocalDate date) {
        String whereClause = "staff_id = ? AND work_date = ?";
        return convertToSchedules(scheduleRepository.selectByCondition(whereClause, staffId, date));
    }
    
    /**
     * Lấy lịch làm việc của nhân viên trong khoảng thời gian
     * @param staffId ID của nhân viên
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách lịch làm việc
     */
    public List<Schedule> getSchedulesByStaffAndDateRange(int staffId, LocalDate startDate, LocalDate endDate) {
        String whereClause = "staff_id = ? AND work_date BETWEEN ? AND ?";
        return convertToSchedules(scheduleRepository.selectByCondition(whereClause, staffId, startDate, endDate));
    }
    
    /**
     * Lấy toàn bộ lịch làm việc của nhân viên
     * @param staffId ID của nhân viên
     * @return Danh sách lịch làm việc
     */
    public List<Schedule> getAllSchedulesByStaff(int staffId) {
        String whereClause = "staff_id = ?";
        return convertToSchedules(scheduleRepository.selectByCondition(whereClause, staffId));
    }
    
    /**
     * Lấy lịch làm việc của tất cả nhân viên theo ngày
     * @param date Ngày cần xem lịch
     * @return Danh sách lịch làm việc
     */
    public List<Schedule> getSchedulesByDate(LocalDate date) {
        String whereClause = "work_date = ?";
        return convertToSchedules(scheduleRepository.selectByCondition(whereClause, date));
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
    
 // Lấy danh sách lịch làm việc
    public List<WorkSchedule> getAllSchedules() {
        return scheduleRepository.selectAll();
    }

    /**
     * Thêm lịch làm việc mới
     * @param staffId ID của nhân viên
     * @param workDate Ngày làm việc
     * @param shift Ca làm việc (MORNING, AFTERNOON, EVENING)
     * @param note Ghi chú
     * @return true nếu thêm thành công, false nếu thất bại
     */
    public boolean addSchedule(int staffId, LocalDate workDate, String shift, String note) {
        // Kiểm tra xem đã có lịch vào ca này chưa
        if (isScheduleExists(staffId, workDate, shift)) {
            return false; // Đã có lịch vào ca này rồi
        }
        
        Schedule schedule = new Schedule(0, staffId, workDate, shift, note);
        
        // Chuyển đổi Schedule sang WorkSchedule để lưu vào database
        StaffService staffService = new StaffService();
        Staff staff = staffService.getStaffById(staffId);
        
        if (staff == null) {
            return false; // Không tìm thấy nhân viên
        }
        
        model.WorkSchedule workSchedule = new model.WorkSchedule(
            0, // ID sẽ được tự động tạo
            staff,
            workDate,
            enums.Shift.valueOf(shift), // Chuyển đổi String thành Enum
            note
        );
        
        int result = scheduleRepository.insert(workSchedule);
        return result > 0;
    }
    
    public boolean addSchedule(WorkSchedule workSchedule) {
        if (workSchedule == null || workSchedule.getStaff() == null || workSchedule.getWorkDate() == null || workSchedule.getShift() == null) {
            System.err.println("Thông tin lịch làm việc không đầy đủ.");
            return false;
        }

        int staffId = workSchedule.getStaff().getId();
        LocalDate workDate = workSchedule.getWorkDate();
        String shift = workSchedule.getShift().toString();

        // Kiểm tra xem đã có lịch vào ca này cho nhân viên này vào ngày này chưa
        if (isScheduleExists(staffId, workDate, shift)) {
            System.err.println("Đã có lịch cho nhân viên " + staffId + " vào ca " + shift + " ngày " + workDate);
            return false; // Đã có lịch
        }

        // Gọi repository để thêm WorkSchedule trực tiếp
        int result = scheduleRepository.insert(workSchedule);
        return result > 0;
    }
    
    /**
     * Cập nhật lịch làm việc
     * @param scheduleId ID của lịch làm việc
     * @param shift Ca làm việc mới
     * @param note Ghi chú mới
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    public boolean updateSchedule(int scheduleId, String shift, String note) {
        model.WorkSchedule workSchedule = scheduleRepository.selectById(scheduleId);
        
        if (workSchedule == null) {
            return false; // Không tìm thấy lịch làm việc
        }
        
        workSchedule.setShift(enums.Shift.valueOf(shift));
        workSchedule.setNote(note);
        
        int result = scheduleRepository.update(workSchedule);
        return result > 0;
    }
    
    /**
     * Xóa lịch làm việc
     * @param scheduleId ID của lịch làm việc
     * @return true nếu xóa thành công, false nếu thất bại
     */
    public boolean deleteSchedule(int scheduleId) {
        model.WorkSchedule workSchedule = scheduleRepository.selectById(scheduleId);
        
        if (workSchedule == null) {
            return false; // Không tìm thấy lịch làm việc
        }
        
        int result = scheduleRepository.delete(workSchedule);
        return result > 0;
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
        List<?> results = scheduleRepository.selectByCondition(whereClause, staffId, workDate, shift);
        return !results.isEmpty();
    }
    
    /**
     * Chuyển đổi từ WorkSchedule sang Schedule
     * @param workSchedules Danh sách WorkSchedule
     * @return Danh sách Schedule
     */
    private List<Schedule> convertToSchedules(List<?> workSchedules) {
        return workSchedules.stream()
            .filter(ws -> ws instanceof model.WorkSchedule)
            .map(ws -> {
                model.WorkSchedule workSchedule = (model.WorkSchedule) ws;
                return new Schedule(
                    workSchedule.getScheduleID(),
                    workSchedule.getStaff().getId(),
                    workSchedule.getWorkDate(),
                    workSchedule.getShift().name(),
                    workSchedule.getNote()
                );
            })
            .collect(java.util.stream.Collectors.toList());
    }
}