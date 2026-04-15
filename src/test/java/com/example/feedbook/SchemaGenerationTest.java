package com.example.feedbook;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SchemaGenerationTest {

    @Test
    public void testSchemaGeneration() {
        // Booting up the persistence unit automatically triggers Hibernate
        // to connect to DB and create your tables due to 'update' property
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("feedbookPU-test");

        assertNotNull(emf, "EntityManagerFactory created successfully");
        
        System.out.println("✅ Schema generation successful! Tables should now be in your PostgreSQL database.");

        emf.close();
    }
}
