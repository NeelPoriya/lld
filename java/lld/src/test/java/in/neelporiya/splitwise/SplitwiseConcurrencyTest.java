package in.neelporiya.splitwise;

import in.neelporiya.splitwise.split.EqualSplitStrategy;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // CONCURRENCY: many threads record expenses simultaneously. Each expense nets to zero across
 * participants, so the invariant is: the sum of every user's net balance must remain exactly zero
 * (money is conserved). A lost or double-applied debt would break it.
 */
class SplitwiseConcurrencyTest {

    @Test
    void concurrentExpensesConserveMoney() throws InterruptedException {
        AtomicInteger seq = new AtomicInteger();
        SplitwiseService service = new SplitwiseService(MutableClock.atEpoch(), () -> "e" + seq.incrementAndGet());

        int userCount = 10;
        List<String> users = new java.util.ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            String u = "u" + i;
            users.add(u);
            service.addUser(u);
        }

        int expensesToAdd = 500;
        ExecutorService pool = Executors.newFixedThreadPool(16);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(expensesToAdd);

        for (int i = 0; i < expensesToAdd; i++) {
            pool.submit(() -> {
                try {
                    startGun.await();
                    String payer = users.get(ThreadLocalRandom.current().nextInt(userCount));
                    service.addExpense(payer, 1000, new EqualSplitStrategy(), users, "shared");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        startGun.countDown();
        assertTrue(done.await(20, TimeUnit.SECONDS));
        pool.shutdownNow();

        long totalNet = users.stream().mapToLong(service::netBalance).sum();
        assertEquals(0, totalNet, "the sum of all balances must be zero (money is conserved)");
        assertEquals(expensesToAdd, service.getExpenses().size());
    }
}
