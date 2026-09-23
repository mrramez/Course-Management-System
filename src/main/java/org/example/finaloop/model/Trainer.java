package org.example.finaloop.model;

import org.example.finaloop.database.DataBaseStart;

import java.util.List;
import java.util.Map;

public class Trainer extends ParentUsers {

    public Trainer() {
        super();
        setUserType("trainer");
    }

    public Trainer(int userId, String userName, String userEmail) {
        super(userId, userName, userEmail, "trainer");
    }

    @Override
    public String getHomePage() {
        return "TrainerDashboard.fxml";
    }

    public List<Course> viewMyCourses() {
        return DataBaseStart.getTrainerCourses(getUserId());
    }

    public List<Map<String, Object>> viewMyStudents() {
        return DataBaseStart.getTrainerStudents(getUserId());
    }

    public List<Map<String, Object>> viewMyStudents(int courseId) {
        return DataBaseStart.getCourseStudents(getUserId(), courseId);
    }

    public List<Attendance> getAttendanceSheet(int courseId, String date) {
        return DataBaseStart.getAttendanceSheet(getUserId(), courseId, date);
    }

    public boolean markAttendance(List<Attendance> attendanceList) {
        return DataBaseStart.markAttendance(getUserId(), attendanceList);
    }

    @Override
    public String toString() {
        return getUserName();
    }
}
