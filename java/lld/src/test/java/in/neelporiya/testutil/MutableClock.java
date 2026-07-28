package in.neelporiya.testutil;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * A {@link Clock} whose "now" can be moved forward on demand.
 *
 * <p>// TESTABILITY: This is the single most reused testing trick in this whole repository. Any code
 * that depends on time (TTL caches, auctions, schedulers, rate limiters, parking fees) should take a
 * {@code Clock} in its constructor and read {@code clock.instant()} instead of calling
 * {@code Instant.now()}. Production wires in {@code Clock.systemUTC()}; tests wire in this
 * {@code MutableClock} and call {@link #advance(Duration)} to jump 3 hours (or 30 days) forward in
 * <em>microseconds</em> — no {@code Thread.sleep}, no flakiness, fully deterministic.
 *
 * <p>Thread-safe so it can be shared by concurrency tests.
 */
public final class MutableClock extends Clock {

    private final ZoneId zone;
    private Instant instant;

    public MutableClock(Instant start, ZoneId zone) {
        this.instant = start;
        this.zone = zone;
    }

    public static MutableClock at(Instant start) {
        return new MutableClock(start, ZoneOffset.UTC);
    }

    public static MutableClock atEpoch() {
        return at(Instant.EPOCH);
    }

    public synchronized void advance(Duration duration) {
        this.instant = this.instant.plus(duration);
    }

    public synchronized void setInstant(Instant newInstant) {
        this.instant = newInstant;
    }

    @Override
    public synchronized Instant instant() {
        return instant;
    }

    @Override
    public synchronized long millis() {
        return instant.toEpochMilli();
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId newZone) {
        return new MutableClock(instant, newZone);
    }
}
