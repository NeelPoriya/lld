package in.neelporiya.jobscheduler;

import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // CONCURRENCY: with a real thread pool as the executor, all jobs due at once are dispatched and
 * run in parallel. We assert every job runs exactly once.
 */
class JobSchedulerConcurrencyTest {

    @Test
    void allDueJobsRunOnAThreadPool() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        JobScheduler scheduler = new JobScheduler(MutableClock.atEpoch(), pool);

        int jobCount = 200;
        AtomicInteger runs = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(jobCount);
        for (int i = 0; i < jobCount; i++) {
            scheduler.schedule(Job.builder().task(() -> {
                runs.incrementAndGet();
                latch.countDown();
            }).build());
        }

        int dispatched = scheduler.runDueJobs();
        assertEquals(jobCount, dispatched);
        assertTrue(latch.await(10, TimeUnit.SECONDS), "all jobs should run");
        pool.shutdownNow();

        assertEquals(jobCount, runs.get());
    }
}
