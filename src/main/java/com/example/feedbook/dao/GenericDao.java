package com.example.feedbook.dao;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

public abstract class GenericDao<T, ID extends Serializable> {

    @Inject
    protected EntityManager em;

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    protected Class<T> entityClass;

    public GenericDao() {
        // Default constructor required by proxying frameworks
    }

    public GenericDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public void save(T entity) {
        boolean txStarted = false;
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
            txStarted = true;
        }
        try {
            em.persist(entity);
            if (txStarted) em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (txStarted && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public void update(T entity) {
        boolean txStarted = false;
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
            txStarted = true;
        }
        try {
            em.merge(entity);
            if (txStarted) em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (txStarted && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public void delete(T entity) {
        boolean txStarted = false;
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
            txStarted = true;
        }
        try {
            em.remove(em.merge(entity));
            if (txStarted) em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (txStarted && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public T findById(ID id) {
        return em.find(entityClass, id);
    }

    public List<T> findAll() {
        return em.createQuery("FROM " + entityClass.getSimpleName(), entityClass).getResultList();
    }
}
