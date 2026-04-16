package com.example.feedbook.bean;

import com.example.feedbook.entity.Comment;
import com.example.feedbook.entity.Post;
import com.example.feedbook.entity.PostVisibility;
import com.example.feedbook.entity.Role;
import com.example.feedbook.service.CommentService;
import com.example.feedbook.service.PostService;
import com.example.feedbook.service.ImageService;
import jakarta.servlet.http.Part;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class PostBean implements Serializable {

    @Inject
    private PostService postService;

    @Inject
    private ImageService imageService;

    @Inject
    private CommentService commentService;

    @Inject
    private AuthBean authBean;

    // Feed
    private List<Post> globalFeed;

    // New post form
    private String newContent;
    private String newVisibility = "PUBLIC";
    private Part newImage;

    // View / edit
    private Long postId;
    private Post currentPost;
    private List<Comment> comments;

    // Edit form
    private String editContent;

    // New comment form
    private String newCommentContent;

    @PostConstruct
    public void init() {
        // postId is set via f:viewParam before this runs on view pages
    }

    // -------------------------------------------------------------------------
    // Feed
    // -------------------------------------------------------------------------

    public List<Post> getGlobalFeed() {
        if (globalFeed == null) {
            globalFeed = postService.getGlobalFeed();
        }
        return globalFeed;
    }

    // -------------------------------------------------------------------------
    // Create post
    // -------------------------------------------------------------------------

    public String doCreatePost() {
        try {
            String imageUrl = imageService != null ? imageService.saveImage(newImage) : null;
            PostVisibility vis = PostVisibility.valueOf(newVisibility);
            postService.createPost(authBean.getCurrentUser().getId(), newContent, vis, imageUrl);
            return "/index.xhtml?faces-redirect=true";
        } catch (Exception e) {
            addError("Could not create post: " + e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // View post + comments
    // -------------------------------------------------------------------------

    public void loadPost() {
        if (postId == null) return;
        try {
            currentPost = postService.findById(postId);
            comments = commentService.getCommentsForPost(postId);
        } catch (Exception e) {
            addError("Post not found.");
        }
    }

    // -------------------------------------------------------------------------
    // Edit post
    // -------------------------------------------------------------------------

    public void loadEditPost() {
        if (postId == null) return;
        try {
            currentPost = postService.findById(postId);
            editContent = currentPost.getContent();
        } catch (Exception e) {
            addError("Post not found.");
        }
    }

    public String doEditPost() {
        try {
            postService.editPost(authBean.getCurrentUser().getId(), postId, editContent);
            return "/post/view.xhtml?postId=" + postId + "&faces-redirect=true";
        } catch (SecurityException e) {
            addError("You can only edit your own posts.");
            return null;
        } catch (Exception e) {
            addError("Could not edit post: " + e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Delete post
    // -------------------------------------------------------------------------

    public String doDeletePost(Long id) {
        try {
            postService.deletePost(authBean.getCurrentUser().getId(), id);
            return "/index.xhtml?faces-redirect=true";
        } catch (SecurityException e) {
            addError("You do not have permission to delete this post.");
            return null;
        } catch (Exception e) {
            addError("Could not delete post: " + e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Comments
    // -------------------------------------------------------------------------

    public String doAddComment() {
        try {
            commentService.addComment(authBean.getCurrentUser().getId(), postId, newCommentContent);
            return "/post/view.xhtml?postId=" + postId + "&faces-redirect=true";
        } catch (Exception e) {
            addError("Could not add comment: " + e.getMessage());
            return null;
        }
    }

    public String doDeleteComment(Long commentId) {
        try {
            commentService.deleteComment(authBean.getCurrentUser().getId(), commentId);
            return "/post/view.xhtml?postId=" + postId + "&faces-redirect=true";
        } catch (SecurityException e) {
            addError("You do not have permission to delete this comment.");
            return null;
        } catch (Exception e) {
            addError("Could not delete comment: " + e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    public boolean canDelete(Post post) {
        if (!authBean.isLoggedIn()) return false;
        Long uid = authBean.getCurrentUser().getId();
        boolean isAuthor = post.getAuthor().getId().equals(uid);
        boolean isAdmin = authBean.getCurrentUser().getRole() == Role.ADMIN;
        return isAuthor || isAdmin;
    }

    public boolean canDeleteComment(Comment comment) {
        if (!authBean.isLoggedIn()) return false;
        Long uid = authBean.getCurrentUser().getId();
        boolean isAuthor = comment.getAuthor().getId().equals(uid);
        boolean isAdmin = authBean.getCurrentUser().getRole() == Role.ADMIN;
        return isAuthor || isAdmin;
    }

    public boolean canEdit(Post post) {
        if (!authBean.isLoggedIn()) return false;
        return post.getAuthor().getId().equals(authBean.getCurrentUser().getId());
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public String getNewContent() { return newContent; }
    public void setNewContent(String newContent) { this.newContent = newContent; }

    public String getNewVisibility() { return newVisibility; }
    public void setNewVisibility(String newVisibility) { this.newVisibility = newVisibility; }
    
    public Part getNewImage() { return newImage; }
    public void setNewImage(Part newImage) { this.newImage = newImage; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public Post getCurrentPost() { return currentPost; }

    public List<Comment> getComments() { return comments; }

    public String getEditContent() { return editContent; }
    public void setEditContent(String editContent) { this.editContent = editContent; }

    public String getNewCommentContent() { return newCommentContent; }
    public void setNewCommentContent(String newCommentContent) { this.newCommentContent = newCommentContent; }
}