package in.neelporiya.socialnetwork.model;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A social-network account with profile data and bidirectional friendships.
 *
 * <p>// DESIGN PATTERN: Builder — tests and interview examples can construct expressive users without
 * telescoping constructors. The service still owns id generation.
 */
public class User {

    private final String id;
    private final Profile profile;
    private final Set<String> friendIds = ConcurrentHashMap.newKeySet();

    private User(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.profile = Objects.requireNonNull(builder.profile, "profile");
    }

    public static Builder builder() {
        return new Builder();
    }

    /** // CONCURRENCY: the backing set is a ConcurrentHashMap key-set, so duplicate adds are safe. */
    public void addFriend(User friend) {
        friendIds.add(friend.getId());
    }

    public boolean isFriendsWith(User other) {
        return friendIds.contains(other.getId());
    }

    public Set<String> getFriendIds() {
        return Set.copyOf(friendIds);
    }

    public String getId() {
        return id;
    }

    public Profile getProfile() {
        return profile;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof User other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static class Builder {
        private String id;
        private Profile profile;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder profile(Profile profile) {
            this.profile = profile;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
