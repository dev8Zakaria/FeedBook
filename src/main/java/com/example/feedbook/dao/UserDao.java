package com.example.feedbook.dao;

import com.example.feedbook.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;

import java.util.Optional;

@ApplicationScoped
public class UserDao extends GenericDao<User, Long> {

    public UserDao() {
        super(User.class);
    }

    public Optional<User> findByEmail(String email) {
        try {
            User user = em.createQuery("FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByUsername(String username) {
        try {
            User user = em.createQuery("FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
