package in.neelporiya.coffeevendingmachine;

/**
 * Value object describing the outcome of a brew attempt.
 *
 * <p>// INTERVIEW INSIGHT: "out of milk" and "all outlets busy" are <em>expected</em> outcomes of a
 * normal operation, not exceptional programmer errors. Modelling them as a returned result (instead
 * of throwing) keeps the happy path clean and forces callers to handle the failure explicitly.
 */
public record BrewResult(boolean success, String beverage, String failureReason) {

    public static BrewResult ok(String beverage) {
        return new BrewResult(true, beverage, null);
    }

    public static BrewResult fail(String beverage, String reason) {
        return new BrewResult(false, beverage, reason);
    }
}
