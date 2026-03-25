package com.group5.paul_esys.modules.subjects.services;

import com.group5.paul_esys.modules.subjects.model.Subject;
import com.group5.paul_esys.modules.subjects.utils.SubjectUtils;
import com.group5.paul_esys.modules.users.services.ConnectionService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubjectService {

  private final Connection conn = ConnectionService.getConnection();
  private final Logger logger = LoggerFactory.getLogger(SubjectService.class);

  public List<Subject> getAllSubjects() {
    List<Subject> subjects = new ArrayList<>();
    try {
      PreparedStatement ps = conn.prepareStatement("SELECT * FROM subjects ORDER BY subject_code");
      ResultSet rs = ps.executeQuery();
      
      while (rs.next()) {
        subjects.add(SubjectUtils.mapResultSetToSubject(rs));
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return subjects;
  }

  public Optional<Subject> getSubjectById(Long id) {
    try {
      PreparedStatement ps = conn.prepareStatement("SELECT * FROM subjects WHERE id = ?");
      ps.setLong(1, id);
      
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return Optional.of(SubjectUtils.mapResultSetToSubject(rs));
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return Optional.empty();
  }

  public Optional<Subject> getSubjectByCode(String subjectCode) {
    try {
      PreparedStatement ps = conn.prepareStatement("SELECT * FROM subjects WHERE subject_code = ?");
      ps.setString(1, subjectCode);
      
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return Optional.of(SubjectUtils.mapResultSetToSubject(rs));
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return Optional.empty();
  }

  public List<Subject> getSubjectsByDepartment(Long departmentId) {
    List<Subject> subjects = new ArrayList<>();
    try {
      PreparedStatement ps = conn.prepareStatement("SELECT * FROM subjects WHERE department_id = ? ORDER BY subject_code");
      ps.setLong(1, departmentId);
      
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        subjects.add(SubjectUtils.mapResultSetToSubject(rs));
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return subjects;
  }

  public List<Subject> getSubjectsByCurriculum(Long curriculumId) {
    List<Subject> subjects = new ArrayList<>();
    try {
      PreparedStatement ps = conn.prepareStatement("SELECT * FROM subjects WHERE curriculum_id = ? ORDER BY subject_code");
      ps.setLong(1, curriculumId);
      
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        subjects.add(SubjectUtils.mapResultSetToSubject(rs));
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return subjects;
  }

  public boolean createSubject(Subject subject) {
    try {
      PreparedStatement ps = conn.prepareStatement(
          "INSERT INTO subjects (subject_name, subject_code, units, description, curriculum_id, department_id) VALUES (?, ?, ?, ?, ?, ?)"
      );
      ps.setString(1, subject.getSubjectName());
      ps.setString(2, subject.getSubjectCode());
      ps.setFloat(3, subject.getUnits());
      ps.setString(4, subject.getDescription());
      ps.setLong(5, subject.getCurriculumId());
      ps.setLong(6, subject.getDepartmentId());
      
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public boolean updateSubject(Subject subject) {
    try {
      PreparedStatement ps = conn.prepareStatement(
          "UPDATE subjects SET subject_name = ?, subject_code = ?, units = ?, description = ?, curriculum_id = ?, department_id = ? WHERE id = ?"
      );
      ps.setString(1, subject.getSubjectName());
      ps.setString(2, subject.getSubjectCode());
      ps.setFloat(3, subject.getUnits());
      ps.setString(4, subject.getDescription());
      ps.setLong(5, subject.getCurriculumId());
      ps.setLong(6, subject.getDepartmentId());
      ps.setLong(7, subject.getId());
      
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public boolean deleteSubject(Long id) {
    try {
      PreparedStatement ps = conn.prepareStatement("DELETE FROM subjects WHERE id = ?");
      ps.setLong(1, id);
      
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }
}
