package com.group5.paul_esys.modules.registrar.model;

public record ScheduleGenerationSectionOption(
    Long sectionId,
    String sectionCode,
    Long enrollmentPeriodId,
    String enrollmentPeriodLabel,
    Integer sectionCapacity
) {
}
