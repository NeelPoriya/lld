package in.neelporiya.fooddelivery;

import java.math.BigDecimal;

/** An immutable menu entry. Availability is tracked by the {@link Menu}, not the item itself. */
public record MenuItem(String id, String name, BigDecimal price) {

    public MenuItem {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("price must be non-negative");
        }
    }
}
