package in.neelporiya.cricinfo;

import java.util.Optional;

/** Final result after both innings complete. */
public record MatchResult(Optional<Team> winner, String summary) {

    public static MatchResult wonByRuns(Team winner, int runs) {
        return new MatchResult(Optional.of(winner), winner.getName() + " won by " + runs + " runs");
    }

    public static MatchResult wonByWickets(Team winner, int wickets) {
        return new MatchResult(Optional.of(winner), winner.getName() + " won by " + wickets + " wickets");
    }

    public static MatchResult tie() {
        return new MatchResult(Optional.empty(), "Match tied");
    }
}
