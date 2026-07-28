package in.neelporiya.carrental.state;

import in.neelporiya.carrental.exception.IllegalReservationTransitionException;
import in.neelporiya.carrental.model.Reservation;
import in.neelporiya.carrental.model.ReservationStatus;

import java.time.Instant;

public interface ReservationState {

    ReservationStatus status();

    default void pickUp(Reservation reservation, Instant now) {
        throw illegal("pick up");
    }

    default void returnVehicle(Reservation reservation, Instant now) {
        throw illegal("return");
    }

    default void cancel(Reservation reservation, Instant now) {
        throw illegal("cancel");
    }

    private IllegalReservationTransitionException illegal(String action) {
        return new IllegalReservationTransitionException("Cannot " + action + " a " + status() + " reservation");
    }
}
