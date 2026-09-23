package org.example.finaloop.model;

import org.example.finaloop.database.DataBaseStart;

import java.util.List;

public class Student extends ParentUsers {

    public Student() {
        super();
        setUserType("student");
    }

    public Student(int userId, String userName, String userEmail) {
        super(userId, userName, userEmail, "student");
    }

    @Override
    public String getHomePage() {
        return "CoursesHub.fxml";
    }

    public int enrollInCourse(int courseId) {
        return DataBaseStart.enrollInCourse(getUserId(), courseId);
    }

    public boolean payFees(int enrollmentId) {
        return DataBaseStart.payFees(enrollmentId);
    }

    public List<Enrollment> getMyCourses() {
        return DataBaseStart.getMyCourses(getUserId());
    }

    public boolean addReview(int courseId, int rating, String comment) {
        return DataBaseStart.addReview(getUserId(), courseId, rating, comment);
    }
}
