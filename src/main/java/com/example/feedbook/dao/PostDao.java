package com.example.feedbook.dao;

import com.example.feedbook.entity.Post;
import com.example.feedbook.entity.PostVisibility;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class PostDao extends GenericDao<Post, Long> {

    public PostDao() {
        super(Post.class);
    }

    public List<Post> getGlobalFeed() {
        return em.createQuery("FROM Post p WHERE p.visibility = :vis ORDER BY p.createdAt DESC", Post.class)
                .setParameter("vis", PostVisibility.PUBLIC)
                .getResultList();
    }

    public List<Post> getFollowFeed(Long userId) {
        return em.createQuery(
                "SELECT p FROM Post p " +
                "JOIN Follow f ON p.author = f.followed " +
                "WHERE f.follower.id = :userId " +
                "ORDER BY p.createdAt DESC", Post.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<Post> getGroupPosts(Long groupId) {
        return em.createQuery("FROM Post p WHERE p.group.id = :groupId ORDER BY p.createdAt DESC", Post.class)
                .setParameter("groupId", groupId)
                .getResultList();
    }
}
