package in.neelporiya.phases.phase12concurrency;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CacheWithStats {
    static class Cache<K, V> {
        Map<K, V> cache = new HashMap<>();

        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
        Lock readLock = rwLock.readLock();
        Lock writeLock = rwLock.writeLock();

        AtomicInteger hits = new AtomicInteger(0);
        AtomicInteger misses = new AtomicInteger(0);

        public void put(K key, V value) {
            writeLock.lock();
            try {
                cache.put(key, value);
            } finally {
                writeLock.unlock();
            }
        }

        public V get(K key) {
            readLock.lock();
            try {
                V val = cache.get(key);
                if (val != null) {
                    hits.incrementAndGet();
                } else {
                    misses.incrementAndGet();
                }
                return val;
            } finally {
                readLock.unlock();
            }
        }

        public void clear() {
            writeLock.lock();

            try {
                cache.clear();
                misses.set(0);
                hits.set(0);
            } finally {
                writeLock.unlock();
            }
        }

        public double getHitRatio() {
            readLock.lock();

            try {
                if (hits.get() + misses.get() == 0) return 0.0;
                return (double) hits.get() / (hits.get() + misses.get());
            } finally {
                readLock.unlock();
            }
        }
    }

    static void main() {
        Cache<Integer, String> mp = new Cache<>();
        mp.put(1, "Hello");
        mp.get(1);
        mp.put(2, "Okay");
        mp.put(3, "Hmmm...");

        mp.get(2);
        mp.get(4);

        System.out.println(mp.getHitRatio());
    }
}
