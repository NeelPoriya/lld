package in.neelporiya.jobscheduler;

import java.time.Duration;

/**
 * // DESIGN PATTERN: Strategy — how many times to retry a failing job and how long to wait between
 * attempts. Fixed vs exponential backoff swap in without touching the scheduler.
 */
public interface RetryPolicy {

    /** Total attempts allowed (1 = no retries). */
    int maxAttempts();

    /** Delay before the given (1-based) retry attempt. */
    Duration backoff(int attempt);
}
