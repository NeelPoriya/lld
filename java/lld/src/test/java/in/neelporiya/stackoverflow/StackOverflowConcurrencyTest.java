package in.neelporiya.stackoverflow;

import in.neelporiya.stackoverflow.model.Answer;
import in.neelporiya.stackoverflow.model.Question;
import in.neelporiya.stackoverflow.model.User;
import in.neelporiya.stackoverflow.model.VoteType;
import in.neelporiya.stackoverflow.service.StackOverflowService;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // CONCURRENCY: many distinct users upvote the same answer at the same instant. The per-post lock
 * must produce an exact final score, and the author's atomic reputation must equal 10 × voters.
 */
class StackOverflowConcurrencyTest {

    @Test
    void concurrentUpvotesOnSameAnswerAreCountedExactlyOnce() throws InterruptedException {
        AtomicInteger seq = new AtomicInteger();
        StackOverflowService service =
                new StackOverflowService(MutableClock.atEpoch(), () -> "id-" + seq.incrementAndGet());

        User author = service.registerUser("author");
        Question q = service.postQuestion(author, "hot", "trending", Set.of("java"));
        User answerer = service.registerUser("answerer");
        Answer answer = service.postAnswer(answerer, q, "the answer");

        int voterCount = 300;
        List<User> voters = new java.util.ArrayList<>();
        for (int i = 0; i < voterCount; i++) {
            voters.add(service.registerUser("voter-" + i));
        }

        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(voterCount);

        for (User voter : voters) {
            pool.submit(() -> {
                try {
                    startGun.await();
                    // Each voter votes UP a few times; idempotency must keep it counted once.
                    service.vote(voter, answer, VoteType.UP);
                    service.vote(voter, answer, VoteType.UP);
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

        assertEquals(voterCount, answer.getScore(), "each distinct voter must count exactly once");
        assertEquals(10 * voterCount, answerer.getReputation(), "reputation must equal 10 per upvote");
    }
}
