package in.neelporiya.restaurant.billing;

import in.neelporiya.restaurant.Order;

public class NoTaxStrategy implements TaxStrategy {
    @Override
    public long taxCents(Order order, long subtotalCents) {
        return 0;
    }
}
