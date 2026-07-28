package in.neelporiya.vendingmachine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Greedy change-making: repeatedly use the largest available denomination.
 *
 * <p>// INTERVIEW INSIGHT: Greedy is perfect for canonical currencies like US coins, but not for
 * every artificial denomination set. With {4, 3, 1}, greedy fails to find the optimal 6 as 3+3
 * because it tries 4 first. We isolate it behind {@link ChangeStrategy} so a dynamic-programming
 * strategy can replace it without touching {@link VendingMachine}.
 */
public class GreedyChangeStrategy implements ChangeStrategy {

    @Override
    public List<Denomination> makeChange(int amountCents, Map<Denomination, Integer> availableCash) {
        Objects.requireNonNull(availableCash, "availableCash");
        if (amountCents < 0) {
            throw new IllegalArgumentException("Change amount cannot be negative");
        }

        int remaining = amountCents;
        List<Denomination> change = new ArrayList<>();
        List<Denomination> denominations = availableCash.keySet().stream()
                .sorted(Comparator.comparingInt(Denomination::cents).reversed())
                .toList();

        for (Denomination denomination : denominations) {
            int count = availableCash.getOrDefault(denomination, 0);
            while (count > 0 && denomination.cents() <= remaining) {
                change.add(denomination);
                remaining -= denomination.cents();
                count--;
            }
        }

        if (remaining != 0) {
            throw new UnableToMakeChangeException("Cannot make exact change for " + amountCents + " cents");
        }
        return List.copyOf(change);
    }
}
