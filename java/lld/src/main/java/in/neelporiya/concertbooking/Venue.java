package in.neelporiya.concertbooking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Venue implements Identifiable {

    private final String id;
    private final String name;
    private final Map<String, Section> sections;
    private final Map<String, Seat> seats;

    private Venue(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.name = Objects.requireNonNull(builder.name, "name");
        this.sections = Collections.unmodifiableMap(new LinkedHashMap<>(builder.sections));
        this.seats = Collections.unmodifiableMap(new LinkedHashMap<>(builder.seats));
    }

    public static Builder builder(String id, String name) {
        return new Builder(id, name);
    }

    public Venue freshSeatInventoryCopy() {
        Builder builder = builder(id, name);
        sections.values().forEach(builder::addSection);
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

    public List<Section> getSections() {
        return List.copyOf(sections.values());
    }

    public List<Seat> getSeats() {
        return List.copyOf(seats.values());
    }

    public Section getSection(String sectionId) {
        Section section = sections.get(sectionId);
        if (section == null) {
            throw new IllegalArgumentException("unknown section " + sectionId);
        }
        return section;
    }

    public Seat getSeat(String seatId) {
        Seat seat = seats.get(seatId);
        if (seat == null) {
            throw new IllegalArgumentException("unknown seat " + seatId);
        }
        return seat;
    }

    /**
     * // DESIGN PATTERN: Builder keeps test fixture venues readable while preserving immutable maps.
     */
    public static class Builder {
        private final String id;
        private final String name;
        private final Map<String, Section> sections = new LinkedHashMap<>();
        private final Map<String, Seat> seats = new LinkedHashMap<>();

        private Builder(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public Builder addSection(Section section) {
            sections.put(section.getId(), section);
            return this;
        }

        public Builder addSeat(Seat seat) {
            if (!sections.containsKey(seat.getSectionId())) {
                throw new IllegalArgumentException("add section before seat " + seat.getId());
            }
            seats.put(seat.getId(), seat);
            return this;
        }

        public Builder addSeats(List<Seat> seatsToAdd) {
            new ArrayList<>(seatsToAdd).forEach(this::addSeat);
            return this;
        }

        public Venue build() {
            return new Venue(this);
        }
    }
}
