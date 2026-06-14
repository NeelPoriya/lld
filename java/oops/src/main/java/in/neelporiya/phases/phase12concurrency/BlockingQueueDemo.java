package in.neelporiya.phases.phase12concurrency;

import in.neelporiya.runner.Concept;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueDemo implements Concept {
    @Override
    public String title() {
        return "Blocking queue in Java";
    }

    @Override
    public String description() {
        return "blocking queue comes in to picture when there are producers which are constantly " +
                "pushing data while consumers try to consume data. Now the rate at which these two" +
                " are working may vary, so we might need to put checks on both consumer and provider side" +
                " that consumer don't keep on consuming even when queue is empty and producer don't push " +
                "if the queue is full.";
    }

    @Override
    public void run() {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(3);

        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 5; ++i) {
                    System.out.println("[Producer] Trying to put " + i);
                    queue.put(i);
                    System.out.println("[Producer] Successfully put: " + i);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 5; ++i) {
                    System.out.println("[Consumer] Trying to take...");
                    Integer item = queue.take();
                    System.out.println("[Consumer] Successfully took: " + item);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();

        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException ex) {
            System.out.println("Caught: " + ex.getClass() + " " + ex.getMessage());
        }
    }
}
