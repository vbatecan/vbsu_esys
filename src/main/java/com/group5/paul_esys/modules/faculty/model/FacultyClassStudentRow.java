package com.group5.paul_esys.modules.faculty.model;

import com.group5.paul_esys.modules.enums.StudentEnrolledSubjectStatus;

public record FacultyClassStudentRow(
    String studentId,
    Long enrollmentId,
    Long offeringId,
    Long semesterSubjectId,
    String fullName,
    String studentStatus,
    String course,
    String curriculum,
    String yearLevel,
    StudentEnrolledSubjectStatus enrolledSubjectStatus
) {
}
