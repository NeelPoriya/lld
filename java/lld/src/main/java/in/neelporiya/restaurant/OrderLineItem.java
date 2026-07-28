package in.neelporiya.restaurant;

import java.util.Objects;

/** Snapshot of an ordered menu item and quantity. */
public class OrderLineItem {
    private final MenuItem menuItem;
    private final int quantity;

    public OrderLineItem(MenuItem menuItem, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        this.menuItem = Objects.requireNonNull(menuItem, "menuItem");
        this.quantity = quantity;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public long lineTotalCents() {
        return menuItem.getPriceCents() * quantity;
    }
}
