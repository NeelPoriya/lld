package in.neelporiya.parkinglot;

import in.neelporiya.parkinglot.exception.InvalidTicketException;
import in.neelporiya.parkinglot.exception.NoSpotAvailableException;
import in.neelporiya.parkinglot.spot.ParkingSpot;
import in.neelporiya.parkinglot.spot.ParkingSpotType;
import in.neelporiya.parkinglot.vehicle.Car;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

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
 * // CONCURRENCY: These tests are the real proof that the lock-free CAS design is correct. We create
 * more concurrent drivers than spots and assert that (a) nobody double-books a spot and (b) exactly
 * {@code capacity} vehicles get in.
 */
class ParkingLotConcurrencyTest {

    private ParkingLot lotWithCompactSpots(int capacity) {
        List<ParkingSpot> spots = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            spots.add(new ParkingSpot("C" + i, ParkingSpotType.COMPACT, 1, i));
        }
        return ParkingLot.builder()
                .clock(MutableClock.atEpoch())
                .addFloor(new ParkingFloor(1, spots))
                .build();
    }

    @Test
    void concurrentParkingNeverDoubleBooksASpot() throws InterruptedException {
        int capacity = 10;
        int drivers = 200; // massively over-subscribed
        ParkingLot lot = lotWithCompactSpots(capacity);

        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(drivers);

        Set<String> claimedSpotIds = ConcurrentHashMap.newKeySet();
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger rejections = new AtomicInteger();
        AtomicInteger doubleBookings = new AtomicInteger();

        for (int i = 0; i < drivers; i++) {
            String plate = "KA-" + i;
            pool.submit(() -> {
                try {
                    startGun.await(); // release all threads at once to maximise contention
                    ParkingTicket ticket = lot.parkVehicle(new Car(plate));
                    successes.incrementAndGet();
                    // If two vehicles ever land on the same spot id, this set rejects the duplicate.
                    if (!claimedSpotIds.add(ticket.getSpot().getId())) {
                        doubleBookings.incrementAndGet();
                    }
                } catch (NoSpotAvailableException expected) {
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

        assertEquals(0, doubleBookings.get(), "a spot was handed to two vehicles");
        assertEquals(capacity, successes.get(), "exactly capacity vehicles should park");
        assertEquals(drivers - capacity, rejections.get(), "the rest must be rejected");
        assertEquals(capacity, claimedSpotIds.size(), "every used spot must be distinct");
        assertTrue(lot.availability().getOrDefault(ParkingSpotType.COMPACT, 0L) == 0L,
                "lot should report full");
    }

    @Test
    void concurrentUnparkOfSameTicketSucceedsExactlyOnce() throws InterruptedException {
        ParkingLot lot = lotWithCompactSpots(1);
        ParkingTicket ticket = lot.parkVehicle(new Car("KA-777"));

        int racers = 50;
        ExecutorService pool = Executors.newFixedThreadPool(16);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(racers);
        AtomicInteger receipts = new AtomicInteger();
        AtomicInteger invalid = new AtomicInteger();

        for (int i = 0; i < racers; i++) {
            pool.submit(() -> {
                try {
                    startGun.await();
                    lot.unpark(ticket.getId());
                    receipts.incrementAndGet();
                } catch (InvalidTicketException expected) {
                    invalid.incrementAndGet();
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

        // The atomic ConcurrentHashMap.remove guarantees exactly one winner.
        assertEquals(1, receipts.get(), "ticket must be redeemable exactly once");
        assertEquals(racers - 1, invalid.get(), "all other exits must be rejected");
    }
}
