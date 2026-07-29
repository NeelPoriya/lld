package in.neelporiya.ridesharing;

import java.util.Collection;
import java.util.Optional;

/**
 * // DESIGN PATTERN: Strategy. Different products can choose nearest, highest-rated, cheapest, or
 * pooled matching without changing the service facade.
 */
public interface MatchingStrategy {

    Optional<Driver> match(RideRequest request, Collection<Driver> drivers);
}
