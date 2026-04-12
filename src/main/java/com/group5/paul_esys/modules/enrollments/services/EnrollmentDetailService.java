package com.group5.paul_esys.modules.enrollments.services;

import com.group5.paul_esys.modules.enums.EnrollmentDetailStatus;
import com.group5.paul_esys.modules.enrollments.model.EnrollmentDetail;
import com.group5.paul_esys.modules.enrollments.utils.EnrollmentDetailUtils;
import com.group5.paul_esys.modules.users.services.ConnectionService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnrollmentDetailService {

  private static final EnrollmentDetailService INSTANCE = new EnrollmentDetailService();
  private static final Logger logger = LoggerFactory.getLogger(EnrollmentDetailService.class);

  private EnrollmentDetailService() {
  }

  public static EnrollmentDetailService getInstance() {
    return INSTANCE;
  }

  public List<EnrollmentDetail> getAllEnrollmentDetails() {
    List<EnrollmentDetail> details = new ArrayList<>();
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM enrollments_details ORDER BY created_at DESC");
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        details.add(EnrollmentDetailUtils.mapResultSetToEnrollmentDetail(rs));
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return details;
  }

  public Optional<EnrollmentDetail> getEnrollmentDetailById(Long id) {
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM enrollments_details WHERE id = ?")) {
      ps.setLong(1, id);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(EnrollmentDetailUtils.mapResultSetToEnrollmentDetail(rs));
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return Optional.empty();
  }

  public List<EnrollmentDetail> getEnrollmentDetailsByEnrollment(Long enrollmentId) {
    List<EnrollmentDetail> details = new ArrayList<>();
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM enrollments_details WHERE enrollment_id = ? ORDER BY created_at")) {
      ps.setLong(1, enrollmentId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          details.add(EnrollmentDetailUtils.mapResultSetToEnrollmentDetail(rs));
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return details;
  }

  public List<EnrollmentDetail> getEnrollmentDetailsBySection(Long sectionId) {
    List<EnrollmentDetail> details = new ArrayList<>();
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement(
            "SELECT ed.* FROM enrollments_details ed "
                + "INNER JOIN offerings o ON o.id = ed.offering_id "
                + "WHERE o.section_id = ? ORDER BY ed.created_at"
        )) {
      ps.setLong(1, sectionId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          details.add(EnrollmentDetailUtils.mapResultSetToEnrollmentDetail(rs));
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return details;
  }

  public List<EnrollmentDetail> getEnrollmentDetailsByOffering(Long offeringId) {
    List<EnrollmentDetail> details = new ArrayList<>();
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM enrollments_details WHERE offering_id = ? ORDER BY created_at")) {
      ps.setLong(1, offeringId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          details.add(EnrollmentDetailUtils.mapResultSetToEnrollmentDetail(rs));
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return details;
  }

  public long countSelectedByOffering(Long offeringId) {
    String sql = "SELECT COUNT(*) AS total FROM enrollments_details WHERE offering_id = ? AND status = ?";

    try (
      Connection conn = ConnectionService.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql)
    ) {
      ps.setLong(1, offeringId);
      ps.setString(2, EnrollmentDetailStatus.SELECTED.name());

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getLong("total");
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }

    return 0;
  }

  public boolean createEnrollmentDetail(EnrollmentDetail detail) {
    if (!isOfferingEligibleForEnrollment(detail.getEnrollmentId(), detail.getOfferingId())) {
      logger.warn(
          "Rejected enrollment detail create: enrollmentId={}, offeringId={} is not eligible for current rules",
          detail.getEnrollmentId(),
          detail.getOfferingId()
      );
      return false;
    }

    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO enrollments_details (enrollment_id, offering_id, units, status) VALUES (?, ?, ?, ?)"
        )) {
      ps.setLong(1, detail.getEnrollmentId());
      ps.setLong(2, detail.getOfferingId());
      ps.setFloat(3, detail.getUnits());
      ps.setString(4, detail.getStatus().name());

      boolean created = ps.executeUpdate() > 0;
      if (created) {
        StudentSemesterProgressService.getInstance().syncByEnrollmentId(detail.getEnrollmentId());
      }
      return created;
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public boolean updateEnrollmentDetail(EnrollmentDetail detail) {
    if (!isOfferingEligibleForEnrollment(detail.getEnrollmentId(), detail.getOfferingId())) {
      logger.warn(
          "Rejected enrollment detail update: enrollmentId={}, offeringId={} is not eligible for current rules",
          detail.getEnrollmentId(),
          detail.getOfferingId()
      );
      return false;
    }

    try (Connection conn = ConnectionService.getConnection();
      PreparedStatement ps = conn.prepareStatement(
        "UPDATE enrollments_details SET enrollment_id = ?, offering_id = ?, units = ?, status = ? WHERE id = ?"
      )) {
      ps.setLong(1, detail.getEnrollmentId());
      ps.setLong(2, detail.getOfferingId());
      ps.setFloat(3, detail.getUnits());
      ps.setString(4, detail.getStatus().name());
      ps.setLong(5, detail.getId());

      boolean updated = ps.executeUpdate() > 0;
      if (updated) {
        StudentSemesterProgressService.getInstance().syncByEnrollmentId(detail.getEnrollmentId());
      }
      return updated;
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public boolean updateEnrollmentDetailStatus(Long id, EnrollmentDetailStatus status) {
    Optional<Long> enrollmentId = getEnrollmentIdByDetailId(id);

    try (Connection conn = ConnectionService.getConnection();
      PreparedStatement ps = conn.prepareStatement(
        "UPDATE enrollments_details SET status = ? WHERE id = ?"
      )) {
      ps.setString(1, status.name());
      ps.setLong(2, id);

      boolean updated = ps.executeUpdate() > 0;
      if (updated && enrollmentId.isPresent()) {
        StudentSemesterProgressService.getInstance().syncByEnrollmentId(enrollmentId.get());
      }
      return updated;
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public boolean deleteEnrollmentDetail(Long id) {
    Optional<Long> enrollmentId = getEnrollmentIdByDetailId(id);

    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("DELETE FROM enrollments_details WHERE id = ?")) {
      ps.setLong(1, id);

      boolean deleted = ps.executeUpdate() > 0;
      if (deleted && enrollmentId.isPresent()) {
        StudentSemesterProgressService.getInstance().syncByEnrollmentId(enrollmentId.get());
      }
      return deleted;
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  private Optional<Long> getEnrollmentIdByDetailId(Long detailId) {
    String sql = "SELECT enrollment_id FROM enrollments_details WHERE id = ?";

    try (
      Connection conn = ConnectionService.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql)
    ) {
      ps.setLong(1, detailId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(rs.getLong("enrollment_id"));
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }

    return Optional.empty();
  }

  private boolean isOfferingEligibleForEnrollment(Long enrollmentId, Long offeringId) {
    if (enrollmentId == null || offeringId == null) {
      return false;
    }

    String sql =
        "SELECT e.student_id, ep.semester AS enrollment_semester, st.year_level, o.semester_subject_id "
            + "FROM enrollments e "
            + "JOIN enrollment_period ep ON ep.id = e.enrollment_period_id "
            + "LEFT JOIN students st ON st.student_id = e.student_id "
            + "JOIN offerings o ON o.id = ? "
            + "WHERE e.id = ?";

    try (
        Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)
    ) {
      ps.setLong(1, offeringId);
      ps.setLong(2, enrollmentId);

      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          return false;
        }

        String studentId = rs.getString("student_id");
        String enrollmentSemester = rs.getString("enrollment_semester");

        long rawYearLevel = rs.getLong("year_level");
        Long enrollmentYearLevel = rs.wasNull() ? null : rawYearLevel;

        long rawSemesterSubjectId = rs.getLong("semester_subject_id");
        if (rs.wasNull() || studentId == null || studentId.isBlank()) {
          return false;
        }

        Long semesterSubjectId = rawSemesterSubjectId;
        Set<Long> eligibleSemesterSubjectIds = StudentEnrollmentEligibilityService.getInstance()
            .getEligibleSemesterSubjectIds(studentId, enrollmentSemester, enrollmentYearLevel);

        return eligibleSemesterSubjectIds.contains(semesterSubjectId);
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }
}
