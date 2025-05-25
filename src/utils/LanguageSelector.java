package utils;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.StringConverter;

import java.util.Locale;

public class LanguageSelector extends ComboBox<Locale> {
    
    public LanguageSelector() {
        setupLanguageSelector();
    }
    
    private void setupLanguageSelector() {
        // Add supported languages
        getItems().addAll(LanguageManager.ENGLISH, LanguageManager.VIETNAMESE);
        
        // Set current language as selected
        setValue(LanguageManager.getInstance().getCurrentLocale());
        
        // Custom cell factory for displaying language names
        setCellFactory(listView -> new LanguageCell());
        setButtonCell(new LanguageCell());
        
        // String converter for display
        setConverter(new StringConverter<Locale>() {
            @Override
            public String toString(Locale locale) {
                if (locale == null) return "";
                return locale.equals(LanguageManager.VIETNAMESE) ? "Tiếng Việt" : "English";
            }
            
            @Override
            public Locale fromString(String string) {
                return "Tiếng Việt".equals(string) ? LanguageManager.VIETNAMESE : LanguageManager.ENGLISH;
            }
        });
        
        // Handle language change
        setOnAction(event -> {
            Locale selectedLocale = getValue();
            if (selectedLocale != null && !selectedLocale.equals(LanguageManager.getInstance().getCurrentLocale())) {
                I18nUtil.switchLanguage(selectedLocale);
            }
        });
        
        // Set preferred width
        setPrefWidth(120);
    }
    
    private static class LanguageCell extends ListCell<Locale> {
        @Override
        protected void updateItem(Locale locale, boolean empty) {
            super.updateItem(locale, empty);
            
            if (empty || locale == null) {
                setText(null);
                setGraphic(null);
            } else {
                if (locale.equals(LanguageManager.VIETNAMESE)) {
                    setText("Tiếng Việt");
                    // You can add flag icons here if you have them
                    // setGraphic(new ImageView(new Image("/images/vn-flag.png")));
                } else {
                    setText("English");
                    // setGraphic(new ImageView(new Image("/images/us-flag.png")));
                }
            }
        }
    }
}