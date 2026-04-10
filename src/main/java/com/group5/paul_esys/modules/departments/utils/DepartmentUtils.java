package com.group5.paul_esys.modules.departments.utils;

import com.group5.paul_esys.modules.departments.model.Department;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DepartmentUtils {

  public static Department mapResultSetToDepartment(ResultSet rs) throws SQLException {
    return new Department(
        rs.getLong("id"),
        rs.getString("department_name"),
        rs.getString("department_code"),
        rs.getString("description"),
        rs.getTimestamp("updated_at"),
        rs.getTimestamp("created_at")
    );
  }
}
