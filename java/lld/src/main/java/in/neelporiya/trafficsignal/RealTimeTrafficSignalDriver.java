package in.neelporiya.trafficsignal;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Optional production adapter that periodically calls {@link TrafficSignalController#tick()}.
 *
 * <p>// TESTABILITY: the core controller does not depend on this class. Unit tests call tick()
 * directly with a MutableClock; production can wire this scheduler with Clock.systemUTC().
 *
 * <p>// CONCURRENCY: the scheduler is single-threaded and stoppable; controller state remains guarded
 * by its own lock, so manual ticks and scheduled ticks are still safe if both happen.
 */
public class RealTimeTrafficSignalDriver implements AutoCloseable {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public RealTimeTrafficSignalDriver(TrafficSignalController controller, Duration tickInterval) {
        Objects.requireNonNull(controller, "controller");
        Objects.requireNonNull(tickInterval, "tickInterval");
        if (tickInterval.isZero() || tickInterval.isNegative()) {
            throw new IllegalArgumentException("tickInterval must be positive");
        }
        executor.scheduleAtFixedRate(
                controller::tick,
                0,
                tickInterval.toMillis(),
                TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    @Override
    public void close() {
        shutdown();
    }
}
