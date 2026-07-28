package in.neelporiya.parkinglot.vehicle;

import java.util.Objects;

/**
 * Base type for every vehicle that can enter the lot.
 *
 * <p>We model concrete vehicles ({@link Car}, {@link Motorcycle}, {@link Truck}) as subclasses so
 * behaviour/attributes can diverge later (e.g. an electric car needing a charging spot) without
 * touching call sites. The {@link VehicleType} is the coarse discriminator used for spot fitment
 * and pricing.
 */
public abstract class Vehicle {

    private final String licensePlate;
    private final VehicleType type;

    protected Vehicle(String licensePlate, VehicleType type) {
        this.licensePlate = Objects.requireNonNull(licensePlate, "licensePlate");
        this.type = Objects.requireNonNull(type, "type");
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public VehicleType getType() {
        return type;
    }

    // Identity is the plate: two Car objects for the same plate are the same vehicle.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle other)) return false;
        return licensePlate.equals(other.licensePlate);
    }

    @Override
    public int hashCode() {
        return licensePlate.hashCode();
    }

    @Override
    public String toString() {
        return type + "(" + licensePlate + ")";
    }
}
