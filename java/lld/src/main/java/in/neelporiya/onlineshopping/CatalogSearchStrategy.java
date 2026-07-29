package in.neelporiya.onlineshopping;

import java.util.Collection;
import java.util.List;

/** // DESIGN PATTERN: Strategy — keyword search can be replaced by fuzzy, faceted, or ranked search. */
public interface CatalogSearchStrategy {
    List<Product> search(Collection<Product> products, String query);
}
