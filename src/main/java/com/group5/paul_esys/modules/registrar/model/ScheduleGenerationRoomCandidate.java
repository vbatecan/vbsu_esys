package com.group5.paul_esys.modules.registrar.model;

public record ScheduleGenerationRoomCandidate(
    Long roomId,
    String roomLabel,
    String roomType,
    Integer capacity
) {
}
