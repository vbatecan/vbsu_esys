package com.group5.paul_esys.modules.offerings.model;

public record OfferingGenerationPlanRow(
    Long subjectId,
    String subjectCode,
    String subjectName,
    Long sectionId,
    String sectionCode,
    Integer sectionCapacity,
    Long semesterSubjectId,
    boolean alreadyExists
) {
}
