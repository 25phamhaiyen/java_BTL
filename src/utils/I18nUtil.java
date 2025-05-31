	package utils;
	
	import javafx.scene.control.*;
	import javafx.stage.Stage;
	
	import java.util.ArrayList;
	import java.util.List;
	import java.util.Locale;
	
	public class I18nUtil {
	    private static final List<I18nUpdatable> updatableComponents = new ArrayList<>();
	    
	    public interface I18nUpdatable {
	        void updateLanguage();
	    }
	    
	    // Register component for automatic language updates
	    public static void register(I18nUpdatable component) {
	        updatableComponents.add(component);
	    }
	    
	    // Update all registered components
	    public static void updateAllComponents() {
	        updatableComponents.forEach(I18nUpdatable::updateLanguage);
	    }
	    
	    // Utility methods for common UI components
	    public static void setText(Label label, String key) {
	        if (label != null) {
	            label.setText(LanguageManager.getInstance().getString(key));
	        }
	    }
	    
	    public static void setText(Button button, String key) {
	        if (button != null) {
	            button.setText(LanguageManager.getInstance().getString(key));
	        }
	    }
	    
	    public static void setText(MenuItem menuItem, String key) {
	        if (menuItem != null) {
	            menuItem.setText(LanguageManager.getInstance().getString(key));
	        }
	    }
	    
	    public static void setTitle(Stage stage, String key) {
	        if (stage != null) {
	            stage.setTitle(LanguageManager.getInstance().getString(key));
	        }
	    }
	    
	    public static void setPromptText(TextField textField, String key) {
	        if (textField != null) {
	            textField.setPromptText(LanguageManager.getInstance().getString(key));
	        }
	    }
	    
	    public static void setPromptText(TextArea textArea, String key) {
	        if (textArea != null) {
	            textArea.setPromptText(LanguageManager.getInstance().getString(key));
	        }
	    }
	    
	    public static void setHeaderText(TableColumn<?, ?> column, String key) {
	        if (column != null) {
	            column.setText(LanguageManager.getInstance().getString(key));
	        }
	    }
	    
	    // Utility for alerts
	    public static void showAlert(Alert.AlertType type, String titleKey, String messageKey) {
	        Alert alert = new Alert(type);
	        alert.setTitle(LanguageManager.getInstance().getString(titleKey));
	        alert.setHeaderText(null);
	        alert.setContentText(LanguageManager.getInstance().getString(messageKey));
	        alert.showAndWait();
	    }
	    
	    public static boolean showConfirmation(String titleKey, String messageKey) {
	        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
	        alert.setTitle(LanguageManager.getInstance().getString(titleKey));
	        alert.setHeaderText(null);
	        alert.setContentText(LanguageManager.getInstance().getString(messageKey));
	        
	        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
	    }
	    
	    // Language switching
	    public static void switchLanguage(Locale locale) {
	        LanguageManager.getInstance().changeLanguage(locale);
	        updateAllComponents();
	    }
	}