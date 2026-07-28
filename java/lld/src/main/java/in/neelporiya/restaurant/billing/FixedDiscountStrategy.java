package in.neelporiya.restaurant.billing;

import in.neelporiya.restaurant.Order;

/** Fixed discount capped at the subtotal so totals never go below zero. */
public class FixedDiscountStrategy implements DiscountStrategy {
    private final long discountCents;

    public FixedDiscountStrategy(long discountCents) {
        if (discountCents < 0) {
            throw new IllegalArgumentException("discountCents cannot be negative");
        }
        this.discountCents = discountCents;
    }

    @Override
    public long discountCents(Order order, long subtotalCents) {
        return Math.min(discountCents, subtotalCents);
    }
}
