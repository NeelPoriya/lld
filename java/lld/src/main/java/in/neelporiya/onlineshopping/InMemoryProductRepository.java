package in.neelporiya.onlineshopping;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryProductRepository implements ProductRepository {

    private final ConcurrentMap<String, Product> products = new ConcurrentHashMap<>();

    @Override
    public void save(Product product) {
        products.put(product.id(), product);
    }

    @Override
    public Optional<Product> findById(String id) {
        return Optional.ofNullable(products.get(id));
    }

    @Override
    public Collection<Product> findAll() {
        return products.values();
    }
}
