package in.neelporiya.hotelmanagement.state;

import in.neelporiya.hotelmanagement.model.ReservationStatus;

public final class CancelledState implements ReservationState {

    @Override
    public ReservationStatus status() {
        return ReservationStatus.CANCELLED;
    }
}
