package in.neelporiya.lrucache;

import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LRUCacheTest {

    private final MutableClock clock = MutableClock.atEpoch();

    private <K, V> LRUCache<K, V> cache(int capacity) {
        return LRUCache.<K, V>builder().capacity(capacity).clock(clock).build();
    }

    @Test
    void basicPutGet() {
        LRUCache<String, Integer> c = cache(2);
        c.put("a", 1);
        assertEquals(1, c.get("a"));
        assertNull(c.get("missing"));
        assertEquals(1, c.size());
    }

    @Test
    void evictsLeastRecentlyUsedOnOverflow() {
        LRUCache<String, Integer> c = cache(2);
        c.put("a", 1);
        c.put("b", 2);
        c.put("c", 3); // over capacity -> evicts "a" (LRU)

        assertNull(c.get("a"));
        assertEquals(2, c.get("b"));
        assertEquals(3, c.get("c"));
        assertEquals(2, c.size());
    }

    @Test
    void getPromotesRecencySoADifferentKeyIsEvicted() {
        LRUCache<String, Integer> c = cache(2);
        c.put("a", 1);
        c.put("b", 2);
        c.get("a");     // "a" becomes most-recently-used, so "b" is now the LRU
        c.put("c", 3);  // evicts "b", not "a"

        assertEquals(1, c.get("a"));
        assertNull(c.get("b"));
        assertEquals(3, c.get("c"));
    }

    @Test
    void updatingExistingKeyKeepsSizeAndRefreshesValue() {
        LRUCache<String, Integer> c = cache(2);
        c.put("a", 1);
        c.put("a", 99);
        assertEquals(99, c.get("a"));
        assertEquals(1, c.size());
    }

    @Test
    void removeAndClear() {
        LRUCache<String, Integer> c = cache(3);
        c.put("a", 1);
        c.put("b", 2);
        assertEquals(1, c.remove("a"));
        assertNull(c.remove("a"));
        assertFalse(c.containsKey("a"));
        c.clear();
        assertEquals(0, c.size());
    }

    @Test
    void evictionListenerReceivesCapacityReasonAndEvictedKey() {
        List<String> evictions = new ArrayList<>();
        LRUCache<String, Integer> c = LRUCache.<String, Integer>builder()
                .capacity(1)
                .clock(clock)
                .evictionListener((k, v, reason) -> evictions.add(k + ":" + reason))
                .build();

        c.put("a", 1);
        c.put("b", 2); // evicts "a"

        assertEquals(List.of("a:CAPACITY"), evictions);
    }

    @Test
    void capacityMustBePositive() {
        assertThrows(IllegalArgumentException.class,
                () -> LRUCache.builder().capacity(0).build());
    }

    // ---- TTL: the injected-clock showcase ----

    @Test
    void entryExpiresAfterTtlUsingInjectedClock() {
        LRUCache<String, Integer> c = cache(10);
        c.putWithTtl("token", 42, Duration.ofMinutes(5));

        clock.advance(Duration.ofMinutes(4));
        assertEquals(42, c.get("token"), "still alive at 4 minutes");

        clock.advance(Duration.ofMinutes(2)); // total 6 minutes > 5
        assertNull(c.get("token"), "expired after 6 minutes");
        assertEquals(0, c.size(), "expired entry reclaimed on access");
    }

    @Test
    void defaultTtlAppliesToPlainPut() {
        LRUCache<String, Integer> c = LRUCache.<String, Integer>builder()
                .capacity(10)
                .clock(clock)
                .defaultTtl(Duration.ofSeconds(30))
                .build();
        c.put("k", 7);

        clock.advance(Duration.ofSeconds(31));
        assertNull(c.get("k"));
    }

    @Test
    void expiryFiresExpiredEvictionReason() {
        List<String> evictions = new ArrayList<>();
        LRUCache<String, Integer> c = LRUCache.<String, Integer>builder()
                .capacity(10)
                .clock(clock)
                .evictionListener((k, v, reason) -> evictions.add(k + ":" + reason))
                .build();
        c.putWithTtl("k", 1, Duration.ofSeconds(10));

        clock.advance(Duration.ofSeconds(11));
        c.get("k"); // triggers lazy expiry

        assertEquals(List.of("k:EXPIRED"), evictions);
    }

    @Test
    void containsKeyTreatsExpiredAsAbsentWithoutPromoting() {
        LRUCache<String, Integer> c = cache(10);
        c.putWithTtl("k", 1, Duration.ofSeconds(10));
        assertTrue(c.containsKey("k"));

        clock.advance(Duration.ofSeconds(11));
        assertFalse(c.containsKey("k"));
        assertEquals(0, c.size());
    }
}
