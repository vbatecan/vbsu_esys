package com.group5.paul_esys.modules.subjects.model;

public enum SubjectSchedulePattern {
  LECTURE_ONLY,
  LECTURE_LAB,
  GE_PAIRED,
  PE_PAIRED,
  NSTP_BLOCK;

  public static SubjectSchedulePattern fromValue(String value) {
    if (value == null || value.isBlank()) {
      return LECTURE_ONLY;
    }

    try {
      return SubjectSchedulePattern.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      return LECTURE_ONLY;
    }
  }
}
