package in.neelporiya.carrental.model;

import java.util.Objects;

public final class Vehicle {

    private final String id;
    private final VehicleType type;
    private final String licensePlate;
    private final String storeId;
    private final String make;
    private final String model;

    Vehicle(String id, VehicleType type, String licensePlate, String storeId, String make, String model) {
        this.id = requireText(id, "id");
        this.type = Objects.requireNonNull(type, "type");
        this.licensePlate = requireText(licensePlate, "licensePlate");
        this.storeId = requireText(storeId, "storeId");
        this.make = requireText(make, "make");
        this.model = requireText(model, "model");
    }

    public String getId() {
        return id;
    }

    public VehicleType getType() {
        return type;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getStoreId() {
        return storeId;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
