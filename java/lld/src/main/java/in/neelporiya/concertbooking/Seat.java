package in.neelporiya.concertbooking;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * One physical seat in a venue.
 *
 * <p>// DESIGN PATTERN: State. The AtomicReference value represents AVAILABLE (null), HELD, or
 * BOOKED; legal transitions are enforced by methods instead of scattered conditionals.
 */
public class Seat implements Identifiable {

    private final String id;
    private final String sectionId;
    private final String rowLabel;
    private final int number;
    private final AtomicReference<SeatClaim> claim = new AtomicReference<>();

    public Seat(String id, String sectionId, String rowLabel, int number) {
        this.id = Objects.requireNonNull(id, "id");
        this.sectionId = Objects.requireNonNull(sectionId, "sectionId");
        this.rowLabel = Objects.requireNonNull(rowLabel, "rowLabel");
        this.number = number;
    }

    public Seat freshCopy() {
        // INTERVIEW INSIGHT: the physical seat label belongs to the venue, but availability belongs
        // to a specific concert date. A fresh copy lets two concerts at the same venue sell A1
        // independently.
        return new Seat(id, sectionId, rowLabel, number);
    }

    public boolean tryHold(String userId, String bookingId, Instant expiresAt, Instant now) {
        releaseIfExpired(now);
        SeatClaim newClaim = new SeatClaim(userId, bookingId, expiresAt, SeatState.HELD);
        // CONCURRENCY: this CAS is the interview crux. If many users race for this exact seat,
        // exactly one compareAndSet(null, hold) wins; every loser sees false and the service rolls
        // back or tries another seat. There is no check-then-act gap and no global venue lock.
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
            // CONCURRENCY: HELD -> BOOKED is also CAS-protected, so a stale confirmer cannot book a
            // seat that was concurrently expired/cancelled and re-held by somebody else.
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
            // TESTABILITY: expiry is based only on the injected Clock's Instant passed by the
            // service; tests advance MutableClock and trigger this lazy release instantly.
            if (claim.compareAndSet(current, null)) {
                return;
            }
        }
    }

    @Override
    public String getId() {
        return id;
    }

    public String getSectionId() {
        return sectionId;
    }

    public String getRowLabel() {
        return rowLabel;
    }

    public int getNumber() {
        return number;
    }
}
