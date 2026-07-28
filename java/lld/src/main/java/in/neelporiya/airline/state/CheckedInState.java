package in.neelporiya.airline.state;

import in.neelporiya.airline.model.Booking;
import in.neelporiya.airline.model.BookingStatus;

import java.time.Instant;

public final class CheckedInState implements BookingState {

    @Override
    public BookingStatus status() {
        return BookingStatus.CHECKED_IN;
    }

    @Override
    public void cancel(Booking booking, Instant now) {
        booking.markCancelled(now);
        booking.transitionTo(new CancelledState());
    }
}
