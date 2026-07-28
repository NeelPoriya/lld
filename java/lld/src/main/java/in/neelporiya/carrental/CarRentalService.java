package in.neelporiya.carrental;

import in.neelporiya.carrental.exception.ReservationNotFoundException;
import in.neelporiya.carrental.exception.VehicleNotAvailableException;
import in.neelporiya.carrental.exception.VehicleNotFoundException;
import in.neelporiya.carrental.model.AddOn;
import in.neelporiya.carrental.model.DateRange;
import in.neelporiya.carrental.model.Reservation;
import in.neelporiya.carrental.model.ReservationStatus;
import in.neelporiya.carrental.model.Vehicle;
import in.neelporiya.carrental.model.VehicleType;
import in.neelporiya.carrental.pricing.PerDayPricingStrategy;
import in.neelporiya.carrental.pricing.PricingStrategy;
import in.neelporiya.carrental.repository.ReservationRepository;
import in.neelporiya.carrental.repository.VehicleRepository;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * // DESIGN PATTERN: Facade — one clean API over fleet search, reservations, lifecycle, pricing and repositories.
 *
 * <p>// TESTABILITY: {@link Clock} and id {@link Supplier} are injected; tests use MutableClock and deterministic ids.
 */
public class CarRentalService {

    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;
    private final PricingStrategy pricingStrategy;
    private final Clock clock;
    private final Supplier<String> idGenerator;
    private final ConcurrentMap<String, Object> locksByVehicleId = new ConcurrentHashMap<>();

    public CarRentalService(Clock clock, Supplier<String> idGenerator, PricingStrategy pricingStrategy) {
        this(new VehicleRepository(), new ReservationRepository(), clock, idGenerator, pricingStrategy);
    }

    public CarRentalService(
            VehicleRepository vehicleRepository,
            ReservationRepository reservationRepository,
            Clock clock,
            Supplier<String> idGenerator,
            PricingStrategy pricingStrategy) {
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository, "vehicleRepository");
        this.reservationRepository = Objects.requireNonNull(reservationRepository, "reservationRepository");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
        this.pricingStrategy = Objects.requireNonNull(pricingStrategy, "pricingStrategy");
    }

    public static CarRentalService createDefault() {
        return new CarRentalService(Clock.systemUTC(), () -> UUID.randomUUID().toString(), new PerDayPricingStrategy());
    }

    public void addVehicle(Vehicle vehicle) {
        vehicleRepository.save(vehicle);
    }

    public Optional<Vehicle> findVehicle(String vehicleId) {
        return vehicleRepository.findById(vehicleId);
    }

    public Optional<Reservation> findReservation(String reservationId) {
        return reservationRepository.findById(reservationId);
    }

    public List<Vehicle> searchAvailable(String storeId, DateRange range) {
        return searchAvailable(storeId, range, Optional.empty());
    }

    public List<Vehicle> searchAvailable(String storeId, DateRange range, Optional<VehicleType> type) {
        Objects.requireNonNull(storeId, "storeId");
        Objects.requireNonNull(range, "range");
        Objects.requireNonNull(type, "type");
        // EXTENSIBILITY: extra filters (transmission, fuel, accessibility) can be added here without changing reservations.
        return vehicleRepository.findAll().stream()
                .filter(vehicle -> vehicle.getStoreId().equals(storeId))
                .filter(vehicle -> type.map(vehicleType -> vehicle.getType() == vehicleType).orElse(true))
                .filter(vehicle -> isAvailable(vehicle.getId(), range))
                .toList();
    }

    public Reservation reserve(String customerId, String vehicleId, DateRange range, Set<AddOn> addOns) {
        Objects.requireNonNull(range, "range");
        Vehicle vehicle = requireVehicle(vehicleId);
        Object lock = locksByVehicleId.computeIfAbsent(vehicleId, ignored -> new Object());

        synchronized (lock) {
            // INTERVIEW INSIGHT: "check availability, then save" is only safe when both happen under the same vehicle lock.
            // CONCURRENCY: overlap check and insert are one critical section; otherwise two threads can both see "free" and double-book.
            boolean overlapsExisting = reservationRepository.activeForVehicle(vehicleId).stream()
                    .anyMatch(existing -> existing.getRange().overlaps(range));
            if (overlapsExisting) {
                throw new VehicleNotAvailableException("Vehicle " + vehicleId + " is not available for the requested range");
            }

            Reservation reservation = Reservation.builder()
                    .id(idGenerator.get())
                    .vehicleId(vehicleId)
                    .customerId(customerId)
                    .range(range)
                    .addOns(addOns)
                    .quotedPrice(pricingStrategy.calculate(vehicle, range, addOns))
                    .createdAt(clock.instant())
                    .build();
            reservationRepository.save(reservation);
            return reservation;
        }
    }

    public Reservation pickUp(String reservationId) {
        Reservation reservation = requireReservation(reservationId);
        synchronized (lockFor(reservation.getVehicleId())) {
            reservation.pickUp(clock.instant());
        }
        return reservation;
    }

    public Reservation returnVehicle(String reservationId) {
        Reservation reservation = requireReservation(reservationId);
        synchronized (lockFor(reservation.getVehicleId())) {
            reservation.returnVehicle(clock.instant());
        }
        return reservation;
    }

    public Reservation cancel(String reservationId) {
        Reservation reservation = requireReservation(reservationId);
        synchronized (lockFor(reservation.getVehicleId())) {
            reservation.cancel(clock.instant());
        }
        return reservation;
    }

    public boolean isAvailable(String vehicleId, DateRange range) {
        Objects.requireNonNull(range, "range");
        requireVehicle(vehicleId);
        synchronized (lockFor(vehicleId)) {
            return reservationRepository.activeForVehicle(vehicleId).stream()
                    .noneMatch(reservation -> reservation.getStatus() != ReservationStatus.CANCELLED
                            && reservation.getRange().overlaps(range));
        }
    }

    private Object lockFor(String vehicleId) {
        return locksByVehicleId.computeIfAbsent(vehicleId, ignored -> new Object());
    }

    private Vehicle requireVehicle(String vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException("No vehicle with id " + vehicleId));
    }

    private Reservation requireReservation(String reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("No reservation with id " + reservationId));
    }
}
