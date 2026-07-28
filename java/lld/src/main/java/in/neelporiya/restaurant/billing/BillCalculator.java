package in.neelporiya.restaurant.billing;

import in.neelporiya.restaurant.Bill;
import in.neelporiya.restaurant.Order;

import java.util.Objects;

/** // DESIGN PATTERN: Strategy composition — tax, discount and tip can vary independently. */
public class BillCalculator {
    private final TaxStrategy taxStrategy;
    private final DiscountStrategy discountStrategy;
    private final TipStrategy tipStrategy;

    public BillCalculator(TaxStrategy taxStrategy, DiscountStrategy discountStrategy, TipStrategy tipStrategy) {
        this.taxStrategy = Objects.requireNonNull(taxStrategy, "taxStrategy");
        this.discountStrategy = Objects.requireNonNull(discountStrategy, "discountStrategy");
        this.tipStrategy = Objects.requireNonNull(tipStrategy, "tipStrategy");
    }

    public static BillCalculator defaults() {
        return new BillCalculator(new NoTaxStrategy(), new NoDiscountStrategy(), new NoTipStrategy());
    }

    public Bill calculate(Order order) {
        long subtotal = order.subtotalCents();
        return new Bill(
                order.getId(),
                subtotal,
                taxStrategy.taxCents(order, subtotal),
                discountStrategy.discountCents(order, subtotal),
                tipStrategy.tipCents(order, subtotal));
    }
}
