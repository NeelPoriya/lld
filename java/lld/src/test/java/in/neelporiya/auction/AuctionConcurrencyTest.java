package in.neelporiya.auction;

import in.neelporiya.auction.exception.InvalidBidException;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // CONCURRENCY: many bidders hit the same item simultaneously. The per-auction lock must ensure
 * the final highest bid equals the global maximum attempted (no lost update), and that the highest
 * only ever increases.
 */
class AuctionConcurrencyTest {

    @Test
    void concurrentBiddingConvergesToTheGlobalMaximum() throws InterruptedException {
        MutableClock clock = MutableClock.at(Instant.EPOCH.plusSeconds(10)); // inside the window
        AtomicInteger seq = new AtomicInteger();
        AuctionService service = new AuctionService(clock, () -> "a" + seq.incrementAndGet());
        Auction auction = service.createAuction("Rare Coin", "seller", 10_000, 0, 100,
                Instant.EPOCH, Instant.EPOCH.plusSeconds(1_000));

        int bidders = 200;
        long maxAmount = 10_000 + (long) bidders * 100; // 30_000

        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(bidders);
        AtomicInteger accepted = new AtomicInteger();

        for (int i = 1; i <= bidders; i++) {
            long amount = 10_000 + (long) i * 100; // distinct, spaced by the increment
            String bidder = "bidder-" + i;
            pool.submit(() -> {
                try {
                    startGun.await();
                    service.placeBid(auction.getId(), bidder, amount);
                    accepted.incrementAndGet();
                } catch (InvalidBidException outbidByHigher) {
                    // Expected: a lower bid submitted after a higher one is rejected.
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

        Bid highest = auction.getHighestBid().orElseThrow();
        assertEquals(maxAmount, highest.amountCents(), "highest bid must equal the global maximum");
        assertEquals("bidder-" + bidders, highest.bidderId());
        assertTrue(accepted.get() >= 1);
    }
}
