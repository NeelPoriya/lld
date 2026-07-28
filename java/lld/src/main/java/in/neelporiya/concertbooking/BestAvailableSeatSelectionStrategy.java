package in.neelporiya.concertbooking;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public class BestAvailableSeatSelectionStrategy implements SeatSelectionStrategy {

    @Override
    public List<Seat> selectSeats(Concert concert, int count, Instant now) {
        return concert.getVenue().getSeats().stream()
                .filter(seat -> seat.getState(now) == SeatState.AVAILABLE)
                .sorted(Comparator.comparing(Seat::getSectionId)
                        .thenComparing(Seat::getRowLabel)
                        .thenComparingInt(Seat::getNumber))
                .limit(count)
                .toList();
    }
}
