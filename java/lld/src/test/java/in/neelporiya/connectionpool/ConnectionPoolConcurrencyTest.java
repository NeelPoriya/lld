package in.neelporiya.connectionpool;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // CONCURRENCY: the whole point of the pool. Many more threads than resources hammer borrow/return.
 * We assert the pool NEVER hands out more than {@code maxSize} resources at once, never creates more
 * than {@code maxSize}, and leaks nothing (everything returned at the end).
 */
class ConnectionPoolConcurrencyTest {

    @Test
    void neverExceedsMaxSizeUnderContention() throws InterruptedException {
        int maxSize = 8;
        FakeConnectionFactory factory = new FakeConnectionFactory();
        ConnectionPool<FakeConnection> pool = ConnectionPool.<FakeConnection>builder()
                .factory(factory)
                .maxSize(maxSize)
                .borrowTimeout(Duration.ofSeconds(5))
                .build();

        int threads = 32;
        int opsPerThread = 500;
        AtomicInteger concurrentlyBorrowed = new AtomicInteger();
        AtomicInteger maxConcurrent = new AtomicInteger();

        ExecutorService pool32 = Executors.newFixedThreadPool(threads);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            pool32.submit(() -> {
                try {
                    startGun.await();
                    for (int i = 0; i < opsPerThread; i++) {
                        try (PooledResource<FakeConnection> handle = pool.borrow()) {
                            int now = concurrentlyBorrowed.incrementAndGet();
                            maxConcurrent.accumulateAndGet(now, Math::max);
                            // touch the resource so it isn't optimised away
                            assertTrue(handle.get().valid);
                            concurrentlyBorrowed.decrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        startGun.countDown();
        assertTrue(done.await(30, TimeUnit.SECONDS));
        pool32.shutdownNow();

        assertTrue(maxConcurrent.get() <= maxSize,
                "observed " + maxConcurrent.get() + " concurrent borrows but max is " + maxSize);
        assertTrue(factory.createdCount.get() <= maxSize,
                "created " + factory.createdCount.get() + " resources but max is " + maxSize);
        assertEquals(0, pool.borrowedCount(), "no leaks: everything returned");
    }
}
