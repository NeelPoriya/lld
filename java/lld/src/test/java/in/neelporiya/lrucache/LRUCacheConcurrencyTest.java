package in.neelporiya.lrucache;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // CONCURRENCY: hammer the cache from many threads. The single-lock design must keep the map and
 * the linked list perfectly in sync ({@code isConsistent()}) and never exceed capacity, no matter
 * how the puts/gets/removes interleave.
 */
class LRUCacheConcurrencyTest {

    @Test
    void staysConsistentAndBoundedUnderContention() throws InterruptedException {
        int capacity = 100;
        LRUCache<Integer, Integer> cache = LRUCache.<Integer, Integer>builder()
                .capacity(capacity)
                .build();

        int threads = 16;
        int opsPerThread = 20_000;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    startGun.await();
                    ThreadLocalRandom rnd = ThreadLocalRandom.current();
                    for (int i = 0; i < opsPerThread; i++) {
                        int key = rnd.nextInt(500); // key space > capacity forces evictions
                        int op = rnd.nextInt(3);
                        switch (op) {
                            case 0 -> cache.put(key, key);
                            case 1 -> cache.get(key);
                            default -> cache.remove(key);
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
        pool.shutdownNow();

        assertTrue(cache.size() <= capacity, "cache must never exceed capacity");
        assertTrue(cache.isConsistent(), "map and linked list must describe the same entries");
    }
}
