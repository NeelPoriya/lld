package in.neelporiya.parkinglot;

import in.neelporiya.parkinglot.spot.ParkingSpot;
import in.neelporiya.parkinglot.vehicle.Vehicle;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * The contract between the lot and the driver: it pins down which spot the vehicle holds and when
 * it entered. The exit time and fee are filled in at {@link #close}.
 *
 * <p>// INTERVIEW INSIGHT: money is {@link BigDecimal}, never {@code double}. Floating point can't
 * represent 0.10 exactly, so currency math with {@code double} silently drifts. This is a favourite
 * interviewer trap.
 *
 * <p>// CONCURRENCY: the mutable fields are {@code volatile} and are written exactly once by the
 * single thread that owns the un-park (see {@code ParkingLot.unpark}, which claims ownership via an
 * atomic map remove before touching the ticket). {@code volatile} guarantees the reader thread sees
 * the final values.
 */
public class ParkingTicket {

    private final String id;
    private final Vehicle vehicle;
    private final ParkingSpot spot;
    private final Instant entryTime;

    private volatile Instant exitTime;
    private volatile BigDecimal fee;
    private volatile TicketStatus status;

    public ParkingTicket(String id, Vehicle vehicle, ParkingSpot spot, Instant entryTime) {
        this.id = Objects.requireNonNull(id, "id");
        this.vehicle = Objects.requireNonNull(vehicle, "vehicle");
        this.spot = Objects.requireNonNull(spot, "spot");
        this.entryTime = Objects.requireNonNull(entryTime, "entryTime");
        this.status = TicketStatus.ACTIVE;
    }

    void close(Instant exitTime, BigDecimal fee) {
        this.exitTime = exitTime;
        this.fee = fee;
        this.status = TicketStatus.PAID;
    }

    public String getId() {
        return id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSpot getSpot() {
        return spot;
    }

    public Instant getEntryTime() {
        return entryTime;
    }

    public Instant getExitTime() {
        return exitTime;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public TicketStatus getStatus() {
        return status;
    }
}
