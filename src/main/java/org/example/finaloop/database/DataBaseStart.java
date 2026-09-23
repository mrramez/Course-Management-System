package org.example.finaloop.database;

import org.example.finaloop.model.Attendance;
import org.example.finaloop.model.Course;
import org.example.finaloop.model.Enrollment;
import org.example.finaloop.model.Review;
import org.example.finaloop.model.Trainer;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class DataBaseStart {

    public static void initializeDataBase(){
        String sql = """
                CREATE TABLE IF NOT EXISTS users(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                role TEXT NOT NULL DEFAULT 'user' CHECK(role IN('admin', 'student' , 'trainer')),
                create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;

        String enrollmentsSql = """
                CREATE TABLE IF NOT EXISTS enrollments(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER NOT NULL,
                course_id INTEGER NOT NULL,
                payment_status TEXT NOT NULL DEFAULT 'unpaid' CHECK(payment_status IN('unpaid','paid')),
                enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(student_id) REFERENCES users(id),
                FOREIGN KEY(course_id) REFERENCES courses(id)
                );
                """;

        String reviewsSql = """
                CREATE TABLE IF NOT EXISTS reviews(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER NOT NULL,
                course_id INTEGER NOT NULL,
                rating INTEGER NOT NULL CHECK(rating BETWEEN 1 AND 5),
                comment TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(student_id) REFERENCES users(id),
                FOREIGN KEY(course_id) REFERENCES courses(id)
                );
                """;

        String coursesSql = """
                CREATE TABLE IF NOT EXISTS courses(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                description TEXT,
                price REAL NOT NULL DEFAULT 0 CHECK(price >= 0),
                trainer_id INTEGER,
                image_path TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(trainer_id) REFERENCES users(id)
                );
                """;

        String attendanceSql = """
                CREATE TABLE IF NOT EXISTS attendance(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER NOT NULL,
                course_id INTEGER NOT NULL,
                attendance_date TEXT NOT NULL,
                status TEXT NOT NULL CHECK(status IN('present','absent')),
                FOREIGN KEY(student_id) REFERENCES users(id),
                FOREIGN KEY(course_id) REFERENCES courses(id),
                UNIQUE(student_id, course_id, attendance_date)
                );
                """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()){
            stmt.execute(sql);
            stmt.execute(coursesSql);
            stmt.execute(enrollmentsSql);
            stmt.execute(reviewsSql);
            stmt.execute(attendanceSql);
            System.out.println("Create table and DataBase is Done! ");
        } catch (SQLException e){
            System.err.println("Some Thing get wrong :( !"+ e.getMessage());
        }

        addImageColumnIfMissing();
    }

    private static void addImageColumnIfMissing(){
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(courses)")) {

            boolean found = false;
            while (rs.next()){
                if ("image_path".equalsIgnoreCase(rs.getString("name"))){
                    found = true;
                }
            }
            if (!found){
                stmt.execute("ALTER TABLE courses ADD COLUMN image_path TEXT");
            }

        } catch (SQLException e){
            System.err.println("AddImageColumn Error: " + e.getMessage());
        }
    }

    public static Connection connect() throws SQLException {
        return DBconnection.getConnection();
    }

    public static boolean registerUser(String userName, String userEmail, String plainPassword, String userRole){

        String sql = "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)";

        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));

        try(Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userName);
            pstmt.setString(2, userEmail);
            pstmt.setString(3, hashedPassword);
            pstmt.setString(4, userRole);

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e){
            System.err.println("Something get wrong while adding!!! "+ e.getMessage());
        return false;
        }
    }

    public static  String loginUser(String email, String plainPassword){
        String sql = "SELECT password_hash , role FROM users WHERE email = ?";
        try(Connection conn = connect();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                String storedHash = rs.getString("password_hash");
                String role = rs.getString("role");

                if (BCrypt.checkpw(plainPassword,storedHash)) {
                    return role;
                }
            }
        } catch (SQLException e){
            System.out.println("Login DB Error: "+ e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static int enrollInCourse(int studentId, int courseId){
        if (isAlreadyEnrolled(studentId, courseId)){
            return -1;
        }

        String sql = "INSERT INTO enrollments (student_id, course_id) VALUES (?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()){
                return keys.getInt(1);
            }
            return -1;

        } catch (SQLException e){
            System.err.println("Enroll Error: " + e.getMessage());
            return -1;
        }
    }

    public static boolean isAlreadyEnrolled(int studentId, int courseId){
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND course_id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e){
            System.err.println("IsAlreadyEnrolled Error: " + e.getMessage());
            return false;
        }
    }

    public static boolean payFees(int enrollmentId){
        String sql = "UPDATE enrollments SET payment_status = 'paid' WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, enrollmentId);
            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e){
            System.err.println("Payment Error: " + e.getMessage());
            return false;
        }
    }

    public static List<Enrollment> getMyCourses(int studentId){
        List<Enrollment> result = new ArrayList<>();
        String sql = "SELECT id, student_id, course_id, payment_status, enrolled_at FROM enrollments WHERE student_id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()){
                Enrollment e = new Enrollment();
                e.setId(rs.getInt("id"));
                e.setStudentId(rs.getInt("student_id"));
                e.setCourseId(rs.getInt("course_id"));
                e.setPaymentStatus(rs.getString("payment_status"));
                e.setEnrolledAt(rs.getString("enrolled_at"));
                result.add(e);
            }

        } catch (SQLException e){
            System.err.println("GetMyCourses Error: " + e.getMessage());
        }
        return result;
    }

    public static boolean addReview(int studentId, int courseId, int rating, String comment){
        String sql = "INSERT INTO reviews (student_id, course_id, rating, comment) VALUES (?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            pstmt.setInt(3, rating);
            pstmt.setString(4, comment);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e){
            System.err.println("AddReview Error: " + e.getMessage());
            return false;
        }
    }

    public static List<Review> getCourseReviews(int courseId){
        List<Review> result = new ArrayList<>();
        String sql = "SELECT id, student_id, course_id, rating, comment, created_at FROM reviews WHERE course_id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()){
                Review r = new Review();
                r.setId(rs.getInt("id"));
                r.setStudentId(rs.getInt("student_id"));
                r.setCourseId(rs.getInt("course_id"));
                r.setRating(rs.getInt("rating"));
                r.setComment(rs.getString("comment"));
                r.setCreatedAt(rs.getString("created_at"));
                result.add(r);
            }

        } catch (SQLException e){
            System.err.println("GetCourseReviews Error: " + e.getMessage());
        }
        return result;
    }

    public static int getUserIdByEmail(String email){
        String sql = "SELECT id FROM users WHERE email = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                return rs.getInt("id");
            }

        } catch (SQLException e){
            System.err.println("GetUserId Error: " + e.getMessage());
        }
        return -1;
    }

    public static List<Map<String, Object>> getAllCoursesForBrowse(){
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = """
                SELECT c.id AS id, c.name AS name, c.description AS description,
                       c.price AS price, c.image_path AS image_path, u.name AS trainer_name,
                       (SELECT COUNT(*) FROM enrollments e WHERE e.course_id = c.id) AS students_count,
                       (SELECT AVG(r.rating) FROM reviews r WHERE r.course_id = c.id) AS avg_rating,
                       (SELECT COUNT(*) FROM reviews r WHERE r.course_id = c.id) AS reviews_count
                FROM courses c
                LEFT JOIN users u ON c.trainer_id = u.id
                ORDER BY c.id DESC
                """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()){
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("name", rs.getString("name"));
                row.put("description", rs.getString("description"));
                row.put("price", rs.getDouble("price"));
                row.put("image_path", rs.getString("image_path"));
                row.put("trainer_name", rs.getString("trainer_name"));
                row.put("students_count", rs.getInt("students_count"));
                row.put("avg_rating", rs.getDouble("avg_rating"));
                row.put("reviews_count", rs.getInt("reviews_count"));
                result.add(row);
            }

        } catch (SQLException e){
            System.err.println("GetAllCourses Error (make sure the courses table exists): " + e.getMessage());
        }
        return result;
    }

    public static List<Map<String, Object>> getMyCoursesWithDetails(int studentId){
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = """
                SELECT e.id AS enrollment_id, e.course_id AS course_id,
                       c.name AS name, c.price AS price, e.payment_status AS payment_status,
                       c.image_path AS image_path, u.name AS trainer_name
                FROM enrollments e
                JOIN courses c ON e.course_id = c.id
                LEFT JOIN users u ON c.trainer_id = u.id
                WHERE e.student_id = ?
                ORDER BY e.id DESC
                """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()){
                Map<String, Object> row = new HashMap<>();
                row.put("enrollment_id", rs.getInt("enrollment_id"));
                row.put("course_id", rs.getInt("course_id"));
                row.put("name", rs.getString("name"));
                row.put("price", rs.getDouble("price"));
                row.put("payment_status", rs.getString("payment_status"));
                row.put("image_path", rs.getString("image_path"));
                row.put("trainer_name", rs.getString("trainer_name"));
                result.add(row);
            }

        } catch (SQLException e){
            System.err.println("GetMyCoursesWithDetails Error: " + e.getMessage());
        }
        return result;
    }

    public static Set<Integer> getEnrolledCourseIds(int studentId){
        Set<Integer> result = new HashSet<>();
        String sql = "SELECT course_id FROM enrollments WHERE student_id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                result.add(rs.getInt("course_id"));
            }

        } catch (SQLException e){
            System.err.println("GetEnrolledCourseIds Error: " + e.getMessage());
        }
        return result;
    }

    public static boolean addCourse(String name, String description, double price, int trainerId, String imagePath){
        String sql = "INSERT INTO courses (name, description, price, trainer_id, image_path) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, trainerId);
            pstmt.setString(5, imagePath);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e){
            System.err.println("AddCourse Error: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateCourse(int courseId, String name, String description, double price, int trainerId, String imagePath){
        String sql = "UPDATE courses SET name = ?, description = ?, price = ?, trainer_id = ?, image_path = ? WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, trainerId);
            pstmt.setString(5, imagePath);
            pstmt.setInt(6, courseId);
            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e){
            System.err.println("UpdateCourse Error: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteCourse(int courseId){
        String sql = "DELETE FROM courses WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement attendanceStmt = conn.prepareStatement("DELETE FROM attendance WHERE course_id = ?");
             PreparedStatement reviewsStmt = conn.prepareStatement("DELETE FROM reviews WHERE course_id = ?");
             PreparedStatement enrollmentsStmt = conn.prepareStatement("DELETE FROM enrollments WHERE course_id = ?");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            attendanceStmt.setInt(1, courseId);
            attendanceStmt.executeUpdate();
            reviewsStmt.setInt(1, courseId);
            reviewsStmt.executeUpdate();
            enrollmentsStmt.setInt(1, courseId);
            enrollmentsStmt.executeUpdate();

            pstmt.setInt(1, courseId);
            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e){
            System.err.println("DeleteCourse Error: " + e.getMessage());
            return false;
        }
    }

    public static List<Course> getAllCourses(){
        List<Course> result = new ArrayList<>();
        String sql = """
                SELECT c.id AS id, c.name AS name, c.description AS description,
                       c.price AS price, c.trainer_id AS trainer_id, c.image_path AS image_path,
                       u.name AS trainer_name,
                       (SELECT COUNT(*) FROM enrollments e WHERE e.course_id = c.id) AS students_count
                FROM courses c
                LEFT JOIN users u ON c.trainer_id = u.id
                ORDER BY c.id DESC
                """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()){
                Course c = new Course();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setDescription(rs.getString("description"));
                c.setPrice(rs.getDouble("price"));
                c.setTrainerId(rs.getInt("trainer_id"));
                c.setImagePath(rs.getString("image_path"));
                if (rs.getString("trainer_name") != null){
                    c.setTrainer(new Trainer(rs.getInt("trainer_id"), rs.getString("trainer_name"), null));
                }
                c.setStudentsCount(rs.getInt("students_count"));
                result.add(c);
            }

        } catch (SQLException e){
            System.err.println("GetAllCourses Error: " + e.getMessage());
        }
        return result;
    }

    public static List<Trainer> getAllTrainers(){
        List<Trainer> result = new ArrayList<>();
        String sql = "SELECT id, name, email FROM users WHERE role = 'trainer' ORDER BY name";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()){
                result.add(new Trainer(rs.getInt("id"), rs.getString("name"), rs.getString("email")));
            }

        } catch (SQLException e){
            System.err.println("GetAllTrainers Error: " + e.getMessage());
        }
        return result;
    }

    public static int countUsersByRole(String role){
        return count("SELECT COUNT(*) FROM users WHERE role = ?", role);
    }

    public static int countEnrollments(){
        return count("SELECT COUNT(*) FROM enrollments", null);
    }

    public static int countAttendanceSessions(int trainerId){
        String sql = """
                SELECT COUNT(DISTINCT a.course_id || '-' || a.attendance_date)
                FROM attendance a
                JOIN courses c ON a.course_id = c.id
                WHERE c.trainer_id = ?
                """;
        return count(sql, trainerId);
    }

    private static int count(String sql, Object parameter){
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (parameter != null){
                pstmt.setObject(1, parameter);
            }
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e){
            System.err.println("Count Error: " + e.getMessage());
            return 0;
        }
    }

    public static String getUserNameById(int userId){
        String sql = "SELECT name FROM users WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                return rs.getString("name");
            }

        } catch (SQLException e){
            System.err.println("GetUserName Error: " + e.getMessage());
        }
        return "";
    }

    public static List<Course> getTrainerCourses(int trainerId){
        List<Course> result = new ArrayList<>();
        String sql = """
                SELECT c.id AS id, c.name AS name, c.description AS description,
                       c.price AS price, c.trainer_id AS trainer_id, c.image_path AS image_path,
                       COUNT(e.id) AS students_count
                FROM courses c
                LEFT JOIN enrollments e ON e.course_id = c.id
                WHERE c.trainer_id = ?
                GROUP BY c.id
                ORDER BY c.name
                """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, trainerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()){
                Course c = new Course();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setDescription(rs.getString("description"));
                c.setPrice(rs.getDouble("price"));
                c.setTrainerId(rs.getInt("trainer_id"));
                c.setImagePath(rs.getString("image_path"));
                c.setStudentsCount(rs.getInt("students_count"));
                result.add(c);
            }

        } catch (SQLException e){
            System.err.println("GetTrainerCourses Error: " + e.getMessage());
        }
        return result;
    }

    public static List<Map<String, Object>> getTrainerStudents(int trainerId){
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = """
                SELECT u.id AS student_id, u.name AS name, u.email AS email,
                       c.name AS course_name, e.payment_status AS payment_status
                FROM enrollments e
                JOIN users u ON e.student_id = u.id
                JOIN courses c ON e.course_id = c.id
                WHERE c.trainer_id = ?
                ORDER BY c.name, u.name
                """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, trainerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()){
                Map<String, Object> row = new HashMap<>();
                row.put("student_id", rs.getInt("student_id"));
                row.put("name", rs.getString("name"));
                row.put("email", rs.getString("email"));
                row.put("course_name", rs.getString("course_name"));
                row.put("payment_status", rs.getString("payment_status"));
                result.add(row);
            }

        } catch (SQLException e){
            System.err.println("GetTrainerStudents Error: " + e.getMessage());
        }
        return result;
    }

    public static List<Map<String, Object>> getCourseStudents(int trainerId, int courseId){
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = """
                SELECT u.id AS student_id, u.name AS name, u.email AS email,
                       c.name AS course_name, e.payment_status AS payment_status
                FROM enrollments e
                JOIN users u ON e.student_id = u.id
                JOIN courses c ON e.course_id = c.id
                WHERE c.trainer_id = ? AND c.id = ?
                ORDER BY u.name
                """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, trainerId);
            pstmt.setInt(2, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()){
                Map<String, Object> row = new HashMap<>();
                row.put("student_id", rs.getInt("student_id"));
                row.put("name", rs.getString("name"));
                row.put("email", rs.getString("email"));
                row.put("course_name", rs.getString("course_name"));
                row.put("payment_status", rs.getString("payment_status"));
                result.add(row);
            }

        } catch (SQLException e){
            System.err.println("GetCourseStudents Error: " + e.getMessage());
        }
        return result;
    }

    public static List<Attendance> getAttendanceSheet(int trainerId, int courseId, String date){
        List<Attendance> result = new ArrayList<>();
        String sql = """
                SELECT u.id AS student_id, u.name AS name, a.id AS attendance_id, a.status AS status
                FROM enrollments e
                JOIN users u ON e.student_id = u.id
                JOIN courses c ON e.course_id = c.id
                LEFT JOIN attendance a ON a.student_id = e.student_id
                                      AND a.course_id = e.course_id
                                      AND a.attendance_date = ?
                WHERE e.course_id = ? AND c.trainer_id = ?
                ORDER BY u.name
                """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, date);
            pstmt.setInt(2, courseId);
            pstmt.setInt(3, trainerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()){
                Attendance a = new Attendance();
                a.setId(rs.getInt("attendance_id"));
                a.setStudentId(rs.getInt("student_id"));
                a.setCourseId(courseId);
                a.setAttendanceDate(date);
                a.setStatus(rs.getString("status"));
                a.setStudentName(rs.getString("name"));
                result.add(a);
            }

        } catch (SQLException e){
            System.err.println("GetAttendanceSheet Error: " + e.getMessage());
        }
        return result;
    }

    public static boolean markAttendance(int trainerId, List<Attendance> attendanceList){
        String checkSql = "SELECT COUNT(*) FROM courses WHERE id = ? AND trainer_id = ?";
        String sql = """
                INSERT INTO attendance (student_id, course_id, attendance_date, status)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(student_id, course_id, attendance_date)
                DO UPDATE SET status = excluded.status
                """;

        try (Connection conn = connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Attendance a : attendanceList){
                checkStmt.setInt(1, a.getCourseId());
                checkStmt.setInt(2, trainerId);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next() || rs.getInt(1) == 0){
                    System.err.println("MarkAttendance Error: course " + a.getCourseId() + " is not taught by this trainer");
                    return false;
                }

                pstmt.setInt(1, a.getStudentId());
                pstmt.setInt(2, a.getCourseId());
                pstmt.setString(3, a.getAttendanceDate());
                pstmt.setString(4, a.getStatus());
                pstmt.executeUpdate();
            }
            return true;

        } catch (SQLException e){
            System.err.println("MarkAttendance Error: " + e.getMessage());
            return false;
        }
    }

}
