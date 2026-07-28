package in.neelporiya.movieticket;

/** // DESIGN PATTERN: Strategy. Pricing can vary by seat type, show time, demand, or coupon. */
public interface SeatPricingStrategy {
    long priceCents(Show show, Seat seat);
}
