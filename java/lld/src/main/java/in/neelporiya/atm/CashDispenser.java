package in.neelporiya.atm;

import java.util.Map;
import java.util.Optional;

/**
 * // DESIGN PATTERN: Chain of Responsibility — note handlers cooperate to produce a withdrawal
 * plan. Each handler dispenses as many notes of its denomination as possible and passes the
 * remainder to the next handler.
 */
public interface CashDispenser {
    Optional<Map<NoteDenomination, Integer>> dispense(int amountCents, Map<NoteDenomination, Integer> availableNotes);
}
