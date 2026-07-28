package in.neelporiya.airline.notification;

import in.neelporiya.airline.model.Booking;

import java.time.Instant;

public record BookingEvent(BookingEventType type, Booking booking, Instant occurredAt) {
}
