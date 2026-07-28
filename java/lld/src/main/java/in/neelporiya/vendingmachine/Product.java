package in.neelporiya.vendingmachine;

import java.util.Objects;

/** Immutable catalog entry. Quantity lives in {@link InventoryItem}. */
public record Product(String code, String name, int priceCents) {
    public Product {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(name, "name");
        if (code.isBlank()) {
            throw new IllegalArgumentException("Product code cannot be blank");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be blank");
        }
        if (priceCents <= 0) {
            throw new IllegalArgumentException("Product price must be positive");
        }
    }
}
