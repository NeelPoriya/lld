package in.neelporiya.onlineshopping;

import java.util.Optional;

/** // DESIGN PATTERN: Repository — carts can later move from memory to Redis/DB unchanged. */
public interface CartRepository {
    void save(Cart cart);

    Optional<Cart> findById(String id);
}
