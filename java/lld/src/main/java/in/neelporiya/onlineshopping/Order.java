package in.neelporiya.onlineshopping;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Immutable order facts plus a synchronized status state machine.
 *
 * <p>// DESIGN PATTERN: Builder — checkout builds an order from a cart snapshot, generated id,
 * deterministic clock time, and computed total.
 */
public class Order {

    private final String id;
    private final String customerId;
    private final List<OrderItem> items;
    private final long totalCents;
    private final Instant createdAt;

    private volatile OrderStatus status;
    private volatile PaymentReceipt paymentReceipt;

    private Order(Builder builder) {
        this.id = builder.id;
        this.customerId = builder.customerId;
        this.items = List.copyOf(builder.items);
        this.totalCents = builder.totalCents;
        this.createdAt = builder.createdAt;
        this.status = OrderStatus.PLACED;
    }

    public synchronized void markPaid(PaymentReceipt receipt) {
        transitionTo(OrderStatus.PAID);
        this.paymentReceipt = Objects.requireNonNull(receipt, "receipt");
    }

    public synchronized void transitionTo(OrderStatus next) {
        Objects.requireNonNull(next, "next");
        if (!status.canTransitionTo(next)) {
            throw new InvalidOrderTransitionException("Cannot transition order " + id + " from "
                    + status + " to " + next);
        }
        status = next;
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public long getTotalCents() {
        return totalCents;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public PaymentReceipt getPaymentReceipt() {
        return paymentReceipt;
    }

    public static Builder builder(String id, String customerId) {
        return new Builder(id, customerId);
    }

    public static final class Builder {
        private final String id;
        private final String customerId;
        private final List<OrderItem> items = new ArrayList<>();
        private long totalCents;
        private Instant createdAt = Instant.EPOCH;

        private Builder(String id, String customerId) {
            this.id = Objects.requireNonNull(id, "id");
            this.customerId = Objects.requireNonNull(customerId, "customerId");
        }

        public Builder addItem(OrderItem item) {
            this.items.add(Objects.requireNonNull(item, "item"));
            return this;
        }

        public Builder totalCents(long totalCents) {
            if (totalCents < 0) {
                throw new IllegalArgumentException("totalCents must be >= 0");
            }
            this.totalCents = totalCents;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
            return this;
        }

        public Order build() {
            if (items.isEmpty()) {
                throw new IllegalStateException("Order needs at least one item");
            }
            return new Order(this);
        }
    }
}
