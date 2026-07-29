package in.neelporiya.jobscheduler;

import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobSchedulerTest {

    // Inline executor: a dispatched job runs synchronously, so assertions are immediate.
    private static final Executor INLINE = Runnable::run;

    private final MutableClock clock = MutableClock.atEpoch();

    private JobScheduler scheduler() {
        AtomicInteger seq = new AtomicInteger();
        return new JobScheduler(clock, INLINE, () -> "j" + seq.incrementAndGet());
    }

    @Test
    void oneShotRunsOnlyOnceItIsDue() {
        JobScheduler scheduler = scheduler();
        AtomicInteger runs = new AtomicInteger();
        String id = scheduler.schedule(Job.builder().task(runs::incrementAndGet).delay(Duration.ofSeconds(10)).build());

        assertEquals(0, scheduler.runDueJobs(), "not due yet");
        assertEquals(0, runs.get());

        clock.advance(Duration.ofSeconds(10));
        assertEquals(1, scheduler.runDueJobs());
        assertEquals(1, runs.get());
        assertEquals(JobStatus.COMPLETED, scheduler.statusOf(id));
    }

    @Test
    void higherPriorityRunsFirstAmongJobsDueTogether() {
        JobScheduler scheduler = scheduler();
        List<String> order = new ArrayList<>();
        scheduler.schedule(Job.builder().priority(1).task(() -> order.add("low")).build());
        scheduler.schedule(Job.builder().priority(5).task(() -> order.add("high")).build());

        scheduler.runDueJobs();

        assertEquals(List.of("high", "low"), order);
    }

    @Test
    void recurringJobRunsEveryInterval() {
        JobScheduler scheduler = scheduler();
        AtomicInteger runs = new AtomicInteger();
        scheduler.schedule(Job.builder().task(runs::incrementAndGet).interval(Duration.ofSeconds(5)).build());

        scheduler.runDueJobs();                 // t=0
        clock.advance(Duration.ofSeconds(5));
        scheduler.runDueJobs();                 // t=5
        clock.advance(Duration.ofSeconds(5));
        scheduler.runDueJobs();                 // t=10

        assertEquals(3, runs.get());
        assertEquals(1, scheduler.pendingCount(), "recurring job re-enqueues itself");
    }

    @Test
    void failingJobRetriesUpToMaxThenFails() {
        JobScheduler scheduler = scheduler();
        AtomicInteger runs = new AtomicInteger();
        AtomicInteger retries = new AtomicInteger();
        AtomicInteger failures = new AtomicInteger();
        scheduler.addListener(new JobExecutionListener() {
            @Override
            public void onRetry(Job job, Exception error) {
                retries.incrementAndGet();
            }

            @Override
            public void onFailure(Job job, Exception error) {
                failures.incrementAndGet();
            }
        });
        String id = scheduler.schedule(Job.builder()
                .task(() -> {
                    runs.incrementAndGet();
                    throw new RuntimeException("boom");
                })
                .retryPolicy(new FixedDelayRetryPolicy(3, Duration.ofSeconds(1)))
                .build());

        scheduler.runDueJobs();                 // attempt 1
        clock.advance(Duration.ofSeconds(1));
        scheduler.runDueJobs();                 // attempt 2
        clock.advance(Duration.ofSeconds(1));
        scheduler.runDueJobs();                 // attempt 3 -> gives up

        assertEquals(3, runs.get());
        assertEquals(2, retries.get());
        assertEquals(1, failures.get());
        assertEquals(JobStatus.FAILED, scheduler.statusOf(id));
    }

    @Test
    void retryingJobCanEventuallySucceed() {
        JobScheduler scheduler = scheduler();
        AtomicInteger runs = new AtomicInteger();
        String id = scheduler.schedule(Job.builder()
                .task(() -> {
                    if (runs.incrementAndGet() < 2) {
                        throw new RuntimeException("transient");
                    }
                })
                .retryPolicy(new FixedDelayRetryPolicy(3, Duration.ofSeconds(1)))
                .build());

        scheduler.runDueJobs();                 // fails, schedules retry
        clock.advance(Duration.ofSeconds(1));
        scheduler.runDueJobs();                 // succeeds

        assertEquals(2, runs.get());
        assertEquals(JobStatus.COMPLETED, scheduler.statusOf(id));
    }

    @Test
    void cancelledJobDoesNotRun() {
        JobScheduler scheduler = scheduler();
        AtomicInteger runs = new AtomicInteger();
        String id = scheduler.schedule(Job.builder().task(runs::incrementAndGet).delay(Duration.ofSeconds(10)).build());

        assertTrue(scheduler.cancel(id));
        assertEquals(JobStatus.CANCELLED, scheduler.statusOf(id));
        assertEquals(0, scheduler.pendingCount());

        clock.advance(Duration.ofSeconds(10));
        assertEquals(0, scheduler.runDueJobs());
        assertEquals(0, runs.get());
    }

    @Test
    void cancellingACompletedJobReturnsFalse() {
        JobScheduler scheduler = scheduler();
        String id = scheduler.schedule(Job.builder().task(() -> {}).build());
        scheduler.runDueJobs();
        assertEquals(JobStatus.COMPLETED, scheduler.statusOf(id));
        assertFalse(scheduler.cancel(id));
    }
}
