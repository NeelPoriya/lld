package in.neelporiya.atm;

/**
 * // DESIGN PATTERN: Factory — centralizes the classic 2000→500→200→100 ATM dispenser chain.
 *
 * <p>// EXTENSIBILITY: Adding a new denomination is a local wiring change here, not a change in
 * {@link Atm} or {@link CashInventory}.
 */
public final class CashDispenserFactory {

    private CashDispenserFactory() {
    }

    public static CashDispenser standardIndianDispenser() {
        return new NoteDispenserHandler(NoteDenomination.TWO_THOUSAND,
                new NoteDispenserHandler(NoteDenomination.FIVE_HUNDRED,
                        new NoteDispenserHandler(NoteDenomination.TWO_HUNDRED,
                                new NoteDispenserHandler(NoteDenomination.ONE_HUNDRED, null))));
    }
}
