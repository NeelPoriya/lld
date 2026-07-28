package in.neelporiya.parkinglot.spot;

import in.neelporiya.parkinglot.vehicle.VehicleType;

import java.util.Set;

/**
 * Physical spot sizes and the fitment rule "which vehicle types fit here".
 *
 * <p>Fitment is intentionally asymmetric and realistic:
 * <ul>
 *   <li>a MOTORCYCLE spot holds only motorcycles,</li>
 *   <li>a COMPACT spot holds motorcycles and cars,</li>
 *   <li>a LARGE spot holds anything.</li>
 * </ul>
 *
 * // INTERVIEW INSIGHT: encoding fitment as data on the enum (a {@code Set<VehicleType>}) keeps the
 * rule in ONE place. The alternative — {@code if/else} chains scattered across the codebase — is the
 * classic thing interviewers ding you for.
 */
public enum ParkingSpotType {
    MOTORCYCLE(Set.of(VehicleType.MOTORCYCLE)),
    COMPACT(Set.of(VehicleType.MOTORCYCLE, VehicleType.CAR)),
    LARGE(Set.of(VehicleType.MOTORCYCLE, VehicleType.CAR, VehicleType.TRUCK));

    private final Set<VehicleType> acceptedVehicleTypes;

    ParkingSpotType(Set<VehicleType> acceptedVehicleTypes) {
        this.acceptedVehicleTypes = acceptedVehicleTypes;
    }

    public boolean canFit(VehicleType vehicleType) {
        return acceptedVehicleTypes.contains(vehicleType);
    }
}
