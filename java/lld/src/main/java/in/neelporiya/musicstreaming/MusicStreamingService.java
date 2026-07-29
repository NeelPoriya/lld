package in.neelporiya.musicstreaming;

import in.neelporiya.musicstreaming.exception.PlaylistNotFoundException;
import in.neelporiya.musicstreaming.exception.SongNotFoundException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * // DESIGN PATTERN: Facade — one API over the song catalogue, playlists, search and player creation.
 *
 * <p>The service owns the shared catalogue/playlists; a {@link Player} is a per-session object you
 * mint from it with the {@link PlaybackStrategy} you want.
 */
public class MusicStreamingService {

    private final Map<String, Song> songsById = new ConcurrentHashMap<>();
    private final List<Song> songs = new CopyOnWriteArrayList<>(); // insertion order for stable search
    private final Map<String, Playlist> playlists = new ConcurrentHashMap<>();
    private final Supplier<String> idGenerator;

    public MusicStreamingService() {
        this(() -> UUID.randomUUID().toString());
    }

    public MusicStreamingService(Supplier<String> idGenerator) {
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }

    public Song addSong(String title, String artist, String album, Duration duration) {
        Song song = new Song(idGenerator.get(), title, artist, album, duration);
        songsById.put(song.id(), song);
        songs.add(song);
        return song;
    }

    public Playlist createPlaylist(String owner, String name) {
        Playlist playlist = new Playlist(idGenerator.get(), name, owner);
        playlists.put(playlist.id(), playlist);
        return playlist;
    }

    public void addToPlaylist(String playlistId, String songId) {
        Playlist playlist = requirePlaylist(playlistId);
        Song song = requireSong(songId);
        playlist.add(song);
    }

    public boolean removeFromPlaylist(String playlistId, String songId) {
        return requirePlaylist(playlistId).remove(songId);
    }

    /**
     * Case-insensitive substring search over title, artist and album, in catalogue order.
     *
     * <p>// EXTENSIBILITY: a real system swaps this linear scan for an inverted index (see the
     * Facebook Search problem) behind the same method signature.
     */
    public List<Song> search(String text) {
        String needle = text.toLowerCase(Locale.ROOT);
        List<Song> hits = new ArrayList<>();
        for (Song song : songs) {
            if (matches(song.title(), needle) || matches(song.artist(), needle) || matches(song.album(), needle)) {
                hits.add(song);
            }
        }
        return hits;
    }

    /** Mint a fresh playback session using the given ordering strategy. */
    public Player createPlayer(PlaybackStrategy strategy) {
        return new Player(strategy);
    }

    public Song getSong(String songId) {
        return songsById.get(songId);
    }

    public Playlist getPlaylist(String playlistId) {
        return playlists.get(playlistId);
    }

    private static boolean matches(String field, String needle) {
        return field != null && field.toLowerCase(Locale.ROOT).contains(needle);
    }

    private Playlist requirePlaylist(String playlistId) {
        Playlist playlist = playlists.get(playlistId);
        if (playlist == null) {
            throw new PlaylistNotFoundException("no playlist " + playlistId);
        }
        return playlist;
    }

    private Song requireSong(String songId) {
        Song song = songsById.get(songId);
        if (song == null) {
            throw new SongNotFoundException("no song " + songId);
        }
        return song;
    }
}
