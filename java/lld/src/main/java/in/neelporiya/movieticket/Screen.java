package in.neelporiya.movieticket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Screen implements Identifiable {

    private final String id;
    private final String name;
    private final Map<String, Seat> seats;

    private Screen(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.name = Objects.requireNonNull(builder.name, "name");
        this.seats = Collections.unmodifiableMap(new LinkedHashMap<>(builder.seats));
    }

    public static Builder builder(String id, String name) {
        return new Builder(id, name);
    }

    public Screen freshSeatInventoryCopy() {
        Builder builder = builder(id, name);
        seats.values().stream().map(Seat::freshCopy).forEach(builder::addSeat);
        return builder.build();
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Seat> getSeats() {
        return List.copyOf(seats.values());
    }

    public Seat getSeat(String seatId) {
        Seat seat = seats.get(seatId);
        if (seat == null) {
            throw new IllegalArgumentException("unknown seat " + seatId);
        }
        return seat;
    }

    /** // DESIGN PATTERN: Builder keeps seat-map setup readable while Screen remains immutable. */
    public static class Builder {
        private final String id;
        private final String name;
        private final Map<String, Seat> seats = new LinkedHashMap<>();

        private Builder(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public Builder addSeat(Seat seat) {
            seats.put(seat.getId(), seat);
            return this;
        }

        public Builder addSeats(List<Seat> seatsToAdd) {
            new ArrayList<>(seatsToAdd).forEach(this::addSeat);
            return this;
        }

        public Screen build() {
            return new Screen(this);
        }
    }
}
