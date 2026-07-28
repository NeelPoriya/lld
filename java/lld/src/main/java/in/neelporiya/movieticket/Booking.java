package in.neelporiya.movieticket;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * // DESIGN PATTERN: State for HELD -> CONFIRMED, HELD -> EXPIRED, and HELD/CONFIRMED -> CANCELLED.
 */
public class Booking implements Identifiable {

    private final String id;
    private final String showId;
    private final String userId;
    private final List<String> seatIds;
    private final Instant heldAt;
    private final Instant expiresAt;
    private final long totalPriceCents;
    private final AtomicReference<BookingStatus> status = new AtomicReference<>(BookingStatus.HELD);
    private volatile Instant completedAt;

    public Booking(String id, String showId, String userId, List<String> seatIds,
                   Instant heldAt, Instant expiresAt, long totalPriceCents) {
        this.id = Objects.requireNonNull(id, "id");
        this.showId = Objects.requireNonNull(showId, "showId");
        this.userId = Objects.requireNonNull(userId, "userId");
        this.seatIds = List.copyOf(seatIds);
        this.heldAt = Objects.requireNonNull(heldAt, "heldAt");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
        this.totalPriceCents = totalPriceCents;
    }

    public boolean markConfirmed(Instant now) {
        boolean changed = status.compareAndSet(BookingStatus.HELD, BookingStatus.CONFIRMED);
        if (changed) {
            completedAt = now;
        }
        return changed;
    }

    public boolean markExpired(Instant now) {
        boolean changed = status.compareAndSet(BookingStatus.HELD, BookingStatus.EXPIRED);
        if (changed) {
            completedAt = now;
        }
        return changed;
    }

    public boolean markCancelled(Instant now) {
        while (true) {
            BookingStatus current = status.get();
            if (current == BookingStatus.EXPIRED || current == BookingStatus.CANCELLED) {
                return false;
            }
            if (status.compareAndSet(current, BookingStatus.CANCELLED)) {
                completedAt = now;
                return true;
            }
        }
    }

    public boolean isExpiredAt(Instant now) {
        return getStatus() == BookingStatus.HELD && !now.isBefore(expiresAt);
    }

    @Override
    public String getId() {
        return id;
    }

    public String getShowId() {
        return showId;
    }

    public String getUserId() {
        return userId;
    }

    public List<String> getSeatIds() {
        return seatIds;
    }

    public Instant getHeldAt() {
        return heldAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public long getTotalPriceCents() {
        return totalPriceCents;
    }

    public BookingStatus getStatus() {
        return status.get();
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
