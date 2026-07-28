package in.neelporiya.elevator;

import in.neelporiya.elevator.dispatch.NearestDispatchStrategy;
import in.neelporiya.elevator.observer.ElevatorObserver;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElevatorTest {

    /** Observer that records the floors (and times) at which cars stopped. */
    private static final class StopRecorder implements ElevatorObserver {
        final List<Integer> stops = new CopyOnWriteArrayList<>();
        final List<Instant> times = new CopyOnWriteArrayList<>();

        @Override
        public void onStop(String elevatorId, int floor, Instant at) {
            stops.add(floor);
            times.add(at);
        }
    }

    @Test
    void servesStopsInLookOrderGoingUp() {
        Elevator car = new Elevator("A", 0, MutableClock.atEpoch());
        StopRecorder recorder = new StopRecorder();
        car.addObserver(recorder);
        ElevatorController controller = new ElevatorController(List.of(car), new NearestDispatchStrategy());

        controller.requestCarCall("A", 5);
        controller.requestCarCall("A", 3);
        controller.requestCarCall("A", 8);
        controller.runUntilIdle(100);

        assertEquals(List.of(3, 5, 8), recorder.stops, "LOOK serves ascending while going up");
        assertEquals(8, car.getCurrentFloor());
        assertTrue(car.isIdle());
    }

    @Test
    void reversesDirectionAfterExhaustingOneSide() {
        Elevator car = new Elevator("A", 5, MutableClock.atEpoch());
        StopRecorder recorder = new StopRecorder();
        car.addObserver(recorder);
        ElevatorController controller = new ElevatorController(List.of(car), new NearestDispatchStrategy());

        controller.requestCarCall("A", 8); // above
        controller.requestCarCall("A", 2); // below
        controller.runUntilIdle(100);

        // Goes up to 8 first, then reverses and comes down to 2.
        assertEquals(List.of(8, 2), recorder.stops);
        assertEquals(2, car.getCurrentFloor());
    }

    @Test
    void hallCallDispatchedToNearestCar() {
        Elevator a = new Elevator("A", 0, MutableClock.atEpoch());
        Elevator b = new Elevator("B", 9, MutableClock.atEpoch());
        ElevatorController controller = new ElevatorController(List.of(a, b), new NearestDispatchStrategy());

        Elevator chosen = controller.requestHallCall(8, Direction.UP);

        assertEquals("B", chosen.getId(), "car at floor 9 is nearest to floor 8");
        assertEquals(1, b.pendingStops());
        assertEquals(0, a.pendingStops());
    }

    @Test
    void stopEventsCarryInjectedClockTime() {
        MutableClock clock = MutableClock.atEpoch();
        Elevator car = new Elevator("A", 0, clock);
        StopRecorder recorder = new StopRecorder();
        car.addObserver(recorder);

        car.addStop(1);
        clock.advance(Duration.ofSeconds(10));
        car.step(); // moves to floor 1 and stops

        assertEquals(List.of(1), recorder.stops);
        assertEquals(Instant.EPOCH.plusSeconds(10), recorder.times.get(0));
    }

    @Test
    void idleCarStepIsNoOp() {
        Elevator car = new Elevator("A", 0, MutableClock.atEpoch());
        assertTrue(car.isIdle());
        // Stepping an idle car with no requests does nothing and stays idle.
        ElevatorController controller = new ElevatorController(List.of(car), new NearestDispatchStrategy());
        controller.step();
        assertEquals(0, car.getCurrentFloor());
        assertTrue(car.isIdle());
    }
}
