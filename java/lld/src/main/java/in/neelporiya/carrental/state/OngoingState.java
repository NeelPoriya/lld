package in.neelporiya.carrental.state;

import in.neelporiya.carrental.model.Reservation;
import in.neelporiya.carrental.model.ReservationStatus;

import java.time.Instant;
import java.util.Objects;

public final class OngoingState implements ReservationState {

    @Override
    public ReservationStatus status() {
        return ReservationStatus.ONGOING;
    }

    @Override
    public void returnVehicle(Reservation reservation, Instant now) {
        reservation.markReturned(Objects.requireNonNull(now, "now"));
        reservation.transitionTo(new CompletedState());
    }
}
