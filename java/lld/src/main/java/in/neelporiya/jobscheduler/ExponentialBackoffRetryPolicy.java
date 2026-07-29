package in.neelporiya.jobscheduler;

import java.time.Duration;

/** Retry with exponentially growing backoff: {@code base * 2^(attempt-1)}. */
public class ExponentialBackoffRetryPolicy implements RetryPolicy {

    private final int maxAttempts;
    private final Duration base;

    public ExponentialBackoffRetryPolicy(int maxAttempts, Duration base) {
        this.maxAttempts = maxAttempts;
        this.base = base;
    }

    @Override
    public int maxAttempts() {
        return maxAttempts;
    }

    @Override
    public Duration backoff(int attempt) {
        return base.multipliedBy(1L << (attempt - 1)); // base, 2*base, 4*base, ...
    }
}
