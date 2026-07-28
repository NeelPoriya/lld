package in.neelporiya.splitwise.split;

import in.neelporiya.splitwise.Split;
import in.neelporiya.splitwise.exception.InvalidSplitException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Splits by percentage. Percentages must total 100; any rounding remainder is added to the first
 * participant so the shares sum exactly to the total.
 */
public class PercentSplitStrategy implements SplitStrategy {

    private final Map<String, Integer> percentByUser;

    public PercentSplitStrategy(Map<String, Integer> percentByUser) {
        this.percentByUser = Map.copyOf(percentByUser);
    }

    @Override
    public List<Split> split(long totalCents, List<String> participants) {
        int totalPercent = percentByUser.values().stream().mapToInt(Integer::intValue).sum();
        if (totalPercent != 100) {
            throw new InvalidSplitException("percentages sum to " + totalPercent + ", must be 100");
        }
        List<Split> splits = new ArrayList<>();
        long allocated = 0;
        for (Map.Entry<String, Integer> entry : percentByUser.entrySet()) {
            long share = totalCents * entry.getValue() / 100;
            allocated += share;
            splits.add(new Split(entry.getKey(), share));
        }
        long remainder = totalCents - allocated;
        if (remainder != 0 && !splits.isEmpty()) {
            Split first = splits.get(0);
            splits.set(0, new Split(first.userId(), first.amountCents() + remainder));
        }
        return splits;
    }
}
