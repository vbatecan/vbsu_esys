package com.group5.paul_esys.modules.enrollments.services;

import com.group5.paul_esys.modules.users.services.ConnectionService;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StudentEnrollmentEligibilityService {

  private static final StudentEnrollmentEligibilityService INSTANCE = new StudentEnrollmentEligibilityService();
  private static final Logger logger = LoggerFactory.getLogger(StudentEnrollmentEligibilityService.class);

  private StudentEnrollmentEligibilityService() {
  }

  public static StudentEnrollmentEligibilityService getInstance() {
    return INSTANCE;
  }

  public Set<Long> getEligibleSemesterSubjectIds(String studentId, String enrollmentSemesterLabel, Long enrollmentYearLevel) {
    if (studentId == null || studentId.isBlank()) {
      return Collections.emptySet();
    }

    try (Connection conn = ConnectionService.getConnection()) {
      Optional<Long> curriculumIdOpt = resolveCurriculumId(conn, studentId);
      if (curriculumIdOpt.isEmpty()) {
        return Collections.emptySet();
      }

      Map<Long, String> semesterLabelById = new HashMap<>();
      Map<Long, Integer> yearLevelBySemesterId = new HashMap<>();
      List<Long> orderedSemesterIds = getOrderedSemesterIdsByCurriculum(conn, curriculumIdOpt.get(), semesterLabelById, yearLevelBySemesterId);
      if (orderedSemesterIds.isEmpty()) {
        return Collections.emptySet();
      }

      Map<Long, Long> requiredCountBySemester = new HashMap<>();
      Map<Long, Long> completedCountBySemester = new HashMap<>();
      Map<Long, Long> activityCountBySemester = new HashMap<>();

      for (Long semesterId : orderedSemesterIds) {
        long requiredCount = countRequiredSubjects(conn, semesterId);
        long completedCount = countCompletedSubjects(conn, studentId, semesterId);
        long activityCount = countTrackedSubjectActivity(conn, studentId, semesterId)
            + countSelectedEnrollmentActivity(conn, studentId, semesterId);

        requiredCountBySemester.put(semesterId, requiredCount);
        completedCountBySemester.put(semesterId, completedCount);
        activityCountBySemester.put(semesterId, activityCount);
      }

      Integer currentSemesterIndex = findCurrentSemesterIndex(
          orderedSemesterIds,
          semesterLabelById,
          yearLevelBySemesterId,
          enrollmentSemesterLabel,
          enrollmentYearLevel
      );

      if (currentSemesterIndex == null) {
        currentSemesterIndex = findHighestActiveSemesterIndex(
            orderedSemesterIds,
            completedCountBySemester,
            activityCountBySemester
        );
      }

      if (currentSemesterIndex == null) {
        currentSemesterIndex = 0;
      }

      LinkedHashSet<Long> allowedSemesterIds = new LinkedHashSet<>();
      for (int index = 0; index <= currentSemesterIndex && index < orderedSemesterIds.size(); index++) {
        Long semesterId = orderedSemesterIds.get(index);
        long requiredCount = requiredCountBySemester.getOrDefault(semesterId, 0L);
        long completedCount = completedCountBySemester.getOrDefault(semesterId, 0L);

        boolean isCurrentSemester = index == currentSemesterIndex;
        boolean hasBacktrackLoad = completedCount < requiredCount;

        if (isCurrentSemester || hasBacktrackLoad) {
          allowedSemesterIds.add(semesterId);
        }
      }

      if (allowedSemesterIds.isEmpty()) {
        allowedSemesterIds.add(orderedSemesterIds.get(currentSemesterIndex));
      }

      return getSemesterSubjectIds(conn, allowedSemesterIds);
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return Collections.emptySet();
    }
  }

  private Optional<Long> resolveCurriculumId(Connection conn, String studentId) throws SQLException {
    String studentSql = "SELECT curriculum_id, course_id FROM students WHERE student_id = ?";

    try (PreparedStatement ps = conn.prepareStatement(studentSql)) {
      ps.setString(1, studentId);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          return Optional.empty();
        }

        long curriculumId = rs.getLong("curriculum_id");
        if (!rs.wasNull()) {
          return Optional.of(curriculumId);
        }

        long courseId = rs.getLong("course_id");
        if (rs.wasNull()) {
          return Optional.empty();
        }

        return getLatestCurriculumByCourse(conn, courseId);
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

  private List<Long> getOrderedSemesterIdsByCurriculum(
      Connection conn,
      Long curriculumId,
      Map<Long, String> semesterLabelById,
      Map<Long, Integer> yearLevelBySemesterId
  ) throws SQLException {
    boolean hasYearLevel = hasYearLevelColumn(conn);
    String sql = hasYearLevel
        ? "SELECT id, semester, year_level, created_at FROM semester WHERE curriculum_id = ?"
        : "SELECT id, semester, created_at FROM semester WHERE curriculum_id = ?";

    List<Long> semesterIds = new ArrayList<>();
    Map<Long, Timestamp> createdAtBySemesterId = new HashMap<>();

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, curriculumId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Long semesterId = rs.getLong("id");
          semesterIds.add(semesterId);

          semesterLabelById.put(semesterId, rs.getString("semester"));
          createdAtBySemesterId.put(semesterId, rs.getTimestamp("created_at"));

          Integer yearLevel = null;
          if (hasYearLevel) {
            int rawYearLevel = rs.getInt("year_level");
            if (!rs.wasNull()) {
              yearLevel = rawYearLevel;
            }
          }
          yearLevelBySemesterId.put(semesterId, yearLevel);
        }
      }
    }

    semesterIds.sort(Comparator
        .comparing((Long semesterId) -> yearLevelBySemesterId.get(semesterId),
            Comparator.nullsLast(Integer::compareTo))
        .thenComparing(semesterId -> resolveSemesterRank(semesterLabelById.get(semesterId)))
        .thenComparing(semesterId -> createdAtBySemesterId.get(semesterId),
            Comparator.nullsLast(Timestamp::compareTo))
        .thenComparingLong(Long::longValue));

    return semesterIds;
  }

  private boolean hasYearLevelColumn(Connection conn) {
    try {
      DatabaseMetaData metadata = conn.getMetaData();
      try (ResultSet rs = metadata.getColumns(null, null, "SEMESTER", "YEAR_LEVEL")) {
        if (rs.next()) {
          return true;
        }
      }

      try (ResultSet rs = metadata.getColumns(null, null, "semester", "year_level")) {
        return rs.next();
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  private Integer findCurrentSemesterIndex(
      List<Long> orderedSemesterIds,
      Map<Long, String> semesterLabelById,
      Map<Long, Integer> yearLevelBySemesterId,
      String enrollmentSemesterLabel,
      Long enrollmentYearLevel
  ) {
    if (orderedSemesterIds.isEmpty()) {
      return null;
    }

    if (enrollmentSemesterLabel == null || enrollmentSemesterLabel.isBlank()) {
      return null;
    }

    Integer normalizedYearLevel = enrollmentYearLevel == null ? null : enrollmentYearLevel.intValue();

    if (normalizedYearLevel != null) {
      for (int index = 0; index < orderedSemesterIds.size(); index++) {
        Long semesterId = orderedSemesterIds.get(index);
        Integer candidateYearLevel = yearLevelBySemesterId.get(semesterId);
        if (candidateYearLevel == null || !candidateYearLevel.equals(normalizedYearLevel)) {
          continue;
        }

        if (semesterLabelsMatch(semesterLabelById.get(semesterId), enrollmentSemesterLabel)) {
          return index;
        }
      }
    }

    for (int index = 0; index < orderedSemesterIds.size(); index++) {
      Long semesterId = orderedSemesterIds.get(index);
      if (semesterLabelsMatch(semesterLabelById.get(semesterId), enrollmentSemesterLabel)) {
        return index;
      }
    }

    return null;
  }

  private Integer findHighestActiveSemesterIndex(
      List<Long> orderedSemesterIds,
      Map<Long, Long> completedCountBySemester,
      Map<Long, Long> activityCountBySemester
  ) {
    for (int index = orderedSemesterIds.size() - 1; index >= 0; index--) {
      Long semesterId = orderedSemesterIds.get(index);
      long completedCount = completedCountBySemester.getOrDefault(semesterId, 0L);
      long activityCount = activityCountBySemester.getOrDefault(semesterId, 0L);
      if (completedCount > 0 || activityCount > 0) {
        return index;
      }
    }

    return null;
  }

  private int resolveSemesterRank(String semesterLabel) {
    if (semesterLabel == null || semesterLabel.isBlank()) {
      return Integer.MAX_VALUE;
    }

    String normalized = semesterLabel.trim().toUpperCase(Locale.ROOT);
    if (normalized.contains("1ST") || normalized.contains("FIRST") || normalized.matches(".*\\b1\\b.*")) {
      return 1;
    }

    if (normalized.contains("2ND") || normalized.contains("SECOND") || normalized.matches(".*\\b2\\b.*")) {
      return 2;
    }

    if (normalized.contains("3RD") || normalized.contains("THIRD") || normalized.matches(".*\\b3\\b.*")) {
      return 3;
    }

    if (normalized.contains("SUMMER")) {
      return 9;
    }

    return 99;
  }

  private boolean semesterLabelsMatch(String left, String right) {
    if (left == null || right == null) {
      return false;
    }

    String normalizedLeft = normalizeSemesterToken(left);
    String normalizedRight = normalizeSemesterToken(right);
    if (!normalizedLeft.isEmpty() && normalizedLeft.equals(normalizedRight)) {
      return true;
    }

    int leftRank = resolveSemesterRank(left);
    int rightRank = resolveSemesterRank(right);
    return leftRank < 99 && leftRank == rightRank;
  }

  private String normalizeSemesterToken(String value) {
    return value == null
        ? ""
        : value.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
  }

  private Set<Long> getSemesterSubjectIds(Connection conn, Set<Long> semesterIds) throws SQLException {
    if (semesterIds == null || semesterIds.isEmpty()) {
      return Collections.emptySet();
    }

    String placeholders = String.join(",", Collections.nCopies(semesterIds.size(), "?"));
    String sql = "SELECT id FROM semester_subjects WHERE semester_id IN (" + placeholders + ") ORDER BY semester_id, id";

    LinkedHashSet<Long> semesterSubjectIds = new LinkedHashSet<>();
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      int parameterIndex = 1;
      for (Long semesterId : semesterIds) {
        ps.setLong(parameterIndex++, semesterId);
      }

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          semesterSubjectIds.add(rs.getLong("id"));
        }
      }
    }

    return semesterSubjectIds;
  }

  private long countRequiredSubjects(Connection conn, Long semesterId) throws SQLException {
    String sql = "SELECT COUNT(*) AS total FROM semester_subjects WHERE semester_id = ?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, semesterId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getLong("total");
        }
      }
    }

    return 0;
  }

  private long countCompletedSubjects(Connection conn, String studentId, Long semesterId) throws SQLException {
    String sql =
        "SELECT COUNT(DISTINCT ses.semester_subject_id) AS total "
            + "FROM student_enrolled_subjects ses "
            + "JOIN semester_subjects ss ON ss.id = ses.semester_subject_id "
            + "WHERE ses.student_id = ? AND ss.semester_id = ? AND ses.status = 'COMPLETED'";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, studentId);
      ps.setLong(2, semesterId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getLong("total");
        }
      }
    }

    return 0;
  }

  private long countTrackedSubjectActivity(Connection conn, String studentId, Long semesterId) throws SQLException {
    String sql =
        "SELECT COUNT(*) AS total "
            + "FROM student_enrolled_subjects ses "
            + "JOIN semester_subjects ss ON ss.id = ses.semester_subject_id "
            + "WHERE ses.student_id = ? AND ss.semester_id = ?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, studentId);
      ps.setLong(2, semesterId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getLong("total");
        }
      }
    }

    return 0;
  }

  private long countSelectedEnrollmentActivity(Connection conn, String studentId, Long semesterId) throws SQLException {
    String sql =
        "SELECT COUNT(DISTINCT o.semester_subject_id) AS total "
            + "FROM enrollments_details ed "
            + "JOIN enrollments e ON e.id = ed.enrollment_id "
            + "JOIN offerings o ON o.id = ed.offering_id "
            + "JOIN semester_subjects ss ON ss.id = o.semester_subject_id "
            + "WHERE e.student_id = ? AND ss.semester_id = ? AND ed.status = 'SELECTED'";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, studentId);
      ps.setLong(2, semesterId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getLong("total");
        }
      }
    }

    return 0;
  }
}
