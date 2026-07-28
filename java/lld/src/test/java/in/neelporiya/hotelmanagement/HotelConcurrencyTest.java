package in.neelporiya.hotelmanagement;

import in.neelporiya.hotelmanagement.exception.RoomNotAvailableException;
import in.neelporiya.hotelmanagement.model.Reservation;
import in.neelporiya.hotelmanagement.model.RoomFactory;
import in.neelporiya.hotelmanagement.model.RoomType;
import in.neelporiya.hotelmanagement.model.StayRange;
import in.neelporiya.hotelmanagement.pricing.NightlyRatePricingStrategy;
import in.neelporiya.hotelmanagement.search.DefaultRoomSearchStrategy;
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
 * // CONCURRENCY: Many threads book the same room and range; the per-room lock must allow exactly one winner.
 */
class HotelConcurrencyTest {

    @Test
    void concurrentOverlappingBookingsForSameRoomHaveExactlyOneSuccess() throws InterruptedException {
        AtomicInteger ids = new AtomicInteger();
        HotelService service = new HotelService(
                MutableClock.atEpoch(),
                () -> "res-" + ids.incrementAndGet(),
                new NightlyRatePricingStrategy(),
                new DefaultRoomSearchStrategy());
        service.addRoom(RoomFactory.create("room-101", "101", RoomType.STANDARD, 1));

        int guests = 100;
        ExecutorService pool = Executors.newFixedThreadPool(24);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(guests);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();
        Set<String> winningReservationIds = ConcurrentHashMap.newKeySet();
        StayRange range = new StayRange(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5));

        for (int i = 0; i < guests; i++) {
            String guestId = "guest-" + i;
            pool.submit(() -> {
                try {
                    startGun.await();
                    Reservation reservation = service.bookRoom(guestId, "room-101", range);
                    successes.incrementAndGet();
                    winningReservationIds.add(reservation.getId());
                } catch (RoomNotAvailableException expected) {
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

        assertEquals(1, successes.get(), "exactly one overlapping booking should win");
        assertEquals(guests - 1, conflicts.get(), "all other overlapping requests must be rejected");
        assertEquals(1, winningReservationIds.size(), "there must be only one persisted winning id");
    }
}
