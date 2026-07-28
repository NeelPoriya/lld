package in.neelporiya.coffeevendingmachine;

import java.util.Map;

/**
 * // DESIGN PATTERN: Factory — builds the standard menu so callers don't hand-assemble ingredient
 * maps. New standard drinks are added here in one place.
 */
public final class BeverageFactory {

    private BeverageFactory() {
    }

    public static Recipe espresso() {
        return new Recipe("Espresso",
                Map.of(Ingredient.WATER, 30, Ingredient.COFFEE_BEANS, 18),
                150);
    }

    public static Recipe latte() {
        return new Recipe("Latte",
                Map.of(Ingredient.WATER, 30, Ingredient.MILK, 150, Ingredient.COFFEE_BEANS, 18),
                220);
    }

    public static Recipe cappuccino() {
        return new Recipe("Cappuccino",
                Map.of(Ingredient.WATER, 30, Ingredient.MILK, 100, Ingredient.COFFEE_BEANS, 18, Ingredient.SUGAR, 5),
                200);
    }
}
