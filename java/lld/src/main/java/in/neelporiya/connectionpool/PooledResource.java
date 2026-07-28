package in.neelporiya.connectionpool;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * // DESIGN PATTERN: Handle / RAII. An {@link AutoCloseable} wrapper around a borrowed resource so
 * callers use {@code try (var h = pool.borrow()) { ... }} and the resource is returned to the pool
 * automatically — even if the body throws. {@code close()} is idempotent (returns exactly once).
 */
public final class PooledResource<T> implements AutoCloseable {

    private final ConnectionPool<T> pool;
    private final T resource;
    private final AtomicBoolean returned = new AtomicBoolean(false);

    PooledResource(ConnectionPool<T> pool, T resource) {
        this.pool = pool;
        this.resource = resource;
    }

    public T get() {
        if (returned.get()) {
            throw new IllegalStateException("resource already returned to the pool");
        }
        return resource;
    }

    @Override
    public void close() {
        // CONCURRENCY: compareAndSet makes double-close (or close from two threads) a safe no-op.
        if (returned.compareAndSet(false, true)) {
            pool.release(resource);
        }
    }
}
