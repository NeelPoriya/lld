package in.neelporiya.restaurant.repository;

import in.neelporiya.restaurant.Order;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** // DESIGN PATTERN: Repository — gives service a persistence seam for tests or databases later. */
public class OrderRepository {
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public void save(Order order) {
        orders.put(order.getId(), order);
    }

    public Optional<Order> findById(String id) {
        return Optional.ofNullable(orders.get(id));
    }

    public Collection<Order> findAll() {
        return Map.copyOf(orders).values();
    }
}
