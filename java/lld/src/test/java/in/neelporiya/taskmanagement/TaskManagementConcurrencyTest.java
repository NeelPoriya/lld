package in.neelporiya.taskmanagement;

import in.neelporiya.taskmanagement.exception.InvalidTransitionException;
import in.neelporiya.taskmanagement.model.Task;
import in.neelporiya.taskmanagement.model.TaskStatus;
import in.neelporiya.taskmanagement.service.TaskManagementService;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // CONCURRENCY: many threads race to perform the SAME legal transition on one task. The per-task
 * lock + workflow rule must let exactly one succeed; the rest see an illegal transition (because the
 * status has already moved). The version must advance exactly once — proof there was no double apply.
 */
class TaskManagementConcurrencyTest {

    @Test
    void concurrentIdenticalTransitionAppliesExactlyOnce() throws InterruptedException {
        AtomicInteger seq = new AtomicInteger();
        TaskManagementService service =
                new TaskManagementService(MutableClock.atEpoch(), () -> "T" + seq.incrementAndGet());
        Task task = service.createTask("t", "d",
                in.neelporiya.taskmanagement.model.Priority.MEDIUM, Set.of(), null);

        int threads = 64;
        ExecutorService pool = Executors.newFixedThreadPool(16);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    startGun.await();
                    service.changeStatus(task.getId(), TaskStatus.IN_PROGRESS);
                    successes.incrementAndGet();
                } catch (InvalidTransitionException expected) {
                    rejected.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        startGun.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS));
        pool.shutdownNow();

        assertEquals(1, successes.get(), "only one thread can perform the TODO->IN_PROGRESS move");
        assertEquals(threads - 1, rejected.get());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals(1, task.getVersion(), "exactly one mutation must have been applied");
    }
}
