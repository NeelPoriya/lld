package in.neelporiya.socialnetwork;

import in.neelporiya.socialnetwork.model.Post;
import in.neelporiya.socialnetwork.model.Profile;
import in.neelporiya.socialnetwork.model.User;
import in.neelporiya.socialnetwork.service.SocialNetworkService;
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
 * // CONCURRENCY: many threads like the same post at the same time, and each thread double-clicks.
 * The post's concurrent like set must count each distinct user exactly once.
 */
class SocialNetworkConcurrencyTest {

    @Test
    void concurrentLikesAreCountedExactlyOncePerDistinctUser() throws InterruptedException {
        AtomicInteger seq = new AtomicInteger();
        SocialNetworkService service =
                new SocialNetworkService(MutableClock.atEpoch(), () -> "id-" + seq.incrementAndGet());
        User author = service.registerUser(Profile.of("author", "", ""));
        Post post = service.createPost(author, "viral");

        int likerCount = 300;
        List<User> likers = new ArrayList<>();
        for (int i = 0; i < likerCount; i++) {
            likers.add(service.registerUser(Profile.of("liker-" + i, "", "")));
        }

        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(likerCount);

        for (User liker : likers) {
            pool.submit(() -> {
                try {
                    startGun.await();
                    service.likePost(liker, post);
                    service.likePost(liker, post);
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

        assertEquals(likerCount, post.likeCount(), "each distinct liker must count exactly once");
    }
}
