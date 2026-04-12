package com.group5.paul_esys.modules.subjects.services;

import com.group5.paul_esys.modules.subjects.model.Subject;
import com.group5.paul_esys.modules.subjects.model.SubjectSchedulePattern;
import com.group5.paul_esys.modules.subjects.utils.SubjectUtils;
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

public class SubjectService {

  private static final SubjectService INSTANCE = new SubjectService();
  private static final Logger logger = LoggerFactory.getLogger(SubjectService.class);

  private SubjectService() {
  }

  public static SubjectService getInstance() {
    return INSTANCE;
  }

  public List<Subject> getAllSubjects() {
    List<Subject> subjects = new ArrayList<>();
    try (Connection conn = ConnectionService.getConnection()) {
      ensureEstimatedTimeDefaults(conn);
      ensureSchedulePatternDefaults(conn);
      try (
          PreparedStatement ps = conn.prepareStatement("SELECT * FROM subjects ORDER BY subject_code");
          ResultSet rs = ps.executeQuery()
      ) {
        while (rs.next()) {
          subjects.add(SubjectUtils.mapResultSetToSubject(rs));
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return subjects;
  }

  public List<SubjectWithDepartment> getAllSubjectsWithDepartments() {
    List<SubjectWithDepartment> results = new ArrayList<>();
    try (Connection conn = ConnectionService.getConnection()) {
      ensureEstimatedTimeDefaults(conn);
      ensureSchedulePatternDefaults(conn);

      String estimatedTimeExpression = hasSubjectEstimatedTimeColumn(conn)
        ? "COALESCE(s.estimated_time, 90)"
        : "90";

      String schedulePatternExpression = hasSubjectSchedulePatternColumn(conn)
        ? "COALESCE(s.schedule_pattern, 'LECTURE_ONLY')"
        : "'LECTURE_ONLY'";

      String sql = "SELECT s.id, s.subject_name, s.subject_code, s.units, "
        + estimatedTimeExpression + " AS estimated_time, "
        + schedulePatternExpression + " AS schedule_pattern, "
        + "s.description, s.department_id, "
        + "s.created_at, s.updated_at, "
        + "d.id as dept_id, d.department_name, d.department_code "
        + "FROM subjects s "
        + "LEFT JOIN departments d ON s.department_id = d.id "
        + "ORDER BY s.subject_code";

      try (
        PreparedStatement ps = conn.prepareStatement(sql);
          ResultSet rs = ps.executeQuery()
      ) {
        while (rs.next()) {
          Subject subject = new Subject(
              rs.getLong("id"),
              rs.getString("subject_name"),
              rs.getString("subject_code"),
              rs.getFloat("units"),
              normalizeEstimatedTime(rs.getObject("estimated_time", Integer.class)),
          SubjectSchedulePattern.fromValue(rs.getString("schedule_pattern")),
              rs.getString("description"),
              rs.getLong("department_id"),
              rs.getTimestamp("updated_at"),
              rs.getTimestamp("created_at")
          );
          String departmentName = rs.getString("department_name");
          results.add(new SubjectWithDepartment(subject, departmentName));
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return results;
  }

  public static class SubjectWithDepartment {
    private final Subject subject;
    private final String departmentName;

    public SubjectWithDepartment(Subject subject, String departmentName) {
      this.subject = subject;
      this.departmentName = departmentName;
    }

    public Subject getSubject() {
      return subject;
    }

    public String getDepartmentName() {
      return departmentName;
    }
  }

  public Optional<Subject> getSubjectById(Long id) {
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM subjects WHERE id = ?")) {
      ps.setLong(1, id);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(SubjectUtils.mapResultSetToSubject(rs));
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return Optional.empty();
  }

  public Optional<Subject> getSubjectByCode(String subjectCode) {
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM subjects WHERE subject_code = ?")) {
      ps.setString(1, subjectCode);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(SubjectUtils.mapResultSetToSubject(rs));
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return Optional.empty();
  }

  public List<Subject> getSubjectsByDepartment(Long departmentId) {
    List<Subject> subjects = new ArrayList<>();
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM subjects WHERE department_id = ? ORDER BY subject_code")) {
      ps.setLong(1, departmentId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          subjects.add(SubjectUtils.mapResultSetToSubject(rs));
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return subjects;
  }

  public List<Subject> getSubjectsByCurriculum(Long curriculumId) {
    // subjects table no longer stores curriculum_id; keep this method for compatibility.
    return getAllSubjects();
  }

  public boolean createSubject(Subject subject) {
    try (Connection conn = ConnectionService.getConnection()) {
      boolean hasSchedulePattern = hasSubjectSchedulePatternColumn(conn);
      String sql = hasSchedulePattern
          ? "INSERT INTO subjects (subject_name, subject_code, units, estimated_time, schedule_pattern, description, department_id) VALUES (?, ?, ?, ?, ?, ?, ?)"
          : "INSERT INTO subjects (subject_name, subject_code, units, estimated_time, description, department_id) VALUES (?, ?, ?, ?, ?, ?)";

      try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, subject.getSubjectName());
      ps.setString(2, subject.getSubjectCode());
      ps.setFloat(3, subject.getUnits());
      ps.setInt(4, normalizeEstimatedTime(subject.getEstimatedTime()));
      if (hasSchedulePattern) {
        ps.setString(5, normalizeSchedulePattern(subject.getSchedulePattern()));
        ps.setString(6, subject.getDescription());
        ps.setLong(7, subject.getDepartmentId());
      } else {
        ps.setString(5, subject.getDescription());
        ps.setLong(6, subject.getDepartmentId());
      }
      
      return ps.executeUpdate() > 0;
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public boolean updateSubject(Subject subject) {
    try (Connection conn = ConnectionService.getConnection()) {
      boolean hasSchedulePattern = hasSubjectSchedulePatternColumn(conn);
      String sql = hasSchedulePattern
          ? "UPDATE subjects SET subject_name = ?, subject_code = ?, units = ?, estimated_time = ?, schedule_pattern = ?, description = ?, department_id = ? WHERE id = ?"
          : "UPDATE subjects SET subject_name = ?, subject_code = ?, units = ?, estimated_time = ?, description = ?, department_id = ? WHERE id = ?";

      try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, subject.getSubjectName());
      ps.setString(2, subject.getSubjectCode());
      ps.setFloat(3, subject.getUnits());
      ps.setInt(4, normalizeEstimatedTime(subject.getEstimatedTime()));
      if (hasSchedulePattern) {
        ps.setString(5, normalizeSchedulePattern(subject.getSchedulePattern()));
        ps.setString(6, subject.getDescription());
        ps.setLong(7, subject.getDepartmentId());
        ps.setLong(8, subject.getId());
      } else {
        ps.setString(5, subject.getDescription());
        ps.setLong(6, subject.getDepartmentId());
        ps.setLong(7, subject.getId());
      }
      
      return ps.executeUpdate() > 0;
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public boolean deleteSubject(Long id) {
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("DELETE FROM subjects WHERE id = ?")) {
      ps.setLong(1, id);
      
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  private int normalizeEstimatedTime(Integer estimatedTime) {
    if (estimatedTime == null || estimatedTime <= 0) {
      return 90;
    }

    return estimatedTime;
  }

  private String normalizeSchedulePattern(SubjectSchedulePattern schedulePattern) {
    if (schedulePattern == null) {
      return SubjectSchedulePattern.LECTURE_ONLY.name();
    }

    return schedulePattern.name();
  }

  private void ensureEstimatedTimeDefaults(Connection conn) {
    String sql = "UPDATE subjects SET estimated_time = 90 WHERE estimated_time IS NULL OR estimated_time <= 0";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.executeUpdate();
    } catch (SQLException e) {
      if (isMissingEstimatedTimeColumn(e)) {
        return;
      }

      logger.warn("Unable to backfill subject estimated_time defaults: {}", e.getMessage());
    }
  }

  private void ensureSchedulePatternDefaults(Connection conn) {
    String sql = "UPDATE subjects SET schedule_pattern = 'LECTURE_ONLY' WHERE schedule_pattern IS NULL OR TRIM(schedule_pattern) = ''";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.executeUpdate();
    } catch (SQLException e) {
      if (isMissingSchedulePatternColumn(e)) {
        return;
      }

      logger.warn("Unable to backfill subject schedule_pattern defaults: {}", e.getMessage());
    }
  }

  private boolean isMissingEstimatedTimeColumn(SQLException e) {
    String sqlState = e.getSQLState();
    return "42X04".equals(sqlState) || "42S22".equals(sqlState);
  }

  private boolean isMissingSchedulePatternColumn(SQLException e) {
    String sqlState = e.getSQLState();
    return "42X04".equals(sqlState) || "42S22".equals(sqlState);
  }

  private boolean hasSubjectEstimatedTimeColumn(Connection conn) {
    try {
      try (ResultSet rs = conn.getMetaData().getColumns(null, null, "SUBJECTS", "ESTIMATED_TIME")) {
        if (rs.next()) {
          return true;
        }
      }

      try (ResultSet rs = conn.getMetaData().getColumns(null, null, "subjects", "estimated_time")) {
        return rs.next();
      }
    } catch (SQLException e) {
      logger.warn("Unable to resolve subjects.estimated_time metadata: {}", e.getMessage());
      return false;
    }
  }

  private boolean hasSubjectSchedulePatternColumn(Connection conn) {
    try {
      try (ResultSet rs = conn.getMetaData().getColumns(null, null, "SUBJECTS", "SCHEDULE_PATTERN")) {
        if (rs.next()) {
          return true;
        }
      }

      try (ResultSet rs = conn.getMetaData().getColumns(null, null, "subjects", "schedule_pattern")) {
        return rs.next();
      }
    } catch (SQLException e) {
      logger.warn("Unable to resolve subjects.schedule_pattern metadata: {}", e.getMessage());
      return false;
    }
  }
}
