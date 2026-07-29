package in.neelporiya.ridesharing;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Ride lifecycle encoded as a data-driven state machine.
 *
 * <p>// DESIGN PATTERN: State machine. Terminal states have empty transition sets, so completed,
 * cancelled, and no-driver rides cannot silently reopen.
 */
public enum RideStatus {
    REQUESTED,
    MATCHED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    NO_DRIVERS;

    private static final Map<RideStatus, Set<RideStatus>> ALLOWED = new EnumMap<>(RideStatus.class);

    static {
        ALLOWED.put(REQUESTED, EnumSet.of(MATCHED, CANCELLED, NO_DRIVERS));
        ALLOWED.put(MATCHED, EnumSet.of(IN_PROGRESS, CANCELLED));
        ALLOWED.put(IN_PROGRESS, EnumSet.of(COMPLETED));
        ALLOWED.put(COMPLETED, EnumSet.noneOf(RideStatus.class));
        ALLOWED.put(CANCELLED, EnumSet.noneOf(RideStatus.class));
        ALLOWED.put(NO_DRIVERS, EnumSet.noneOf(RideStatus.class));
    }

    public boolean canTransitionTo(RideStatus next) {
        return ALLOWED.get(this).contains(next);
    }
}
