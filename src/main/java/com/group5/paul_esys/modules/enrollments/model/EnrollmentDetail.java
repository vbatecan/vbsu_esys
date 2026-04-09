package com.group5.paul_esys.modules.enrollments.model;

import com.group5.paul_esys.modules.enums.EnrollmentDetailStatus;
import java.sql.Timestamp;

public class EnrollmentDetail {

  private Long id;
  private Long enrollmentId;
  private Long offeringId;
  private Float units;
  private EnrollmentDetailStatus status;
  private Timestamp createdAt;
  private Timestamp updatedAt;

  public EnrollmentDetail() {
  }

  public EnrollmentDetail(Long id, Long enrollmentId, Long offeringId, Float units, EnrollmentDetailStatus status, Timestamp createdAt, Timestamp updatedAt) {
    this.id = id;
    this.enrollmentId = enrollmentId;
    this.offeringId = offeringId;
    this.units = units;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public EnrollmentDetail setId(Long id) {
    this.id = id;
    return this;
  }

  public Long getEnrollmentId() {
    return enrollmentId;
  }

  public EnrollmentDetail setEnrollmentId(Long enrollmentId) {
    this.enrollmentId = enrollmentId;
    return this;
  }

  public Long getOfferingId() {
    return offeringId;
  }

  public EnrollmentDetail setOfferingId(Long offeringId) {
    this.offeringId = offeringId;
    return this;
  }

  public Float getUnits() {
    return units;
  }

  public EnrollmentDetail setUnits(Float units) {
    this.units = units;
    return this;
  }

  public EnrollmentDetailStatus getStatus() {
    return status;
  }

  public EnrollmentDetail setStatus(EnrollmentDetailStatus status) {
    this.status = status;
    return this;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public EnrollmentDetail setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public Timestamp getUpdatedAt() {
    return updatedAt;
  }

  public EnrollmentDetail setUpdatedAt(Timestamp updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }
}
