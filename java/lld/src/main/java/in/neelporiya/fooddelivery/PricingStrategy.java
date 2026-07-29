package in.neelporiya.fooddelivery;

/**
 * // DESIGN PATTERN: Strategy — how a cart plus a delivery distance becomes a {@link Bill}. Surge
 * pricing, flat fee, free-delivery-over-X etc. slot in without touching order placement.
 */
public interface PricingStrategy {

    Bill price(Cart cart, double distance);
}
