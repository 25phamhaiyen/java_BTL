package utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import java.time.LocalTime;
public class ValidatorUtil {
    
    // Patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZÀ-ỹ\\s]+$");
    private static final Pattern PET_NAME_PATTERN = Pattern.compile("^[a-zA-ZÀ-ỹ0-9\\s]+$");
    
    // Validation cho tên người
    public static void validatePersonName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên không được để trống.");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Tên không được quá 100 ký tự.");
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Tên chỉ được chứa chữ cái và khoảng trắng.");
        }
    }
    
    // Validation cho tên thú cưng
    public static void validatePetName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên thú cưng không được để trống.");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("Tên thú cưng không được quá 50 ký tự.");
        }
        if (!PET_NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Tên thú cưng chỉ được chứa chữ cái, số và khoảng trắng.");
        }
    }
    
    // Validation cho số điện thoại
    public static void validatePhone(String phone) {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("Số điện thoại phải bắt đầu bằng 0 và có đúng 10 chữ số.");
        }
    }
    
    // Validation cho email
    public static void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email không hợp lệ.");
        }
        if (email.length() > 100) {
            throw new IllegalArgumentException("Email không được quá 100 ký tự.");
        }
    }
    
    // Validation cho địa chỉ
    public static void validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ không được để trống.");
        }
        if (address.length() > 200) {
            throw new IllegalArgumentException("Địa chỉ không được quá 200 ký tự.");
        }
    }
    
    // Validation cho ngày sinh
    public static void validateBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException("Ngày sinh không được để trống.");
        }
        if (birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày sinh không thể là ngày trong tương lai.");
        }
        if (birthDate.isBefore(LocalDate.of(1900, 1, 1))) {
            throw new IllegalArgumentException("Ngày sinh không hợp lệ (quá xa).");
        }
    }
    
    // Validation cho cân nặng thú cưng
    public static void validatePetWeight(double weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Cân nặng phải lớn hơn 0.");
        }
        if (weight > 200) {
            throw new IllegalArgumentException("Cân nặng không hợp lệ (quá lớn).");
        }
    }
    
    // Validation cho ngày đặt lịch
    public static void validateBookingDateTime(LocalDateTime bookingDateTime) {
        LocalDate bookingDate = bookingDateTime.toLocalDate();
        if (bookingDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày hẹn không thể là ngày quá khứ.");
        }
        if (bookingDate.isAfter(LocalDate.now().plusYears(1))) {
            throw new IllegalArgumentException("Ngày hẹn không thể quá xa trong tương lai (tối đa 1 năm).");
        }
        
        if (bookingDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian đặt lịch phải là thời gian trong tương lai.");
        }
    }
    
    // Validation cho giờ làm việc
    public static void validateWorkingHours(LocalTime time) {
        if (time.isBefore(LocalTime.of(8, 0)) || time.isAfter(LocalTime.of(22, 0))) {
            throw new IllegalArgumentException("Giờ hẹn phải từ 8:00 đến 22:00.");
        }
    }
}