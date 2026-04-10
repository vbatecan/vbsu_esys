package com.group5.paul_esys.modules.registrar.model;

public record SectionScheduleOption(
    Long offeringId,
    String sectionCode,
    String instructor,
    String schedule,
    String room,
    Integer availableSlots
) {
}
