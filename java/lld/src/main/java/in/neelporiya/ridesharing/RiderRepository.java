package in.neelporiya.ridesharing;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RiderRepository implements Repository<Rider> {

    private final ConcurrentMap<String, Rider> riders = new ConcurrentHashMap<>();

    @Override
    public void save(Rider rider) {
        riders.put(rider.id(), rider);
    }

    @Override
    public Optional<Rider> findById(String id) {
        return Optional.ofNullable(riders.get(id));
    }

    @Override
    public Collection<Rider> findAll() {
        return riders.values();
    }
}
