package in.neelporiya.phases.phase12concurrency.problems.synchronizationproblems;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * We want to print "Foo" and "Bar" alternatively multiple (3) times.
 */
public class Problem1 {
    private static final int TIMES = 3;
    private static final Semaphore fooSemaphore = new Semaphore(1);
    private static final Semaphore barSemaphore = new Semaphore(0);

    private static void printWord(Semaphore acquireSem, Semaphore releaseSem, String word) {
        for (int i = 0; i < TIMES; ++i) {
            try {
                acquireSem.acquire();
                // Adding sleep to visualize the printing order.
                Thread.sleep(1000);

                System.out.print(word);
                releaseSem.release();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    static void main() {
        List<Runnable> tasks = List.of(
                () -> printWord(fooSemaphore, barSemaphore, "Foo"),
                () -> printWord(barSemaphore, fooSemaphore, "Bar")
        );

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (Runnable r : tasks) {
                executor.submit(r);
            }
        }
    }
}
