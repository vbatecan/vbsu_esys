package com.group5.paul_esys.modules.enrollments.model;

import com.group5.paul_esys.modules.enums.StudentEnrolledSubjectStatus;
import java.sql.Timestamp;

public class StudentEnrolledSubject {

  private String studentId;
  private Long enrollmentId;
  private Long offeringId;
  private Long semesterSubjectId;
  private StudentEnrolledSubjectStatus status;
  private boolean selected;
  private Timestamp createdAt;
  private Timestamp updatedAt;

  public StudentEnrolledSubject() {
  }

  public StudentEnrolledSubject(
      String studentId,
      Long enrollmentId,
      Long offeringId,
      Long semesterSubjectId,
      StudentEnrolledSubjectStatus status,
      boolean selected,
      Timestamp createdAt,
      Timestamp updatedAt
  ) {
    this.studentId = studentId;
    this.enrollmentId = enrollmentId;
    this.offeringId = offeringId;
    this.semesterSubjectId = semesterSubjectId;
    this.status = status;
    this.selected = selected;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getStudentId() {
    return studentId;
  }

  public StudentEnrolledSubject setStudentId(String studentId) {
    this.studentId = studentId;
    return this;
  }

  public Long getEnrollmentId() {
    return enrollmentId;
  }

  public StudentEnrolledSubject setEnrollmentId(Long enrollmentId) {
    this.enrollmentId = enrollmentId;
    return this;
  }

  public Long getOfferingId() {
    return offeringId;
  }

  public StudentEnrolledSubject setOfferingId(Long offeringId) {
    this.offeringId = offeringId;
    return this;
  }

  public Long getSemesterSubjectId() {
    return semesterSubjectId;
  }

  public StudentEnrolledSubject setSemesterSubjectId(Long semesterSubjectId) {
    this.semesterSubjectId = semesterSubjectId;
    return this;
  }

  public StudentEnrolledSubjectStatus getStatus() {
    return status;
  }

  public StudentEnrolledSubject setStatus(StudentEnrolledSubjectStatus status) {
    this.status = status;
    return this;
  }

  public boolean isSelected() {
    return selected;
  }

  public StudentEnrolledSubject setSelected(boolean selected) {
    this.selected = selected;
    return this;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public StudentEnrolledSubject setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public Timestamp getUpdatedAt() {
    return updatedAt;
  }

  public StudentEnrolledSubject setUpdatedAt(Timestamp updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }
}
