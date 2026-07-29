package in.neelporiya.ridesharing;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DriverRepository implements Repository<Driver> {

    private final ConcurrentMap<String, Driver> drivers = new ConcurrentHashMap<>();

    @Override
    public void save(Driver driver) {
        drivers.put(driver.getId(), driver);
    }

    @Override
    public Optional<Driver> findById(String id) {
        return Optional.ofNullable(drivers.get(id));
    }

    @Override
    public Collection<Driver> findAll() {
        return drivers.values();
    }
}
