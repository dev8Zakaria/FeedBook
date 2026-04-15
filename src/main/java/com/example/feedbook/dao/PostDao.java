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

    // Global feed: PUBLIC posts outside any group, newest first
    public List<Post> getGlobalFeed() {
        return em.createQuery(
                        "FROM Post p WHERE p.visibility = :vis AND p.group IS NULL ORDER BY p.createdAt DESC",
                        Post.class)
                .setParameter("vis", PostVisibility.PUBLIC)
                .getResultList();
    }

    // Follow feed: posts from users the given user follows
    public List<Post> getFollowFeed(Long userId) {
        return em.createQuery(
                        "SELECT p FROM Post p " +
                                "WHERE p.author IN (" +
                                "  SELECT f.followed FROM Follow f WHERE f.follower.id = :userId" +
                                ") ORDER BY p.createdAt DESC",
                        Post.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<Post> getGroupPosts(Long groupId) {
        return em.createQuery(
                        "FROM Post p WHERE p.group.id = :groupId ORDER BY p.createdAt DESC",
                        Post.class)
                .setParameter("groupId", groupId)
                .getResultList();
    }

    public List<Post> findByAuthorId(Long authorId) {
        return em.createQuery(
                        "FROM Post p WHERE p.author.id = :authorId ORDER BY p.createdAt DESC",
                        Post.class)
                .setParameter("authorId", authorId)
                .getResultList();
    }
}
