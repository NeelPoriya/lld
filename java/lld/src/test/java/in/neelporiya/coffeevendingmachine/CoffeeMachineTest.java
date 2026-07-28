package in.neelporiya.coffeevendingmachine;

import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoffeeMachineTest {

    private final MutableClock clock = MutableClock.atEpoch();

    private CoffeeMachine machineForLattes(int lattes) {
        return CoffeeMachine.builder()
                .clock(clock)
                .outlets(1)
                .refill(Ingredient.WATER, 30 * lattes)
                .refill(Ingredient.MILK, 150 * lattes)
                .refill(Ingredient.COFFEE_BEANS, 18 * lattes)
                .addRecipe(BeverageFactory.latte())
                .build();
    }

    @Test
    void brewSucceedsAndConsumesIngredientsAndRecordsRevenue() {
        CoffeeMachine machine = machineForLattes(1);

        BrewResult result = machine.brew("Latte");

        assertTrue(result.success());
        assertEquals("Latte", result.beverage());
        assertEquals(0, machine.inventory().quantityOf(Ingredient.WATER));
        assertEquals(0, machine.inventory().quantityOf(Ingredient.MILK));
        assertEquals(220, machine.revenueCents());
        assertEquals(1, machine.servedBeverages().size());
    }

    @Test
    void unknownBeverageFails() {
        CoffeeMachine machine = machineForLattes(1);
        BrewResult result = machine.brew("Mocha");
        assertFalse(result.success());
        assertEquals("unknown beverage", result.failureReason());
    }

    @Test
    void insufficientIngredientsFailsAndConsumesNothing() {
        CoffeeMachine machine = CoffeeMachine.builder()
                .clock(clock)
                .refill(Ingredient.WATER, 30)
                .refill(Ingredient.MILK, 10) // not enough for a latte (needs 150)
                .refill(Ingredient.COFFEE_BEANS, 18)
                .addRecipe(BeverageFactory.latte())
                .build();

        BrewResult result = machine.brew("Latte");

        assertFalse(result.success());
        assertEquals("insufficient ingredients", result.failureReason());
        // All-or-nothing: the water/beans that WERE available must be untouched.
        assertEquals(30, machine.inventory().quantityOf(Ingredient.WATER));
        assertEquals(18, machine.inventory().quantityOf(Ingredient.COFFEE_BEANS));
    }

    @Test
    void lowInventoryListenerFiresWhenTankDropsBelowThreshold() {
        List<Ingredient> lowAlerts = new ArrayList<>();
        CoffeeMachine machine = CoffeeMachine.builder()
                .clock(clock)
                .lowInventoryThreshold(50)
                .refill(Ingredient.WATER, 60)          // 60 - 30 = 30 <= 50 -> alert
                .refill(Ingredient.COFFEE_BEANS, 100)  // 100 - 18 = 82 > 50 -> no alert
                .addRecipe(BeverageFactory.espresso())
                .addLowInventoryListener((ingredient, remaining) -> lowAlerts.add(ingredient))
                .build();

        machine.brew("Espresso");

        assertEquals(List.of(Ingredient.WATER), lowAlerts);
    }

    @Test
    void servedTimestampsComeFromInjectedClock() {
        CoffeeMachine machine = machineForLattes(2);

        machine.brew("Latte");
        clock.advance(Duration.ofHours(1));
        machine.brew("Latte");

        assertEquals(Instant.EPOCH, machine.servedBeverages().get(0).servedAt());
        assertEquals(Instant.EPOCH.plus(Duration.ofHours(1)), machine.servedBeverages().get(1).servedAt());
    }
}
