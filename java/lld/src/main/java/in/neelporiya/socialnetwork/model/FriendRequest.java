package in.neelporiya.socialnetwork.model;

import java.time.Instant;
import java.util.Objects;

public class FriendRequest {

    private final String id;
    private final User sender;
    private final User recipient;
    private final Instant createdAt;
    private FriendRequestStatus status = FriendRequestStatus.PENDING; // guarded by this request

    public FriendRequest(String id, User sender, User recipient, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.sender = Objects.requireNonNull(sender, "sender");
        this.recipient = Objects.requireNonNull(recipient, "recipient");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public synchronized FriendRequestStatus getStatus() {
        return status;
    }

    public synchronized boolean isPending() {
        return status == FriendRequestStatus.PENDING;
    }

    public synchronized void markAccepted() {
        if (status != FriendRequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be accepted");
        }
        status = FriendRequestStatus.ACCEPTED;
    }

    public synchronized void markRejected() {
        if (status != FriendRequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be rejected");
        }
        status = FriendRequestStatus.REJECTED;
    }

    public String getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    public User getRecipient() {
        return recipient;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
