package in.neelporiya.movieticket;

import java.time.Duration;
import java.util.Objects;

public class Movie implements Identifiable {

    private final String id;
    private final String title;
    private final String language;
    private final Duration duration;

    public Movie(String id, String title, String language, Duration duration) {
        this.id = Objects.requireNonNull(id, "id");
        this.title = Objects.requireNonNull(title, "title");
        this.language = Objects.requireNonNull(language, "language");
        this.duration = Objects.requireNonNull(duration, "duration");
    }

    @Override
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLanguage() {
        return language;
    }

    public Duration getDuration() {
        return duration;
    }
}
