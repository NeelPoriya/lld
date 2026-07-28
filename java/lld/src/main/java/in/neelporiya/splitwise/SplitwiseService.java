package in.neelporiya.splitwise;

import in.neelporiya.splitwise.exception.InvalidSplitException;
import in.neelporiya.splitwise.split.SplitStrategy;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * // DESIGN PATTERN: Facade over users, expenses and the balance sheet.
 */
public class SplitwiseService {

    private final Set<String> users = ConcurrentHashMap.newKeySet();
    private final List<Expense> expenses = new CopyOnWriteArrayList<>();
    private final BalanceSheet balanceSheet = new BalanceSheet();
    private final Clock clock;
    private final Supplier<String> idGenerator;

    public SplitwiseService(Clock clock, Supplier<String> idGenerator) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }

    public static SplitwiseService createDefault() {
        return new SplitwiseService(Clock.systemUTC(), () -> UUID.randomUUID().toString());
    }

    public void addUser(String userId) {
        users.add(userId);
    }

    public Expense addExpense(String paidBy, long totalCents, SplitStrategy strategy,
                              List<String> participants, String description) {
        List<Split> splits = strategy.split(totalCents, participants);
        long sum = splits.stream().mapToLong(Split::amountCents).sum();
        if (sum != totalCents) {
            throw new InvalidSplitException("splits sum to " + sum + " but total is " + totalCents);
        }
        Expense expense = new Expense(idGenerator.get(), paidBy, totalCents, splits, description, clock.instant());
        expenses.add(expense);
        balanceSheet.applyExpense(expense);
        return expense;
    }

    public void settleUp(String from, String to, long amountCents) {
        balanceSheet.settle(from, to, amountCents);
    }

    public long amountOwed(String debtor, String creditor) {
        return balanceSheet.amountOwed(debtor, creditor);
    }

    public long netBalance(String user) {
        return balanceSheet.netBalance(user);
    }

    public Map<String, Long> balancesFor(String user) {
        return balanceSheet.balancesFor(user);
    }

    public List<Settlement> simplifyDebts() {
        return balanceSheet.simplify();
    }

    public List<Expense> getExpenses() {
        return List.copyOf(expenses);
    }
}
