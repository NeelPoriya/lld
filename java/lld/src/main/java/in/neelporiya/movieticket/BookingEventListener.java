package in.neelporiya.movieticket;

/** // DESIGN PATTERN: Observer. Email/SMS/analytics react without coupling to BookingService. */
public interface BookingEventListener {
    void onBookingEvent(BookingEvent event);
}
