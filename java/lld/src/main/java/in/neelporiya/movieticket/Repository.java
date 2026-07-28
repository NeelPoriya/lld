package in.neelporiya.movieticket;

import java.util.List;
import java.util.Optional;

/**
 * // DESIGN PATTERN: Repository keeps BookingService independent from storage details.
 * // EXTENSIBILITY: swap in SQL/Redis without changing the booking facade.
 */
public interface Repository<T extends Identifiable> {
    void save(T entity);

    Optional<T> findById(String id);

    List<T> findAll();
}
