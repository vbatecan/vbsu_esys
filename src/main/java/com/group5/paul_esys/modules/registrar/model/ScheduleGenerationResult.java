package com.group5.paul_esys.modules.registrar.model;

import java.util.List;

public record ScheduleGenerationResult(
    boolean successful,
    int candidateCount,
    int readyCount,
    int createdCount,
    int skippedCount,
    List<ScheduleGenerationPlanRow> rows,
    String message
) {
}
