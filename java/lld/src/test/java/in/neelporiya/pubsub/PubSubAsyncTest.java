package in.neelporiya.pubsub;

import in.neelporiya.pubsub.delivery.AsynchronousDeliveryStrategy;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // CONCURRENCY: exercises the async, per-subscriber-worker delivery path. All timing is made
 * deterministic with {@link CountDownLatch}es (await N messages) + {@code broker.close()} to force a
 * full drain — never {@code Thread.sleep}.
 */
class PubSubAsyncTest {

    private Broker asyncBroker() {
        AtomicInteger seq = new AtomicInteger();
        return new Broker(new AsynchronousDeliveryStrategy(), MutableClock.atEpoch(),
                () -> "id-" + seq.incrementAndGet());
    }

    @Test
    void allMessagesDeliveredInOrderToASubscriber() throws InterruptedException {
        Broker broker = asyncBroker();
        int n = 200;
        List<String> received = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(n);
        broker.subscribe("t", msg -> {
            received.add(msg.payload());
            latch.countDown();
        });

        for (int i = 0; i < n; i++) {
            broker.publish("t", "m" + i);
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "all messages should arrive");
        broker.close();

        List<String> expected = IntStream.range(0, n).mapToObj(i -> "m" + i).toList();
        assertEquals(expected, received, "a single worker preserves per-subscriber FIFO order");
    }

    @Test
    void aSlowSubscriberDoesNotBlockOthers() throws InterruptedException {
        Broker broker = asyncBroker();
        int n = 50;

        CountDownLatch gate = new CountDownLatch(1); // holds the slow subscriber hostage
        CountDownLatch fastDone = new CountDownLatch(n);

        broker.subscribe("t", msg -> {
            try {
                gate.await(); // block this subscriber's worker indefinitely
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        broker.subscribe("t", msg -> fastDone.countDown());

        for (int i = 0; i < n; i++) {
            broker.publish("t", "m" + i);
        }

        // The fast subscriber gets everything even though the slow one is stuck on message 1.
        assertTrue(fastDone.await(5, TimeUnit.SECONDS), "slow consumer must not block the fast one");

        gate.countDown(); // release the slow subscriber so close() can drain + join
        broker.close();
    }

    @Test
    void aThrowingSubscriberIsIsolated() throws InterruptedException {
        Broker broker = asyncBroker();
        int n = 40;
        CountDownLatch healthyDone = new CountDownLatch(n);

        broker.subscribe("t", msg -> {
            throw new RuntimeException("boom"); // every message throws
        });
        broker.subscribe("t", msg -> healthyDone.countDown());

        for (int i = 0; i < n; i++) {
            broker.publish("t", "m" + i);
        }

        assertTrue(healthyDone.await(5, TimeUnit.SECONDS),
                "a throwing subscriber must not stop delivery to others");
        broker.close();
    }

    @Test
    void concurrentPublishersDeliverEveryMessage() throws InterruptedException {
        Broker broker = asyncBroker();
        int publishers = 8;
        int perPublisher = 1000;
        int total = publishers * perPublisher;

        AtomicInteger receivedCount = new AtomicInteger();
        CountDownLatch allReceived = new CountDownLatch(total);
        broker.subscribe("t", msg -> {
            receivedCount.incrementAndGet();
            allReceived.countDown();
        });

        ExecutorService pool = Executors.newFixedThreadPool(publishers);
        CountDownLatch startGun = new CountDownLatch(1);
        for (int p = 0; p < publishers; p++) {
            pool.submit(() -> {
                try {
                    startGun.await();
                    for (int i = 0; i < perPublisher; i++) {
                        broker.publish("t", "x");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        startGun.countDown();
        assertTrue(allReceived.await(10, TimeUnit.SECONDS));
        pool.shutdownNow();
        broker.close();

        assertEquals(total, receivedCount.get(), "every published message must be delivered once");
    }
}
