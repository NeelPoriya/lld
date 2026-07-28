package in.neelporiya.auction;

import in.neelporiya.auction.exception.AuctionNotFoundException;
import in.neelporiya.auction.observer.AuctionListener;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * // DESIGN PATTERN: Facade over auctions, notifications, and the clock.
 */
public class AuctionService {

    private final java.util.Map<String, Auction> auctions = new ConcurrentHashMap<>();
    private final List<AuctionListener> listeners = new CopyOnWriteArrayList<>();
    private final Clock clock;
    private final Supplier<String> idGenerator;

    public AuctionService(Clock clock, Supplier<String> idGenerator) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }

    public static AuctionService createDefault() {
        return new AuctionService(Clock.systemUTC(), () -> UUID.randomUUID().toString());
    }

    public void addListener(AuctionListener listener) {
        listeners.add(listener);
    }

    public Auction createAuction(String itemName, String sellerId, long startingPriceCents, long reserveCents,
                                 long minIncrementCents, Instant startTime, Instant endTime) {
        Auction auction = new Auction(idGenerator.get(), itemName, sellerId, startingPriceCents, reserveCents,
                minIncrementCents, startTime, endTime);
        auctions.put(auction.getId(), auction);
        return auction;
    }

    public PlaceBidResult placeBid(String auctionId, String bidderId, long amountCents) {
        Auction auction = require(auctionId);
        PlaceBidResult result = auction.placeBid(bidderId, amountCents, clock.instant());
        listeners.forEach(l -> l.onBid(auction, result.newHighest()));
        if (result.outbidBidderId() != null) {
            listeners.forEach(l -> l.onOutbid(result.outbidBidderId(), auction));
        }
        return result;
    }

    /** Force-close an auction now (e.g. seller ends it early). */
    public Bid closeAuction(String auctionId) {
        Auction auction = require(auctionId);
        CloseOutcome outcome = auction.close(clock.instant());
        if (outcome.newlyClosed()) {
            listeners.forEach(l -> l.onClosed(auction, outcome.winningBid()));
        }
        return outcome.winningBid();
    }

    /** Sweep and close every auction whose end time has passed. Idempotent. */
    public void closeExpiredAuctions() {
        Instant now = clock.instant();
        for (Auction auction : auctions.values()) {
            if (!now.isBefore(auction.getEndTime())) {
                CloseOutcome outcome = auction.close(now);
                if (outcome.newlyClosed()) {
                    listeners.forEach(l -> l.onClosed(auction, outcome.winningBid()));
                }
            }
        }
    }

    public AuctionStatus statusOf(String auctionId) {
        return require(auctionId).status(clock.instant());
    }

    public Auction getAuction(String auctionId) {
        return require(auctionId);
    }

    private Auction require(String auctionId) {
        Auction auction = auctions.get(auctionId);
        if (auction == null) {
            throw new AuctionNotFoundException("no auction " + auctionId);
        }
        return auction;
    }
}
