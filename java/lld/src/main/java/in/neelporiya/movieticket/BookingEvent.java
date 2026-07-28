package in.neelporiya.movieticket;

import java.time.Instant;
import java.util.Objects;

public class BookingEvent {

    private final BookingEventType type;
    private final Booking booking;
    private final Instant occurredAt;

    public BookingEvent(BookingEventType type, Booking booking, Instant occurredAt) {
        this.type = Objects.requireNonNull(type, "type");
        this.booking = Objects.requireNonNull(booking, "booking");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }

    public BookingEventType getType() {
        return type;
    }

    public Booking getBooking() {
        return booking;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
