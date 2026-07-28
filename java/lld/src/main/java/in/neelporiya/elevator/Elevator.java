package in.neelporiya.elevator;

import in.neelporiya.elevator.observer.ElevatorObserver;

import java.time.Clock;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A single elevator car implementing the LOOK scheduling algorithm.
 *
 * <p><b>LOOK</b>: keep moving in the current direction, stopping at each requested floor, until there
 * are no more requests that way; then reverse (if there are requests the other way) or go idle. This
 * is what real elevators do — far better than FCFS, which makes the car yo-yo.
 *
 * <p>// CONCURRENCY: pending stops live in two {@link NavigableSet}s ({@code up}/{@code down})
 * guarded by a {@link ReentrantLock}. {@code addStop} (called from request threads) and {@code step}
 * (called by the simulation) are each atomic, so a request added mid-step is never lost.
 *
 * <p>// TESTABILITY: movement is discrete — one {@link #step()} moves the car one floor. Event times
 * come from an injected {@link Clock}. Tests pump {@code step()} and assert exact floors/stop order
 * with zero real waiting.
 */
public class Elevator {

    private final String id;
    private final Clock clock;
    private final ReentrantLock lock = new ReentrantLock();
    private final NavigableSet<Integer> up = new TreeSet<>();   // requested floors above current
    private final NavigableSet<Integer> down = new TreeSet<>(); // requested floors below current
    private final List<ElevatorObserver> observers = new CopyOnWriteArrayList<>();

    private int currentFloor;
    private Direction direction = Direction.IDLE;

    public Elevator(String id, int startFloor) {
        this(id, startFloor, Clock.systemUTC());
    }

    public Elevator(String id, int startFloor, Clock clock) {
        this.id = id;
        this.currentFloor = startFloor;
        this.clock = clock;
    }

    public void addObserver(ElevatorObserver observer) {
        observers.add(observer);
    }

    /** Register a floor the car must visit. Direction of a hall call is used by dispatch, not here. */
    public void addStop(int floor) {
        lock.lock();
        try {
            if (floor > currentFloor) {
                up.add(floor);
            } else if (floor < currentFloor) {
                down.add(floor);
            } else {
                // Already here: open doors immediately.
                notifyStop(currentFloor);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Advance the simulation by one floor of travel (or a direction flip / stop).
     *
     * @return {@code true} if the car did something, {@code false} if it was idle with no work.
     */
    public boolean step() {
        lock.lock();
        try {
            if (up.isEmpty() && down.isEmpty()) {
                direction = Direction.IDLE;
                return false;
            }
            // Decide/adjust direction per LOOK.
            if (direction == Direction.IDLE) {
                direction = !up.isEmpty() ? Direction.UP : Direction.DOWN;
            } else if (direction == Direction.UP && up.isEmpty()) {
                direction = Direction.DOWN; // nothing above -> reverse
            } else if (direction == Direction.DOWN && down.isEmpty()) {
                direction = Direction.UP;   // nothing below -> reverse
            }

            if (direction == Direction.UP) {
                currentFloor++;
                notifyMove(currentFloor);
                if (up.remove(currentFloor)) {
                    notifyStop(currentFloor);
                }
            } else {
                currentFloor--;
                notifyMove(currentFloor);
                if (down.remove(currentFloor)) {
                    notifyStop(currentFloor);
                }
            }

            if (up.isEmpty() && down.isEmpty()) {
                direction = Direction.IDLE;
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    private void notifyMove(int floor) {
        java.time.Instant now = clock.instant();
        observers.forEach(o -> o.onMove(id, floor, now));
    }

    private void notifyStop(int floor) {
        java.time.Instant now = clock.instant();
        observers.forEach(o -> o.onStop(id, floor, now));
    }

    public boolean isIdle() {
        lock.lock();
        try {
            return direction == Direction.IDLE && up.isEmpty() && down.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    public int getCurrentFloor() {
        lock.lock();
        try {
            return currentFloor;
        } finally {
            lock.unlock();
        }
    }

    public Direction getDirection() {
        lock.lock();
        try {
            return direction;
        } finally {
            lock.unlock();
        }
    }

    public int pendingStops() {
        lock.lock();
        try {
            return up.size() + down.size();
        } finally {
            lock.unlock();
        }
    }

    public String getId() {
        return id;
    }
}
