package in.neelporiya.ridesharing;

/**
 * // DESIGN PATTERN: Strategy. Pricing is isolated from ride orchestration so distance, time, and
 * surge formulas can evolve independently.
 */
public interface PricingStrategy {

    Fare calculate(Ride ride);
}
