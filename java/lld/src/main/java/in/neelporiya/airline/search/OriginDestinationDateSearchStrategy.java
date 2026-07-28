package in.neelporiya.airline.search;

import in.neelporiya.airline.model.FlightInstance;
import in.neelporiya.airline.model.Passenger;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class OriginDestinationDateSearchStrategy implements FlightSearchStrategy {

    @Override
    public List<FlightInstance> search(List<FlightInstance> instances, String origin, String destination, LocalDate date) {
        // EXTENSIBILITY: layovers, flexible dates, and airline filters can be introduced as alternative strategies.
        Objects.requireNonNull(instances, "instances");
        Passenger.requireText(origin, "origin");
        Passenger.requireText(destination, "destination");
        Objects.requireNonNull(date, "date");
        return instances.stream()
                .filter(instance -> instance.getFlight().getOrigin().equals(origin))
                .filter(instance -> instance.getFlight().getDestination().equals(destination))
                .filter(instance -> instance.getFlightDate().equals(date))
                .sorted(Comparator.comparing(instance -> instance.getFlight().getFlightNumber()))
                .toList();
    }
}
