package in.neelporiya.airline;

import in.neelporiya.airline.exception.BookingNotFoundException;
import in.neelporiya.airline.exception.FlightInstanceNotFoundException;
import in.neelporiya.airline.exception.SeatUnavailableException;
import in.neelporiya.airline.model.Booking;
import in.neelporiya.airline.model.Flight;
import in.neelporiya.airline.model.FlightInstance;
import in.neelporiya.airline.model.Passenger;
import in.neelporiya.airline.model.Seat;
import in.neelporiya.airline.notification.BookingEvent;
import in.neelporiya.airline.notification.BookingEventListener;
import in.neelporiya.airline.notification.BookingEventType;
import in.neelporiya.airline.pricing.FarePricingStrategy;
import in.neelporiya.airline.pricing.SeatClassFarePricingStrategy;
import in.neelporiya.airline.repository.BookingRepository;
import in.neelporiya.airline.repository.FlightInstanceRepository;
import in.neelporiya.airline.repository.FlightRepository;
import in.neelporiya.airline.search.FlightSearchStrategy;
import in.neelporiya.airline.search.OriginDestinationDateSearchStrategy;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * // DESIGN PATTERN: Facade — one API for search, booking, cancellation, lifecycle, repositories and notifications.
 *
 * <p>// TESTABILITY: Clock and id Supplier are injected; tests use MutableClock and deterministic ids.
 */
public final class AirlineService {

    private final FlightRepository flightRepository;
    private final FlightInstanceRepository flightInstanceRepository;
    private final BookingRepository bookingRepository;
    private final FarePricingStrategy farePricingStrategy;
    private final FlightSearchStrategy flightSearchStrategy;
    private final Clock clock;
    private final Supplier<String> idGenerator;
    private final List<BookingEventListener> listeners = new CopyOnWriteArrayList<>();

    public AirlineService(Clock clock, Supplier<String> idGenerator) {
        this(new FlightRepository(), new FlightInstanceRepository(), new BookingRepository(),
                new SeatClassFarePricingStrategy(), new OriginDestinationDateSearchStrategy(), clock, idGenerator);
    }

    public AirlineService(
            FlightRepository flightRepository,
            FlightInstanceRepository flightInstanceRepository,
            BookingRepository bookingRepository,
            FarePricingStrategy farePricingStrategy,
            FlightSearchStrategy flightSearchStrategy,
            Clock clock,
            Supplier<String> idGenerator) {
        this.flightRepository = Objects.requireNonNull(flightRepository, "flightRepository");
        this.flightInstanceRepository = Objects.requireNonNull(flightInstanceRepository, "flightInstanceRepository");
        this.bookingRepository = Objects.requireNonNull(bookingRepository, "bookingRepository");
        this.farePricingStrategy = Objects.requireNonNull(farePricingStrategy, "farePricingStrategy");
        this.flightSearchStrategy = Objects.requireNonNull(flightSearchStrategy, "flightSearchStrategy");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }

    public static AirlineService createDefault() {
        return new AirlineService(Clock.systemUTC(), () -> UUID.randomUUID().toString());
    }

    public void addFlight(Flight flight) {
        flightRepository.save(flight);
    }

    public void addFlightInstance(FlightInstance flightInstance) {
        flightInstanceRepository.save(flightInstance);
    }

    public void addListener(BookingEventListener listener) {
        // DESIGN PATTERN: Observer lets email/SMS/analytics react without the service knowing those channels.
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public List<FlightInstance> searchFlights(String origin, String destination, LocalDate date) {
        return flightSearchStrategy.search(flightInstanceRepository.findAll(), origin, destination, date);
    }

    public Booking bookSeat(Passenger passenger, String flightInstanceId, String seatNumber) {
        Objects.requireNonNull(passenger, "passenger");
        FlightInstance instance = requireFlightInstance(flightInstanceId);
        Seat seat = instance.findSeat(seatNumber)
                .orElseThrow(() -> new SeatUnavailableException("No seat " + seatNumber + " on instance " + flightInstanceId));
        String bookingId = idGenerator.get();

        // CONCURRENCY: all racing callers may find the same Seat object, but exactly one CAS below can write its booking id.
        if (!seat.tryClaim(bookingId)) {
            throw new SeatUnavailableException("Seat " + seatNumber + " is already booked");
        }

        Booking booking = Booking.builder()
                .id(bookingId)
                .passenger(passenger)
                .flightInstanceId(flightInstanceId)
                .seatNumber(seat.getSeatNumber())
                .seatClass(seat.getSeatClass())
                .fare(farePricingStrategy.price(instance, seat))
                .createdAt(clock.instant())
                .build();
        try {
            bookingRepository.save(booking);
        } catch (RuntimeException e) {
            seat.releaseBooking(bookingId);
            throw e;
        }
        notifyListeners(BookingEventType.BOOKED, booking);
        return booking;
    }

    public Booking checkIn(String bookingId) {
        Booking booking = requireBooking(bookingId);
        booking.checkIn(clock.instant());
        notifyListeners(BookingEventType.CHECKED_IN, booking);
        return booking;
    }

    public Booking cancel(String bookingId) {
        Booking booking = requireBooking(bookingId);
        FlightInstance instance = requireFlightInstance(booking.getFlightInstanceId());
        Seat seat = instance.findSeat(booking.getSeatNumber())
                .orElseThrow(() -> new SeatUnavailableException("No seat " + booking.getSeatNumber()));

        booking.cancel(clock.instant());
        seat.releaseBooking(booking.getId());
        notifyListeners(BookingEventType.CANCELLED, booking);
        return booking;
    }

    public Booking findBookingOrThrow(String bookingId) {
        return requireBooking(bookingId);
    }

    public List<Booking> bookings() {
        return bookingRepository.findAll();
    }

    private void notifyListeners(BookingEventType type, Booking booking) {
        BookingEvent event = new BookingEvent(type, booking, clock.instant());
        for (BookingEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    private FlightInstance requireFlightInstance(String flightInstanceId) {
        return flightInstanceRepository.findById(flightInstanceId)
                .orElseThrow(() -> new FlightInstanceNotFoundException("No flight instance with id " + flightInstanceId));
    }

    private Booking requireBooking(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("No booking with id " + bookingId));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private FlightRepository flightRepository = new FlightRepository();
        private FlightInstanceRepository flightInstanceRepository = new FlightInstanceRepository();
        private BookingRepository bookingRepository = new BookingRepository();
        private FarePricingStrategy farePricingStrategy = new SeatClassFarePricingStrategy();
        private FlightSearchStrategy flightSearchStrategy = new OriginDestinationDateSearchStrategy();
        private Clock clock = Clock.systemUTC();
        private Supplier<String> idGenerator = () -> UUID.randomUUID().toString();

        public Builder clock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public Builder idGenerator(Supplier<String> idGenerator) {
            this.idGenerator = idGenerator;
            return this;
        }

        public Builder farePricingStrategy(FarePricingStrategy farePricingStrategy) {
            this.farePricingStrategy = farePricingStrategy;
            return this;
        }

        public Builder flightSearchStrategy(FlightSearchStrategy flightSearchStrategy) {
            this.flightSearchStrategy = flightSearchStrategy;
            return this;
        }

        public AirlineService build() {
            return new AirlineService(flightRepository, flightInstanceRepository, bookingRepository,
                    farePricingStrategy, flightSearchStrategy, clock, idGenerator);
        }
    }
}
