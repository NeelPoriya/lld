package in.neelporiya.airline.search;

import in.neelporiya.airline.model.FlightInstance;

import java.time.LocalDate;
import java.util.List;

public interface FlightSearchStrategy {

    List<FlightInstance> search(List<FlightInstance> instances, String origin, String destination, LocalDate date);
}
