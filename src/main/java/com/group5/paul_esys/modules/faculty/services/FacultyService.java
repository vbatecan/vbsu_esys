package com.group5.paul_esys.modules.faculty.services;

import com.group5.paul_esys.modules.faculty.model.Faculty;
import com.group5.paul_esys.modules.faculty.utils.FacultyUtils;
import com.group5.paul_esys.modules.users.services.ConnectionService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FacultyService {

  private static final FacultyService INSTANCE = new FacultyService();
  private static final Logger logger = LoggerFactory.getLogger(FacultyService.class);

  private FacultyService() {
  }

  public static FacultyService getInstance() {
    return INSTANCE;
  }

  public List<Faculty> getAllFaculty() {
    List<Faculty> faculty = new ArrayList<>();
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM faculty ORDER BY last_name, first_name");
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        faculty.add(FacultyUtils.mapResultSetToFaculty(rs));
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return faculty;
  }

  public Optional<Faculty> getFacultyById(Long id) {
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM faculty WHERE id = ?")) {
      ps.setLong(1, id);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(FacultyUtils.mapResultSetToFaculty(rs));
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return Optional.empty();
  }

  public Optional<Faculty> getFacultyByUserId(Long userId) {
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM faculty WHERE user_id = ?")) {
      ps.setLong(1, userId);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(FacultyUtils.mapResultSetToFaculty(rs));
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return Optional.empty();
  }

  public List<Faculty> getFacultyByDepartment(Long departmentId) {
    List<Faculty> faculty = new ArrayList<>();
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM faculty WHERE department_id = ? ORDER BY last_name, first_name")) {
      ps.setLong(1, departmentId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          faculty.add(FacultyUtils.mapResultSetToFaculty(rs));
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return faculty;
  }

  public boolean createFaculty(Faculty faculty) {
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO faculty (user_id, first_name, last_name, department_id) VALUES (?, ?, ?, ?)"
        )) {
      ps.setLong(1, faculty.getUserId());
      ps.setString(2, faculty.getFirstName());
      ps.setString(3, faculty.getLastName());
      ps.setLong(4, faculty.getDepartmentId());
      
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public boolean updateFaculty(Faculty faculty) {
    try (Connection conn = ConnectionService.getConnection();
      PreparedStatement ps = conn.prepareStatement(
        "UPDATE faculty SET user_id = ?, first_name = ?, last_name = ?, department_id = ? WHERE id = ?"
      )) {
      ps.setLong(1, faculty.getUserId());
      ps.setString(2, faculty.getFirstName());
      ps.setString(3, faculty.getLastName());
      ps.setLong(4, faculty.getDepartmentId());
      ps.setLong(5, faculty.getId());
      
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public boolean deleteFaculty(Long id) {
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("DELETE FROM faculty WHERE id = ?")) {
      ps.setLong(1, id);
      
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }
}
