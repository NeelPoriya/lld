package in.neelporiya.coffeevendingmachine.observer;

import in.neelporiya.coffeevendingmachine.Ingredient;

/**
 * // DESIGN PATTERN: Observer — notified when a tank falls to/below its low-water mark so an
 * operator or telemetry system can refill. The inventory doesn't know or care who is listening.
 */
public interface LowInventoryListener {
    void onLow(Ingredient ingredient, int remaining);
}
