package in.neelporiya.stackoverflow.model;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A member of the community.
 *
 * <p>// CONCURRENCY: {@code reputation} is an {@link AtomicInteger} because votes cast on many
 * <em>different</em> posts (each guarded by its own lock) all funnel reputation changes into the
 * same author concurrently. {@code addAndGet} makes those increments lost-update-proof without a
 * per-user lock.
 */
public class User {

    private final String id;
    private final String name;
    private final AtomicInteger reputation = new AtomicInteger(0);
    private final Set<String> badges = new CopyOnWriteArraySet<>();

    public User(String id, String name) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
    }

    public int addReputation(int delta) {
        return reputation.addAndGet(delta);
    }

    public int getReputation() {
        return reputation.get();
    }

    public void awardBadge(String badge) {
        badges.add(badge);
    }

    public Set<String> getBadges() {
        return Set.copyOf(badges);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof User user && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name + "(rep=" + getReputation() + ")";
    }
}
