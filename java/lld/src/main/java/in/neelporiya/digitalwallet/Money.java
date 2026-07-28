package in.neelporiya.digitalwallet;

import in.neelporiya.digitalwallet.exception.CurrencyMismatchException;

/**
 * // DESIGN PATTERN: Value Object. Immutable money as an integer count of minor units (cents/paise)
 * plus a currency.
 *
 * <p>// INTERVIEW INSIGHT: money is NEVER a {@code double}. 0.1 + 0.2 != 0.3 in binary floating
 * point, so currency math silently drifts. Integer minor units are exact; the currency tag prevents
 * accidentally adding USD to INR.
 */
public record Money(long minorUnits, Currency currency) {

    public static Money of(long minorUnits, Currency currency) {
        return new Money(minorUnits, currency);
    }

    public static Money zero(Currency currency) {
        return new Money(0, currency);
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(minorUnits + other.minorUnits, currency);
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        return new Money(minorUnits - other.minorUnits, currency);
    }

    public boolean isPositive() {
        return minorUnits > 0;
    }

    public boolean isLessThan(Money other) {
        requireSameCurrency(other);
        return minorUnits < other.minorUnits;
    }

    private void requireSameCurrency(Money other) {
        if (currency != other.currency) {
            throw new CurrencyMismatchException(currency + " vs " + other.currency);
        }
    }
}
