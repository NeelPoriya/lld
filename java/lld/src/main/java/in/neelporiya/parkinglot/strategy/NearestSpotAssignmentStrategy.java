package in.neelporiya.parkinglot.strategy;

import in.neelporiya.parkinglot.ParkingFloor;
import in.neelporiya.parkinglot.spot.ParkingSpot;
import in.neelporiya.parkinglot.vehicle.Vehicle;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Picks the spot closest to the entrance, breaking ties by preferring the <em>smallest</em> spot
 * that fits.
 *
 * <p>// INTERVIEW INSIGHT: preferring the smallest fitting spot (COMPACT over LARGE for a car) keeps
 * LARGE spots free for trucks that genuinely need them — a small "product sense" detail that stands
 * out.
 *
 * <p>// CONCURRENCY: we build an ordered candidate list, then walk it attempting an atomic
 * {@link ParkingSpot#tryOccupy}. The first successful claim wins. If a concurrent thread grabbed our
 * first choice a nanosecond earlier, {@code tryOccupy} returns false and we transparently fall
 * through to the next-best spot. This retry loop is lock-free.
 */
public class NearestSpotAssignmentStrategy implements SpotAssignmentStrategy {

    private static final Comparator<ParkingSpot> NEAREST_THEN_SMALLEST =
            Comparator.comparingInt(ParkingSpot::getDistanceFromEntrance)
                    .thenComparing(spot -> spot.getType().ordinal());

    @Override
    public Optional<ParkingSpot> assignSpot(List<ParkingFloor> floors, Vehicle vehicle) {
        List<ParkingSpot> candidates = floors.stream()
                .flatMap(floor -> floor.getSpots().stream())
                .filter(spot -> spot.canFit(vehicle))
                .sorted(NEAREST_THEN_SMALLEST)
                .toList();

        for (ParkingSpot spot : candidates) {
            if (spot.tryOccupy(vehicle)) {
                return Optional.of(spot);
            }
            // Lost the CAS race for this spot — a concurrent gate took it. Try the next candidate.
        }
        return Optional.empty();
    }
}
