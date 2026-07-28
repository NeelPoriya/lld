package in.neelporiya.linkedin.model;

import java.time.Instant;

public record Notification(String id, String recipientId, String message, Instant createdAt) {
}
