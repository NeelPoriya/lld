package in.neelporiya.ridesharing;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A driver whose mutable operational state is thread-safe.
 *
 * <p>// CONCURRENCY: the active ride id is an {@link AtomicReference}. Matching claims a driver with
 * {@code compareAndSet(null, rideId)}, so two concurrent ride requests can both observe the driver as
 * nearby, but only one can win the atomic claim.
 */
public class Driver {

    private final String id;
    private final String name;
    private final AtomicReference<Location> location;
    private final AtomicBoolean online = new AtomicBoolean();
    private final AtomicReference<String> activeRideId = new AtomicReference<>();

    public Driver(String id, String name, Location location) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.location = new AtomicReference<>(Objects.requireNonNull(location, "location"));
    }

    public boolean tryClaim(String rideId) {
        if (!online.get()) {
            return false;
        }
        // CONCURRENCY: atomic check-and-set; there is no "isAvailable then assign" race window.
        return activeRideId.compareAndSet(null, Objects.requireNonNull(rideId, "rideId"));
    }

    public void release(String rideId) {
        activeRideId.compareAndSet(Objects.requireNonNull(rideId, "rideId"), null);
    }

    public boolean isAvailable() {
        return online.get() && activeRideId.get() == null;
    }

    public void goOnline() {
        online.set(true);
    }

    public void goOffline() {
        online.set(false);
    }

    public boolean isOnline() {
        return online.get();
    }

    public void updateLocation(Location newLocation) {
        location.set(Objects.requireNonNull(newLocation, "newLocation"));
    }

    public Location getLocation() {
        return location.get();
    }

    public String getActiveRideId() {
        return activeRideId.get();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
