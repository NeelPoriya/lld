package in.neelporiya.phases.phase12concurrency;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBufferWithWaitAndSignal {
    static final int N = 1000;
    static final int CAP = 5;

    static class BoundedBuffer {
        private final Queue<Integer> queue = new LinkedList<>();
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notFull = lock.newCondition();
        private final Condition notEmpty = lock.newCondition();

        public void put(int item) throws InterruptedException {
            lock.lock();
            try {
                // TODO: while the buffer is full, await on notFull
                while (queue.size() == CAP) {
                    notFull.await();
                }
                queue.add(item);
                // TODO: signal notEmpty
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        public int take() throws InterruptedException {
            lock.lock();
            try {
                // TODO: while the buffer is empty, await on notEmpty
                while (queue.isEmpty()) {
                    notEmpty.await();
                }
                int item = queue.poll();
                // TODO: signal notFull
                notFull.signal();
                return item;
            } finally {
                lock.unlock();
            }
        }
    }

    static void main(String[] args) throws InterruptedException {
        BoundedBuffer buffer = new BoundedBuffer();
        long[] sum = new long[1];
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= N; i++) {
                    buffer.put(i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < N; i++) {
                    sum[0] += buffer.take();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
        System.out.println(sum[0]);
    }
}
