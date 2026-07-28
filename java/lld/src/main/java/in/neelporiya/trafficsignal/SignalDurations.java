package in.neelporiya.trafficsignal;

import java.time.Duration;
import java.util.Objects;

/** Per-light configurable durations for the State objects. */
public record SignalDurations(Duration red, Duration green, Duration yellow) {

    public SignalDurations {
        Objects.requireNonNull(red, "red");
        Objects.requireNonNull(green, "green");
        Objects.requireNonNull(yellow, "yellow");
        requirePositive(red, "red");
        requirePositive(green, "green");
        requirePositive(yellow, "yellow");
    }

    private static void requirePositive(Duration duration, String name) {
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(name + " duration must be positive");
        }
    }

    public static SignalDurations ofSeconds(long red, long green, long yellow) {
        return new SignalDurations(
                Duration.ofSeconds(red),
                Duration.ofSeconds(green),
                Duration.ofSeconds(yellow));
    }
}
