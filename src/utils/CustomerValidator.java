package utils;

import javafx.scene.control.Alert;

public class CustomerValidator {
    public static boolean isValid(String name, String phone, String email, String address, String pointsStr) {
        if (name == null || name.isBlank()) {
            showAlert("Tên không được để trống.");
            return false;
        }

        if (phone == null || !phone.matches("^0\\d{9}$")) {
            showAlert("Số điện thoại phải bắt đầu bằng 0 và đủ 10 chữ số.");
            return false;
        }

        if (email != null && !email.isBlank() && !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showAlert("Email không hợp lệ.");
            return false;
        }

        if (address == null || address.isBlank()) {
            showAlert("Địa chỉ không được để trống.");
            return false;
        }

        try {
            int points = Integer.parseInt(pointsStr);
            if (points < 0) {
                showAlert("Điểm tích lũy không được âm.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Điểm tích lũy phải là số nguyên.");
            return false;
        }

        return true;
    }

    private static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Dữ liệu không hợp lệ");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
