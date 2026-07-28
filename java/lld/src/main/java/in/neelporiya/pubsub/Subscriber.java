package in.neelporiya.pubsub;

/**
 * // DESIGN PATTERN: Observer. A consumer of messages on a topic. Functional so tests can pass a
 * lambda, and real consumers implement it.
 */
@FunctionalInterface
public interface Subscriber {
    void onMessage(Message message);
}
