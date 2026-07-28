package in.neelporiya.airline.state;

import in.neelporiya.airline.model.Booking;
import in.neelporiya.airline.model.BookingStatus;

import java.time.Instant;

public final class ConfirmedState implements BookingState {

    @Override
    public BookingStatus status() {
        return BookingStatus.CONFIRMED;
    }

    @Override
    public void checkIn(Booking booking, Instant now) {
        // DESIGN PATTERN: State localizes legal lifecycle transitions instead of scattering if/else in the service.
        booking.markCheckedIn(now);
        booking.transitionTo(new CheckedInState());
    }

    @Override
    public void cancel(Booking booking, Instant now) {
        booking.markCancelled(now);
        booking.transitionTo(new CancelledState());
    }
}
