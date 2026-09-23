package org.example.finaloop.model;

import org.example.finaloop.database.DataBaseStart;
import org.example.finaloop.service.Session;

import java.util.List;

public class Admin extends ParentUsers {

    public Admin() {
        super();
        setUserType("admin");
    }

    public Admin(int userId, String userName, String userEmail) {
        super(userId, userName, userEmail, "admin");
    }

    @Override
    public String getHomePage() {
        return "AdminDashboard.fxml";
    }

    public boolean isAuthorizedAdmin() {
        return "admin".equalsIgnoreCase(getUserType()) && "admin".equalsIgnoreCase(Session.getCurrentRole());
    }

    public boolean addCourse(String name, String description, double price, int trainerId, String imagePath) {
        if (!isAuthorizedAdmin()) {
            return false;
        }
        return DataBaseStart.addCourse(name, description, price, trainerId, imagePath);
    }

    public boolean updateCourse(int courseId, String name, String description, double price, int trainerId, String imagePath) {
        if (!isAuthorizedAdmin()) {
            return false;
        }
        return DataBaseStart.updateCourse(courseId, name, description, price, trainerId, imagePath);
    }

    public boolean deleteCourse(int courseId) {
        if (!isAuthorizedAdmin()) {
            return false;
        }
        return DataBaseStart.deleteCourse(courseId);
    }

    public List<Course> viewAllCourses() {
        return DataBaseStart.getAllCourses();
    }
}
