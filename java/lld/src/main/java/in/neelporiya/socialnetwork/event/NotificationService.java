package in.neelporiya.socialnetwork.event;

import in.neelporiya.socialnetwork.model.Comment;
import in.neelporiya.socialnetwork.model.FriendRequest;
import in.neelporiya.socialnetwork.model.Notification;
import in.neelporiya.socialnetwork.model.Post;
import in.neelporiya.socialnetwork.model.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationService implements SocialNetworkEventListener {

    private final NotificationFactory factory;
    private final Map<String, List<Notification>> notificationsByUser = new ConcurrentHashMap<>();

    public NotificationService(NotificationFactory factory) {
        this.factory = factory;
    }

    @Override
    public void onFriendRequestSent(FriendRequest request) {
        store(factory.friendRequest(request));
    }

    @Override
    public void onPostLiked(Post post, User likedBy) {
        if (!post.getAuthor().equals(likedBy)) {
            store(factory.like(post, likedBy));
        }
    }

    @Override
    public void onCommentAdded(Post post, Comment comment) {
        if (!post.getAuthor().equals(comment.author())) {
            store(factory.comment(post, comment));
        }
    }

    private void store(Notification notification) {
        notificationsByUser
                .computeIfAbsent(notification.recipient().getId(), ignored -> new CopyOnWriteArrayList<>())
                .add(notification);
    }

    public List<Notification> notificationsFor(User user) {
        return List.copyOf(notificationsByUser.getOrDefault(user.getId(), List.of()));
    }
}
