package in.neelporiya.socialnetwork;

import in.neelporiya.socialnetwork.event.SocialNetworkEventListener;
import in.neelporiya.socialnetwork.model.Comment;
import in.neelporiya.socialnetwork.model.FriendRequest;
import in.neelporiya.socialnetwork.model.FriendRequestStatus;
import in.neelporiya.socialnetwork.model.Post;
import in.neelporiya.socialnetwork.model.Profile;
import in.neelporiya.socialnetwork.model.User;
import in.neelporiya.socialnetwork.service.SocialNetworkService;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SocialNetworkServiceTest {

    private MutableClock clock;
    private SocialNetworkService service;
    private User alice;
    private User bob;
    private User carol;

    @BeforeEach
    void setUp() {
        AtomicInteger seq = new AtomicInteger();
        clock = MutableClock.atEpoch();
        service = new SocialNetworkService(clock, () -> "id-" + seq.incrementAndGet());
        alice = service.registerUser(Profile.of("alice", "likes java", "Pune"));
        bob = service.registerUser(Profile.of("bob", "likes lld", "Mumbai"));
        carol = service.registerUser(Profile.of("carol", "likes tests", "Delhi"));
    }

    @Test
    void acceptingFriendRequestMakesFriendshipBidirectional() {
        FriendRequest request = service.sendFriendRequest(alice, bob);
        service.acceptFriendRequest(request);

        assertEquals(FriendRequestStatus.ACCEPTED, request.getStatus());
        assertTrue(alice.isFriendsWith(bob));
        assertTrue(bob.isFriendsWith(alice));
    }

    @Test
    void rejectingFriendRequestDoesNotCreateFriendship() {
        FriendRequest request = service.sendFriendRequest(alice, bob);
        service.rejectFriendRequest(request);

        assertEquals(FriendRequestStatus.REJECTED, request.getStatus());
        assertFalse(alice.isFriendsWith(bob));
        assertFalse(bob.isFriendsWith(alice));
    }

    @Test
    void createsTextPostWithInjectedTimestamp() {
        Post post = service.createPost(alice, "hello social network");

        assertEquals("hello social network", post.getText());
        assertEquals(alice, post.getAuthor());
        assertEquals(clock.instant(), post.getCreatedAt());
    }

    @Test
    void likeIsIdempotentAndUnlikeDecrements() {
        Post post = service.createPost(alice, "hot post");

        assertTrue(service.likePost(bob, post));
        assertFalse(service.likePost(bob, post));
        assertEquals(1, post.likeCount());

        assertTrue(service.unlikePost(bob, post));
        assertEquals(0, post.likeCount());
    }

    @Test
    void commentsAreAttachedToPosts() {
        Post post = service.createPost(alice, "post");
        Comment comment = service.commentOnPost(bob, post, "nice");

        assertEquals(List.of(comment), post.getComments());
        assertEquals("nice", post.getComments().get(0).text());
    }

    @Test
    void newsFeedContainsOwnAndFriendsPostsSortedNewestFirst() {
        service.acceptFriendRequest(service.sendFriendRequest(alice, bob));

        Post oldFriendPost = service.createPost(alice, "old friend post");
        clock.advance(Duration.ofMinutes(1));
        Post strangerPost = service.createPost(carol, "stranger post");
        clock.advance(Duration.ofMinutes(1));
        Post ownPost = service.createPost(bob, "own post");
        clock.advance(Duration.ofMinutes(1));
        Post newFriendPost = service.createPost(alice, "new friend post");

        assertEquals(List.of(newFriendPost, ownPost, oldFriendPost), service.newsFeedFor(bob));
        assertFalse(service.newsFeedFor(bob).contains(strangerPost));
    }

    @Test
    void notificationsFireThroughObserverForFriendRequestLikeAndComment() {
        List<String> events = new ArrayList<>();
        service.addListener(new SocialNetworkEventListener() {
            @Override
            public void onFriendRequestSent(FriendRequest request) {
                events.add("friend:" + request.getRecipient().getProfile().displayName());
            }

            @Override
            public void onPostLiked(Post post, User likedBy) {
                events.add("like:" + likedBy.getProfile().displayName());
            }

            @Override
            public void onCommentAdded(Post post, Comment comment) {
                events.add("comment:" + comment.author().getProfile().displayName());
            }
        });

        service.sendFriendRequest(alice, bob);
        Post post = service.createPost(alice, "observer post");
        service.likePost(bob, post);
        service.commentOnPost(carol, post, "observer comment");

        assertEquals(List.of("friend:bob", "like:bob", "comment:carol"), events);
        assertEquals(1, service.notificationsFor(bob).size());
        assertEquals(2, service.notificationsFor(alice).size());
    }
}
