// src/frontend1/service/ServiceService.java
package service;

import model.Service;
import utils.DatabaseConnection;
import enums.TypeServiceEnum;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceService {
    public List<Service> getAllServices() { // <-- Phải có "public" và đúng tên
        List<Service> services = new ArrayList<>();
        String sql = "SELECT * FROM service";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Service service = new Service(
                    rs.getInt("serviceID"),
                    rs.getString("serviceName"),
                    rs.getDouble("CostPrice"),
                    TypeServiceEnum.fromId(rs.getInt("TypeServiceID")),
                    rs.getString("description")
                );
                services.add(service);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return services;
    }
}