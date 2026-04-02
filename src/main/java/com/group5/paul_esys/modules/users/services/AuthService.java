package com.group5.paul_esys.modules.users.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group5.paul_esys.modules.users.models.user.LoginData;
import com.group5.paul_esys.modules.users.models.user.UserInformation;
import com.group5.paul_esys.modules.users.utils.UserUtils;

public class AuthService {

  private static final AuthService INSTANCE = new AuthService();
  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  private AuthService() {
  }

  public static AuthService getInstance() {
    return INSTANCE;
  }

  public Optional<UserInformation> login(LoginData loginData) {
    if (!loginData.isValid()) {
      throw new IllegalArgumentException("Invalid login data");
    }

    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE email = ?")) {
      ps.setString(1, loginData.getEmail());

      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          return Optional.empty();
        }

        Optional<UserInformation> user = Optional.of(UserUtils.mapResultSetToUserInformation(rs));
        if (user.isPresent()) {
          if (user.get().verifyPassword(loginData.getPassword())) {
            return user;
          }

          return Optional.empty();
        }

        return Optional.empty();
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return Optional.empty();
    }
  }
}
