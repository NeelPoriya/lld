package in.neelporiya.ridesharing;

/**
 * Fare money represented as integer cents.
 *
 * <p>// INTERVIEW INSIGHT: fares are never stored as {@code double}. Integer cents avoid rounding
 * drift and make assertions exact.
 */
public record Money(long cents) {

    public Money {
        if (cents < 0) {
            throw new IllegalArgumentException("money cannot be negative");
        }
    }

    public static Money cents(long cents) {
        return new Money(cents);
    }
}
