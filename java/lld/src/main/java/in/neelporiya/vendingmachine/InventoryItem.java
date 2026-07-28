package in.neelporiya.vendingmachine;

import java.util.Objects;

/**
 * One shelf slot: product + count.
 *
 * <p>// CONCURRENCY: Quantity is a plain int because every read/write is guarded by the
 * vending-machine {@code ReentrantLock}. Mixing AtomicInteger with a separate state lock would not
 * make the whole transaction atomic; one lock around state + inventory + change is clearer here.
 */
public class InventoryItem {

    private final Product product;
    private int quantity; // guarded by VendingMachine.lock

    public InventoryItem(Product product, int quantity) {
        this.product = Objects.requireNonNull(product, "product");
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isInStock() {
        return quantity > 0;
    }

    void decrement() {
        if (quantity <= 0) {
            throw new OutOfStockException("Product is out of stock: " + product.code());
        }
        quantity--;
    }
}
