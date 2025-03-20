package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import datatbase.DatabaseConnection;
import entity.Account;
import entity.Role;
import Enum.RoleType;

public class AccountDAO {
    public Account getAccountByUsername(String username) {
        Account account = null;
        Connection con = DatabaseConnection.getConnection();
        String sql = "SELECT a.accountID, a.userName, a.password, a.email, r.roleID, r.roleName " +
                     "FROM Account a JOIN Role r ON a.roleID = r.roleID WHERE a.userName = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Convert String roleName to Enum RoleType
                RoleType roleType = RoleType.valueOf(rs.getString("roleName").toUpperCase());
                Role role = new Role(rs.getInt("roleID"), roleType);
                
                account = new Account(rs.getInt("accountID"), rs.getString("userName"),
                                      rs.getString("password"), rs.getString("email"), role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return account;
    }
}

