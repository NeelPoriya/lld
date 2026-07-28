package in.neelporiya.hotelmanagement;

import in.neelporiya.hotelmanagement.exception.ReservationNotFoundException;
import in.neelporiya.hotelmanagement.exception.RoomNotAvailableException;
import in.neelporiya.hotelmanagement.exception.RoomNotFoundException;
import in.neelporiya.hotelmanagement.model.Reservation;
import in.neelporiya.hotelmanagement.model.Room;
import in.neelporiya.hotelmanagement.model.RoomType;
import in.neelporiya.hotelmanagement.model.StayRange;
import in.neelporiya.hotelmanagement.observer.ReservationEventListener;
import in.neelporiya.hotelmanagement.pricing.NightlyRatePricingStrategy;
import in.neelporiya.hotelmanagement.pricing.PricingStrategy;
import in.neelporiya.hotelmanagement.repository.ReservationRepository;
import in.neelporiya.hotelmanagement.repository.RoomRepository;
import in.neelporiya.hotelmanagement.search.DefaultRoomSearchStrategy;
import in.neelporiya.hotelmanagement.search.SearchStrategy;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * // DESIGN PATTERN: Facade — one clean API over room inventory, search, booking, lifecycle, pricing and events.
 *
 * <p>// TESTABILITY: {@link Clock} and id {@link Supplier} are injected; tests use MutableClock and deterministic ids.
 */
public class HotelService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final PricingStrategy pricingStrategy;
    private final SearchStrategy searchStrategy;
    private final Clock clock;
    private final Supplier<String> idGenerator;
    private final List<ReservationEventListener> listeners = new CopyOnWriteArrayList<>();
    private final ConcurrentMap<String, Object> locksByRoomId = new ConcurrentHashMap<>();

    public HotelService(Clock clock, Supplier<String> idGenerator, PricingStrategy pricingStrategy, SearchStrategy searchStrategy) {
        this(new RoomRepository(), new ReservationRepository(), clock, idGenerator, pricingStrategy, searchStrategy);
    }

    public HotelService(
            RoomRepository roomRepository,
            ReservationRepository reservationRepository,
            Clock clock,
            Supplier<String> idGenerator,
            PricingStrategy pricingStrategy,
            SearchStrategy searchStrategy) {
        this.roomRepository = Objects.requireNonNull(roomRepository, "roomRepository");
        this.reservationRepository = Objects.requireNonNull(reservationRepository, "reservationRepository");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
        this.pricingStrategy = Objects.requireNonNull(pricingStrategy, "pricingStrategy");
        this.searchStrategy = Objects.requireNonNull(searchStrategy, "searchStrategy");
    }

    public static HotelService createDefault() {
        return new HotelService(
                Clock.systemUTC(),
                () -> UUID.randomUUID().toString(),
                new NightlyRatePricingStrategy(),
                new DefaultRoomSearchStrategy());
    }

    public void addRoom(Room room) {
        roomRepository.save(room);
    }

    public void addListener(ReservationEventListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public Optional<Room> findRoom(String roomId) {
        return roomRepository.findById(roomId);
    }

    public Optional<Reservation> findReservation(String reservationId) {
        return reservationRepository.findById(reservationId);
    }

    public List<Room> searchAvailable(StayRange range) {
        return searchAvailable(range, Optional.empty());
    }

    public List<Room> searchAvailable(StayRange range, Optional<RoomType> type) {
        Objects.requireNonNull(range, "range");
        Objects.requireNonNull(type, "type");
        return searchStrategy.search(roomRepository.findAll(), reservationRepository, range, type);
    }

    public Reservation bookRoom(String guestId, String roomId, StayRange range) {
        Objects.requireNonNull(range, "range");
        Room room = requireRoom(roomId);
        Object lock = lockFor(roomId);

        synchronized (lock) {
            // INTERVIEW INSIGHT: "search says available" is not a booking guarantee; only this room lock closes the race.
            // CONCURRENCY: overlap check and insert are atomic. Without this, two threads can both see "no overlap" and save.
            boolean overlapsExisting = reservationRepository.activeForRoom(roomId).stream()
                    .anyMatch(existing -> existing.getRange().overlaps(range));
            if (overlapsExisting) {
                throw new RoomNotAvailableException("Room " + roomId + " is not available for the requested range");
            }

            Reservation reservation = Reservation.builder()
                    .id(idGenerator.get())
                    .roomId(roomId)
                    .guestId(guestId)
                    .range(range)
                    .quotedPriceCents(pricingStrategy.calculateCents(room, range))
                    .createdAt(clock.instant())
                    .build();
            reservationRepository.save(reservation);
            listeners.forEach(listener -> listener.onBooked(reservation));
            return reservation;
        }
    }

    public Reservation bookRoomOfType(String guestId, RoomType type, StayRange range) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(range, "range");
        // EXTENSIBILITY: type booking composes search strategy with specific-room booking rather than duplicating rules.
        for (Room room : searchAvailable(range, Optional.of(type))) {
            try {
                return bookRoom(guestId, room.getId(), range);
            } catch (RoomNotAvailableException raced) {
                // CONCURRENCY: Another thread may grab the searched room before us; try the next candidate.
            }
        }
        throw new RoomNotAvailableException("No " + type + " room is available for the requested range");
    }

    public Reservation checkIn(String reservationId) {
        Reservation reservation = requireReservation(reservationId);
        synchronized (lockFor(reservation.getRoomId())) {
            reservation.checkIn(clock.instant());
        }
        listeners.forEach(listener -> listener.onCheckedIn(reservation));
        return reservation;
    }

    public Reservation checkOut(String reservationId) {
        Reservation reservation = requireReservation(reservationId);
        synchronized (lockFor(reservation.getRoomId())) {
            reservation.checkOut(clock.instant());
        }
        listeners.forEach(listener -> listener.onCheckedOut(reservation));
        return reservation;
    }

    public Reservation cancel(String reservationId) {
        Reservation reservation = requireReservation(reservationId);
        synchronized (lockFor(reservation.getRoomId())) {
            reservation.cancel(clock.instant());
        }
        listeners.forEach(listener -> listener.onCancelled(reservation));
        return reservation;
    }

    public boolean isAvailable(String roomId, StayRange range) {
        Objects.requireNonNull(range, "range");
        requireRoom(roomId);
        synchronized (lockFor(roomId)) {
            return reservationRepository.activeForRoom(roomId).stream()
                    .noneMatch(reservation -> reservation.getRange().overlaps(range));
        }
    }

    private Object lockFor(String roomId) {
        return locksByRoomId.computeIfAbsent(roomId, ignored -> new Object());
    }

    private Room requireRoom(String roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("No room with id " + roomId));
    }

    private Reservation requireReservation(String reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("No reservation with id " + reservationId));
    }
}
