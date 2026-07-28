package in.neelporiya.airline.model;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class FlightInstance {

    private final String id;
    private final Flight flight;
    private final LocalDate flightDate;
    private final Aircraft aircraft;
    private final Map<String, Seat> seatsByNumber;

    public FlightInstance(String id, Flight flight, LocalDate flightDate, Aircraft aircraft) {
        this.id = Passenger.requireText(id, "id");
        this.flight = Objects.requireNonNull(flight, "flight");
        this.flightDate = Objects.requireNonNull(flightDate, "flightDate");
        this.aircraft = Objects.requireNonNull(aircraft, "aircraft");
        this.seatsByNumber = aircraft.newSeatMapForFlightInstance();
    }

    public Optional<Seat> findSeat(String seatNumber) {
        return Optional.ofNullable(seatsByNumber.get(seatNumber));
    }

    public List<Seat> availableSeats() {
        return seatsByNumber.values().stream()
                .filter(Seat::isAvailable)
                .sorted(Comparator.comparing(Seat::getSeatNumber))
                .toList();
    }

    public String getId() {
        return id;
    }

    public Flight getFlight() {
        return flight;
    }

    public LocalDate getFlightDate() {
        return flightDate;
    }

    public Aircraft getAircraft() {
        return aircraft;
    }

    public List<Seat> getSeats() {
        return seatsByNumber.values().stream()
                .sorted(Comparator.comparing(Seat::getSeatNumber))
                .toList();
    }
}
