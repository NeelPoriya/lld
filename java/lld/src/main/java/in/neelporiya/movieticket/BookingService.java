package in.neelporiya.movieticket;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * // DESIGN PATTERN: Facade. One API over catalog repositories, hold/confirm/cancel/expire,
 * pricing, selection, and notifications.
 */
public class BookingService {

    private final Repository<City> cities;
    private final Repository<Cinema> cinemas;
    private final Repository<Movie> movies;
    private final Repository<Show> shows;
    private final Repository<Booking> bookings;
    private final Clock clock;
    private final Supplier<String> idGenerator;
    private final Duration holdDuration;
    private final SeatPricingStrategy pricingStrategy;
    private final SeatSelectionStrategy seatSelectionStrategy;
    private final List<BookingEventListener> listeners = new CopyOnWriteArrayList<>();

    private BookingService(Builder builder) {
        this.cities = builder.cities;
        this.cinemas = builder.cinemas;
        this.movies = builder.movies;
        this.shows = builder.shows;
        this.bookings = builder.bookings;
        this.clock = builder.clock;
        this.idGenerator = builder.idGenerator;
        this.holdDuration = builder.holdDuration;
        this.pricingStrategy = builder.pricingStrategy;
        this.seatSelectionStrategy = builder.seatSelectionStrategy;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static BookingService createDefault() {
        return builder().build();
    }

    public void addListener(BookingEventListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public City createCity(City city) {
        cities.save(city);
        return city;
    }

    public Cinema createCinema(Cinema cinema) {
        cinemas.save(cinema);
        return cinema;
    }

    public Movie createMovie(Movie movie) {
        movies.save(movie);
        return movie;
    }

    public Show createShow(Show show) {
        movies.save(show.getMovie());
        cinemas.save(show.getCinema());
        shows.save(show);
        return show;
    }

    public List<Show> searchShows(String cityQuery, String movieQuery) {
        String city = cityQuery.toLowerCase(Locale.ROOT);
        String movie = movieQuery.toLowerCase(Locale.ROOT);
        return shows.findAll().stream()
                .filter(show -> show.getCinema().getCityName().toLowerCase(Locale.ROOT).contains(city)
                        || show.getCinema().getCityId().toLowerCase(Locale.ROOT).contains(city))
                .filter(show -> show.getMovie().getTitle().toLowerCase(Locale.ROOT).contains(movie)
                        || show.getMovie().getId().toLowerCase(Locale.ROOT).contains(movie))
                .toList();
    }

    public Booking holdSeats(String showId, String userId, List<String> seatIds) {
        if (seatIds.isEmpty()) {
            throw new IllegalArgumentException("select at least one seat");
        }
        Show show = requireShow(showId);
        cleanupExpiredHolds(showId);
        Instant now = clock.instant();
        Instant expiresAt = now.plus(holdDuration);
        String bookingId = idGenerator.get();
        List<Seat> claimed = new ArrayList<>();

        for (String seatId : seatIds) {
            Seat seat = show.getScreen().getSeat(seatId);
            if (seat.tryHold(userId, bookingId, expiresAt, now)) {
                claimed.add(seat);
            } else {
                claimed.forEach(previous -> previous.releaseForBooking(bookingId));
                throw new SeatUnavailableException("seat unavailable: " + seatId);
            }
        }

        Booking booking = new Booking(bookingId, showId, userId, seatIds, now, expiresAt, priceSeats(show, claimed));
        bookings.save(booking);
        fire(BookingEventType.HOLD_PLACED, booking, now);
        return booking;
    }

    public Booking holdBestAvailableSeats(String showId, String userId, int count) {
        Show show = requireShow(showId);
        cleanupExpiredHolds(showId);
        List<String> seatIds = seatSelectionStrategy.selectSeats(show, count, clock.instant()).stream()
                .map(Seat::getId)
                .toList();
        if (seatIds.size() != count) {
            throw new SeatUnavailableException("not enough seats");
        }
        return holdSeats(showId, userId, seatIds);
    }

    public Booking confirmBooking(String bookingId, String userId) {
        Booking booking = requireBooking(bookingId);
        synchronized (booking) {
            Instant now = clock.instant();
            expireIfNeeded(booking, now);
            if (!booking.getUserId().equals(userId)) {
                throw new BookingException("booking belongs to a different user");
            }
            if (booking.getStatus() != BookingStatus.HELD) {
                throw new BookingException("booking is not confirmable: " + booking.getStatus());
            }
            Show show = requireShow(booking.getShowId());
            List<Seat> seats = seatsFor(show, booking);
            boolean allHeld = seats.stream().allMatch(seat -> seat.canConfirm(bookingId, now));
            if (!allHeld || booking.isExpiredAt(now)) {
                expireIfNeeded(booking, now);
                throw new BookingException("hold expired or unavailable");
            }
            if (!booking.markConfirmed(now)) {
                throw new BookingException("booking is not confirmable");
            }
            for (Seat seat : seats) {
                if (!seat.confirmHold(bookingId, now)) {
                    throw new BookingException("seat changed before confirmation");
                }
            }
            fire(BookingEventType.BOOKING_CONFIRMED, booking, now);
            return booking;
        }
    }

    public Booking cancelBooking(String bookingId, String userId) {
        Booking booking = requireBooking(bookingId);
        synchronized (booking) {
            Instant now = clock.instant();
            if (!booking.getUserId().equals(userId)) {
                throw new BookingException("booking belongs to a different user");
            }
            if (!booking.markCancelled(now)) {
                throw new BookingException("booking cannot be cancelled: " + booking.getStatus());
            }
            Show show = requireShow(booking.getShowId());
            seatsFor(show, booking).forEach(seat -> seat.releaseForBooking(bookingId));
            fire(BookingEventType.BOOKING_CANCELLED, booking, now);
            return booking;
        }
    }

    public List<Seat> availableSeats(String showId) {
        Show show = requireShow(showId);
        cleanupExpiredHolds(showId);
        Instant now = clock.instant();
        return show.getScreen().getSeats().stream()
                .filter(seat -> seat.getState(now) == SeatState.AVAILABLE)
                .toList();
    }

    public SeatState seatState(String showId, String seatId) {
        Show show = requireShow(showId);
        cleanupExpiredHolds(showId);
        return show.getScreen().getSeat(seatId).getState(clock.instant());
    }

    public Booking getBooking(String bookingId) {
        Booking booking = requireBooking(bookingId);
        expireIfNeeded(booking, clock.instant());
        return booking;
    }

    public int cleanupExpiredHolds(String showId) {
        Instant now = clock.instant();
        int expired = 0;
        for (Booking booking : bookings.findAll()) {
            if (booking.getShowId().equals(showId) && expireIfNeeded(booking, now)) {
                expired++;
            }
        }
        return expired;
    }

    private boolean expireIfNeeded(Booking booking, Instant now) {
        synchronized (booking) {
            if (!booking.isExpiredAt(now)) {
                return false;
            }
            if (!booking.markExpired(now)) {
                return false;
            }
            Show show = requireShow(booking.getShowId());
            seatsFor(show, booking).forEach(seat -> seat.releaseForBooking(booking.getId()));
            fire(BookingEventType.HOLD_EXPIRED, booking, now);
            return true;
        }
    }

    private long priceSeats(Show show, List<Seat> seats) {
        long total = 0L;
        for (Seat seat : seats) {
            total += pricingStrategy.priceCents(show, seat);
        }
        return total;
    }

    private List<Seat> seatsFor(Show show, Booking booking) {
        return booking.getSeatIds().stream()
                .map(seatId -> show.getScreen().getSeat(seatId))
                .toList();
    }

    private Show requireShow(String showId) {
        return shows.findById(showId).orElseThrow(() -> new BookingException("unknown show " + showId));
    }

    private Booking requireBooking(String bookingId) {
        return bookings.findById(bookingId).orElseThrow(() -> new BookingException("unknown booking " + bookingId));
    }

    private void fire(BookingEventType type, Booking booking, Instant now) {
        BookingEvent event = new BookingEvent(type, booking, now);
        listeners.forEach(listener -> listener.onBookingEvent(event));
    }

    public static class Builder {
        private Repository<City> cities = new InMemoryRepository<>();
        private Repository<Cinema> cinemas = new InMemoryRepository<>();
        private Repository<Movie> movies = new InMemoryRepository<>();
        private Repository<Show> shows = new InMemoryRepository<>();
        private Repository<Booking> bookings = new InMemoryRepository<>();
        private Clock clock = Clock.systemUTC();
        private Supplier<String> idGenerator = () -> UUID.randomUUID().toString();
        private Duration holdDuration = Duration.ofMinutes(5);
        private SeatPricingStrategy pricingStrategy = new SeatTypePricingStrategy();
        private SeatSelectionStrategy seatSelectionStrategy = new BestAvailableSeatSelectionStrategy();

        public Builder cities(Repository<City> cities) {
            this.cities = Objects.requireNonNull(cities, "cities");
            return this;
        }

        public Builder cinemas(Repository<Cinema> cinemas) {
            this.cinemas = Objects.requireNonNull(cinemas, "cinemas");
            return this;
        }

        public Builder movies(Repository<Movie> movies) {
            this.movies = Objects.requireNonNull(movies, "movies");
            return this;
        }

        public Builder shows(Repository<Show> shows) {
            this.shows = Objects.requireNonNull(shows, "shows");
            return this;
        }

        public Builder bookings(Repository<Booking> bookings) {
            this.bookings = Objects.requireNonNull(bookings, "bookings");
            return this;
        }

        public Builder clock(Clock clock) {
            // TESTABILITY: tests pass MutableClock and advance past holdDuration; production uses UTC.
            this.clock = Objects.requireNonNull(clock, "clock");
            return this;
        }

        public Builder idGenerator(Supplier<String> idGenerator) {
            this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
            return this;
        }

        public Builder holdDuration(Duration holdDuration) {
            this.holdDuration = Objects.requireNonNull(holdDuration, "holdDuration");
            return this;
        }

        public Builder pricingStrategy(SeatPricingStrategy pricingStrategy) {
            this.pricingStrategy = Objects.requireNonNull(pricingStrategy, "pricingStrategy");
            return this;
        }

        public Builder seatSelectionStrategy(SeatSelectionStrategy seatSelectionStrategy) {
            this.seatSelectionStrategy = Objects.requireNonNull(seatSelectionStrategy, "seatSelectionStrategy");
            return this;
        }

        public BookingService build() {
            return new BookingService(this);
        }
    }
}
