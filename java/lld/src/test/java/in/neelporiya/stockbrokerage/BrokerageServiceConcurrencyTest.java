package in.neelporiya.stockbrokerage;

import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // CONCURRENCY: one account with cash for exactly N shares is hit by many concurrent single-share
 * buys. The per-account lock must let exactly N fill; the rest are REJECTED, cash lands at zero and
 * is never driven negative.
 */
class BrokerageServiceConcurrencyTest {

    @Test
    void concurrentBuysNeverOverspendOneAccount() throws InterruptedException {
        InMemoryMarketData feed = new InMemoryMarketData().setPrice("AAPL", new BigDecimal("100"));
        AtomicInteger seq = new AtomicInteger();
        BrokerageService broker = new BrokerageService(feed, MutableClock.atEpoch(), () -> "id" + seq.incrementAndGet());
        broker.listStock("AAPL", "Apple Inc.");
        Account acc = broker.openAccount("alice", new BigDecimal("1000")); // exactly 10 shares @ 100

        int attempts = 100;
        ExecutorService pool = Executors.newFixedThreadPool(attempts);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(attempts);
        AtomicInteger filled = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        for (int i = 0; i < attempts; i++) {
            pool.execute(() -> {
                try {
                    go.await();
                    Order order = broker.marketOrder(acc.getId(), "AAPL", OrderSide.BUY, 1);
                    if (order.getStatus() == OrderStatus.FILLED) {
                        filled.incrementAndGet();
                    } else if (order.getStatus() == OrderStatus.REJECTED) {
                        rejected.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        go.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS));
        pool.shutdownNow();

        assertEquals(10, filled.get(), "exactly 10 shares affordable");
        assertEquals(attempts - 10, rejected.get());
        assertEquals(10, broker.holdingQuantity(acc.getId(), "AAPL"));
        assertEquals(0, new BigDecimal("0").compareTo(broker.cashOf(acc.getId())), "cash exhausted, never negative");
    }
}
