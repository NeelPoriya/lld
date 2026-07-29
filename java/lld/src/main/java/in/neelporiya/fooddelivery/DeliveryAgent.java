package in.neelporiya.fooddelivery;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A courier. The key to correct dispatch is claiming an agent ATOMICALLY so two orders can never grab
 * the same courier.
 *
 * <p>// CONCURRENCY: {@code available} is an {@link AtomicBoolean}; {@link #tryClaim()} is a
 * compare-and-set (true → false). Whoever CASes first wins the courier; everyone else moves on. No
 * lock needed for the claim itself — same trick as an atomic seat hold in the booking systems.
 */
public class DeliveryAgent {

    private final String id;
    private final String name;
    private volatile Location location;
    private final AtomicBoolean available = new AtomicBoolean(true);

    public DeliveryAgent(String id, String name, Location location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isAvailable() {
        return available.get();
    }

    /** @return true iff this call transitioned the agent from free to busy. */
    public boolean tryClaim() {
        return available.compareAndSet(true, false);
    }

    public void release() {
        available.set(true);
    }
}
