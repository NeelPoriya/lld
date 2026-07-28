package in.neelporiya.airline;

import in.neelporiya.airline.exception.SeatUnavailableException;
import in.neelporiya.airline.model.Aircraft;
import in.neelporiya.airline.model.AircraftBuilder;
import in.neelporiya.airline.model.Booking;
import in.neelporiya.airline.model.Flight;
import in.neelporiya.airline.model.FlightInstance;
import in.neelporiya.airline.model.Passenger;
import in.neelporiya.airline.model.SeatClass;
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
 * // CONCURRENCY: These tests release many passengers together to prove that per-seat CAS gives exactly one winner per seat.
 */
class AirlineConcurrencyTest {

    private AirlineService serviceWithAircraft(Aircraft aircraft, AtomicInteger ids) {
        AirlineService service = AirlineService.builder()
                .clock(MutableClock.atEpoch())
                .idGenerator(() -> "booking-" + ids.incrementAndGet())
                .build();
        Flight flight = new Flight("NP101", "BLR", "DEL");
        service.addFlight(flight);
        service.addFlightInstance(new FlightInstance("inst-1", flight, LocalDate.of(2026, 8, 1), aircraft));
        return service;
    }

    @Test
    void concurrentBookingOfSameSeatHasExactlyOneSuccess() throws InterruptedException {
        AtomicInteger ids = new AtomicInteger();
        Aircraft aircraft = AircraftBuilder.forTailNumber("VT-CAS")
                .addSeat("E1", SeatClass.ECONOMY, 10_000)
                .build();
        AirlineService service = serviceWithAircraft(aircraft, ids);

        int passengers = 100;
        ExecutorService pool = Executors.newFixedThreadPool(24);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(passengers);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();
        Set<String> winningBookingIds = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < passengers; i++) {
            String passengerId = "p" + i;
            pool.submit(() -> {
                try {
                    startGun.await();
                    Booking booking = service.bookSeat(new Passenger(passengerId, passengerId), "inst-1", "E1");
                    successes.incrementAndGet();
                    winningBookingIds.add(booking.getId());
                } catch (SeatUnavailableException expected) {
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

        assertEquals(1, successes.get(), "exactly one passenger should win seat E1");
        assertEquals(passengers - 1, conflicts.get(), "all other passengers must be rejected");
        assertEquals(1, winningBookingIds.size(), "there must be only one persisted winning booking id");
    }

    @Test
    void concurrentBookingAcrossSmallCabinSucceedsExactlyCapacityTimes() throws InterruptedException {
        AtomicInteger ids = new AtomicInteger();
        int capacity = 8;
        AircraftBuilder builder = AircraftBuilder.forTailNumber("VT-SMALL");
        for (int i = 1; i <= capacity; i++) {
            builder.addSeat("E" + i, SeatClass.ECONOMY, 10_000);
        }
        AirlineService service = serviceWithAircraft(builder.build(), ids);

        int passengers = 80;
        ExecutorService pool = Executors.newFixedThreadPool(24);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(passengers);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();
        Set<String> claimedSeatNumbers = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < passengers; i++) {
            String passengerId = "p" + i;
            String requestedSeat = "E" + ((i % capacity) + 1);
            pool.submit(() -> {
                try {
                    startGun.await();
                    Booking booking = service.bookSeat(new Passenger(passengerId, passengerId), "inst-1", requestedSeat);
                    successes.incrementAndGet();
                    claimedSeatNumbers.add(booking.getSeatNumber());
                } catch (SeatUnavailableException expected) {
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

        assertEquals(capacity, successes.get(), "one passenger should win each seat");
        assertEquals(passengers - capacity, conflicts.get(), "overflow requests must be rejected");
        assertEquals(capacity, claimedSeatNumbers.size(), "every claimed seat should be distinct");
    }
}
