package in.neelporiya.splitwise;

import in.neelporiya.splitwise.exception.InvalidSplitException;
import in.neelporiya.splitwise.split.EqualSplitStrategy;
import in.neelporiya.splitwise.split.ExactSplitStrategy;
import in.neelporiya.splitwise.split.PercentSplitStrategy;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SplitwiseServiceTest {

    private SplitwiseService service;

    @BeforeEach
    void setUp() {
        AtomicInteger seq = new AtomicInteger();
        service = new SplitwiseService(MutableClock.atEpoch(), () -> "e" + seq.incrementAndGet());
    }

    @Test
    void equalSplitDistributesEvenly() {
        service.addExpense("alice", 3000, new EqualSplitStrategy(),
                List.of("alice", "bob", "carol"), "Dinner");

        assertEquals(1000, service.amountOwed("bob", "alice"));
        assertEquals(1000, service.amountOwed("carol", "alice"));
        assertEquals(2000, service.netBalance("alice"));
        assertEquals(-1000, service.netBalance("bob"));
    }

    @Test
    void equalSplitRemainderGoesToFirstParticipants() {
        // 1000 / 3 = 333 with remainder 1 -> first participant pays 334.
        service.addExpense("alice", 1000, new EqualSplitStrategy(),
                List.of("alice", "bob", "carol"), "Coffee");
        assertEquals(333, service.amountOwed("bob", "alice"));
        assertEquals(333, service.amountOwed("carol", "alice"));
        assertEquals(666, service.netBalance("alice")); // paid 1000, own share 334
    }

    @Test
    void exactSplitMustSumToTotal() {
        assertThrows(InvalidSplitException.class, () -> service.addExpense("alice", 1000,
                new ExactSplitStrategy(Map.of("bob", 600L)), List.of("bob"), "x"));

        service.addExpense("alice", 1000, new ExactSplitStrategy(Map.of("bob", 600L, "carol", 400L)),
                List.of("bob", "carol"), "x");
        assertEquals(600, service.amountOwed("bob", "alice"));
        assertEquals(400, service.amountOwed("carol", "alice"));
    }

    @Test
    void percentSplitComputesShares() {
        service.addExpense("alice", 1000, new PercentSplitStrategy(Map.of("alice", 40, "bob", 30, "carol", 30)),
                List.of("alice", "bob", "carol"), "x");
        assertEquals(300, service.amountOwed("bob", "alice"));
        assertEquals(300, service.amountOwed("carol", "alice"));
    }

    @Test
    void percentagesMustSumTo100() {
        assertThrows(InvalidSplitException.class, () -> service.addExpense("alice", 1000,
                new PercentSplitStrategy(Map.of("bob", 40, "carol", 40)), List.of("bob", "carol"), "x"));
    }

    @Test
    void reverseDebtsAreNetted() {
        service.addExpense("alice", 1000, new ExactSplitStrategy(Map.of("bob", 1000L)), List.of("bob"), "1");
        service.addExpense("bob", 300, new ExactSplitStrategy(Map.of("alice", 300L)), List.of("alice"), "2");

        assertEquals(700, service.amountOwed("bob", "alice"));
        assertEquals(0, service.amountOwed("alice", "bob"));
    }

    @Test
    void settleUpReducesDebt() {
        service.addExpense("alice", 1000, new ExactSplitStrategy(Map.of("bob", 1000L)), List.of("bob"), "1");
        service.settleUp("bob", "alice", 600);
        assertEquals(400, service.amountOwed("bob", "alice"));
        service.settleUp("bob", "alice", 400);
        assertEquals(0, service.amountOwed("bob", "alice"));
    }

    @Test
    void simplifyDebtsMinimizesTransactions() {
        // A owes B 1000, B owes C 1000  =>  simplest plan is A pays C 1000 (B nets out).
        service.addExpense("B", 1000, new ExactSplitStrategy(Map.of("A", 1000L)), List.of("A"), "1");
        service.addExpense("C", 1000, new ExactSplitStrategy(Map.of("B", 1000L)), List.of("B"), "2");

        List<Settlement> plan = service.simplifyDebts();

        assertEquals(List.of(new Settlement("A", "C", 1000)), plan);
        assertEquals(0, service.netBalance("B"));
    }
}
