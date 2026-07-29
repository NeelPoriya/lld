package in.neelporiya.fooddelivery;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Delivery fee = base + perUnitDistance * distance; tax = subtotal * rate. All money is rounded to
 * two decimals, HALF_UP.
 */
public class StandardPricing implements PricingStrategy {

    private final BigDecimal baseFee;
    private final BigDecimal perDistanceFee;
    private final BigDecimal taxRate;

    public StandardPricing(BigDecimal baseFee, BigDecimal perDistanceFee, BigDecimal taxRate) {
        this.baseFee = baseFee;
        this.perDistanceFee = perDistanceFee;
        this.taxRate = taxRate;
    }

    @Override
    public Bill price(Cart cart, double distance) {
        BigDecimal subtotal = money(cart.subtotal());
        BigDecimal deliveryFee = money(baseFee.add(perDistanceFee.multiply(BigDecimal.valueOf(distance))));
        BigDecimal tax = money(subtotal.multiply(taxRate));
        BigDecimal total = money(subtotal.add(deliveryFee).add(tax));
        return new Bill(subtotal, deliveryFee, tax, total);
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
