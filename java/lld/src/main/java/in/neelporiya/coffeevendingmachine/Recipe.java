package in.neelporiya.coffeevendingmachine;

import java.util.Map;
import java.util.Objects;

/**
 * A drink recipe: the exact ingredient amounts it needs and its price.
 *
 * <p>// EXTENSIBILITY: a recipe is pure data. Adding a new drink to the menu means creating a
 * {@code Recipe} — no new classes, no changes to the machine. Money is integer cents, never a
 * {@code double}.
 */
public record Recipe(String name, Map<Ingredient, Integer> ingredients, int priceCents) {

    public Recipe {
        Objects.requireNonNull(name, "name");
        // Defensive immutable copy so a caller can't mutate the recipe after registering it.
        ingredients = Map.copyOf(ingredients);
        if (priceCents < 0) {
            throw new IllegalArgumentException("priceCents must be >= 0");
        }
    }
}
