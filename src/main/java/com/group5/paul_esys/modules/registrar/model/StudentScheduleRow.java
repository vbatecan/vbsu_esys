package com.group5.paul_esys.modules.registrar.model;

public record StudentScheduleRow(
    Long enrollmentDetailId,
    Long enrollmentId,
    Long offeringId,
    Long subjectId,
    Long enrollmentPeriodId,
    String subjectCode,
    String subjectName,
    String sectionCode,
    String instructor,
    String schedule,
    String room,
    Float units
) {
}
