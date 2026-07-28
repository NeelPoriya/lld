package in.neelporiya.trafficsignal;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * One physical signal head at an intersection.
 *
 * <p>// DESIGN PATTERN: State — the light holds a {@link SignalState}, not a raw enum plus switch.
 *
 * <p>// CONCURRENCY: state reads/writes are synchronized on this light. Observers are stored in a
 * {@link CopyOnWriteArrayList}, so notification can happen without holding the state lock and without
 * ConcurrentModificationException if a display board is added while ticks are running.
 */
public class TrafficLight {

    private final String id;
    private final Direction direction;
    private final SignalDurations durations;
    private final List<SignalObserver> observers = new CopyOnWriteArrayList<>();

    private SignalState currentState;

    public TrafficLight(String id, Direction direction, SignalDurations durations) {
        this(id, direction, durations, SignalColor.RED);
    }

    public TrafficLight(String id, Direction direction, SignalDurations durations, SignalColor initialColor) {
        this.id = Objects.requireNonNull(id, "id");
        this.direction = Objects.requireNonNull(direction, "direction");
        this.durations = Objects.requireNonNull(durations, "durations");
        this.currentState = stateFor(Objects.requireNonNull(initialColor, "initialColor"));
    }

    public String getId() {
        return id;
    }

    public Direction getDirection() {
        return direction;
    }

    public SignalDurations getDurations() {
        return durations;
    }

    public synchronized SignalState getCurrentState() {
        return currentState;
    }

    public synchronized SignalColor getColor() {
        return currentState.color();
    }

    public void addObserver(SignalObserver observer) {
        observers.add(Objects.requireNonNull(observer, "observer"));
    }

    /**
     * Move this light to a concrete color and notify subscribers if the visible color changed.
     */
    public void transitionTo(SignalColor color, Instant changedAt) {
        Objects.requireNonNull(color, "color");
        Objects.requireNonNull(changedAt, "changedAt");

        SignalChangeEvent event;
        synchronized (this) {
            SignalColor previous = currentState.color();
            if (previous == color) {
                return;
            }
            currentState = stateFor(color);
            event = new SignalChangeEvent(id, direction, previous, color, changedAt);
        }

        // DESIGN PATTERN: Observer — notification is outside the lock to avoid callback deadlocks.
        observers.forEach(observer -> observer.onStateChanged(event));
    }

    /** Advance according to the current State object's transition function. */
    public void transitionToNext(Instant changedAt) {
        Objects.requireNonNull(changedAt, "changedAt");

        SignalChangeEvent event;
        synchronized (this) {
            SignalColor previous = currentState.color();
            currentState = currentState.next();
            event = new SignalChangeEvent(id, direction, previous, currentState.color(), changedAt);
        }
        observers.forEach(observer -> observer.onStateChanged(event));
    }

    private SignalState stateFor(SignalColor color) {
        return switch (color) {
            case RED -> new RedSignalState(durations);
            case GREEN -> new GreenSignalState(durations);
            case YELLOW -> new YellowSignalState(durations);
        };
    }
}
