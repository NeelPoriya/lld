package in.neelporiya.pubsub.delivery;

import in.neelporiya.pubsub.Message;
import in.neelporiya.pubsub.Subscriber;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Gives every subscription its own queue + dedicated worker thread.
 *
 * <p>// CONCURRENCY: {@code publish} merely enqueues (fast, non-blocking); the worker drains and
 * invokes {@code onMessage}. Consequences:
 * <ul>
 *   <li><b>Isolation</b> — a slow consumer only backs up ITS OWN queue; the publisher and other
 *       subscribers are unaffected.</li>
 *   <li><b>Per-subscriber FIFO</b> — one worker draining one queue preserves order.</li>
 *   <li><b>Fault isolation</b> — {@code onMessage} is wrapped in try/catch so a throwing subscriber
 *       doesn't kill its worker (and never affects anyone else).</li>
 * </ul>
 * {@code close()} flips a flag and lets the worker drain the queue before exiting, then joins — no
 * lost messages, no leaked threads.
 */
public class AsynchronousDeliveryStrategy implements DeliveryStrategy {

    @Override
    public DeliveryChannel createChannel(String subscriberId, Subscriber subscriber) {
        return new AsyncDeliveryChannel(subscriberId, subscriber);
    }

    private static final class AsyncDeliveryChannel implements DeliveryChannel {

        private final BlockingQueue<Message> queue = new LinkedBlockingQueue<>();
        private final Subscriber subscriber;
        private final Thread worker;
        private volatile boolean accepting = true;

        AsyncDeliveryChannel(String subscriberId, Subscriber subscriber) {
            this.subscriber = subscriber;
            this.worker = new Thread(this::run, "pubsub-worker-" + subscriberId);
            this.worker.setDaemon(true);
            this.worker.start();
        }

        @Override
        public void deliver(Message message) {
            if (accepting) {
                queue.offer(message);
            }
        }

        private void run() {
            try {
                while (true) {
                    Message message = queue.poll(20, TimeUnit.MILLISECONDS);
                    if (message != null) {
                        try {
                            subscriber.onMessage(message);
                        } catch (RuntimeException isolated) {
                            // Swallow so one bad message/subscriber can't kill the delivery loop.
                        }
                    } else if (!accepting) {
                        // Stop only once we've stopped accepting AND the queue is drained.
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void close() {
            accepting = false; // worker will drain remaining messages, then exit
            try {
                worker.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
