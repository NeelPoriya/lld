package in.neelporiya.onlineshopping;

import java.util.Collection;
import java.util.Optional;

/** // DESIGN PATTERN: Repository — hides storage behind collection-like methods. */
public interface ProductRepository {
    void save(Product product);

    Optional<Product> findById(String id);

    Collection<Product> findAll();
}
