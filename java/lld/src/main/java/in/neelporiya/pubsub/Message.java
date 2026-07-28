package in.neelporiya.pubsub;

import java.time.Instant;

/**
 * An immutable message. The {@code offset} is a per-topic, monotonically increasing sequence — the
 * seam a real broker would use for retention/replay ("resume from offset N").
 */
public record Message(String id, String topic, String payload, Instant publishedAt, long offset) {
}
