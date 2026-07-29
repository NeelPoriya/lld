package in.neelporiya.facebooksearch;

import java.time.Instant;

/**
 * One indexable thing (a person, page, group or post).
 *
 * <p>// INTERVIEW INSIGHT: search ranking needs signals. Here they are {@code popularity} (friends /
 * followers / likes) and {@code createdAt} (recency). A real system blends dozens of signals, but the
 * design — an immutable document carrying its ranking features — is the same.
 */
public record SearchDocument(String id, String text, EntityType type, long popularity, Instant createdAt) {

    public SearchDocument {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id is required");
        }
        if (text == null) {
            throw new IllegalArgumentException("text is required");
        }
    }
}
