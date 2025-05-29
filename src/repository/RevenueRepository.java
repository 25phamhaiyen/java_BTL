package repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.chart.XYChart;
import model.RevenueReport;
import utils.DatabaseConnection;

public class RevenueRepository {

	public XYChart.Series<String, Number> getRevenueData(String timeUnit) {
	    XYChart.Series<String, Number> series = new XYChart.Series<>();
	    String sql = "";

	    switch (timeUnit.toUpperCase()) {
	        case "WEEK":
	            sql = """
	                SELECT
	                    CONCAT(DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL (n - 1) WEEK), '%d/%m'),
	                           ' - ',
	                           DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL (n - 1) WEEK) + INTERVAL 6 DAY, '%d/%m')) AS label,
	                    COALESCE(SUM(i.total), 0) AS revenue
	                FROM (
	                    SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
	                ) AS weeks
	                LEFT JOIN invoice i ON i.status = 'COMPLETED'
	                    AND i.payment_date >= DATE_SUB(CURDATE(), INTERVAL (weeks.n - 1) WEEK)
	                    AND i.payment_date < DATE_SUB(CURDATE(), INTERVAL (weeks.n - 2) WEEK)
	                GROUP BY label
	                ORDER BY MIN(DATE_SUB(CURDATE(), INTERVAL (weeks.n - 1) WEEK))
	                """;
	            break;

	        case "MONTH":
	            sql = """
	                SELECT
	                    DATE_FORMAT(payment_date, '%m/%Y') AS label,
	                    SUM(total) AS revenue
	                FROM invoice
	                WHERE status = 'COMPLETED'
	                    AND YEAR(payment_date) = YEAR(CURDATE())
	                GROUP BY label
	                ORDER BY STR_TO_DATE(label, '%m/%Y') 
	                """;
//	        	sql = """
//                SELECT
//                    DATE_FORMAT(payment_date, '%m/%Y') AS label,
//                    SUM(total) AS revenue,
//                    payment_date AS order_date  -- Thêm cột này để sắp xếp
//                FROM invoice
//                WHERE status = 'COMPLETED'
//                    AND payment_date >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH)
//                    AND payment_date <= CURDATE()
//                GROUP BY label, payment_date
//                ORDER BY order_date ASC
//                """;
	            break;

	        case "YEAR":
	            sql = """
	                SELECT
	                    YEAR(payment_date) AS label,
	                    SUM(total) AS revenue
	                FROM invoice
	                WHERE status = 'COMPLETED'
	                    AND YEAR(payment_date) >= YEAR(CURDATE()) - 3
	                GROUP BY label
	                ORDER BY label
	                """;
	            break;

	        default:
	            return series;
	    }

	    try (Connection conn = DatabaseConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql);
	         ResultSet rs = stmt.executeQuery()) {
	        while (rs.next()) {
	            String label = rs.getString("label");
	            double revenue = rs.getDouble("revenue");
	            series.getData().add(new XYChart.Data<>(label, revenue));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return series;
	}


	public double getRevenueTotal(String timeUnit) {
		String sql = switch (timeUnit.toUpperCase()) {
		case "WEEK" -> """
				    SELECT SUM(total) FROM invoice
				    WHERE YEARWEEK(payment_date, 1) = YEARWEEK(CURDATE(), 1)
				    AND status = 'COMPLETED'
				""";
		case "MONTH" -> """
				    SELECT SUM(total) FROM invoice
				    WHERE YEAR(payment_date) = YEAR(CURDATE()) AND MONTH(payment_date) = MONTH(CURDATE())
				    AND status = 'COMPLETED'
				""";
		case "YEAR" -> """
				    SELECT SUM(total) FROM invoice
				    WHERE YEAR(payment_date) = YEAR(CURDATE())
				    AND status = 'COMPLETED'
				""";
		default -> "";
		};

		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			if (rs.next())
				return rs.getDouble(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return 0.0;
	}

}