package in.neelporiya.lrucache;

/**
 * A small cache abstraction. Callers depend on this interface, so an LFU/FIFO/other policy could be
 * substituted for {@link LRUCache} without changing client code.
 */
public interface Cache<K, V> {

    /** @return the value, or {@code null} if absent or expired. */
    V get(K key);

    void put(K key, V value);

    /** @return the removed value, or {@code null} if it was absent. */
    V remove(K key);

    boolean containsKey(K key);

    int size();

    void clear();
}
