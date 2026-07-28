package in.neelporiya.movieticket;

import java.time.Instant;
import java.util.List;

/** // DESIGN PATTERN: Strategy. Exact-seat picking and best-available evolve independently. */
public interface SeatSelectionStrategy {
    List<Seat> selectSeats(Show show, int count, Instant now);
}
