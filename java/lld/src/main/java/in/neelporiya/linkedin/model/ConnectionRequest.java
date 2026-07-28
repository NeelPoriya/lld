package in.neelporiya.linkedin.model;

import java.time.Instant;
import java.util.Objects;

public class ConnectionRequest {

    private final String id;
    private final Member sender;
    private final Member recipient;
    private final Instant createdAt;
    private ConnectionRequestStatus status = ConnectionRequestStatus.PENDING; // guarded by this

    public ConnectionRequest(String id, Member sender, Member recipient, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.sender = Objects.requireNonNull(sender, "sender");
        this.recipient = Objects.requireNonNull(recipient, "recipient");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public synchronized boolean isPending() {
        return status == ConnectionRequestStatus.PENDING;
    }

    public synchronized void markAccepted() {
        if (!isPending()) {
            throw new IllegalStateException("Connection request is no longer pending");
        }
        status = ConnectionRequestStatus.ACCEPTED;
    }

    public synchronized void markRejected() {
        if (!isPending()) {
            throw new IllegalStateException("Connection request is no longer pending");
        }
        status = ConnectionRequestStatus.REJECTED;
    }

    public String getId() {
        return id;
    }

    public Member getSender() {
        return sender;
    }

    public Member getRecipient() {
        return recipient;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public synchronized ConnectionRequestStatus getStatus() {
        return status;
    }
}


