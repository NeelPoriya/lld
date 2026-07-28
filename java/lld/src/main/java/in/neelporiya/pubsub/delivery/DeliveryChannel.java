package in.neelporiya.pubsub.delivery;

import in.neelporiya.pubsub.Message;

/**
 * The per-subscription delivery mechanism. It has state (an async channel owns a queue + thread),
 * so it is created once per subscription and closed on unsubscribe/shutdown.
 */
public interface DeliveryChannel {

    void deliver(Message message);

    /** Drain any buffered messages and release resources (stop the worker thread). */
    void close();
}
