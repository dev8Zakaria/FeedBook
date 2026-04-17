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

    public List<Group> findAllGroups() {
        return em.createQuery("FROM Group g ORDER BY g.createdAt DESC", Group.class)
                .getResultList();
    }

    public List<Group> findMyAdminGroups(Long userId) {
        return em.createQuery(
                "SELECT gm.group FROM GroupMember gm WHERE gm.user.id = :userId AND gm.role = 'ADMIN' AND gm.status = 'APPROVED' ORDER BY gm.group.createdAt DESC", 
                Group.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
