package in.neelporiya.cricinfo;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** State machine for an innings. */
public enum InningsStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED;

    private static final Map<InningsStatus, Set<InningsStatus>> ALLOWED = new EnumMap<>(InningsStatus.class);

    static {
        ALLOWED.put(NOT_STARTED, EnumSet.of(IN_PROGRESS));
        ALLOWED.put(IN_PROGRESS, EnumSet.of(COMPLETED));
        ALLOWED.put(COMPLETED, EnumSet.noneOf(InningsStatus.class));
    }

    public boolean canTransitionTo(InningsStatus next) {
        return ALLOWED.get(this).contains(next);
    }
}
