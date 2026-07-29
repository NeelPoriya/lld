package in.neelporiya.musicstreaming;

import in.neelporiya.musicstreaming.exception.PlaylistNotFoundException;
import in.neelporiya.musicstreaming.exception.SongNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MusicStreamingServiceTest {

    private MusicStreamingService service;
    private Song bohemian;
    private Song imagine;
    private Song yesterday;

    @BeforeEach
    void setUp() {
        AtomicInteger seq = new AtomicInteger();
        service = new MusicStreamingService(() -> "id" + seq.incrementAndGet());
        bohemian = service.addSong("Bohemian Rhapsody", "Queen", "A Night at the Opera", Duration.ofMinutes(6));
        imagine = service.addSong("Imagine", "John Lennon", "Imagine", Duration.ofMinutes(3));
        yesterday = service.addSong("Yesterday", "The Beatles", "Help!", Duration.ofMinutes(2));
    }

    private List<String> titles(List<Song> songs) {
        return songs.stream().map(Song::title).collect(Collectors.toList());
    }

    @Test
    void searchMatchesTitleCaseInsensitively() {
        assertEquals(List.of("Imagine"), titles(service.search("imag")));
    }

    @Test
    void searchMatchesArtist() {
        assertEquals(List.of("Bohemian Rhapsody"), titles(service.search("queen")));
    }

    @Test
    void searchMatchesAlbum() {
        assertEquals(List.of("Yesterday"), titles(service.search("help")));
    }

    @Test
    void searchReturnsHitsInCatalogueOrder() {
        // "a" appears in every title above; result must preserve insertion order.
        assertEquals(List.of("Bohemian Rhapsody", "Imagine", "Yesterday"), titles(service.search("a")));
    }

    @Test
    void playlistAddPreservesOrder() {
        Playlist playlist = service.createPlaylist("alice", "Faves");
        service.addToPlaylist(playlist.id(), yesterday.id());
        service.addToPlaylist(playlist.id(), bohemian.id());

        assertEquals(List.of("Yesterday", "Bohemian Rhapsody"), titles(playlist.songs()));
    }

    @Test
    void removeFromPlaylist() {
        Playlist playlist = service.createPlaylist("alice", "Faves");
        service.addToPlaylist(playlist.id(), imagine.id());
        assertTrue(service.removeFromPlaylist(playlist.id(), imagine.id()));
        assertFalse(service.removeFromPlaylist(playlist.id(), imagine.id()));
        assertEquals(0, playlist.size());
    }

    @Test
    void addingToUnknownPlaylistOrSongThrows() {
        assertThrows(PlaylistNotFoundException.class, () -> service.addToPlaylist("ghost", bohemian.id()));
        Playlist playlist = service.createPlaylist("alice", "Faves");
        assertThrows(SongNotFoundException.class, () -> service.addToPlaylist(playlist.id(), "ghost"));
    }

    @Test
    void createdPlayerCanPlayAPlaylist() {
        Playlist playlist = service.createPlaylist("alice", "Faves");
        service.addToPlaylist(playlist.id(), bohemian.id());
        service.addToPlaylist(playlist.id(), imagine.id());

        Player player = service.createPlayer(new SequentialPlayback());
        player.load(playlist.songs());
        player.play();
        assertEquals(bohemian, player.currentSong());
        player.next();
        assertEquals(imagine, player.currentSong());
    }
}
