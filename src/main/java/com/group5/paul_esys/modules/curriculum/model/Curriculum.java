package com.group5.paul_esys.modules.curriculum.model;

import java.util.Date;

public class Curriculum {

  private Long id;
  private String name;
  private Date curYear;
  private Long course;

  public Curriculum() {
  }

  public Curriculum(Long id, String name, Date curYear, Long course) {
    this.id = id;
    this.name = name;
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

  public String getName() {
    return name;
  }

  public Curriculum setName(String name) {
    this.name = name;
    return this;
  }

  public String getSemester() {
    return getName();
  }

  public Curriculum setSemester(String semester) {
    return setName(semester);
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
