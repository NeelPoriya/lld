package in.neelporiya.trafficsignal;

import java.util.List;

/**
 * // DESIGN PATTERN: Strategy — fixed-time today, adaptive/traffic-sensor timing tomorrow.
 */
public interface SignalTimingPlan {

    List<Direction> directionsInOrder();

    SignalDurations durationsFor(Direction direction);

    Direction firstGreenDirection();

    Direction nextGreenDirection(Direction currentDirection);
}
