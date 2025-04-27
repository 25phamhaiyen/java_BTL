package repository;

import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.scene.chart.XYChart;

public class RevenueRepository {

	public double getMonthlyRevenue() {
		String query = "SELECT SUM(total) AS total_revenue " + "FROM invoice "
				+ "WHERE MONTH(payment_date) = MONTH(CURRENT_DATE) " + "AND YEAR(payment_date) = YEAR(CURRENT_DATE) "
				+ "AND `status` = 'COMPLETED'";
		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(query);
				ResultSet resultSet = statement.executeQuery()) {

			if (resultSet.next()) {
				return resultSet.getDouble("total_revenue");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0.0; // Return 0 if no data is found or an error occurs
	}public XYChart.Series<String, Number> getRevenueData(String timeUnit) {
        String query = "";
        if (timeUnit.equals("WEEK")) {
            query = "SELECT WEEK(payment_date) AS time, SUM(total) AS revenue " +
                    "FROM invoice WHERE `status` = 'COMPLETED' " +
                    "GROUP BY WEEK(payment_date)";
        } else if (timeUnit.equals("MONTH")) {
            query = "SELECT MONTH(payment_date) AS time, SUM(total) AS revenue " +
                    "FROM invoice WHERE `status` = 'COMPLETED' " +
                    "GROUP BY MONTH(payment_date)";
        } else if (timeUnit.equals("YEAR")) {
            query = "SELECT YEAR(payment_date) AS time, SUM(total) AS revenue " +
                    "FROM invoice WHERE `status` = 'COMPLETED' " +
                    "GROUP BY YEAR(payment_date)";
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String time = resultSet.getString("time");
                double revenue = resultSet.getDouble("revenue");
                series.getData().add(new XYChart.Data<>(time, revenue));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return series;
    }
}