package in.neelporiya.ridesharing;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * // DESIGN PATTERN: Facade. This is the single API for onboarding users, driver availability,
 * matching, lifecycle transitions, notifications, and fare calculation.
 *
 * <p>// EXTENSIBILITY: matching and pricing are constructor-injected strategies; repositories are
 * isolated behind small classes so in-memory maps can later become SQL tables.
 */
public class RideSharingService {

    private final RiderRepository riders;
    private final DriverRepository drivers;
    private final RideRepository rides;
    private final MatchingStrategy matchingStrategy;
    private final PricingStrategy pricingStrategy;
    private final Clock clock;
    private final Supplier<String> idGenerator;
    private final List<RideStatusObserver> observers = new CopyOnWriteArrayList<>();

    public RideSharingService(
            RiderRepository riders,
            DriverRepository drivers,
            RideRepository rides,
            MatchingStrategy matchingStrategy,
            PricingStrategy pricingStrategy,
            Clock clock,
            Supplier<String> idGenerator) {
        this.riders = Objects.requireNonNull(riders, "riders");
        this.drivers = Objects.requireNonNull(drivers, "drivers");
        this.rides = Objects.requireNonNull(rides, "rides");
        this.matchingStrategy = Objects.requireNonNull(matchingStrategy, "matchingStrategy");
        this.pricingStrategy = Objects.requireNonNull(pricingStrategy, "pricingStrategy");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }

    public static RideSharingService createDefault() {
        return new RideSharingService(
                new RiderRepository(),
                new DriverRepository(),
                new RideRepository(),
                new NearestDriverMatchingStrategy(),
                new DistanceBasedPricingStrategy(100, 150, 25),
                Clock.systemUTC(),
                () -> UUID.randomUUID().toString());
    }

    public Rider registerRider(String id, String name) {
        Rider rider = new Rider(id, name);
        riders.save(rider);
        return rider;
    }

    public Driver registerDriver(String id, String name, Location location) {
        Driver driver = new Driver(id, name, location);
        drivers.save(driver);
        return driver;
    }

    public void addObserver(RideStatusObserver observer) {
        observers.add(Objects.requireNonNull(observer, "observer"));
    }

    public void goOnline(String driverId) {
        requireDriver(driverId).goOnline();
    }

    public void goOffline(String driverId) {
        requireDriver(driverId).goOffline();
    }

    public void updateDriverLocation(String driverId, Location location) {
        requireDriver(driverId).updateLocation(location);
    }

    public Ride requestRide(String riderId, Location pickup, Location drop) {
        Rider rider = requireRider(riderId);
        String rideId = idGenerator.get();
        Ride ride = new Ride(rideId, rider, pickup, drop, clock.instant());
        rides.save(ride);

        RideRequest request = new RideRequest(rideId, rider, pickup, drop);
        matchingStrategy.match(request, drivers.findAll()).ifPresentOrElse(driver -> {
            ride.assignDriver(driver);
            transitionAndNotify(ride, RideStatus.MATCHED);
        }, () -> transitionAndNotify(ride, RideStatus.NO_DRIVERS));

        return ride;
    }

    public Ride startRide(String rideId) {
        Ride ride = requireRide(rideId);
        transitionAndNotify(ride, RideStatus.IN_PROGRESS);
        return ride;
    }

    public Ride completeRide(String rideId) {
        Ride ride = requireRide(rideId);
        transitionAndNotify(ride, RideStatus.COMPLETED);
        ride.setFare(pricingStrategy.calculate(ride));
        releaseDriver(ride);
        return ride;
    }

    public Ride cancelRide(String rideId) {
        Ride ride = requireRide(rideId);
        transitionAndNotify(ride, RideStatus.CANCELLED);
        releaseDriver(ride);
        return ride;
    }

    public Ride getRide(String rideId) {
        return requireRide(rideId);
    }

    public Driver getDriver(String driverId) {
        return requireDriver(driverId);
    }

    private void transitionAndNotify(Ride ride, RideStatus next) {
        Instant now = clock.instant();
        RideStatus status = ride.transitionTo(next, now);
        Driver driver = ride.getDriver();
        String driverId = driver == null ? null : driver.getId();
        RideStatusEvent event = new RideStatusEvent(ride.getId(), ride.getRider().id(), driverId, status, now);
        observers.forEach(observer -> observer.onRideStatusChanged(event));
    }

    private void releaseDriver(Ride ride) {
        Driver driver = ride.getDriver();
        if (driver != null) {
            driver.release(ride.getId());
        }
    }

    private Rider requireRider(String riderId) {
        return riders.findById(riderId).orElseThrow(() -> new NoSuchElementException("no rider " + riderId));
    }

    private Driver requireDriver(String driverId) {
        return drivers.findById(driverId).orElseThrow(() -> new NoSuchElementException("no driver " + driverId));
    }

    private Ride requireRide(String rideId) {
        return rides.findById(rideId).orElseThrow(() -> new NoSuchElementException("no ride " + rideId));
    }
}
