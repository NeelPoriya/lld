package in.neelporiya.restaurant.event;

import in.neelporiya.restaurant.Order;
import in.neelporiya.restaurant.OrderStatus;

/** // DESIGN PATTERN: Observer — kitchen displays, SMS, analytics react without coupling to the service. */
public interface OrderEventListener {
    void onOrderPlaced(Order order);

    void onOrderStatusChanged(Order order, OrderStatus previous, OrderStatus next);
}
