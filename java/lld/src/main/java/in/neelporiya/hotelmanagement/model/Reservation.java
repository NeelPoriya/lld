package in.neelporiya.hotelmanagement.model;

import in.neelporiya.hotelmanagement.state.ConfirmedState;
import in.neelporiya.hotelmanagement.state.ReservationState;

import java.time.Instant;
import java.util.Objects;

public final class Reservation {

    private final String id;
    private final String roomId;
    private final String guestId;
    private final StayRange range;
    private final long quotedPriceCents;
    private final Instant createdAt;
    private ReservationState state;
    private Instant checkedInAt;
    private Instant checkedOutAt;
    private Instant cancelledAt;

    private Reservation(Builder builder) {
        this.id = Room.requireText(builder.id, "id");
        this.roomId = Room.requireText(builder.roomId, "roomId");
        this.guestId = Room.requireText(builder.guestId, "guestId");
        this.range = Objects.requireNonNull(builder.range, "range");
        if (builder.quotedPriceCents < 0) {
            throw new IllegalArgumentException("quotedPriceCents must be non-negative");
        }
        this.quotedPriceCents = builder.quotedPriceCents;
        this.createdAt = Objects.requireNonNull(builder.createdAt, "createdAt");
        this.state = new ConfirmedState();
    }

    public synchronized void checkIn(Instant now) {
        state.checkIn(this, now);
    }

    public synchronized void checkOut(Instant now) {
        state.checkOut(this, now);
    }

    public synchronized void cancel(Instant now) {
        state.cancel(this, now);
    }

    public String getId() {
        return id;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getGuestId() {
        return guestId;
    }

    public StayRange getRange() {
        return range;
    }

    public long getQuotedPriceCents() {
        return quotedPriceCents;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public synchronized ReservationStatus getStatus() {
        return state.status();
    }

    public synchronized Instant getCheckedInAt() {
        return checkedInAt;
    }

    public synchronized Instant getCheckedOutAt() {
        return checkedOutAt;
    }

    public synchronized Instant getCancelledAt() {
        return cancelledAt;
    }

    public synchronized void transitionTo(ReservationState nextState) {
        this.state = Objects.requireNonNull(nextState, "nextState");
    }

    public synchronized void markCheckedIn(Instant checkedInAt) {
        this.checkedInAt = Objects.requireNonNull(checkedInAt, "checkedInAt");
    }

    public synchronized void markCheckedOut(Instant checkedOutAt) {
        this.checkedOutAt = Objects.requireNonNull(checkedOutAt, "checkedOutAt");
    }

    public synchronized void markCancelled(Instant cancelledAt) {
        this.cancelledAt = Objects.requireNonNull(cancelledAt, "cancelledAt");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String roomId;
        private String guestId;
        private StayRange range;
        private long quotedPriceCents;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder roomId(String roomId) {
            this.roomId = roomId;
            return this;
        }

        public Builder guestId(String guestId) {
            this.guestId = guestId;
            return this;
        }

        public Builder range(StayRange range) {
            this.range = range;
            return this;
        }

        public Builder quotedPriceCents(long quotedPriceCents) {
            this.quotedPriceCents = quotedPriceCents;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Reservation build() {
            // DESIGN PATTERN: Builder keeps a many-field reservation readable while preserving invariants.
            return new Reservation(this);
        }
    }
}
