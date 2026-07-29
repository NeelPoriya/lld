package in.neelporiya.fooddelivery;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A customer's basket for ONE restaurant. Enforces the single-restaurant rule (Swiggy carts don't mix
 * kitchens) and accumulates quantities.
 */
public class Cart {

    private final String restaurantId;
    private final Map<MenuItem, Integer> lines = new LinkedHashMap<>();

    public Cart(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public Cart add(MenuItem item, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        lines.merge(item, quantity, Integer::sum);
        return this;
    }

    public void remove(MenuItem item) {
        lines.remove(item);
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public List<OrderLine> toOrderLines() {
        List<OrderLine> orderLines = new ArrayList<>();
        lines.forEach((item, qty) -> orderLines.add(new OrderLine(item, qty)));
        return orderLines;
    }

    public BigDecimal subtotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<MenuItem, Integer> line : lines.entrySet()) {
            total = total.add(line.getKey().price().multiply(BigDecimal.valueOf(line.getValue())));
        }
        return total;
    }
}
