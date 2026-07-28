package in.neelporiya.splitwise.split;

import in.neelporiya.splitwise.Split;
import in.neelporiya.splitwise.exception.InvalidSplitException;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits equally. The remainder cents (total % n) are handed one-each to the first few participants,
 * so the shares always sum <em>exactly</em> to the total — no lost or invented pennies.
 */
public class EqualSplitStrategy implements SplitStrategy {

    @Override
    public List<Split> split(long totalCents, List<String> participants) {
        int n = participants.size();
        if (n == 0) {
            throw new InvalidSplitException("cannot split among zero participants");
        }
        long base = totalCents / n;
        long remainder = totalCents % n; // this many participants pay one extra cent
        List<Split> splits = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            long share = base + (i < remainder ? 1 : 0);
            splits.add(new Split(participants.get(i), share));
        }
        return splits;
    }
}
