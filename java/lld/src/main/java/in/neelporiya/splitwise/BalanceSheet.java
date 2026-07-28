package in.neelporiya.splitwise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Tracks who owes whom, keeping each pair netted to a single direction.
 *
 * <p>{@code owes[a][b] = x} means "a owes b x cents" (always {@code x > 0}); the reverse entry is
 * never simultaneously positive.
 *
 * <p>// CONCURRENCY: an expense touches several pairs at once and the netting invariant spans
 * multiple map entries, so the whole sheet is guarded by one {@link ReentrantLock}. Applying an
 * expense is therefore atomic — concurrent entries can't corrupt or lose a debt.
 */
public class BalanceSheet {

    private final Map<String, Map<String, Long>> owes = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public void applyExpense(Expense expense) {
        lock.lock();
        try {
            for (Split split : expense.splits()) {
                if (!split.userId().equals(expense.paidBy())) {
                    // The participant owes the payer their share.
                    addDebt(split.userId(), expense.paidBy(), split.amountCents());
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /** {@code from} pays {@code to}, reducing what {@code from} owes {@code to}. */
    public void settle(String from, String to, long amountCents) {
        lock.lock();
        try {
            // Settling is the reverse of a debt: it credits `from` against `to`.
            addDebt(to, from, amountCents);
        } finally {
            lock.unlock();
        }
    }

    /** Record that {@code debtor} owes {@code creditor} an additional {@code amount}, netting reverse. */
    private void addDebt(String debtor, String creditor, long amount) {
        long reverse = get(creditor, debtor); // what creditor currently owes debtor
        if (reverse >= amount) {
            set(creditor, debtor, reverse - amount);
        } else {
            set(creditor, debtor, 0);
            set(debtor, creditor, get(debtor, creditor) + (amount - reverse));
        }
    }

    public long amountOwed(String debtor, String creditor) {
        lock.lock();
        try {
            return get(debtor, creditor);
        } finally {
            lock.unlock();
        }
    }

    /** Positive => the user is a net creditor (is owed); negative => net debtor (owes). */
    public long netBalance(String user) {
        lock.lock();
        try {
            long owedToUser = 0;
            for (Map<String, Long> row : owes.values()) {
                owedToUser += row.getOrDefault(user, 0L);
            }
            long userOwes = owes.getOrDefault(user, Map.of()).values().stream().mapToLong(Long::longValue).sum();
            return owedToUser - userOwes;
        } finally {
            lock.unlock();
        }
    }

    /** Counterparty -> signed amount (+ owed to user, - user owes counterparty). */
    public Map<String, Long> balancesFor(String user) {
        lock.lock();
        try {
            Map<String, Long> result = new HashMap<>();
            owes.getOrDefault(user, Map.of()).forEach((creditor, amt) -> result.merge(creditor, -amt, Long::sum));
            owes.forEach((debtor, row) -> {
                if (!debtor.equals(user)) {
                    Long amt = row.get(user);
                    if (amt != null) {
                        result.merge(debtor, amt, Long::sum);
                    }
                }
            });
            return result;
        } finally {
            lock.unlock();
        }
    }

    /**
     * // INTERVIEW INSIGHT: greedy debt simplification. Repeatedly match the biggest net creditor
     * with the biggest net debtor and settle the smaller magnitude. This drives everyone to zero in
     * close to the minimum number of payments.
     */
    public List<Settlement> simplify() {
        lock.lock();
        try {
            Map<String, Long> net = new HashMap<>();
            owes.forEach((debtor, row) -> row.forEach((creditor, amt) -> {
                net.merge(debtor, -amt, Long::sum);
                net.merge(creditor, amt, Long::sum);
            }));

            List<Settlement> settlements = new ArrayList<>();
            while (true) {
                String maxCreditor = null;
                long maxCredit = 0;
                String maxDebtor = null;
                long maxDebt = 0;
                for (Map.Entry<String, Long> e : net.entrySet()) {
                    if (e.getValue() > maxCredit) {
                        maxCredit = e.getValue();
                        maxCreditor = e.getKey();
                    }
                    if (e.getValue() < maxDebt) {
                        maxDebt = e.getValue();
                        maxDebtor = e.getKey();
                    }
                }
                if (maxCreditor == null || maxDebtor == null) {
                    break; // everything settled
                }
                long amount = Math.min(maxCredit, -maxDebt);
                settlements.add(new Settlement(maxDebtor, maxCreditor, amount));
                net.put(maxCreditor, maxCredit - amount);
                net.put(maxDebtor, maxDebt + amount);
            }
            return settlements;
        } finally {
            lock.unlock();
        }
    }

    private long get(String a, String b) {
        return owes.getOrDefault(a, Map.of()).getOrDefault(b, 0L);
    }

    private void set(String a, String b, long value) {
        if (value == 0) {
            Map<String, Long> row = owes.get(a);
            if (row != null) {
                row.remove(b);
                if (row.isEmpty()) {
                    owes.remove(a);
                }
            }
        } else {
            owes.computeIfAbsent(a, k -> new HashMap<>()).put(b, value);
        }
    }
}
