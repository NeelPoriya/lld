package in.neelporiya.pubsub;

import in.neelporiya.pubsub.delivery.DeliveryChannel;
import in.neelporiya.pubsub.delivery.DeliveryStrategy;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * // DESIGN PATTERN: Facade — the client-facing message broker. Owns the topic registry, applies the
 * injected {@link DeliveryStrategy}, and stamps messages with the injected {@link Clock}.
 */
public class Broker {

    private final Map<String, Topic> topics = new ConcurrentHashMap<>();
    private final List<DeliveryChannel> channels = new CopyOnWriteArrayList<>();
    private final DeliveryStrategy deliveryStrategy;
    private final Clock clock;
    private final Supplier<String> idGenerator;

    public Broker(DeliveryStrategy deliveryStrategy, Clock clock, Supplier<String> idGenerator) {
        this.deliveryStrategy = Objects.requireNonNull(deliveryStrategy, "deliveryStrategy");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }

    public static Broker createDefault(DeliveryStrategy strategy) {
        return new Broker(strategy, Clock.systemUTC(), () -> UUID.randomUUID().toString());
    }

    public Subscription subscribe(String topicName, Subscriber subscriber) {
        Topic topic = topics.computeIfAbsent(topicName, Topic::new);
        DeliveryChannel channel = deliveryStrategy.createChannel(idGenerator.get(), subscriber);
        Subscription subscription = new Subscription(idGenerator.get(), topicName, channel);
        topic.addSubscription(subscription);
        channels.add(channel);
        return subscription;
    }

    public void unsubscribe(Subscription subscription) {
        Topic topic = topics.get(subscription.topic());
        if (topic != null) {
            topic.removeSubscription(subscription);
        }
        subscription.channel().close();
        channels.remove(subscription.channel());
    }

    /**
     * Publish a payload to a topic.
     *
     * @return the message's per-topic offset.
     */
    public long publish(String topicName, String payload) {
        // Create the topic even if there are no subscribers yet, so its offset sequence is stable.
        Topic topic = topics.computeIfAbsent(topicName, Topic::new);
        long offset = topic.nextOffset();
        Message message = new Message(idGenerator.get(), topicName, payload, clock.instant(), offset);
        // CONCURRENCY: iterate a copy-on-write snapshot; concurrent (un)subscribes are safe.
        for (Subscription subscription : topic.subscriptions()) {
            subscription.channel().deliver(message);
        }
        return offset;
    }

    public int subscriberCount(String topicName) {
        Topic topic = topics.get(topicName);
        return topic == null ? 0 : topic.subscriberCount();
    }

    /** Drain and stop every delivery channel — call on application shutdown. */
    public void close() {
        for (DeliveryChannel channel : channels) {
            channel.close();
        }
        channels.clear();
    }
}
