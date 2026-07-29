package in.neelporiya.onlineshopping;

import java.util.List;

/** // DESIGN PATTERN: Strategy — discounts/taxes are swappable without editing checkout. */
public interface PricingStrategy {
    long totalCents(List<CartItem> items);
}
