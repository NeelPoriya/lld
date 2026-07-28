package in.neelporiya.hotelmanagement.observer;

import in.neelporiya.hotelmanagement.model.Reservation;

public interface ReservationEventListener {

    default void onBooked(Reservation reservation) {
    }

    default void onCancelled(Reservation reservation) {
    }

    default void onCheckedIn(Reservation reservation) {
    }

    default void onCheckedOut(Reservation reservation) {
    }
}
