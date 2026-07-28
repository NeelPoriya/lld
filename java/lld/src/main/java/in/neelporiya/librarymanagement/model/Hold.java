package in.neelporiya.librarymanagement.model;

import java.time.Instant;
import java.util.Objects;

/** A member's reservation request for the next returned copy of a book. */
public class Hold {

    private final String id;
    private final String bookId;
    private final String memberId;
    private final Instant createdAt;

    private volatile Instant notifiedAt;
    private volatile String reservedBarcode;

    public Hold(String id, String bookId, String memberId, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.bookId = Objects.requireNonNull(bookId, "bookId");
        this.memberId = Objects.requireNonNull(memberId, "memberId");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public synchronized void markNotified(Instant notifiedAt, String reservedBarcode) {
        this.notifiedAt = Objects.requireNonNull(notifiedAt, "notifiedAt");
        this.reservedBarcode = Objects.requireNonNull(reservedBarcode, "reservedBarcode");
    }

    public boolean isNotified() {
        return notifiedAt != null;
    }

    public String getId() {
        return id;
    }

    public String getBookId() {
        return bookId;
    }

    public String getMemberId() {
        return memberId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getNotifiedAt() {
        return notifiedAt;
    }

    public String getReservedBarcode() {
        return reservedBarcode;
    }
}
