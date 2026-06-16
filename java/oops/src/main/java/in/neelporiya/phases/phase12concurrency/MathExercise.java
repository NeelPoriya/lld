package in.neelporiya.phases.phase12concurrency;

public class MathExercise {
    static final int THREADS = 8;
    static final int M = 1000;

    static class Statistics {
        private long sum = 0;
        private long count = 0;
        private long max = 0;
        private final Object lock = new Object();
        synchronized void add(long x) {
            // TODO: under one lock, update sum, count, and max together
            sum += x;
            count++;
            max = Math.max(max, x);
        }
        synchronized String report() {
            // TODO: under the lock, return "sum count max"
            return sum + " " + count + " " + max;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Statistics stats = new Statistics();
        Thread[] threads = new Thread[THREADS];
        for (int t = 0; t < THREADS; t++) {
            threads[t] = new Thread(() -> {
                for (int i = 1; i <= M; i++) {
                    stats.add(i);
                }
            });
        }
        for (Thread th : threads) {
            th.start();
        }
        for (Thread th : threads) {
            th.join();
        }
        System.out.println(stats.report());
    }
}