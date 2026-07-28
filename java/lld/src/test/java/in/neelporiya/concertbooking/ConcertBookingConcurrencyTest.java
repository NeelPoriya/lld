package in.neelporiya.concertbooking;

import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
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
 * // CONCURRENCY: Many users race for one seat; the Seat AtomicReference CAS must allow exactly one
 * hold and reject every other contender.
 */
class ConcertBookingConcurrencyTest {

    @Test
    void manyUsersRacingForSameSeatProduceExactlyOneHold() throws InterruptedException {
        MutableClock clock = MutableClock.atEpoch();
        AtomicInteger ids = new AtomicInteger();
        BookingService service = BookingService.builder()
                .clock(clock)
                .holdDuration(Duration.ofMinutes(5))
                .idGenerator(() -> "b-" + ids.incrementAndGet())
                .build();
        Venue venue = Venue.builder("venue-1", "Race Arena")
                .addSection(new Section("floor", "Floor", "STANDARD", 10000))
                .addSeat(new Seat("F1", "floor", "F", 1))
                .build();
        Concert concert = new Concert("concert-1", "CAS Live", clock.instant(), venue);
        service.createConcert(concert);

        int users = 100;
        ExecutorService pool = Executors.newFixedThreadPool(24);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(users);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger failures = new AtomicInteger();
        Set<String> winningBookings = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < users; i++) {
            String userId = "user-" + i;
            pool.submit(() -> {
                try {
                    startGun.await();
                    Booking booking = service.holdSeats(concert.getId(), userId, List.of("F1"));
                    winningBookings.add(booking.getId());
                    successes.incrementAndGet();
                } catch (SeatUnavailableException expected) {
                    failures.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        startGun.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS), "workers did not finish");
        pool.shutdownNow();

        assertEquals(1, successes.get());
        assertEquals(users - 1, failures.get());
        assertEquals(1, winningBookings.size());
        assertEquals(SeatState.HELD, service.seatState(concert.getId(), "F1"));
    }
}
