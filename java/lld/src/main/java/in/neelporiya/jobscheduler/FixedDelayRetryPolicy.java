package in.neelporiya.jobscheduler;

import java.time.Duration;

/** Retry up to {@code maxAttempts} times, waiting a constant {@code delay} between attempts. */
public class FixedDelayRetryPolicy implements RetryPolicy {

    private final int maxAttempts;
    private final Duration delay;

    public FixedDelayRetryPolicy(int maxAttempts, Duration delay) {
        this.maxAttempts = maxAttempts;
        this.delay = delay;
    }

    @Override
    public int maxAttempts() {
        return maxAttempts;
    }

    @Override
    public Duration backoff(int attempt) {
        return delay;
    }
}
