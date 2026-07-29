package in.neelporiya.ridesharing;

import java.time.Duration;

public class DistanceBasedPricingStrategy implements PricingStrategy {

    private final long baseFareCents;
    private final long centsPerDistanceUnit;
    private final long centsPerMinute;

    public DistanceBasedPricingStrategy(long baseFareCents, long centsPerDistanceUnit, long centsPerMinute) {
        if (baseFareCents < 0 || centsPerDistanceUnit < 0 || centsPerMinute < 0) {
            throw new IllegalArgumentException("pricing inputs cannot be negative");
        }
        this.baseFareCents = baseFareCents;
        this.centsPerDistanceUnit = centsPerDistanceUnit;
        this.centsPerMinute = centsPerMinute;
    }

    @Override
    public Fare calculate(Ride ride) {
        long distance = ride.getPickup().distanceUnitsTo(ride.getDrop());
        long minutes = 0;
        if (ride.getStartedAt() != null && ride.getCompletedAt() != null) {
            minutes = Math.max(0, Duration.between(ride.getStartedAt(), ride.getCompletedAt()).toMinutes());
        }
        long cents = baseFareCents + distance * centsPerDistanceUnit + minutes * centsPerMinute;
        return new Fare(Money.cents(cents), distance, minutes);
    }
}
