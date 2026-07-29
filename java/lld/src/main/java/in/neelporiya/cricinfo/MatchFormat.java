package in.neelporiya.cricinfo;

/**
 * // DESIGN PATTERN: Strategy — format-specific rules are pluggable.
 *
 * <p>// EXTENSIBILITY: add The Hundred, Test-match sessions, rain-shortened DLS rules, or
 * super overs by implementing this interface; Match does not need to know the concrete format.
 */
public interface MatchFormat {

    String name();

    int oversPerInnings();

    default int maxLegalDeliveries() {
        return oversPerInnings() * 6;
    }

    default boolean isInningsComplete(Innings innings) {
        Scorecard scorecard = innings.getScorecard();
        int allOutWickets = innings.getBattingTeam().getPlayers().size() - 1;
        return scorecard.getLegalDeliveries() >= maxLegalDeliveries()
                || scorecard.getWickets() >= allOutWickets;
    }

    static MatchFormat t20() {
        return new LimitedOversFormat("T20", 20);
    }

    static MatchFormat odi() {
        return new LimitedOversFormat("ODI", 50);
    }

    static MatchFormat limitedOvers(int overs) {
        return new LimitedOversFormat(overs + "-over", overs);
    }
}
