package in.neelporiya.socialnetwork.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Post {

    private final String id;
    private final User author;
    private final String text;
    private final Instant createdAt;
    private final Set<String> likedUserIds = ConcurrentHashMap.newKeySet();
    private final List<Comment> comments = new CopyOnWriteArrayList<>();

    public Post(String id, User author, String text, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.author = Objects.requireNonNull(author, "author");
        this.text = Objects.requireNonNull(text, "text");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    /**
     * // CONCURRENCY: this atomic set-add is the whole idempotency guarantee. Under 100 concurrent
     * double-clicks from the same user, exactly one add returns true and the like count remains 1.
     */
    public boolean like(User user) {
        return likedUserIds.add(user.getId());
    }

    public boolean unlike(User user) {
        return likedUserIds.remove(user.getId());
    }

    public boolean isLikedBy(User user) {
        return likedUserIds.contains(user.getId());
    }

    public int likeCount() {
        return likedUserIds.size();
    }

    public int getLikeCount() {
        return likeCount();
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public List<Comment> getComments() {
        return List.copyOf(comments);
    }

    public String getId() {
        return id;
    }

    public User getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
