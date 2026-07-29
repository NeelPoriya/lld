package in.neelporiya.cricinfo;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Match workflow encoded as data.
 *
 * <p>// DESIGN PATTERN: State machine — legal transitions live in one map, not scattered if/else
 * checks. COMPLETED is terminal, so a finished match cannot silently be reopened.
 */
public enum MatchStatus {
    SCHEDULED,
    IN_PROGRESS,
    INNINGS_BREAK,
    COMPLETED;

    private static final Map<MatchStatus, Set<MatchStatus>> ALLOWED = new EnumMap<>(MatchStatus.class);

    static {
        ALLOWED.put(SCHEDULED, EnumSet.of(IN_PROGRESS));
        ALLOWED.put(IN_PROGRESS, EnumSet.of(INNINGS_BREAK, COMPLETED));
        ALLOWED.put(INNINGS_BREAK, EnumSet.of(IN_PROGRESS));
        ALLOWED.put(COMPLETED, EnumSet.noneOf(MatchStatus.class));
    }

    public boolean canTransitionTo(MatchStatus next) {
        return ALLOWED.get(this).contains(next);
    }
}
