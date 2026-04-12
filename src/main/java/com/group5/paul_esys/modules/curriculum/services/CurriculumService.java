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
    if (id == null) {
      return false;
    }

    Connection conn = null;
    try {
      conn = ConnectionService.getConnection();
      conn.setAutoCommit(false);

      unlinkStudentsFromCurriculum(conn, id);
      deleteSemesterProgressByCurriculum(conn, id);
      deleteStudentEnrolledSubjectsByCurriculum(conn, id);
      deleteEnrollmentDetailsByCurriculum(conn, id);
      deleteSchedulesByCurriculum(conn, id);
      deleteOfferingsByCurriculum(conn, id);
      deleteSemesterSubjectsByCurriculum(conn, id);
      deleteSemestersByCurriculum(conn, id);

      boolean deleted = deleteCurriculumRow(conn, id);
      if (!deleted) {
        conn.rollback();
        return false;
      }

      conn.commit();
      return true;
    } catch (SQLException e) {
      rollbackQuietly(conn);
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    } finally {
      if (conn != null) {
        try {
          conn.setAutoCommit(true);
          conn.close();
        } catch (SQLException e) {
          logger.error("ERROR: " + e.getMessage(), e);
        }
      }
    }
  }

  private void unlinkStudentsFromCurriculum(Connection conn, Long curriculumId) throws SQLException {
    String sql = "UPDATE students SET curriculum_id = NULL, updated_at = CURRENT_TIMESTAMP WHERE curriculum_id = ?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, curriculumId);
      ps.executeUpdate();
    }
  }

  private void deleteSemesterProgressByCurriculum(Connection conn, Long curriculumId) throws SQLException {
    String sql = "DELETE FROM student_semester_progress WHERE curriculum_id = ? "
      + "OR semester_id IN (SELECT id FROM semester WHERE curriculum_id = ?)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, curriculumId);
      ps.setLong(2, curriculumId);
      ps.executeUpdate();
    }
  }

  private void deleteStudentEnrolledSubjectsByCurriculum(Connection conn, Long curriculumId) throws SQLException {
    String sql = "DELETE FROM student_enrolled_subjects WHERE semester_subject_id IN ("
      + "SELECT ss.id FROM semester_subjects ss "
      + "INNER JOIN semester sem ON sem.id = ss.semester_id "
      + "WHERE sem.curriculum_id = ?)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, curriculumId);
      ps.executeUpdate();
    }
  }

  private void deleteEnrollmentDetailsByCurriculum(Connection conn, Long curriculumId) throws SQLException {
    String sql = "DELETE FROM enrollments_details WHERE offering_id IN ("
      + "SELECT o.id FROM offerings o "
      + "INNER JOIN semester_subjects ss ON ss.id = o.semester_subject_id "
      + "INNER JOIN semester sem ON sem.id = ss.semester_id "
      + "WHERE sem.curriculum_id = ?)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, curriculumId);
      ps.executeUpdate();
    }
  }

  private void deleteSchedulesByCurriculum(Connection conn, Long curriculumId) throws SQLException {
    String sql = "DELETE FROM schedules WHERE offering_id IN ("
      + "SELECT o.id FROM offerings o "
      + "INNER JOIN semester_subjects ss ON ss.id = o.semester_subject_id "
      + "INNER JOIN semester sem ON sem.id = ss.semester_id "
      + "WHERE sem.curriculum_id = ?)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, curriculumId);
      ps.executeUpdate();
    }
  }

  private void deleteOfferingsByCurriculum(Connection conn, Long curriculumId) throws SQLException {
    String sql = "DELETE FROM offerings WHERE semester_subject_id IN ("
      + "SELECT ss.id FROM semester_subjects ss "
      + "INNER JOIN semester sem ON sem.id = ss.semester_id "
      + "WHERE sem.curriculum_id = ?)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, curriculumId);
      ps.executeUpdate();
    }
  }

  private void deleteSemesterSubjectsByCurriculum(Connection conn, Long curriculumId) throws SQLException {
    String sql = "DELETE FROM semester_subjects WHERE semester_id IN ("
      + "SELECT id FROM semester WHERE curriculum_id = ?)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, curriculumId);
      ps.executeUpdate();
    }
  }

  private void deleteSemestersByCurriculum(Connection conn, Long curriculumId) throws SQLException {
    String sql = "DELETE FROM semester WHERE curriculum_id = ?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, curriculumId);
      ps.executeUpdate();
    }
  }

  private boolean deleteCurriculumRow(Connection conn, Long curriculumId) throws SQLException {
    String sql = "DELETE FROM curriculum WHERE id = ?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, curriculumId);
      return ps.executeUpdate() > 0;
    }
  }

  private void rollbackQuietly(Connection conn) {
    if (conn == null) {
      return;
    }

    try {
      conn.rollback();
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
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
