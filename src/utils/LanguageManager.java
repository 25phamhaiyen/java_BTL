//package utils;
//import java.util.*;
//
//public class LanguageManager {
//    private static Locale currentLocale = new Locale("vi", "VN"); // mặc định
//    private static ResourceBundle bundle = ResourceBundle.getBundle("lang.messages", currentLocale);
//    private static final List<LanguageChangeListener> listeners = new ArrayList<>();
//    
//    /**
//     * Thiết lập ngôn ngữ mới và thông báo cho tất cả các listeners
//     */
//    public static void setLocale(Locale locale) {
//        currentLocale = locale;
//        bundle = ResourceBundle.getBundle("lang.messages", locale);
//        notifyListeners();
//    }
//    
//    /**
//     * Lấy locale hiện tại
//     */
//    public static Locale getCurrentLocale() {
//        return currentLocale;
//    }
//    
//    /**
//     * Lấy chuỗi dịch theo key và tham số
//     */
//    public static String getString(String key, Object... params) {
//        try {
//            String pattern = bundle.getString(key);
//            return params.length > 0 ? java.text.MessageFormat.format(pattern, params) : pattern;
//        } catch (MissingResourceException e) {
//            return "!" + key + "!"; // Trả về key nếu không tìm thấy
//        }
//    }
//    
//    /**
//     * Thêm listener để lắng nghe sự thay đổi ngôn ngữ
//     */
//    public static void addListener(LanguageChangeListener listener) {
//        if (listener != null && !listeners.contains(listener)) {
//            listeners.add(listener);
//        }
//    }
//    
//    /**
//     * Xóa listener
//     */
//    public static void removeListener(LanguageChangeListener listener) {
//        listeners.remove(listener);
//    }
//    
//    /**
//     * Xóa tất cả listeners
//     */
//    public static void clearListeners() {
//        listeners.clear();
//    }
//    
//    /**
//     * Thông báo cho tất cả listeners về sự thay đổi ngôn ngữ
//     */
//    private static void notifyListeners() {
//        for (LanguageChangeListener listener : listeners) {
//            try {
//                listener.onLanguageChanged();
//            } catch (Exception e) {
//                e.printStackTrace(); // Log lỗi nhưng không dừng việc thông báo
//            }
//        }
//    }
//    
//    /**
//     * Lấy danh sách các ngôn ngữ có sẵn
//     */
//    public static List<Locale> getAvailableLocales() {
//        return Arrays.asList(
//            new Locale("vi", "VN"), // Tiếng Việt
//            new Locale("en", "US"), // Tiếng Anh
//            new Locale("zh", "CN")  // Tiếng Trung (nếu cần)
//        );
//    }
//    
//    /**
//     * Kiểm tra xem có bao nhiêu listeners đang đăng ký
//     */
//    public static int getListenerCount() {
//        return listeners.size();
//    }
//}