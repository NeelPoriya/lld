package in.neelporiya.restaurant.billing;

import in.neelporiya.restaurant.Order;

/** // DESIGN PATTERN: Strategy — coupons, loyalty and happy-hour rules plug in here. */
public interface DiscountStrategy {
    long discountCents(Order order, long subtotalCents);
}
