package com.group5.paul_esys.modules.courses.utils;

import com.group5.paul_esys.modules.courses.model.Course;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CourseUtils {

  public static Course mapResultSetToCourse(ResultSet rs) throws SQLException {
    return new Course(
        rs.getLong("id"),
        rs.getString("course_name"),
        rs.getString("description"),
        rs.getLong("department_id"),
        rs.getTimestamp("updated_at"),
        rs.getTimestamp("created_at")
    );
  }
}
