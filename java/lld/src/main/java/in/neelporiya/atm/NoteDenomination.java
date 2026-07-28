package in.neelporiya.atm;

/**
 * Physical notes supported by this ATM.
 *
 * <p>// INTERVIEW INSIGHT: Money is stored as integer cents, never {@code double}; this avoids
 * rounding bugs in account balances and transaction assertions.
 */
public enum NoteDenomination {
    TWO_THOUSAND(2_000),
    FIVE_HUNDRED(500),
    TWO_HUNDRED(200),
    ONE_HUNDRED(100);

    private final int rupees;

    NoteDenomination(int rupees) {
        this.rupees = rupees;
    }

    public int rupees() {
        return rupees;
    }

    public int cents() {
        return rupees * 100;
    }
}
