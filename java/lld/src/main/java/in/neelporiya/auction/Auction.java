package in.neelporiya.auction;

import in.neelporiya.auction.exception.AuctionNotActiveException;
import in.neelporiya.auction.exception.InvalidBidException;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A single auction listing.
 *
 * <p>// CONCURRENCY: {@code highestBid} is guarded by a {@link ReentrantLock}. {@link #placeBid}
 * validates the incoming amount against the current highest and installs the new highest as ONE
 * atomic step. The lost-update bug — two concurrent bids both reading the old highest and one
 * clobbering the other — is impossible because validate-and-set happen under the same lock.
 *
 * <p>// TESTABILITY: the ACTIVE window is decided from the {@code now} passed in (sourced from an
 * injected clock in the service), so tests move an auction through its lifecycle by advancing a fake
 * clock — no waiting for real time.
 */
public class Auction {

    private final String id;
    private final String itemName;
    private final String sellerId;
    private final long startingPriceCents;
    private final long reserveCents;
    private final long minIncrementCents;
    private final Instant startTime;
    private final Instant endTime;

    private final ReentrantLock lock = new ReentrantLock();
    private Bid highestBid;      // guarded by lock
    private boolean closed;      // guarded by lock
    private Bid winningBid;      // guarded by lock; set at close

    public Auction(String id, String itemName, String sellerId, long startingPriceCents, long reserveCents,
                   long minIncrementCents, Instant startTime, Instant endTime) {
        this.id = id;
        this.itemName = itemName;
        this.sellerId = sellerId;
        this.startingPriceCents = startingPriceCents;
        this.reserveCents = reserveCents;
        this.minIncrementCents = minIncrementCents;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public AuctionStatus status(Instant now) {
        lock.lock();
        try {
            if (closed || !now.isBefore(endTime)) {
                return AuctionStatus.CLOSED;
            }
            return now.isBefore(startTime) ? AuctionStatus.SCHEDULED : AuctionStatus.ACTIVE;
        } finally {
            lock.unlock();
        }
    }

    public PlaceBidResult placeBid(String bidderId, long amountCents, Instant now) {
        lock.lock();
        try {
            if (closed || !now.isBefore(endTime)) {
                throw new AuctionNotActiveException("auction " + id + " is closed");
            }
            if (now.isBefore(startTime)) {
                throw new AuctionNotActiveException("auction " + id + " has not started");
            }
            long minRequired = highestBid == null
                    ? startingPriceCents
                    : highestBid.amountCents() + minIncrementCents;
            if (amountCents < minRequired) {
                throw new InvalidBidException("bid " + amountCents + " < required " + minRequired);
            }
            String outbid = highestBid == null ? null : highestBid.bidderId();
            highestBid = new Bid(bidderId, amountCents, now);
            return new PlaceBidResult(highestBid, outbid);
        } finally {
            lock.unlock();
        }
    }

    public CloseOutcome close(Instant now) {
        lock.lock();
        try {
            if (closed) {
                return new CloseOutcome(false, winningBid);
            }
            closed = true;
            // Winner only if the top bid meets the reserve.
            winningBid = (highestBid != null && highestBid.amountCents() >= reserveCents) ? highestBid : null;
            return new CloseOutcome(true, winningBid);
        } finally {
            lock.unlock();
        }
    }

    public Optional<Bid> getHighestBid() {
        lock.lock();
        try {
            return Optional.ofNullable(highestBid);
        } finally {
            lock.unlock();
        }
    }

    public Optional<Bid> getWinningBid() {
        lock.lock();
        try {
            return Optional.ofNullable(winningBid);
        } finally {
            lock.unlock();
        }
    }

    public boolean isClosed() {
        lock.lock();
        try {
            return closed;
        } finally {
            lock.unlock();
        }
    }

    public String getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public String getSellerId() {
        return sellerId;
    }

    public Instant getEndTime() {
        return endTime;
    }
}
