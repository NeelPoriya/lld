package in.neelporiya.airline.model;

import in.neelporiya.airline.state.BookingState;
import in.neelporiya.airline.state.ConfirmedState;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public final class Booking {

    private final String id;
    private final Passenger passenger;
    private final String flightInstanceId;
    private final String seatNumber;
    private final SeatClass seatClass;
    private final BigDecimal fare;
    private final Instant createdAt;
    private BookingState state;
    private Instant checkedInAt;
    private Instant cancelledAt;

    private Booking(Builder builder) {
        this.id = Passenger.requireText(builder.id, "id");
        this.passenger = Objects.requireNonNull(builder.passenger, "passenger");
        this.flightInstanceId = Passenger.requireText(builder.flightInstanceId, "flightInstanceId");
        this.seatNumber = Passenger.requireText(builder.seatNumber, "seatNumber");
        this.seatClass = Objects.requireNonNull(builder.seatClass, "seatClass");
        this.fare = Objects.requireNonNull(builder.fare, "fare");
        this.createdAt = Objects.requireNonNull(builder.createdAt, "createdAt");
        this.state = new ConfirmedState();
    }

    public synchronized void checkIn(Instant now) {
        state.checkIn(this, now);
    }

    public synchronized void cancel(Instant now) {
        state.cancel(this, now);
    }

    public synchronized BookingStatus getStatus() {
        return state.status();
    }

    public synchronized void transitionTo(BookingState nextState) {
        this.state = Objects.requireNonNull(nextState, "nextState");
    }

    public synchronized void markCheckedIn(Instant checkedInAt) {
        this.checkedInAt = Objects.requireNonNull(checkedInAt, "checkedInAt");
    }

    public synchronized void markCancelled(Instant cancelledAt) {
        this.cancelledAt = Objects.requireNonNull(cancelledAt, "cancelledAt");
    }

    public String getId() {
        return id;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public String getFlightInstanceId() {
        return flightInstanceId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public SeatClass getSeatClass() {
        return seatClass;
    }

    public BigDecimal getFare() {
        return fare;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public synchronized Instant getCheckedInAt() {
        return checkedInAt;
    }

    public synchronized Instant getCancelledAt() {
        return cancelledAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private Passenger passenger;
        private String flightInstanceId;
        private String seatNumber;
        private SeatClass seatClass;
        private BigDecimal fare;
        private Instant createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder passenger(Passenger passenger) {
            this.passenger = passenger;
            return this;
        }

        public Builder flightInstanceId(String flightInstanceId) {
            this.flightInstanceId = flightInstanceId;
            return this;
        }

        public Builder seatNumber(String seatNumber) {
            this.seatNumber = seatNumber;
            return this;
        }

        public Builder seatClass(SeatClass seatClass) {
            this.seatClass = seatClass;
            return this;
        }

        public Builder fare(BigDecimal fare) {
            this.fare = fare;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Booking build() {
            // DESIGN PATTERN: Builder avoids telescoping constructors for ticket metadata.
            return new Booking(this);
        }
    }
}
