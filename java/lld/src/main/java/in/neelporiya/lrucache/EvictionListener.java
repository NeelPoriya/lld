package in.neelporiya.lrucache;

/**
 * // DESIGN PATTERN: Observer. Notified whenever an entry leaves the cache. Useful for write-back
 * caches (flush to DB on eviction), metrics, or logging. Invoked <em>after</em> the cache lock is
 * released, so a listener may safely call back into the cache.
 */
@FunctionalInterface
public interface EvictionListener<K, V> {
    void onEvict(K key, V value, EvictionReason reason);
}
