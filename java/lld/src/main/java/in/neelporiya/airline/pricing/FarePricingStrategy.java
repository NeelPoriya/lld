package in.neelporiya.airline.pricing;

import in.neelporiya.airline.model.FlightInstance;
import in.neelporiya.airline.model.Seat;

import java.math.BigDecimal;

public interface FarePricingStrategy {

    BigDecimal price(FlightInstance flightInstance, Seat seat);
}
