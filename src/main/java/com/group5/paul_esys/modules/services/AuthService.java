package com.group5.paul_esys.modules.services;

import com.group5.paul_esys.modules.models.enums.Role;
import com.group5.paul_esys.modules.models.user.UserInformation;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

  public Connection conn = ConnectionService.getConnection();

  public Optional<UserInformation> login() {
    try {
      PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?");

      // Map the result to UserInformation
      ResultSet rs = ps.executeQuery();
    } catch (SQLException e) {
      System.out.println("ERROR: " + e.getMessage());
    }

    return Optional.empty();
  }
}
