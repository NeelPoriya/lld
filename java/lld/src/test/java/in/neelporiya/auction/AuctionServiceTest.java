package in.neelporiya.auction;

import in.neelporiya.auction.exception.AuctionNotActiveException;
import in.neelporiya.auction.exception.InvalidBidException;
import in.neelporiya.auction.observer.AuctionListener;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionServiceTest {

    private static final Instant START = Instant.EPOCH.plusSeconds(10);
    private static final Instant END = Instant.EPOCH.plusSeconds(100);

    private MutableClock clock;
    private AuctionService service;

    @BeforeEach
    void setUp() {
        clock = MutableClock.atEpoch();
        AtomicInteger seq = new AtomicInteger();
        service = new AuctionService(clock, () -> "a" + seq.incrementAndGet());
    }

    private Auction auction(long reserve) {
        return service.createAuction("Painting", "seller", 10_000, reserve, 100, START, END);
    }

    @Test
    void bidBeforeStartIsRejected() {
        Auction a = auction(12_000);
        assertEquals(AuctionStatus.SCHEDULED, service.statusOf(a.getId()));
        assertThrows(AuctionNotActiveException.class, () -> service.placeBid(a.getId(), "alice", 10_500));
    }

    @Test
    void firstBidMustMeetStartingPrice() {
        Auction a = auction(12_000);
        clock.setInstant(START);
        assertThrows(InvalidBidException.class, () -> service.placeBid(a.getId(), "alice", 9_000));
        service.placeBid(a.getId(), "alice", 10_000); // exactly starting price is allowed
        assertEquals(10_000, a.getHighestBid().orElseThrow().amountCents());
    }

    @Test
    void bidMustBeatHighestByIncrement() {
        Auction a = auction(12_000);
        clock.setInstant(START);
        service.placeBid(a.getId(), "alice", 10_000);
        assertThrows(InvalidBidException.class, () -> service.placeBid(a.getId(), "bob", 10_050));
        service.placeBid(a.getId(), "bob", 10_100);
        assertEquals("bob", a.getHighestBid().orElseThrow().bidderId());
    }

    @Test
    void previousHighBidderIsNotifiedOfOutbid() {
        List<String> outbid = new ArrayList<>();
        service.addListener(new AuctionListener() {
            @Override
            public void onOutbid(String outbidBidderId, Auction auction) {
                outbid.add(outbidBidderId);
            }
        });
        Auction a = auction(12_000);
        clock.setInstant(START);
        service.placeBid(a.getId(), "alice", 10_000);
        service.placeBid(a.getId(), "bob", 10_100);

        assertEquals(List.of("alice"), outbid);
    }

    @Test
    void bidAfterEndIsRejected() {
        Auction a = auction(12_000);
        clock.setInstant(END);
        assertEquals(AuctionStatus.CLOSED, service.statusOf(a.getId()));
        assertThrows(AuctionNotActiveException.class, () -> service.placeBid(a.getId(), "alice", 10_500));
    }

    @Test
    void winnerDeterminedWhenReserveMet() {
        List<Bid> winners = new ArrayList<>();
        service.addListener(new AuctionListener() {
            @Override
            public void onClosed(Auction auction, Bid winningBid) {
                winners.add(winningBid);
            }
        });
        Auction a = auction(12_000);
        clock.setInstant(START);
        service.placeBid(a.getId(), "alice", 12_500);

        clock.setInstant(END);
        service.closeExpiredAuctions();

        assertTrue(a.isClosed());
        assertEquals("alice", a.getWinningBid().orElseThrow().bidderId());
        assertEquals(1, winners.size());
        assertEquals(12_500, winners.get(0).amountCents());
    }

    @Test
    void noWinnerWhenReserveNotMet() {
        Auction a = auction(20_000); // reserve above any bid
        clock.setInstant(START);
        service.placeBid(a.getId(), "alice", 12_500);

        clock.setInstant(END);
        service.closeExpiredAuctions();

        assertTrue(a.isClosed());
        assertFalse(a.getWinningBid().isPresent(), "top bid below reserve => no winner");
    }

    @Test
    void closeIsIdempotent() {
        Auction a = auction(12_000);
        clock.setInstant(START);
        service.placeBid(a.getId(), "alice", 12_500);
        clock.setInstant(END);
        service.closeExpiredAuctions();
        service.closeExpiredAuctions(); // second sweep must not re-fire / change anything
        assertEquals("alice", a.getWinningBid().orElseThrow().bidderId());
    }
}
