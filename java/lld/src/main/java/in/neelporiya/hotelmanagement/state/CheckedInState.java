package in.neelporiya.hotelmanagement.state;

import in.neelporiya.hotelmanagement.model.Reservation;
import in.neelporiya.hotelmanagement.model.ReservationStatus;

import java.time.Instant;
import java.util.Objects;

public final class CheckedInState implements ReservationState {

    @Override
    public ReservationStatus status() {
        return ReservationStatus.CHECKED_IN;
    }

    @Override
    public void checkOut(Reservation reservation, Instant now) {
        reservation.markCheckedOut(Objects.requireNonNull(now, "now"));
        reservation.transitionTo(new CheckedOutState());
    }
}
