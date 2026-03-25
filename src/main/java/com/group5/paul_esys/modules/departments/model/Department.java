package com.group5.paul_esys.modules.departments.model;

import java.sql.Timestamp;

public class Department {

  private Long id;
  private String departmentName;
  private String description;
  private Timestamp updatedAt;
  private Timestamp createdAt;

  public Department() {
  }

  public Department(Long id, String departmentName, String description, Timestamp updatedAt, Timestamp createdAt) {
    this.id = id;
    this.departmentName = departmentName;
    this.description = description;
    this.updatedAt = updatedAt;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public Department setId(Long id) {
    this.id = id;
    return this;
  }

  public String getDepartmentName() {
    return departmentName;
  }

  public Department setDepartmentName(String departmentName) {
    this.departmentName = departmentName;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public Department setDescription(String description) {
    this.description = description;
    return this;
  }

  public Timestamp getUpdatedAt() {
    return updatedAt;
  }

  public Department setUpdatedAt(Timestamp updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public Department setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
    return this;
  }
}
