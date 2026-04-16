package com.example.feedbook.bean;

import com.example.feedbook.entity.Follow;
import com.example.feedbook.entity.Post;
import com.example.feedbook.entity.User;
import com.example.feedbook.service.FollowService;
import com.example.feedbook.service.PostService;
import com.example.feedbook.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class ProfileBean implements Serializable {

    @Inject
    private UserService userService;

    @Inject
    private PostService postService;

    @Inject
    private FollowService followService;

    @Inject
    private AuthBean authBean;

    private Long userId; // from view param
    private User profileUser;
    
    private List<Post> userPosts;
    private List<Follow> followers;
    private List<Follow> following;

    // Edit fields
    private String firstName;
    private String lastName;
    private String bio;

    @PostConstruct
    public void init() {
    }

    public void loadProfile() {
        try {
            Long targetId = (userId != null) ? userId : authBean.getCurrentUser().getId();
            profileUser = userService.findById(targetId);
            
            // load stats/content
            userPosts = postService.getPostsByAuthor(targetId);
            followers = followService.getFollowers(targetId);
            following = followService.getFollowing(targetId);
            
            // initialize edit fields in case they navigate to edit
            firstName = profileUser.getFirstName();
            lastName = profileUser.getLastName();
            bio = profileUser.getBio();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Profile not found.", null));
        }
    }

    public boolean isMyProfile() {
        if (!authBean.isLoggedIn() || profileUser == null) return false;
        return authBean.getCurrentUser().getId().equals(profileUser.getId());
    }

    public boolean getIsFollowing() {
        if (!authBean.isLoggedIn() || profileUser == null || isMyProfile()) return false;
        return followService.isFollowing(authBean.getCurrentUser().getId(), profileUser.getId());
    }

    public void toggleFollow() {
        if (!authBean.isLoggedIn() || isMyProfile()) return;
        
        Long myId = authBean.getCurrentUser().getId();
        Long targetId = profileUser.getId();

        try {
            if (getIsFollowing()) {
                followService.unfollow(myId, targetId);
            } else {
                followService.follow(myId, targetId);
            }
            // Reload following stats
            followers = followService.getFollowers(targetId);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Could not update follow status.", null));
        }
    }

    public String saveProfile() {
        if (!isMyProfile()) return null;
        try {
            userService.updateProfile(authBean.getCurrentUser().getId(), firstName, lastName, bio);
            // update session user
            User updated = userService.findById(authBean.getCurrentUser().getId());
            authBean.setCurrentUser(updated);
            
            return "/profile/view.xhtml?userId=" + updated.getId() + "&faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error saving profile.", null));
            return null;
        }
    }

    // Getters / Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public User getProfileUser() { return profileUser; }
    
    public List<Post> getUserPosts() { return userPosts; }
    public List<Follow> getFollowers() { return followers; }
    public List<Follow> getFollowing() { return following; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
