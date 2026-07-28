package in.neelporiya.movieticket;

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
 * // CONCURRENCY: Many users race for one show seat; Seat's AtomicReference CAS must allow exactly
 * one hold and reject every other contender.
 */
class MovieTicketConcurrencyTest {

    @Test
    void manyUsersRacingForSameSeatProduceExactlyOneHold() throws InterruptedException {
        MutableClock clock = MutableClock.atEpoch();
        AtomicInteger ids = new AtomicInteger();
        BookingService service = BookingService.builder()
                .clock(clock)
                .holdDuration(Duration.ofMinutes(5))
                .idGenerator(() -> "b-" + ids.incrementAndGet())
                .build();
        City city = service.createCity(new City("blr", "Bengaluru"));
        Screen screen = Screen.builder("screen-1", "Race Hall")
                .addSeat(new Seat("F1", "F", 1, SeatType.REGULAR, 10000))
                .build();
        Cinema cinema = service.createCinema(Cinema.builder("cinema-1", "CAS Cinemas", city)
                .addScreen(screen)
                .build());
        Movie movie = service.createMovie(new Movie("movie-1", "CAS Wars", "EN", Duration.ofMinutes(120)));
        Show show = service.createShow(new Show("show-1", movie, cinema, screen, clock.instant()));

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
                    Booking booking = service.holdSeats(show.getId(), userId, List.of("F1"));
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
        assertEquals(SeatState.HELD, service.seatState(show.getId(), "F1"));
    }
}
