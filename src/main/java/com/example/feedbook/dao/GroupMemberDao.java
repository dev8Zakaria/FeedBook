package com.example.feedbook.dao;

import com.example.feedbook.entity.GroupMember;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class GroupMemberDao extends GenericDao<GroupMember, Long> {

    public GroupMemberDao() {
        super(GroupMember.class);
    }

    public List<GroupMember> findByGroupId(Long groupId) {
        return em.createQuery("FROM GroupMember gm WHERE gm.group.id = :groupId ORDER BY gm.joinedAt DESC", GroupMember.class)
                .setParameter("groupId", groupId)
                .getResultList();
    }

    public Optional<GroupMember> findMember(Long groupId, Long userId) {
        try {
            GroupMember member = em.createQuery("FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.user.id = :userId", GroupMember.class)
                    .setParameter("groupId", groupId)
                    .setParameter("userId", userId)
                    .getSingleResult();
            return Optional.of(member);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
