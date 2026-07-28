package in.neelporiya.movieticket;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A seat inside one show inventory.
 *
 * <p>// DESIGN PATTERN: State. null means AVAILABLE; a SeatClaim means HELD or BOOKED.
 */
public class Seat implements Identifiable {

    private final String id;
    private final String rowLabel;
    private final int number;
    private final SeatType type;
    private final long basePriceCents;
    private final AtomicReference<SeatClaim> claim = new AtomicReference<>();

    public Seat(String id, String rowLabel, int number, SeatType type, long basePriceCents) {
        if (basePriceCents < 0) {
            throw new IllegalArgumentException("price cannot be negative");
        }
        this.id = Objects.requireNonNull(id, "id");
        this.rowLabel = Objects.requireNonNull(rowLabel, "rowLabel");
        this.number = number;
        this.type = Objects.requireNonNull(type, "type");
        this.basePriceCents = basePriceCents;
    }

    public Seat freshCopy() {
        // INTERVIEW INSIGHT: physical label A1 belongs to the screen, but availability belongs to a show.
        return new Seat(id, rowLabel, number, type, basePriceCents);
    }

    public boolean tryHold(String userId, String bookingId, Instant expiresAt, Instant now) {
        releaseIfExpired(now);
        SeatClaim newClaim = new SeatClaim(userId, bookingId, expiresAt, SeatState.HELD);
        // CONCURRENCY: compareAndSet(null, hold) fuses "is free?" and "claim" atomically.
        return claim.compareAndSet(null, newClaim);
    }

    public boolean canConfirm(String bookingId, Instant now) {
        releaseIfExpired(now);
        SeatClaim current = claim.get();
        return current != null
                && current.getState() == SeatState.HELD
                && current.getBookingId().equals(bookingId)
                && !current.isExpired(now);
    }

    public boolean confirmHold(String bookingId, Instant now) {
        while (true) {
            releaseIfExpired(now);
            SeatClaim current = claim.get();
            if (current == null || current.getState() != SeatState.HELD || !current.getBookingId().equals(bookingId)) {
                return false;
            }
            SeatClaim booked = new SeatClaim(current.getUserId(), bookingId, current.getExpiresAt(), SeatState.BOOKED);
            // CONCURRENCY: CAS blocks stale confirmations after expiry/cancel/re-hold races.
            if (claim.compareAndSet(current, booked)) {
                return true;
            }
        }
    }

    public boolean releaseForBooking(String bookingId) {
        while (true) {
            SeatClaim current = claim.get();
            if (current == null || !current.getBookingId().equals(bookingId)) {
                return false;
            }
            if (claim.compareAndSet(current, null)) {
                return true;
            }
        }
    }

    public SeatState getState(Instant now) {
        releaseIfExpired(now);
        SeatClaim current = claim.get();
        return current == null ? SeatState.AVAILABLE : current.getState();
    }

    public SeatClaim getClaim(Instant now) {
        releaseIfExpired(now);
        return claim.get();
    }

    public void releaseIfExpired(Instant now) {
        while (true) {
            SeatClaim current = claim.get();
            if (current == null || !current.isExpired(now)) {
                return;
            }
            // TESTABILITY: service passes injected Clock time; MutableClock tests jump instantly.
            if (claim.compareAndSet(current, null)) {
                return;
            }
        }
    }

    @Override
    public String getId() {
        return id;
    }

    public String getRowLabel() {
        return rowLabel;
    }

    public int getNumber() {
        return number;
    }

    public SeatType getType() {
        return type;
    }

    public long getBasePriceCents() {
        return basePriceCents;
    }
}
