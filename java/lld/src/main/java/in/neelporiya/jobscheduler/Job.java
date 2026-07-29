package in.neelporiya.jobscheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * // DESIGN PATTERN: Command — a unit of work as a first-class, queueable object bundling the task
 * with its schedule, priority, retry policy and runtime state.
 *
 * <p>Runtime fields ({@code nextRunTime}, {@code attempts}, {@code status}) are mutated only by the
 * scheduler; {@code status} is an {@link AtomicReference} so cancellation races safely with dispatch.
 */
public class Job {

    private final String id;
    private final Runnable task;
    private final int priority;
    private final Duration initialDelay;
    private final Duration interval; // null => one-shot
    private final RetryPolicy retryPolicy;

    private volatile Instant nextRunTime;
    private volatile int attempts;
    private final AtomicReference<JobStatus> status = new AtomicReference<>(JobStatus.SCHEDULED);

    private Job(Builder builder) {
        this.id = builder.id;
        this.task = builder.task;
        this.priority = builder.priority;
        this.initialDelay = builder.initialDelay;
        this.interval = builder.interval;
        this.retryPolicy = builder.retryPolicy;
    }

    // --- accessors used by the scheduler ---
    String getId() {
        return id;
    }

    Runnable getTask() {
        return task;
    }

    int getPriority() {
        return priority;
    }

    Duration getInitialDelay() {
        return initialDelay;
    }

    Duration getInterval() {
        return interval;
    }

    boolean isRecurring() {
        return interval != null;
    }

    RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    Instant getNextRunTime() {
        return nextRunTime;
    }

    void setNextRunTime(Instant nextRunTime) {
        this.nextRunTime = nextRunTime;
    }

    int getAttempts() {
        return attempts;
    }

    void incrementAttempts() {
        this.attempts++;
    }

    void resetAttempts() {
        this.attempts = 0;
    }

    public JobStatus getStatus() {
        return status.get();
    }

    void setStatus(JobStatus newStatus) {
        status.set(newStatus);
    }

    boolean compareAndSetStatus(JobStatus expected, JobStatus newStatus) {
        return status.compareAndSet(expected, newStatus);
    }

    public String id() {
        return id;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private Runnable task;
        private int priority = 0;
        private Duration initialDelay = Duration.ZERO;
        private Duration interval;
        private RetryPolicy retryPolicy = new NoRetryPolicy();

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder task(Runnable task) {
            this.task = task;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder delay(Duration initialDelay) {
            this.initialDelay = Objects.requireNonNull(initialDelay, "initialDelay");
            return this;
        }

        public Builder interval(Duration interval) {
            this.interval = interval;
            return this;
        }

        public Builder retryPolicy(RetryPolicy retryPolicy) {
            this.retryPolicy = Objects.requireNonNull(retryPolicy, "retryPolicy");
            return this;
        }

        public Job build() {
            Objects.requireNonNull(task, "task");
            return new Job(this);
        }
    }
}
