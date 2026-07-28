package in.neelporiya.connectionpool;

import in.neelporiya.connectionpool.exception.PoolClosedException;
import in.neelporiya.connectionpool.exception.PoolExhaustedException;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionPoolTest {

    private ConnectionPool<FakeConnection> pool(FakeConnectionFactory factory, int max) {
        return ConnectionPool.<FakeConnection>builder()
                .factory(factory)
                .maxSize(max)
                .borrowTimeout(Duration.ofMillis(200))
                .build();
    }

    @Test
    void returnedResourceIsReused() throws InterruptedException {
        FakeConnectionFactory factory = new FakeConnectionFactory();
        ConnectionPool<FakeConnection> pool = pool(factory, 2);

        PooledResource<FakeConnection> h1 = pool.borrow();
        FakeConnection first = h1.get();
        h1.close();

        PooledResource<FakeConnection> h2 = pool.borrow();
        assertSame(first, h2.get(), "an idle resource must be reused, not recreated");
        assertEquals(1, factory.createdCount.get());
        h2.close();
    }

    @Test
    void createsLazilyUpToMaxSize() throws InterruptedException {
        FakeConnectionFactory factory = new FakeConnectionFactory();
        ConnectionPool<FakeConnection> pool = pool(factory, 3);

        PooledResource<FakeConnection> a = pool.borrow();
        PooledResource<FakeConnection> b = pool.borrow();
        PooledResource<FakeConnection> c = pool.borrow();

        assertEquals(3, factory.createdCount.get());
        assertEquals(3, pool.borrowedCount());
        assertEquals(0, pool.idleCount());
        a.close();
        b.close();
        c.close();
    }

    @Test
    void borrowTimesOutWhenExhausted() throws InterruptedException {
        FakeConnectionFactory factory = new FakeConnectionFactory();
        ConnectionPool<FakeConnection> pool = pool(factory, 1);

        PooledResource<FakeConnection> held = pool.borrow(); // takes the only permit
        assertThrows(PoolExhaustedException.class, pool::borrow);

        held.close(); // releasing frees the permit
        PooledResource<FakeConnection> next = pool.borrow();
        assertNotSame(null, next.get());
        next.close();
    }

    @Test
    void invalidResourceIsDiscardedAndReplaced() throws InterruptedException {
        FakeConnectionFactory factory = new FakeConnectionFactory();
        ConnectionPool<FakeConnection> pool = pool(factory, 1);

        PooledResource<FakeConnection> h1 = pool.borrow();
        FakeConnection bad = h1.get();
        h1.close();          // back to idle
        bad.valid = false;   // it has gone stale/broken

        PooledResource<FakeConnection> h2 = pool.borrow();
        assertNotSame(bad, h2.get(), "a resource failing validation must be replaced");
        assertTrue(bad.closed, "the invalid resource must be closed");
        assertTrue(h2.get().valid);
        h2.close();
    }

    @Test
    void idleResourceIsEvictedAfterMaxIdleTime() throws InterruptedException {
        MutableClock clock = MutableClock.atEpoch();
        FakeConnectionFactory factory = new FakeConnectionFactory();
        ConnectionPool<FakeConnection> pool = ConnectionPool.<FakeConnection>builder()
                .factory(factory)
                .maxSize(1)
                .maxIdleTime(Duration.ofMinutes(5))
                .clock(clock)
                .build();

        PooledResource<FakeConnection> h1 = pool.borrow();
        FakeConnection first = h1.get();
        h1.close(); // returned at t=0

        clock.advance(Duration.ofMinutes(6)); // now older than maxIdleTime

        PooledResource<FakeConnection> h2 = pool.borrow();
        assertNotSame(first, h2.get(), "stale idle resource must be evicted");
        assertTrue(first.closed, "evicted resource must be closed");
        h2.close();
    }

    @Test
    void tryWithResourcesReturnsToPool() throws InterruptedException {
        FakeConnectionFactory factory = new FakeConnectionFactory();
        ConnectionPool<FakeConnection> pool = pool(factory, 2);

        try (PooledResource<FakeConnection> handle = pool.borrow()) {
            assertEquals(1, pool.borrowedCount());
            assertTrue(handle.get().valid);
        }
        assertEquals(0, pool.borrowedCount(), "resource auto-returned by try-with-resources");
        assertEquals(1, pool.idleCount());
    }

    @Test
    void doubleCloseIsANoOp() throws InterruptedException {
        FakeConnectionFactory factory = new FakeConnectionFactory();
        ConnectionPool<FakeConnection> pool = pool(factory, 1);
        PooledResource<FakeConnection> h = pool.borrow();
        h.close();
        h.close(); // must not release a second permit
        assertEquals(0, pool.borrowedCount());
        assertEquals(1, pool.idleCount());
    }

    @Test
    void shutdownClosesIdleResourcesAndRejectsNewBorrows() throws InterruptedException {
        FakeConnectionFactory factory = new FakeConnectionFactory();
        ConnectionPool<FakeConnection> pool = pool(factory, 2);

        PooledResource<FakeConnection> h = pool.borrow();
        FakeConnection borrowed = h.get();
        h.close(); // now idle

        pool.shutdown();
        assertTrue(pool.isClosed());
        assertTrue(borrowed.closed, "idle resources are closed on shutdown");
        assertThrows(PoolClosedException.class, pool::borrow);
    }

    @Test
    void resourceReturnedAfterShutdownIsClosedNotPooled() throws InterruptedException {
        FakeConnectionFactory factory = new FakeConnectionFactory();
        ConnectionPool<FakeConnection> pool = pool(factory, 2);

        PooledResource<FakeConnection> h = pool.borrow();
        FakeConnection borrowed = h.get();
        pool.shutdown();
        h.close(); // returning into a closed pool

        assertTrue(borrowed.closed);
        assertEquals(0, pool.idleCount());
        assertFalse(pool.isClosed() && pool.idleCount() > 0);
    }
}
