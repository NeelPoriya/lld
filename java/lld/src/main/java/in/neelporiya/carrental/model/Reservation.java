package in.neelporiya.carrental.model;

import in.neelporiya.carrental.state.ReservedState;
import in.neelporiya.carrental.state.ReservationState;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public final class Reservation {

    private final String id;
    private final String vehicleId;
    private final String customerId;
    private final DateRange range;
    private final Set<AddOn> addOns;
    private final BigDecimal quotedPrice;
    private final Instant createdAt;
    private ReservationState state;
    private Instant pickedUpAt;
    private Instant returnedAt;
    private Instant cancelledAt;

    private Reservation(Builder builder) {
        this.id = Vehicle.requireText(builder.id, "id");
        this.vehicleId = Vehicle.requireText(builder.vehicleId, "vehicleId");
        this.customerId = Vehicle.requireText(builder.customerId, "customerId");
        this.range = Objects.requireNonNull(builder.range, "range");
        this.addOns = Set.copyOf(builder.addOns);
        this.quotedPrice = Objects.requireNonNull(builder.quotedPrice, "quotedPrice");
        this.createdAt = Objects.requireNonNull(builder.createdAt, "createdAt");
        this.state = new ReservedState();
    }

    public synchronized void pickUp(Instant now) {
        state.pickUp(this, now);
    }

    public synchronized void returnVehicle(Instant now) {
        state.returnVehicle(this, now);
    }

    public synchronized void cancel(Instant now) {
        state.cancel(this, now);
    }

    public String getId() {
        return id;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public DateRange getRange() {
        return range;
    }

    public Set<AddOn> getAddOns() {
        return addOns;
    }

    public BigDecimal getQuotedPrice() {
        return quotedPrice;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public synchronized ReservationStatus getStatus() {
        return state.status();
    }

    public synchronized Instant getPickedUpAt() {
        return pickedUpAt;
    }

    public synchronized Instant getReturnedAt() {
        return returnedAt;
    }

    public synchronized Instant getCancelledAt() {
        return cancelledAt;
    }

    public synchronized void transitionTo(ReservationState nextState) {
        this.state = Objects.requireNonNull(nextState, "nextState");
    }

    public synchronized void markPickedUp(Instant pickedUpAt) {
        this.pickedUpAt = Objects.requireNonNull(pickedUpAt, "pickedUpAt");
    }

    public synchronized void markReturned(Instant returnedAt) {
        this.returnedAt = Objects.requireNonNull(returnedAt, "returnedAt");
    }

    public synchronized void markCancelled(Instant cancelledAt) {
        this.cancelledAt = Objects.requireNonNull(cancelledAt, "cancelledAt");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String vehicleId;
        private String customerId;
        private DateRange range;
        private Set<AddOn> addOns = Set.of();
        private BigDecimal quotedPrice;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder vehicleId(String vehicleId) {
            this.vehicleId = vehicleId;
            return this;
        }

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder range(DateRange range) {
            this.range = range;
            return this;
        }

        public Builder addOns(Set<AddOn> addOns) {
            this.addOns = addOns == null ? Set.of() : Set.copyOf(addOns);
            return this;
        }

        public Builder quotedPrice(BigDecimal quotedPrice) {
            this.quotedPrice = quotedPrice;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Reservation build() {
            // DESIGN PATTERN: Builder keeps construction readable while preserving Reservation invariants.
            return new Reservation(this);
        }
    }
}
