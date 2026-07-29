package in.neelporiya.musicstreaming;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerTest {

    private static Song song(String id, String title) {
        return new Song(id, title, "Artist", "Album", Duration.ofMinutes(3));
    }

    private static final class Recorder implements PlaybackListener {
        final List<String> started = new ArrayList<>();
        int paused;
        int resumed;
        int stopped;

        @Override
        public void onSongStarted(Song s) {
            started.add(s.title());
        }

        @Override
        public void onPaused(Song s) {
            paused++;
        }

        @Override
        public void onResumed(Song s) {
            resumed++;
        }

        @Override
        public void onStopped() {
            stopped++;
        }
    }

    private final Song a = song("1", "A");
    private final Song b = song("2", "B");
    private final Song c = song("3", "C");

    @Test
    void playStartsFirstSongAndNotifies() {
        Player player = new Player(new SequentialPlayback());
        Recorder rec = new Recorder();
        player.addListener(rec);
        player.load(List.of(a, b, c));

        player.play();

        assertEquals(PlaybackStateType.PLAYING, player.stateType());
        assertEquals(a, player.currentSong());
        assertEquals(List.of("A"), rec.started);
    }

    @Test
    void pauseThenResumeTogglesStateAndNotifies() {
        Player player = new Player(new SequentialPlayback());
        Recorder rec = new Recorder();
        player.addListener(rec);
        player.load(List.of(a));
        player.play();

        player.pause();
        assertEquals(PlaybackStateType.PAUSED, player.stateType());
        player.play(); // resume
        assertEquals(PlaybackStateType.PLAYING, player.stateType());

        assertEquals(1, rec.paused);
        assertEquals(1, rec.resumed);
    }

    @Test
    void pausingAStoppedPlayerIsIllegal() {
        Player player = new Player(new SequentialPlayback());
        player.load(List.of(a));
        assertThrows(IllegalStateException.class, player::pause);
    }

    @Test
    void skippingWhileStoppedIsIllegal() {
        Player player = new Player(new SequentialPlayback());
        player.load(List.of(a));
        assertThrows(IllegalStateException.class, player::next);
    }

    @Test
    void playingWithNothingLoadedIsIllegal() {
        Player player = new Player(new SequentialPlayback());
        assertThrows(IllegalStateException.class, player::play);
    }

    @Test
    void sequentialAdvancesThenStopsAtEnd() {
        Player player = new Player(new SequentialPlayback());
        Recorder rec = new Recorder();
        player.addListener(rec);
        player.load(List.of(a, b));
        player.play(); // A
        player.next(); // B
        player.next(); // past end -> stop

        assertEquals(List.of("A", "B"), rec.started);
        assertEquals(1, rec.stopped);
        assertEquals(PlaybackStateType.STOPPED, player.stateType());
    }

    @Test
    void previousReturnsToPriorTrackViaHistory() {
        Player player = new Player(new SequentialPlayback());
        player.load(List.of(a, b, c));
        player.play(); // A
        player.next(); // B
        player.next(); // C
        assertEquals(c, player.currentSong());

        player.previous(); // back to B
        assertEquals(b, player.currentSong());
        player.previous(); // back to A
        assertEquals(a, player.currentSong());
    }

    @Test
    void repeatOneStaysOnTheSameTrack() {
        Player player = new Player(new RepeatOnePlayback());
        player.load(List.of(a, b));
        player.play();
        player.next();
        player.next();
        assertEquals(a, player.currentSong());
        assertEquals(PlaybackStateType.PLAYING, player.stateType());
    }

    @Test
    void repeatAllWrapsAroundInsteadOfStopping() {
        Player player = new Player(new RepeatAllPlayback());
        player.load(List.of(a, b));
        player.play(); // A (0)
        player.next(); // B (1)
        player.next(); // wraps to A (0)
        assertEquals(a, player.currentSong());
        assertEquals(PlaybackStateType.PLAYING, player.stateType());
    }

    @Test
    void shuffleIsReproducibleForAFixedSeed() {
        List<String> first = playThroughShuffle(new Random(42));
        List<String> second = playThroughShuffle(new Random(42));
        assertEquals(first, second, "same seed -> identical shuffle order");
    }

    private List<String> playThroughShuffle(Random random) {
        Player player = new Player(new ShufflePlayback(random));
        Recorder rec = new Recorder();
        player.addListener(rec);
        player.load(List.of(a, b, c));
        player.play();
        for (int i = 0; i < 6; i++) {
            player.next();
        }
        return rec.started;
    }

    @Test
    void stopFromPausedReturnsToStopped() {
        Player player = new Player(new SequentialPlayback());
        player.load(List.of(a, b));
        player.play();
        player.pause();
        player.stop();
        assertEquals(PlaybackStateType.STOPPED, player.stateType());
        // After stop, skipping is illegal again.
        assertThrows(IllegalStateException.class, player::next);
    }

    @Test
    void currentSongIsNullBeforeAnythingLoads() {
        Player player = new Player(new SequentialPlayback());
        assertNull(player.currentSong());
    }
}
