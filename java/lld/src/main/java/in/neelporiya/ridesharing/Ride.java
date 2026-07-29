package in.neelporiya.ridesharing;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Aggregate root for one trip.
 *
 * <p>// INTERVIEW INSIGHT: status changes go through one method, not scattered setters. That keeps
 * lifecycle rules auditable in interviews and prevents "complete before start" bugs.
 */
public class Ride {

    private final String id;
    private final Rider rider;
    private final Location pickup;
    private final Location drop;
    private final Instant requestedAt;
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicReference<Driver> driver = new AtomicReference<>();
    private final AtomicReference<Fare> fare = new AtomicReference<>();
    private volatile RideStatus status = RideStatus.REQUESTED;
    private volatile Instant matchedAt;
    private volatile Instant startedAt;
    private volatile Instant completedAt;
    private volatile Instant cancelledAt;

    public Ride(String id, Rider rider, Location pickup, Location drop, Instant requestedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.rider = Objects.requireNonNull(rider, "rider");
        this.pickup = Objects.requireNonNull(pickup, "pickup");
        this.drop = Objects.requireNonNull(drop, "drop");
        this.requestedAt = Objects.requireNonNull(requestedAt, "requestedAt");
    }

    public RideStatus transitionTo(RideStatus next, Instant at) {
        Objects.requireNonNull(next, "next");
        Objects.requireNonNull(at, "at");
        lock.lock();
        try {
            if (!status.canTransitionTo(next)) {
                throw new IllegalStateException("cannot transition ride " + id + " from " + status + " to " + next);
            }
            status = next;
            if (next == RideStatus.MATCHED) {
                matchedAt = at;
            } else if (next == RideStatus.IN_PROGRESS) {
                startedAt = at;
            } else if (next == RideStatus.COMPLETED) {
                completedAt = at;
            } else if (next == RideStatus.CANCELLED) {
                cancelledAt = at;
            }
            return status;
        } finally {
            lock.unlock();
        }
    }

    public RideStatus getStatus() {
        lock.lock();
        try {
            return status;
        } finally {
            lock.unlock();
        }
    }

    public void assignDriver(Driver assignedDriver) {
        driver.set(Objects.requireNonNull(assignedDriver, "assignedDriver"));
    }

    public Driver getDriver() {
        return driver.get();
    }

    public void setFare(Fare fare) {
        this.fare.set(Objects.requireNonNull(fare, "fare"));
    }

    public Fare getFare() {
        return fare.get();
    }

    public String getId() {
        return id;
    }

    public Rider getRider() {
        return rider;
    }

    public Location getPickup() {
        return pickup;
    }

    public Location getDrop() {
        return drop;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getMatchedAt() {
        return matchedAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }
}
