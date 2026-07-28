package in.neelporiya.airline.repository;

import in.neelporiya.airline.model.Flight;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class FlightRepository {

    private final ConcurrentMap<String, Flight> flightsByNumber = new ConcurrentHashMap<>();

    public void save(Flight flight) {
        // DESIGN PATTERN: Repository hides storage details from the facade.
        Objects.requireNonNull(flight, "flight");
        flightsByNumber.put(flight.getFlightNumber(), flight);
    }

    public Optional<Flight> findByNumber(String flightNumber) {
        return Optional.ofNullable(flightsByNumber.get(flightNumber));
    }

    public List<Flight> findAll() {
        return List.copyOf(flightsByNumber.values());
    }
}
