package in.neelporiya.concertbooking;

/**
 * // DESIGN PATTERN: Observer. Email/SMS/analytics subscribe without coupling to BookingService.
 */
public interface BookingEventListener {
    void onBookingEvent(BookingEvent event);
}
