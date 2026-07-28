package in.neelporiya.airline.model;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * One physical seat on one dated flight instance.
 *
 * <p>// CONCURRENCY: booking is a check-then-act race if code reads "free" and later writes
 * "occupied". This seat stores the winning booking id in an AtomicReference, so
 * {@code compareAndSet(null, bookingId)} makes the availability check and claim one atomic step.
 */
public final class Seat {

    private final String seatNumber;
    private final SeatClass seatClass;
    private final long baseFareCents;
    private final AtomicReference<String> bookingId = new AtomicReference<>();

    public Seat(String seatNumber, SeatClass seatClass, long baseFareCents) {
        this.seatNumber = Passenger.requireText(seatNumber, "seatNumber");
        this.seatClass = Objects.requireNonNull(seatClass, "seatClass");
        if (baseFareCents < 0) {
            throw new IllegalArgumentException("baseFareCents must not be negative");
        }
        this.baseFareCents = baseFareCents;
    }

    public boolean tryClaim(String bookingId) {
        Passenger.requireText(bookingId, "bookingId");
        // INTERVIEW INSIGHT: no caller may split this into isAvailable() then occupy(); that gap double-sells seats.
        return this.bookingId.compareAndSet(null, bookingId);
    }

    public boolean releaseBooking(String expectedBookingId) {
        Passenger.requireText(expectedBookingId, "expectedBookingId");
        // CONCURRENCY: only the booking that owns this seat can free it; stale cancels cannot clear a rebooked seat.
        return bookingId.compareAndSet(expectedBookingId, null);
    }

    public boolean isAvailable() {
        return bookingId.get() == null;
    }

    public Optional<String> getBookingId() {
        return Optional.ofNullable(bookingId.get());
    }

    public Seat copyUnclaimed() {
        return new Seat(seatNumber, seatClass, baseFareCents);
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public SeatClass getSeatClass() {
        return seatClass;
    }

    public long getBaseFareCents() {
        return baseFareCents;
    }
}
