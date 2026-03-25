package com.group5.paul_esys.modules.curriculum.model;

import java.util.Date;

public class Curriculum {

  private Long id;
  private String semester;
  private Date curYear;
  private Long course;

  public Curriculum() {
  }

  public Curriculum(Long id, String semester, Date curYear, Long course) {
    this.id = id;
    this.semester = semester;
    this.curYear = curYear;
    this.course = course;
  }

  public Long getId() {
    return id;
  }

  public Curriculum setId(Long id) {
    this.id = id;
    return this;
  }

  public String getSemester() {
    return semester;
  }

  public Curriculum setSemester(String semester) {
    this.semester = semester;
    return this;
  }

  public Date getCurYear() {
    return curYear;
  }

  public Curriculum setCurYear(Date curYear) {
    this.curYear = curYear;
    return this;
  }

  public Long getCourse() {
    return course;
  }

  public Curriculum setCourse(Long course) {
    this.course = course;
    return this;
  }
}
