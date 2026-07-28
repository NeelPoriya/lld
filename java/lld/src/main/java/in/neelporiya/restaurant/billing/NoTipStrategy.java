package in.neelporiya.restaurant.billing;

import in.neelporiya.restaurant.Order;

public class NoTipStrategy implements TipStrategy {
    @Override
    public long tipCents(Order order, long subtotalCents) {
        return 0;
    }
}
