package com.example.feedbook.service;

import com.example.feedbook.dao.GroupDao;
import com.example.feedbook.dao.GroupMemberDao;
import com.example.feedbook.dao.PostDao;
import com.example.feedbook.dao.UserDao;
import com.example.feedbook.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class PostService {

    @Inject
     PostDao postDao;

    @Inject
     UserDao userDao;

    @Inject
     GroupDao groupDao;

    @Inject
     GroupMemberDao groupMemberDao;

    /**
     * Create a standalone (non-group) post.
     */
    
    public Post createPost(Long authorId, String content, PostVisibility visibility, String imageUrl) {
        User author = userDao.findById(authorId);
        if (author == null) throw new IllegalArgumentException("User not found.");

        Post post = new Post();
        post.setAuthor(author);
        post.setContent(content);
        post.setVisibility(visibility);
        post.setImageUrl(imageUrl);

        postDao.save(post);
        return post;
    }

    /**
     * Create a post inside a group. User must be a member of the group.
     */
    
    public Post createGroupPost(Long authorId, Long groupId, String content) {
        // Convenience overload for callers that don't provide an image.
        return createGroupPost(authorId, groupId, content, null);
    }

    public Post createGroupPost(Long authorId, Long groupId, String content, String imageUrl) {
        User author = userDao.findById(authorId);
        if (author == null) throw new IllegalArgumentException("User not found.");

        Group group = groupDao.findById(groupId);
        if (group == null) throw new IllegalArgumentException("Group not found.");

        groupMemberDao.findMember(groupId, authorId)
                .orElseThrow(() -> new SecurityException("You must be a member of the group to post in it."));

        Post post = new Post();
        post.setAuthor(author);
        post.setContent(content);
        post.setVisibility(PostVisibility.GROUP);
        post.setGroup(group);
        post.setImageUrl(imageUrl);

        postDao.save(post);
        return post;
    }

    /**
     * Edit a post's content. Only the author can edit their own post.
     */
    
    public Post editPost(Long requesterId, Long postId, String newContent) {
        Post post = getPostOrThrow(postId);

        if (!post.getAuthor().getId().equals(requesterId)) {
            throw new SecurityException("You can only edit your own posts.");
        }

        post.setContent(newContent);
        postDao.update(post);
        return post;
    }

    /**
     * Delete a post. Author or app admin can delete.
     */
    
    public void deletePost(Long requesterId, Long postId) {
        Post post = getPostOrThrow(postId);
        User requester = userDao.findById(requesterId);

        boolean isAuthor = post.getAuthor().getId().equals(requesterId);
        boolean isAdmin = requester != null && requester.getRole() == Role.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new SecurityException("You do not have permission to delete this post.");
        }

        postDao.delete(post);
    }

    public Post findById(Long postId) {
        return getPostOrThrow(postId);
    }

    public List<Post> getGlobalFeed() {
        return postDao.getGlobalFeed();
    }

    public List<Post> getFollowFeed(Long userId) {
        return postDao.getFollowFeed(userId);
    }

    public List<Post> getGroupPosts(Long groupId) {
        return postDao.getGroupPosts(groupId);
    }

    public List<Post> getPostsByAuthor(Long authorId) {
        return postDao.findByAuthorId(authorId);
    }

    private Post getPostOrThrow(Long postId) {
        Post post = postDao.findById(postId);
        if (post == null) throw new IllegalArgumentException("Post not found: " + postId);
        return post;
    }
}
