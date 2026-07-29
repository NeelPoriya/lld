package in.neelporiya.onlineshopping;

import java.util.List;
import java.util.Objects;

/** Percentage discount followed by tax, using integer arithmetic only. */
public class PercentageDiscountTaxPricingStrategy implements PricingStrategy {

    private final int discountPercent;
    private final int taxBasisPoints;

    public PercentageDiscountTaxPricingStrategy(int discountPercent, int taxBasisPoints) {
        if (discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("discountPercent must be 0..100");
        }
        if (taxBasisPoints < 0) {
            throw new IllegalArgumentException("taxBasisPoints must be >= 0");
        }
        this.discountPercent = discountPercent;
        this.taxBasisPoints = taxBasisPoints;
    }

    @Override
    public long totalCents(List<CartItem> items) {
        Objects.requireNonNull(items, "items");
        long subtotal = items.stream().mapToLong(CartItem::lineTotalCents).sum();
        long discount = subtotal * discountPercent / 100;
        long afterDiscount = subtotal - discount;
        long tax = afterDiscount * taxBasisPoints / 10_000;
        return afterDiscount + tax;
    }
}
