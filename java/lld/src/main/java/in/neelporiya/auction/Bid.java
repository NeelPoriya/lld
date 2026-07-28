package in.neelporiya.auction;

import java.time.Instant;

/** A single bid. Amount is integer cents (never a floating-point currency value). */
public record Bid(String bidderId, long amountCents, Instant at) {
}
