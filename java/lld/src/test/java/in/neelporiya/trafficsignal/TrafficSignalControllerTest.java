package in.neelporiya.trafficsignal;

import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrafficSignalControllerTest {

    private final SignalDurations durations = SignalDurations.ofSeconds(5, 10, 3);
    private final MutableClock clock = MutableClock.atEpoch();

    private TrafficSignalController fourWayController() {
        return new TrafficSignalController(FixedSignalTimingPlan.fourWay(durations), clock);
    }

    @Test
    void lightTransitionsGreenToYellowToRedAtElapsedDurations() {
        TrafficSignalController controller = new TrafficSignalController(
                new FixedSignalTimingPlan(List.of(Direction.NORTH), durations),
                clock);

        assertEquals(SignalColor.GREEN, controller.colorOf(Direction.NORTH));

        // TESTABILITY: no sleeping; we move the injected clock to just before the GREEN duration.
        clock.advance(Duration.ofSeconds(9));
        controller.tick();
        assertEquals(SignalColor.GREEN, controller.colorOf(Direction.NORTH));

        clock.advance(Duration.ofSeconds(1));
        controller.tick();
        assertEquals(SignalColor.YELLOW, controller.colorOf(Direction.NORTH));

        clock.advance(Duration.ofSeconds(2));
        controller.tick();
        assertEquals(SignalColor.YELLOW, controller.colorOf(Direction.NORTH));

        clock.advance(Duration.ofSeconds(1));
        controller.tick();
        assertEquals(SignalColor.RED, controller.colorOf(Direction.NORTH));

        clock.advance(Duration.ofSeconds(5));
        controller.tick();
        assertEquals(SignalColor.GREEN, controller.colorOf(Direction.NORTH));
    }

    @Test
    void intersectionNeverHasTwoGreenDirectionsAcrossFullCycle() {
        TrafficSignalController controller = fourWayController();
        List<Direction> expectedGreenOrder = List.of(
                Direction.NORTH,
                Direction.EAST,
                Direction.SOUTH,
                Direction.WEST,
                Direction.NORTH);

        assertOnlyGreen(controller, expectedGreenOrder.getFirst());

        for (int i = 1; i < expectedGreenOrder.size(); i++) {
            clock.advance(Duration.ofSeconds(10));
            controller.tick();
            assertTrue(controller.safetyInvariantHolds());
            assertEquals(SignalColor.YELLOW, controller.colorOf(expectedGreenOrder.get(i - 1)));

            clock.advance(Duration.ofSeconds(3));
            controller.tick();
            assertOnlyGreen(controller, expectedGreenOrder.get(i));
        }
    }

    @Test
    void emergencyOverrideForcesPriorityDirectionAndResumesNormalCycle() {
        TrafficSignalController controller = fourWayController();

        controller.forceGreen(Direction.SOUTH);

        assertEquals(EmergencyMode.PRIORITY_DIRECTION, controller.getEmergencyMode());
        assertOnlyGreen(controller, Direction.SOUTH);

        // CONCURRENCY/TESTABILITY: while override is active, ticks do not advance the normal plan.
        clock.advance(Duration.ofMinutes(10));
        controller.tick();
        assertOnlyGreen(controller, Direction.SOUTH);

        controller.clearOverride();
        assertEquals(EmergencyMode.NORMAL, controller.getEmergencyMode());
        assertOnlyGreen(controller, Direction.SOUTH);

        clock.advance(Duration.ofSeconds(10));
        controller.tick();
        assertEquals(SignalColor.YELLOW, controller.colorOf(Direction.SOUTH));

        clock.advance(Duration.ofSeconds(3));
        controller.tick();
        assertOnlyGreen(controller, Direction.WEST);
    }

    @Test
    void allRedOverrideForcesAllLightsRedAndThenRestoresPreviousDirection() {
        TrafficSignalController controller = fourWayController();

        clock.advance(Duration.ofSeconds(10));
        controller.tick();
        assertEquals(SignalColor.YELLOW, controller.colorOf(Direction.NORTH));

        controller.activateAllRedOverride();
        assertEquals(EmergencyMode.ALL_RED, controller.getEmergencyMode());
        assertEquals(0, greenCount(controller.snapshot()));

        clock.advance(Duration.ofSeconds(100));
        controller.tick();
        assertEquals(0, greenCount(controller.snapshot()));

        controller.clearOverride();
        assertOnlyGreen(controller, Direction.NORTH);
    }

    @Test
    void observersAreNotifiedOnEveryStateChange() {
        TrafficSignalController controller = fourWayController();
        List<String> events = new ArrayList<>();

        // DESIGN PATTERN: Observer — a logger/display can subscribe without changing controller code.
        controller.addObserver(event -> events.add(
                event.direction() + ":" + event.previousColor() + "->" + event.newColor()));

        clock.advance(Duration.ofSeconds(10));
        controller.tick();
        clock.advance(Duration.ofSeconds(3));
        controller.tick();

        assertEquals(List.of(
                "NORTH:GREEN->YELLOW",
                "NORTH:YELLOW->RED",
                "EAST:RED->GREEN"), events);
    }

    private void assertOnlyGreen(TrafficSignalController controller, Direction expectedGreen) {
        Map<Direction, SignalColor> snapshot = controller.snapshot();
        assertEquals(1, greenCount(snapshot));
        assertEquals(SignalColor.GREEN, snapshot.get(expectedGreen));
        assertTrue(controller.safetyInvariantHolds());
    }

    private long greenCount(Map<Direction, SignalColor> snapshot) {
        return snapshot.values().stream().filter(color -> color == SignalColor.GREEN).count();
    }
}
