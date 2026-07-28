package in.neelporiya.carrental.state;

import in.neelporiya.carrental.model.ReservationStatus;

public final class CompletedState implements ReservationState {

    @Override
    public ReservationStatus status() {
        return ReservationStatus.COMPLETED;
    }
}
