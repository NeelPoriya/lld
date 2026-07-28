package in.neelporiya.parkinglot.spot;

import in.neelporiya.parkinglot.vehicle.Vehicle;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A single physical parking spot.
 *
 * <p>// CONCURRENCY: This is the heart of the lot's thread-safety. The occupying vehicle is held in
 * an {@link AtomicReference}. Occupying a spot is a single {@code compareAndSet(null, vehicle)} —
 * an atomic "claim it only if it is still free". When two gate threads race for the same free spot,
 * exactly one {@code compareAndSet} returns {@code true}; the loser sees {@code false} and moves on
 * to the next candidate. No locks, no {@code synchronized}, so unrelated spots never contend.
 *
 * <p>// INTERVIEW INSIGHT: The buggy version everyone writes first is
 * {@code if (spot.isFree()) spot.occupy(vehicle);} — the gap between the check and the act is the
 * race window. {@code compareAndSet} fuses check-and-act into one indivisible CPU instruction.
 */
public class ParkingSpot {

    private final String id;
    private final ParkingSpotType type;
    private final int floorNumber;

    /**
     * Lower = closer to the entrance/elevator. Used by the "nearest spot" assignment strategy.
     */
    private final int distanceFromEntrance;

    private final AtomicReference<Vehicle> occupant = new AtomicReference<>();

    public ParkingSpot(String id, ParkingSpotType type, int floorNumber, int distanceFromEntrance) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
        this.floorNumber = floorNumber;
        this.distanceFromEntrance = distanceFromEntrance;
    }

    public boolean canFit(Vehicle vehicle) {
        return type.canFit(vehicle.getType());
    }

    public boolean isFree() {
        return occupant.get() == null;
    }

    /**
     * Atomically claim this spot for {@code vehicle}.
     *
     * @return {@code true} if we won the spot, {@code false} if it did not fit or was taken by a
     *         concurrent thread first.
     */
    public boolean tryOccupy(Vehicle vehicle) {
        if (!canFit(vehicle)) {
            return false;
        }
        // CONCURRENCY: the whole thread-safety story is this one call.
        return occupant.compareAndSet(null, vehicle);
    }

    /**
     * Free the spot.
     *
     * @return the vehicle that was here, or {@code null} if it was already free.
     */
    public Vehicle vacate() {
        return occupant.getAndSet(null);
    }

    public Vehicle getOccupant() {
        return occupant.get();
    }

    public String getId() {
        return id;
    }

    public ParkingSpotType getType() {
        return type;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public int getDistanceFromEntrance() {
        return distanceFromEntrance;
    }

    @Override
    public String toString() {
        return "Spot(" + id + ", " + type + ", floor " + floorNumber + ")";
    }
}
