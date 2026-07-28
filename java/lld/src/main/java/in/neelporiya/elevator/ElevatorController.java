package in.neelporiya.elevator;

import in.neelporiya.elevator.dispatch.DispatchStrategy;
import in.neelporiya.elevator.observer.ElevatorObserver;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * // DESIGN PATTERN: Facade. The building's controller: it routes hall/car calls to cars via the
 * {@link DispatchStrategy} and advances the simulation.
 *
 * <p>// TESTABILITY: the simulation is pumped explicitly with {@link #step()} / {@link #runUntilIdle}
 * rather than a background thread, so tests are 100% deterministic. A production deployment would
 * wrap {@code step()} in a scheduled executor tick.
 */
public class ElevatorController {

    private final List<Elevator> elevators;
    private final DispatchStrategy dispatchStrategy;

    public ElevatorController(List<Elevator> elevators, DispatchStrategy dispatchStrategy) {
        this.elevators = List.copyOf(elevators);
        this.dispatchStrategy = Objects.requireNonNull(dispatchStrategy, "dispatchStrategy");
    }

    /** External button in the hallway: "I'm on {@code floor} and want to go {@code direction}." */
    public Elevator requestHallCall(int floor, Direction direction) {
        Elevator chosen = dispatchStrategy.selectElevator(elevators, floor, direction);
        chosen.addStop(floor);
        return chosen;
    }

    /** Internal button in the car: "take me to {@code destinationFloor}." */
    public void requestCarCall(String elevatorId, int destinationFloor) {
        elevator(elevatorId)
                .orElseThrow(() -> new IllegalArgumentException("no elevator " + elevatorId))
                .addStop(destinationFloor);
    }

    /** Advance every car by one step. */
    public void step() {
        for (Elevator elevator : elevators) {
            elevator.step();
        }
    }

    /**
     * Pump the simulation until all cars are idle or {@code maxSteps} is reached.
     *
     * @return the number of steps executed.
     */
    public int runUntilIdle(int maxSteps) {
        int steps = 0;
        while (steps < maxSteps && elevators.stream().anyMatch(e -> !e.isIdle())) {
            step();
            steps++;
        }
        return steps;
    }

    public void addObserver(ElevatorObserver observer) {
        elevators.forEach(e -> e.addObserver(observer));
    }

    public Optional<Elevator> elevator(String id) {
        return elevators.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    public List<Elevator> getElevators() {
        return elevators;
    }
}
