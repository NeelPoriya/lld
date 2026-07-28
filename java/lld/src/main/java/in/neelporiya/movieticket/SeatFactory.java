package in.neelporiya.movieticket;

import java.util.ArrayList;
import java.util.List;

/** // DESIGN PATTERN: Factory for repeatable seat-map creation in demos/tests. */
public class SeatFactory {

    public List<Seat> row(String rowLabel, int count, SeatType type, long priceCents) {
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            seats.add(new Seat(rowLabel + i, rowLabel, i, type, priceCents));
        }
        return seats;
    }
}
