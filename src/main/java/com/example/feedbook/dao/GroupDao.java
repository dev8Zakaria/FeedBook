package com.example.feedbook.dao;

import com.example.feedbook.entity.Group;
import com.example.feedbook.entity.GroupType;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class GroupDao extends GenericDao<Group, Long> {

    public GroupDao() {
        super(Group.class);
    }

    public List<Group> findPublicGroups() {
        return em.createQuery("FROM Group g WHERE g.type = :type ORDER BY g.createdAt DESC", Group.class)
                .setParameter("type", GroupType.PUBLIC)
                .getResultList();
    }
}
