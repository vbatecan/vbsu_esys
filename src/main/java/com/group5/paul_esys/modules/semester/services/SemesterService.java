package com.group5.paul_esys.modules.semester.services;

import com.group5.paul_esys.modules.semester.model.Semester;
import com.group5.paul_esys.modules.semester.utils.SemesterUtils;
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

public class SemesterService {

  private static final SemesterService INSTANCE = new SemesterService();
  private static final Logger logger = LoggerFactory.getLogger(SemesterService.class);

  private SemesterService() {
  }

  public static SemesterService getInstance() {
    return INSTANCE;
  }

  private boolean hasYearLevelColumn(Connection conn) {
    try {
      DatabaseMetaData metadata = conn.getMetaData();
      try (
        ResultSet rs = metadata.getColumns(null, null, "SEMESTER", "YEAR_LEVEL")
      ) {
        if (rs.next()) {
          return true;
        }
      }

      try (
        ResultSet rs = metadata.getColumns(null, null, "semester", "year_level")
      ) {
        return rs.next();
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public List<Semester> getAllSemesters() {
    List<Semester> semesters = new ArrayList<>();
    try (
      Connection conn = ConnectionService.getConnection();
      PreparedStatement ps = conn.prepareStatement("SELECT * FROM semester ORDER BY created_at DESC");
      ResultSet rs = ps.executeQuery()
    ) {
      while (rs.next()) {
        semesters.add(SemesterUtils.mapResultSetToSemester(rs));
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return semesters;
  }

  public Optional<Semester> getSemesterById(Long id) {
    try (
      Connection conn = ConnectionService.getConnection();
      PreparedStatement ps = conn.prepareStatement("SELECT * FROM semester WHERE id = ?")
    ) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(SemesterUtils.mapResultSetToSemester(rs));
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }

    return Optional.empty();
  }

  public List<Semester> getSemestersByCurriculum(Long curriculumId) {
    List<Semester> semesters = new ArrayList<>();
    try (
      Connection conn = ConnectionService.getConnection();
      PreparedStatement ps = conn.prepareStatement("SELECT * FROM semester WHERE curriculum_id = ? ORDER BY created_at DESC")
    ) {
      ps.setLong(1, curriculumId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          semesters.add(SemesterUtils.mapResultSetToSemester(rs));
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }

    return semesters;
  }

  public boolean semesterExists(Semester semester) {
    if (semester == null || semester.getCurriculumId() == null || semester.getSemester() == null) {
      return false;
    }

    try (Connection conn = ConnectionService.getConnection()) {
      boolean hasYearLevel = hasYearLevelColumn(conn);
      String sql;
      if (hasYearLevel) {
        sql = semester.getId() == null
          ? "SELECT 1 FROM semester WHERE curriculum_id = ? AND UPPER(TRIM(semester)) = UPPER(TRIM(?)) AND year_level = ? FETCH FIRST 1 ROWS ONLY"
          : "SELECT 1 FROM semester WHERE curriculum_id = ? AND UPPER(TRIM(semester)) = UPPER(TRIM(?)) AND year_level = ? AND id <> ? FETCH FIRST 1 ROWS ONLY";
      } else {
        sql = semester.getId() == null
          ? "SELECT 1 FROM semester WHERE curriculum_id = ? AND UPPER(TRIM(semester)) = UPPER(TRIM(?)) FETCH FIRST 1 ROWS ONLY"
          : "SELECT 1 FROM semester WHERE curriculum_id = ? AND UPPER(TRIM(semester)) = UPPER(TRIM(?)) AND id <> ? FETCH FIRST 1 ROWS ONLY";
      }

      try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setLong(1, semester.getCurriculumId());
        ps.setString(2, semester.getSemester().trim());

        if (hasYearLevel) {
          ps.setInt(3, semester.getYearLevel() == null ? 1 : semester.getYearLevel());
          if (semester.getId() != null) {
            ps.setLong(4, semester.getId());
          }
        } else if (semester.getId() != null) {
          ps.setLong(3, semester.getId());
        }

        try (ResultSet rs = ps.executeQuery()) {
          return rs.next();
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public boolean createSemester(Semester semester) {
    try (Connection conn = ConnectionService.getConnection()) {
      boolean hasYearLevel = hasYearLevelColumn(conn);
      String sql;
      if (hasYearLevel) {
        sql = "INSERT INTO semester (curriculum_id, semester, year_level) VALUES (?, ?, ?)";
      } else {
        sql = "INSERT INTO semester (curriculum_id, semester) VALUES (?, ?)";
      }

      try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setLong(1, semester.getCurriculumId());
        ps.setString(2, semester.getSemester());

        if (hasYearLevel) {
          ps.setInt(3, semester.getYearLevel() == null ? 1 : semester.getYearLevel());
        }

        return ps.executeUpdate() > 0;
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public boolean updateSemester(Semester semester) {
    try (Connection conn = ConnectionService.getConnection()) {
      boolean hasYearLevel = hasYearLevelColumn(conn);
      String sql;
      if (hasYearLevel) {
        sql = "UPDATE semester SET curriculum_id = ?, semester = ?, year_level = ? WHERE id = ?";
      } else {
        sql = "UPDATE semester SET curriculum_id = ?, semester = ? WHERE id = ?";
      }

      try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, semester.getCurriculumId());
      ps.setString(2, semester.getSemester());

      if (hasYearLevel) {
        ps.setInt(3, semester.getYearLevel() == null ? 1 : semester.getYearLevel());
        ps.setLong(4, semester.getId());
      } else {
        ps.setLong(3, semester.getId());
      }

      return ps.executeUpdate() > 0;
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public boolean deleteSemester(Long id) {
    if (id == null) {
      return false;
    }

    Connection conn = null;
    try {
      conn = ConnectionService.getConnection();
      conn.setAutoCommit(false);

      deleteStudentEnrolledSubjectsBySemester(conn, id);
      deleteEnrollmentDetailsBySemester(conn, id);
      deleteSchedulesBySemester(conn, id);
      deleteOfferingsBySemester(conn, id);
      deleteSemesterProgressBySemester(conn, id);
      deleteSemesterSubjectsBySemester(conn, id);

      boolean deleted = deleteSemesterRow(conn, id);
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

  private void deleteStudentEnrolledSubjectsBySemester(Connection conn, Long semesterId) throws SQLException {
    String sql = "DELETE FROM student_enrolled_subjects WHERE semester_subject_id IN ("
      + "SELECT id FROM semester_subjects WHERE semester_id = ?)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, semesterId);
      ps.executeUpdate();
    }
  }

  private void deleteEnrollmentDetailsBySemester(Connection conn, Long semesterId) throws SQLException {
    String sql = "DELETE FROM enrollments_details WHERE offering_id IN ("
      + "SELECT o.id FROM offerings o "
      + "INNER JOIN semester_subjects ss ON ss.id = o.semester_subject_id "
      + "WHERE ss.semester_id = ?)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, semesterId);
      ps.executeUpdate();
    }
  }

  private void deleteSchedulesBySemester(Connection conn, Long semesterId) throws SQLException {
    String sql = "DELETE FROM schedules WHERE offering_id IN ("
      + "SELECT o.id FROM offerings o "
      + "INNER JOIN semester_subjects ss ON ss.id = o.semester_subject_id "
      + "WHERE ss.semester_id = ?)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, semesterId);
      ps.executeUpdate();
    }
  }

  private void deleteOfferingsBySemester(Connection conn, Long semesterId) throws SQLException {
    String sql = "DELETE FROM offerings WHERE semester_subject_id IN ("
      + "SELECT id FROM semester_subjects WHERE semester_id = ?)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, semesterId);
      ps.executeUpdate();
    }
  }

  private void deleteSemesterProgressBySemester(Connection conn, Long semesterId) throws SQLException {
    String sql = "DELETE FROM student_semester_progress WHERE semester_id = ?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, semesterId);
      ps.executeUpdate();
    }
  }

  private void deleteSemesterSubjectsBySemester(Connection conn, Long semesterId) throws SQLException {
    String sql = "DELETE FROM semester_subjects WHERE semester_id = ?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, semesterId);
      ps.executeUpdate();
    }
  }

  private boolean deleteSemesterRow(Connection conn, Long semesterId) throws SQLException {
    String sql = "DELETE FROM semester WHERE id = ?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, semesterId);
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
}
