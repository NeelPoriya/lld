package in.neelporiya.concertbooking;

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
 * // DESIGN PATTERN: Facade. This is the clean interview API over venues, concerts, repositories,
 * pricing, selection, hold expiry, confirmation, cancellation, and notifications.
 */
public class BookingService {

    private final Repository<Venue> venues;
    private final Repository<Concert> concerts;
    private final Repository<Booking> bookings;
    private final Clock clock;
    private final Supplier<String> idGenerator;
    private final Duration holdDuration;
    private final SectionPricingStrategy pricingStrategy;
    private final SeatSelectionStrategy seatSelectionStrategy;
    private final List<BookingEventListener> listeners = new CopyOnWriteArrayList<>();

    private BookingService(Builder builder) {
        this.venues = builder.venues;
        this.concerts = builder.concerts;
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
        listeners.add(listener);
    }

    public Venue createVenue(Venue venue) {
        venues.save(venue);
        return venue;
    }

    public Concert createConcert(Concert concert) {
        venues.save(concert.getVenue());
        concerts.save(concert);
        return concert;
    }

    public List<Concert> searchConcerts(String query) {
        String normalized = query.toLowerCase(Locale.ROOT);
        return concerts.findAll().stream()
                .filter(concert -> concert.getArtist().toLowerCase(Locale.ROOT).contains(normalized)
                        || concert.getVenue().getName().toLowerCase(Locale.ROOT).contains(normalized))
                .toList();
    }

    public Booking holdSeats(String concertId, String userId, List<String> seatIds) {
        if (seatIds.isEmpty()) {
            throw new IllegalArgumentException("select at least one seat");
        }
        Concert concert = requireConcert(concertId);
        cleanupExpiredHolds(concertId);
        Instant now = clock.instant();
        Instant expiresAt = now.plus(holdDuration);
        String bookingId = idGenerator.get();
        List<Seat> claimed = new ArrayList<>();

        for (String seatId : seatIds) {
            Seat seat = concert.getVenue().getSeat(seatId);
            if (seat.tryHold(userId, bookingId, expiresAt, now)) {
                claimed.add(seat);
            } else {
                claimed.forEach(previous -> previous.releaseForBooking(bookingId));
                throw new SeatUnavailableException("seat unavailable: " + seatId);
            }
        }

        Booking booking = new Booking(bookingId, concertId, userId, seatIds, now, expiresAt,
                priceSeats(concert, claimed));
        bookings.save(booking);
        fire(BookingEventType.HOLD_PLACED, booking, now);
        return booking;
    }

    public Booking holdBestAvailableSeats(String concertId, String userId, int count) {
        Concert concert = requireConcert(concertId);
        cleanupExpiredHolds(concertId);
        List<String> seatIds = seatSelectionStrategy.selectSeats(concert, count, clock.instant())
                .stream()
                .map(Seat::getId)
                .toList();
        if (seatIds.size() != count) {
            throw new SeatUnavailableException("not enough seats");
        }
        return holdSeats(concertId, userId, seatIds);
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
            Concert concert = requireConcert(booking.getConcertId());
            List<Seat> seats = seatsFor(concert, booking);
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
            Concert concert = requireConcert(booking.getConcertId());
            seatsFor(concert, booking).forEach(seat -> seat.releaseForBooking(bookingId));
            fire(BookingEventType.BOOKING_CANCELLED, booking, now);
            return booking;
        }
    }

    public List<Seat> availableSeats(String concertId) {
        Concert concert = requireConcert(concertId);
        cleanupExpiredHolds(concertId);
        Instant now = clock.instant();
        return concert.getVenue().getSeats().stream()
                .filter(seat -> seat.getState(now) == SeatState.AVAILABLE)
                .toList();
    }

    public SeatState seatState(String concertId, String seatId) {
        Concert concert = requireConcert(concertId);
        cleanupExpiredHolds(concertId);
        return concert.getVenue().getSeat(seatId).getState(clock.instant());
    }

    public Booking getBooking(String bookingId) {
        Booking booking = requireBooking(bookingId);
        expireIfNeeded(booking, clock.instant());
        return booking;
    }

    public int cleanupExpiredHolds(String concertId) {
        Instant now = clock.instant();
        int expired = 0;
        for (Booking booking : bookings.findAll()) {
            if (booking.getConcertId().equals(concertId) && expireIfNeeded(booking, now)) {
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
            Concert concert = requireConcert(booking.getConcertId());
            seatsFor(concert, booking).forEach(seat -> seat.releaseForBooking(booking.getId()));
            fire(BookingEventType.HOLD_EXPIRED, booking, now);
            return true;
        }
    }

    private long priceSeats(Concert concert, List<Seat> seats) {
        long total = 0L;
        for (Seat seat : seats) {
            Section section = concert.getVenue().getSection(seat.getSectionId());
            total += pricingStrategy.priceCents(concert, section, seat);
        }
        return total;
    }

    private List<Seat> seatsFor(Concert concert, Booking booking) {
        return booking.getSeatIds().stream()
                .map(seatId -> concert.getVenue().getSeat(seatId))
                .toList();
    }

    private Concert requireConcert(String concertId) {
        return concerts.findById(concertId).orElseThrow(() -> new BookingException("unknown concert " + concertId));
    }

    private Booking requireBooking(String bookingId) {
        return bookings.findById(bookingId).orElseThrow(() -> new BookingException("unknown booking " + bookingId));
    }

    private void fire(BookingEventType type, Booking booking, Instant now) {
        BookingEvent event = new BookingEvent(type, booking, now);
        listeners.forEach(listener -> listener.onBookingEvent(event));
    }

    public static class Builder {
        private Repository<Venue> venues = new InMemoryRepository<>();
        private Repository<Concert> concerts = new InMemoryRepository<>();
        private Repository<Booking> bookings = new InMemoryRepository<>();
        private Clock clock = Clock.systemUTC();
        private Supplier<String> idGenerator = () -> UUID.randomUUID().toString();
        private Duration holdDuration = Duration.ofMinutes(5);
        private SectionPricingStrategy pricingStrategy = new FixedSectionPricingStrategy();
        private SeatSelectionStrategy seatSelectionStrategy = new BestAvailableSeatSelectionStrategy();

        public Builder venues(Repository<Venue> venues) {
            this.venues = Objects.requireNonNull(venues, "venues");
            return this;
        }

        public Builder concerts(Repository<Concert> concerts) {
            this.concerts = Objects.requireNonNull(concerts, "concerts");
            return this;
        }

        public Builder bookings(Repository<Booking> bookings) {
            this.bookings = Objects.requireNonNull(bookings, "bookings");
            return this;
        }

        public Builder clock(Clock clock) {
            // TESTABILITY: production passes system UTC; tests pass MutableClock and advance it past
            // the hold duration with zero Thread.sleep.
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

        public Builder pricingStrategy(SectionPricingStrategy pricingStrategy) {
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
