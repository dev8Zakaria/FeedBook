package com.example.feedbook.service;

import com.example.feedbook.dao.UserDao;
import com.example.feedbook.entity.Role;
import com.example.feedbook.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class UserService {

    @Inject
     UserDao userDao;

    public User findById(Long id) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        return user;
    }

    public Optional<User> findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    /**
     * Update profile fields: firstName, lastName, bio.
     * Only the user themselves can do this (enforced in backing bean via session).
     */
    
    public User updateProfile(Long userId, String firstName, String lastName, String bio) {
        User user = findById(userId);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setBio(bio);
        userDao.update(user);
        return user;
    }

    /**
     * Ban or unban a user. Only app admins can call this.
     */
    
    public void setBanned(Long requesterId, Long targetUserId, boolean banned) {
        User requester = findById(requesterId);
        if (requester.getRole() != Role.ADMIN) {
            throw new SecurityException("Only admins can ban or unban users.");
        }

        User target = findById(targetUserId);
        target.setIsBanned(banned);
        userDao.update(target);
    }
}
