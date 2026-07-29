package in.neelporiya.onlineshopping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Customer cart with a small Builder for readable construction in interviews and tests.
 *
 * <p>// DESIGN PATTERN: Builder — callers can assemble a cart line-by-line without exposing the
 * internal map representation.
 */
public class Cart {

    private final String id;
    private final String customerId;
    private final Map<String, CartItem> items = new LinkedHashMap<>();
    private boolean closedForCheckout;

    private Cart(Builder builder) {
        this.id = builder.id;
        this.customerId = builder.customerId;
        this.items.putAll(builder.items);
    }

    public synchronized void addProduct(Product product, int quantity) {
        ensureOpen();
        Objects.requireNonNull(product, "product");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        items.merge(product.id(), new CartItem(product, quantity),
                (oldItem, newItem) -> new CartItem(product, oldItem.quantity() + newItem.quantity()));
    }

    synchronized boolean tryCloseForCheckout() {
        if (closedForCheckout) {
            return false;
        }
        closedForCheckout = true;
        return true;
    }

    synchronized void reopenAfterFailedCheckout() {
        closedForCheckout = false;
    }

    public synchronized List<CartItem> items() {
        return List.copyOf(items.values());
    }

    public synchronized Map<String, Integer> quantitiesByProductId() {
        Map<String, Integer> quantities = new LinkedHashMap<>();
        for (CartItem item : items.values()) {
            quantities.put(item.product().id(), item.quantity());
        }
        return quantities;
    }

    private void ensureOpen() {
        if (closedForCheckout) {
            throw new CartAlreadyCheckedOutException("Cart is already checked out: " + id);
        }
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public static Builder builder(String id, String customerId) {
        return new Builder(id, customerId);
    }

    public static final class Builder {
        private final String id;
        private final String customerId;
        private final Map<String, CartItem> items = new LinkedHashMap<>();

        private Builder(String id, String customerId) {
            this.id = Objects.requireNonNull(id, "id");
            this.customerId = Objects.requireNonNull(customerId, "customerId");
        }

        public Builder addProduct(Product product, int quantity) {
            Objects.requireNonNull(product, "product");
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be > 0");
            }
            items.merge(product.id(), new CartItem(product, quantity),
                    (oldItem, newItem) -> new CartItem(product, oldItem.quantity() + newItem.quantity()));
            return this;
        }

        public Cart build() {
            return new Cart(this);
        }
    }
}
