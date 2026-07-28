package in.neelporiya.hotelmanagement.pricing;

import in.neelporiya.hotelmanagement.model.Room;
import in.neelporiya.hotelmanagement.model.StayRange;

import java.util.Objects;

public final class TaxDiscountPricingStrategy implements PricingStrategy {

    private final PricingStrategy baseStrategy;
    private final int taxBasisPoints;
    private final int discountBasisPoints;

    public TaxDiscountPricingStrategy(PricingStrategy baseStrategy, int taxBasisPoints, int discountBasisPoints) {
        if (taxBasisPoints < 0 || discountBasisPoints < 0 || discountBasisPoints > 10_000) {
            throw new IllegalArgumentException("basis points must be non-negative and discount <= 100%");
        }
        this.baseStrategy = Objects.requireNonNull(baseStrategy, "baseStrategy");
        this.taxBasisPoints = taxBasisPoints;
        this.discountBasisPoints = discountBasisPoints;
    }

    @Override
    public long calculateCents(Room room, StayRange range) {
        // EXTENSIBILITY: Decorator-style pricing composes tax/discount without editing base nightly pricing.
        long base = baseStrategy.calculateCents(room, range);
        long afterDiscount = base - (base * discountBasisPoints / 10_000);
        return afterDiscount + (afterDiscount * taxBasisPoints / 10_000);
    }
}
