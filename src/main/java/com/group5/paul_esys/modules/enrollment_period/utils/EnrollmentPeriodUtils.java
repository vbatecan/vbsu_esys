package com.group5.paul_esys.modules.enrollment_period.utils;

import com.group5.paul_esys.modules.enrollment_period.model.EnrollmentPeriod;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class EnrollmentPeriodUtils {

  private static final String STATUS_OPEN = "OPEN";
  private static final String STATUS_CLOSED = "CLOSED";
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  public static EnrollmentPeriod mapResultSetToEnrollmentPeriod(ResultSet rs) throws SQLException {
    String description = null;

    try {
      description = rs.getString("description");
    } catch (SQLException ignored) {
      description = null;
    }

    return new EnrollmentPeriod(
        rs.getLong("id"),
        rs.getString("school_year"),
        rs.getString("semester"),
        rs.getTimestamp("start_date"),
        rs.getTimestamp("end_date"),
        normalizeDescription(description),
        rs.getTimestamp("updated_at"),
        rs.getTimestamp("created_at")
    );
  }

  public static Timestamp toSqlTimestamp(Date date) {
    if (date == null) {
      return null;
    }

    return new Timestamp(date.getTime());
  }

  public static String normalizeDescription(String description) {
    if (description == null) {
      return null;
    }

    String normalized = description.trim();
    return normalized.isEmpty() ? null : normalized;
  }

  public static String safeText(String value, String fallback) {
    if (value == null) {
      return fallback;
    }

    String normalized = value.trim();
    return normalized.isEmpty() ? fallback : normalized;
  }

  public static String resolveStatus(EnrollmentPeriod period) {
    if (period == null) {
      return STATUS_CLOSED;
    }

    return isOpen(period, LocalDateTime.now()) ? STATUS_OPEN : STATUS_CLOSED;
  }

  public static boolean isOpen(EnrollmentPeriod period, LocalDateTime now) {
    if (period == null || period.getStartDate() == null || period.getEndDate() == null) {
      return false;
    }

    LocalDateTime startDateTime = LocalDateTime.ofInstant(period.getStartDate().toInstant(), ZoneId.systemDefault());
    LocalDateTime endDateTime = LocalDateTime.ofInstant(period.getEndDate().toInstant(), ZoneId.systemDefault());

    return !now.isBefore(startDateTime) && !now.isAfter(endDateTime);
  }

  public static String formatDateTime(Date date) {
    if (date == null) {
      return "N/A";
    }

    Instant instant = date.toInstant();
    LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    return DATE_TIME_FORMATTER.format(localDateTime);
  }
}
