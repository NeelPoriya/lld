package in.neelporiya.atm;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * One node in the ATM note-dispensing chain.
 *
 * <p>// DESIGN PATTERN: Chain of Responsibility — 2000 delegates to 500, 500 delegates to 200,
 * and so on. The ATM facade does not know the denomination algorithm details.
 *
 * <p>// INTERVIEW INSIGHT: The handler tries the greedy count first, then smaller counts if the
 * downstream chain cannot make exact change. That keeps the classic CoR shape while avoiding false
 * rejections such as "500 + 3×200 available, withdraw 600".
 */
public class NoteDispenserHandler implements CashDispenser {

    private final NoteDenomination denomination;
    private final CashDispenser next;

    public NoteDispenserHandler(NoteDenomination denomination, CashDispenser next) {
        this.denomination = Objects.requireNonNull(denomination, "denomination");
        this.next = next;
    }

    @Override
    public Optional<Map<NoteDenomination, Integer>> dispense(
            int amountCents, Map<NoteDenomination, Integer> availableNotes) {
        if (amountCents < 0) {
            return Optional.empty();
        }

        int noteValue = denomination.cents();
        int requested = amountCents / noteValue;
        int available = availableNotes.getOrDefault(denomination, 0);
        int maxUsable = Math.min(requested, available);
        for (int count = maxUsable; count >= 0; count--) {
            int remainder = amountCents - (count * noteValue);
            Optional<Map<NoteDenomination, Integer>> downstream = next == null
                    ? exactEnd(remainder)
                    : next.dispense(remainder, availableNotes);
            if (downstream.isPresent()) {
                Map<NoteDenomination, Integer> plan = new EnumMap<>(NoteDenomination.class);
                plan.putAll(downstream.get());
                if (count > 0) {
                    plan.put(denomination, count);
                }
                return Optional.of(plan);
            }
        }
        return Optional.empty();
    }

    private static Optional<Map<NoteDenomination, Integer>> exactEnd(int remainder) {
        return remainder == 0
                ? Optional.of(new EnumMap<>(NoteDenomination.class))
                : Optional.empty();
    }
}
