# FeedBook Project Plan

## Phase 0: Project setup & configuration
**~1 session**

* **Configure `pom.xml`** — Jakarta EE 11, Hibernate 6, JSF, PrimeFaces (optional) | `pom.xml`
* **Configure `persistence.xml`** — datasource, dialect, DDL auto | `persistence.xml`
* **Configure `web.xml`** (FacesServlet mapping) and `faces-config.xml` | `web.xml`
* **Set up `beans.xml`** with `bean-discovery-mode="all"` | `CDI`
* **Create `webapp/` folder structure** — `views/`, `WEB-INF/templates/`, `resources/` | `structure`

---

## Phase 1: Hibernate entities & database model
**~2 sessions**

* **`User` entity** — Role enum, isBanned, createdAt | `@Entity`
* **`Post` entity** — visibility enum, ManyToOne author + group | `@Entity`
* **`Comment` entity** — ManyToOne author + post | `@Entity`
* **`Group` entity** — type enum, owner FK | `@Entity`
* **`GroupMember` join entity** — role enum, joinedAt | `@Entity`
* **`Follow` join entity** — follower / followed self-join on User | `@Entity`
* **Verify schema generation** — check all tables created correctly | `test`

---

## Phase 2: DAO & service layer
**~2 sessions**

* **`GenericDao`** — findById, findAll, save, update, delete | `DAO`
* **`UserDao`** — findByEmail, findByUsername | `DAO`
* **`PostDao`** — global feed, follow feed, group posts queries | `DAO`
* **`CommentDao`, `GroupDao`, `GroupMemberDao`, `FollowDao`** | `DAO`
* **`AuthService`** — register (hash password), login, logout | `Service`
* **`PostService` + `CommentService`** — CRUD + visibility logic | `Service`
* **`GroupService`** — create, join, leave, promote, expel | `Service`
* **`FollowService`** — follow, unfollow, getFollowers, getFollowing | `Service`

---

## Phase 3: Authentication & session management
**~1–2 sessions**

* **`AuthBean`** — @Named @SessionScoped, login/register actions | `JSF Bean`
* **`login.xhtml` + `register.xhtml`** Facelets pages | `XHTML`
* **Auth guard** — redirect to login if session is empty (PhaseListener or Filter) | `Security`
* **isBanned check** — block login if user is banned | `Security`

---

## Phase 4: Core pages — feed, posts & comments
**~2–3 sessions**

* **`index.xhtml`** — global feed (PUBLIC posts, sorted by date) | `Page`
* **`feed.xhtml`** — my feed (followed users' posts) | `Page`
* **`post/new.xhtml`** — create post form, group selector | `Page`
* **`post/view.xhtml`** — post detail + comments + add comment | `Page`
* **Edit/delete post** — author-only + admin override buttons | `Logic`

---

## Phase 5: Profile & follow system
**~1–2 sessions**

* **`profile/view.xhtml`** — user info, posts, followers/following | `Page`
* **Follow/unfollow button** — ajax action, update counter | `Feature`
* **`profile/edit.xhtml`** — bio, picture, change password | `Page`

---

## Phase 6: Groups & communities
**~2 sessions**

* **`groups/list.xhtml`** — list PUBLIC groups, join button | `Page`
* **`groups/new.xhtml`** — create group form | `Page`
* **`groups/view.xhtml`** — group feed + members + admin actions | `Page`
* **PRIVATE group access control** — redirect non-members | `Security`
* **Group admin** — promote member, expel, delete any post | `Feature`

---

## Phase 7: Admin dashboard
**~1 session**

* **`admin/dashboard.xhtml`** — manage users, posts, groups | `Page`
* **Admin-only guard** — role check, redirect non-admins | `Security`
* **Ban/unban user** — set isBanned, invalidate session | `Feature`

---

## Phase 8: Facelets layout & UI polish
**~1 session**

* **`WEB-INF/templates/layout.xhtml`** — navbar + content slot + footer | `Template`
* **CSS + JS resources** — `h:outputStylesheet`, `h:outputScript` | `Resources`
* **Flash messages + validation feedback** — `h:messages` | `UX`

> 💡 **Note:** You can build the layout template at the very start (Phase 0) so every page inherits it from day one.