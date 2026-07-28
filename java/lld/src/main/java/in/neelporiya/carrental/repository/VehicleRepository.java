package in.neelporiya.carrental.repository;

import in.neelporiya.carrental.model.Vehicle;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class VehicleRepository {

    private final ConcurrentMap<String, Vehicle> vehiclesById = new ConcurrentHashMap<>();

    public void save(Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "vehicle");
        // DESIGN PATTERN: Repository hides the storage choice from CarRentalService.
        vehiclesById.put(vehicle.getId(), vehicle);
    }

    public Optional<Vehicle> findById(String id) {
        return Optional.ofNullable(vehiclesById.get(id));
    }

    public List<Vehicle> findAll() {
        return List.copyOf(vehiclesById.values());
    }
}
