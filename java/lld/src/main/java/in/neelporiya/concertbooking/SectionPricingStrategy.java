package in.neelporiya.concertbooking;

/**
 * // DESIGN PATTERN: Strategy. Pricing can be fixed-by-section today and dynamic/surge tomorrow.
 */
public interface SectionPricingStrategy {
    long priceCents(Concert concert, Section section, Seat seat);
}
