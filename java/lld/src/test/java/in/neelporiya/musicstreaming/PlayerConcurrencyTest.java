package in.neelporiya.musicstreaming;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // CONCURRENCY: a shared player is driven by many threads calling {@code next()} at once. With
 * repeat-all (which never stops), the cursor must always land on a valid, in-queue track and no
 * control may throw.
 */
class PlayerConcurrencyTest {

    @Test
    void concurrentSkipsKeepTheCursorValid() throws InterruptedException {
        List<Song> queue = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            queue.add(new Song(String.valueOf(i), "T" + i, "Artist", "Album", Duration.ofMinutes(3)));
        }
        Player player = new Player(new RepeatAllPlayback());
        player.load(queue);
        player.play();

        int threads = 32;
        int skipsPerThread = 200;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        for (int t = 0; t < threads; t++) {
            pool.execute(() -> {
                try {
                    go.await();
                    for (int i = 0; i < skipsPerThread; i++) {
                        player.next();
                    }
                } catch (Throwable error) {
                    failure.compareAndSet(null, error);
                } finally {
                    done.countDown();
                }
            });
        }

        go.countDown();
        assertTrue(done.await(15, TimeUnit.SECONDS));
        pool.shutdownNow();

        assertNull(failure.get(), () -> "no control should throw: " + failure.get());
        assertEquals(PlaybackStateType.PLAYING, player.stateType(), "repeat-all never stops");
        assertNotNull(player.currentSong(), "cursor is always on a valid track");
        assertTrue(queue.contains(player.currentSong()));
    }
}
