package com.group5.paul_esys.modules.registrar.model;

public record SourceSelection(
    Long enrollmentDetailId,
    Long enrollmentId,
    Long offeringId,
    Long subjectId,
    Long enrollmentPeriodId,
    Float units,
    String status
) {
}
