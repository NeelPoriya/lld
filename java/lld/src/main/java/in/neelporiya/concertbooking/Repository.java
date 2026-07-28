package in.neelporiya.concertbooking;

import java.util.List;
import java.util.Optional;

/**
 * // DESIGN PATTERN: Repository hides storage. Tests use in-memory; a real system can replace it
 * with SQL without changing BookingService.
 *
 * <p>// EXTENSIBILITY: swapping storage is an interface implementation decision, not a service
 * rewrite.
 */
public interface Repository<T extends Identifiable> {
    void save(T entity);

    Optional<T> findById(String id);

    List<T> findAll();
}
