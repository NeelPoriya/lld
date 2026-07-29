package in.neelporiya.cricinfo;

/** T20/ODI-style limited-overs format. */
public record LimitedOversFormat(String name, int oversPerInnings) implements MatchFormat {
    public LimitedOversFormat {
        if (oversPerInnings <= 0) {
            throw new IllegalArgumentException("oversPerInnings must be positive");
        }
    }
}
