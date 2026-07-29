package in.neelporiya.onlineshopping;

import java.util.Optional;

/** // DESIGN PATTERN: Repository — order persistence is not baked into the domain object. */
public interface OrderRepository {
    void save(Order order);

    Optional<Order> findById(String id);
}
