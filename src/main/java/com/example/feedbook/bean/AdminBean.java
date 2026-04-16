package com.example.feedbook.bean;

import com.example.feedbook.entity.Post;
import com.example.feedbook.entity.Role;
import com.example.feedbook.entity.User;
import com.example.feedbook.service.PostService;
import com.example.feedbook.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;

@Named
@RequestScoped
public class AdminBean {

    @Inject
    private UserService userService;

    @Inject
    private PostService postService;

    @Inject
    private AuthBean authBean;

    private List<User> allUsers;
    private List<Post> allPosts;

    @PostConstruct
    public void init() {
        allUsers = userService.findAll();
        allPosts = postService.findAll();
    }

    public void checkAdmin() {
        if (!authBean.isLoggedIn() || authBean.getCurrentUser().getRole() != Role.ADMIN) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/index.xhtml");
            } catch (Exception e) {}
        }
    }

    // ------------------------------------------------------------------ Users

    public String banUser(Long targetUserId) {
        try {
            userService.setBanned(authBean.getCurrentUser().getId(), targetUserId, true);
            addInfo("User banned successfully.");
        } catch (Exception e) {
            addError(e.getMessage());
        }
        return null; // stay on same page, @RequestScoped re-loads via @PostConstruct
    }

    public String unbanUser(Long targetUserId) {
        try {
            userService.setBanned(authBean.getCurrentUser().getId(), targetUserId, false);
            addInfo("User unbanned successfully.");
        } catch (Exception e) {
            addError(e.getMessage());
        }
        return null;
    }

    // ------------------------------------------------------------------ Posts

    public String deletePost(Long postId) {
        try {
            postService.deletePost(authBean.getCurrentUser().getId(), postId);
            addInfo("Post deleted.");
        } catch (Exception e) {
            addError(e.getMessage());
        }
        return null;
    }

    // ---------------------------------------------------------------- Helpers

    public boolean isCurrentUser(Long userId) {
        return authBean.getCurrentUser() != null
                && authBean.getCurrentUser().getId().equals(userId);
    }

    private void addInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", msg));
    }

    // -------------------------------------------------------------- Accessors

    public List<User> getAllUsers() { return allUsers; }
    public List<Post> getAllPosts() { return allPosts; }
}