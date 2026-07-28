package in.neelporiya.concertbooking;

import java.time.Instant;
import java.util.List;

/**
 * // DESIGN PATTERN: Strategy. Users may pick exact seats, while "best available" can be automated.
 */
public interface SeatSelectionStrategy {
    List<Seat> selectSeats(Concert concert, int count, Instant now);
}
