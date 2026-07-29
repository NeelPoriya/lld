package in.neelporiya.onlineshopping;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Simple deterministic case-insensitive search over name, description, and keywords. */
public class KeywordCatalogSearchStrategy implements CatalogSearchStrategy {

    @Override
    public List<Product> search(Collection<Product> products, String query) {
        String needle = query.toLowerCase(Locale.ROOT);
        return products.stream()
                .filter(product -> matches(product, needle))
                .sorted(Comparator.comparing(Product::id))
                .toList();
    }

    private boolean matches(Product product, String needle) {
        return product.name().toLowerCase(Locale.ROOT).contains(needle)
                || product.description().toLowerCase(Locale.ROOT).contains(needle)
                || product.keywords().stream().anyMatch(keyword -> keyword.toLowerCase(Locale.ROOT).contains(needle));
    }
}
