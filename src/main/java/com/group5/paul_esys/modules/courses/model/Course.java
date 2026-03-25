package com.group5.paul_esys.modules.courses.model;

import java.sql.Timestamp;

public class Course {

  private Long id;
  private String courseName;
  private String description;
  private Long departmentId;
  private Timestamp updatedAt;
  private Timestamp createdAt;

  public Course() {
  }

  public Course(Long id, String courseName, String description, Long departmentId, Timestamp updatedAt, Timestamp createdAt) {
    this.id = id;
    this.courseName = courseName;
    this.description = description;
    this.departmentId = departmentId;
    this.updatedAt = updatedAt;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public Course setId(Long id) {
    this.id = id;
    return this;
  }

  public String getCourseName() {
    return courseName;
  }

  public Course setCourseName(String courseName) {
    this.courseName = courseName;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public Course setDescription(String description) {
    this.description = description;
    return this;
  }

  public Long getDepartmentId() {
    return departmentId;
  }

  public Course setDepartmentId(Long departmentId) {
    this.departmentId = departmentId;
    return this;
  }

  public Timestamp getUpdatedAt() {
    return updatedAt;
  }

  public Course setUpdatedAt(Timestamp updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public Course setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
    return this;
  }
}
