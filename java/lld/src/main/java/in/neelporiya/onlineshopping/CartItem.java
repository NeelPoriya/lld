package in.neelporiya.onlineshopping;

import java.util.Objects;

/** One product line in a shopping cart. */
public record CartItem(Product product, int quantity) {

    public CartItem {
        Objects.requireNonNull(product, "product");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
    }

    public long lineTotalCents() {
        return product.priceCents() * quantity;
    }
}
