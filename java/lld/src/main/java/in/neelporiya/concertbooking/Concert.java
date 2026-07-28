package in.neelporiya.concertbooking;

import java.time.Instant;
import java.util.Objects;

public class Concert implements Identifiable {

    private final String id;
    private final String artist;
    private final Instant startsAt;
    private final Venue venue;

    public Concert(String id, String artist, Instant startsAt, Venue venue) {
        this.id = Objects.requireNonNull(id, "id");
        this.artist = Objects.requireNonNull(artist, "artist");
        this.startsAt = Objects.requireNonNull(startsAt, "startsAt");
        // EXTENSIBILITY: each event gets its own seat inventory over the same venue layout. A future
        // implementation could replace this with a ConcertSeatInventory aggregate backed by a DB.
        this.venue = Objects.requireNonNull(venue, "venue").freshSeatInventoryCopy();
    }

    @Override
    public String getId() {
        return id;
    }

    public String getArtist() {
        return artist;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Venue getVenue() {
        return venue;
    }
}
