package com.group5.paul_esys.modules.enrollments.services;

import com.group5.paul_esys.modules.enums.SemesterProgressStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class StudentAcademicPromotionService {

  private static final StudentAcademicPromotionService INSTANCE = new StudentAcademicPromotionService();

  private StudentAcademicPromotionService() {
  }

  public static StudentAcademicPromotionService getInstance() {
    return INSTANCE;
  }

  public boolean promoteIfYearCompleted(Connection conn, String studentId) throws SQLException {
    if (conn == null || studentId == null || studentId.isBlank()) {
      return false;
    }

    Optional<PromotionContext> promotionContext = resolvePromotionContext(conn, studentId);
    if (promotionContext.isEmpty()) {
      return false;
    }

    Long curriculumId = promotionContext.get().curriculumId();
    long currentYearLevel = promotionContext.get().currentYearLevel();
    long nextYearLevel = currentYearLevel + 1;

    if (!hasSemestersForYear(conn, curriculumId, currentYearLevel)) {
      return false;
    }

    if (!hasSemestersForYear(conn, curriculumId, nextYearLevel)) {
      return false;
    }

    long incompleteSemesters = countIncompleteSemestersForYear(conn, studentId, curriculumId, currentYearLevel);
    if (incompleteSemesters > 0) {
      return false;
    }

    return incrementStudentYearLevel(conn, studentId, currentYearLevel, nextYearLevel);
  }

  private Optional<PromotionContext> resolvePromotionContext(Connection conn, String studentId) throws SQLException {
    String sql = "SELECT curriculum_id, course_id, year_level FROM students WHERE student_id = ?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, studentId);

      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          return Optional.empty();
        }

        long currentYearLevel = rs.getLong("year_level");
        if (rs.wasNull()) {
          return Optional.empty();
        }

        Long curriculumId = rs.getLong("curriculum_id");
        if (rs.wasNull()) {
          long courseId = rs.getLong("course_id");
          if (rs.wasNull()) {
            return Optional.empty();
          }

          Optional<Long> latestCurriculum = getLatestCurriculumByCourse(conn, courseId);
          if (latestCurriculum.isEmpty()) {
            return Optional.empty();
          }

          curriculumId = latestCurriculum.get();
        }

        return Optional.of(new PromotionContext(curriculumId, currentYearLevel));
      }
    }
  }

  private Optional<Long> getLatestCurriculumByCourse(Connection conn, Long courseId) throws SQLException {
    String sql = "SELECT id FROM curriculum WHERE course = ? ORDER BY cur_year DESC, created_at DESC FETCH FIRST 1 ROWS ONLY";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, courseId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(rs.getLong("id"));
        }
      }
    }

    return Optional.empty();
  }

  private boolean hasSemestersForYear(Connection conn, Long curriculumId, long yearLevel) throws SQLException {
    String sql = "SELECT COUNT(*) AS total FROM semester WHERE curriculum_id = ? AND year_level = ?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, curriculumId);
      ps.setLong(2, yearLevel);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getLong("total") > 0;
        }
      }
    }

    return false;
  }

  private long countIncompleteSemestersForYear(Connection conn, String studentId, Long curriculumId, long yearLevel) throws SQLException {
    String sql = """
      SELECT COUNT(*) AS total
      FROM semester s
      LEFT JOIN student_semester_progress sp
        ON sp.semester_id = s.id
       AND sp.student_id = ?
      WHERE s.curriculum_id = ?
        AND s.year_level = ?
        AND (sp.status IS NULL OR sp.status <> ?)
      """;

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, studentId);
      ps.setLong(2, curriculumId);
      ps.setLong(3, yearLevel);
      ps.setString(4, SemesterProgressStatus.COMPLETED.name());

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getLong("total");
        }
      }
    }

    return 0;
  }

  private boolean incrementStudentYearLevel(Connection conn, String studentId, long currentYearLevel, long nextYearLevel)
      throws SQLException {
    String sql = "UPDATE students SET year_level = ?, updated_at = CURRENT_TIMESTAMP WHERE student_id = ? AND year_level = ?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, nextYearLevel);
      ps.setString(2, studentId);
      ps.setLong(3, currentYearLevel);
      return ps.executeUpdate() > 0;
    }
  }

  private record PromotionContext(
      Long curriculumId,
      long currentYearLevel
  ) {
  }
}
