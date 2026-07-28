package in.neelporiya.hotelmanagement.state;

import in.neelporiya.hotelmanagement.model.ReservationStatus;

public final class CheckedOutState implements ReservationState {

    @Override
    public ReservationStatus status() {
        return ReservationStatus.CHECKED_OUT;
    }
}
