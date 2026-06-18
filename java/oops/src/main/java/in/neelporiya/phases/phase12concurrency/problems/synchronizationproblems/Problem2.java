package in.neelporiya.phases.phase12concurrency.problems.synchronizationproblems;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.IntConsumer;

/**
 * <h1>Problem Statement</h1>
 * <p>
 * Three threads are given: zero(), even(), and odd(). Design a mechanism so they cooperate to print the sequence "0102030405...0n" for a given n.
 * <p>
 * The Setup
 * Here's what each thread does.
 * <p>
 * Thread 1 (zero): Calls zero() in a loop. Each call should print "0".
 * Thread 2 (even): Calls even() in a loop. Each call should print an even number (2, 4, 6, ...).
 * Thread 3 (odd): Calls odd() in a loop. Each call should print an odd number (1, 3, 5, ...).
 * Output: For n=5, the output must be exactly "0102030405".
 * Notice the asymmetry: the zero thread runs n times, but odd and even together also run n times, alternating. For n=5, zero prints 5 times, odd prints 3 times (1, 3, 5), and even prints 2 times (2, 4). This uneven distribution adds complexity to loop bounds and termination.
 * <p>
 * The Rules
 * With the roles clear, here are the ordering constraints that define correctness.
 * <p>
 * "0" is printed before every non-zero number.
 * Odd numbers (1, 3, 5, ...) and even numbers (2, 4, 6, ...) alternate.
 * The sequence is: 0, 1, 0, 2, 0, 3, 0, 4, 0, 5, ... up to n.
 * Thread zero runs n times, thread odd runs ceil(n/2) times, thread even runs floor(n/2) times.
 * The pattern becomes clear when you trace through it: zero always goes, then odd or even takes a turn, then back to zero. The tricky part is that zero must know whether to wake odd or even after each print.
 * <p>
 * The Goal
 * Design a synchronization mechanism that:
 * <p>
 * Correct ordering: Output is exactly "0102030405...0n".
 * No deadlock: All threads complete their iterations.
 * Efficient: Threads don't busy-wait or waste CPU cycles.
 */
public class Problem2 {
    private final int n = 5;
    private final Semaphore zeroSem = new Semaphore(1);
    private final Semaphore oddSem = new Semaphore(0);
    private final Semaphore evenSem = new Semaphore(0);
    private final IntConsumer consumer = new IntConsumer() {
        @Override
        public void accept(int value) {
            System.out.print(value);
        }
    };

    private void printSeries(Semaphore sem, int start) {
        try {
            for (int i = start; i <= n; i += 2) {
                sem.acquire();
                consumer.accept(i);
                zeroSem.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void zero() {
        try {
            for (int i = 1; i <= n; ++i) {
                zeroSem.acquire();
                consumer.accept(0);
                if (i % 2 == 1) oddSem.release();
                else evenSem.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    void main() {
        List<Runnable> tasks = List.of(
                this::zero,
                () -> printSeries(oddSem, 1),
                () -> printSeries(evenSem, 2)
        );

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (Runnable r : tasks) {
                executor.submit(r);
            }
        }
    }
}
