package in.neelporiya.pubsub;

import in.neelporiya.pubsub.delivery.SynchronousDeliveryStrategy;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Synchronous delivery makes every assertion immediate and deterministic — no waiting.
 */
class PubSubSyncTest {

    private final MutableClock clock = MutableClock.atEpoch();

    private Broker broker() {
        AtomicInteger seq = new AtomicInteger();
        return new Broker(new SynchronousDeliveryStrategy(), clock, () -> "id-" + seq.incrementAndGet());
    }

    @Test
    void subscriberReceivesPublishedMessage() {
        Broker broker = broker();
        List<String> received = new ArrayList<>();
        broker.subscribe("orders", msg -> received.add(msg.payload()));

        broker.publish("orders", "order-1");

        assertEquals(List.of("order-1"), received);
    }

    @Test
    void onlyMessagesAfterSubscriptionAreReceived() {
        Broker broker = broker();
        broker.publish("orders", "before"); // no subscribers yet
        List<String> received = new ArrayList<>();
        broker.subscribe("orders", msg -> received.add(msg.payload()));
        broker.publish("orders", "after");

        assertEquals(List.of("after"), received);
    }

    @Test
    void fanOutToAllSubscribers() {
        Broker broker = broker();
        List<String> a = new ArrayList<>();
        List<String> b = new ArrayList<>();
        broker.subscribe("news", msg -> a.add(msg.payload()));
        broker.subscribe("news", msg -> b.add(msg.payload()));

        broker.publish("news", "headline");

        assertEquals(List.of("headline"), a);
        assertEquals(List.of("headline"), b);
        assertEquals(2, broker.subscriberCount("news"));
    }

    @Test
    void unsubscribeStopsDelivery() {
        Broker broker = broker();
        List<String> received = new ArrayList<>();
        Subscription sub = broker.subscribe("t", msg -> received.add(msg.payload()));

        broker.publish("t", "first");
        broker.unsubscribe(sub);
        broker.publish("t", "second");

        assertEquals(List.of("first"), received);
        assertEquals(0, broker.subscriberCount("t"));
    }

    @Test
    void offsetsIncrementPerTopic() {
        Broker broker = broker();
        assertEquals(0, broker.publish("t", "a"));
        assertEquals(1, broker.publish("t", "b"));
        assertEquals(0, broker.publish("other", "a")); // independent sequence per topic
    }

    @Test
    void messageTimestampComesFromInjectedClock() {
        Broker broker = broker();
        List<Instant> times = new ArrayList<>();
        broker.subscribe("t", msg -> times.add(msg.publishedAt()));

        broker.publish("t", "a");
        clock.advance(Duration.ofSeconds(5));
        broker.publish("t", "b");

        assertEquals(List.of(Instant.EPOCH, Instant.EPOCH.plusSeconds(5)), times);
    }

    @Test
    void publishingToTopicWithNoSubscribersIsHarmless() {
        Broker broker = broker();
        assertTrue(broker.publish("empty", "x") >= 0);
        assertEquals(0, broker.subscriberCount("empty"));
    }
}
