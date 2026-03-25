package com.group5.paul_esys.modules.curriculum.services;

import com.group5.paul_esys.modules.curriculum.model.Curriculum;
import com.group5.paul_esys.modules.curriculum.utils.CurriculumUtils;
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

public class CurriculumService {

  private final Connection conn = ConnectionService.getConnection();
  private final Logger logger = LoggerFactory.getLogger(CurriculumService.class);

  public List<Curriculum> getAllCurriculums() {
    List<Curriculum> curriculums = new ArrayList<>();
    try {
      PreparedStatement ps = conn.prepareStatement("SELECT * FROM curriculum ORDER BY cur_year DESC, semester");
      ResultSet rs = ps.executeQuery();
      
      while (rs.next()) {
        curriculums.add(CurriculumUtils.mapResultSetToCurriculum(rs));
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return curriculums;
  }

  public Optional<Curriculum> getCurriculumById(Long id) {
    try {
      PreparedStatement ps = conn.prepareStatement("SELECT * FROM curriculum WHERE id = ?");
      ps.setLong(1, id);
      
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return Optional.of(CurriculumUtils.mapResultSetToCurriculum(rs));
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return Optional.empty();
  }

  public List<Curriculum> getCurriculumsByCourse(Long courseId) {
    List<Curriculum> curriculums = new ArrayList<>();
    try {
      PreparedStatement ps = conn.prepareStatement("SELECT * FROM curriculum WHERE course = ? ORDER BY cur_year DESC, semester");
      ps.setLong(1, courseId);
      
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        curriculums.add(CurriculumUtils.mapResultSetToCurriculum(rs));
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return curriculums;
  }

  public boolean createCurriculum(Curriculum curriculum) {
    try {
      PreparedStatement ps = conn.prepareStatement(
          "INSERT INTO curriculum (semester, cur_year, course) VALUES (?, ?, ?)"
      );
      ps.setString(1, curriculum.getSemester());
      ps.setDate(2, new java.sql.Date(curriculum.getCurYear().getTime()));
      ps.setLong(3, curriculum.getCourse());
      
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public boolean updateCurriculum(Curriculum curriculum) {
    try {
      PreparedStatement ps = conn.prepareStatement(
          "UPDATE curriculum SET semester = ?, cur_year = ?, course = ? WHERE id = ?"
      );
      ps.setString(1, curriculum.getSemester());
      ps.setDate(2, new java.sql.Date(curriculum.getCurYear().getTime()));
      ps.setLong(3, curriculum.getCourse());
      ps.setLong(4, curriculum.getId());
      
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public boolean deleteCurriculum(Long id) {
    try {
      PreparedStatement ps = conn.prepareStatement("DELETE FROM curriculum WHERE id = ?");
      ps.setLong(1, id);
      
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }
}
