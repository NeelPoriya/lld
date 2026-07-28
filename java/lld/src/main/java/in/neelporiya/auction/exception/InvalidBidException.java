package in.neelporiya.auction.exception;

/** Thrown when a bid is too low (below starting price or below highest + increment). */
public class InvalidBidException extends RuntimeException {
    public InvalidBidException(String message) {
        super(message);
    }
}
