package in.neelporiya.ridesharing;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RideRepository implements Repository<Ride> {

    private final ConcurrentMap<String, Ride> rides = new ConcurrentHashMap<>();

    @Override
    public void save(Ride ride) {
        rides.put(ride.getId(), ride);
    }

    @Override
    public Optional<Ride> findById(String id) {
        return Optional.ofNullable(rides.get(id));
    }

    @Override
    public Collection<Ride> findAll() {
        return rides.values();
    }
}
