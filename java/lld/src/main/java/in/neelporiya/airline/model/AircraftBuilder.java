package in.neelporiya.airline.model;

import java.util.ArrayList;
import java.util.List;

public final class AircraftBuilder {

    private final String tailNumber;
    private final List<Seat> seats = new ArrayList<>();

    private AircraftBuilder(String tailNumber) {
        this.tailNumber = Passenger.requireText(tailNumber, "tailNumber");
    }

    public static AircraftBuilder forTailNumber(String tailNumber) {
        return new AircraftBuilder(tailNumber);
    }

    public AircraftBuilder addSeat(String seatNumber, SeatClass seatClass, long baseFareCents) {
        // DESIGN PATTERN: Builder keeps large cabin construction readable in tests and demos.
        seats.add(new Seat(seatNumber, seatClass, baseFareCents));
        return this;
    }

    public Aircraft build() {
        return new Aircraft(tailNumber, seats);
    }
}
