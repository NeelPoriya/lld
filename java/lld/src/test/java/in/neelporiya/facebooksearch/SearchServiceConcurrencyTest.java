package in.neelporiya.facebooksearch;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // CONCURRENCY: writers index distinct documents while readers search at the same time. The
 * read/write lock must keep both indexes consistent — after the dust settles, every indexed document
 * is findable and no reader saw a corrupt state.
 */
class SearchServiceConcurrencyTest {

    @Test
    void concurrentIndexingAndSearchingStaysConsistent() throws InterruptedException {
        SearchService search = new SearchService();
        int docCount = 500;

        ExecutorService pool = Executors.newFixedThreadPool(16);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(docCount);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        for (int i = 0; i < docCount; i++) {
            int n = i;
            pool.execute(() -> {
                try {
                    go.await();
                    // Every doc shares the word "common" and has a unique word "uN".
                    search.index(new SearchDocument("d" + n, "common u" + n, EntityType.POST, n, Instant.EPOCH));
                    // Interleave reads to exercise the read lock while writes happen.
                    search.search("common", 10);
                    search.typeahead("comm", 5);
                } catch (Throwable error) {
                    failure.compareAndSet(null, error);
                } finally {
                    done.countDown();
                }
            });
        }

        go.countDown();
        assertTrue(done.await(20, TimeUnit.SECONDS));
        pool.shutdownNow();

        assertNull(failure.get(), () -> "no reader/writer should fail: " + failure.get());
        assertEquals(docCount, search.size());
        // Every shared-word doc is found (limit large enough to hold them all).
        assertEquals(docCount, search.search("common", docCount + 1).size());
        // A few unique lookups resolve to exactly their document.
        assertEquals(List.of("d0"), search.search("u0", 10).stream().map(SearchDocument::id).toList());
        assertEquals(List.of("d499"), search.search("u499", 10).stream().map(SearchDocument::id).toList());
    }
}
