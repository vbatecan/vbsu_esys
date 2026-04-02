package com.group5.paul_esys.modules.registrar.model;

import java.sql.Timestamp;

public class Registrar {

  private Long id;
  private Long userId;
  private String employeeId;
  private String firstName;
  private String lastName;
  private String contactNumber;
  private Timestamp updatedAt;
  private Timestamp createdAt;

  public Registrar() {
  }

  public Registrar(Long id, Long userId, String employeeId, String firstName, String lastName, String contactNumber, Timestamp updatedAt, Timestamp createdAt) {
    this.id = id  ;
    this.userId = userId;
    this.employeeId = employeeId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.contactNumber = contactNumber;
    this.updatedAt = updatedAt;
    this.createdAt = createdAt;
  }

    public Long getId() {
        return id;
    }

    public Registrar setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getUserId() {
        return userId;
    }

    public Registrar setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public Registrar setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
        return this;
    }

  public String getFirstName() {
    return firstName;
  }

  public Registrar setFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public String getLastName() {
    return lastName;
  }

  public Registrar setLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public String getContactNumber() {
    return contactNumber;
  }

  public Registrar setContactNumber(String contactNumber) {
    this.contactNumber = contactNumber;
    return this;
  }

  public Timestamp getUpdatedAt() {
    return updatedAt;
  }

  public Registrar setUpdatedAt(Timestamp updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public Registrar setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
    return this;
  }
}
