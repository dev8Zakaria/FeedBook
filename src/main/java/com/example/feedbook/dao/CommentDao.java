package com.example.feedbook.dao;

import com.example.feedbook.entity.Comment;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class CommentDao extends GenericDao<Comment, Long> {

    public CommentDao() {
        super(Comment.class);
    }

    public List<Comment> findByPostId(Long postId) {
        return em.createQuery("FROM Comment c WHERE c.post.id = :postId ORDER BY c.createdAt ASC", Comment.class)
                .setParameter("postId", postId)
                .getResultList();
    }
}
