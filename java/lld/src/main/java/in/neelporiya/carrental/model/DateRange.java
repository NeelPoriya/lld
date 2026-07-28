package in.neelporiya.carrental.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class DateRange {

    private final LocalDate start;
    private final LocalDate end;

    public DateRange(LocalDate start, LocalDate end) {
        this.start = Objects.requireNonNull(start, "start");
        this.end = Objects.requireNonNull(end, "end");
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Rental range must be half-open with start before end");
        }
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public long days() {
        return ChronoUnit.DAYS.between(start, end);
    }

    public boolean overlaps(DateRange other) {
        Objects.requireNonNull(other, "other");
        // CONCURRENCY: The atomic reservation path relies on this exact half-open overlap rule.
        return start.isBefore(other.end) && other.start.isBefore(end);
    }
}
