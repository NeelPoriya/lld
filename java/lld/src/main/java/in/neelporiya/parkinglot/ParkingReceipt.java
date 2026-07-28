package in.neelporiya.parkinglot;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable value object returned on exit. Using a {@code record} gives us a correct
 * {@code equals}/{@code hashCode}/{@code toString} for free — ideal for a pure data carrier.
 */
public record ParkingReceipt(
        String ticketId,
        String licensePlate,
        String spotId,
        Instant entryTime,
        Instant exitTime,
        BigDecimal fee) {
}
