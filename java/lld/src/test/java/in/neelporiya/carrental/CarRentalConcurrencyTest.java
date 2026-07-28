package in.neelporiya.carrental;

import in.neelporiya.carrental.exception.VehicleNotAvailableException;
import in.neelporiya.carrental.model.DateRange;
import in.neelporiya.carrental.model.Reservation;
import in.neelporiya.carrental.model.VehicleFactory;
import in.neelporiya.carrental.model.VehicleType;
import in.neelporiya.carrental.pricing.PerDayPricingStrategy;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
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
 * // CONCURRENCY: Many threads reserve the same vehicle and same date range; the per-vehicle lock must allow exactly one winner.
 */
class CarRentalConcurrencyTest {

    @Test
    void concurrentOverlappingReservationsForSameVehicleHaveExactlyOneSuccess() throws InterruptedException {
        AtomicInteger ids = new AtomicInteger();
        CarRentalService service = new CarRentalService(
                MutableClock.atEpoch(),
                () -> "res-" + ids.incrementAndGet(),
                new PerDayPricingStrategy());
        service.addVehicle(VehicleFactory.create("suv-1", VehicleType.SUV, "KA-01", "BLR", "Toyota", "Fortuner"));

        int customers = 100;
        ExecutorService pool = Executors.newFixedThreadPool(24);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(customers);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();
        Set<String> winningReservationIds = ConcurrentHashMap.newKeySet();
        DateRange range = new DateRange(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5));

        for (int i = 0; i < customers; i++) {
            String customerId = "cust-" + i;
            pool.submit(() -> {
                try {
                    startGun.await();
                    Reservation reservation = service.reserve(customerId, "suv-1", range, Set.of());
                    successes.incrementAndGet();
                    winningReservationIds.add(reservation.getId());
                } catch (VehicleNotAvailableException expected) {
                    conflicts.incrementAndGet();
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

        assertEquals(1, successes.get(), "exactly one overlapping reservation should win");
        assertEquals(customers - 1, conflicts.get(), "all other overlapping requests must be rejected");
        assertEquals(1, winningReservationIds.size(), "there must be only one persisted winning id");
    }
}
