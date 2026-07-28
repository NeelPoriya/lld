package in.neelporiya.parkinglot;

import in.neelporiya.parkinglot.spot.ParkingSpot;
import in.neelporiya.parkinglot.spot.ParkingSpotType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A floor is simply an ordered collection of spots.
 *
 * <p>// CONCURRENCY: the {@code spots} list is <em>structurally immutable</em> (defensive copy +
 * {@code List.copyOf}). The mutable state (occupancy) lives inside each {@link ParkingSpot} and is
 * guarded there by an {@code AtomicReference}. So the floor object itself needs no locking — it is
 * effectively immutable and freely shared across threads.
 */
public class ParkingFloor {

    private final int floorNumber;
    private final List<ParkingSpot> spots;

    public ParkingFloor(int floorNumber, List<ParkingSpot> spots) {
        this.floorNumber = floorNumber;
        this.spots = List.copyOf(Objects.requireNonNull(spots, "spots"));
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public List<ParkingSpot> getSpots() {
        return spots; // already unmodifiable
    }

    /** Live count of free spots grouped by type (for display boards). */
    public Map<ParkingSpotType, Long> freeSpotCountByType() {
        return spots.stream()
                .filter(ParkingSpot::isFree)
                .collect(Collectors.groupingBy(ParkingSpot::getType, Collectors.counting()));
    }
}
