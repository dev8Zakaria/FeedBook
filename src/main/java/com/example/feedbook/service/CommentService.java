package com.example.feedbook.service;

import com.example.feedbook.dao.CommentDao;
import com.example.feedbook.dao.PostDao;
import com.example.feedbook.dao.UserDao;
import com.example.feedbook.entity.Comment;
import com.example.feedbook.entity.Post;
import com.example.feedbook.entity.Role;
import com.example.feedbook.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class CommentService {

    @Inject
     CommentDao commentDao;

    @Inject
     PostDao postDao;

    @Inject
     UserDao userDao;

    /**
     * Add a comment to a post.
     */
    
    public Comment addComment(Long authorId, Long postId, String content) {
        User author = userDao.findById(authorId);
        if (author == null) throw new IllegalArgumentException("User not found.");

        Post post = postDao.findById(postId);
        if (post == null) throw new IllegalArgumentException("Post not found.");

        Comment comment = new Comment();
        comment.setAuthor(author);
        comment.setPost(post);
        comment.setContent(content);

        commentDao.save(comment);
        return comment;
    }

    /**
     * Delete a comment. Author or app admin can delete.
     */
    
    public void deleteComment(Long requesterId, Long commentId) {
        Comment comment = commentDao.findById(commentId);
        if (comment == null) throw new IllegalArgumentException("Comment not found.");

        User requester = userDao.findById(requesterId);

        boolean isAuthor = comment.getAuthor().getId().equals(requesterId);
        boolean isAdmin = requester != null && requester.getRole() == Role.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new SecurityException("You do not have permission to delete this comment.");
        }

        commentDao.delete(comment);
    }

    public List<Comment> getCommentsForPost(Long postId) {
        return commentDao.findByPostId(postId);
    }
}
