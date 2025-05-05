package service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import enums.StatusEnum;
import model.Booking;
import model.Customer;
import model.Pet;
import model.Staff;
import repository.BookingRepository;
import repository.CustomerRepository;
import repository.PetRepository;
import repository.StaffRepository;

public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final PetRepository petRepository;
    private final StaffRepository staffRepository;
    
    public BookingService() {
        this.bookingRepository = BookingRepository.getInstance();
        this.customerRepository = CustomerRepository.getInstance();
        this.petRepository = PetRepository.getInstance();
        this.staffRepository = StaffRepository.getInstance();
    }
    
    /**
     * Lấy tất cả booking
     */
    public List<Booking> getAllBookings() throws Exception {
        return bookingRepository.selectAll();
    }
    
    /**
     * Lấy booking theo ID
     */
    public Booking getBookingById(int bookingId) throws Exception {
        return bookingRepository.selectById(bookingId);
    }
    
    /**
     * Lấy danh sách booking theo ID của nhân viên
     */
    public List<Booking> getBookingsByStaffId(int staffId) throws Exception {
        String condition = "b.staff_id = ?";
        return bookingRepository.selectByCondition(condition, staffId);
    }
    
    /**
     * Lấy danh sách booking theo ID của khách hàng
     */
    public List<Booking> getBookingsByCustomerId(int customerId) throws Exception {
        String condition = "customer_id = ?";
        return bookingRepository.selectByCondition(condition, customerId);
    }
    
    /**
     * Lấy danh sách booking theo ID của thú cưng
     */
    public List<Booking> getBookingsByPetId(int petId) throws Exception {
        String condition = "pet_id = ?";
        return bookingRepository.selectByCondition(condition, petId);
    }
    
    /**
     * Lấy danh sách booking theo ngày
     */
    public List<Booking> getBookingsByDate(LocalDate date) throws Exception {
        String condition = "DATE(booking_time) = ?";
        return bookingRepository.selectByCondition(condition, date);
    }
    
    /**
     * Lấy danh sách booking trong khoảng thời gian
     */
    public List<Booking> getBookingsByDateRange(LocalDate startDate, LocalDate endDate) throws Exception {
        String condition = "DATE(booking_time) BETWEEN ? AND ?";
        return bookingRepository.selectByCondition(condition, startDate, endDate);
    }
    
    /**
     * Lấy danh sách booking theo trạng thái
     */
    public List<Booking> getBookingsByStatus(StatusEnum status) throws Exception {
        String condition = "status = ?";
        return bookingRepository.selectByCondition(condition, status.name());
    }
    
    /**
     * Lấy danh sách booking của một nhân viên trong ngày
     */
    public List<Booking> getBookingsByStaffAndDate(int staffId, LocalDate date) throws Exception {
        String condition = "b.staff_id = ? AND DATE(booking_time) = ?";
        return bookingRepository.selectByCondition(condition, staffId, date);
    }
    
    /**
     * Lấy danh sách booking của một nhân viên trong khoảng thời gian
     * New method implementation (was missing)
     */
    public List<Booking> getBookingsByStaffAndDateRange(int staffId, LocalDate startDate, LocalDate endDate) throws Exception {
        String condition = "b.staff_id = ? AND DATE(booking_time) BETWEEN ? AND ?";
        return bookingRepository.selectByCondition(condition, staffId, startDate, endDate);
    }
    
    /**
     * Tạo booking mới
     */
 // Cập nhật phương thức createBooking trong BookingService.java

    /**
     * Tạo booking mới với logic kiểm tra ngày và trạng thái
     */
    public Booking createBooking(int customerId, int petId, int staffId, LocalDateTime bookingTime, String note) throws Exception {
        // Kiểm tra dữ liệu đầu vào
        if (customerId <= 0 || petId <= 0 || bookingTime == null) {
            throw new Exception("Dữ liệu không hợp lệ");
        }
        
        // THÊM: Kiểm tra booking chỉ được tạo từ ngày hiện tại trở đi
        LocalDateTime now = LocalDateTime.now();
        if (bookingTime.isBefore(now.withHour(0).withMinute(0).withSecond(0).withNano(0))) {
            throw new Exception("Không thể đặt lịch cho ngày trước ngày hiện tại");
        }
        
        // Kiểm tra khách hàng
        Customer customer = customerRepository.selectById(customerId);
        if (customer == null) {
            throw new Exception("Không tìm thấy khách hàng");
        }
        
        // Kiểm tra thú cưng
        Pet pet = petRepository.selectById(petId);
        if (pet == null) {
            throw new Exception("Không tìm thấy thú cưng");
        }
        
        // Kiểm tra nhân viên (nếu có)
        Staff staff = null;
        if (staffId > 0) {
            staff = staffRepository.selectById(staffId);
            if (staff == null) {
                throw new Exception("Không tìm thấy nhân viên");
            }
            
            // Kiểm tra xem nhân viên đã có lịch trùng không
            if (isTimeSlotBooked(staffId, bookingTime)) {
                throw new Exception("Nhân viên đã có lịch vào thời gian này");
            }
        }
        
        // Tạo booking mới
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setPet(pet);
        booking.setStaff(staff);
        booking.setBookingTime(bookingTime);
        
        // THÊM: Xét trạng thái booking dựa trên thời gian
        if (bookingTime.toLocalDate().isEqual(now.toLocalDate())) {
            // Nếu booking là ngày hôm nay -> PENDING
            booking.setStatus(StatusEnum.PENDING);
        } else {
            // Nếu booking là ngày tương lai -> PENDING
            booking.setStatus(StatusEnum.PENDING);
        }
        
        booking.setNote(note);
        
        // Lưu vào cơ sở dữ liệu
        bookingRepository.insert(booking);
        
        return booking;
    }
    /**
     * Cập nhật booking
     */
    public Booking updateBooking(Booking booking) throws Exception {
        // Kiểm tra booking tồn tại không
        Booking existingBooking = bookingRepository.selectById(booking.getBookingId());
        if (existingBooking == null) {
            throw new Exception("Lịch đặt không tồn tại");
        }
        
        // Kiểm tra dữ liệu đầu vào
        if (booking.getCustomer() == null || 
            booking.getPet() == null || 
            booking.getBookingTime() == null || 
            booking.getStatus() == null) {
            throw new Exception("Dữ liệu không hợp lệ");
        }
        
        // Kiểm tra xem thời gian đã được đặt chưa (nếu thay đổi thời gian)
        if (booking.getStaff() != null && 
            !booking.getBookingTime().equals(existingBooking.getBookingTime()) &&
            isTimeSlotBooked(booking.getStaff().getId(), booking.getBookingTime())) {
            throw new Exception("Nhân viên đã có lịch vào thời gian này");
        }
        
        // Cập nhật vào cơ sở dữ liệu
        bookingRepository.update(booking);
        
        return booking;
    }
    
    /**
     * Cập nhật trạng thái booking
     * Modified method to accept StatusEnum directly instead of String
     */
    /**
     * Cập nhật trạng thái lịch đặt
     */
    public Booking updateBookingStatus(int bookingId, StatusEnum status) throws Exception {
        Booking booking = bookingRepository.selectById(bookingId);
        if (booking == null) {
            throw new Exception("Lịch đặt không tồn tại");
        }
        
        // Kiểm tra logic chuyển trạng thái
        validateStatusChange(booking.getStatus(), status);
        
        // Cập nhật trạng thái
        booking.setStatus(status);
        bookingRepository.update(booking);
        
        return booking;
    }
    /**
     * Xóa booking
     */
    public boolean deleteBooking(int bookingId) throws Exception {
        Booking booking = bookingRepository.selectById(bookingId);
        if (booking == null) {
            throw new Exception("Lịch đặt không tồn tại");
        }
        
        // Không cho phép xóa các booking đã hoàn thành
        if (StatusEnum.COMPLETED.equals(booking.getStatus())) {
            throw new Exception("Không thể xóa lịch đặt đã hoàn thành");
        }
        
        return bookingRepository.delete(booking) > 0;
    }
    
    /**
     * Kiểm tra sự thay đổi trạng thái có hợp lệ không
     */
    private void validateStatusChange(StatusEnum currentStatus, StatusEnum newStatus) throws Exception {
        // Nếu đã hủy thì không thể chuyển sang trạng thái khác
        if (StatusEnum.CANCELLED.equals(currentStatus) && !StatusEnum.CANCELLED.equals(newStatus)) {
            throw new Exception("Không thể thay đổi trạng thái của lịch đã hủy");
        }
        
        // Nếu đã hoàn thành thì không thể chuyển sang trạng thái khác
        if (StatusEnum.COMPLETED.equals(currentStatus) && !StatusEnum.COMPLETED.equals(newStatus)) {
            throw new Exception("Không thể thay đổi trạng thái của lịch đã hoàn thành");
        }
        
        // Kiểm tra logic chuyển trạng thái
        switch (currentStatus) {
            case PENDING:
                // Từ pending có thể chuyển sang confirmed, cancelled
                if (!StatusEnum.CONFIRMED.equals(newStatus) && 
                    !StatusEnum.CANCELLED.equals(newStatus) && 
                    !StatusEnum.PENDING.equals(newStatus)) {
                    throw new Exception("Lịch đang chờ xác nhận chỉ có thể chuyển sang trạng thái đã xác nhận hoặc đã hủy");
                }
                break;
                
            case CONFIRMED:
                // Từ confirmed có thể chuyển sang completed, cancelled
                if (!StatusEnum.COMPLETED.equals(newStatus) && 
                    !StatusEnum.CANCELLED.equals(newStatus) && 
                    !StatusEnum.CONFIRMED.equals(newStatus)) {
                    throw new Exception("Lịch đã xác nhận chỉ có thể chuyển sang trạng thái đã hoàn thành hoặc đã hủy");
                }
                break;
            
            default:
                break;
        }
    }
    
    /**
     * Kiểm tra xem thời gian đã được đặt chưa
     */
    private boolean isTimeSlotBooked(int staffId, LocalDateTime bookingTime) throws Exception {
        // Lấy tất cả booking của nhân viên trong ngày
        List<Booking> bookings = getBookingsByStaffAndDate(staffId, bookingTime.toLocalDate());
        
        // Kiểm tra xem có booking nào trùng thời gian không
        for (Booking booking : bookings) {
            LocalDateTime startTime = booking.getBookingTime();
            
            // Giả sử mỗi booking kéo dài 1 giờ
            LocalDateTime endTime = startTime.plusHours(1);
            
            // Nếu thời gian đặt nằm trong khoảng thời gian của một booking đã tồn tại
            if ((bookingTime.isEqual(startTime) || bookingTime.isAfter(startTime)) && 
                bookingTime.isBefore(endTime)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gán nhân viên cho booking
     */
    public Booking assignStaffToBooking(int bookingId, int staffId) throws Exception {
        Booking booking = bookingRepository.selectById(bookingId);
        if (booking == null) {
            throw new Exception("Lịch đặt không tồn tại");
        }
        
        Staff staff = staffRepository.selectById(staffId);
        if (staff == null) {
            throw new Exception("Nhân viên không tồn tại");
        }
        
        // Kiểm tra xem nhân viên có lịch trùng không
        if (isTimeSlotBooked(staffId, booking.getBookingTime())) {
            throw new Exception("Nhân viên đã có lịch vào thời gian này");
        }
        
        booking.setStaff(staff);
        bookingRepository.update(booking);
        
        return booking;
    }
    
    /**
     * Lấy danh sách booking trong ngày hiện tại
     */
    public List<Booking> getTodayBookings() throws Exception {
        return getBookingsByDate(LocalDate.now());
    }
    
    /**
     * Lấy danh sách booking trong tuần hiện tại
     */
    public List<Booking> getCurrentWeekBookings() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        return getBookingsByDateRange(startOfWeek, endOfWeek);
    }
    
    /**
     * Thời lượng phục vụ cho một booking (tính bằng phút)
     */
    public int getBookingDuration(int bookingId) throws Exception {
        // Tạm thời hardcode thời lượng là 60 phút (1 giờ)
        // Trong thực tế, bạn có thể tính tổng thời lượng của tất cả dịch vụ trong booking
        return 60;
    }
    
    /**
     * Tìm kiếm booking theo từ khóa
     * New method implementation (was missing)
     */
    public List<Booking> searchBookings(String keyword) throws Exception {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBookings();
        }
        
        // Tìm kiếm theo tên khách hàng, số điện thoại, hoặc tên thú cưng
        String condition = "c.full_name LIKE ? OR c.phone LIKE ? OR p.name LIKE ?";
        String searchParam = "%" + keyword + "%";
        
        return bookingRepository.selectByCondition(condition, searchParam, searchParam, searchParam);
    }
}