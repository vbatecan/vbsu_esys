package com.group5.paul_esys.modules.registrar.model;

public record TargetSelection(
    Long offeringId,
    Long subjectId,
    Long enrollmentPeriodId,
    Long semesterSubjectId
) {
}
