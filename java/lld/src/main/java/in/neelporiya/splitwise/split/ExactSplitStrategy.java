package in.neelporiya.splitwise.split;

import in.neelporiya.splitwise.Split;
import in.neelporiya.splitwise.exception.InvalidSplitException;

import java.util.List;
import java.util.Map;

/** Each participant owes an explicitly stated amount; the amounts must sum to the total. */
public class ExactSplitStrategy implements SplitStrategy {

    private final Map<String, Long> exactAmounts;

    public ExactSplitStrategy(Map<String, Long> exactAmounts) {
        this.exactAmounts = Map.copyOf(exactAmounts);
    }

    @Override
    public List<Split> split(long totalCents, List<String> participants) {
        long sum = exactAmounts.values().stream().mapToLong(Long::longValue).sum();
        if (sum != totalCents) {
            throw new InvalidSplitException("exact splits sum to " + sum + " but total is " + totalCents);
        }
        return exactAmounts.entrySet().stream()
                .map(e -> new Split(e.getKey(), e.getValue()))
                .toList();
    }
}
