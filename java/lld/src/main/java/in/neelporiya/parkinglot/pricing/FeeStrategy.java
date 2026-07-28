package in.neelporiya.parkinglot.pricing;

import in.neelporiya.parkinglot.ParkingTicket;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * // DESIGN PATTERN: Strategy.
 *
 * <p>Encapsulates "how much does this stay cost?". Flat rate, hourly, per-vehicle, weekend surge,
 * or dynamic demand pricing all become interchangeable implementations.
 *
 * <p>// TESTABILITY: the exit {@link Instant} is a <em>parameter</em>, not read from a wall clock
 * inside the strategy. That is what lets a test compute a 3-hour fee in microseconds.
 */
public interface FeeStrategy {

    BigDecimal calculateFee(ParkingTicket ticket, Instant exitTime);
}
