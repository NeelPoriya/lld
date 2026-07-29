package in.neelporiya.onlineshopping;

/** // DESIGN PATTERN: Observer — notifications react to order changes without coupling to checkout. */
public interface OrderStatusListener {
    void onStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus);
}
