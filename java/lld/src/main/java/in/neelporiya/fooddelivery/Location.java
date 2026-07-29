package in.neelporiya.fooddelivery;

/**
 * A simple 2-D coordinate.
 *
 * <p>// TESTABILITY: distance is plain Euclidean on a flat plane — deterministic and easy to assert.
 * Swap in Haversine for real lat/long without touching any caller.
 */
public record Location(double x, double y) {

    public double distanceTo(Location other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
