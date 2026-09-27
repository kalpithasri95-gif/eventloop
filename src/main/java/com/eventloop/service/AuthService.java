package com.eventloop.service;

import com.eventloop.dao.UserDAO;
import com.eventloop.exception.ValidationException;
import com.eventloop.model.User;
import com.eventloop.util.ValidationUtil;
import java.sql.SQLException;

public class AuthService {
    private static AuthService instance;
    private final UserDAO userDAO;
    private User currentUser;

    private AuthService() {
        this.userDAO = new UserDAO();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public User login(String username, String password) throws ValidationException, SQLException {
        ValidationUtil.requireNonEmpty(username, "Username");
        ValidationUtil.requireNonEmpty(password, "Password");

        User user = userDAO.authenticate(username.trim(), password);
        if (user == null) {
            throw new ValidationException("Invalid username or password.");
        }
        this.currentUser = user;
        return user;
    }

    public boolean register(String username, String password, String fullName, String email, String role, String department) 
            throws ValidationException, SQLException {
        ValidationUtil.requireNonEmpty(username, "Username");
        ValidationUtil.requireNonEmpty(password, "Password");
        ValidationUtil.requireNonEmpty(fullName, "Full Name");
        ValidationUtil.requireNonEmpty(email, "Email");
        ValidationUtil.validateEmail(email);

        if (password.length() < 4) {
            throw new ValidationException("Password must be at least 4 characters long.");
        }

        if (userDAO.isUsernameTaken(username.trim())) {
            throw new ValidationException("Username '" + username + "' is already taken.");
        }

        if (userDAO.isEmailTaken(email.trim())) {
            throw new ValidationException("Email '" + email + "' is already registered.");
        }

        User newUser = new User(0, username.trim(), password, fullName.trim(), email.trim(), role, department != null ? department.trim() : "General");
        return userDAO.register(newUser);
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}
