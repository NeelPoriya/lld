package in.neelporiya.airline.state;

import in.neelporiya.airline.exception.IllegalBookingTransitionException;
import in.neelporiya.airline.model.Booking;
import in.neelporiya.airline.model.BookingStatus;

import java.time.Instant;

public interface BookingState {

    BookingStatus status();

    default void checkIn(Booking booking, Instant now) {
        throw illegal("check in");
    }

    default void cancel(Booking booking, Instant now) {
        throw illegal("cancel");
    }

    private IllegalBookingTransitionException illegal(String action) {
        return new IllegalBookingTransitionException("Cannot " + action + " a " + status() + " booking");
    }
}
