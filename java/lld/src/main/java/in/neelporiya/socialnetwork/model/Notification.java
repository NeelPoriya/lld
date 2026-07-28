package in.neelporiya.socialnetwork.model;

import java.time.Instant;
import java.util.Objects;

public record Notification(String id, User recipient, String message, Instant createdAt) {

    public Notification {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(recipient, "recipient");
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(createdAt, "createdAt");
    }
}
