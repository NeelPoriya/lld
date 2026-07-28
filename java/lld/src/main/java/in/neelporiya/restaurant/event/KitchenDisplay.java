package in.neelporiya.restaurant.event;

import in.neelporiya.restaurant.Order;
import in.neelporiya.restaurant.OrderStatus;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Simple observer useful in tests and interviews. */
public class KitchenDisplay implements OrderEventListener {
    private final List<String> events = new CopyOnWriteArrayList<>();

    @Override
    public void onOrderPlaced(Order order) {
        events.add("placed:" + order.getId());
    }

    @Override
    public void onOrderStatusChanged(Order order, OrderStatus previous, OrderStatus next) {
        events.add(previous + "->" + next + ":" + order.getId());
    }

    public List<String> events() {
        return List.copyOf(events);
    }
}
