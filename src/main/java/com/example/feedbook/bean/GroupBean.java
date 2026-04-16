package com.example.feedbook.bean;

import com.example.feedbook.entity.Group;
import com.example.feedbook.entity.GroupMember;
import com.example.feedbook.entity.GroupType;
import com.example.feedbook.entity.Post;
import com.example.feedbook.service.GroupService;
import com.example.feedbook.service.PostService;
import com.example.feedbook.service.ImageService;
import jakarta.servlet.http.Part;
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
public class GroupBean implements Serializable {

    @Inject
    private GroupService groupService;
    
    @Inject
    private PostService postService;

    @Inject
    private AuthBean authBean;

    @Inject
    private ImageService imageService;

    // List
    private List<Group> allGroups;
    private List<Group> myGroups;
    private boolean showMyGroups = false;

    // View
    private Long groupId;
    private Group currentGroup;
    private List<Post> groupPosts;
    private List<GroupMember> members;
    private List<GroupMember> pendingMembers;

    // New
    private String newGroupName;
    private String newGroupDescription;
    private String newGroupType = "PUBLIC";
    private Part newGroupImage;

    @PostConstruct
    public void init() {}

    public void loadGroups() {
        allGroups = groupService.getAllGroups();
        if (authBean.isLoggedIn()) {
            myGroups = groupService.getMyAdminGroups(authBean.getCurrentUser().getId());
        }
    }

    public void toggleMyGroups() {
        showMyGroups = !showMyGroups;
    }

    public void loadGroup() {
        if (groupId != null) {
            try {
                currentGroup = groupService.findById(groupId);
                
                // Security check
                if (currentGroup.getType() == GroupType.PRIVATE && !isMember() && !isAdmin()) {
                    FacesContext.getCurrentInstance().getExternalContext().redirect(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/groups/list.xhtml");
                    return;
                }
                
                groupPosts = postService.getGroupPosts(groupId);
                members = groupService.getMembers(groupId);
                if (isAdmin()) {
                    pendingMembers = groupService.getPendingMembers(groupId);
                }
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Group not found or access denied.", null));
            }
        }
    }

    public boolean isMember() {
        if (!authBean.isLoggedIn() || currentGroup == null) return false;
        return groupService.isMember(authBean.getCurrentUser().getId(), currentGroup.getId());
    }

    public boolean isPending() {
        if (!authBean.isLoggedIn() || currentGroup == null) return false;
        return groupService.isPending(authBean.getCurrentUser().getId(), currentGroup.getId());
    }

    public boolean isAdmin() {
        if (!authBean.isLoggedIn() || currentGroup == null) return false;
        return groupService.isGroupAdmin(authBean.getCurrentUser().getId(), currentGroup.getId());
    }

    public String joinGroup(Long targetGroupId) {
        if (!authBean.isLoggedIn()) return null;
        try {
            groupService.joinGroup(authBean.getCurrentUser().getId(), targetGroupId);
            return "/groups/view.xhtml?groupId=" + targetGroupId + "&faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
            return null;
        }
    }

    public void requestJoinContext(Long targetGroupId) {
        if (!authBean.isLoggedIn()) return;
        try {
            groupService.joinGroup(authBean.getCurrentUser().getId(), targetGroupId);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Join request sent.", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
    }

    public void acceptRequest(Long targetUserId) {
        if (!isAdmin()) return;
        try {
            groupService.acceptRequest(authBean.getCurrentUser().getId(), currentGroup.getId(), targetUserId);
            // reload
            members = groupService.getMembers(currentGroup.getId());
            pendingMembers = groupService.getPendingMembers(currentGroup.getId());
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
    }

    public void refuseRequest(Long targetUserId) {
        if (!isAdmin()) return;
        try {
            groupService.refuseRequest(authBean.getCurrentUser().getId(), currentGroup.getId(), targetUserId);
            // reload
            pendingMembers = groupService.getPendingMembers(currentGroup.getId());
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
    }

    public String leaveGroup() {
        if (!authBean.isLoggedIn() || currentGroup == null) return null;
        try {
            groupService.leaveGroup(authBean.getCurrentUser().getId(), currentGroup.getId());
            return "/groups/list.xhtml?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
            return null;
        }
    }

    public String createGroup() {
        if (!authBean.isLoggedIn()) return null;
        try {
            String imageUrl = imageService.saveImage(newGroupImage);
            GroupType type = GroupType.valueOf(newGroupType);
            Group group = groupService.createGroup(authBean.getCurrentUser().getId(), newGroupName, newGroupDescription, type, imageUrl);
            return "/groups/view.xhtml?groupId=" + group.getId() + "&faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error creating group: " + e.getMessage(), null));
            return null;
        }
    }

    public void promoteMember(Long memberUserId) {
        if (!isAdmin()) return;
        try {
            groupService.promoteMember(authBean.getCurrentUser().getId(), currentGroup.getId(), memberUserId);
            members = groupService.getMembers(currentGroup.getId()); // reload
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
    }

    public void kickMember(Long memberUserId) {
        if (!isAdmin()) return;
        try {
            groupService.kickMember(authBean.getCurrentUser().getId(), currentGroup.getId(), memberUserId);
            members = groupService.getMembers(currentGroup.getId()); // reload
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
    }

    // Getters and Setters
    public List<Group> getAllGroups() { return allGroups; }
    public List<Group> getMyGroups() { return myGroups; }
    public boolean isShowMyGroups() { return showMyGroups; }
    public List<GroupMember> getPendingMembers() { return pendingMembers; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public Group getCurrentGroup() { return currentGroup; }
    public List<Post> getGroupPosts() { return groupPosts; }
    public List<GroupMember> getMembers() { return members; }

    public String getNewGroupName() { return newGroupName; }
    public void setNewGroupName(String newGroupName) { this.newGroupName = newGroupName; }
    public String getNewGroupDescription() { return newGroupDescription; }
    public void setNewGroupDescription(String newGroupDescription) { this.newGroupDescription = newGroupDescription; }
    public String getNewGroupType() { return newGroupType; }
    public void setNewGroupType(String newGroupType) { this.newGroupType = newGroupType; }
    public Part getNewGroupImage() { return newGroupImage; }
    public void setNewGroupImage(Part newGroupImage) { this.newGroupImage = newGroupImage; }
}
