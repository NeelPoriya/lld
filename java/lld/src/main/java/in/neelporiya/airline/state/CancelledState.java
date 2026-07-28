package in.neelporiya.airline.state;

import in.neelporiya.airline.model.BookingStatus;

public final class CancelledState implements BookingState {

    @Override
    public BookingStatus status() {
        return BookingStatus.CANCELLED;
    }
}
