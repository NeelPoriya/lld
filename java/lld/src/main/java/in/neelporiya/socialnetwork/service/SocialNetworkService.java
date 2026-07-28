package in.neelporiya.socialnetwork.service;

import in.neelporiya.socialnetwork.event.NotificationFactory;
import in.neelporiya.socialnetwork.event.NotificationService;
import in.neelporiya.socialnetwork.event.SocialNetworkEventListener;
import in.neelporiya.socialnetwork.feed.ChronologicalFeedStrategy;
import in.neelporiya.socialnetwork.feed.NewsFeedStrategy;
import in.neelporiya.socialnetwork.model.Comment;
import in.neelporiya.socialnetwork.model.FriendRequest;
import in.neelporiya.socialnetwork.model.Notification;
import in.neelporiya.socialnetwork.model.Post;
import in.neelporiya.socialnetwork.model.Profile;
import in.neelporiya.socialnetwork.model.User;
import in.neelporiya.socialnetwork.repository.PostRepository;
import in.neelporiya.socialnetwork.repository.UserRepository;

import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * // DESIGN PATTERN: Facade — one API over users, friendships, posts, feed ranking, repositories, and
 * observer notifications. Clients do not coordinate these objects themselves.
 *
 * <p>// TESTABILITY: both {@link Clock} and id {@link Supplier} are injected, so tests use
 * MutableClock and deterministic ids instead of sleeping or parsing random UUIDs.
 */
public class SocialNetworkService {

    private final UserRepository userRepository = new UserRepository();
    private final PostRepository postRepository = new PostRepository();
    private final List<SocialNetworkEventListener> listeners = new CopyOnWriteArrayList<>();

    private final Clock clock;
    private final Supplier<String> idGenerator;
    private final NewsFeedStrategy feedStrategy;
    private final NotificationService notificationService;

    public SocialNetworkService(Clock clock, Supplier<String> idGenerator) {
        this(clock, idGenerator, new ChronologicalFeedStrategy());
    }

    public SocialNetworkService(Clock clock, Supplier<String> idGenerator, NewsFeedStrategy feedStrategy) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
        this.feedStrategy = Objects.requireNonNull(feedStrategy, "feedStrategy");
        this.notificationService = new NotificationService(new NotificationFactory(clock, idGenerator));
        this.listeners.add(notificationService);
    }

    public static SocialNetworkService createDefault() {
        return new SocialNetworkService(Clock.systemUTC(), () -> UUID.randomUUID().toString());
    }

    public void addListener(SocialNetworkEventListener listener) {
        listeners.add(listener);
    }

    public User registerUser(Profile profile) {
        User user = User.builder()
                .id(idGenerator.get())
                .profile(profile)
                .build();
        userRepository.save(user);
        return user;
    }

    public User registerUser(String displayName) {
        return registerUser(Profile.of(displayName, "", ""));
    }

    public FriendRequest sendFriendRequest(User sender, User recipient) {
        if (sender.equals(recipient)) {
            throw new IllegalArgumentException("Cannot send a friend request to yourself");
        }
        if (sender.isFriendsWith(recipient)) {
            throw new IllegalStateException("Users are already friends");
        }

        FriendRequest request = new FriendRequest(idGenerator.get(), sender, recipient, clock.instant());
        listeners.forEach(listener -> listener.onFriendRequestSent(request));
        return request;
    }

    /**
     * // CONCURRENCY: synchronize on the request so accept/reject cannot both win. Then lock users in
     * sorted id order before updating both friend sets, preventing deadlocks and preserving the
     * bidirectional invariant when concurrent accept calls race.
     */
    public void acceptFriendRequest(FriendRequest request) {
        synchronized (request) {
            if (!request.isPending()) {
                throw new IllegalStateException("Friend request is no longer pending");
            }
            User first = request.getSender().getId().compareTo(request.getRecipient().getId()) <= 0
                    ? request.getSender()
                    : request.getRecipient();
            User second = first.equals(request.getSender()) ? request.getRecipient() : request.getSender();

            synchronized (first) {
                synchronized (second) {
                    request.getSender().addFriend(request.getRecipient());
                    request.getRecipient().addFriend(request.getSender());
                    request.markAccepted();
                }
            }
        }
    }

    public void rejectFriendRequest(FriendRequest request) {
        request.markRejected();
    }

    public boolean areFriends(User first, User second) {
        return first.isFriendsWith(second) && second.isFriendsWith(first);
    }

    public Post createPost(User author, String text) {
        Post post = new Post(idGenerator.get(), author, text, clock.instant());
        postRepository.save(post);
        return post;
    }

    /**
     * // INTERVIEW INSIGHT: notify only when the like actually changes state. A double-click should
     * not inflate the like count or spam notifications.
     */
    public boolean likePost(User user, Post post) {
        boolean changed = post.like(user);
        if (changed) {
            listeners.forEach(listener -> listener.onPostLiked(post, user));
        }
        return changed;
    }

    public boolean unlikePost(User user, Post post) {
        return post.unlike(user);
    }

    public Comment commentOnPost(User author, Post post, String text) {
        Comment comment = new Comment(idGenerator.get(), author, text, clock.instant());
        post.addComment(comment);
        listeners.forEach(listener -> listener.onCommentAdded(post, comment));
        return comment;
    }

    public List<Post> newsFeedFor(User viewer) {
        Set<String> authorIds = new HashSet<>(viewer.getFriendIds());
        authorIds.add(viewer.getId());
        return feedStrategy.rank(viewer, postRepository.findByAuthorIds(authorIds));
    }

    public List<Notification> notificationsFor(User user) {
        return notificationService.notificationsFor(user);
    }

    /** // EXTENSIBILITY: expose repositories for demos/tests without coupling clients to storage type. */
    public User findUser(String id) {
        return userRepository.findById(id);
    }

    public Post findPost(String id) {
        return postRepository.findById(id);
    }
}
