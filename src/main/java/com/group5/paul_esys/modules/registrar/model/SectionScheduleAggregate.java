package com.group5.paul_esys.modules.registrar.model;

import java.util.LinkedHashSet;
import java.util.Set;

public final class SectionScheduleAggregate {

  private final Long offeringId;
  private final String sectionCode;
  private final Integer offeringCapacity;
  private final Integer sectionCapacity;
  private final Set<String> instructorParts = new LinkedHashSet<>();
  private final Set<String> scheduleParts = new LinkedHashSet<>();
  private final Set<String> roomParts = new LinkedHashSet<>();

  public SectionScheduleAggregate(Long offeringId, String sectionCode, Integer offeringCapacity, Integer sectionCapacity) {
    this.offeringId = offeringId;
    this.sectionCode = sectionCode;
    this.offeringCapacity = offeringCapacity;
    this.sectionCapacity = sectionCapacity;
  }

  public Long offeringId() {
    return offeringId;
  }

  public String sectionCode() {
    return sectionCode;
  }

  public Integer offeringCapacity() {
    return offeringCapacity;
  }

  public Integer sectionCapacity() {
    return sectionCapacity;
  }

  public Set<String> instructorParts() {
    return instructorParts;
  }

  public Set<String> scheduleParts() {
    return scheduleParts;
  }

  public Set<String> roomParts() {
    return roomParts;
  }
}
