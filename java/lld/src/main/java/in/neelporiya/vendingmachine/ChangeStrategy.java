package in.neelporiya.vendingmachine;

import java.util.List;
import java.util.Map;

/**
 * // DESIGN PATTERN: Strategy — change-making policy is swappable and independently testable.
 *
 * <p>// EXTENSIBILITY: Replace greedy with dynamic programming, limited-inventory optimization, or
 * a country-specific currency strategy without editing the vending machine core.
 */
public interface ChangeStrategy {
    List<Denomination> makeChange(int amountCents, Map<Denomination, Integer> availableCash);
}
