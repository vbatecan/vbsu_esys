package com.group5.paul_esys.modules.registrar.model;

import java.util.List;
import java.util.Set;

public record ScheduleGenerationTemplateBlock(
    String blockCode,
    String blockLabel,
    int minutes,
    List<String> preferredDays,
    Set<String> allowedRoomTypes,
    boolean requiresDifferentDayFromOffering
) {
}
