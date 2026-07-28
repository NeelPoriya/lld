package in.neelporiya.concertbooking;

import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcertBookingServiceTest {

    private BookingService service(MutableClock clock) {
        AtomicInteger ids = new AtomicInteger();
        return BookingService.builder()
                .clock(clock)
                .holdDuration(Duration.ofMinutes(5))
                .idGenerator(() -> "b-" + ids.incrementAndGet())
                .build();
    }

    private Concert addConcert(BookingService service, Instant startsAt) {
        Venue venue = Venue.builder("venue-1", "City Arena")
                .addSection(new Section("vip", "VIP", "GOLD", 25000))
                .addSection(new Section("balcony", "Balcony", "SILVER", 7500))
                .addSeat(new Seat("A1", "vip", "A", 1))
                .addSeat(new Seat("A2", "vip", "A", 2))
                .addSeat(new Seat("B1", "balcony", "B", 1))
                .build();
        Concert concert = new Concert("concert-1", "The Patterns", startsAt, venue);
        service.createConcert(concert);
        return concert;
    }

    @Test
    void searchesConcertsByArtistOrVenue() {
        MutableClock clock = MutableClock.atEpoch();
        BookingService service = service(clock);
        addConcert(service, clock.instant());

        assertEquals(1, service.searchConcerts("patterns").size());
        assertEquals(1, service.searchConcerts("arena").size());
        assertTrue(service.searchConcerts("unknown").isEmpty());
    }

    @Test
    void holdSeatsThenConfirmBooksThem() {
        MutableClock clock = MutableClock.atEpoch();
        BookingService service = service(clock);
        Concert concert = addConcert(service, clock.instant());

        Booking booking = service.holdSeats(concert.getId(), "user-1", List.of("A1", "B1"));
        assertEquals(BookingStatus.HELD, booking.getStatus());
        assertEquals(32500, booking.getTotalPriceCents());

        service.confirmBooking(booking.getId(), "user-1");

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(SeatState.BOOKED, service.seatState(concert.getId(), "A1"));
        assertEquals(SeatState.BOOKED, service.seatState(concert.getId(), "B1"));
    }

    @Test
    void secondUserCannotHoldAlreadyHeldSeat() {
        MutableClock clock = MutableClock.atEpoch();
        BookingService service = service(clock);
        Concert concert = addConcert(service, clock.instant());

        service.holdSeats(concert.getId(), "user-1", List.of("A1"));

        assertThrows(SeatUnavailableException.class,
                () -> service.holdSeats(concert.getId(), "user-2", List.of("A1")));
    }

    @Test
    void expiredHoldFreesSeatForAnotherUserWithoutSleeping() {
        MutableClock clock = MutableClock.atEpoch();
        BookingService service = service(clock);
        Concert concert = addConcert(service, clock.instant());

        service.holdSeats(concert.getId(), "user-1", List.of("A1"));
        clock.advance(Duration.ofMinutes(6));

        Booking second = service.holdSeats(concert.getId(), "user-2", List.of("A1"));
        service.confirmBooking(second.getId(), "user-2");

        assertEquals(BookingStatus.CONFIRMED, second.getStatus());
        assertEquals(SeatState.BOOKED, service.seatState(concert.getId(), "A1"));
    }

    @Test
    void confirmingExpiredHoldFails() {
        MutableClock clock = MutableClock.atEpoch();
        BookingService service = service(clock);
        Concert concert = addConcert(service, clock.instant());
        Booking booking = service.holdSeats(concert.getId(), "user-1", List.of("A1"));

        clock.advance(Duration.ofMinutes(6));

        assertThrows(BookingException.class, () -> service.confirmBooking(booking.getId(), "user-1"));
        assertEquals(BookingStatus.EXPIRED, booking.getStatus());
        assertEquals(SeatState.AVAILABLE, service.seatState(concert.getId(), "A1"));
    }

    @Test
    void pricingComesFromSeatSection() {
        MutableClock clock = MutableClock.atEpoch();
        BookingService service = service(clock);
        Concert concert = addConcert(service, clock.instant());

        Booking vip = service.holdSeats(concert.getId(), "user-1", List.of("A1"));
        Booking balcony = service.holdSeats(concert.getId(), "user-2", List.of("B1"));

        assertEquals(25000, vip.getTotalPriceCents());
        assertEquals(7500, balcony.getTotalPriceCents());
    }

    @Test
    void cancellationFreesSeats() {
        MutableClock clock = MutableClock.atEpoch();
        BookingService service = service(clock);
        Concert concert = addConcert(service, clock.instant());
        Booking booking = service.holdSeats(concert.getId(), "user-1", List.of("A1"));
        service.confirmBooking(booking.getId(), "user-1");

        service.cancelBooking(booking.getId(), "user-1");

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        Booking second = service.holdSeats(concert.getId(), "user-2", List.of("A1"));
        assertEquals(BookingStatus.HELD, second.getStatus());
    }

    @Test
    void observerReceivesBookingEvents() {
        MutableClock clock = MutableClock.atEpoch();
        BookingService service = service(clock);
        Concert concert = addConcert(service, clock.instant());
        List<BookingEventType> events = new ArrayList<>();
        service.addListener(event -> events.add(event.getType()));

        Booking booking = service.holdSeats(concert.getId(), "user-1", List.of("A1"));
        service.confirmBooking(booking.getId(), "user-1");

        assertEquals(List.of(BookingEventType.HOLD_PLACED, BookingEventType.BOOKING_CONFIRMED), events);
    }
}
