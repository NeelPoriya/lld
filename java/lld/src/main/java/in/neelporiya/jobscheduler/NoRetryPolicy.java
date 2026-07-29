package in.neelporiya.jobscheduler;

import java.time.Duration;

/** Run once, never retry. */
public class NoRetryPolicy implements RetryPolicy {

    @Override
    public int maxAttempts() {
        return 1;
    }

    @Override
    public Duration backoff(int attempt) {
        return Duration.ZERO;
    }
}
