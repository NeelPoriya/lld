package in.neelporiya.stocktrading;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // CONCURRENCY: N buys and N sells at the SAME crossing price are submitted for one symbol from many
 * threads in random order. The per-symbol lock must make matching correct regardless of interleaving:
 * every order fills, exactly N unit trades print, quantity is conserved and the book empties.
 */
class MatchingEngineConcurrencyTest {

    private static final String SYM = "AAPL";

    @Test
    void quantityIsConservedUnderConcurrentMatching() throws InterruptedException {
        MatchingEngine engine = new MatchingEngine();
        AtomicInteger tradeCount = new AtomicInteger();
        AtomicInteger tradedQuantity = new AtomicInteger();
        engine.addListener(trade -> {
            tradeCount.incrementAndGet();
            tradedQuantity.addAndGet(trade.quantity());
        });

        int perSide = 300;
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < perSide; i++) {
            orders.add(Order.limit("b" + i, SYM, OrderSide.BUY, 100, 1));
            orders.add(Order.limit("s" + i, SYM, OrderSide.SELL, 100, 1));
        }
        Collections.shuffle(orders, new Random(7)); // deterministic interleaving

        ExecutorService pool = Executors.newFixedThreadPool(16);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(orders.size());
        for (Order order : orders) {
            pool.execute(() -> {
                try {
                    go.await();
                    engine.placeOrder(order);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        go.countDown();
        assertTrue(done.await(20, TimeUnit.SECONDS));
        pool.shutdownNow();

        assertEquals(perSide, tradeCount.get(), "one unit trade per matched pair");
        assertEquals(perSide, tradedQuantity.get(), "total traded quantity conserved");
        assertEquals(0, engine.getOrderBook(SYM).restingOrderCount(), "book fully cleared");
        assertTrue(engine.getOrderBook(SYM).bestBid().isEmpty());
        assertTrue(engine.getOrderBook(SYM).bestAsk().isEmpty());

        for (Order order : orders) {
            assertEquals(OrderStatus.FILLED, order.getStatus(), order.getId() + " should be filled");
            assertEquals(0, order.getRemainingQuantity());
        }
    }
}
