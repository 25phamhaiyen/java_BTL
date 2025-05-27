package utils;

import java.util.*;

public class LanguageManagerAd {
    private static Locale currentLocale = new Locale("vi", "VN"); // mặc định
    private static ResourceBundle bundle = ResourceBundle.getBundle("lang.messages", currentLocale);
    private static final List<LanguageChangeListener> listeners = new ArrayList<>();

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        bundle = ResourceBundle.getBundle("lang.messages", locale);
        notifyListeners();
    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    public static String getString(String key, Object... params) {
        String pattern = bundle.getString(key);
        return params.length > 0 ? java.text.MessageFormat.format(pattern, params) : pattern;
    }

    public static void addListener(LanguageChangeListener listener) {
        listeners.add(listener);
    }

    private static void notifyListeners() {
        for (LanguageChangeListener listener : listeners) {
            listener.onLanguageChanged();
        }
    }
}
