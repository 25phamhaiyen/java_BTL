package utils;

import enums.GenderEnum;
import model.Pet;
import model.PetType;
import javafx.scene.control.Alert;

import java.time.LocalDate;

public class PetValidator {

    public static boolean isValid(String name, GenderEnum gender, PetType type, LocalDate dob, String weightText) {
        if (name == null || name.trim().isEmpty()) {
            showAlert("Vui lòng nhập tên thú cưng.");
            return false;
        }

        if (gender == null) {
            showAlert("Vui lòng chọn giới tính.");
            return false;
        }

        if (type == null) {
            showAlert("Vui lòng chọn giống loài.");
            return false;
        }

        if (dob == null || dob.isAfter(LocalDate.now())) {
            showAlert("Ngày sinh không hợp lệ.");
            return false;
        }

        try {
            double weight = Double.parseDouble(weightText);
            if (weight <= 0) {
                showAlert("Cân nặng phải lớn hơn 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Cân nặng không hợp lệ.");
            return false;
        }

        return true;
    }

    public static double parseWeight(String weightText) {
        return Double.parseDouble(weightText);
    }

    private static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Lỗi nhập liệu");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
