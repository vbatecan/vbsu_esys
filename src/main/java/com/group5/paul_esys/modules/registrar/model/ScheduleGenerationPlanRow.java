package com.group5.paul_esys.modules.registrar.model;

import java.time.LocalTime;

public record ScheduleGenerationPlanRow(
    Long offeringId,
    String subjectCode,
    String subjectName,
    String schedulePattern,
    String blockLabel,
    Integer estimatedMinutes,
    String day,
    LocalTime startTime,
    LocalTime endTime,
    Long roomId,
    String roomLabel,
    String status,
    String details
) {
}
