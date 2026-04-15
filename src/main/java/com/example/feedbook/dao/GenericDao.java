package com.example.feedbook.dao;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

public abstract class GenericDao<T, ID extends Serializable> {

    @Inject
    public EntityManager em;

    protected Class<T> entityClass;

    public GenericDao() {
        // Default constructor required by proxying frameworks
    }

    public GenericDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public void save(T entity) {
        em.persist(entity);
    }

    public void update(T entity) {
        em.merge(entity);
    }

    public void delete(T entity) {
        em.remove(em.merge(entity));
    }

    public T findById(ID id) {
        return em.find(entityClass, id);
    }

    public List<T> findAll() {
        return em.createQuery("FROM " + entityClass.getSimpleName(), entityClass).getResultList();
    }
}
