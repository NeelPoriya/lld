package in.neelporiya.ridesharing;

import java.util.Collection;
import java.util.Optional;

/**
 * // DESIGN PATTERN: Repository. The service depends on collection-like storage, not a database.
 */
public interface Repository<T> {

    void save(T value);

    Optional<T> findById(String id);

    Collection<T> findAll();
}
