package in.neelporiya.cricinfo;

/**
 * Immutable value object describing what happened on one delivery.
 *
 * <p>// INTERVIEW INSIGHT: cricket scoring has two counters: team runs and legal balls. Wides and
 * no-balls add runs but do not consume a legal ball; keeping that rule inside this value object
 * prevents every caller from re-implementing it incorrectly.
 */
public record BallOutcome(int batterRuns, int extraRuns, ExtraType extraType, boolean wicket) {

    public BallOutcome {
        if (batterRuns < 0 || batterRuns > 6) {
            throw new IllegalArgumentException("Batter runs must be between 0 and 6");
        }
        if (extraRuns < 0) {
            throw new IllegalArgumentException("Extra runs cannot be negative");
        }
        extraType = extraType == null ? ExtraType.NONE : extraType;
        if (extraType == ExtraType.NONE && extraRuns != 0) {
            throw new IllegalArgumentException("Extra runs need an extra type");
        }
    }

    public static BallOutcome runs(int runs) {
        return new BallOutcome(runs, 0, ExtraType.NONE, false);
    }

    public static BallOutcome wicketBall() {
        return new BallOutcome(0, 0, ExtraType.NONE, true);
    }

    public static BallOutcome runsAndWicket(int runs) {
        return new BallOutcome(runs, 0, ExtraType.NONE, true);
    }

    public static BallOutcome wide() {
        return new BallOutcome(0, 1, ExtraType.WIDE, false);
    }

    public static BallOutcome noBall(int batterRuns) {
        return new BallOutcome(batterRuns, 1, ExtraType.NO_BALL, false);
    }

    public int totalRuns() {
        return batterRuns + extraRuns;
    }

    public boolean isLegalDelivery() {
        return extraType == ExtraType.NONE;
    }
}
