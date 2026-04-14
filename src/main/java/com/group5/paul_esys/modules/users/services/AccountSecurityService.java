package com.group5.paul_esys.modules.users.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountSecurityService {

    private static final Logger logger = LoggerFactory.getLogger(AccountSecurityService.class);
    private static AccountSecurityService instance;

    private AccountSecurityService() {
    }

    public static synchronized AccountSecurityService getInstance() {
        if (instance == null) {
            instance = new AccountSecurityService();
        }
        return instance;
    }

    public boolean verifyCurrentPassword(Long userId, String currentPlainPassword) {
        if (userId == null || currentPlainPassword == null || currentPlainPassword.isBlank()) {
            return false;
        }

        String selectSql = "SELECT password FROM users WHERE id = ?";
        try (
            Connection conn = ConnectionService.getConnection();
            PreparedStatement stmt = conn.prepareStatement(selectSql)
        ) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                String storedHash = rs.getString("password");
                if (storedHash == null || storedHash.isBlank()) {
                    return false;
                }

                BCrypt.Result result = BCrypt.verifyer().verify(
                    currentPlainPassword.toCharArray(),
                    storedHash.toCharArray()
                );
                return result.verified;
            }
        } catch (SQLException ex) {
            logger.error("ERROR: {}", ex.getMessage(), ex);
            return false;
        }
    }

    public boolean updatePassword(Long userId, String newPlainPassword) {
        if (userId == null || newPlainPassword == null || newPlainPassword.isBlank()) {
            return false;
        }

        String updateSql = "UPDATE users SET password = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        String hashedPassword = BCrypt
            .withDefaults()
            .hashToString(12, newPlainPassword.toCharArray());

        try (
            Connection conn = ConnectionService.getConnection();
            PreparedStatement stmt = conn.prepareStatement(updateSql)
        ) {
            stmt.setString(1, hashedPassword);
            stmt.setLong(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.error("ERROR: {}", ex.getMessage(), ex);
            return false;
        }
    }
}
