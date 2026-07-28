package in.neelporiya.movieticket;

import java.time.Instant;
import java.util.Objects;

public class Show implements Identifiable {

    private final String id;
    private final Movie movie;
    private final Cinema cinema;
    private final Screen screen;
    private final Instant startsAt;

    public Show(String id, Movie movie, Cinema cinema, Screen screen, Instant startsAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.movie = Objects.requireNonNull(movie, "movie");
        this.cinema = Objects.requireNonNull(cinema, "cinema");
        // EXTENSIBILITY: replace this with a DB-backed ShowSeatInventory aggregate at scale.
        this.screen = Objects.requireNonNull(screen, "screen").freshSeatInventoryCopy();
        this.startsAt = Objects.requireNonNull(startsAt, "startsAt");
    }

    @Override
    public String getId() {
        return id;
    }

    public Movie getMovie() {
        return movie;
    }

    public Cinema getCinema() {
        return cinema;
    }

    public Screen getScreen() {
        return screen;
    }

    public Instant getStartsAt() {
        return startsAt;
    }
}
