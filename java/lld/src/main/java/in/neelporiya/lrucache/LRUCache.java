package in.neelporiya.lrucache;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A thread-safe, O(1) LRU cache with optional per-entry TTL.
 *
 * <p><b>Data structures:</b> a {@link HashMap} for O(1) key → node lookup, plus an intrinsic
 * doubly-linked list (with sentinel {@code head}/{@code tail}) that orders nodes by recency —
 * most-recently-used just after {@code head}, least-recently-used just before {@code tail}. Both
 * "promote to MRU" and "evict LRU" are O(1) pointer splices.
 *
 * <p>// CONCURRENCY: a single {@link ReentrantLock} guards every operation because an LRU update is
 * a compound read-modify-write spanning the map and the list. Eviction callbacks are fired outside
 * the lock. See DESIGN.md for the lock-striping scaling note.
 *
 * <p>// TESTABILITY: expiry is computed from an injected {@link Clock}; tests advance a MutableClock
 * to expire entries instantly.
 */
public class LRUCache<K, V> implements Cache<K, V> {

    private static final class Node<K, V> {
        final K key;
        V value;
        Instant expiresAt; // null => never expires
        Node<K, V> prev;
        Node<K, V> next;

        Node(K key, V value, Instant expiresAt) {
            this.key = key;
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }

    private final int capacity;
    private final Clock clock;
    private final Duration defaultTtl; // null => entries don't expire by default
    private final Map<K, Node<K, V>> map;
    private final Node<K, V> head; // sentinel
    private final Node<K, V> tail; // sentinel
    private final ReentrantLock lock = new ReentrantLock();
    private final List<EvictionListener<K, V>> listeners = new CopyOnWriteArrayList<>();

    private LRUCache(Builder<K, V> builder) {
        if (builder.capacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1");
        }
        this.capacity = builder.capacity;
        this.clock = builder.clock;
        this.defaultTtl = builder.defaultTtl;
        this.map = new HashMap<>();
        this.head = new Node<>(null, null, null);
        this.tail = new Node<>(null, null, null);
        head.next = tail;
        tail.prev = head;
        listeners.addAll(builder.listeners);
    }

    @Override
    public V get(K key) {
        V result = null;
        Node<K, V> expired = null;
        lock.lock();
        try {
            Node<K, V> node = map.get(key);
            if (node != null) {
                if (isExpired(node, clock.instant())) {
                    unlink(node);
                    map.remove(key);
                    expired = node; // notify after unlock
                } else {
                    moveToFront(node); // mark as most-recently-used
                    result = node.value;
                }
            }
        } finally {
            lock.unlock();
        }
        if (expired != null) {
            fire(expired.key, expired.value, EvictionReason.EXPIRED);
        }
        return result;
    }

    @Override
    public void put(K key, V value) {
        putWithTtl(key, value, defaultTtl);
    }

    /**
     * Insert/update with an explicit TTL ({@code null} = never expires). Refreshes recency and
     * expiry for existing keys; evicts the LRU entry if capacity is exceeded.
     */
    public void putWithTtl(K key, V value, Duration ttl) {
        Objects.requireNonNull(key, "key");
        Node<K, V> capacityEvicted = null;
        lock.lock();
        try {
            Instant expiresAt = ttl == null ? null : clock.instant().plus(ttl);
            Node<K, V> existing = map.get(key);
            if (existing != null) {
                existing.value = value;
                existing.expiresAt = expiresAt;
                moveToFront(existing);
            } else {
                Node<K, V> node = new Node<>(key, value, expiresAt);
                map.put(key, node);
                addFront(node);
                if (map.size() > capacity) {
                    Node<K, V> lru = tail.prev; // least-recently-used real node
                    unlink(lru);
                    map.remove(lru.key);
                    capacityEvicted = lru;
                }
            }
        } finally {
            lock.unlock();
        }
        if (capacityEvicted != null) {
            fire(capacityEvicted.key, capacityEvicted.value, EvictionReason.CAPACITY);
        }
    }

    @Override
    public V remove(K key) {
        Node<K, V> removed = null;
        lock.lock();
        try {
            Node<K, V> node = map.remove(key);
            if (node != null) {
                unlink(node);
                removed = node;
            }
        } finally {
            lock.unlock();
        }
        if (removed != null) {
            fire(removed.key, removed.value, EvictionReason.EXPLICIT);
            return removed.value;
        }
        return null;
    }

    @Override
    public boolean containsKey(K key) {
        // Presence check that treats an expired entry as absent (and reclaims it), WITHOUT
        // promoting recency — matching the semantics of Map.containsKey.
        Node<K, V> expired = null;
        boolean present = false;
        lock.lock();
        try {
            Node<K, V> node = map.get(key);
            if (node != null) {
                if (isExpired(node, clock.instant())) {
                    unlink(node);
                    map.remove(key);
                    expired = node;
                } else {
                    present = true;
                }
            }
        } finally {
            lock.unlock();
        }
        if (expired != null) {
            fire(expired.key, expired.value, EvictionReason.EXPIRED);
        }
        return present;
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return map.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        List<Node<K, V>> dropped;
        lock.lock();
        try {
            dropped = new ArrayList<>(map.values());
            map.clear();
            head.next = tail;
            tail.prev = head;
        } finally {
            lock.unlock();
        }
        for (Node<K, V> node : dropped) {
            fire(node.key, node.value, EvictionReason.EXPLICIT);
        }
    }

    public void addEvictionListener(EvictionListener<K, V> listener) {
        listeners.add(listener);
    }

    // --- internals ---

    private boolean isExpired(Node<K, V> node, Instant now) {
        return node.expiresAt != null && !now.isBefore(node.expiresAt);
    }

    private void addFront(Node<K, V> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    private void unlink(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
        node.prev = null;
        node.next = null;
    }

    private void moveToFront(Node<K, V> node) {
        unlink(node);
        addFront(node);
    }

    private void fire(K key, V value, EvictionReason reason) {
        listeners.forEach(listener -> listener.onEvict(key, value, reason));
    }

    /**
     * // TESTABILITY: invariant check used by the concurrency test — the map and the linked list must
     * always describe the same set of entries. If locking were wrong, these would drift apart.
     */
    boolean isConsistent() {
        lock.lock();
        try {
            int listLength = 0;
            for (Node<K, V> n = head.next; n != tail; n = n.next) {
                listLength++;
                if (!map.containsKey(n.key)) {
                    return false;
                }
                if (listLength > map.size()) { // cycle / corruption guard
                    return false;
                }
            }
            return listLength == map.size();
        } finally {
            lock.unlock();
        }
    }

    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    /** // DESIGN PATTERN: Builder — capacity is required; clock/ttl/listeners are optional. */
    public static final class Builder<K, V> {
        private int capacity = 16;
        private Clock clock = Clock.systemUTC();
        private Duration defaultTtl;
        private final List<EvictionListener<K, V>> listeners = new ArrayList<>();

        public Builder<K, V> capacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public Builder<K, V> clock(Clock clock) {
            this.clock = Objects.requireNonNull(clock, "clock");
            return this;
        }

        public Builder<K, V> defaultTtl(Duration defaultTtl) {
            this.defaultTtl = defaultTtl;
            return this;
        }

        public Builder<K, V> evictionListener(EvictionListener<K, V> listener) {
            this.listeners.add(listener);
            return this;
        }

        public LRUCache<K, V> build() {
            return new LRUCache<>(this);
        }
    }
}
