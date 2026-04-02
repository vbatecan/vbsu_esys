package com.group5.paul_esys.modules.courses.services;

import com.group5.paul_esys.modules.courses.model.Course;
import com.group5.paul_esys.modules.courses.utils.CourseUtils;
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

public class CourseService {

  private static final CourseService INSTANCE = new CourseService();
  private static final Logger logger = LoggerFactory.getLogger(CourseService.class);

  private CourseService() {
  }

  public static CourseService getInstance() {
    return INSTANCE;
  }

  public List<Course> getAllCourses() {
    List<Course> courses = new ArrayList<>();
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM courses ORDER BY course_name");
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        courses.add(CourseUtils.mapResultSetToCourse(rs));
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return courses;
  }

  public Optional<Course> getCourseById(Long id) {
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM courses WHERE id = ?")) {
      ps.setLong(1, id);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(CourseUtils.mapResultSetToCourse(rs));
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return Optional.empty();
  }

  public List<Course> getCoursesByDepartment(Long departmentId) {
    List<Course> courses = new ArrayList<>();
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM courses WHERE department_id = ? ORDER BY course_name")) {
      ps.setLong(1, departmentId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          courses.add(CourseUtils.mapResultSetToCourse(rs));
        }
      }
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
    }
    return courses;
  }

  public boolean createCourse(Course course) {
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO courses (course_name, description, department_id) VALUES (?, ?, ?)"
        )) {
      ps.setString(1, course.getCourseName());
      ps.setString(2, course.getDescription());
      ps.setLong(3, course.getDepartmentId());
      
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public boolean updateCourse(Course course) {
    try (Connection conn = ConnectionService.getConnection();
      PreparedStatement ps = conn.prepareStatement(
        "UPDATE courses SET course_name = ?, description = ?, department_id = ? WHERE id = ?"
      )) {
      ps.setString(1, course.getCourseName());
      ps.setString(2, course.getDescription());
      ps.setLong(3, course.getDepartmentId());
      ps.setLong(4, course.getId());
      
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }

  public boolean deleteCourse(Long id) {
    try (Connection conn = ConnectionService.getConnection();
        PreparedStatement ps = conn.prepareStatement("DELETE FROM courses WHERE id = ?")) {
      ps.setLong(1, id);
      
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      logger.error("ERROR: " + e.getMessage(), e);
      return false;
    }
  }
}
