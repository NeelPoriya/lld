package in.neelporiya.parkinglot.strategy;

import in.neelporiya.parkinglot.ParkingFloor;
import in.neelporiya.parkinglot.spot.ParkingSpot;
import in.neelporiya.parkinglot.vehicle.Vehicle;

import java.util.List;
import java.util.Optional;

/**
 * // DESIGN PATTERN: Strategy.
 *
 * <p>Encapsulates the policy "given the floors and a vehicle, which spot do we give it?". Swapping
 * "nearest to entrance" for "spread load across floors" or "cheapest spot first" means writing a
 * new implementation — {@code ParkingLot} never changes (Open/Closed).
 *
 * <p>// CONCURRENCY CONTRACT: an implementation MUST return a spot it has already
 * <strong>atomically claimed</strong> (via {@link ParkingSpot#tryOccupy}). Fusing "select" and
 * "claim" inside the strategy is what makes concurrent parking safe — there is no window between
 * choosing a spot and taking it in which another thread could steal it.
 */
public interface SpotAssignmentStrategy {

    /**
     * @return a spot already occupied by {@code vehicle}, or empty if none is available.
     */
    Optional<ParkingSpot> assignSpot(List<ParkingFloor> floors, Vehicle vehicle);
}
