package in.neelporiya.connectionpool;

import in.neelporiya.connectionpool.exception.PoolClosedException;
import in.neelporiya.connectionpool.exception.PoolExhaustedException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * A thread-safe, bounded, lazily-populated object pool.
 *
 * <p>See DESIGN.md for the full rationale. The core invariant is that the total number of live
 * resources (borrowed + idle) never exceeds {@code maxSize}, enforced by a {@link Semaphore}.
 *
 * @param <T> the pooled resource type (e.g. a DB connection)
 */
public class ConnectionPool<T> {

    /** Idle resource plus the time it was returned (for idle-timeout eviction). */
    private record Entry<T>(T resource, Instant returnedAt) {
    }

    private final ResourceFactory<T> factory;
    private final int maxSize;
    private final Duration borrowTimeout;
    private final Duration maxIdleTime; // null => no idle eviction
    private final Clock clock;

    private final Semaphore permits;
    private final BlockingQueue<Entry<T>> idle = new LinkedBlockingQueue<>();
    private volatile boolean closed;

    private ConnectionPool(Builder<T> builder) {
        this.factory = builder.factory;
        this.maxSize = builder.maxSize;
        this.borrowTimeout = builder.borrowTimeout;
        this.maxIdleTime = builder.maxIdleTime;
        this.clock = builder.clock;
        this.permits = new Semaphore(maxSize, true); // fair: FIFO among waiting borrowers
    }

    /**
     * Borrow a resource, blocking up to {@code borrowTimeout} if the pool is saturated.
     *
     * @throws PoolClosedException if the pool is shut down.
     * @throws PoolExhaustedException if no permit becomes available within the timeout.
     * @throws InterruptedException if the waiting thread is interrupted.
     */
    public PooledResource<T> borrow() throws InterruptedException {
        if (closed) {
            throw new PoolClosedException("pool is closed");
        }
        // CONCURRENCY: acquire a permit FIRST. This is what guarantees live <= maxSize.
        if (!permits.tryAcquire(borrowTimeout.toNanos(), TimeUnit.NANOSECONDS)) {
            throw new PoolExhaustedException("no resource available within " + borrowTimeout);
        }
        try {
            if (closed) {
                throw new PoolClosedException("pool is closed");
            }
            return new PooledResource<>(this, obtainUsableResource());
        } catch (RuntimeException e) {
            permits.release(); // give the permit back on any failure to obtain a resource
            throw e;
        }
    }

    /** Reuse a healthy idle resource, or create a fresh one. Permit is already held by the caller. */
    private T obtainUsableResource() {
        Entry<T> entry;
        while ((entry = idle.poll()) != null) {
            if (isStale(entry) || !factory.validate(entry.resource())) {
                factory.close(entry.resource()); // discard bad/stale resource, try the next
                continue;
            }
            return entry.resource();
        }
        // Idle queue empty -> create. Bounded because we hold a permit and idle is empty.
        return factory.create();
    }

    private boolean isStale(Entry<T> entry) {
        return maxIdleTime != null
                && Duration.between(entry.returnedAt(), clock.instant()).compareTo(maxIdleTime) > 0;
    }

    /** Package-private: called by {@link PooledResource#close()}. */
    void release(T resource) {
        if (closed) {
            factory.close(resource); // don't pool into a dead pool
        } else {
            idle.offer(new Entry<>(resource, clock.instant()));
        }
        permits.release();
    }

    /** Close idle resources now; borrowed ones are closed when returned. Rejects further borrows. */
    public void shutdown() {
        closed = true;
        Entry<T> entry;
        while ((entry = idle.poll()) != null) {
            factory.close(entry.resource());
        }
    }

    public int idleCount() {
        return idle.size();
    }

    public int borrowedCount() {
        return maxSize - permits.availablePermits();
    }

    public int maxSize() {
        return maxSize;
    }

    public boolean isClosed() {
        return closed;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static final class Builder<T> {
        private ResourceFactory<T> factory;
        private int maxSize = 10;
        private Duration borrowTimeout = Duration.ofSeconds(10);
        private Duration maxIdleTime; // null = disabled
        private Clock clock = Clock.systemUTC();

        public Builder<T> factory(ResourceFactory<T> factory) {
            this.factory = factory;
            return this;
        }

        public Builder<T> maxSize(int maxSize) {
            if (maxSize < 1) {
                throw new IllegalArgumentException("maxSize must be >= 1");
            }
            this.maxSize = maxSize;
            return this;
        }

        public Builder<T> borrowTimeout(Duration borrowTimeout) {
            this.borrowTimeout = Objects.requireNonNull(borrowTimeout, "borrowTimeout");
            return this;
        }

        public Builder<T> maxIdleTime(Duration maxIdleTime) {
            this.maxIdleTime = maxIdleTime;
            return this;
        }

        public Builder<T> clock(Clock clock) {
            this.clock = Objects.requireNonNull(clock, "clock");
            return this;
        }

        public ConnectionPool<T> build() {
            Objects.requireNonNull(factory, "factory");
            return new ConnectionPool<>(this);
        }
    }
}
