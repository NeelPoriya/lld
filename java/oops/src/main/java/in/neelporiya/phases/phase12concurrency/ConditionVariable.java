package in.neelporiya.phases.phase12concurrency;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionVariable {
    public static class Demo1 {
        private final Object lock = new Object();
        private boolean dataReady = false;
        private String data = "Old data";

        public void consumer() throws InterruptedException {
            synchronized (lock) {
                while (!dataReady) {
                    lock.wait();
                }
                System.out.println("Data is loaded: " + data);
                dataReady = false;
            }
        }

        public void producer() throws InterruptedException {
            synchronized(lock) {
                Thread.sleep(1000);
                data = "This is the data";
                dataReady = true;
                lock.notify();
            }
        }
    }

    public static class Demo2 {
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        private boolean dataReady = false;
        private String data;

        public void consumer() {
            lock.lock();
            try {
                while (!dataReady) {
                    condition.await();
                }
                System.out.println("Data is ready: " + data);
                dataReady = false;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }

        public void producer() {
            try {
                lock.lock();
                Thread.sleep(1000);
                data = "This is the new data";
                dataReady = true;
                condition.signal();
            } catch (InterruptedException ex) {
                System.out.println("Caught: " + ex.getClass() + " " + ex.getMessage());
            } finally {
                lock.unlock();
            }
        }
    }

    static void testDemo1() throws InterruptedException {
        Demo1 demo1 = new Demo1();

        Thread[] threads = new Thread[2];
        threads[0] = new Thread(() -> {
            try {
                demo1.consumer();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        threads[1] = new Thread(() -> {
            try {
                demo1.producer();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });

        for (Thread t : threads) {
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        System.out.println("Completed Demo 1...");
    }

    static void testDemo2() throws InterruptedException {
        Demo2 demo = new Demo2();
        Thread[] threads = new Thread[2];
        threads[0] = new Thread(demo::consumer);
        threads[1] = new Thread(demo::producer);

        for (var t : threads) t.start();
        for (var t : threads) t.join();

        System.out.println("Completed Demo 2...");
    }

    static void main() throws InterruptedException {
        testDemo1();
        testDemo2();
    }
}
