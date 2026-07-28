package in.neelporiya.pubsub.delivery;

import in.neelporiya.pubsub.Subscriber;

/**
 * // DESIGN PATTERN: Strategy. Chooses HOW messages reach a subscriber. Swapping sync for async
 * changes delivery semantics without touching the broker or subscribers.
 */
public interface DeliveryStrategy {

    DeliveryChannel createChannel(String subscriberId, Subscriber subscriber);
}
