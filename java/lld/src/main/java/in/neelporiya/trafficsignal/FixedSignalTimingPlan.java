package in.neelporiya.trafficsignal;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A deterministic round-robin timing plan.
 *
 * <p>// EXTENSIBILITY: replace this with an adaptive implementation that reads sensors without
 * changing {@link TrafficSignalController}.
 */
public class FixedSignalTimingPlan implements SignalTimingPlan {

    private final List<Direction> order;
    private final Map<Direction, SignalDurations> durationsByDirection;

    public FixedSignalTimingPlan(List<Direction> order, SignalDurations durations) {
        this(order, mapAll(order, durations));
    }

    public FixedSignalTimingPlan(List<Direction> order, Map<Direction, SignalDurations> durationsByDirection) {
        if (order == null || order.isEmpty()) {
            throw new IllegalArgumentException("At least one direction is required");
        }
        this.order = List.copyOf(order);
        this.durationsByDirection = new EnumMap<>(Direction.class);
        for (Direction direction : this.order) {
            this.durationsByDirection.put(direction,
                    Objects.requireNonNull(durationsByDirection.get(direction), "duration for " + direction));
        }
    }

    public static FixedSignalTimingPlan fourWay(SignalDurations durations) {
        return new FixedSignalTimingPlan(
                List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST),
                durations);
    }

    @Override
    public List<Direction> directionsInOrder() {
        return order;
    }

    @Override
    public SignalDurations durationsFor(Direction direction) {
        SignalDurations durations = durationsByDirection.get(direction);
        if (durations == null) {
            throw new IllegalArgumentException("Unknown direction in plan: " + direction);
        }
        return durations;
    }

    @Override
    public Direction firstGreenDirection() {
        return order.getFirst();
    }

    @Override
    public Direction nextGreenDirection(Direction currentDirection) {
        int index = order.indexOf(currentDirection);
        if (index < 0) {
            throw new IllegalArgumentException("Unknown direction in plan: " + currentDirection);
        }
        return order.get((index + 1) % order.size());
    }

    private static Map<Direction, SignalDurations> mapAll(List<Direction> order, SignalDurations durations) {
        Objects.requireNonNull(durations, "durations");
        Map<Direction, SignalDurations> map = new EnumMap<>(Direction.class);
        for (Direction direction : new ArrayList<>(Objects.requireNonNull(order, "order"))) {
            map.put(direction, durations);
        }
        return map;
    }
}
