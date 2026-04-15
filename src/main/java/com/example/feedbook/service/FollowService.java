package com.example.feedbook.service;

import com.example.feedbook.dao.FollowDao;
import com.example.feedbook.dao.UserDao;
import com.example.feedbook.entity.Follow;
import com.example.feedbook.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class FollowService {

    @Inject
     FollowDao followDao;

    @Inject
     UserDao userDao;

    /**
     * Follow another user.
     * Throws if already following, or if trying to follow yourself.
     */
    
    public Follow follow(Long followerId, Long followedId) {
        if (followerId.equals(followedId)) {
            throw new IllegalArgumentException("You cannot follow yourself.");
        }

        if (followDao.findFollow(followerId, followedId).isPresent()) {
            throw new IllegalStateException("You are already following this user.");
        }

        User follower = userDao.findById(followerId);
        User followed = userDao.findById(followedId);

        if (follower == null || followed == null) {
            throw new IllegalArgumentException("User not found.");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowed(followed);

        followDao.save(follow);
        return follow;
    }

    /**
     * Unfollow a user.
     */
    
    public void unfollow(Long followerId, Long followedId) {
        Follow follow = followDao.findFollow(followerId, followedId)
                .orElseThrow(() -> new IllegalStateException("You are not following this user."));

        followDao.delete(follow);
    }

    public boolean isFollowing(Long followerId, Long followedId) {
        return followDao.findFollow(followerId, followedId).isPresent();
    }

    public List<Follow> getFollowers(Long userId) {
        return followDao.getFollowers(userId);
    }

    public List<Follow> getFollowing(Long userId) {
        return followDao.getFollowing(userId);
    }
}
