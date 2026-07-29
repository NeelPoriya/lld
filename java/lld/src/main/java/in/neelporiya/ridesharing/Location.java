package in.neelporiya.ridesharing;

/**
 * Immutable grid point used by both matching and pricing.
 *
 * <p>// TESTABILITY: integer coordinates make all tests deterministic. Matching uses squared
 * Euclidean distance so it never needs floating point math.
 */
public record Location(int x, int y) {

    public long squaredDistanceTo(Location other) {
        long dx = (long) x - other.x;
        long dy = (long) y - other.y;
        return dx * dx + dy * dy;
    }

    public long distanceUnitsTo(Location other) {
        return ceilSqrt(squaredDistanceTo(other));
    }

    private static long ceilSqrt(long value) {
        long low = 0;
        long high = Math.min(value, 3_037_000_499L);
        while (low <= high) {
            long mid = low + (high - low) / 2;
            long square = mid * mid;
            if (square == value) {
                return mid;
            }
            if (square < value) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return low;
    }
}
