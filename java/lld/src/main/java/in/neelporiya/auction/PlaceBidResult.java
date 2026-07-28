package in.neelporiya.auction;

/** Result of a successful bid: the new highest bid and who (if anyone) was outbid by it. */
public record PlaceBidResult(Bid newHighest, String outbidBidderId) {
}
