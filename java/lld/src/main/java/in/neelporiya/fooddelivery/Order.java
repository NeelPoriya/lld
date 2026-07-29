package in.neelporiya.fooddelivery;

import in.neelporiya.fooddelivery.exception.InvalidOrderTransitionException;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A customer's order and its lifecycle. Status transitions are validated against {@link OrderStatus}'s
 * table; the assigned {@link DeliveryAgent} appears only once the order is dispatched.
 *
 * <p>// CONCURRENCY: each order carries its own {@link ReentrantLock}. The service holds it across the
 * check-then-transition (and the agent claim) so an order can't be advanced down two paths at once.
 */
public class Order {

    private final String id;
    private final Customer customer;
    private final Restaurant restaurant;
    private final List<OrderLine> lines;
    private final Bill bill;
    private final Instant placedAt;
    private final ReentrantLock lock = new ReentrantLock();

    private volatile OrderStatus status = OrderStatus.PLACED;
    private volatile DeliveryAgent agent;

    public Order(String id, Customer customer, Restaurant restaurant, List<OrderLine> lines,
                 Bill bill, Instant placedAt) {
        this.id = id;
        this.customer = customer;
        this.restaurant = restaurant;
        this.lines = List.copyOf(lines);
        this.bill = bill;
        this.placedAt = placedAt;
    }

    ReentrantLock getLock() {
        return lock;
    }

    /** Validate and apply a transition. Caller must hold {@link #getLock()}. */
    void transitionTo(OrderStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new InvalidOrderTransitionException(status + " -> " + next + " is not a legal transition");
        }
        this.status = next;
    }

    void assignAgent(DeliveryAgent agent) {
        this.agent = agent;
    }

    public String getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public List<OrderLine> getLines() {
        return lines;
    }

    public Bill getBill() {
        return bill;
    }

    public Instant getPlacedAt() {
        return placedAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public DeliveryAgent getAgent() {
        return agent;
    }
}
