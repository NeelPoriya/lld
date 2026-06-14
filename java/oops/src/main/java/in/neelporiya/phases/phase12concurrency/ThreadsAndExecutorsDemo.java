package in.neelporiya.phases.phase12concurrency;

import in.neelporiya.runner.Concept;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadsAndExecutorsDemo implements Concept {
    @Override
    public String title() {
        return "Concurrency in Java";
    }

    @Override
    public String description() {
        return "Introduction to threads and thread pool which runs methods " +
                "concurrently on parallel threads using thread.start();";
    }

    public void SimpleThread() throws InterruptedException {
        Runnable task = () -> System.out.println("on " + Thread.currentThread().getName());
        Thread thread = new Thread(task);
        thread.start();
        thread.join();
    }

    public void ThreadPool() throws InterruptedException, ExecutionException {
        ExecutorService pool = Executors.newFixedThreadPool(3);
        Future<Integer> future = pool.submit(() -> 2 + 2);
        Integer result = future.get();
        System.out.println("Result of task submitted to pool: " + result);
        pool.shutdown();
    }

    @Override
    public void run() {
        try {
            SimpleThread();
        } catch (InterruptedException e) {
            System.out.println("Caught: " + e.getClass() + " " + e.getMessage());
        }

        try {
            ThreadPool();
        } catch (InterruptedException | ExecutionException ex) {
            System.out.println("Caught: " + ex.getClass() + " " + ex.getMessage());
        }

        try {
            int[] counter = {0};
            Runnable inc = () -> { for (int i = 0; i < 1e5; ++i) counter[0]++; };
            Thread a = new Thread(inc), b = new Thread(inc);
            a.start(); b.start(); a.join(); b.join();
            System.out.println(counter[0]);
        } catch (InterruptedException ex) {
            System.out.println("Caught: " + ex.getClass() + " " + ex.getMessage());
        }

        try {
            AtomicInteger counter = new AtomicInteger();
            Runnable inc = () -> { for (int i = 0; i < 1e5; ++i) counter.incrementAndGet(); };
            Thread a = new Thread(inc), b = new Thread(inc);
            a.start(); b.start(); a.join(); b.join();
            System.out.println(counter.get());
        } catch (InterruptedException ex) {
            System.out.println("Caught: " + ex.getClass() + " " + ex.getMessage());
        }

        try {
            final int[] counter = {0};
            Object lock = new Object();
            Runnable inc = () -> { for (int i = 0; i < 1e5; ++i) synchronized (lock) { counter[0]++; }};
            Thread a = new Thread(inc), b = new Thread(inc);
            a.start(); b.start(); a.join(); b.join();
            System.out.println(counter[0]);
        } catch (InterruptedException ex) {
            System.out.println("Caught: " + ex.getClass() + " " + ex.getMessage());
        }

        /*
        In analogy with C#
        Thread == Thread
        Runnable == Action
        Callable<V> == Func<V>
        ExecutorService == ThreadPool/TaskScheduler
        Future<V> == Task<V>
        synchronized == lock
        AtomicInteger == Interlocked/Interlocked.Increment
         */
    }
}
