package in.neelporiya.elevator.observer;

import java.time.Instant;

/**
 * // DESIGN PATTERN: Observer — floor indicators, dashboards and logs react to car movement without
 * the elevator knowing about them. Both callbacks carry the event time from the injected clock.
 */
public interface ElevatorObserver {

    default void onMove(String elevatorId, int floor, Instant at) {
    }

    default void onStop(String elevatorId, int floor, Instant at) {
    }
}
