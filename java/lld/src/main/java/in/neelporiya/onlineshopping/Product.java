package in.neelporiya.onlineshopping;

import java.util.Objects;
import java.util.Set;

/**
 * Immutable catalog entry.
 *
 * <p>// INTERVIEW INSIGHT: money is stored as integer cents, never {@code double}. Floating-point
 * currency silently drifts; integer cents make totals deterministic and easy to assert in tests.
 */
public record Product(String id, String name, String description, long priceCents, Set<String> keywords) {

    public Product {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(keywords, "keywords");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (priceCents < 0) {
            throw new IllegalArgumentException("priceCents must be >= 0");
        }
        keywords = Set.copyOf(keywords);
    }
}
