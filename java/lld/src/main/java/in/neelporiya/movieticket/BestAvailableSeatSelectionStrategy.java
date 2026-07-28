package in.neelporiya.movieticket;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public class BestAvailableSeatSelectionStrategy implements SeatSelectionStrategy {

    @Override
    public List<Seat> selectSeats(Show show, int count, Instant now) {
        return show.getScreen().getSeats().stream()
                .filter(seat -> seat.getState(now) == SeatState.AVAILABLE)
                .sorted(Comparator.comparing(Seat::getType).reversed()
                        .thenComparing(Seat::getRowLabel)
                        .thenComparingInt(Seat::getNumber))
                .limit(count)
                .toList();
    }
}
