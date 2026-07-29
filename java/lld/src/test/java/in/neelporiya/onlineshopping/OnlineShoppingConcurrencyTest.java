package in.neelporiya.onlineshopping;

import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // CONCURRENCY: Many buyers race for limited stock. The all-or-nothing inventory reservation must
 * allow exactly stock-count orders and must leave stock at zero, never negative.
 */
class OnlineShoppingConcurrencyTest {

    @Test
    void concurrentCheckoutNeverOversellsLastUnits() throws InterruptedException {
        int stock = 7;
        int buyers = 100;
        AtomicInteger ids = new AtomicInteger();
        ShoppingService service = ShoppingService.builder()
                .clock(MutableClock.atEpoch())
                .idGenerator(() -> "id-" + ids.incrementAndGet())
                .build();
        service.addProduct(new Product("console", "Game Console", "Popular electronics", 50_000,
                Set.of("gaming")));
        service.addStock("console", stock);

        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(buyers);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger rejections = new AtomicInteger();

        for (int i = 0; i < buyers; i++) {
            int buyer = i;
            pool.submit(() -> {
                try {
                    Cart cart = service.createCart("customer-" + buyer);
                    service.addToCart(cart.getId(), "console", 1);
                    startGun.await();
                    service.checkout(cart.getId());
                    successes.incrementAndGet();
                } catch (InsufficientStockException expected) {
                    rejections.incrementAndGet();
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

        assertEquals(stock, successes.get(), "exactly available stock should checkout");
        assertEquals(buyers - stock, rejections.get(), "the rest must be rejected");
        assertEquals(0, service.inventory().quantityOf("console"));
    }
}
