package in.neelporiya.hotelmanagement.state;

import in.neelporiya.hotelmanagement.model.Reservation;
import in.neelporiya.hotelmanagement.model.ReservationStatus;

import java.time.Instant;
import java.util.Objects;

public final class ConfirmedState implements ReservationState {

    @Override
    public ReservationStatus status() {
        return ReservationStatus.CONFIRMED;
    }

    @Override
    public void checkIn(Reservation reservation, Instant now) {
        reservation.markCheckedIn(Objects.requireNonNull(now, "now"));
        reservation.transitionTo(new CheckedInState());
    }

    @Override
    public void cancel(Reservation reservation, Instant now) {
        reservation.markCancelled(Objects.requireNonNull(now, "now"));
        reservation.transitionTo(new CancelledState());
    }
}
