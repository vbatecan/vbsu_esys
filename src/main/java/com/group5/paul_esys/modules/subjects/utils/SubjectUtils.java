package com.group5.paul_esys.modules.subjects.utils;

import com.group5.paul_esys.modules.subjects.model.Subject;
import com.group5.paul_esys.modules.subjects.model.SubjectSchedulePattern;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class SubjectUtils {

  public static Subject mapResultSetToSubject(ResultSet rs) throws SQLException {
    Integer estimatedTime = null;
    String schedulePattern = null;

    if (hasColumn(rs, "estimated_time")) {
      estimatedTime = rs.getObject("estimated_time", Integer.class);
    }

    if (hasColumn(rs, "schedule_pattern")) {
      schedulePattern = rs.getString("schedule_pattern");
    }

    if (estimatedTime == null || estimatedTime <= 0) {
      estimatedTime = 90;
    }

    return new Subject(
        rs.getLong("id"),
        rs.getString("subject_name"),
        rs.getString("subject_code"),
        rs.getFloat("units"),
        estimatedTime,
        SubjectSchedulePattern.fromValue(schedulePattern),
        rs.getString("description"),
        rs.getLong("department_id"),
        rs.getTimestamp("updated_at"),
        rs.getTimestamp("created_at")
    );
  }

  private static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
    ResultSetMetaData metadata = rs.getMetaData();
    for (int index = 1; index <= metadata.getColumnCount(); index++) {
      String label = metadata.getColumnLabel(index);
      String name = metadata.getColumnName(index);

      if (columnName.equalsIgnoreCase(label) || columnName.equalsIgnoreCase(name)) {
        return true;
      }
    }

    return false;
  }
}
