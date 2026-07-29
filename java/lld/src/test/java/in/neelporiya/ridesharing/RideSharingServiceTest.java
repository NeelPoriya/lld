package in.neelporiya.ridesharing;

import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RideSharingServiceTest {

    private RideSharingService service(MutableClock clock, Supplier<String> ids) {
        return new RideSharingService(
                new RiderRepository(),
                new DriverRepository(),
                new RideRepository(),
                new NearestDriverMatchingStrategy(),
                new DistanceBasedPricingStrategy(100, 200, 10),
                clock,
                ids);
    }

    @Test
    void requestMatchesNearestAvailableDriver() {
        RideSharingService service = service(MutableClock.atEpoch(), sequentialIds());
        service.registerRider("r1", "Rider");
        service.registerDriver("far", "Far", new Location(100, 100));
        service.registerDriver("near", "Near", new Location(1, 1));
        service.goOnline("far");
        service.goOnline("near");

        Ride ride = service.requestRide("r1", new Location(0, 0), new Location(3, 4));

        assertEquals(RideStatus.MATCHED, ride.getStatus());
        assertEquals("near", ride.getDriver().getId());
    }

    @Test
    void matchedDriverIsNoLongerAvailableForSecondRequest() {
        RideSharingService service = service(MutableClock.atEpoch(), sequentialIds());
        service.registerRider("r1", "Rider 1");
        service.registerRider("r2", "Rider 2");
        service.registerDriver("d1", "Driver 1", new Location(0, 0));
        service.registerDriver("d2", "Driver 2", new Location(10, 0));
        service.goOnline("d1");
        service.goOnline("d2");

        Ride first = service.requestRide("r1", new Location(0, 0), new Location(1, 1));
        Ride second = service.requestRide("r2", new Location(0, 0), new Location(1, 1));

        assertEquals(RideStatus.MATCHED, first.getStatus());
        assertEquals(RideStatus.MATCHED, second.getStatus());
        assertNotEquals(first.getDriver().getId(), second.getDriver().getId());
    }

    @Test
    void fareIsComputedFromDistanceAndTime() {
        MutableClock clock = MutableClock.atEpoch();
        RideSharingService service = service(clock, sequentialIds());
        service.registerRider("r1", "Rider");
        service.registerDriver("d1", "Driver", new Location(0, 0));
        service.goOnline("d1");

        Ride ride = service.requestRide("r1", new Location(0, 0), new Location(3, 4));
        service.startRide(ride.getId());
        clock.advance(Duration.ofMinutes(7));
        service.completeRide(ride.getId());

        assertEquals(5, ride.getFare().distanceUnits());
        assertEquals(7, ride.getFare().durationMinutes());
        assertEquals(1_170, ride.getFare().amount().cents());
    }

    @Test
    void lifecycleTransitionsAreEnforced() {
        RideSharingService service = service(MutableClock.atEpoch(), sequentialIds());
        service.registerRider("r1", "Rider");
        service.registerDriver("d1", "Driver", new Location(0, 0));
        service.goOnline("d1");
        Ride ride = service.requestRide("r1", new Location(0, 0), new Location(1, 1));

        assertThrows(IllegalStateException.class, () -> service.completeRide(ride.getId()));

        service.startRide(ride.getId());
        service.completeRide(ride.getId());
        assertEquals(RideStatus.COMPLETED, ride.getStatus());
    }

    @Test
    void cancelFreesTheDriverForAnotherRide() {
        RideSharingService service = service(MutableClock.atEpoch(), sequentialIds());
        service.registerRider("r1", "Rider 1");
        service.registerRider("r2", "Rider 2");
        service.registerDriver("d1", "Driver", new Location(0, 0));
        service.goOnline("d1");

        Ride first = service.requestRide("r1", new Location(0, 0), new Location(1, 1));
        service.cancelRide(first.getId());
        Ride second = service.requestRide("r2", new Location(0, 0), new Location(1, 1));

        assertEquals(RideStatus.CANCELLED, first.getStatus());
        assertEquals(RideStatus.MATCHED, second.getStatus());
        assertEquals("d1", second.getDriver().getId());
    }

    @Test
    void noAvailableDriversIsHandledAsTerminalRideState() {
        RideSharingService service = service(MutableClock.atEpoch(), sequentialIds());
        service.registerRider("r1", "Rider");

        Ride ride = service.requestRide("r1", new Location(0, 0), new Location(1, 1));

        assertEquals(RideStatus.NO_DRIVERS, ride.getStatus());
        assertNull(ride.getDriver());
        assertThrows(IllegalStateException.class, () -> service.cancelRide(ride.getId()));
    }

    @Test
    void observersReceiveRideStatusNotifications() {
        RideSharingService service = service(MutableClock.atEpoch(), sequentialIds());
        List<RideStatusEvent> events = new ArrayList<>();
        service.addObserver(events::add);
        service.registerRider("r1", "Rider");
        service.registerDriver("d1", "Driver", new Location(0, 0));
        service.goOnline("d1");

        Ride ride = service.requestRide("r1", new Location(0, 0), new Location(1, 1));
        service.startRide(ride.getId());

        assertEquals(List.of(RideStatus.MATCHED, RideStatus.IN_PROGRESS),
                events.stream().map(RideStatusEvent::status).toList());
    }

    @Test
    void concurrentRequestsNeverDoubleAssignADriver() throws InterruptedException {
        int riders = 100;
        int drivers = 5;
        RideSharingService service = service(MutableClock.atEpoch(), sequentialIds());
        for (int i = 0; i < riders; i++) {
            service.registerRider("r" + i, "Rider " + i);
        }
        for (int i = 0; i < drivers; i++) {
            service.registerDriver("d" + i, "Driver " + i, new Location(i, 0));
            service.goOnline("d" + i);
        }

        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(riders);
        Set<String> assignedDrivers = ConcurrentHashMap.newKeySet();
        AtomicInteger matched = new AtomicInteger();
        AtomicInteger noDrivers = new AtomicInteger();
        AtomicInteger doubleAssignments = new AtomicInteger();

        for (int i = 0; i < riders; i++) {
            String riderId = "r" + i;
            pool.submit(() -> {
                try {
                    startGun.await();
                    Ride ride = service.requestRide(riderId, new Location(0, 0), new Location(1, 1));
                    if (ride.getStatus() == RideStatus.MATCHED) {
                        matched.incrementAndGet();
                        if (!assignedDrivers.add(ride.getDriver().getId())) {
                            doubleAssignments.incrementAndGet();
                        }
                    } else if (ride.getStatus() == RideStatus.NO_DRIVERS) {
                        noDrivers.incrementAndGet();
                    }
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

        assertEquals(0, doubleAssignments.get(), "a driver was assigned to two active rides");
        assertEquals(drivers, matched.get(), "only the available drivers can be matched");
        assertEquals(riders - drivers, noDrivers.get(), "all excess riders should get NO_DRIVERS");
        assertEquals(drivers, assignedDrivers.size(), "every active ride must have a distinct driver");
    }

    private Supplier<String> sequentialIds() {
        AtomicInteger next = new AtomicInteger();
        return () -> "ride-" + next.incrementAndGet();
    }
}
