package com.group5.paul_esys.modules.curriculum.services;

import com.group5.paul_esys.modules.curriculum.model.Curriculum;
import com.group5.paul_esys.modules.curriculum.utils.CurriculumUtils;
import com.group5.paul_esys.modules.users.services.ConnectionService;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurriculumService {

  private static final CurriculumService INSTANCE = new CurriculumService();
  private static final Logger logger = LoggerFactory.getLogger(CurriculumService.class);

  private CurriculumService() {
  }

  public static CurriculumService getInstance() {
    return INSTANCE;
  }

  private boolean hasCurriculumNameColumn(Connection conn) {
    try {
      DatabaseMetaData metadata = conn.getMetaData();
      try (
        ResultSet rs = metadata.getColumns(null, null, "CURRICULUM", "NAME")
      ) {
        if (rs.next()) {
          return true;
        }
      }

      try (
        ResultSet rs = metadata.getColumns(null, null, "curriculum", "name")
      ) {
        return rs.next();
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public List<Curriculum> getAllCurriculums() {
    List<Curriculum> curriculums = new ArrayList<>();
    try (Connection conn = ConnectionService.getConnection()) {
      String orderColumn = hasCurriculumNameColumn(conn) ? "name" : "semester";
      try (
        PreparedStatement ps = conn.prepareStatement(
          "SELECT * FROM curriculum ORDER BY cur_year DESC, " + orderColumn
        );
        ResultSet rs = ps.executeQuery()
      ) {
        while (rs.next()) {
          curriculums.add(CurriculumUtils.mapResultSetToCurriculum(rs));
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return curriculums;
  }

  public Optional<Curriculum> getCurriculumById(Long id) {
    try (
      Connection conn = ConnectionService.getConnection();
      PreparedStatement ps = conn.prepareStatement(
        "SELECT * FROM curriculum WHERE id = ?"
      )
    ) {
      ps.setLong(1, id);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(CurriculumUtils.mapResultSetToCurriculum(rs));
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return Optional.empty();
  }

  public List<Curriculum> getCurriculumsByCourse(Long courseId) {
    List<Curriculum> curriculums = new ArrayList<>();
    try (Connection conn = ConnectionService.getConnection()) {
      String orderColumn = hasCurriculumNameColumn(conn) ? "name" : "semester";
      try (
        PreparedStatement ps = conn.prepareStatement(
          "SELECT * FROM curriculum WHERE course = ? ORDER BY cur_year DESC, "
            + orderColumn
        )
      ) {
        ps.setLong(1, courseId);

        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            curriculums.add(CurriculumUtils.mapResultSetToCurriculum(rs));
          }
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return curriculums;
  }

  public boolean createCurriculum(Curriculum curriculum) {
    try (Connection conn = ConnectionService.getConnection()) {
      String sql;
      if (hasCurriculumNameColumn(conn)) {
        sql = "INSERT INTO curriculum (name, cur_year, course) VALUES (?, ?, ?)";
      } else {
        sql =
          "INSERT INTO curriculum (semester, cur_year, course) VALUES (?, ?, ?)";
      }

      try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, curriculum.getName());
        ps.setDate(2, new java.sql.Date(curriculum.getCurYear().getTime()));
        ps.setLong(3, curriculum.getCourse());

        return ps.executeUpdate() > 0;
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public boolean updateCurriculum(Curriculum curriculum) {
    try (Connection conn = ConnectionService.getConnection()) {
      String sql;
      if (hasCurriculumNameColumn(conn)) {
        sql = "UPDATE curriculum SET name = ?, cur_year = ?, course = ? WHERE id = ?";
      } else {
        sql =
          "UPDATE curriculum SET semester = ?, cur_year = ?, course = ? WHERE id = ?";
      }

      try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, curriculum.getName());
        ps.setDate(2, new java.sql.Date(curriculum.getCurYear().getTime()));
        ps.setLong(3, curriculum.getCourse());
        ps.setLong(4, curriculum.getId());

        return ps.executeUpdate() > 0;
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public boolean deleteCurriculum(Long id) {
    try (
      Connection conn = ConnectionService.getConnection();
      PreparedStatement ps = conn.prepareStatement(
        "DELETE FROM curriculum WHERE id = ?"
      )
    ) {
      ps.setLong(1, id);

      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public float getTotalUnitsForSemester(Long curriculumId, String semesterLabel) {
    String sql = "SELECT COALESCE(SUM(s.units), 0) as total_units " +
                 "FROM semester sem " +
                 "JOIN semester_subjects ss ON ss.semester_id = sem.id " +
                 "JOIN subjects s ON s.id = ss.subject_id " +
                 "WHERE sem.curriculum_id = ? AND sem.semester = ?";

    try (
      Connection conn = ConnectionService.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql)
    ) {
      ps.setLong(1, curriculumId);
      ps.setString(2, semesterLabel);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getFloat("total_units");
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }

    return 0.0f;
  }
}
