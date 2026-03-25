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

  private final Connection conn = ConnectionService.getConnection();
  private final Logger logger = LoggerFactory.getLogger(AuthService.class);

  public Optional<UserInformation> login(LoginData loginData) {
    if (!loginData.isValid()) {
      throw new IllegalArgumentException("Invalid login data");
    }

    try {
      PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE email = ?");
      ps.setString(1, loginData.getEmail());

      ResultSet rs = ps.executeQuery();
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
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return Optional.empty();
    }
  }
}
