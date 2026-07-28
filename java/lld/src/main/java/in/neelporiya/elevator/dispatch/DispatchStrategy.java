package in.neelporiya.elevator.dispatch;

import in.neelporiya.elevator.Direction;
import in.neelporiya.elevator.Elevator;

import java.util.List;

/**
 * // DESIGN PATTERN: Strategy — decides WHICH car should serve a hall call. Swapping "nearest" for
 * "same-direction" or "least-loaded" is a new implementation; the controller never changes.
 */
public interface DispatchStrategy {

    Elevator selectElevator(List<Elevator> elevators, int floor, Direction desiredDirection);
}
