package in.neelporiya.carrental.model;

import java.util.Objects;

public final class VehicleFactory {

    private VehicleFactory() {
    }

    public static Vehicle create(String id, VehicleType type, String licensePlate, String storeId, String make, String model) {
        // DESIGN PATTERN: Factory centralizes vehicle construction as new type-specific defaults appear.
        return new Vehicle(id, Objects.requireNonNull(type, "type"), licensePlate, storeId, make, model);
    }
}
