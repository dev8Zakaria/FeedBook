package com.example.feedbook.service;

import com.example.feedbook.dao.UserDao;
import com.example.feedbook.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@ApplicationScoped
public class AuthService {

    @Inject
     UserDao userDao;

    /**
     * Registers a new user. Throws if username or email is already taken.
     */
    
    public User register(String username, String email, String password) {
        if (userDao.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already taken: " + username);
        }
        if (userDao.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(hashPassword(password));

        userDao.save(user);
        return user;
    }

    /**
     * Logs in a user by email and password.
     * Returns the User if credentials match and account is not banned.
     */
    public User login(String email, String password) {
        Optional<User> optional = userDao.findByEmail(email);

        if (optional.isEmpty()) {
            throw new IllegalArgumentException("No account found with that email.");
        }

        User user = optional.get();

        if (user.getIsBanned()) {
            throw new IllegalStateException("This account has been banned.");
        }

        if (!user.getPassword().equals(hashPassword(password))) {
            throw new IllegalArgumentException("Incorrect password.");
        }

        return user;
    }

    /**
     * SHA-256 password hashing.
     * In production, replace with BCrypt (e.g. via a library dependency).
     */
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
