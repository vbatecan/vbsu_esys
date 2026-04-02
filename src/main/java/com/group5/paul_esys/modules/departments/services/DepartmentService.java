package com.group5.paul_esys.modules.departments.services;

import com.group5.paul_esys.modules.departments.model.Department;
import com.group5.paul_esys.modules.departments.utils.DepartmentUtils;
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

public class DepartmentService {

  private static final DepartmentService INSTANCE = new DepartmentService();
  private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);

  private DepartmentService() {
  }

  public static DepartmentService getInstance() {
    return INSTANCE;
  }

  public List<Department> getAllDepartments() {
    List<Department> departments = new ArrayList<>();
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM departments ORDER BY department_name");
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        departments.add(DepartmentUtils.mapResultSetToDepartment(rs));
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return departments;
  }

  public Optional<Department> getDepartmentById(Long id) {
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM departments WHERE id = ?")) {
      ps.setLong(1, id);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(DepartmentUtils.mapResultSetToDepartment(rs));
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return Optional.empty();
  }

  public boolean createDepartment(Department department) {
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO departments (department_name, description) VALUES (?, ?)"
        )) {
      ps.setString(1, department.getDepartmentName());
      ps.setString(2, department.getDescription());
      
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public boolean updateDepartment(Department department) {
    try (Connection conn = ConnectionService.getConnection();
      PreparedStatement ps = conn.prepareStatement(
        "UPDATE departments SET department_name = ?, description = ? WHERE id = ?"
      )) {
      ps.setString(1, department.getDepartmentName());
      ps.setString(2, department.getDescription());
      ps.setLong(3, department.getId());
      
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public boolean deleteDepartment(Long id) {
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("DELETE FROM departments WHERE id = ?")) {
      ps.setLong(1, id);
      
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }
}
