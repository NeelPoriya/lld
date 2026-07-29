package in.neelporiya.fooddelivery;

import in.neelporiya.fooddelivery.exception.NoAgentAvailableException;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // CONCURRENCY: more ready orders than couriers, all dispatched at once. The atomic claim must let
 * exactly (#couriers) succeed, reject the rest, and never assign one courier to two orders.
 */
class FoodDeliveryConcurrencyTest {

    @Test
    void noCourierIsAssignedToTwoOrders() throws InterruptedException {
        AtomicInteger seq = new AtomicInteger();
        FoodDeliveryService app = new FoodDeliveryService(
                new StandardPricing(new BigDecimal("20"), new BigDecimal("5"), new BigDecimal("0.05")),
                new FirstAvailableStrategy(), MutableClock.atEpoch(), () -> "id" + seq.incrementAndGet());
        Restaurant r = app.registerRestaurant("Dosa Place", new Location(0, 0));
        MenuItem dosa = r.getMenu().addItem(new MenuItem("m1", "Masala Dosa", new BigDecimal("100")));
        Customer c = app.registerCustomer("alice", new Location(1, 0));

        int couriers = 10;
        int orderCount = 40;
        for (int i = 0; i < couriers; i++) {
            app.registerAgent("rider-" + i, new Location(1, 0));
        }

        List<Order> ready = new ArrayList<>();
        for (int i = 0; i < orderCount; i++) {
            Order order = app.placeOrder(c.id(), app.newCart(r.getId()).add(dosa, 1));
            app.acceptOrder(order.getId());
            app.startPreparing(order.getId());
            app.markReady(order.getId());
            ready.add(order);
        }

        ExecutorService pool = Executors.newFixedThreadPool(orderCount);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(orderCount);
        AtomicInteger dispatched = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        Set<String> assignedAgentIds = ConcurrentHashMap.newKeySet();

        for (Order order : ready) {
            pool.execute(() -> {
                try {
                    go.await();
                    DeliveryAgent agent = app.dispatch(order.getId());
                    dispatched.incrementAndGet();
                    assertTrue(assignedAgentIds.add(agent.getId()), "agent assigned to two orders: " + agent.getId());
                } catch (NoAgentAvailableException noAgent) {
                    rejected.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        go.countDown();
        assertTrue(done.await(15, TimeUnit.SECONDS));
        pool.shutdownNow();

        assertEquals(couriers, dispatched.get(), "exactly one dispatch per courier");
        assertEquals(orderCount - couriers, rejected.get());
        assertEquals(couriers, assignedAgentIds.size(), "each courier used at most once");
    }
}
