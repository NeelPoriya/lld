package in.neelporiya.hotelmanagement.state;

import in.neelporiya.hotelmanagement.exception.IllegalReservationTransitionException;
import in.neelporiya.hotelmanagement.model.Reservation;
import in.neelporiya.hotelmanagement.model.ReservationStatus;

import java.time.Instant;

public interface ReservationState {

    ReservationStatus status();

    default void checkIn(Reservation reservation, Instant now) {
        throw illegal("check in");
    }

    default void checkOut(Reservation reservation, Instant now) {
        throw illegal("check out");
    }

    default void cancel(Reservation reservation, Instant now) {
        throw illegal("cancel");
    }

    private IllegalReservationTransitionException illegal(String action) {
        return new IllegalReservationTransitionException("Cannot " + action + " a " + status() + " reservation");
    }
}
