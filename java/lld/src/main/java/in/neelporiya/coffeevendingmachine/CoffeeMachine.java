package in.neelporiya.coffeevendingmachine;

import in.neelporiya.coffeevendingmachine.observer.LowInventoryListener;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

/**
 * // DESIGN PATTERN: Facade over the outlets + inventory + menu. Built via a // DESIGN PATTERN:
 * Builder so tanks, menu, outlet count and clock are wired readably.
 *
 * <h2>Concurrency model</h2>
 * <ul>
 *   <li>A {@link Semaphore} sized to the number of physical outlets caps concurrent brews. When all
 *       outlets are busy, {@code brew} fails fast instead of blocking forever.</li>
 *   <li>Ingredient consumption is the atomic reservation inside {@link Inventory}.</li>
 *   <li>Revenue is an {@link AtomicLong}; the served log is copy-on-write.</li>
 * </ul>
 */
public class CoffeeMachine {

    private final Inventory inventory;
    private final RecipeBook menu;
    private final Semaphore outlets;
    private final Clock clock;

    private final List<ServedBeverage> served = new CopyOnWriteArrayList<>();
    private final AtomicLong revenueCents = new AtomicLong();

    private CoffeeMachine(Builder builder) {
        this.inventory = builder.inventory;
        this.menu = builder.menu;
        this.outlets = new Semaphore(builder.outlets, true); // fair: FIFO among waiting outlets
        this.clock = builder.clock;
    }

    /**
     * Brew a drink by name.
     *
     * @return a {@link BrewResult} describing success or the specific expected failure.
     */
    public BrewResult brew(String name) {
        Optional<Recipe> recipe = menu.get(name);
        if (recipe.isEmpty()) {
            return BrewResult.fail(name, "unknown beverage");
        }
        Recipe r = recipe.get();

        // CONCURRENCY: acquire an outlet permit (bounded parallelism). tryAcquire => fail fast.
        if (!outlets.tryAcquire()) {
            return BrewResult.fail(r.name(), "all outlets busy");
        }
        try {
            if (!inventory.tryConsume(r)) {
                return BrewResult.fail(r.name(), "insufficient ingredients");
            }
            // The (slow) physical brew conceptually happens HERE, outside the inventory lock, so
            // multiple outlets brew simultaneously once each has reserved its ingredients.
            revenueCents.addAndGet(r.priceCents());
            served.add(new ServedBeverage(r.name(), clock.instant()));
            return BrewResult.ok(r.name());
        } finally {
            outlets.release();
        }
    }

    public long revenueCents() {
        return revenueCents.get();
    }

    public List<ServedBeverage> servedBeverages() {
        return List.copyOf(served);
    }

    public Inventory inventory() {
        return inventory;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final java.util.Map<Ingredient, Integer> refills = new java.util.EnumMap<>(Ingredient.class);
        private final java.util.List<LowInventoryListener> lowListeners = new java.util.ArrayList<>();
        private final RecipeBook menu = new RecipeBook();
        private int lowThreshold = 50;
        private int outlets = 1;
        private Clock clock = Clock.systemUTC();

        public Builder lowInventoryThreshold(int threshold) {
            this.lowThreshold = threshold;
            return this;
        }

        public Builder outlets(int count) {
            if (count < 1) {
                throw new IllegalArgumentException("need at least one outlet");
            }
            this.outlets = count;
            return this;
        }

        public Builder clock(Clock clock) {
            this.clock = Objects.requireNonNull(clock, "clock");
            return this;
        }

        public Builder refill(Ingredient ingredient, int amount) {
            this.refills.merge(ingredient, amount, Integer::sum);
            return this;
        }

        public Builder addRecipe(Recipe recipe) {
            this.menu.add(recipe);
            return this;
        }

        public Builder addLowInventoryListener(LowInventoryListener listener) {
            this.lowListeners.add(listener);
            return this;
        }

        public CoffeeMachine build() {
            // Construct the Inventory once, so builder call order never matters.
            this.inventory = new Inventory(lowThreshold);
            lowListeners.forEach(inventory::addListener);
            refills.forEach(inventory::refill);
            return new CoffeeMachine(this);
        }

        private Inventory inventory;
    }
}
