package in.neelporiya.onlineshopping;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryCartRepository implements CartRepository {

    private final ConcurrentMap<String, Cart> carts = new ConcurrentHashMap<>();

    @Override
    public void save(Cart cart) {
        carts.put(cart.getId(), cart);
    }

    @Override
    public Optional<Cart> findById(String id) {
        return Optional.ofNullable(carts.get(id));
    }
}
