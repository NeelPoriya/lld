package in.neelporiya.ridesharing;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

/**
 * Chooses the closest driver who can be atomically claimed.
 */
public class NearestDriverMatchingStrategy implements MatchingStrategy {

    @Override
    public Optional<Driver> match(RideRequest request, Collection<Driver> drivers) {
        return drivers.stream()
                .filter(Driver::isAvailable)
                .sorted(Comparator
                        .comparingLong((Driver driver) -> driver.getLocation().squaredDistanceTo(request.pickup()))
                        .thenComparing(Driver::getId))
                .filter(driver -> driver.tryClaim(request.rideId()))
                .findFirst();
    }
}
