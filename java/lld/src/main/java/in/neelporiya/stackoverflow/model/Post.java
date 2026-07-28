package in.neelporiya.stackoverflow.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * // DESIGN PATTERN: Template Method — the shared voting/commenting machinery lives here; concrete
 * {@link Question} and {@link Answer} add their own state.
 *
 * <p>// CONCURRENCY: This is the vote hot spot. {@code votesByUser} and {@code score} are guarded by
 * the post's intrinsic lock ({@code synchronized}). The critical property is that "look up the
 * user's previous vote → update the map → adjust the score" happens as ONE atomic step, so:
 * <ul>
 *   <li>a user is counted at most once (re-voting the same way is a no-op),</li>
 *   <li>toggling UP→DOWN moves the score by exactly −2, and</li>
 *   <li>concurrent voters on the same post can't corrupt the tally.</li>
 * </ul>
 * Because the lock is per-post, votes on <em>different</em> posts run fully in parallel.
 */
public abstract class Post {

    private final String id;
    private final User author;
    private final String body;
    private final Instant createdAt;

    private final Map<String, VoteType> votesByUser = new HashMap<>(); // guarded by 'this'
    private int score;                                                 // guarded by 'this'
    private final List<Comment> comments = new CopyOnWriteArrayList<>();

    protected Post(String id, User author, String body, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.author = Objects.requireNonNull(author, "author");
        this.body = Objects.requireNonNull(body, "body");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public synchronized VoteChange castVote(User voter, VoteType type) {
        Objects.requireNonNull(type, "type");
        VoteType previous = votesByUser.get(voter.getId());
        if (previous == type) {
            return new VoteChange(previous, type); // idempotent: same vote again = no change
        }
        votesByUser.put(voter.getId(), type);
        score += type.value() - valueOf(previous);
        return new VoteChange(previous, type);
    }

    public synchronized VoteChange retractVote(User voter) {
        VoteType previous = votesByUser.remove(voter.getId());
        if (previous == null) {
            return new VoteChange(null, null);
        }
        score -= previous.value();
        return new VoteChange(previous, null);
    }

    public synchronized int getScore() {
        return score;
    }

    private static int valueOf(VoteType type) {
        return type == null ? 0 : type.value();
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

    public String getBody() {
        return body;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
