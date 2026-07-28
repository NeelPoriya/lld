package in.neelporiya.restaurant;

import java.time.Instant;
import java.util.Objects;

/** Half-open interval [start, end) used for reservations. */
public record TimeSlot(Instant start, Instant end) {
    public TimeSlot {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("start must be before end");
        }
    }

    public boolean overlaps(TimeSlot other) {
        return start.isBefore(other.end) && end.isAfter(other.start);
    }
}
