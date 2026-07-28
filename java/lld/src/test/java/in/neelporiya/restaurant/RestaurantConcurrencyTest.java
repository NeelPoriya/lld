package in.neelporiya.restaurant;

import in.neelporiya.restaurant.exception.InvalidOrderTransitionException;
import in.neelporiya.restaurant.exception.ReservationConflictException;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * // CONCURRENCY: Latches release many workers at once so the check-and-act races are exercised
 * deterministically without Thread.sleep.
 */
class RestaurantConcurrencyTest {

    @Test
    void concurrentReservationsForSameTableAndSlotAllowExactlyOneWinner() throws InterruptedException {
        MutableClock clock = MutableClock.atEpoch();
        AtomicInteger reservationIds = new AtomicInteger();
        RestaurantService service = RestaurantService.builder()
                .clock(clock)
                .reservationIdGenerator(() -> "R-" + reservationIds.incrementAndGet())
                .build();
        service.addTable(new Table("T1", 4));

        int hosts = 100;
        TimeSlot slot = new TimeSlot(Instant.EPOCH.plus(Duration.ofHours(19)), Instant.EPOCH.plus(Duration.ofHours(21)));
        ExecutorService pool = Executors.newFixedThreadPool(24);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(hosts);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();

        for (int i = 0; i < hosts; i++) {
            int guest = i;
            pool.submit(() -> {
                try {
                    startGun.await();
                    service.reserveTable("T1", "Guest-" + guest, slot);
                    successes.incrementAndGet();
                } catch (ReservationConflictException expected) {
                    conflicts.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        startGun.countDown();
        Assertions.assertTrue(done.await(10, TimeUnit.SECONDS), "workers did not finish in time");
        pool.shutdownNow();

        Assertions.assertEquals(1, successes.get(), "exactly one host may reserve the slot");
        Assertions.assertEquals(hosts - 1, conflicts.get(), "all other overlapping attempts must fail");
        Assertions.assertEquals(1, service.reservationsForTable("T1").size());
    }

    @Test
    void concurrentOrderTransitionRaceHasExactlyOneWinner() throws InterruptedException {
        MutableClock clock = MutableClock.atEpoch();
        RestaurantService service = RestaurantService.builder()
                .clock(clock)
                .orderIdGenerator(() -> "O-1")
                .build();
        service.addTable(new Table("T1", 2));
        service.addMenuItem(new MenuItem("tea", "Tea", 100));
        Order order = service.placeOrder("T1", List.of(new OrderRequestItem("tea", 1)));

        int waiters = 80;
        ExecutorService pool = Executors.newFixedThreadPool(20);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(waiters);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        for (int i = 0; i < waiters; i++) {
            pool.submit(() -> {
                try {
                    startGun.await();
                    service.changeOrderStatus(order.getId(), OrderStatus.PREPARING);
                    successes.incrementAndGet();
                } catch (InvalidOrderTransitionException expected) {
                    rejected.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        startGun.countDown();
        Assertions.assertTrue(done.await(10, TimeUnit.SECONDS), "workers did not finish in time");
        pool.shutdownNow();

        Assertions.assertEquals(1, successes.get(), "one waiter should apply PLACED->PREPARING");
        Assertions.assertEquals(waiters - 1, rejected.get(), "later racers see PREPARING and are rejected");
        Assertions.assertEquals(OrderStatus.PREPARING, order.getStatus());
    }
}
