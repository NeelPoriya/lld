package in.neelporiya.phases.phase12concurrency;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomCompareAndSwap {
    public static class CustomAtomicInteger {
        private volatile int value;

        public void increment() {
            int expectedValue, newValue;
            do {
                expectedValue = value;
                newValue = value + 1;
            } while (!compareAndSwap(expectedValue, newValue));
        }

        private synchronized boolean compareAndSwap(int expected, int newValue) {
            if (this.value == expected) {
                this.value = newValue;
                return true;
            }
            return false;
        }

        public synchronized void printValue() {
            System.out.println(this.value);
        }
    }

    static void main() {
        CustomAtomicInteger atomic = new CustomAtomicInteger();

        Instant start = Instant.now();
        try (ExecutorService service = Executors.newFixedThreadPool(4)) {
            for (int j = 0; j < 4; ++j) {
                service.submit(() -> {
                    for (int i = 0; i < 10_000; ++i) {
                        atomic.increment();
                    }
                });
            }
        } catch (Exception ex) {
            System.out.println("Caught: " + ex.getClass() + " " + ex.getMessage());
        }

        System.out.println("Took " + Duration.between(start, Instant.now()).toMillis() + " ms");
        atomic.printValue();

        start = Instant.now();
        final int[] normalCounter = {0};

        try (ExecutorService service = Executors.newFixedThreadPool(4)) {
            for (int j = 0; j < 4; ++j) {
                service.submit(() -> {
                    for (int i = 0; i < 10_000; ++i) {
                        normalCounter[0]++;
                    }
                });
            }
        }

        System.out.println(normalCounter[0]);
        System.out.println("Took " + Duration.between(start, Instant.now()).toMillis() + " ms");
    }
}
