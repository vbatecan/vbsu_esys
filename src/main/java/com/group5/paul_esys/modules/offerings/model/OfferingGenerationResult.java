package com.group5.paul_esys.modules.offerings.model;

public record OfferingGenerationResult(
    boolean successful,
    int candidateCount,
    int existingCount,
    int createdCount,
    int skippedCount,
    String message
) {
}
