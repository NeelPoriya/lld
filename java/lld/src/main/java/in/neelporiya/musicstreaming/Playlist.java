package in.neelporiya.musicstreaming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An ordered, mutable collection of songs owned by a user.
 *
 * <p>// CONCURRENCY: guarded by an intrinsic lock so edits (add/remove) and snapshot reads
 * ({@link #songs()}) are consistent when a playlist is shared across sessions.
 */
public class Playlist {

    private final String id;
    private final String name;
    private final String owner;
    private final List<Song> songs = new ArrayList<>();

    public Playlist(String id, String name, String owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
    }

    public synchronized void add(Song song) {
        songs.add(song);
    }

    public synchronized boolean remove(String songId) {
        return songs.removeIf(s -> s.id().equals(songId));
    }

    /** A defensive snapshot — safe to iterate while the playlist is edited elsewhere. */
    public synchronized List<Song> songs() {
        return Collections.unmodifiableList(new ArrayList<>(songs));
    }

    public synchronized int size() {
        return songs.size();
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String owner() {
        return owner;
    }
}
