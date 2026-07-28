package in.neelporiya.atm;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

/**
 * // TESTABILITY: Returning the exact note breakdown lets tests prove the dispenser chain chose
 * real notes instead of only checking that a balance changed.
 */
public record WithdrawResult(
        String accountId,
        int amountCents,
        Map<NoteDenomination, Integer> dispensedNotes,
        int balanceAfterCents,
        Instant timestamp) {

    public WithdrawResult {
        dispensedNotes = Map.copyOf(new EnumMap<>(dispensedNotes));
    }
}
