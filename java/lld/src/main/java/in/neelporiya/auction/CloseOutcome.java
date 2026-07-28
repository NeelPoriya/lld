package in.neelporiya.auction;

/** Outcome of a close attempt: whether this call actually closed it, and the winning bid (nullable). */
public record CloseOutcome(boolean newlyClosed, Bid winningBid) {
}
