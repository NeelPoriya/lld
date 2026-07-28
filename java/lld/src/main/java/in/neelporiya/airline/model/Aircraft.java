package in.neelporiya.airline.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Aircraft {

    private final String tailNumber;
    private final List<Seat> seatMapTemplate;

    public Aircraft(String tailNumber, List<Seat> seatMapTemplate) {
        this.tailNumber = Passenger.requireText(tailNumber, "tailNumber");
        Objects.requireNonNull(seatMapTemplate, "seatMapTemplate");
        if (seatMapTemplate.isEmpty()) {
            throw new IllegalArgumentException("seatMapTemplate must not be empty");
        }
        this.seatMapTemplate = List.copyOf(seatMapTemplate);
    }

    public static Aircraft singleAisle(String tailNumber, int economySeats, int businessSeats, int firstSeats) {
        // DESIGN PATTERN: Factory method creates a usable cabin without leaking seat numbering rules to clients.
        AircraftBuilder builder = AircraftBuilder.forTailNumber(tailNumber);
        for (int i = 1; i <= firstSeats; i++) {
            builder.addSeat("F" + i, SeatClass.FIRST, 50_000);
        }
        for (int i = 1; i <= businessSeats; i++) {
            builder.addSeat("B" + i, SeatClass.BUSINESS, 25_000);
        }
        for (int i = 1; i <= economySeats; i++) {
            builder.addSeat("E" + i, SeatClass.ECONOMY, 10_000);
        }
        return builder.build();
    }

    public Map<String, Seat> newSeatMapForFlightInstance() {
        return seatMapTemplate.stream()
                .map(Seat::copyUnclaimed)
                .collect(Collectors.toUnmodifiableMap(Seat::getSeatNumber, Function.identity()));
    }

    public String getTailNumber() {
        return tailNumber;
    }

    public List<Seat> getSeatMapTemplate() {
        return seatMapTemplate;
    }
}
