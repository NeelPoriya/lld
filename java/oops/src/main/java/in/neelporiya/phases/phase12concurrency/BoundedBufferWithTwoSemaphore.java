package in.neelporiya.phases.phase12concurrency;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

public class BoundedBufferWithTwoSemaphore {
    public static class Main {
        static final int PRODUCERS = 3;
        static final int CONSUMERS = 2;
        static final int K = 1000;
        static final int CAP = 8;
        static final int POISON = -1;

        static final Queue<Integer> queue = new LinkedList<>();
        static final Semaphore empty = new Semaphore(CAP);
        static final Semaphore full = new Semaphore(0);
        static final Object mutex = new Object();

        static void put(int item) throws InterruptedException {
            empty.acquire();
            synchronized(mutex) {
                queue.offer(item);
            }
            full.release();
        }
        static int take() throws InterruptedException {
            full.acquire();
            int res = 0;
            synchronized(mutex) {
                if (!queue.isEmpty()) res = queue.poll();
            }
            empty.release();
            return res;
        }

        public static void main(String[] args) throws InterruptedException {
            AtomicLong sum = new AtomicLong(0);
            Thread[] producers = new Thread[PRODUCERS];
            for (int p = 0; p < PRODUCERS; p++) {
                producers[p] = new Thread(() -> {
                    try {
                        for (int i = 1; i <= K; i++) {
                            put(i);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            Thread[] consumers = new Thread[CONSUMERS];
            for (int c = 0; c < CONSUMERS; c++) {
                consumers[c] = new Thread(() -> {
                    try {
                        while (true) {
                            int item = take();
                            if (item == POISON) {
                                break;
                            }
                            sum.addAndGet(item);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            for (Thread t : producers) {
                t.start();
            }
            for (Thread t : consumers) {
                t.start();
            }
            for (Thread t : producers) {
                t.join();
            }
            for (int c = 0; c < CONSUMERS; c++) {
                put(POISON);
            }
            for (Thread t : consumers) {
                t.join();
            }
            System.out.println(sum.get());
        }
    }
}
