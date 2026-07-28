package in.neelporiya.restaurant.billing;

import in.neelporiya.restaurant.Order;

/** // DESIGN PATTERN: Strategy — service charge or customer tip policy is swappable. */
public interface TipStrategy {
    long tipCents(Order order, long subtotalCents);
}
