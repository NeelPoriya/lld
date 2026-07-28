package in.neelporiya.pubsub.delivery;

import in.neelporiya.pubsub.Message;
import in.neelporiya.pubsub.Subscriber;

/**
 * Delivers inline on the publishing thread.
 *
 * <p>// INTERVIEW INSIGHT: simple and perfectly ordered, but a slow or throwing subscriber blocks the
 * publisher and every other subscriber. That is exactly why production systems go async. We keep it
 * because it makes tests deterministic (the message is delivered before {@code publish} returns).
 */
public class SynchronousDeliveryStrategy implements DeliveryStrategy {

    @Override
    public DeliveryChannel createChannel(String subscriberId, Subscriber subscriber) {
        return new DeliveryChannel() {
            @Override
            public void deliver(Message message) {
                subscriber.onMessage(message);
            }

            @Override
            public void close() {
                // nothing to release
            }
        };
    }
}
