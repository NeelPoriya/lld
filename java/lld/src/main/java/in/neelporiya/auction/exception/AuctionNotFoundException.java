package in.neelporiya.auction.exception;

/** Thrown when an auction id is unknown. */
public class AuctionNotFoundException extends RuntimeException {
    public AuctionNotFoundException(String message) {
        super(message);
    }
}
