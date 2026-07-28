package in.neelporiya.coffeevendingmachine;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // CONCURRENCY: the machine holds exactly one latte's worth of ingredients. Many threads brew at
 * once. The atomic multi-ingredient reservation must let exactly ONE succeed and must never drive a
 * tank negative (no over-pour).
 */
class CoffeeMachineConcurrencyTest {

    @Test
    void exactlyOneBrewGetsTheLastServing() throws InterruptedException {
        int threads = 64;
        CoffeeMachine machine = CoffeeMachine.builder()
                .outlets(threads) // plenty of outlets, so failures are purely "insufficient", not "busy"
                .refill(Ingredient.WATER, 30)
                .refill(Ingredient.MILK, 150)
                .refill(Ingredient.COFFEE_BEANS, 18)
                .addRecipe(BeverageFactory.latte())
                .build();

        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger successes = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    startGun.await();
                    if (machine.brew("Latte").success()) {
                        successes.incrementAndGet();
                    }
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

        assertEquals(1, successes.get(), "only one brew can have the single available serving");
        // No tank may have gone negative.
        assertEquals(0, machine.inventory().quantityOf(Ingredient.WATER));
        assertEquals(0, machine.inventory().quantityOf(Ingredient.MILK));
        assertEquals(0, machine.inventory().quantityOf(Ingredient.COFFEE_BEANS));
    }
}
