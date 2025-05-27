package utils;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class LanguageManager {
    private static LanguageManager instance;
    private ResourceBundle resourceBundle;
    private Locale currentLocale;
    private Preferences prefs;
    
    // Observable properties for UI binding
    private final ObservableList<StringProperty> observableStrings = FXCollections.observableArrayList();
    
    // Supported languages
    @SuppressWarnings("deprecation")
	public static final Locale ENGLISH = new Locale("en");
    @SuppressWarnings("deprecation")
	public static final Locale VIETNAMESE = new Locale("vi");
    
    private LanguageManager() {
        prefs = Preferences.userNodeForPackage(LanguageManager.class);
        // Load saved language or default to English
        String savedLang = prefs.get("language", "en");
        currentLocale = savedLang.equals("vi") ? VIETNAMESE : ENGLISH;
        loadResourceBundle();
    }
    
    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }
    
    private void loadResourceBundle() {
        try {
            resourceBundle = ResourceBundle.getBundle("lang.messages", currentLocale);
        } catch (Exception e) {
            // Fallback to default
            resourceBundle = ResourceBundle.getBundle("lang.messages", ENGLISH);
        }
    }
    
    public String getString(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (Exception e) {
            return key; // Return key if translation not found
        }
    }
    
    public void changeLanguage(Locale locale) {
        currentLocale = locale;
        loadResourceBundle();
        
        // Save preference
        prefs.put("language", locale.getLanguage());
        
        // Update all observable strings
        updateObservableStrings();
    }
    
    public Locale getCurrentLocale() {
        return currentLocale;
    }
    
    public String getCurrentLanguageName() {
        return currentLocale.equals(VIETNAMESE) ? "Tiếng Việt" : "English";
    }
    
    // Create observable string property that updates when language changes
    public StringProperty createStringProperty(String key) {
        StringProperty property = new SimpleStringProperty(getString(key));
        observableStrings.add(property);
        return property;
    }
    
    // Update all observable strings when language changes
    private void updateObservableStrings() {
        // This would require keeping track of keys, which is more complex
        // For now, we'll use a simpler approach with manual updates
    }
    
    // Utility method to get formatted string
    public String getString(String key, Object... args) {
        String message = resourceBundle.getString(key);
        if (args.length > 0) {
            return MessageFormat.format(message, args);
        }
        return message;
    }
}
