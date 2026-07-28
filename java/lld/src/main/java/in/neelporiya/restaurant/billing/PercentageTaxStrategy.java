package in.neelporiya.restaurant.billing;

import in.neelporiya.restaurant.Order;

/** Percentage strategy using basis points: 1000 = 10.00%. */
public class PercentageTaxStrategy implements TaxStrategy {
    private final int basisPoints;

    public PercentageTaxStrategy(int basisPoints) {
        if (basisPoints < 0) {
            throw new IllegalArgumentException("basisPoints cannot be negative");
        }
        this.basisPoints = basisPoints;
    }

    @Override
    public long taxCents(Order order, long subtotalCents) {
        return percentOf(subtotalCents, basisPoints);
    }

    static long percentOf(long amountCents, int basisPoints) {
        return (amountCents * basisPoints + 5_000) / 10_000;
    }
}
