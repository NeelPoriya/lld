package in.neelporiya.librarymanagement.model;

import java.time.Instant;
import java.util.Objects;

/** A checkout record for one physical copy. Fine money is integer cents, never double. */
public class Loan {

    private final String id;
    private final String memberId;
    private final String bookId;
    private final String barcode;
    private final Instant checkedOutAt;
    private final Instant dueAt;

    private volatile Instant returnedAt;
    private volatile long fineCents;

    public Loan(String id, String memberId, String bookId, String barcode, Instant checkedOutAt, Instant dueAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.memberId = Objects.requireNonNull(memberId, "memberId");
        this.bookId = Objects.requireNonNull(bookId, "bookId");
        this.barcode = Objects.requireNonNull(barcode, "barcode");
        this.checkedOutAt = Objects.requireNonNull(checkedOutAt, "checkedOutAt");
        this.dueAt = Objects.requireNonNull(dueAt, "dueAt");
    }

    public synchronized void close(Instant returnedAt, long fineCents) {
        if (this.returnedAt != null) {
            throw new IllegalStateException("Loan already returned: " + id);
        }
        this.returnedAt = Objects.requireNonNull(returnedAt, "returnedAt");
        this.fineCents = fineCents;
    }

    public String getId() {
        return id;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getBookId() {
        return bookId;
    }

    public String getBarcode() {
        return barcode;
    }

    public Instant getCheckedOutAt() {
        return checkedOutAt;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public Instant getReturnedAt() {
        return returnedAt;
    }

    public long getFineCents() {
        return fineCents;
    }
}
