package in.neelporiya.auction.observer;

import in.neelporiya.auction.Auction;
import in.neelporiya.auction.Bid;

/**
 * // DESIGN PATTERN: Observer — bid confirmations, outbid alerts and "you won" emails react to
 * auction events without the auction/service knowing who is listening.
 */
public interface AuctionListener {

    default void onBid(Auction auction, Bid bid) {
    }

    default void onOutbid(String outbidBidderId, Auction auction) {
    }

    /** @param winningBid the winner, or {@code null} if reserve was not met. */
    default void onClosed(Auction auction, Bid winningBid) {
    }
}
