package com.group5.paul_esys.modules.user.services;

import com.group5.paul_esys.modules.config.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionService {

  /**
   *
   * Gagawa siya ng connection sa database.
   *
   * @return - Yung connection pero if nagfail magrreturn ng null
   */
  public static Connection getConnection() {
    try {
      return DriverManager.getConnection(Config.DB_URL, Config.DB_USER, Config.DB_PASS);
    } catch (SQLException e) {
      System.out.println("ERROR: " + e.getMessage());
    }

    return null;
  }
}
