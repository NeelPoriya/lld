package in.neelporiya.cricinfo;

import java.time.Instant;
import java.util.Objects;

/** One recorded ball in the scorebook. */
public record Delivery(
        int inningsNumber,
        int overNumber,
        int ballInOver,
        Player batter,
        Player bowler,
        BallOutcome outcome,
        Instant timestamp) {

    public Delivery {
        if (inningsNumber < 1) {
            throw new IllegalArgumentException("inningsNumber is 1-based");
        }
        if (overNumber < 1) {
            throw new IllegalArgumentException("overNumber is 1-based");
        }
        if (ballInOver < 1 || ballInOver > 6) {
            throw new IllegalArgumentException("ballInOver must be 1..6");
        }
        Objects.requireNonNull(batter, "batter");
        Objects.requireNonNull(bowler, "bowler");
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(timestamp, "timestamp");
    }
}
