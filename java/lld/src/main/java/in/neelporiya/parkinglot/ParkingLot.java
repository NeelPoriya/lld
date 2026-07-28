package in.neelporiya.parkinglot;

import in.neelporiya.parkinglot.exception.InvalidTicketException;
import in.neelporiya.parkinglot.exception.NoSpotAvailableException;
import in.neelporiya.parkinglot.observer.ParkingEventListener;
import in.neelporiya.parkinglot.pricing.FeeStrategy;
import in.neelporiya.parkinglot.pricing.VehicleTypeHourlyFeeStrategy;
import in.neelporiya.parkinglot.spot.ParkingSpot;
import in.neelporiya.parkinglot.spot.ParkingSpotType;
import in.neelporiya.parkinglot.strategy.NearestSpotAssignmentStrategy;
import in.neelporiya.parkinglot.strategy.SpotAssignmentStrategy;
import in.neelporiya.parkinglot.vehicle.Vehicle;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * // DESIGN PATTERN: Facade — one clean entry point over floors, spots, strategies and clock.
 *
 * <p>Note we deliberately do <strong>not</strong> make this a Singleton. A Singleton would be global
 * mutable state that is awkward to reset between tests and cannot model two lots in one JVM. Instead
 * the object is built via a {@link Builder} and injected where needed.
 *
 * <h2>Thread-safety summary</h2>
 * <ul>
 *   <li>{@code activeTickets} is a {@link ConcurrentHashMap}; {@code remove} is the atomic gate that
 *       guarantees a ticket is redeemed exactly once even under concurrent exits.</li>
 *   <li>Spot claiming is lock-free CAS inside {@link ParkingSpot}.</li>
 *   <li>{@code floors} and {@code listeners} are effectively immutable / copy-on-write.</li>
 * </ul>
 * There is <em>no</em> global lock, so throughput scales with the number of free spots.
 */
public class ParkingLot {

    private final List<ParkingFloor> floors;
    private final SpotAssignmentStrategy assignmentStrategy;
    private final FeeStrategy feeStrategy;
    private final Clock clock;
    private final Supplier<String> ticketIdGenerator;
    private final List<ParkingEventListener> listeners;

    private final Map<String, ParkingTicket> activeTickets = new ConcurrentHashMap<>();

    private ParkingLot(Builder builder) {
        this.floors = List.copyOf(builder.floors);
        this.assignmentStrategy = builder.assignmentStrategy;
        this.feeStrategy = builder.feeStrategy;
        this.clock = builder.clock;
        this.ticketIdGenerator = builder.ticketIdGenerator;
        this.listeners = new CopyOnWriteArrayList<>(builder.listeners);
    }

    /**
     * Park a vehicle: pick and atomically claim a spot, issue a ticket stamped with the current
     * time (from the injected {@link Clock}), and notify listeners.
     *
     * @throws NoSpotAvailableException if no fitting spot is free.
     */
    public ParkingTicket parkVehicle(Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "vehicle");

        ParkingSpot spot = assignmentStrategy.assignSpot(floors, vehicle)
                .orElseThrow(() -> new NoSpotAvailableException(
                        "No spot available for " + vehicle));

        // TESTABILITY: entry time comes from the injected clock, never Instant.now().
        ParkingTicket ticket =
                new ParkingTicket(ticketIdGenerator.get(), vehicle, spot, clock.instant());
        activeTickets.put(ticket.getId(), ticket);

        listeners.forEach(listener -> listener.onVehicleParked(ticket));
        return ticket;
    }

    /**
     * Redeem a ticket: compute the fee, free the spot, and notify listeners.
     *
     * <p>// CONCURRENCY: {@code activeTickets.remove(ticketId)} is the linearization point. If two
     * threads try to exit the same ticket simultaneously, only one receives the non-null ticket and
     * proceeds; the other gets {@code null} and we raise {@link InvalidTicketException}. This makes
     * double-exit impossible without any explicit locking.
     *
     * @throws InvalidTicketException if the id is unknown or already redeemed.
     */
    public ParkingReceipt unpark(String ticketId) {
        Objects.requireNonNull(ticketId, "ticketId");

        ParkingTicket ticket = activeTickets.remove(ticketId);
        if (ticket == null) {
            throw new InvalidTicketException("Unknown or already-used ticket: " + ticketId);
        }

        Instant exitTime = clock.instant();
        BigDecimal fee = feeStrategy.calculateFee(ticket, exitTime);
        ticket.close(exitTime, fee);
        ticket.getSpot().vacate();

        ParkingReceipt receipt = new ParkingReceipt(
                ticket.getId(),
                ticket.getVehicle().getLicensePlate(),
                ticket.getSpot().getId(),
                ticket.getEntryTime(),
                exitTime,
                fee);

        listeners.forEach(listener -> listener.onVehicleUnparked(receipt));
        return receipt;
    }

    public Optional<ParkingTicket> findActiveTicket(String ticketId) {
        return Optional.ofNullable(activeTickets.get(ticketId));
    }

    /** Live count of free spots per type across all floors (snapshot). */
    public Map<ParkingSpotType, Long> availability() {
        return floors.stream()
                .flatMap(floor -> floor.getSpots().stream())
                .filter(ParkingSpot::isFree)
                .collect(Collectors.groupingBy(ParkingSpot::getType, Collectors.counting()));
    }

    public int activeTicketCount() {
        return activeTickets.size();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * // DESIGN PATTERN: Builder — assemble a lot readably, apply sensible defaults, and produce an
     * immutable configuration. Also our seam for // TESTABILITY: {@code clock} and
     * {@code ticketIdGenerator} are injectable so tests are fully deterministic.
     */
    public static final class Builder {
        private final List<ParkingFloor> floors = new ArrayList<>();
        private final List<ParkingEventListener> listeners = new ArrayList<>();
        private SpotAssignmentStrategy assignmentStrategy = new NearestSpotAssignmentStrategy();
        private FeeStrategy feeStrategy = VehicleTypeHourlyFeeStrategy.withDefaults();
        private Clock clock = Clock.systemUTC();
        private Supplier<String> ticketIdGenerator = () -> UUID.randomUUID().toString();

        public Builder addFloor(ParkingFloor floor) {
            this.floors.add(Objects.requireNonNull(floor, "floor"));
            return this;
        }

        public Builder assignmentStrategy(SpotAssignmentStrategy strategy) {
            this.assignmentStrategy = Objects.requireNonNull(strategy, "assignmentStrategy");
            return this;
        }

        public Builder feeStrategy(FeeStrategy strategy) {
            this.feeStrategy = Objects.requireNonNull(strategy, "feeStrategy");
            return this;
        }

        public Builder clock(Clock clock) {
            this.clock = Objects.requireNonNull(clock, "clock");
            return this;
        }

        public Builder ticketIdGenerator(Supplier<String> generator) {
            this.ticketIdGenerator = Objects.requireNonNull(generator, "ticketIdGenerator");
            return this;
        }

        public Builder addListener(ParkingEventListener listener) {
            this.listeners.add(Objects.requireNonNull(listener, "listener"));
            return this;
        }

        public ParkingLot build() {
            if (floors.isEmpty()) {
                throw new IllegalStateException("A parking lot needs at least one floor");
            }
            return new ParkingLot(this);
        }
    }
}
