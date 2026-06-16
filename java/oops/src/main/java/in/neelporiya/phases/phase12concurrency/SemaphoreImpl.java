package in.neelporiya.phases.phase12concurrency;

import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Semaphore implementation using Condition variables.
 */
public class SemaphoreImpl {

    public static class Semaphore {
        // Idiomatic way: Count down available permits instead of counting up used capacity.
        private int availablePermits;
        private final int maxPermits;

        private final ReentrantLock lock = new ReentrantLock(true);
        // A Semaphore only needs ONE condition: waiting for permits to become available.
        private final Condition permitsAvailable = lock.newCondition();

        public Semaphore(int maxPermits) {
            this.maxPermits = maxPermits;
            this.availablePermits = maxPermits; // Start with all permits available
        }

        public void acquire() {
            lock.lock();
            try {
                // Wait while there are 0 permits available
                while (availablePermits == 0) {
                    permitsAvailable.await();
                }

                // We got the permit! Decrement availability.
                availablePermits--;

                // Notice we DO NOT need to signal anything here.
                // Acquiring a permit doesn't help other threads waiting to acquire.

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupt status
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }

        public void release() {
            lock.lock();
            try {
                // Bounded Semaphore logic: Don't exceed max capacity
                if (availablePermits < maxPermits) {
                    availablePermits++;

                    // We just freed up a permit! Wake up ONE waiting thread.
                    permitsAvailable.signal();
                } else {
                    // Releasing an unacquired permit should throw an error, NOT block.
                    throw new IllegalStateException("Semaphore capacity exceeded!");
                }
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) { // Note: changed to standard main signature
        Semaphore sem = new Semaphore(4);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 5; ++i) {
                executor.submit(() -> {
                    for (int j = 0; j < 3; ++j) {
                        sem.acquire();

                        System.out.printf("%s, Task %d is now being processed. Waiting 2 seconds...\n", Thread.currentThread().threadId(), j);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException(e);
                        } finally {
                            sem.release();
                        }
                    }
                });
            }
        }

        System.out.println("Finished...");
    }
}