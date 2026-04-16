package com.example.feedbook.service;

import com.example.feedbook.dao.GroupDao;
import com.example.feedbook.dao.GroupMemberDao;
import com.example.feedbook.dao.UserDao;
import com.example.feedbook.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class GroupService {

    @Inject
     GroupDao groupDao;

    @Inject
     GroupMemberDao groupMemberDao;

    @Inject
     UserDao userDao;

    /**
     * Create a group. The creator is automatically added as ADMIN.
     */
    
    public Group createGroup(Long ownerId, String name, String description, GroupType type, String imageUrl) {
        User owner = userDao.findById(ownerId);
        if (owner == null) throw new IllegalArgumentException("User not found.");

        Group group = new Group();
        group.setName(name);
        group.setDescription(description);
        group.setType(type);
        group.setOwner(owner);
        group.setImageUrl(imageUrl);

        groupDao.save(group);

        // Auto-add creator as group ADMIN
        GroupMember membership = new GroupMember();
        membership.setGroup(group);
        membership.setUser(owner);
        membership.setRole(GroupRole.ADMIN);
        groupMemberDao.save(membership);

        return group;
    }

    /**
     * Join a group. Fails if already a member, or if group is PRIVATE
     * (private groups require an invite — extend later).
     */
    
    public GroupMember joinGroup(Long userId, Long groupId) {
        User user = userDao.findById(userId);
        Group group = groupDao.findById(groupId);

        if (user == null) throw new IllegalArgumentException("User not found.");
        if (group == null) throw new IllegalArgumentException("Group not found.");

        if (groupMemberDao.findMember(groupId, userId).isPresent()) {
            throw new IllegalStateException("You are already a member or have a pending request.");
        }

        GroupMember membership = new GroupMember();
        membership.setGroup(group);
        membership.setUser(user);
        membership.setRole(GroupRole.MEMBER);

        if (group.getType() == GroupType.PRIVATE) {
            membership.setStatus(MembershipStatus.PENDING);
        } else {
            membership.setStatus(MembershipStatus.APPROVED);
        }

        groupMemberDao.save(membership);
        return membership;
    }

    public void acceptRequest(Long requesterId, Long groupId, Long targetUserId) {
        assertGroupAdmin(requesterId, groupId);
        GroupMember target = groupMemberDao.findMember(groupId, targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found."));
        if (target.getStatus() == MembershipStatus.PENDING) {
            target.setStatus(MembershipStatus.APPROVED);
            groupMemberDao.update(target);
        }
    }

    public void refuseRequest(Long requesterId, Long groupId, Long targetUserId) {
        assertGroupAdmin(requesterId, groupId);
        GroupMember target = groupMemberDao.findMember(groupId, targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found."));
        if (target.getStatus() == MembershipStatus.PENDING) {
            groupMemberDao.delete(target);
        }
    }

    /**
     * Leave a group. The group owner cannot leave (they must transfer or delete).
     */
    
    public void leaveGroup(Long userId, Long groupId) {
        Group group = groupDao.findById(groupId);
        if (group == null) throw new IllegalArgumentException("Group not found.");

        if (group.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("The group owner cannot leave the group.");
        }

        GroupMember membership = groupMemberDao.findMember(groupId, userId)
                .orElseThrow(() -> new IllegalStateException("You are not a member of this group."));

        groupMemberDao.delete(membership);
    }

    /**
     * Promote a member to ADMIN. Only group admins can do this.
     */
    
    public void promoteMember(Long requesterId, Long groupId, Long targetUserId) {
        assertGroupAdmin(requesterId, groupId);

        GroupMember target = groupMemberDao.findMember(groupId, targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Target user is not a member of this group."));

        target.setRole(GroupRole.ADMIN);
        groupMemberDao.update(target);
    }

    /**
     * Kick a member from the group. Only group admins can do this.
     * Admins cannot kick other admins (only the owner can manage admins).
     */
    
    public void kickMember(Long requesterId, Long groupId, Long targetUserId) {
        assertGroupAdmin(requesterId, groupId);

        GroupMember target = groupMemberDao.findMember(groupId, targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Target user is not a member of this group."));

        if (target.getRole() == GroupRole.ADMIN) {
            throw new SecurityException("Admins cannot kick other admins.");
        }

        groupMemberDao.delete(target);
    }

    public Group findById(Long groupId) {
        Group group = groupDao.findById(groupId);
        if (group == null) throw new IllegalArgumentException("Group not found: " + groupId);
        return group;
    }

    public List<Group> getAllGroups() {
        return groupDao.findAllGroups();
    }

    public List<Group> getMyAdminGroups(Long userId) {
        return groupDao.findMyAdminGroups(userId);
    }

    public List<GroupMember> getMembers(Long groupId) {
        return groupMemberDao.findByGroupId(groupId);
    }

    public List<GroupMember> getPendingMembers(Long groupId) {
        return groupMemberDao.findPendingMembers(groupId);
    }

    public boolean isMember(Long userId, Long groupId) {
        return groupMemberDao.findMember(groupId, userId)
                .map(m -> m.getStatus() == MembershipStatus.APPROVED)
                .orElse(false);
    }

    public boolean isPending(Long userId, Long groupId) {
        return groupMemberDao.findMember(groupId, userId)
                .map(m -> m.getStatus() == MembershipStatus.PENDING)
                .orElse(false);
    }

    public boolean isGroupAdmin(Long userId, Long groupId) {
        return groupMemberDao.findMember(groupId, userId)
                .map(m -> m.getRole() == GroupRole.ADMIN && m.getStatus() == MembershipStatus.APPROVED)
                .orElse(false);
    }

    // --- Private helpers ---

    private void assertGroupAdmin(Long userId, Long groupId) {
        GroupMember requester = groupMemberDao.findMember(groupId, userId)
                .orElseThrow(() -> new SecurityException("You are not a member of this group."));

        if (requester.getRole() != GroupRole.ADMIN) {
            throw new SecurityException("Only group admins can perform this action.");
        }
    }
}
