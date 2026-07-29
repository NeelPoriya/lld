package in.neelporiya.cricinfo;

import java.time.Instant;
import java.util.Objects;

/** Immutable event sent after every recorded delivery. */
public record ScoreUpdate(
        String matchId,
        int inningsNumber,
        Delivery delivery,
        Scorecard scorecard,
        MatchStatus matchStatus,
        Instant timestamp) {

    public ScoreUpdate {
        Objects.requireNonNull(matchId, "matchId");
        Objects.requireNonNull(delivery, "delivery");
        Objects.requireNonNull(scorecard, "scorecard");
        Objects.requireNonNull(matchStatus, "matchStatus");
        Objects.requireNonNull(timestamp, "timestamp");
    }
}
