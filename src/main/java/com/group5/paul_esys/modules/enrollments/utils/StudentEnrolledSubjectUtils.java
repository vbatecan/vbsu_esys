package com.group5.paul_esys.modules.enrollments.utils;

import com.group5.paul_esys.modules.enums.StudentEnrolledSubjectStatus;
import com.group5.paul_esys.modules.enrollments.model.StudentEnrolledSubject;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentEnrolledSubjectUtils {

  public static StudentEnrolledSubject mapResultSetToStudentEnrolledSubject(ResultSet rs) throws SQLException {
    boolean selected = false;
    try {
      selected = rs.getBoolean("is_selected");
      if (rs.wasNull()) {
        selected = false;
      }
    } catch (SQLException ignored) {
      selected = false;
    }

    return new StudentEnrolledSubject(
        rs.getString("student_id"),
        rs.getLong("enrollment_id"),
        rs.getLong("offering_id"),
        rs.getLong("semester_subject_id"),
        StudentEnrolledSubjectStatus.valueOf(rs.getString("status")),
        selected,
        rs.getTimestamp("created_at"),
        rs.getTimestamp("updated_at")
    );
  }
}