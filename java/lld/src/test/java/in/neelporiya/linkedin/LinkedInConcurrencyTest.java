package in.neelporiya.linkedin;

import in.neelporiya.linkedin.model.Member;
import in.neelporiya.linkedin.model.Profile;
import in.neelporiya.linkedin.service.LinkedInService;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // CONCURRENCY: many distinct members endorse the same skill at the same instant, and every thread
 * double-clicks. The target member's concurrent endorsement set must count each endorser exactly once.
 */
class LinkedInConcurrencyTest {

    @Test
    void concurrentEndorsementsAreCountedExactlyOncePerDistinctEndorser() throws InterruptedException {
        AtomicInteger seq = new AtomicInteger();
        LinkedInService service =
                new LinkedInService(MutableClock.atEpoch(), () -> "id-" + seq.incrementAndGet());
        Member target = service.registerMember("target", Profile.builder().headline("Engineer").addSkill("java").build());

        int endorserCount = 300;
        List<Member> endorsers = new ArrayList<>();
        for (int i = 0; i < endorserCount; i++) {
            endorsers.add(service.registerMember("endorser-" + i, Profile.builder().headline("Peer").addSkill("java").build()));
        }

        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(endorserCount);

        for (Member endorser : endorsers) {
            pool.submit(() -> {
                try {
                    startGun.await();
                    service.endorseSkill(endorser, target, "java");
                    service.endorseSkill(endorser, target, "java");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        startGun.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS));
        pool.shutdownNow();

        assertEquals(endorserCount, target.endorsementCount("java"), "each distinct endorser must count exactly once");
    }
}
