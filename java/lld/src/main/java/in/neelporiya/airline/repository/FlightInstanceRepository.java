package in.neelporiya.airline.repository;

import in.neelporiya.airline.model.FlightInstance;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class FlightInstanceRepository {

    private final ConcurrentMap<String, FlightInstance> instancesById = new ConcurrentHashMap<>();

    public void save(FlightInstance instance) {
        Objects.requireNonNull(instance, "instance");
        instancesById.put(instance.getId(), instance);
    }

    public Optional<FlightInstance> findById(String id) {
        return Optional.ofNullable(instancesById.get(id));
    }

    public List<FlightInstance> findAll() {
        return List.copyOf(instancesById.values());
    }
}
