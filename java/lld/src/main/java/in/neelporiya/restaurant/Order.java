package in.neelporiya.restaurant;

import in.neelporiya.restaurant.exception.InvalidOrderTransitionException;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Order aggregate. It owns and protects lifecycle state.
 *
 * <p>// CONCURRENCY: {@link #changeStatus(OrderStatus, Clock)} is synchronized, so the full transition
 * is atomic per order: validate legal edge → change status → stamp audit time. If many threads race
 * PLACED→PREPARING, one wins and later racers see PREPARING, making the same transition illegal.
 */
public class Order {
    private final String id;
    private final Table table;
    private final List<OrderLineItem> lineItems;
    private final Instant placedAt;

    private volatile OrderStatus status = OrderStatus.PLACED;
    private volatile Instant updatedAt;
    private volatile Instant readyAt;
    private volatile Instant servedAt;
    private volatile Instant paidAt;

    private Order(Builder builder) {
        this.id = builder.id;
        this.table = builder.table;
        this.lineItems = List.copyOf(builder.lineItems);
        this.placedAt = builder.placedAt;
        this.updatedAt = builder.placedAt;
    }

    public synchronized OrderStatus changeStatus(OrderStatus next, Clock clock) {
        Objects.requireNonNull(next, "next");
        if (!status.canTransitionTo(next)) {
            throw new InvalidOrderTransitionException("Cannot move order " + id + " from " + status + " to " + next);
        }
        OrderStatus previous = status;
        Instant now = clock.instant();
        status = next;
        updatedAt = now;
        if (next == OrderStatus.READY) {
            readyAt = now;
        } else if (next == OrderStatus.SERVED) {
            servedAt = now;
        } else if (next == OrderStatus.PAID) {
            paidAt = now;
        }
        return previous;
    }

    public long subtotalCents() {
        return lineItems.stream().mapToLong(OrderLineItem::lineTotalCents).sum();
    }

    public String getId() {
        return id;
    }

    public Table getTable() {
        return table;
    }

    public List<OrderLineItem> getLineItems() {
        return lineItems;
    }

    public Instant getPlacedAt() {
        return placedAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getReadyAt() {
        return readyAt;
    }

    public Instant getServedAt() {
        return servedAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** // DESIGN PATTERN: Builder — orders have a variable number of line items. */
    public static final class Builder {
        private String id;
        private Table table;
        private final List<OrderLineItem> lineItems = new ArrayList<>();
        private Instant placedAt = Instant.EPOCH;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder table(Table table) {
            this.table = table;
            return this;
        }

        public Builder addItem(MenuItem item, int quantity) {
            this.lineItems.add(new OrderLineItem(item, quantity));
            return this;
        }

        public Builder placedAt(Instant placedAt) {
            this.placedAt = placedAt;
            return this;
        }

        public Order build() {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(table, "table");
            Objects.requireNonNull(placedAt, "placedAt");
            if (lineItems.isEmpty()) {
                throw new IllegalStateException("An order needs at least one line item");
            }
            return new Order(this);
        }
    }
}
