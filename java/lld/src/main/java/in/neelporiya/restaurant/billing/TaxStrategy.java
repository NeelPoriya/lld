package in.neelporiya.restaurant.billing;

import in.neelporiya.restaurant.Order;

/** // DESIGN PATTERN: Strategy — tax calculation is pluggable per jurisdiction. */
public interface TaxStrategy {
    long taxCents(Order order, long subtotalCents);
}
