package com.example.feedbook.dao;

import com.example.feedbook.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class DaoIntegrationTest {

    private static EntityManagerFactory emf;
    private static EntityManager em;

    @BeforeAll
    public static void setup() {
        // Boot up Hibernate 
        emf = Persistence.createEntityManagerFactory("feedbookPU");
        em = emf.createEntityManager();

        // Wipe old test data to prevent unique constraint failures
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Comment").executeUpdate();
        em.createQuery("DELETE FROM Follow").executeUpdate();
        em.createQuery("DELETE FROM GroupMember").executeUpdate();
        em.createQuery("DELETE FROM Post").executeUpdate();
        em.createQuery("DELETE FROM Group").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.getTransaction().commit();
    }

    @AfterAll
    public static void tearDown() {
        if (em != null) em.close();
        if (emf != null) emf.close();
    }

    @Test
    public void testUserDao() {
        // 1. Instantiate the DAO manually since CDI/JSF isn't running in a basic JUnit test
        UserDao userDao = new UserDao();
        userDao.setEntityManager(em); // Manually injecting the EntityManager

        // 2. Start a transaction
        em.getTransaction().begin();

        // 3. Create a mock user
        User testUser = new User();
        testUser.setUsername("testuser123");
        testUser.setEmail("test123@feedbook.com");
        testUser.setPassword("secret123");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        
        userDao.save(testUser); // Testing the inherited `save` method from GenericDao
        
        // 4. Commit the transaction to flush to DB
        em.getTransaction().commit();

        // 5. Test the custom finder methods in UserDao
        Optional<User> foundByEmail = userDao.findByEmail("test123@feedbook.com");
        assertTrue(foundByEmail.isPresent(), "User should be found by email");
        assertEquals("testuser123", foundByEmail.get().getUsername(), "Match username");

        Optional<User> foundByUsername = userDao.findByUsername("testuser123");
        assertTrue(foundByUsername.isPresent(), "User should be found by username");

        Optional<User> notFound = userDao.findByEmail("nobody@feedbook.com");
        assertFalse(notFound.isPresent(), "Should automatically handle NoResultException correctly");
    }
}
