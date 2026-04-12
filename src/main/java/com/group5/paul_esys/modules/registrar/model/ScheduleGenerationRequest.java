package com.group5.paul_esys.modules.registrar.model;

import java.time.LocalTime;

public record ScheduleGenerationRequest(
    Long sectionId,
    Long enrollmentPeriodId,
    LocalTime minStartTime,
    LocalTime maxEndTime,
    Integer slotMinutes
) {
}
