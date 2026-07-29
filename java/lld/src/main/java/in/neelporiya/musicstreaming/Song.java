package in.neelporiya.musicstreaming;

import java.time.Duration;

/** Immutable catalogue entry for one track. */
public record Song(String id, String title, String artist, String album, Duration duration) {

    public Song {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
    }
}
