package org.example.finaloop.service;

public class Authentication {

    public static boolean isValidEmail(String email){

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email != null && email.matches(emailRegex);
    }

    public static boolean isValidPassword(String password){
        if (password == null || password.length()<8){
            return false;
        }
        String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$";
        return password.matches(passwordRegex);
    }

}
