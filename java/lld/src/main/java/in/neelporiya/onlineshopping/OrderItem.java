package in.neelporiya.onlineshopping;

import java.util.Objects;

/** Snapshot of a cart line at checkout time. */
public record OrderItem(String productId, String productName, int quantity, long unitPriceCents) {

    public OrderItem {
        Objects.requireNonNull(productId, "productId");
        Objects.requireNonNull(productName, "productName");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        if (unitPriceCents < 0) {
            throw new IllegalArgumentException("unitPriceCents must be >= 0");
        }
    }

    public long lineTotalCents() {
        return unitPriceCents * quantity;
    }
}
