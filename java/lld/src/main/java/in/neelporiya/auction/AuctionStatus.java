package in.neelporiya.auction;

public enum AuctionStatus {
    /** Before the start time. */
    SCHEDULED,
    /** Within the bidding window. */
    ACTIVE,
    /** After the end time, or explicitly closed. */
    CLOSED
}
