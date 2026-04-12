package com.group5.paul_esys.modules.registrar.model;

public record ScheduleGenerationOfferingCandidate(
    Long offeringId,
    String subjectCode,
    String subjectName,
    String schedulePattern,
    Integer estimatedMinutes,
    Integer requiredCapacity
) {
}
