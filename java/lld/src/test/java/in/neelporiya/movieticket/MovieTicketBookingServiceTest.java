package in.neelporiya.movieticket;

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

class MovieTicketBookingServiceTest {

    private BookingService service(MutableClock clock) {
        AtomicInteger ids = new AtomicInteger();
        return BookingService.builder()
                .clock(clock)
                .holdDuration(Duration.ofMinutes(5))
                .idGenerator(() -> "b-" + ids.incrementAndGet())
                .build();
    }

    private Show addShow(BookingService service, Instant startsAt) {
        City city = service.createCity(new City("blr", "Bengaluru"));
        Screen screen = Screen.builder("screen-1", "Audi 1")
                .addSeat(new Seat("A1", "A", 1, SeatType.PREMIUM, 30000))
                .addSeat(new Seat("A2", "A", 2, SeatType.PREMIUM, 30000))
                .addSeat(new Seat("B1", "B", 1, SeatType.REGULAR, 12000))
                .build();
        Cinema cinema = service.createCinema(Cinema.builder("cinema-1", "Galaxy Cinemas", city)
                .addScreen(screen)
                .build());
        Movie movie = service.createMovie(new Movie("movie-1", "The Patterns", "EN", Duration.ofMinutes(140)));
        Show show = new Show("show-1", movie, cinema, screen, startsAt);
        service.createShow(show);
        return show;
    }

    @Test
    void searchesShowsByCityAndMovie() {
        MutableClock clock = MutableClock.atEpoch();
        BookingService service = service(clock);
        addShow(service, clock.instant());

        assertEquals(1, service.searchShows("bengaluru", "patterns").size());
        assertEquals(1, service.searchShows("blr", "movie-1").size());
        assertTrue(service.searchShows("mumbai", "patterns").isEmpty());
    }

    @Test
    void holdSeatsThenConfirmBooksThem() {
        MutableClock clock = MutableClock.atEpoch();
        BookingService service = service(clock);
        Show show = addShow(service, clock.instant());

        Booking booking = service.holdSeats(show.getId(), "user-1", List.of("A1", "B1"));
        assertEquals(BookingStatus.HELD, booking.getStatus());
        assertEquals(42000, booking.getTotalPriceCents());

        service.confirmBooking(booking.getId(), "user-1");

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(SeatState.BOOKED, service.seatState(show.getId(), "A1"));
        assertEquals(SeatState.BOOKED, service.seatState(show.getId(), "B1"));
    }

    @Test
    void secondUserCannotHoldAlreadyHeldSeat() {
        MutableClock clock = MutableClock.atEpoch();
        BookingService service = service(clock);
        Show show = addShow(service, clock.instant());

        service.holdSeats(show.getId(), "user-1", List.of("A1"));

        assertThrows(SeatUnavailableException.class,
                () -> service.holdSeats(show.getId(), "user-2", List.of("A1")));
    }

    @Test
    void expiredHoldFreesSeatForAnotherUserWithoutSleeping() {
        MutableClock clock = MutableClock.atEpoch();
        BookingService service = service(clock);
        Show show = addShow(service, clock.instant());

        service.holdSeats(show.getId(), "user-1", List.of("A1"));
        clock.advance(Duration.ofMinutes(6));

        Booking second = service.holdSeats(show.getId(), "user-2", List.of("A1"));
        service.confirmBooking(second.getId(), "user-2");

        assertEquals(BookingStatus.CONFIRMED, second.getStatus());
        assertEquals(SeatState.BOOKED, service.seatState(show.getId(), "A1"));
    }

    @Test
    void confirmingExpiredHoldFails() {
        MutableClock clock = MutableClock.atEpoch();
        BookingService service = service(clock);
        Show show = addShow(service, clock.instant());
        Booking booking = service.holdSeats(show.getId(), "user-1", List.of("A1"));

        clock.advance(Duration.ofMinutes(6));

        assertThrows(BookingException.class, () -> service.confirmBooking(booking.getId(), "user-1"));
        assertEquals(BookingStatus.EXPIRED, booking.getStatus());
        assertEquals(SeatState.AVAILABLE, service.seatState(show.getId(), "A1"));
    }

    @Test
    void pricingComesFromSeatType() {
        MutableClock clock = MutableClock.atEpoch();
        BookingService service = service(clock);
        Show show = addShow(service, clock.instant());

        Booking premium = service.holdSeats(show.getId(), "user-1", List.of("A1"));
        Booking regular = service.holdSeats(show.getId(), "user-2", List.of("B1"));

        assertEquals(30000, premium.getTotalPriceCents());
        assertEquals(12000, regular.getTotalPriceCents());
    }

    @Test
    void cancellationFreesSeats() {
        MutableClock clock = MutableClock.atEpoch();
        BookingService service = service(clock);
        Show show = addShow(service, clock.instant());
        Booking booking = service.holdSeats(show.getId(), "user-1", List.of("A1"));
        service.confirmBooking(booking.getId(), "user-1");

        service.cancelBooking(booking.getId(), "user-1");

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        Booking second = service.holdSeats(show.getId(), "user-2", List.of("A1"));
        assertEquals(BookingStatus.HELD, second.getStatus());
    }

    @Test
    void observerReceivesBookingEvents() {
        MutableClock clock = MutableClock.atEpoch();
        BookingService service = service(clock);
        Show show = addShow(service, clock.instant());
        List<BookingEventType> events = new ArrayList<>();
        service.addListener(event -> events.add(event.getType()));

        Booking booking = service.holdSeats(show.getId(), "user-1", List.of("A1"));
        service.confirmBooking(booking.getId(), "user-1");

        assertEquals(List.of(BookingEventType.HOLD_PLACED, BookingEventType.BOOKING_CONFIRMED), events);
    }
}
