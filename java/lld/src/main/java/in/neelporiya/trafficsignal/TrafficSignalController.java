package in.neelporiya.trafficsignal;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Facade over the whole intersection.
 *
 * <p>// INTERVIEW INSIGHT: prefer this injectable controller over a Singleton. A Singleton is easy to
 * mention in interviews, but it creates global mutable state, makes tests order-dependent, and cannot
 * model two intersections. Dependency injection keeps the object resettable and deterministic.
 *
 * <p>// TESTABILITY: transition decisions read only the injected {@link Clock}. Tests advance a
 * MutableClock and call {@link #tick()} — no Thread.sleep, no flaky wall-clock assertions.
 */
public class TrafficSignalController {

    private final Map<Direction, TrafficLight> lights;
    private final SignalTimingPlan timingPlan;
    private final Clock clock;
    private final List<SignalObserver> observers = new CopyOnWriteArrayList<>();

    // CONCURRENCY: one small lock guards phase, override mode and multi-light state snapshots.
    private final ReentrantLock lock = new ReentrantLock();

    private Direction activeDirection;
    private Direction savedDirectionBeforeAllRed;
    private Instant phaseStartedAt;
    private EmergencyMode emergencyMode = EmergencyMode.NORMAL;

    public TrafficSignalController(SignalTimingPlan timingPlan, Clock clock) {
        this.timingPlan = Objects.requireNonNull(timingPlan, "timingPlan");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.lights = createLights(timingPlan);
        this.activeDirection = timingPlan.firstGreenDirection();
        this.phaseStartedAt = clock.instant();
        enforceOnly(activeDirection, SignalColor.GREEN, phaseStartedAt);
    }

    /**
     * Advance the controller if enough injected-clock time has elapsed for the current State.
     */
    public void tick() {
        lock.lock();
        try {
            if (emergencyMode != EmergencyMode.NORMAL) {
                return;
            }

            TrafficLight activeLight = lights.get(activeDirection);
            SignalState state = activeLight.getCurrentState();
            Instant now = clock.instant();
            if (Duration.between(phaseStartedAt, now).compareTo(state.duration()) < 0) {
                return;
            }

            if (state.color() == SignalColor.GREEN) {
                activeLight.transitionToNext(now); // GREEN -> YELLOW
                phaseStartedAt = now;
                return;
            }

            if (state.color() == SignalColor.YELLOW) {
                activeLight.transitionToNext(now); // YELLOW -> RED before another direction moves.
                Direction nextDirection = timingPlan.nextGreenDirection(activeDirection);
                activeDirection = nextDirection;
                if (nextDirection != activeLight.getDirection()) {
                    lights.get(nextDirection).transitionTo(SignalColor.GREEN, now);
                }
                phaseStartedAt = now;
                return;
            }

            // Single-direction intersections can visibly wait RED before cycling back to GREEN.
            activeLight.transitionToNext(now); // RED -> GREEN
            phaseStartedAt = now;
        } finally {
            lock.unlock();
        }
    }

    /** Emergency stop: all approaches become RED and normal ticking pauses. */
    public void activateAllRedOverride() {
        lock.lock();
        try {
            savedDirectionBeforeAllRed = activeDirection;
            emergencyMode = EmergencyMode.ALL_RED;
            Instant now = clock.instant();
            lights.values().forEach(light -> light.transitionTo(SignalColor.RED, now));
            phaseStartedAt = now;
        } finally {
            lock.unlock();
        }
    }

    /** Ambulance/preemption mode: exactly the priority direction is GREEN, every other light is RED. */
    public void forceGreen(Direction priorityDirection) {
        Objects.requireNonNull(priorityDirection, "priorityDirection");
        lock.lock();
        try {
            if (!lights.containsKey(priorityDirection)) {
                throw new IllegalArgumentException("Unknown direction: " + priorityDirection);
            }
            emergencyMode = EmergencyMode.PRIORITY_DIRECTION;
            activeDirection = priorityDirection;
            Instant now = clock.instant();
            enforceOnly(priorityDirection, SignalColor.GREEN, now);
            phaseStartedAt = now;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Leave override mode and restart a clean GREEN phase from the active direction.
     */
    public void clearOverride() {
        lock.lock();
        try {
            if (emergencyMode == EmergencyMode.ALL_RED && savedDirectionBeforeAllRed != null) {
                activeDirection = savedDirectionBeforeAllRed;
            }
            emergencyMode = EmergencyMode.NORMAL;
            savedDirectionBeforeAllRed = null;
            phaseStartedAt = clock.instant();
            enforceOnly(activeDirection, SignalColor.GREEN, phaseStartedAt);
        } finally {
            lock.unlock();
        }
    }

    public EmergencyMode getEmergencyMode() {
        lock.lock();
        try {
            return emergencyMode;
        } finally {
            lock.unlock();
        }
    }

    public SignalColor colorOf(Direction direction) {
        return light(direction).getColor();
    }

    public TrafficLight light(Direction direction) {
        TrafficLight light = lights.get(Objects.requireNonNull(direction, "direction"));
        if (light == null) {
            throw new IllegalArgumentException("Unknown direction: " + direction);
        }
        return light;
    }

    public Direction getActiveDirection() {
        lock.lock();
        try {
            return activeDirection;
        } finally {
            lock.unlock();
        }
    }

    public Map<Direction, SignalColor> snapshot() {
        lock.lock();
        try {
            Map<Direction, SignalColor> snapshot = new EnumMap<>(Direction.class);
            lights.forEach((direction, light) -> snapshot.put(direction, light.getColor()));
            return Map.copyOf(snapshot);
        } finally {
            lock.unlock();
        }
    }

    public boolean safetyInvariantHolds() {
        return snapshot().values().stream().filter(color -> color == SignalColor.GREEN).count() <= 1;
    }

    public void addObserver(SignalObserver observer) {
        observers.add(Objects.requireNonNull(observer, "observer"));
        lights.values().forEach(light -> light.addObserver(observer));
    }

    public RealTimeTrafficSignalDriver startRealTimeDriver(Duration tickInterval) {
        return new RealTimeTrafficSignalDriver(this, tickInterval);
    }

    private Map<Direction, TrafficLight> createLights(SignalTimingPlan timingPlan) {
        Map<Direction, TrafficLight> result = new EnumMap<>(Direction.class);
        for (Direction direction : timingPlan.directionsInOrder()) {
            result.put(direction, new TrafficLight(
                    direction.name() + "-LIGHT",
                    direction,
                    timingPlan.durationsFor(direction)));
        }
        return result;
    }

    private void enforceOnly(Direction greenDirection, SignalColor greenColor, Instant changedAt) {
        // INTERVIEW INSIGHT: turn conflicting approaches RED before granting GREEN so even transient
        // observer-visible updates preserve the safety invariant.
        lights.forEach((direction, light) -> {
            if (direction != greenDirection) {
                light.transitionTo(SignalColor.RED, changedAt);
            }
        });
        lights.get(greenDirection).transitionTo(greenColor, changedAt);
    }
}
