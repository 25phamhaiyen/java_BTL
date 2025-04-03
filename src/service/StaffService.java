package service;

import java.util.List;
import java.util.logging.Logger;

import model.Staff;
import repository.StaffRepository;

public class StaffService {
    private final StaffRepository StaffRepository;
    private static final Logger LOGGER = Logger.getLogger(StaffService.class.getName());

    public StaffService() {
        this.StaffRepository = new StaffRepository();
    }

    // 1. Thêm nhân viên mới với validation
    public boolean addStaff(Staff staff) {
        try {
            // Kiểm tra AccountID có tồn tại không (nếu cần)
            // Có thể thêm validation bổ sung ở đây
            
            int result = StaffRepository.insert(staff);
            if (result > 0) {
                LOGGER.info("Thêm nhân viên thành công: " + staff.getId());
                return true;
            }
            return false;
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Lỗi validation khi thêm nhân viên: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            LOGGER.severe("Lỗi hệ thống khi thêm nhân viên: " + e.getMessage());
            return false;
        }
    }

    // 2. Cập nhật thông tin nhân viên
    public boolean updateStaff(Staff staff) {
        try {
            // Kiểm tra nhân viên có tồn tại không
            Staff existing = StaffRepository.selectById(staff.getId());
            if (existing == null) {
                LOGGER.warning("Nhân viên không tồn tại: " + staff.getId());
                return false;
            }

            int result = StaffRepository.update(staff);
            return result > 0;
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Lỗi validation khi cập nhật nhân viên: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            LOGGER.severe("Lỗi hệ thống khi cập nhật nhân viên: " + e.getMessage());
            return false;
        }
    }

    // 3. Xóa nhân viên
    public boolean deleteStaff(int staffId) {
        try {
            Staff staff = new Staff();
            staff.setId(staffId);
            int result = StaffRepository.delete(staff);
            return result > 0;
        } catch (Exception e) {
            LOGGER.severe("Lỗi khi xóa nhân viên: " + e.getMessage());
            return false;
        }
    }

    // 4. Lấy thông tin nhân viên theo ID
    public Staff getStaffById(int staffId) {
        try {
            return StaffRepository.selectById(staffId);
        } catch (Exception e) {
            LOGGER.severe("Lỗi khi lấy thông tin nhân viên: " + e.getMessage());
            return null;
        }
    }

    // 5. Lấy danh sách tất cả nhân viên
    public List<Staff> getAllStaffs() {
        try {
            return StaffRepository.selectAll();
        } catch (Exception e) {
            LOGGER.severe("Lỗi khi lấy danh sách nhân viên: " + e.getMessage());
            return List.of(); // Trả về danh sách rỗng thay vì null
        }
    }

    // 6. Tìm kiếm nhân viên theo điều kiện
    public List<Staff> searchStaffs(String condition, Object... params) {
        try {
            return StaffRepository.selectByCondition(condition, params);
        } catch (Exception e) {
            LOGGER.severe("Lỗi khi tìm kiếm nhân viên: " + e.getMessage());
            return List.of();
        }
    }

    // 7. Các phương thức tiện ích khác
    public boolean isPhoneNumberExists(String phoneNumber, Integer excludeStaffId) {
        try {
            return StaffRepository.selectByCondition("phoneNumber = ? AND staffID != ?", phoneNumber, excludeStaffId != null ? excludeStaffId : 0)
                         .size() > 0;
        } catch (Exception e) {
            LOGGER.severe("Lỗi khi kiểm tra số điện thoại: " + e.getMessage());
            return false;
        }
    }

    public boolean isCitizenNumberExists(String citizenNumber, Integer excludeStaffId) {
        try {
            return StaffRepository.selectByCondition("citizenNumber = ? AND staffID != ?", citizenNumber, excludeStaffId != null ? excludeStaffId : 0)
                         .size() > 0;
        } catch (Exception e) {
            LOGGER.severe("Lỗi khi kiểm tra số CCCD: " + e.getMessage());
            return false;
        }
    }

    // 8. Validation từ Entity và DAO
    public void validateStaff(Staff staff) {
        // Kiểm tra các ràng buộc từ Entity
        if (staff.getLastName() == null || staff.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Họ không được để trống");
        }
        if (staff.getFirstName() == null || staff.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên không được để trống");
        }
        if (staff.getPhoneNumber() == null || !staff.getPhoneNumber().matches("^[0-9]{10}$")) {
            throw new IllegalArgumentException("Số điện thoại phải có đúng 10 chữ số");
        }
        if (staff.getCitizenNumber() == null || !staff.getCitizenNumber().matches("^[0-9]{12}$")) {
            throw new IllegalArgumentException("Số CCCD phải có đúng 12 chữ số");
        }
        if (staff.getAddress() == null || staff.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ không được để trống");
        }
        if (staff.getGender() == null) {
            throw new IllegalArgumentException("Giới tính không được để trống");
        }
        if (staff.getRole() == null || staff.getRole().getRoleID() <= 0) {
            throw new IllegalArgumentException("Vai trò không hợp lệ");
        }

        // Kiểm tra các ràng buộc từ DAO (trùng lặp)
        if (isPhoneNumberExists(staff.getPhoneNumber(), staff.getId())) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại trong hệ thống");
        }
        if (isCitizenNumberExists(staff.getCitizenNumber(), staff.getId())) {
            throw new IllegalArgumentException("Số CCCD đã tồn tại trong hệ thống");
        }
    }
}