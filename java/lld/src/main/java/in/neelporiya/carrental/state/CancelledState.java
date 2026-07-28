package in.neelporiya.carrental.state;

import in.neelporiya.carrental.model.ReservationStatus;

public final class CancelledState implements ReservationState {

    @Override
    public ReservationStatus status() {
        return ReservationStatus.CANCELLED;
    }
}
