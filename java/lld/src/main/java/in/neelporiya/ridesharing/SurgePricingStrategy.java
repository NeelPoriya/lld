package in.neelporiya.ridesharing;

import java.util.Objects;

/**
 * Optional decorator for high-demand periods. A multiplier of 150 means 1.5x.
 */
public class SurgePricingStrategy implements PricingStrategy {

    private final PricingStrategy delegate;
    private final int multiplierPercent;

    public SurgePricingStrategy(PricingStrategy delegate, int multiplierPercent) {
        if (multiplierPercent < 100) {
            throw new IllegalArgumentException("surge multiplier must be at least 100 percent");
        }
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.multiplierPercent = multiplierPercent;
    }

    @Override
    public Fare calculate(Ride ride) {
        Fare base = delegate.calculate(ride);
        long surgedCents = Math.multiplyExact(base.amount().cents(), multiplierPercent) / 100;
        return new Fare(Money.cents(surgedCents), base.distanceUnits(), base.durationMinutes());
    }
}
