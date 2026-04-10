package com.group5.paul_esys.modules.registrar.model;

import java.util.LinkedHashSet;
import java.util.Set;

public final class StudentScheduleAggregate {

  private final Long enrollmentDetailId;
  private final Long enrollmentId;
  private final Long offeringId;
  private final Long subjectId;
  private final Long enrollmentPeriodId;
  private final String subjectCode;
  private final String subjectName;
  private final String sectionCode;
  private final Float units;
  private final Set<String> instructorParts = new LinkedHashSet<>();
  private final Set<String> scheduleParts = new LinkedHashSet<>();
  private final Set<String> roomParts = new LinkedHashSet<>();

  public StudentScheduleAggregate(
      Long enrollmentDetailId,
      Long enrollmentId,
      Long offeringId,
      Long subjectId,
      Long enrollmentPeriodId,
      String subjectCode,
      String subjectName,
      String sectionCode,
      Float units
  ) {
    this.enrollmentDetailId = enrollmentDetailId;
    this.enrollmentId = enrollmentId;
    this.offeringId = offeringId;
    this.subjectId = subjectId;
    this.enrollmentPeriodId = enrollmentPeriodId;
    this.subjectCode = subjectCode;
    this.subjectName = subjectName;
    this.sectionCode = sectionCode;
    this.units = units;
  }

  public Set<String> instructorParts() {
    return instructorParts;
  }

  public Set<String> scheduleParts() {
    return scheduleParts;
  }

  public Set<String> roomParts() {
    return roomParts;
  }

  public StudentScheduleRow toRow() {
    return new StudentScheduleRow(
        enrollmentDetailId,
        enrollmentId,
        offeringId,
        subjectId,
        enrollmentPeriodId,
        subjectCode,
        subjectName,
        sectionCode,
        instructorParts.isEmpty() ? "TBA" : String.join(", ", instructorParts),
        scheduleParts.isEmpty() ? "TBA" : String.join(" | ", scheduleParts),
        roomParts.isEmpty() ? "TBA" : String.join(", ", roomParts),
        units == null ? 0.0f : units
    );
  }
}
