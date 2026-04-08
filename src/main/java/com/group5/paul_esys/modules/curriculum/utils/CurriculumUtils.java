package com.group5.paul_esys.modules.curriculum.utils;

import com.group5.paul_esys.modules.curriculum.model.Curriculum;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CurriculumUtils {

  public static Curriculum mapResultSetToCurriculum(ResultSet rs) throws SQLException {
    String name;
    try {
      name = rs.getString("name");
    } catch (SQLException ignored) {
      name = rs.getString("semester");
    }

    return new Curriculum(
        rs.getLong("id"),
        name,
        rs.getDate("cur_year"),
        rs.getLong("course")
    );
  }
}
