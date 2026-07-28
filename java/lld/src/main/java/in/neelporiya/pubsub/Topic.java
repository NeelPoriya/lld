package in.neelporiya.pubsub;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A named channel with a set of subscriptions and its own message sequence.
 *
 * <p>// CONCURRENCY: the subscription list is a {@link CopyOnWriteArrayList} so {@code publish} can
 * iterate a stable snapshot lock-free while other threads subscribe/unsubscribe.
 */
public class Topic {

    private final String name;
    private final List<Subscription> subscriptions = new CopyOnWriteArrayList<>();
    private final AtomicLong sequence = new AtomicLong();

    public Topic(String name) {
        this.name = name;
    }

    void addSubscription(Subscription subscription) {
        subscriptions.add(subscription);
    }

    void removeSubscription(Subscription subscription) {
        subscriptions.remove(subscription);
    }

    List<Subscription> subscriptions() {
        return subscriptions; // COW list: safe to iterate without copying
    }

    long nextOffset() {
        return sequence.getAndIncrement();
    }

    public String name() {
        return name;
    }

    public int subscriberCount() {
        return subscriptions.size();
    }
}
