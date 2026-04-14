package com.example.feedbook.dao;

import com.example.feedbook.entity.Follow;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class FollowDao extends GenericDao<Follow, Long> {

    public FollowDao() {
        super(Follow.class);
    }

    public List<Follow> getFollowers(Long userId) {
        return em.createQuery("FROM Follow f WHERE f.followed.id = :userId ORDER BY f.createdAt DESC", Follow.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<Follow> getFollowing(Long userId) {
        return em.createQuery("FROM Follow f WHERE f.follower.id = :userId ORDER BY f.createdAt DESC", Follow.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public Optional<Follow> findFollow(Long followerId, Long followedId) {
        try {
            Follow follow = em.createQuery("FROM Follow f WHERE f.follower.id = :followerId AND f.followed.id = :followedId", Follow.class)
                    .setParameter("followerId", followerId)
                    .setParameter("followedId", followedId)
                    .getSingleResult();
            return Optional.of(follow);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
