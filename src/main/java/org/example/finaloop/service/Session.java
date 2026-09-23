package org.example.finaloop.service;

public class Session {

    private static int currentUserId = -1;
    private static String currentEmail;
    private static String currentRole;

    private Session() {
    }

    public static void login(int userId, String email, String role) {
        currentUserId = userId;
        currentEmail = email;
        currentRole = role;
    }

    public static void loginAsGuest() {
        currentUserId = -1;
        currentEmail = null;
        currentRole = "guest";
    }

    public static void logout() {
        currentUserId = -1;
        currentEmail = null;
        currentRole = null;
    }

    public static boolean isGuest() {
        return "guest".equals(currentRole);
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static String getCurrentEmail() {
        return currentEmail;
    }

    public static String getCurrentRole() {
        return currentRole;
    }

    public static boolean isLoggedIn() {
        return currentUserId != -1;
    }
}
