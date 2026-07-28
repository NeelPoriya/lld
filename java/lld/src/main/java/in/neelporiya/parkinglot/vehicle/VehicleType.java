package in.neelporiya.parkinglot.vehicle;

/**
 * The kinds of vehicles the lot supports.
 *
 * <p>Kept as an enum (not a class hierarchy) because the <em>set</em> of types is small and
 * closed; fitment rules live in {@link in.neelporiya.parkinglot.spot.ParkingSpotType}.
 */
public enum VehicleType {
    MOTORCYCLE,
    CAR,
    TRUCK
}
