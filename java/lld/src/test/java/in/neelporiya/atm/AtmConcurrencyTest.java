package in.neelporiya.atm;

import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // CONCURRENCY: Many ATM sessions race against the same account. The invariant is that exactly
 * the affordable withdrawals succeed and the account never goes below zero.
 */
class AtmConcurrencyTest {

    @Test
    void concurrentWithdrawalsOnSameAccountNeverOverdraw() throws InterruptedException {
        Account account = new Account("A-1", "1234", cents(1_000));
        Bank bank = new Bank().addAccount(account);
        CashInventory inventory = new CashInventory().add(NoteDenomination.ONE_HUNDRED, 100);

        int racers = 50;
        int expectedSuccesses = 10;
        ExecutorService pool = Executors.newFixedThreadPool(20);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(racers);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger insufficientFunds = new AtomicInteger();

        for (int i = 0; i < racers; i++) {
            int atmNumber = i;
            pool.submit(() -> {
                try {
                    Atm atm = Atm.builder()
                            .id("ATM-" + atmNumber)
                            .bank(bank)
                            .cashInventory(inventory)
                            .clock(MutableClock.atEpoch())
                            .build();
                    startGun.await(); // release all workers together to maximize account contention
                    atm.insertCard(new Card("CARD-" + atmNumber, "A-1"));
                    atm.enterPin("1234");
                    atm.selectOperation(AtmOperation.WITHDRAW);
                    atm.withdraw(cents(100));
                    successes.incrementAndGet();
                } catch (InsufficientFundsException expected) {
                    insufficientFunds.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        startGun.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS), "workers did not finish in time");
        pool.shutdownNow();

        assertEquals(expectedSuccesses, successes.get(), "only funded withdrawals should succeed");
        assertEquals(racers - expectedSuccesses, insufficientFunds.get());
        assertEquals(0, account.balanceCents(), "account must never overdraw below zero");
        assertEquals(90, inventory.count(NoteDenomination.ONE_HUNDRED), "only successful withdrawals keep notes");
    }

    private static int cents(int rupees) {
        return rupees * 100;
    }
}
