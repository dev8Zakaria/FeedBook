package com.example.feedbook.service;

import com.example.feedbook.dao.*;
import com.example.feedbook.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for all service classes.
 * Runs against a real PostgreSQL DB using the feedbookPU-test persistence unit.
 * CDI is not available here — DAOs and services are wired manually,
 * mirroring the pattern established in DaoIntegrationTest.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceIntegrationTest {

    private static EntityManagerFactory emf;
    private static EntityManager em;

    // DAOs
    private static UserDao userDao;
    private static PostDao postDao;
    private static CommentDao commentDao;
    private static FollowDao followDao;
    private static GroupDao groupDao;
    private static GroupMemberDao groupMemberDao;

    // Services
    private static AuthService authService;
    private static UserService userService;
    private static PostService postService;
    private static CommentService commentService;
    private static FollowService followService;
    private static GroupService groupService;

    // Shared state across tests (ordered execution)
    private static Long userId;
    private static Long adminId;
    private static Long otherUserId;
    private static Long postId;
    private static Long groupPostId;
    private static Long commentId;
    private static Long groupId;

    @BeforeAll
    public static void setup() {
        emf = Persistence.createEntityManagerFactory("feedbookPU");
        em = emf.createEntityManager();

        // Wipe old test data from the database using JPQL, respecting foreign-key hierarchy
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Comment").executeUpdate();
        em.createQuery("DELETE FROM Follow").executeUpdate();
        em.createQuery("DELETE FROM GroupMember").executeUpdate();
        em.createQuery("DELETE FROM Post").executeUpdate();
        em.createQuery("DELETE FROM Group").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.getTransaction().commit();

        // Wire DAOs
        userDao = new UserDao();
        userDao.setEntityManager(em);
        postDao = new PostDao();
        postDao.setEntityManager(em);
        commentDao = new CommentDao();
        commentDao.setEntityManager(em);
        followDao = new FollowDao();
        followDao.setEntityManager(em);
        groupDao = new GroupDao();
        groupDao.setEntityManager(em);
        groupMemberDao = new GroupMemberDao();
        groupMemberDao.setEntityManager(em);

        // Wire services
        authService = new AuthService();
        authService.userDao = userDao;

        userService = new UserService();
        userService.userDao = userDao;

        postService = new PostService();
        postService.postDao = postDao;
        postService.userDao = userDao;
        postService.groupDao = groupDao;
        postService.groupMemberDao = groupMemberDao;

        commentService = new CommentService();
        commentService.commentDao = commentDao;
        commentService.postDao = postDao;
        commentService.userDao = userDao;

        followService = new FollowService();
        followService.followDao = followDao;
        followService.userDao = userDao;

        groupService = new GroupService();
        groupService.groupDao = groupDao;
        groupService.groupMemberDao = groupMemberDao;
        groupService.userDao = userDao;
    }

    @AfterAll
    public static void tearDown() {
        if (em != null) em.close();
        if (emf != null) emf.close();
    }

    // -------------------------------------------------------------------------
    // AuthService
    // -------------------------------------------------------------------------

    @Test
    @Order(1)
    public void testRegister() {
        em.getTransaction().begin();
        User user = authService.register("serviceuser", "serviceuser@feedbook.com", "pass123");
        em.getTransaction().commit();

        assertNotNull(user.getId());
        assertNotNull(user.getPassword());
        assertNotEquals("pass123", user.getPassword(), "Password should be hashed");
        userId = user.getId();
        System.out.println("✅ register: user id=" + userId);
    }

    @Test
    @Order(2)
    public void testRegisterDuplicateUsername() {
        em.getTransaction().begin();
        assertThrows(IllegalArgumentException.class,
                () -> authService.register("serviceuser", "other@feedbook.com", "pass"));
        em.getTransaction().rollback();
        System.out.println("✅ register: duplicate username rejected");
    }

    @Test
    @Order(3)
    public void testLogin() {
        User user = authService.login("serviceuser@feedbook.com", "pass123");
        assertNotNull(user);
        assertEquals("serviceuser", user.getUsername());
        System.out.println("✅ login: success");
    }

    @Test
    @Order(4)
    public void testLoginWrongPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.login("serviceuser@feedbook.com", "wrongpass"));
        System.out.println("✅ login: wrong password rejected");
    }

    // -------------------------------------------------------------------------
    // UserService
    // -------------------------------------------------------------------------

    @Test
    @Order(5)
    public void testUpdateProfile() {
        em.getTransaction().begin();
        User updated = userService.updateProfile(userId, "John", "Doe", "Hello world", null);
        em.getTransaction().commit();

        assertEquals("John", updated.getFirstName());
        assertEquals("Doe", updated.getLastName());
        assertEquals("Hello world", updated.getBio());
        System.out.println("✅ updateProfile: success");
    }

    @Test
    @Order(6)
    public void testBanRequiresAdmin() {
        // Register a second regular user to attempt banning
        em.getTransaction().begin();
        User other = authService.register("otheruser", "other@feedbook.com", "pass456");
        em.getTransaction().commit();
        otherUserId = other.getId();

        // Regular user trying to ban → should fail
        assertThrows(SecurityException.class,
                () -> userService.setBanned(userId, otherUserId, true));
        System.out.println("✅ ban: non-admin correctly rejected");
    }

    @Test
    @Order(7)
    public void testAdminCanBan() {
        // Register an admin user directly (bypass authService to set role)
        em.getTransaction().begin();
        User admin = new User();
        admin.setUsername("adminuser");
        admin.setEmail("admin@feedbook.com");
        admin.setPassword(authService.hashPassword("adminpass"));
        admin.setRole(Role.ADMIN);
        userDao.save(admin);
        em.getTransaction().commit();
        adminId = admin.getId();

        em.getTransaction().begin();
        userService.setBanned(adminId, otherUserId, true);
        em.getTransaction().commit();

        User banned = userDao.findById(otherUserId);
        assertTrue(banned.getIsBanned());
        System.out.println("✅ ban: admin successfully banned user");
    }

    @Test
    @Order(8)
    public void testBannedUserCannotLogin() {
        assertThrows(IllegalStateException.class,
                () -> authService.login("other@feedbook.com", "pass456"));
        System.out.println("✅ login: banned user correctly blocked");
    }

    // -------------------------------------------------------------------------
    // PostService
    // -------------------------------------------------------------------------

    @Test
    @Order(9)
    public void testCreatePost() {
        em.getTransaction().begin();
        Post post = postService.createPost(userId, "Hello FeedBook!", PostVisibility.PUBLIC, null);
        em.getTransaction().commit();

        assertNotNull(post.getId());
        assertEquals(PostVisibility.PUBLIC, post.getVisibility());
        assertNull(post.getGroup(), "Standalone post should have no group");
        postId = post.getId();
        System.out.println("✅ createPost: id=" + postId);
    }

    @Test
    @Order(10)
    public void testGlobalFeedContainsPost() {
        List<Post> feed = postService.getGlobalFeed();
        assertTrue(feed.stream().anyMatch(p -> p.getId().equals(postId)));
        System.out.println("✅ getGlobalFeed: post visible");
    }

    @Test
    @Order(11)
    public void testEditPost() {
        em.getTransaction().begin();
        Post edited = postService.editPost(userId, postId, "Updated content");
        em.getTransaction().commit();

        assertEquals("Updated content", edited.getContent());
        System.out.println("✅ editPost: success");
    }

    @Test
    @Order(12)
    public void testEditPostByNonAuthorFails() {
        assertThrows(SecurityException.class,
                () -> postService.editPost(adminId, postId, "Hacked"));
        System.out.println("✅ editPost: non-author correctly rejected");
    }

    // -------------------------------------------------------------------------
    // CommentService
    // -------------------------------------------------------------------------

    @Test
    @Order(13)
    public void testAddComment() {
        em.getTransaction().begin();
        Comment comment = commentService.addComment(userId, postId, "Nice post!");
        em.getTransaction().commit();

        assertNotNull(comment.getId());
        assertEquals("Nice post!", comment.getContent());
        commentId = comment.getId();
        System.out.println("✅ addComment: id=" + commentId);
    }

    @Test
    @Order(14)
    public void testGetCommentsForPost() {
        List<Comment> comments = commentService.getCommentsForPost(postId);
        assertFalse(comments.isEmpty());
        assertEquals(commentId, comments.get(0).getId());
        System.out.println("✅ getCommentsForPost: found " + comments.size() + " comment(s)");
    }

    @AfterEach
    public void cleanupTransaction() {
        // Prevent a failing test from leaving a transaction open and causing domino-effect failures
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }

    @Test
    @Order(15)
    public void testDeleteCommentByNonAuthorFails() {
        assertThrows(SecurityException.class,
                // otherUserId is a regular user, adminId is an admin (who is allowed to delete)
                () -> commentService.deleteComment(otherUserId, commentId));
        System.out.println("✅ deleteComment: non-author correctly rejected");
    }

    @Test
    @Order(16)
    public void testAdminCanDeleteComment() {
        // Admin deletes the comment
        em.getTransaction().begin();

        // First promote adminId to ADMIN role (already done in Order 7)
        commentService.deleteComment(adminId, commentId);
        em.getTransaction().commit();

        List<Comment> comments = commentService.getCommentsForPost(postId);
        assertTrue(comments.stream().noneMatch(c -> c.getId().equals(commentId)));
        System.out.println("✅ deleteComment: admin deleted comment");
    }

    // -------------------------------------------------------------------------
    // FollowService
    // -------------------------------------------------------------------------

    @Test
    @Order(17)
    public void testFollow() {
        em.getTransaction().begin();
        // Unban otherUser first so we can use them
        userService.setBanned(adminId, otherUserId, false);
        em.getTransaction().commit();

        em.getTransaction().begin();
        Follow follow = followService.follow(userId, adminId);
        em.getTransaction().commit();

        assertNotNull(follow.getId());
        assertTrue(followService.isFollowing(userId, adminId));
        System.out.println("✅ follow: success");
    }

    @Test
    @Order(18)
    public void testFollowSelfFails() {
        assertThrows(IllegalArgumentException.class,
                () -> followService.follow(userId, userId));
        System.out.println("✅ follow: self-follow correctly rejected");
    }

    @Test
    @Order(19)
    public void testFollowDuplicateFails() {
        assertThrows(IllegalStateException.class,
                () -> followService.follow(userId, adminId));
        System.out.println("✅ follow: duplicate follow correctly rejected");
    }

    @Test
    @Order(20)
    public void testFollowFeedContainsPost() {
        // userId follows adminId; create a post as adminId
        em.getTransaction().begin();
        Post adminPost = postService.createPost(adminId, "Admin's post", PostVisibility.PUBLIC, null);
        em.getTransaction().commit();

        List<Post> feed = postService.getFollowFeed(userId);
        assertTrue(feed.stream().anyMatch(p -> p.getAuthor().getId().equals(adminId)));
        System.out.println("✅ getFollowFeed: followed user's post appears");
    }

    @Test
    @Order(21)
    public void testUnfollow() {
        em.getTransaction().begin();
        followService.unfollow(userId, adminId);
        em.getTransaction().commit();

        assertFalse(followService.isFollowing(userId, adminId));
        System.out.println("✅ unfollow: success");
    }

    // -------------------------------------------------------------------------
    // GroupService
    // -------------------------------------------------------------------------

    @Test
    @Order(22)
    public void testCreateGroup() {
        em.getTransaction().begin();
        Group group = groupService.createGroup(userId, "Test Group", "A test group", GroupType.PUBLIC);
        em.getTransaction().commit();

        assertNotNull(group.getId());
        assertTrue(groupService.isMember(userId, group.getId()), "Creator should be auto-added as member");
        assertTrue(groupService.isGroupAdmin(userId, group.getId()), "Creator should be group admin");
        groupId = group.getId();
        System.out.println("✅ createGroup: id=" + groupId);
    }

    @Test
    @Order(23)
    public void testJoinGroup() {
        em.getTransaction().begin();
        groupService.joinGroup(adminId, groupId);
        em.getTransaction().commit();

        assertTrue(groupService.isMember(adminId, groupId));
        System.out.println("✅ joinGroup: success");
    }

    @Test
    @Order(24)
    public void testJoinGroupTwiceFails() {
        assertThrows(IllegalStateException.class,
                () -> groupService.joinGroup(userId, groupId));
        System.out.println("✅ joinGroup: duplicate join correctly rejected");
    }

    @Test
    @Order(25)
    public void testCreateGroupPost() {
        em.getTransaction().begin();
        Post post = postService.createGroupPost(userId, groupId, "Post inside group", null);
        em.getTransaction().commit();

        assertNotNull(post.getId());
        assertEquals(PostVisibility.GROUP, post.getVisibility());
        assertEquals(groupId, post.getGroup().getId());
        groupPostId = post.getId();
        System.out.println("✅ createGroupPost: id=" + groupPostId);
    }

    @Test
    @Order(26)
    public void testGroupPostNotInGlobalFeed() {
        List<Post> feed = postService.getGlobalFeed();
        assertTrue(feed.stream().noneMatch(p -> p.getId().equals(groupPostId)),
                "Group posts should not appear in global feed");
        System.out.println("✅ global feed: group post correctly excluded");
    }

    @Test
    @Order(27)
    public void testNonMemberCannotPostInGroup() {
        assertThrows(SecurityException.class,
                () -> postService.createGroupPost(otherUserId, groupId, "Sneaky post", null));
        System.out.println("✅ createGroupPost: non-member correctly rejected");
    }

    @Test
    @Order(28)
    public void testPromoteMember() {
        em.getTransaction().begin();
        groupService.promoteMember(userId, groupId, adminId);
        em.getTransaction().commit();

        assertTrue(groupService.isGroupAdmin(adminId, groupId));
        System.out.println("✅ promoteMember: success");
    }

    @Test
    @Order(29)
    public void testKickMember() {
        // Add otherUser to group first
        em.getTransaction().begin();
        groupService.joinGroup(otherUserId, groupId);
        em.getTransaction().commit();

        em.getTransaction().begin();
        groupService.kickMember(userId, groupId, otherUserId);
        em.getTransaction().commit();

        assertFalse(groupService.isMember(otherUserId, groupId));
        System.out.println("✅ kickMember: success");
    }

    @Test
    @Order(30)
    public void testOwnerCannotLeaveGroup() {
        assertThrows(IllegalStateException.class,
                () -> groupService.leaveGroup(userId, groupId));
        System.out.println("✅ leaveGroup: owner correctly prevented from leaving");
    }

    @Test
    @Order(31)
    public void testLeaveGroup() {
        em.getTransaction().begin();
        groupService.leaveGroup(adminId, groupId);
        em.getTransaction().commit();

        assertFalse(groupService.isMember(adminId, groupId));
        System.out.println("✅ leaveGroup: success");
    }

    // -------------------------------------------------------------------------
    // Cleanup — delete the test post last since comments cascade from it
    // -------------------------------------------------------------------------

    @Test
    @Order(32)
    public void testAdminDeletesAnyPost() {
        em.getTransaction().begin();
        postService.deletePost(adminId, postId);
        em.getTransaction().commit();

        assertThrows(IllegalArgumentException.class,
                () -> postService.findById(postId));
        System.out.println("✅ deletePost: admin deleted another user's post");
    }
}
