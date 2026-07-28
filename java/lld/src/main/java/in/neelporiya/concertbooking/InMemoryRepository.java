package in.neelporiya.concertbooking;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRepository<T extends Identifiable> implements Repository<T> {

    private final ConcurrentHashMap<String, T> entities = new ConcurrentHashMap<>();

    @Override
    public void save(T entity) {
        entities.put(entity.getId(), entity);
    }

    @Override
    public Optional<T> findById(String id) {
        return Optional.ofNullable(entities.get(id));
    }

    @Override
    public List<T> findAll() {
        return List.copyOf(entities.values());
    }
}
