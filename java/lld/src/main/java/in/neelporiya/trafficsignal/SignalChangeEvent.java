package in.neelporiya.trafficsignal;

import java.time.Instant;

/** Immutable event emitted whenever a TrafficLight changes visible state. */
public record SignalChangeEvent(
        String lightId,
        Direction direction,
        SignalColor previousColor,
        SignalColor newColor,
        Instant changedAt) {
}
