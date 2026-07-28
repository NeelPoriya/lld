package in.neelporiya.elevator.dispatch;

import in.neelporiya.elevator.Direction;
import in.neelporiya.elevator.Elevator;

import java.util.Comparator;
import java.util.List;

/**
 * Picks the car whose current floor is closest to the requested floor, preferring idle cars on ties.
 *
 * <p>// INTERVIEW INSIGHT: a smarter strategy also considers direction (don't send a car that just
 * passed the floor going the other way). We keep "nearest" as the default and note the extension —
 * the whole point of the Strategy seam is that the upgrade is drop-in.
 */
public class NearestDispatchStrategy implements DispatchStrategy {

    @Override
    public Elevator selectElevator(List<Elevator> elevators, int floor, Direction desiredDirection) {
        return elevators.stream()
                .min(Comparator
                        .comparingInt((Elevator e) -> Math.abs(e.getCurrentFloor() - floor))
                        .thenComparing(e -> e.isIdle() ? 0 : 1)
                        .thenComparing(Elevator::getId))
                .orElseThrow(() -> new IllegalStateException("no elevators configured"));
    }
}
