package in.neelporiya.meetingscheduler;

import java.time.Duration;
import java.time.Instant;

/**
 * A half-open time interval {@code [start, end)}.
 *
 * <p>// INTERVIEW INSIGHT: half-open semantics are deliberate — a 10:00–11:00 meeting and an
 * 11:00–12:00 meeting are back-to-back, NOT a conflict. The overlap rule is the one-liner everyone
 * should know cold: two intervals overlap iff {@code a.start < b.end && b.start < a.end}.
 */
public record TimeInterval(Instant start, Instant end) {

    public TimeInterval {
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("start must be strictly before end");
        }
    }

    public boolean overlaps(TimeInterval other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    public Duration duration() {
        return Duration.between(start, end);
    }
}
