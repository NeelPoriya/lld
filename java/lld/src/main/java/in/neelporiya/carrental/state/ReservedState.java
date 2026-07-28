package in.neelporiya.carrental.state;

import in.neelporiya.carrental.model.Reservation;
import in.neelporiya.carrental.model.ReservationStatus;

import java.time.Instant;
import java.util.Objects;

public final class ReservedState implements ReservationState {

    @Override
    public ReservationStatus status() {
        return ReservationStatus.RESERVED;
    }

    @Override
    public void pickUp(Reservation reservation, Instant now) {
        // DESIGN PATTERN: State object owns the legal RESERVED -> ONGOING transition.
        reservation.markPickedUp(Objects.requireNonNull(now, "now"));
        reservation.transitionTo(new OngoingState());
    }

    @Override
    public void cancel(Reservation reservation, Instant now) {
        reservation.markCancelled(Objects.requireNonNull(now, "now"));
        reservation.transitionTo(new CancelledState());
    }
}
