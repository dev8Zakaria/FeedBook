package com.example.feedbook.config; // or test


import com.example.feedbook.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class TestJPA {

    public static void main(String[] args) {

        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("feedbookPU");

        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        User user = new User();
        user.setUsername("zakaria");
        user.setEmail("zakaria@test.com");
        user.setPassword("1234");
        user.setRole(Role.USER);

        em.persist(user);

        em.getTransaction().commit();

        em.close();
        emf.close();

        System.out.println("✅ Test successful");
    }
}