package in.neelporiya.digitalwallet;

import in.neelporiya.digitalwallet.exception.InsufficientFundsException;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DigitalWalletConcurrencyTest {

    private WalletService newService() {
        AtomicInteger seq = new AtomicInteger();
        return new WalletService(MutableClock.atEpoch(), () -> "id-" + seq.incrementAndGet());
    }

    /**
     * // CONCURRENCY: the classic bank-transfer deadlock. Half the threads move A->B while the other
     * half move B->A. Opposite lock acquisition order would deadlock; our global id-ordering makes
     * that impossible. We assert (a) the test finishes (no deadlock) and (b) total money is conserved
     * (each transfer is atomic).
     */
    @Test
    void bidirectionalTransfersNeverDeadlockAndConserveMoney() throws InterruptedException {
        WalletService service = newService();
        Wallet a = service.createWallet("a", Currency.USD);
        Wallet b = service.createWallet("b", Currency.USD);
        service.credit(a.getId(), Money.of(1_000_000, Currency.USD), "seed-a");
        service.credit(b.getId(), Money.of(1_000_000, Currency.USD), "seed-b");

        int threadsPerDirection = 8;
        int transfersPerThread = 1_000;
        ExecutorService pool = Executors.newFixedThreadPool(threadsPerDirection * 2);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadsPerDirection * 2);
        AtomicInteger keySeq = new AtomicInteger();

        Runnable aToB = () -> transferLoop(service, a.getId(), b.getId(), transfersPerThread, keySeq, startGun, done);
        Runnable bToA = () -> transferLoop(service, b.getId(), a.getId(), transfersPerThread, keySeq, startGun, done);
        for (int i = 0; i < threadsPerDirection; i++) {
            pool.submit(aToB);
            pool.submit(bToA);
        }

        startGun.countDown();
        assertTrue(done.await(30, TimeUnit.SECONDS), "transfers deadlocked!");
        pool.shutdownNow();

        long total = service.getBalance(a.getId()).minorUnits() + service.getBalance(b.getId()).minorUnits();
        assertEquals(2_000_000, total, "money must be conserved across all transfers");
    }

    private void transferLoop(WalletService service, String from, String to, int count,
                              AtomicInteger keySeq, CountDownLatch startGun, CountDownLatch done) {
        try {
            startGun.await();
            for (int i = 0; i < count; i++) {
                try {
                    service.transfer(from, to, Money.of(1, Currency.USD), "k" + keySeq.incrementAndGet());
                } catch (InsufficientFundsException ignored) {
                    // A failed transfer is a no-op; conservation still holds.
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            done.countDown();
        }
    }

    @Test
    void concurrentDistinctCreditsAreAllApplied() throws InterruptedException {
        WalletService service = newService();
        Wallet w = service.createWallet("w", Currency.USD);

        int threads = 500;
        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            String key = "credit-" + i;
            pool.submit(() -> {
                try {
                    startGun.await();
                    service.credit(w.getId(), Money.of(1, Currency.USD), key);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        startGun.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS));
        pool.shutdownNow();

        assertEquals(threads, service.getBalance(w.getId()).minorUnits());
    }

    @Test
    void concurrentSameKeyCreditAppliesExactlyOnce() throws InterruptedException {
        WalletService service = newService();
        Wallet w = service.createWallet("w", Currency.USD);

        int threads = 64;
        CopyOnWriteArrayList<String> txnIds = new CopyOnWriteArrayList<>();
        ExecutorService pool = Executors.newFixedThreadPool(16);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    startGun.await();
                    txnIds.add(service.credit(w.getId(), Money.of(100, Currency.USD), "SAME").id());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        startGun.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS));
        pool.shutdownNow();

        assertEquals(100, service.getBalance(w.getId()).minorUnits(), "idempotency: applied once");
        assertEquals(1, txnIds.stream().distinct().count(), "all callers see the same memoized txn");
    }
}
