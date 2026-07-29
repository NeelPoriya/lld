package in.neelporiya.jobscheduler;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * // DESIGN PATTERN: Facade. A priority scheduler that decides which jobs are due (from an injected
 * {@link Clock}) and dispatches them to an injected {@link Executor}.
 *
 * <p>// TESTABILITY: pass an inline executor ({@code Runnable::run}) and a {@code MutableClock} to
 * make execution and time fully deterministic; production passes a thread pool and the system clock,
 * with a small loop calling {@link #runDueJobs()}.
 */
public class JobScheduler {

    // Order: earliest due first; ties broken by higher priority first.
    private static final Comparator<Job> DUE_ORDER = Comparator
            .comparing(Job::getNextRunTime)
            .thenComparing(Comparator.comparingInt(Job::getPriority).reversed());

    private final PriorityQueue<Job> queue = new PriorityQueue<>(DUE_ORDER);
    private final Map<String, Job> jobsById = new ConcurrentHashMap<>();
    private final List<JobExecutionListener> listeners = new CopyOnWriteArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    private final Clock clock;
    private final Executor executor;
    private final Supplier<String> idGenerator;

    public JobScheduler(Clock clock, Executor executor) {
        this(clock, executor, () -> UUID.randomUUID().toString());
    }

    public JobScheduler(Clock clock, Executor executor, Supplier<String> idGenerator) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }

    public void addListener(JobExecutionListener listener) {
        listeners.add(listener);
    }

    /** Schedule a job; its first run is {@code now + delay}. */
    public String schedule(Job job) {
        String id = job.getId() != null ? job.getId() : idGenerator.get();
        job.setNextRunTime(clock.instant().plus(job.getInitialDelay()));
        job.setStatus(JobStatus.SCHEDULED);
        jobsById.put(id, job);
        lock.lock();
        try {
            queue.add(job);
        } finally {
            lock.unlock();
        }
        return id;
    }

    /**
     * Execute every job whose next-run-time has arrived (per the injected clock), in priority order.
     *
     * @return how many jobs were dispatched.
     */
    public int runDueJobs() {
        Instant now = clock.instant();
        List<Job> due = new ArrayList<>();
        // CONCURRENCY: drain due jobs under the lock, then dispatch OUTSIDE it.
        lock.lock();
        try {
            while (!queue.isEmpty() && !queue.peek().getNextRunTime().isAfter(now)) {
                due.add(queue.poll());
            }
        } finally {
            lock.unlock();
        }

        int dispatched = 0;
        for (Job job : due) {
            if (job.getStatus() == JobStatus.CANCELLED) {
                continue; // cancelled while queued -> skip
            }
            job.setStatus(JobStatus.RUNNING);
            dispatched++;
            executor.execute(() -> execute(job));
        }
        return dispatched;
    }

    private void execute(Job job) {
        listeners.forEach(l -> l.onStart(job));
        try {
            job.getTask().run();
            if (job.isRecurring() && job.getStatus() != JobStatus.CANCELLED) {
                job.resetAttempts();
                job.setNextRunTime(clock.instant().plus(job.getInterval()));
                reschedule(job, JobStatus.SCHEDULED);
            } else {
                job.setStatus(JobStatus.COMPLETED);
            }
            listeners.forEach(l -> l.onSuccess(job));
        } catch (RuntimeException error) {
            job.incrementAttempts();
            if (job.getAttempts() < job.getRetryPolicy().maxAttempts() && job.getStatus() != JobStatus.CANCELLED) {
                Duration backoff = job.getRetryPolicy().backoff(job.getAttempts());
                job.setNextRunTime(clock.instant().plus(backoff));
                reschedule(job, JobStatus.RETRYING);
                listeners.forEach(l -> l.onRetry(job, error));
            } else {
                job.setStatus(JobStatus.FAILED);
                listeners.forEach(l -> l.onFailure(job, error));
            }
        }
    }

    private void reschedule(Job job, JobStatus status) {
        if (job.getStatus() == JobStatus.CANCELLED) {
            return;
        }
        job.setStatus(status);
        lock.lock();
        try {
            queue.add(job);
        } finally {
            lock.unlock();
        }
    }

    /** Cancel a job. A running task still finishes but won't be rescheduled. */
    public boolean cancel(String jobId) {
        Job job = jobsById.get(jobId);
        if (job == null) {
            return false;
        }
        JobStatus current = job.getStatus();
        if (current == JobStatus.COMPLETED || current == JobStatus.FAILED) {
            return false;
        }
        job.setStatus(JobStatus.CANCELLED);
        lock.lock();
        try {
            queue.remove(job);
        } finally {
            lock.unlock();
        }
        listeners.forEach(l -> l.onCancel(job));
        return true;
    }

    public JobStatus statusOf(String jobId) {
        Job job = jobsById.get(jobId);
        return job == null ? null : job.getStatus();
    }

    public int pendingCount() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }
}
