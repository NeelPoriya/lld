package in.neelporiya.cricinfo;

import java.time.Clock;
import java.util.Objects;

/** // DESIGN PATTERN: Factory — named constructors capture common interview setup paths. */
public final class MatchFactory {

    private MatchFactory() {
    }

    public static Match t20(String id, Team teamA, Team teamB, Team battingFirst, Clock clock) {
        return limitedOvers(id, teamA, teamB, battingFirst, MatchFormat.t20(), clock);
    }

    public static Match odi(String id, Team teamA, Team teamB, Team battingFirst, Clock clock) {
        return limitedOvers(id, teamA, teamB, battingFirst, MatchFormat.odi(), clock);
    }

    public static Match limitedOvers(String id, Team teamA, Team teamB, Team battingFirst,
                                     MatchFormat format, Clock clock) {
        Objects.requireNonNull(id, "id");
        return Match.builder()
                .idGenerator(() -> id)
                .teams(teamA, teamB)
                .battingFirst(battingFirst)
                .format(format)
                .clock(clock)
                .build();
    }
}
