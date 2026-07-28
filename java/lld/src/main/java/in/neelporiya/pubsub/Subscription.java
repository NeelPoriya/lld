package in.neelporiya.pubsub;

import in.neelporiya.pubsub.delivery.DeliveryChannel;

/**
 * A handle returned by {@code subscribe}. Holding it lets a client {@code unsubscribe} without the
 * broker having to match on subscriber identity (which is fragile for lambdas).
 */
public record Subscription(String id, String topic, DeliveryChannel channel) {
}
