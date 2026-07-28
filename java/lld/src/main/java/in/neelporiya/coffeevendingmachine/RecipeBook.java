package in.neelporiya.coffeevendingmachine;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** The machine's menu: recipes keyed by (case-insensitive) name. Insertion order preserved. */
public class RecipeBook {

    private final Map<String, Recipe> recipes = new LinkedHashMap<>();

    public void add(Recipe recipe) {
        recipes.put(recipe.name().toLowerCase(), recipe);
    }

    public Optional<Recipe> get(String name) {
        return Optional.ofNullable(recipes.get(name.toLowerCase()));
    }

    public Map<String, Recipe> all() {
        return Map.copyOf(recipes);
    }
}
