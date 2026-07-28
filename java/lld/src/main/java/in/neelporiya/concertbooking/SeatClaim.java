package in.neelporiya.concertbooking;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable value inside a Seat's AtomicReference.
 */
public class SeatClaim {

    private final String userId;
    private final String bookingId;
    private final Instant expiresAt;
    private final SeatState state;

    public SeatClaim(String userId, String bookingId, Instant expiresAt, SeatState state) {
        this.userId = Objects.requireNonNull(userId, "userId");
        this.bookingId = Objects.requireNonNull(bookingId, "bookingId");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
        this.state = Objects.requireNonNull(state, "state");
    }

    public String getUserId() {
        return userId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public SeatState getState() {
        return state;
    }

    public boolean isExpired(Instant now) {
        return state == SeatState.HELD && !now.isBefore(expiresAt);
    }
}
