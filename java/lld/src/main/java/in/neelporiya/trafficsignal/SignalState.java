package in.neelporiya.trafficsignal;

import java.time.Duration;

/**
 * // DESIGN PATTERN: State — RED/GREEN/YELLOW are objects with their own transition and duration.
 *
 * <p>The controller asks the current state how long it should stay active and what comes next; it
 * does not hard-code a pile of if/else transition durations.
 */
public interface SignalState {

    SignalColor color();

    Duration duration();

    SignalState next();
}
