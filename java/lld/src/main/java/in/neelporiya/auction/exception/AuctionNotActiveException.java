package in.neelporiya.auction.exception;

/** Thrown when a bid is placed on an auction that is not currently ACTIVE. */
public class AuctionNotActiveException extends RuntimeException {
    public AuctionNotActiveException(String message) {
        super(message);
    }
}
