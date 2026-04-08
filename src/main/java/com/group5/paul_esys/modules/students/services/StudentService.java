package com.group5.paul_esys.modules.students.services;

import com.group5.paul_esys.modules.students.model.Student;
import com.group5.paul_esys.modules.students.model.StudentStatus;
import com.group5.paul_esys.modules.students.utils.StudentMapper;
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

public class StudentService {

    private static final StudentService INSTANCE = new StudentService();
    private static final Logger logger = LoggerFactory.getLogger(
        StudentService.class
    );

    private StudentService() {}

    public static StudentService getInstance() {
        return INSTANCE;
    }

    public Optional<Student> get(String studentId) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (
            Connection conn = ConnectionService.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    if (rs.getRow() > 0) {
                        return Optional.of(StudentMapper.mapToStudent(rs));
                    } else {
                        return Optional.empty();
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return Optional.empty();
    }

    public Optional<Student> insert(Student student) {
        String sql =
            "INSERT INTO students (student_id, user_id, first_name, last_name, middle_name, birthdate, student_status, course_id, year_level) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (
            Connection conn = ConnectionService.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, student.getStudentId());
            stmt.setLong(2, student.getUserId());
            stmt.setString(3, student.getFirstName());
            stmt.setString(4, student.getLastName());
            stmt.setString(5, student.getMiddleName());
            stmt.setDate(
                6,
                new java.sql.Date(student.getBirthdate().getTime())
            );
            stmt.setString(7, student.getStudentStatus().name());
            stmt.setLong(8, student.getCourseId());
            stmt.setLong(9, student.getYearLevel());

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                logger.info("A new student was inserted successfully!");
                return Optional.of(student);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return Optional.empty();
    }

    public void delete(String studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";

        try (
            Connection conn = ConnectionService.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, studentId);
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                logger.info(
                    "Student with ID %s was successfully deleted!",
                    studentId
                );
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    public Optional<Student> getStudentByUserId(Long userId) {
        String sql = "SELECT * FROM students WHERE user_id = ?";
        try (
            Connection conn = ConnectionService.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    if (rs.getRow() > 0) {
                        return Optional.of(StudentMapper.mapToStudent(rs));
                    } else {
                        return Optional.empty();
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return Optional.empty();
    }

    public List<Student> list() {
        String sql = "SELECT * FROM students";
        try (
            Connection conn = ConnectionService.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            List<Student> students = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(
                        new Student(
                            rs.getString("student_id"),
                            rs.getLong("user_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("middle_name"),
                            rs.getDate("birthdate"),
                            StudentStatus.valueOf(
                                rs.getString("student_status")
                            ),
                            rs.getLong("course_id"),
                            rs.getLong("year_level"),
                            rs.getTimestamp("created_at")
                        )
                    );
                }
            }

            return students;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return List.of(); // Placeholder muna
    }
}
