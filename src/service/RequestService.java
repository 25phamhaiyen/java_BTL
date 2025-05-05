package service;

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
import enums.Shift;
import utils.Session;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.List;

public class RequestService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final ShiftChangeRequestRepository shiftChangeRequestRepository;
    private final ShiftRegistrationRequestRepository shiftRegistrationRequestRepository;
    private final WorkScheduleRepository workScheduleRepository;
    
    public RequestService() {
        this.leaveRequestRepository = LeaveRequestRepository.getInstance();
        this.shiftChangeRequestRepository = ShiftChangeRequestRepository.getInstance();
        this.shiftRegistrationRequestRepository = ShiftRegistrationRequestRepository.getInstance();
        this.workScheduleRepository = WorkScheduleRepository.getInstance();
    }
    
    // Leave Request Methods
    public boolean createLeaveRequest(int staffId, LocalDate leaveDate, String reason) {
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
        
        return leaveRequestRepository.insert(leaveRequest) > 0;
    }
    
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.selectAll();
    }
    
    public List<LeaveRequest> getLeaveRequestsByStaff(int staffId) {
        return leaveRequestRepository.selectByCondition("staff_id = ?", staffId);
    }
    
    public List<LeaveRequest> getLeaveRequestsByStatus(RequestStatus status) {
        return leaveRequestRepository.selectByCondition("status = ?", status.name());
    }
    
    public boolean approveLeaveRequest(int requestId, String note) {
        LeaveRequest request = leaveRequestRepository.selectById(requestId);
        if (request == null || request.getStatus() != RequestStatus.PENDING) {
            return false;
        }
        
        Staff currentAdmin = Session.getCurrentStaff();
        request.setStatus(RequestStatus.APPROVED);
        request.setApprovedBy(currentAdmin);
        request.setNote(note);
        request.setResponseDate(new Timestamp(System.currentTimeMillis()));
        
        return leaveRequestRepository.update(request) > 0;
    }
    
    public boolean rejectLeaveRequest(int requestId, String note) {
        LeaveRequest request = leaveRequestRepository.selectById(requestId);
        if (request == null || request.getStatus() != RequestStatus.PENDING) {
            return false;
        }
        
        Staff currentAdmin = Session.getCurrentStaff();
        request.setStatus(RequestStatus.REJECTED);
        request.setApprovedBy(currentAdmin);
        request.setNote(note);
        request.setResponseDate(new Timestamp(System.currentTimeMillis()));
        
        return leaveRequestRepository.update(request) > 0;
    }
    
    // Shift Change Request Methods
    public boolean createShiftChangeRequest(int staffId, LocalDate currentDate, Shift currentShift,
                                           LocalDate desiredDate, Shift desiredShift, String reason) {
        StaffService staffService = new StaffService();
        Staff staff = staffService.getStaffById(staffId);
        
        if (staff == null) {
            System.err.println("Không tìm thấy nhân viên với ID: " + staffId);
            return false;
        }
        
        // Kiểm tra ca hiện tại có tồn tại không
        String whereClause = "staff_id = ? AND work_date = ? AND shift = ?";
        List<WorkSchedule> currentSchedules = workScheduleRepository.selectByCondition(
            whereClause, staffId, currentDate, currentShift.name());
        
        if (currentSchedules.isEmpty()) {
            System.err.println("Không tìm thấy ca làm việc hiện tại để đổi.");
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
        
        return shiftChangeRequestRepository.insert(request) > 0;
    }
    
    public List<ShiftChangeRequest> getAllShiftChangeRequests() {
        return shiftChangeRequestRepository.selectAll();
    }
    
    public List<ShiftChangeRequest> getShiftChangeRequestsByStaff(int staffId) {
        return shiftChangeRequestRepository.selectByCondition("staff_id = ?", staffId);
    }
    
    public List<ShiftChangeRequest> getShiftChangeRequestsByStatus(RequestStatus status) {
        return shiftChangeRequestRepository.selectByCondition("status = ?", status.name());
    }
    
    public boolean approveShiftChangeRequest(int requestId, String note) {
        ShiftChangeRequest request = shiftChangeRequestRepository.selectById(requestId);
        if (request == null || request.getStatus() != RequestStatus.PENDING) {
            return false;
        }
        
        // Xóa ca hiện tại
        String whereClause = "staff_id = ? AND work_date = ? AND shift = ?";
        List<WorkSchedule> currentSchedules = workScheduleRepository.selectByCondition(
            whereClause, request.getStaff().getId(), request.getCurrentDate(), request.getCurrentShift().name());
        
        if (currentSchedules.isEmpty()) {
            return false;
        }
        
        WorkSchedule currentSchedule = currentSchedules.get(0);
        workScheduleRepository.delete(currentSchedule);
        
        // Thêm ca mới
        LocalTime startTime, endTime;
        switch (request.getDesiredShift()) {
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
                return false;
        }

        WorkSchedule newSchedule = new WorkSchedule(
            0, request.getStaff(), request.getDesiredDate(), request.getDesiredShift(),
            startTime, endTime, currentSchedule.getLocation(), currentSchedule.getTask(),
            "Đã đổi từ ca " + request.getCurrentShift() + " ngày " + request.getCurrentDate()
        );
        workScheduleRepository.insert(newSchedule);
        
        Staff currentAdmin = Session.getCurrentStaff();
        request.setStatus(RequestStatus.APPROVED);
        request.setApprovedBy(currentAdmin);
        request.setNote(note);
        request.setResponseDate(new Timestamp(System.currentTimeMillis()));
        
        return shiftChangeRequestRepository.update(request) > 0;
    }
    
    public boolean rejectShiftChangeRequest(int requestId, String note) {
        ShiftChangeRequest request = shiftChangeRequestRepository.selectById(requestId);
        if (request == null || request.getStatus() != RequestStatus.PENDING) {
            return false;
        }
        
        Staff currentAdmin = Session.getCurrentStaff();
        request.setStatus(RequestStatus.REJECTED);
        request.setApprovedBy(currentAdmin);
        request.setNote(note);
        request.setResponseDate(new Timestamp(System.currentTimeMillis()));
        
        return shiftChangeRequestRepository.update(request) > 0;
    }

    // Shift Registration Request Methods
    public boolean createShiftRegistrationRequest(int staffId, LocalDate workDate, Shift shift, String location, String note) {
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
        
        return shiftRegistrationRequestRepository.insert(request) > 0;
    }
    
    public List<ShiftRegistrationRequest> getAllShiftRegistrationRequests() {
        return shiftRegistrationRequestRepository.selectAll();
    }
    
    public List<ShiftRegistrationRequest> getShiftRegistrationRequestsByStaff(int staffId) {
        return shiftRegistrationRequestRepository.selectByCondition("staff_id = ?", staffId);
    }
    
    public List<ShiftRegistrationRequest> getShiftRegistrationRequestsByStatus(RequestStatus status) {
        return shiftRegistrationRequestRepository.selectByCondition("status = ?", status.name());
    }
    
    public boolean approveShiftRegistrationRequest(int requestId, String note) {
        ShiftRegistrationRequest request = shiftRegistrationRequestRepository.selectById(requestId);
        if (request == null || request.getStatus() != RequestStatus.PENDING) {
            return false;
        }
        
        // Thêm ca vào work_schedule
        LocalTime startTime, endTime;
        switch (request.getShift()) {
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
                return false;
        }

        WorkSchedule schedule = new WorkSchedule(
            0, request.getStaff(), request.getWorkDate(), request.getShift(),
            startTime, endTime, request.getLocation(), "Công việc chung", request.getNote()
        );
        workScheduleRepository.insert(schedule);
        
        Staff currentAdmin = Session.getCurrentStaff();
        request.setStatus(RequestStatus.APPROVED);
        request.setApprovedBy(currentAdmin);
        request.setNote(note);
        request.setResponseDate(new Timestamp(System.currentTimeMillis()));
        
        return shiftRegistrationRequestRepository.update(request) > 0;
    }
    
    public boolean rejectShiftRegistrationRequest(int requestId, String note) {
        ShiftRegistrationRequest request = shiftRegistrationRequestRepository.selectById(requestId);
        if (request == null || request.getStatus() != RequestStatus.PENDING) {
            return false;
        }
        
        Staff currentAdmin = Session.getCurrentStaff();
        request.setStatus(RequestStatus.REJECTED);
        request.setApprovedBy(currentAdmin);
        request.setNote(note);
        request.setResponseDate(new Timestamp(System.currentTimeMillis()));
        
        return shiftRegistrationRequestRepository.update(request) > 0;
    }
}