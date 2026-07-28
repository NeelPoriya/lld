package in.neelporiya.restaurant.billing;

import in.neelporiya.restaurant.Order;

public class NoDiscountStrategy implements DiscountStrategy {
    @Override
    public long discountCents(Order order, long subtotalCents) {
        return 0;
    }
}
