package in.neelporiya.restaurant.billing;

import in.neelporiya.restaurant.Order;

public class FixedTipStrategy implements TipStrategy {
    private final long tipCents;

    public FixedTipStrategy(long tipCents) {
        if (tipCents < 0) {
            throw new IllegalArgumentException("tipCents cannot be negative");
        }
        this.tipCents = tipCents;
    }

    @Override
    public long tipCents(Order order, long subtotalCents) {
        return tipCents;
    }
}
