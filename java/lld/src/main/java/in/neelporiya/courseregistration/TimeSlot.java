package in.neelporiya.courseregistration;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

/**
 * A weekly course meeting slot.
 *
 * <p>// INTERVIEW INSIGHT: Keeping conflict logic in this tiny value object prevents the facade from
 * becoming a bag of date/time conditionals.
 */
public record TimeSlot(DayOfWeek day, LocalTime start, LocalTime end) {

    public TimeSlot {
        Objects.requireNonNull(day, "day");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("start must be before end");
        }
    }

    public boolean overlaps(TimeSlot other) {
        Objects.requireNonNull(other, "other");
        return day == other.day && start.isBefore(other.end) && other.start.isBefore(end);
    }
}
