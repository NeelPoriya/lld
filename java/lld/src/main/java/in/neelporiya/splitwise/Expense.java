package in.neelporiya.splitwise;

import java.time.Instant;
import java.util.List;

/** An expense: who paid, the total, and how it was split. */
public record Expense(String id, String paidBy, long totalCents, List<Split> splits, String description, Instant at) {
    public Expense {
        splits = List.copyOf(splits);
    }
}
