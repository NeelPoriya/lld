package in.neelporiya.socialnetwork.event;

import in.neelporiya.socialnetwork.model.Comment;
import in.neelporiya.socialnetwork.model.FriendRequest;
import in.neelporiya.socialnetwork.model.Notification;
import in.neelporiya.socialnetwork.model.Post;
import in.neelporiya.socialnetwork.model.User;

import java.time.Clock;
import java.util.function.Supplier;

/**
 * // DESIGN PATTERN: Factory — message wording and notification construction are centralized here,
 * leaving observers to focus on routing/storage.
 */
public class NotificationFactory {

    private final Clock clock;
    private final Supplier<String> idGenerator;

    public NotificationFactory(Clock clock, Supplier<String> idGenerator) {
        this.clock = clock;
        this.idGenerator = idGenerator;
    }

    public Notification friendRequest(FriendRequest request) {
        return new Notification(
                idGenerator.get(),
                request.getRecipient(),
                request.getSender().getProfile().displayName() + " sent you a friend request",
                clock.instant());
    }

    public Notification like(Post post, User likedBy) {
        return new Notification(
                idGenerator.get(),
                post.getAuthor(),
                likedBy.getProfile().displayName() + " liked your post",
                clock.instant());
    }

    public Notification comment(Post post, Comment comment) {
        return new Notification(
                idGenerator.get(),
                post.getAuthor(),
                comment.author().getProfile().displayName() + " commented on your post",
                clock.instant());
    }
}
