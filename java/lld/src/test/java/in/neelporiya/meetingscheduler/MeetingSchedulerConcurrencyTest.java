package in.neelporiya.meetingscheduler;

import in.neelporiya.meetingscheduler.exception.MeetingConflictException;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // CONCURRENCY: many organizers race to book the SAME room for the SAME slot. The multi-entity
 * check-and-insert is guarded by one lock, so exactly one booking must win and the rest must see a
 * conflict — no double-booking.
 */
class MeetingSchedulerConcurrencyTest {

    @Test
    void onlyOneOrganizerWinsWhenRacingForTheSameSlot() throws InterruptedException {
        MutableClock clock = MutableClock.atEpoch();
        AtomicInteger seq = new AtomicInteger();
        MeetingScheduler scheduler = new MeetingScheduler(clock, () -> "id" + seq.incrementAndGet());
        Room room = scheduler.addRoom("Boardroom", 100);
        TimeInterval slot = new TimeInterval(Instant.EPOCH.plus(Duration.ofHours(10)),
                Instant.EPOCH.plus(Duration.ofHours(11)));

        int organizers = 64;
        ExecutorService pool = Executors.newFixedThreadPool(organizers);
        CountDownLatch ready = new CountDownLatch(organizers);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(organizers);
        AtomicInteger wins = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();

        for (int i = 0; i < organizers; i++) {
            String who = "user" + i;
            pool.execute(() -> {
                ready.countDown();
                try {
                    go.await();
                    scheduler.book(room.id(), slot, who, Set.of(who), "Race");
                    wins.incrementAndGet();
                } catch (MeetingConflictException conflict) {
                    conflicts.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        assertTrue(ready.await(5, TimeUnit.SECONDS));
        go.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS));
        pool.shutdownNow();

        assertEquals(1, wins.get(), "exactly one organizer books the slot");
        assertEquals(organizers - 1, conflicts.get(), "everyone else is rejected");
    }
}
