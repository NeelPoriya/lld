package in.neelporiya.fooddelivery;

/**
 * // DESIGN PATTERN: Observer — order lifecycle events pushed to the customer app, restaurant
 * dashboard and courier app without the service knowing about any of them.
 */
public interface OrderListener {

    default void onPlaced(Order order) {
    }

    default void onStatusChanged(Order order, OrderStatus from, OrderStatus to) {
    }

    default void onAgentAssigned(Order order, DeliveryAgent agent) {
    }
}
